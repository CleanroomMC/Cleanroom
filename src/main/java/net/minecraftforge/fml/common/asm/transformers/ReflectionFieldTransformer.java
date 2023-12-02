package net.minecraftforge.fml.common.asm.transformers;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class ReflectionFieldTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String s, String s1, byte[] bytes) {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
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
                                    }

                                    if (((MethodInsnNode)insnNode).name.equals("get")) {
                                        methodNode.instructions.insert(insnNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/cleanroommc/hackery/ReflectionHackery", "getField", "(Ljava/lang/reflect/Field;Ljava/lang/Object;)Ljava/lang/Object;"));
                                        methodNode.instructions.remove(insnNode);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES) {
            @Override
            protected ClassLoader getClassLoader() {
                return Launch.classLoader;
            }
        };
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }
}
