package zone.rong.mixinextras.injector;

import org.spongepowered.asm.mixin.injection.throwables.InvalidInjectionException;
import org.apache.commons.lang3.ArrayUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.injection.code.Injector;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.injection.struct.InjectionNodes.InjectionNode;
import org.spongepowered.asm.mixin.injection.struct.Target;

public class ModifyReceiverInjector extends Injector {
    public ModifyReceiverInjector(InjectionInfo info) {
        super(info, "@ModifyReceiver");
    }

    @Override
    protected void inject(Target target, InjectionNode node) {
        this.checkTargetIsValid(target, node);
        this.checkTargetModifiers(target, false);
        this.modifyReceiverOfTarget(target, node);
    }

    private void checkTargetIsValid(Target target, InjectionNode node) {
        AbstractInsnNode insn = node.getOriginalTarget();
        switch (insn.getOpcode()) {
            case Opcodes.INVOKEVIRTUAL:
            case Opcodes.INVOKESPECIAL:
            case Opcodes.INVOKEINTERFACE:
            case Opcodes.GETFIELD:
            case Opcodes.PUTFIELD:
                return;
            default:
                throw new InvalidInjectionException(this.info, String.format("%s annotation is targeting an invalid insn in %s in %s",
                        this.annotationType, target, this));
        }
    }

    private void modifyReceiverOfTarget(Target target, InjectionNode node) {
        AbstractInsnNode currentTarget = node.getCurrentTarget();
        Type[] originalArgTypes = getEffectiveArgTypes(node.getOriginalTarget());
        Type[] currentArgTypes = getEffectiveArgTypes(currentTarget);
        InsnList insns = new InsnList();
        boolean isVirtualRedirect = node.isReplaced() && node.hasDecoration("redirector") && node.getCurrentTarget().getOpcode() != Opcodes.INVOKESTATIC;
        this.injectReceiverModifier(target, originalArgTypes, currentArgTypes, isVirtualRedirect, insns);
        target.insertBefore(node, insns);
    }

    private void injectReceiverModifier(Target target, Type[] originalArgTypes, Type[] currentArgTypes, boolean isVirtualRedirect, InsnList insns) {
        InjectorData handler = new InjectorData(target, "receiver modifier");
        this.validateParams(handler, originalArgTypes[0], originalArgTypes);

        int[] argMap = this.storeArgs(target, currentArgTypes, insns, 0);
        int[] handlerArgMap = ArrayUtils.addAll(argMap, target.getArgIndices());
        if (isVirtualRedirect) {
            // We need to disregard the extra "this" which will be added for a virtual redirect.
            handlerArgMap = ArrayUtils.remove(handlerArgMap, 0);
            // We also need to ensure it remains on the stack before the receiver
            insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
        }

        this.invokeHandlerWithArgs(this.methodArgs, insns, handlerArgMap);

        // If this is a virtual redirect, both "this" and the receiver are already on the stack
        this.pushArgs(currentArgTypes, insns, argMap, isVirtualRedirect ? 2 : 1, argMap.length);
    }

    private Type[] getEffectiveArgTypes(AbstractInsnNode node) {
        switch (node.getOpcode()) {
            case Opcodes.INVOKEVIRTUAL:
            case Opcodes.INVOKESPECIAL:
            case Opcodes.INVOKEINTERFACE: {
                MethodInsnNode methodInsnNode = ((MethodInsnNode) node);
                return ArrayUtils.addAll(new Type[]{Type.getObjectType(methodInsnNode.owner)}, Type.getArgumentTypes(methodInsnNode.desc));
            }
            case Opcodes.GETFIELD: {
                FieldInsnNode fieldInsnNode = ((FieldInsnNode) node);
                return new Type[]{Type.getObjectType(fieldInsnNode.owner)};
            }
            case Opcodes.PUTFIELD: {
                FieldInsnNode fieldInsnNode = ((FieldInsnNode) node);
                return new Type[]{Type.getObjectType(fieldInsnNode.owner), Type.getType(fieldInsnNode.desc)};
            }
        }

        throw new UnsupportedOperationException();
    }
}
