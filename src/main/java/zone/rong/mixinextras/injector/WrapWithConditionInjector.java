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

public class WrapWithConditionInjector extends Injector {
    public WrapWithConditionInjector(InjectionInfo info) {
        super(info, "@WrapWithCondition");
    }

    @Override
    protected void inject(Target target, InjectionNode node) {
        this.checkTargetIsLogicallyVoid(target, node);
        this.checkTargetModifiers(target, false);
        this.wrapTargetWithCondition(target, node);
    }

    private void checkTargetIsLogicallyVoid(Target target, InjectionNode node) {
        if (node.hasDecoration(WrapWithConditionInjectionInfo.POPPED_OPERATION_DECORATOR)) {
            // We have already checked that the operation's return value is immediately popped, therefore it is effectively void.
            return;
        }

        Type returnType = getReturnType(node.getCurrentTarget());
        if (returnType == null) {
            throw new InvalidInjectionException(this.info, String.format("%s annotation is targeting an invalid insn in %s in %s",
                    this.annotationType, target, this));
        }

        if (returnType != Type.VOID_TYPE) {
            throw new InvalidInjectionException(this.info,
                    String.format(
                            "%s annotation is targeting an instruction with a non-void return type in %s in %s",
                            this.annotationType, target, this
                    ));
        }
    }

    private void wrapTargetWithCondition(Target target, InjectionNode node) {
        AbstractInsnNode currentTarget = node.getCurrentTarget();
        Type returnType = getReturnType(currentTarget);
        Type[] originalArgTypes = getEffectiveArgTypes(node.getOriginalTarget());
        Type[] currentArgTypes = getEffectiveArgTypes(currentTarget);
        InsnList before = new InsnList();
        InsnList after = new InsnList();
        boolean isVirtualRedirect = node.isReplaced() && node.hasDecoration("redirector") && node.getCurrentTarget().getOpcode() != Opcodes.INVOKESTATIC;
        this.invokeHandler(target, returnType, originalArgTypes, currentArgTypes, isVirtualRedirect, before, after);
        target.wrapNode(currentTarget, currentTarget, before, after);
    }

    private void invokeHandler(Target target, Type returnType, Type[] originalArgTypes, Type[] currentArgTypes, boolean isVirtualRedirect, InsnList before, InsnList after) {
        InjectorData handler = new InjectorData(target, "condition wrapper");
        this.validateParams(handler, Type.BOOLEAN_TYPE, originalArgTypes);

        int[] argMap = this.storeArgs(target, currentArgTypes, before, 0);
        int[] handlerArgMap = ArrayUtils.addAll(argMap, target.getArgIndices());
        if (isVirtualRedirect) {
            // We need to disregard the extra "this" which will be added for a virtual redirect.
            handlerArgMap = ArrayUtils.remove(handlerArgMap, 0);
        }

        this.invokeHandlerWithArgs(this.methodArgs, before, handlerArgMap);

        LabelNode afterOperation = new LabelNode();
        LabelNode afterDummy = new LabelNode();
        before.add(new JumpInsnNode(Opcodes.IFEQ, afterOperation));

        this.pushArgs(currentArgTypes, before, argMap, 0, argMap.length);
        // Target instruction will be here
        after.add(new JumpInsnNode(Opcodes.GOTO, afterDummy));
        after.add(afterOperation);
        if (returnType != Type.VOID_TYPE) {
            after.add(new InsnNode(this.getDummyOpcodeForType(returnType)));
        }
        after.add(afterDummy);
    }

    private Type getReturnType(AbstractInsnNode node) {
        if (node instanceof MethodInsnNode) {
            MethodInsnNode methodInsnNode = (MethodInsnNode) node;
            return Type.getReturnType(methodInsnNode.desc);
        }

        if (node instanceof FieldInsnNode) {
            FieldInsnNode fieldInsnNode = (FieldInsnNode) node;
            if (fieldInsnNode.getOpcode() == Opcodes.GETFIELD || fieldInsnNode.getOpcode() == Opcodes.GETSTATIC) {
                return Type.getType(fieldInsnNode.desc);
            }
            return Type.VOID_TYPE;
        }

        return null;
    }

    private Type[] getEffectiveArgTypes(AbstractInsnNode node) {
        if (node instanceof MethodInsnNode) {
            MethodInsnNode methodInsnNode = ((MethodInsnNode) node);
            return node.getOpcode() == Opcodes.INVOKESTATIC ?
                    Type.getArgumentTypes(methodInsnNode.desc) :
                    ArrayUtils.addAll(new Type[]{Type.getObjectType(methodInsnNode.owner)}, Type.getArgumentTypes(methodInsnNode.desc));
        }
        if (node instanceof FieldInsnNode) {
            FieldInsnNode fieldInsnNode = ((FieldInsnNode) node);
            if (fieldInsnNode.getOpcode() == Opcodes.PUTFIELD) {
                return new Type[]{Type.getObjectType(fieldInsnNode.owner), Type.getType(fieldInsnNode.desc)};
            }
            if (fieldInsnNode.getOpcode() == Opcodes.PUTSTATIC) {
                return new Type[]{Type.getType(fieldInsnNode.desc)};
            }
        }

        throw new UnsupportedOperationException();
    }

    private int getDummyOpcodeForType(Type type) {
        switch (type.getSort()) {
            case Type.BOOLEAN:
            case Type.CHAR:
            case Type.BYTE:
            case Type.SHORT:
            case Type.INT:
                return Opcodes.ICONST_0;
            case Type.FLOAT:
                return Opcodes.FCONST_0;
            case Type.LONG:
                return Opcodes.LCONST_0;
            case Type.DOUBLE:
                return Opcodes.DCONST_0;
            case Type.ARRAY:
            case Type.OBJECT:
                return Opcodes.ACONST_NULL;
            default:
                throw new UnsupportedOperationException();
        }
    }
}
