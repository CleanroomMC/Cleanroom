package com.cleanroommc.hackery.model;

import com.cleanroommc.utils.asm.ASMUtil;
import com.cleanroommc.utils.asm.name.MethodName;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.HashSet;

//TODO rebuild with {@link com.cleanroommc.bouncepad.api.transformer.Transformer}
public class ModelAbstractTransformer implements IClassTransformer {
    public static HashSet<String> transformedClasses=new HashSet<>();
    public static final MethodName m_render=MethodName.of()
            .mcp("render","(Lnet/minecraft/entity/Entity;FFFFFF)V")
            .srg("func_78088_a","(Lnet/minecraft/entity/Entity;FFFFFF)V")
            .notch("a","(Lvg;FFFFFF)V").build();
    public static final MethodName m_setRotationAngles=MethodName.of()
            .mcp("setRotationAngles","(FFFFFFLnet/minecraft/entity/Entity;)V")
            .srg("func_78087_a","(FFFFFFLnet/minecraft/entity/Entity;)V")
            .notch("a","(FFFFFFLvg;)V").build();
    @Override
    public byte[] transform(String s, String s1, byte[] basicClass) {
        if (basicClass!=null){
            ClassReader classReader=new ClassReader(basicClass);
            ClassNode cn=new ClassNode();
            classReader.accept(cn, 0);
            if (
                    "net.minecraft.client.model.ModelBase".equals(s1)
                    || transformedClasses.contains(cn.superName)
            ){
                    transformedClasses.add(cn.name);
                    transformModel(cn);
                    ClassWriter classWriter=new ClassWriter(classReader,ClassWriter.COMPUTE_MAXS);
                    cn.accept(classWriter);
                    return classWriter.toByteArray();
            }else return basicClass;

        }else return basicClass;
    }
    public void transformModel(ClassNode cn){
        for(MethodNode mn:cn.methods){
            if (m_render.is(mn)){
                ASMUtil.injectAfter(
                        mn.instructions,
                        createHook(),
                        (abstractInsnNode)-> {
                            if (abstractInsnNode.getOpcode()==Opcodes.INVOKEVIRTUAL && abstractInsnNode.getType()==AbstractInsnNode.METHOD_INSN){
                                MethodInsnNode methodInsnNode=(MethodInsnNode) abstractInsnNode;
                                if (m_setRotationAngles.is(methodInsnNode.name,methodInsnNode.desc)){
                                    return true;
                                }
                            }
                            return false;
                        }
                );
            }
        }
    }
    public static InsnList createHook(){
        InsnList hook=new InsnList();
        hook.add(new IntInsnNode(Opcodes.ALOAD,0));

        hook.add(new IntInsnNode(Opcodes.ALOAD,1));

        hook.add(new IntInsnNode(Opcodes.FLOAD,2));
        hook.add(new IntInsnNode(Opcodes.FLOAD,3));
        hook.add(new IntInsnNode(Opcodes.FLOAD,4));
        hook.add(new IntInsnNode(Opcodes.FLOAD,5));
        hook.add(new IntInsnNode(Opcodes.FLOAD,6));
        hook.add(new IntInsnNode(Opcodes.FLOAD,7));

        hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"net/minecraftforge/client/ForgeHooksClient","onLivingModelPose","(Lnet/minecraft/client/model/ModelBase;Lnet/minecraft/entity/Entity;FFFFFF)V"));
        return hook;
    }
}
