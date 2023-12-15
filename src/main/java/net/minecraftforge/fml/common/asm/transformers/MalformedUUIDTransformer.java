package net.minecraftforge.fml.common.asm.transformers;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class MalformedUUIDTransformer implements IClassTransformer
{

    @Override
    public byte[] transform(String s, String s1, byte[] bytes)
    {
        if (bytes == null)
        {
            return null;
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
                        if (insnNode.getOpcode() == Opcodes.INVOKESTATIC && insnNode instanceof MethodInsnNode methodInsnNode)
                        {
                            if ("java/util/UUID".equals(methodInsnNode.owner) && "fromString".equalsIgnoreCase(methodInsnNode.name))
                            {
                                methodInsnNode.owner = "com/cleanroommc/loader/javafix/UUIDFix";
                                modified = true;
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
