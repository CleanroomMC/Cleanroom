package zone.rong.mixinbooter.fb;

import com.google.common.primitives.Bytes;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.edit.AnnotationEdit;
import org.spongepowered.asm.mixin.edit.IMixinEditor;
import org.spongepowered.asm.mixin.edit.MixinEdits;
import org.spongepowered.asm.service.MixinService;
import org.spongepowered.asm.util.Annotations;
import org.spongepowered.asm.util.asm.ASM;
import top.outlands.foundation.TransformerDelegate;
import zone.rong.mixinbooter.Tags;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;

/**
 * FermiumBooter ships MixinSquared, this moves its users onto CleanMix's own mixin editing.
 * <ul>
 *     <li>{@code MixinSquaredBootstrap} and both registrars are renamed to this class</li>
 *     <li>{@code MixinCanceller} is renamed to {@link Canceller}, which is registered as an {@link IMixinEditor}</li>
 *     <li>{@code MixinAnnotationAdjuster} is renamed to {@link Adjuster}, which is registered as an {@link IMixinEditor}</li>
 *     <li>Adjustable annotation nodes are renamed to {@link Node}, calls and method references on them reach the
 *     hooks here, which edit through {@link AnnotationEdit}</li>
 *     <li>{@code @TargetHandler} injectors are rewritten to the {@code @Merged} selector</li>
 * </ul>
 * Accessors such as {@code getRequire}, {@code setAt} and {@code withMethod} read and write the annotation value of
 * the same name, with the annotation's own defaults. Constructors, {@code fromNode} and nested values copy the
 * annotation like MixinSquared does, so a change to one only lands once it is set back.
 * <p>
 * Every adjustable type becomes {@link Node}, so {@code instanceof} checks against intermediate types and interfaces
 * match any node, and {@code getClass} does not tell them apart.
 */
public final class MixinSquared implements IClassTransformer {

    private static final String PACKAGE = "com/bawnorton/mixinsquared/";
    private static final String TOOLS = PACKAGE + "adjuster/tools/";
    private static final String INJECTION_POINT = TOOLS + "AdjustableAtNode$InjectionPoint";
    private static final String FACTORY = TOOLS + "AdjustableAnnotationNode$AdjustableAnnotationNodeFactory";
    private static final byte[] PACKAGE_BYTES = PACKAGE.getBytes(StandardCharsets.US_ASCII);
    private static final String SELF = MixinSquared.class.getName().replace('.', '/');
    private static final String NODE = SELF + "$Node";
    private static final String ANNOTATION_NODE = Type.getInternalName(AnnotationNode.class);
    private static final String TARGET_HANDLER = "L" + PACKAGE + "TargetHandler;";
    private static final String HANDLER_SELECTOR = "@MixinSquared:Handler";
    private static final String AT = "Lorg/spongepowered/asm/mixin/injection/At;";
    private static final Set<String> INHERITED = new HashSet<>();
    // The annotation of each node type, and the keys its defaultNode arguments set, or null without a defaultNode
    private static final Map<String, String[]> KINDS = Map.ofEntries(
            entry("AdjustableAccessorNode", "Lorg/spongepowered/asm/mixin/gen/Accessor;", ""),
            entry("AdjustableInvokerNode", "Lorg/spongepowered/asm/mixin/gen/Invoker;", ""),
            entry("AdjustableOverwriteNode", "Lorg/spongepowered/asm/mixin/Overwrite;", ""),
            entry("AdjustableConstantNode", "Lorg/spongepowered/asm/mixin/injection/Constant;", ""),
            entry("AdjustableSliceNode", "Lorg/spongepowered/asm/mixin/injection/Slice;", ""),
            entry("AdjustableNextNode", "Lorg/spongepowered/asm/mixin/injection/Next;", ""),
            entry("AdjustableModifyConstantNode", "Lorg/spongepowered/asm/mixin/injection/ModifyConstant;", ""),
            entry("AdjustableRedirectNode", "Lorg/spongepowered/asm/mixin/injection/Redirect;", null),
            entry("AdjustableLocalNode", "Lcom/llamalad7/mixinextras/sugar/Local;", ""),
            entry("AdjustableWrapWithConditionNode", "Lcom/llamalad7/mixinextras/injector/v2/WrapWithCondition;", ""),
            entry("AdjustableAtNode", AT, "value"),
            entry("AdjustableDescNode", "Lorg/spongepowered/asm/mixin/injection/Desc;", "value"),
            entry("AdjustableDefinitionNode", "Lcom/llamalad7/mixinextras/expression/Definition;", "id"),
            entry("AdjustableDefinitionsNode", "Lcom/llamalad7/mixinextras/expression/Definitions;", "value"),
            entry("AdjustableExpressionNode", "Lcom/llamalad7/mixinextras/expression/Expression;", "value"),
            entry("AdjustableExpressionsNode", "Lcom/llamalad7/mixinextras/expression/Expressions;", "value"),
            entry("AdjustableInjectNode", "Lorg/spongepowered/asm/mixin/injection/Inject;", "at"),
            entry("AdjustableModifyArgNode", "Lorg/spongepowered/asm/mixin/injection/ModifyArg;", "at"),
            entry("AdjustableModifyArgsNode", "Lorg/spongepowered/asm/mixin/injection/ModifyArgs;", "at"),
            entry("AdjustableModifyVariableNode", "Lorg/spongepowered/asm/mixin/injection/ModifyVariable;", "at"),
            entry("AdjustableModifyExpressionValueNode", "Lcom/llamalad7/mixinextras/injector/ModifyExpressionValue;", "method,at"),
            entry("AdjustableModifyReceiverNode", "Lcom/llamalad7/mixinextras/injector/ModifyReceiver;", "method,at"),
            entry("AdjustableModifyReturnValueNode", "Lcom/llamalad7/mixinextras/injector/ModifyReturnValue;", "method,at"),
            entry("AdjustableWrapMethodNode", "Lcom/llamalad7/mixinextras/injector/wrapmethod/WrapMethod;", "method"),
            entry("AdjustableWrapOperationNode", "Lcom/llamalad7/mixinextras/injector/wrapoperation/WrapOperation;", "method"));

    static {
        for (Method method : AnnotationNode.class.getMethods()) {
            INHERITED.add(method.getName());
        }
    }

    static void install() {
        TransformerDelegate.registerTransformer(new MixinSquared());
        MixinEdits.register(new IMixinEditor() {
            @Override
            public void edit(String mixinClassName, ClassNode mixin) {
                targetHandlers(mixin);
            }
        });
    }

    public static void init() { }

    public static void reOrderExtensions() { }

    public static void register(Canceller canceller) {
        MixinEdits.register(new IMixinEditor() {
            @Override
            public boolean shouldApply(String mixinClassName, String targetClassName) {
                return !canceller.shouldCancel(Collections.singletonList(targetClassName), mixinClassName);
            }

            @Override
            public String toString() {
                return canceller.getClass().getName();
            }
        });
    }

    public static void register(Adjuster adjuster) {
        MixinEdits.register(new IMixinEditor() {
            @Override
            public void edit(String mixinClassName, ClassNode mixin) {
                List<String> targets = targets(mixin);
                for (MethodNode method : mixin.methods) {
                    if (method.visibleAnnotations == null) {
                        continue;
                    }
                    for (ListIterator<AnnotationNode> iter = method.visibleAnnotations.listIterator(); iter.hasNext(); ) {
                        Node adjusted = adjuster.adjust(targets, mixinClassName, method, new Node(iter.next()));
                        if (adjusted == null) {
                            iter.remove();
                        } else {
                            iter.set(adjusted);
                        }
                    }
                }
            }

            @Override
            public String toString() {
                return adjuster.getClass().getName();
            }
        });
    }

    public static Object get(Object node, String key) {
        AnnotationNode annotation = (AnnotationNode) node;
        Method member = member(annotation.desc, key);
        Object value = AnnotationEdit.of(annotation).get(key);
        if (value == null && member != null) {
            value = member.getDefaultValue();
        }
        return adapt(value, member == null ? null : member.getReturnType());
    }

    public static void set(Object node, Object value, String key) {
        AnnotationEdit.of((AnnotationNode) node).set(key, unadapt(value));
    }

    public static Object with(Object node, UnaryOperator<Object> operator, String key) {
        set(node, operator.apply(get(node, key)), key);
        return node;
    }

    public static boolean is(Object node, Class<? extends Annotation> type) {
        return ((AnnotationNode) node).desc.equals(Type.getDescriptor(type));
    }

    public static boolean kind(Object node, String desc) {
        return node instanceof Node && ((Node) node).desc.equals(desc);
    }

    public static Node copyOf(AnnotationNode node) {
        return new Node(node);
    }

    public static List<Node> fromList(List<AnnotationNode> nodes, Factory factory) {
        List<Node> list = new ArrayList<>(nodes.size());
        for (AnnotationNode node : nodes) {
            list.add(factory.create(node));
        }
        return list;
    }

    public static Node defaultNode(String desc, String keys) {
        return create(desc, keys);
    }

    public static Node defaultNode(Object value, String desc, String keys) {
        return create(desc, keys, value);
    }

    public static Node defaultNode(Object first, Object second, String desc, String keys) {
        return create(desc, keys, first, second);
    }

    public static Optional<Object> value(Object node, String key) {
        return Optional.ofNullable(AnnotationEdit.of((AnnotationNode) node).get(key));
    }

    public static void value(Object node, String key, Object value) {
        AnnotationEdit.of((AnnotationNode) node).set(key, value);
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        // This class names the package too, transforming it would load its nested classes in the middle of a transform
        if (bytes == null || transformedName.startsWith("com.bawnorton.mixinsquared.") || transformedName.startsWith(MixinSquared.class.getName()) ||
                Bytes.indexOf(bytes, PACKAGE_BYTES) < 0) {
            return bytes;
        }
        try {
            ClassNode node = new ClassNode();
            // ASM 5 cannot remap compressed frames
            new ClassReader(bytes).accept(node, ClassReader.EXPAND_FRAMES);
            Rewriter rewriter = new Rewriter(node);
            for (MethodNode method : new ArrayList<>(node.methods)) {
                rewriter.rewrite(method);
            }
            node.methods.addAll(rewriter.bridges);
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            Renamer renamer = new Renamer();
            node.accept(new ClassRemapper(writer, renamer));
            return renamer.renamed || rewriter.changed ? writer.toByteArray() : bytes;
        } catch (Throwable t) {
            logger().error("Failed to move {} off MixinSquared", transformedName, t);
            return bytes;
        }
    }

    private static void targetHandlers(ClassNode mixin) {
        for (MethodNode method : mixin.methods) {
            AnnotationNode handler = Annotations.get(method.visibleAnnotations, TARGET_HANDLER);
            if (handler == null) {
                continue;
            }
            method.visibleAnnotations.remove(handler);
            String selector = "@Merged(" + Annotations.getValue(handler, "mixin") + "::" + Annotations.getValue(handler, "name") + ")";
            for (AnnotationNode injector : method.visibleAnnotations) {
                List<String> targets = Annotations.getValue(injector, "method");
                if (targets != null) {
                    targets.replaceAll(target -> HANDLER_SELECTOR.equals(target) ? selector : target);
                }
            }
        }
    }

    private static List<String> targets(ClassNode mixin) {
        List<String> targets = new ArrayList<>();
        AnnotationNode annotation = Annotations.getInvisible(mixin, Mixin.class);
        if (annotation != null) {
            for (Type type : Annotations.<Type>getValue(annotation, "value", true)) {
                targets.add(type.getClassName());
            }
            for (String target : Annotations.<String>getValue(annotation, "targets", true)) {
                targets.add(target.replace('/', '.'));
            }
        }
        return targets;
    }

    private static Node create(String desc, String keys, Object... values) {
        Node node = new Node(desc);
        String[] names = keys.isEmpty() ? new String[0] : keys.split(",");
        for (int i = 0; i < names.length; i++) {
            set(node, values[i], names[i]);
        }
        return node;
    }

    private static Method member(String desc, String key) {
        try {
            return Class.forName(Type.getType(desc).getClassName(), false, MixinSquared.class.getClassLoader()).getMethod(key);
        } catch (ReflectiveOperationException | LinkageError e) {
            return null;
        }
    }

    /**
     * Turns a value as stored in bytecode, or a default from reflection, into what MixinSquared's accessors return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Object adapt(Object value, Class<?> type) {
        Class<?> element = type != null && type.isArray() ? type.getComponentType() : type;
        if (value instanceof String[] pair && element != null && element.isEnum()) {
            return Enum.valueOf((Class<Enum>) element, pair[1]);
        }
        if (value instanceof Object[] array) {
            value = Arrays.asList(array);
        }
        if (value instanceof List<?> list) {
            List<Object> adapted = new ArrayList<>(list.size());
            for (Object entry : list) {
                adapted.add(adapt(entry, element));
            }
            return adapted;
        }
        if (value instanceof AnnotationNode node) {
            return new Node(node);
        }
        if (value instanceof Annotation annotation) {
            return new Node(Type.getDescriptor(annotation.annotationType()));
        }
        if (value instanceof Class<?> clazz) {
            return Type.getType(clazz);
        }
        return value;
    }

    private static Object unadapt(Object value) {
        if (value instanceof InjectionPoint point) {
            return point.value();
        }
        if (value instanceof Enum<?> constant) {
            return new String[] { Type.getDescriptor(constant.getDeclaringClass()), constant.name() };
        }
        if (value instanceof Object[] array) {
            value = Arrays.asList(array);
        }
        if (value instanceof List<?> list) {
            List<Object> raw = new ArrayList<>(list.size());
            for (Object entry : list) {
                raw.add(unadapt(entry));
            }
            return raw;
        }
        return value;
    }

    private static boolean isNode(String owner) {
        return owner != null && owner.startsWith(TOOLS) && !owner.equals(INJECTION_POINT) && !owner.equals(FACTORY);
    }

    private static Map.Entry<String, String[]> entry(String node, String desc, String keys) {
        return Map.entry(TOOLS + node, new String[] { desc, keys });
    }

    private static ILogger logger() {
        return MixinService.getService().getLogger(Tags.MOD_NAME + "|MixinSquared");
    }

    /**
     * What MixinSquared's adjustable nodes become. The constructor copies the annotation, as theirs does.
     */
    public static class Node extends AnnotationNode {

        public Node(AnnotationNode node) {
            super(ASM.API_VERSION, node.desc);
            node.accept(this);
        }

        Node(String desc) {
            super(ASM.API_VERSION, desc);
        }

    }

    public interface Canceller {

        boolean shouldCancel(List<String> targetClassNames, String mixinClassName);

    }

    public interface Adjuster {

        Node adjust(List<String> targetClassNames, String mixinClassName, MethodNode handlerNode, Node annotationNode);

    }

    public interface Factory {

        Node create(AnnotationNode node);

    }

    public enum InjectionPoint {
        HEAD,
        TAIL,
        INVOKE,
        INVOKE_ASSIGN,
        INVOKE_STRING,
        FIELD,
        NEW,
        CONSTANT,
        JUMP,
        CUSTOM;

        private String customName;

        // MixinSquared stores the name on the shared constant as well
        public static InjectionPoint custom(String name) {
            CUSTOM.customName = name;
            return CUSTOM;
        }

        public Node toNode() {
            return defaultNode(this, AT, "value");
        }

        private String value() {
            if (this != CUSTOM) {
                return this.name();
            }
            if (this.customName == null) {
                throw new IllegalStateException("Custom InjectionPoint name not set");
            }
            return this.customName;
        }

    }

    private static final class Renamer extends Remapper {

        private boolean renamed;

        Renamer() {
            super(ASM.API_VERSION);
        }

        @Override
        public String map(String internalName) {
            String mapped = switch (internalName) {
                case PACKAGE + "MixinSquaredBootstrap", PACKAGE + "canceller/MixinCancellerRegistrar",
                     PACKAGE + "adjuster/MixinAnnotationAdjusterRegistrar" -> SELF;
                case PACKAGE + "api/MixinCanceller" -> SELF + "$Canceller";
                case PACKAGE + "api/MixinAnnotationAdjuster" -> SELF + "$Adjuster";
                case INJECTION_POINT -> SELF + "$InjectionPoint";
                case FACTORY -> SELF + "$Factory";
                default -> internalName.startsWith(TOOLS) ? NODE : null;
            };
            if (mapped == null) {
                return internalName;
            }
            this.renamed = true;
            return mapped;
        }

    }

    /**
     * Rewrites uses of adjustable nodes while their original names are still in place, before {@link Renamer} runs
     */
    private static final class Rewriter {

        private final ClassNode owner;
        private final Set<String> declared = new HashSet<>();
        private final List<MethodNode> bridges = new ArrayList<>();
        private final Map<Handle, Handle> bridged = new HashMap<>();
        private boolean changed;

        Rewriter(ClassNode owner) {
            this.owner = owner;
            for (MethodNode method : owner.methods) {
                this.declared.add(method.name + method.desc);
            }
        }

        void rewrite(MethodNode method) {
            for (AbstractInsnNode insn : method.instructions.toArray()) {
                if (insn instanceof MethodInsnNode call && this.isNodeCall(call.owner, call.name, call.desc) && !call.name.equals("<init>")) {
                    InsnList replacement = rewrite(call.getOpcode(), call.owner, call.name, call.desc);
                    if (replacement != null) {
                        method.instructions.insert(call, replacement);
                        method.instructions.remove(call);
                        this.changed = true;
                    } else if (!INHERITED.contains(call.name)) {
                        this.unsupported(method, call.owner + "." + call.name + call.desc);
                    }
                } else if (insn.getOpcode() == Opcodes.INSTANCEOF && KINDS.containsKey(((TypeInsnNode) insn).desc)) {
                    InsnList replacement = new InsnList();
                    replacement.add(new LdcInsnNode(KINDS.get(((TypeInsnNode) insn).desc)[0]));
                    replacement.add(new MethodInsnNode(Opcodes.INVOKESTATIC, SELF, "kind", "(Ljava/lang/Object;Ljava/lang/String;)Z", false));
                    method.instructions.insert(insn, replacement);
                    method.instructions.remove(insn);
                    this.changed = true;
                } else if (insn instanceof InvokeDynamicInsnNode dynamic) {
                    for (int i = 0; i < dynamic.bsmArgs.length; i++) {
                        // Node's constructor stands in for theirs, so constructor references need no bridge
                        if (dynamic.bsmArgs[i] instanceof Handle handle && isNode(handle.getOwner()) && handle.getTag() != Opcodes.H_NEWINVOKESPECIAL) {
                            Handle bridge = this.bridged.computeIfAbsent(handle, this::bridge);
                            if (bridge != null) {
                                dynamic.bsmArgs[i] = bridge;
                                this.changed = true;
                            } else if (!INHERITED.contains(handle.getName())) {
                                this.unsupported(method, handle.getOwner() + "::" + handle.getName());
                            }
                        }
                    }
                }
            }
        }

        /**
         * Calls on an adjustable node, including inherited ones a subclass of it makes on itself
         */
        private boolean isNodeCall(String owner, String name, String desc) {
            return isNode(owner) || owner.equals(this.owner.name) && isNode(this.owner.superName) && !this.declared.contains(name + desc);
        }

        /**
         * Makes a static method doing what the handle did, for method references such as {@code AdjustableInjectNode::getRequire}
         */
        private Handle bridge(Handle handle) {
            String desc;
            InsnList body;
            if (handle.getTag() == Opcodes.H_INVOKESTATIC) {
                desc = handle.getDesc();
                body = rewrite(Opcodes.INVOKESTATIC, handle.getOwner(), handle.getName(), desc);
            } else if (handle.getTag() == Opcodes.H_INVOKEVIRTUAL || handle.getTag() == Opcodes.H_INVOKEINTERFACE) {
                desc = "(L" + handle.getOwner() + ";" + handle.getDesc().substring(1);
                int opcode = handle.getTag() == Opcodes.H_INVOKEVIRTUAL ? Opcodes.INVOKEVIRTUAL : Opcodes.INVOKEINTERFACE;
                body = rewrite(opcode, handle.getOwner(), handle.getName(), handle.getDesc());
            } else {
                return null;
            }
            if (body == null) {
                return null;
            }
            boolean isInterface = (this.owner.access & Opcodes.ACC_INTERFACE) != 0;
            int access = Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC | (isInterface ? Opcodes.ACC_PUBLIC : Opcodes.ACC_PRIVATE);
            MethodNode bridge = new MethodNode(access, "mixinsquared$bridge$" + this.bridges.size(), desc, null, null);
            int local = 0;
            for (Type arg : Type.getArgumentTypes(desc)) {
                bridge.instructions.add(new VarInsnNode(arg.getOpcode(Opcodes.ILOAD), local));
                local += arg.getSize();
            }
            bridge.instructions.add(body);
            Type result = Type.getReturnType(desc);
            bridge.instructions.add(new InsnNode(result == Type.VOID_TYPE ? Opcodes.RETURN : result.getOpcode(Opcodes.IRETURN)));
            this.bridges.add(bridge);
            return new Handle(Opcodes.H_INVOKESTATIC, this.owner.name, bridge.name, desc, isInterface);
        }

        private void unsupported(MethodNode method, String member) {
            logger().warn("{}.{} uses MixinSquared's {}, which Cleanroom does not support", this.owner.name, method.name, member);
        }

        /**
         * The replacement of a call on an adjustable node, or null when it has none
         */
        private static InsnList rewrite(int opcode, String owner, String name, String descriptor) {
            InsnList list = new InsnList();
            Type[] args = Type.getArgumentTypes(descriptor);
            Type result = Type.getReturnType(descriptor);
            String[] kind = KINDS.get(owner);
            if (opcode == Opcodes.INVOKESTATIC) {
                if (name.equals("defaultNode") && kind != null && kind[1] != null && args.length <= 2) {
                    list.add(new LdcInsnNode(kind[0]));
                    list.add(new LdcInsnNode(kind[1]));
                    hook(list, "defaultNode", "(" + "Ljava/lang/Object;".repeat(args.length) + "Ljava/lang/String;Ljava/lang/String;)L" + NODE + ";");
                } else if (name.equals("fromNode")) {
                    hook(list, "copyOf", "(L" + ANNOTATION_NODE + ";)L" + NODE + ";");
                } else if (name.equals("fromList")) {
                    hook(list, "fromList", "(Ljava/util/List;L" + SELF + "$Factory;)Ljava/util/List;");
                } else {
                    return null;
                }
            } else if (opcode == Opcodes.INVOKESPECIAL) {
                return null;
            } else if (name.equals("as")) {
                list.add(new InsnNode(Opcodes.POP));
            } else if (name.equals("is")) {
                hook(list, "is", "(Ljava/lang/Object;Ljava/lang/Class;)Z");
            } else if (name.equals("copy")) {
                hook(list, "copyOf", "(L" + ANNOTATION_NODE + ";)L" + NODE + ";");
            } else if (name.equals("get") && args.length == 1) {
                hook(list, "value", "(Ljava/lang/Object;Ljava/lang/String;)Ljava/util/Optional;");
            } else if (name.equals("set") && args.length == 2) {
                hook(list, "value", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/Object;)V");
            } else if (accessor(name, "get") && args.length == 0) {
                list.add(new LdcInsnNode(key(name, 3)));
                hook(list, "get", "(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;");
                cast(list, result);
            } else if (accessor(name, "set") && args.length == 1 && result == Type.VOID_TYPE) {
                box(list, args[0]);
                list.add(new LdcInsnNode(key(name, 3)));
                hook(list, "set", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/String;)V");
            } else if (accessor(name, "with") && args.length == 1) {
                list.add(new LdcInsnNode(key(name, 4)));
                hook(list, "with", "(Ljava/lang/Object;Ljava/util/function/UnaryOperator;Ljava/lang/String;)Ljava/lang/Object;");
                cast(list, result);
            } else {
                return null;
            }
            return list;
        }

        private static void hook(InsnList list, String name, String descriptor) {
            list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, SELF, name, descriptor, false));
        }

        private static void cast(InsnList list, Type type) {
            if (type.getSort() == Type.OBJECT || type.getSort() == Type.ARRAY) {
                list.add(new TypeInsnNode(Opcodes.CHECKCAST, type.getInternalName()));
                return;
            }
            Type boxed = boxed(type);
            list.add(new TypeInsnNode(Opcodes.CHECKCAST, boxed.getInternalName()));
            list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, boxed.getInternalName(), type.getClassName() + "Value", "()" + type.getDescriptor(), false));
        }

        private static void box(InsnList list, Type type) {
            if (type.getSort() != Type.OBJECT && type.getSort() != Type.ARRAY) {
                Type boxed = boxed(type);
                list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, boxed.getInternalName(), "valueOf", "(" + type.getDescriptor() + ")" + boxed.getDescriptor(), false));
            }
        }

        private static boolean accessor(String name, String prefix) {
            return name.length() > prefix.length() && name.startsWith(prefix) && Character.isUpperCase(name.charAt(prefix.length()));
        }

        private static String key(String name, int prefixLength) {
            return Character.toLowerCase(name.charAt(prefixLength)) + name.substring(prefixLength + 1);
        }

        private static Type boxed(Type type) {
            return Type.getObjectType(switch (type.getSort()) {
                case Type.BOOLEAN -> "java/lang/Boolean";
                case Type.CHAR -> "java/lang/Character";
                case Type.BYTE -> "java/lang/Byte";
                case Type.SHORT -> "java/lang/Short";
                case Type.INT -> "java/lang/Integer";
                case Type.FLOAT -> "java/lang/Float";
                case Type.LONG -> "java/lang/Long";
                default -> "java/lang/Double";
            });
        }

    }

}
