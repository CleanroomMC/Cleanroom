package com.cleanroommc.cleanroom.modpatch.transformer;

import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

final class Colytra {

    private static final String HOOKS = "com/cleanroommc/cleanroom/modpatch/transformer/Colytra$Hooks";

    private static final String ELYTRA_TRANSFORMER = "c4.colytra.asm.ElytraTransformer";

    public static byte[] transform(String name, byte[] bytes) {
        if (name.equals(ELYTRA_TRANSFORMER)) {
            ClassReader reader = new ClassReader(bytes);
            ClassWriter writer = new ClassWriter(0);
            ElytraTransformer cv = new ElytraTransformer(writer);
            reader.accept(cv, 0);
            return writer.toByteArray();
        }
        return bytes;
    }

    private static final class ElytraTransformer extends ClassVisitor implements Opcodes {

        ElytraTransformer(ClassVisitor cv) {
            super(ASM9, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
            if (name.equals("lambda$transformElytraUpdate$0")) mv = new TransformElytraUpdate(mv);
            return mv;
        }

        private static final class TransformElytraUpdate extends MethodNode {

            TransformElytraUpdate(MethodVisitor mv) {
                super(ASM9);
                this.mv = mv;
            }

            @Override
            public void visitEnd() {
                AbstractInsnNode node = super.instructions.getFirst();
                while (node.getOpcode() != SIPUSH) node = node.getNext();
                ((JumpInsnNode) node.getNext()).setOpcode(IFEQ);
                super.instructions.insert(node,new MethodInsnNode(INVOKESTATIC, HOOKS, "isNodeMethod$updateElytra", "(Lorg/objectweb/asm/tree/AbstractInsnNode;)Z", false));
                super.instructions.remove(node.getPrevious());
                super.instructions.remove(node);
                super.accept(mv);
            }
        }
    }

    @SuppressWarnings("unused")
    public static final class Hooks {

        private Hooks() {}

        public static boolean isNodeMethod$updateElytra(AbstractInsnNode node) {
            return node.getOpcode() == Opcodes.INVOKEVIRTUAL && ((MethodInsnNode) node).name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "updateElytra" : "func_184616_r");
        }
    }
}
