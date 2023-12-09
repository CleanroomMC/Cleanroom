package net.minecraftforge.fml.common.asm.transformers;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.FMLLog;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;
import java.util.stream.Stream;

public class ReflectionFieldTransformer implements IClassTransformer {
    private static final List<String> excludeList = Stream.of("com.cleanroommc.hackery", "org.spongepowered", "net.minecraft", "com.google", "com.ibm.icu", "io.netty", "com.sun", "it.unimi.dsi", "oshi", "org.slf4j", "com.mojang", "zone.rong", "kotlin").toList();


    @Override
    public byte[] transform(String s, String s1, byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        for (String str: excludeList) {
            if (s1.startsWith(str))
                return bytes;
        }
        //System.out.println(s1);

        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        if (classNode.methods != null) {
            for (MethodNode methodNode : classNode.methods) {
                if (methodNode.instructions != null) {
                    for (AbstractInsnNode insnNode : methodNode.instructions) {
                        if (insnNode.getType() == AbstractInsnNode.METHOD_INSN) {
                            if (insnNode.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                                if (((MethodInsnNode)insnNode).owner.equals("java/lang/reflect/Field")) {
                                    if (((MethodInsnNode)insnNode).name.equals("set")) {
                                        methodNode.instructions.insert(insnNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/cleanroommc/hackery/ReflectionHackery", "setField", "(Ljava/lang/reflect/Field;Ljava/lang/Object;Ljava/lang/Object;)V"));
                                        methodNode.instructions.remove(insnNode);
                                        FMLLog.log.info(s1 + "'s SET transforming");
                                    }

                                    if (((MethodInsnNode)insnNode).name.equals("get")) {
                                        methodNode.instructions.insert(insnNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/cleanroommc/hackery/ReflectionHackery", "getField", "(Ljava/lang/reflect/Field;Ljava/lang/Object;)Ljava/lang/Object;"));
                                        methodNode.instructions.remove(insnNode);
                                        FMLLog.log.info(s1 + "'s GET transforming");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        ClassWriter classWriter = new ClassWriter(0);

        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }
}
