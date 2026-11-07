package com.cleanroommc.cleanroom.modpatch.transformer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.objectweb.asm.*;

final class Corpse {

    private static final String HOOKS = "com/cleanroommc/cleanroom/modpatch/transformer/Corpse$Hooks";

    private static final String MESSAGE_OPEN_HISTORY = "de.maxhenkel.corpse.net.MessageOpenHistory";

    public static byte[] transform(String name, byte[] bytes) {
        if (name.equals(MESSAGE_OPEN_HISTORY)) {
            ClassReader reader = new ClassReader(bytes);
            ClassWriter writer = new ClassWriter(0);
            MessageOpenHistory cv = new MessageOpenHistory(writer);
            reader.accept(cv, 0);
            return writer.toByteArray();
        }
        return bytes;
    }

    private static final class MessageOpenHistory extends ClassVisitor implements Opcodes {

        MessageOpenHistory(ClassVisitor cv) {
            super(ASM9, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
            if (name.equals("onMessage")) mv = new OnMessage(mv);
            return mv;
        }

        private static final class OnMessage extends MethodVisitor {

            OnMessage(MethodVisitor mv) {
                super(ASM9, mv);
            }

            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                if (name.equals("func_147108_a")) {
                    super.visitMethodInsn(INVOKESTATIC, HOOKS, "displayGuiScreenScheduled", "(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/gui/GuiScreen;)V", false);
                    return;
                }
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            }
        }
    }

    @SuppressWarnings("unused")
    public static final class Hooks {

        @SideOnly(Side.CLIENT)
        public static void displayGuiScreenScheduled(Minecraft minecraft, GuiScreen screen) {
            minecraft.addScheduledTask(() -> minecraft.displayGuiScreen(screen));
        }
    }
}
