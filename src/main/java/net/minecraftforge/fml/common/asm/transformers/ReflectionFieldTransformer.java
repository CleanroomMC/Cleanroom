package net.minecraftforge.fml.common.asm.transformers;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.FMLLog;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.List;

public class ReflectionFieldTransformer implements IClassTransformer
{
    private static final List<String> EXCLUSIONS = List.of("com.cleanroommc.hackery", "org.spongepowered", "net.minecraft", "com.google", "com.ibm.icu", "io.netty", "com.sun", "it.unimi.dsi", "oshi", "org.slf4j", "com.mojang", "zone.rong", "kotlin");
    private static final String OUR_REFLECTION_CLASS = "com/cleanroommc/hackery/ReflectionHackery";

    private static String replaceInstruction(InsnList insnList, MethodInsnNode oldNode, String methodName, String methodDescriptor)
    {
        insnList.set(oldNode, new MethodInsnNode(Opcodes.INVOKESTATIC, OUR_REFLECTION_CLASS, methodName, methodDescriptor));
        return oldNode.name + " -> " + methodName;
    }

    @Override
    public byte[] transform(String s, String s1, byte[] bytes)
    {
        if (bytes == null)
        {
            return null;
        }
        for (String str: EXCLUSIONS)
        {
            if (s1.startsWith(str))
            {
                return bytes;
            }
        }

        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        if (classNode.methods != null)
        {
            for (MethodNode methodNode : classNode.methods)
            {
                InsnList instructions = methodNode.instructions;
                if (instructions != null)
                {
                    for (AbstractInsnNode insnNode : instructions)
                    {
                        if (insnNode.getOpcode() == Opcodes.INVOKEVIRTUAL && insnNode instanceof MethodInsnNode methodInsnNode)
                        {
                            if ("java/lang/reflect/Field".equals(methodInsnNode.owner))
                            {
                                String result = switch (methodInsnNode.name)
                                {
                                    case "set" -> replaceInstruction(instructions, methodInsnNode, "setField", "(Ljava/lang/reflect/Field;Ljava/lang/Object;Ljava/lang/Object;)V");
                                    case "get" -> replaceInstruction(instructions, methodInsnNode, "getField", "(Ljava/lang/reflect/Field;Ljava/lang/Object;)Ljava/lang/Object;");
                                    case "setBoolean" -> replaceInstruction(instructions, methodInsnNode, "setBooleanField", "(Ljava/lang/reflect/Field;Ljava/lang/Object;Z)V");
                                    case "getBoolean" -> replaceInstruction(instructions, methodInsnNode, "getBooleanField", "(Ljava/lang/reflect/Field;Ljava/lang/Object;)Z");
                                    case "setByte" -> replaceInstruction(instructions, methodInsnNode, "setByteField", "(Ljava/lang/reflect/Field;Ljava/lang/Object;B)V");
                                    case "getByte" -> replaceInstruction(instructions, methodInsnNode, "getByteField", "(Ljava/lang/reflect/Field;Ljava/lang/Object;)B");
                                    case "setChar" -> replaceInstruction(instructions, methodInsnNode, "setCharField", "(Ljava/lang/reflect/Field;Ljava/lang/Object;C)V");
                                    case "getChar" -> replaceInstruction(instructions, methodInsnNode, "getCharField", "(Ljava/lang/reflect/Field;Ljava/lang/Object;)C");
                                    case "setShort" -> replaceInstruction(instructions, methodInsnNode, "setShortField", "(Ljava/lang/reflect/Field;Ljava/lang/Object;S)V");
                                    case "getShort" -> replaceInstruction(instructions, methodInsnNode, "getShortField", "(Ljava/lang/reflect/Field;Ljava/lang/Object;)S");
                                    case "setInt" -> replaceInstruction(instructions, methodInsnNode, "setIntField", "(Ljava/lang/reflect/Field;Ljava/lang/Object;I)V");
                                    case "getInt" -> replaceInstruction(instructions, methodInsnNode, "getIntField", "(Ljava/lang/reflect/Field;Ljava/lang/Object;)I");
                                    case "setLong" -> replaceInstruction(instructions, methodInsnNode, "setLongField", "(Ljava/lang/reflect/Field;Ljava/lang/Object;J)V");
                                    case "getLong" -> replaceInstruction(instructions, methodInsnNode, "getLongField", "(Ljava/lang/reflect/Field;Ljava/lang/Object;)J");
                                    case "setFloat" -> replaceInstruction(instructions, methodInsnNode, "setFloatField", "(Ljava/lang/reflect/Field;Ljava/lang/Object;F)V");
                                    case "getFloat" -> replaceInstruction(instructions, methodInsnNode, "getFloatField", "(Ljava/lang/reflect/Field;Ljava/lang/Object;)F");
                                    case "setDouble" -> replaceInstruction(instructions, methodInsnNode, "setDoubleField", "(Ljava/lang/reflect/Field;Ljava/lang/Object;D)V");
                                    case "getDouble" -> replaceInstruction(instructions, methodInsnNode, "getDoubleField", "(Ljava/lang/reflect/Field;Ljava/lang/Object;)D");
                                    default -> null;
                                };
                                if (result != null) {
                                    FMLLog.log.info("[{}]: Transformed {}", s1, result);
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
