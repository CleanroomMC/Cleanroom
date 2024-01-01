package net.minecraftforge.fml.common.asm.transformers;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class URLClassLoaderTransformer implements IClassTransformer
{
    //private static final List<String> WHITELIST = List.of("com.cleanroommc.hackery", "org.spongepowered", "net.minecraft", "com.google", "com.ibm.icu", "io.netty", "com.sun", "it.unimi.dsi", "oshi", "org.slf4j", "com.mojang", "zone.rong", "kotlin");

    @Override
    public byte[] transform(String s, String s1, byte[] bytes)
    {
        if (bytes == null)
        {
            return null;
        }

        if (!s1.endsWith("ClassWriter"))
        {
            return bytes;
        }


        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);
        boolean modified = false;
        if (classNode.methods != null)
        {
            for (MethodNode methodNode : classNode.methods)
            {
                InsnList instructions = methodNode.instructions;
                if (instructions != null)
                {
                    for (AbstractInsnNode insnNode : instructions)
                    {
                        if (insnNode.getOpcode() == Opcodes.CHECKCAST && insnNode instanceof TypeInsnNode typeInsnNode)
                        {
                            if ("java/net/URLClassLoader".equals(typeInsnNode.desc))
                            {
                                AbstractInsnNode next = instructions.get(instructions.indexOf(typeInsnNode) + 1);
                                if (next instanceof MethodInsnNode nextInsnNode && next.getOpcode() == Opcodes.INVOKEVIRTUAL)
                                {
                                    if ("java/net/URLClassLoader".equals(nextInsnNode.owner) && "getURLs".equals(nextInsnNode.name))
                                    {
                                        instructions.insertBefore(insnNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/cleanroommc/hackery/ReflectionHackery", "getURL", "(Ljava/lang/ClassLoader;)[Ljava/net/URL;"));
                                        instructions.remove(insnNode);
                                        instructions.remove(nextInsnNode);
                                        modified = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (modified)
        {
            ClassWriter classWriter = new ClassWriter(0);

            classNode.accept(classWriter);
            return classWriter.toByteArray();
        }
        return bytes;
    }

}