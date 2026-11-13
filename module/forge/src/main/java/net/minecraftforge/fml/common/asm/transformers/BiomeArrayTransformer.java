/*
 * Minecraft Forge
 * Copyright (c) 2016-2020.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.minecraftforge.fml.common.asm.transformers;

import com.cleanroommc.util.CleanroomLog;
import top.outlands.foundation.boot.LoadingContext;

import java.lang.classfile.constantpool.PoolEntry;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.jar.Manifest;

import net.minecraft.launchwrapper.IClassTransformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.SourceInterpreter;
import org.objectweb.asm.tree.analysis.SourceValue;
import org.spongepowered.asm.util.Constants.ManifestAttributes;

/**
 * Redirects byte-array writes (and reads, for logging) on {@code Chunk.getBiomeArray()} to
 * {@code ChunkBiomes.storeByte}/{@code loadByte} for legacy Forge mods. Follows {@code CompatibilityTransformer}
 * by skipping Cleanroom-native mods and scanning the raw constant pool before parsing with ASM.
 */
public class BiomeArrayTransformer implements IClassTransformer
{
    private static final String[] SKIP_PREFIXES =
    {
        "net.minecraft.", "net.minecraftforge.", "com.cleanroommc.", "java.", "javax.", "jakarta.", "jdk.", "sun.",
        "org.objectweb.", "org.spongepowered.", "top.outlands.", "zone.rong.", "io.netty.", "org.apache.",
        "com.google.", "com.mojang.", "com.jcraft.", "joptsimple.", "org.lwjgl.", "org.joml.", "org.slf4j.",
        "paulscode.", "LZMA.", "scala.", "kotlin.", "it.unimi.dsi.", "oshi.", "net.java.", "org.luaj."
    };
    private static final String GET_ARRAY_MCP = "getBiomeArray";
    private static final String GET_ARRAY_SRG = "func_76605_m";
    private static final Set<String> GET_ARRAY_NAMES = Set.of(GET_ARRAY_MCP, GET_ARRAY_SRG);
    private static final String GET_ARRAY_DESC = "()[B";
    private static final byte[] MCP_BYTES = GET_ARRAY_MCP.getBytes(StandardCharsets.US_ASCII);
    private static final byte[] SRG_BYTES = GET_ARRAY_SRG.getBytes(StandardCharsets.US_ASCII);
    private static final byte[] DESC_BYTES = GET_ARRAY_DESC.getBytes(StandardCharsets.US_ASCII);
    private static final int CONSTANT_POOL_COUNT_OFFSET = 8;
    private static final int CONSTANT_POOL_OFFSET = 10;
    private static final String OWNER = "com/cleanroommc/cleanroom/common/id/ChunkBiomes";

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes)
    {
        if (bytes == null || isFromCleanroomMod())
        {
            return bytes;
        }
        for (String prefix : SKIP_PREFIXES)
        {
            if (name.startsWith(prefix) || transformedName.startsWith(prefix))
            {
                return bytes;
            }
        }
        if (!hasBiomeArrayConstantPoolEntries(bytes))
        {
            return bytes;
        }

        ClassNode classNode = new ClassNode();
        new ClassReader(bytes).accept(classNode, 0);

        int rewritten = 0;
        for (MethodNode method : classNode.methods)
        {
            boolean callsGetter = false;
            for (AbstractInsnNode insn : method.instructions.toArray())
            {
                if (isGetArrayCall(insn))
                {
                    callsGetter = true;
                    break;
                }
            }
            if (!callsGetter)
            {
                continue;
            }

            Frame<SourceValue>[] frames;
            try
            {
                frames = new Analyzer<>(new SourceInterpreter()).analyze(classNode.name, method);
            }
            catch (AnalyzerException e)
            {
                CleanroomLog.get().warn("BiomeArrayTransformer: could not analyze {}.{}{} ({}); leaving its biome array accesses alone", transformedName, method.name, method.desc, e.getMessage());
                continue;
            }

            AbstractInsnNode[] insns = method.instructions.toArray();
            for (int index = 0; index < insns.length; index++)
            {
                AbstractInsnNode insn = insns[index];
                int opcode = insn.getOpcode();
                if (opcode != Opcodes.BASTORE && opcode != Opcodes.BALOAD)
                {
                    continue;
                }
                Frame<SourceValue> frame = frames[index];
                if (frame == null)
                {
                    continue;
                }
                Set<AbstractInsnNode> sources = sourcesOfArray(frame, opcode);
                if (sources.isEmpty())
                {
                    continue;
                }
                boolean allTracked = true;
                for (AbstractInsnNode source : sources)
                {
                    if (!isGetArrayCall(source))
                    {
                        allTracked = false;
                    }
                }
                if (!allTracked && opcode == Opcodes.BALOAD)
                {
                    continue;
                }
                if (!allTracked)
                {
                    CleanroomLog.get().debug("BiomeArrayTransformer: {}.{}{} insn {}: array value merges a getBiomeArray() result with something else; routing the store anyway (falls back to a plain byte store if it is not a snapshot)", transformedName, method.name, method.desc, index);
                }

                if (opcode == Opcodes.BASTORE)
                {
                    AbstractInsnNode prev = previousRealInsn(insn);
                    boolean hadI2b = prev != null && prev.getOpcode() == Opcodes.I2B;
                    if (hadI2b)
                    {
                        method.instructions.remove(prev);
                    }
                    method.instructions.set(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, OWNER, "storeByte", "([BII)V", false));
                    CleanroomLog.get().debug("BiomeArrayTransformer: {}.{}{} insn {}: BASTORE -> storeByte{}", transformedName, method.name, method.desc, index, hadI2b ? " (dropped I2B)" : "");
                }
                else
                {
                    method.instructions.set(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, OWNER, "loadByte", "([BI)I", false));
                    CleanroomLog.get().debug("BiomeArrayTransformer: {}.{}{} insn {}: BALOAD -> loadByte", transformedName, method.name, method.desc, index);
                }
                rewritten++;
            }
        }

        if (rewritten == 0)
        {
            return bytes;
        }
        CleanroomLog.get().info("BiomeArrayTransformer: redirected {} biome array accesses in {}", rewritten, transformedName);
        ClassWriter writer = new ClassWriter(0);
        classNode.accept(writer);
        return writer.toByteArray();
    }

    private static boolean isFromCleanroomMod()
    {
        LoadingContext context = LoadingContext.current();
        Manifest manifest = context == null ? null : context.manifest();
        return manifest != null &&
            ManifestAttributes.CLEANROOMMODTYPE.equals(manifest.getMainAttributes().getValue(ManifestAttributes.MODTYPE));
    }

    private static boolean isGetArrayCall(AbstractInsnNode insn)
    {
        if (insn.getType() != AbstractInsnNode.METHOD_INSN)
        {
            return false;
        }
        MethodInsnNode call = (MethodInsnNode)insn;
        return (call.getOpcode() == Opcodes.INVOKEVIRTUAL || call.getOpcode() == Opcodes.INVOKESPECIAL || call.getOpcode() == Opcodes.INVOKEINTERFACE)
                && GET_ARRAY_DESC.equals(call.desc) && GET_ARRAY_NAMES.contains(call.name);
    }

    private static Set<AbstractInsnNode> sourcesOfArray(Frame<SourceValue> frame, int opcode)
    {
        int depth = opcode == Opcodes.BASTORE ? 3 : 2;
        int size = frame.getStackSize();
        if (size < depth)
        {
            return Collections.emptySet();
        }
        return frame.getStack(size - depth).insns;
    }

    private static AbstractInsnNode previousRealInsn(AbstractInsnNode insn)
    {
        AbstractInsnNode prev = insn.getPrevious();
        while (prev != null && (prev.getType() == AbstractInsnNode.LABEL || prev.getType() == AbstractInsnNode.LINE || prev.getType() == AbstractInsnNode.FRAME))
        {
            prev = prev.getPrevious();
        }
        return prev;
    }

    private static boolean hasBiomeArrayConstantPoolEntries(byte[] bytes)
    {
        try
        {
            int entryCount = (bytes[CONSTANT_POOL_COUNT_OFFSET] & 0xFF) << 8 | bytes[CONSTANT_POOL_COUNT_OFFSET + 1] & 0xFF;
            int offset = CONSTANT_POOL_OFFSET;
            boolean hasName = false;
            boolean hasDesc = false;
            for (int entry = 1; entry < entryCount; entry++)
            {
                switch (bytes[offset])
                {
                    case PoolEntry.TAG_UTF8 ->
                    {
                        int length = (bytes[offset + 1] & 0xFF) << 8 | bytes[offset + 2] & 0xFF;
                        offset += 3;
                        if (length == DESC_BYTES.length && Arrays.equals(bytes, offset, offset + length, DESC_BYTES, 0, DESC_BYTES.length))
                        {
                            hasDesc = true;
                        }
                        else if (length == MCP_BYTES.length && Arrays.equals(bytes, offset, offset + length, MCP_BYTES, 0, MCP_BYTES.length))
                        {
                            hasName = true;
                        }
                        else if (length == SRG_BYTES.length && Arrays.equals(bytes, offset, offset + length, SRG_BYTES, 0, SRG_BYTES.length))
                        {
                            hasName = true;
                        }
                        if (hasName && hasDesc)
                        {
                            return true;
                        }
                        offset += length;
                    }
                    case PoolEntry.TAG_INTEGER, PoolEntry.TAG_FLOAT, PoolEntry.TAG_FIELDREF, PoolEntry.TAG_METHODREF,
                        PoolEntry.TAG_INTERFACE_METHODREF, PoolEntry.TAG_NAME_AND_TYPE, PoolEntry.TAG_DYNAMIC,
                        PoolEntry.TAG_INVOKE_DYNAMIC -> offset += 5;
                    case PoolEntry.TAG_LONG, PoolEntry.TAG_DOUBLE ->
                    {
                        offset += 9;
                        entry++;
                    }
                    case PoolEntry.TAG_CLASS, PoolEntry.TAG_STRING, PoolEntry.TAG_METHOD_TYPE, PoolEntry.TAG_MODULE,
                        PoolEntry.TAG_PACKAGE -> offset += 3;
                    case PoolEntry.TAG_METHOD_HANDLE -> offset += 4;
                    default ->
                    {
                        return false;
                    }
                }
            }
        }
        catch (ArrayIndexOutOfBoundsException ignored)
        {
        }
        return false;
    }
}
