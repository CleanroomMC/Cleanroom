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

import java.nio.charset.StandardCharsets;
import java.util.Set;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.FMLLog;

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

/**
 * Redirects byte-array writes (and reads, for logging) on {@code Chunk.getBiomeArray()} to
 * {@code ChunkBiomes.storeByte}/{@code loadByte}. The accessor now returns a snapshot copy that is
 * registered to its chunk, so a tracked {@code BASTORE} reaches the chunk's full biome ids; a
 * tracked {@code BASTORE} preceded by {@code I2B} drops the {@code I2B} so the full int id survives.
 * Arrays that escape the method (passed to helpers, fields) are not tracked and their writes are lost;
 * {@code storeByte} falls back to a plain byte store when the array is not a known snapshot.
 * <p>
 * Mod jars are compiled against SRG names, so production bytecode calls
 * {@code func_76605_m} (and {@code func_76616_a} for {@code setBiomeArray}, which is not rewritten);
 * dev bytecode calls {@code getBiomeArray}. 1.12.2 SRG names are stable, so both spellings are
 * matched unconditionally - the remapper cannot be asked for the SRG name because it maps the other
 * way (obf -> SRG) and would return an MCP input unchanged. The owner is deliberately not checked:
 * javac emits the receiver's static type, so a {@code Chunk}-subclass-typed call would be missed, and a
 * false positive on an unrelated {@code getBiomeArray()[B} is harmless ({@code storeByte} falls back
 * to a plain store for an array that is not a snapshot).
 */
public class BiomeArrayTransformer implements IClassTransformer
{
    private static final String[] SKIP_PREFIXES =
    {
        "net.minecraft.", "net.minecraftforge.", "com.cleanroommc.", "java.", "javax.", "jdk.", "sun.",
        "org.objectweb.", "org.spongepowered.", "top.outlands.", "io.netty.", "org.apache.",
        "com.google.", "com.mojang.", "com.jcraft.", "com.google.common.", "joptsimple.", "org.lwjgl.",
        "paulscode.", "LZMA.", "scala.", "it.unimi.dsi.", "oshi.", "net.java.", "org.luaj."
    };
    private static final String GET_ARRAY_MCP = "getBiomeArray";
    private static final String GET_ARRAY_SRG = "func_76605_m"; // setBiomeArray is func_76616_a
    private static final Set<String> GET_ARRAY_NAMES = Set.of(GET_ARRAY_MCP, GET_ARRAY_SRG);
    private static final String GET_ARRAY_DESC = "()[B";
    private static final String OWNER = "com/cleanroommc/ids/ChunkBiomes";

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes)
    {
        if (bytes == null)
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
        if (!contains(bytes, GET_ARRAY_MCP) && !contains(bytes, GET_ARRAY_SRG))
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
                FMLLog.log.warn("BiomeArrayTransformer: could not analyze {}.{}{} ({}); leaving its biome array accesses alone", transformedName, method.name, method.desc, e.getMessage());
                continue;
            }

            // The analyzer frames line up with the instruction list's node indices; walk a fixed
            // snapshot of the list so removing/replacing nodes does not disturb the indexes.
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
                    FMLLog.log.debug("BiomeArrayTransformer: {}.{}{} insn {}: array value merges a getBiomeArray() result with something else; routing the store anyway (falls back to a plain byte store if it is not a snapshot)", transformedName, method.name, method.desc, index);
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
                    FMLLog.log.debug("BiomeArrayTransformer: {}.{}{} insn {}: BASTORE -> storeByte{}", transformedName, method.name, method.desc, index, hadI2b ? " (dropped I2B)" : "");
                }
                else
                {
                    method.instructions.set(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, OWNER, "loadByte", "([BI)I", false));
                    FMLLog.log.debug("BiomeArrayTransformer: {}.{}{} insn {}: BALOAD -> loadByte", transformedName, method.name, method.desc, index);
                }
                rewritten++;
            }
        }

        if (rewritten == 0)
        {
            return bytes;
        }
        FMLLog.log.info("BiomeArrayTransformer: redirected {} biome array accesses in {}", rewritten, transformedName);
        ClassWriter writer = new ClassWriter(0);
        classNode.accept(writer);
        return writer.toByteArray();
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

    /** The instructions that produced the array operand of a BALOAD or BASTORE. */
    private static Set<AbstractInsnNode> sourcesOfArray(Frame<SourceValue> frame, int opcode)
    {
        int depth = opcode == Opcodes.BASTORE ? 3 : 2;
        int size = frame.getStackSize();
        if (size < depth)
        {
            return java.util.Collections.emptySet();
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

    /** Cheap constant-pool-free scan: does the class file mention the method name at all? */
    private static boolean contains(byte[] bytes, String needle)
    {
        byte[] utf8 = needle.getBytes(StandardCharsets.UTF_8);
        outer:
        for (int i = 0; i + utf8.length <= bytes.length; i++)
        {
            for (int j = 0; j < utf8.length; j++)
            {
                if (bytes[i + j] != utf8[j])
                {
                    continue outer;
                }
            }
            return true;
        }
        return false;
    }
}
