package com.cleanroommc.hackery.model;

import com.cleanroommc.event.ModelPoseHackTargetRegistryEvent;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.common.MinecraftForge;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.List;
import java.util.ListIterator;

/**
 * @Project Cleanroom
 * @Author Hileb
 * @Date 2024/1/11 0:30
 **/
public class ModelAbstractTransformer implements IClassTransformer {
    public static List<String> targets;
    public ModelAbstractTransformer(){
        ModelPoseHackTargetRegistryEvent event=new ModelPoseHackTargetRegistryEvent();
        //com.cleanroommc.event.EarlyBus.BUS.post(event); //TODO: waiting for early bus
        targets=event.getTargets();
    }
    @Override
    public byte[] transform(String s, String s1, byte[] basicClass) {
        if (basicClass!=null && targets.contains(s1)){
            ClassReader classReader=new ClassReader(basicClass);
            ClassNode cn=new ClassNode();
            classReader.accept(cn, 0);

            transformModel(cn);

            ClassWriter classWriter=new ClassWriter(classReader,ClassWriter.COMPUTE_MAXS);
            cn.accept(classWriter);
            return classWriter.toByteArray();
        }else return basicClass;
    }
    public void transformModel(ClassNode cn){
        for(MethodNode mn:cn.methods){
            if (
                    "func_78087_a".equals(mn.name)
                    ||
                            ("setRotationAngles".equals(mn.name) && "(FFFFFFLnet/minecraft/entity/Entity;)V".equals(mn.desc))
            ){
                ListIterator<AbstractInsnNode> iterator= mn.instructions.iterator();
                AbstractInsnNode node;
                while (iterator.hasNext()){
                    node=iterator.next();
                    if (node.getOpcode()== Opcodes.RETURN){
                        iterator.remove();
                        iterator.add(new IntInsnNode(Opcodes.ALOAD,0));

                        iterator.add(new IntInsnNode(Opcodes.FLOAD,1));
                        iterator.add(new IntInsnNode(Opcodes.FLOAD,2));
                        iterator.add(new IntInsnNode(Opcodes.FLOAD,3));
                        iterator.add(new IntInsnNode(Opcodes.FLOAD,4));
                        iterator.add(new IntInsnNode(Opcodes.FLOAD,5));
                        iterator.add(new IntInsnNode(Opcodes.FLOAD,6));

                        iterator.add(new IntInsnNode(Opcodes.ALOAD,7));

                        iterator.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"net/minecraftforge/client/ForgeHooksClient","onLivingModelPose","(Lnet/minecraft/client/model/ModelBase;FFFFFFLnet/minecraft/entity/Entity;)V"));
                        iterator.add(node);
                    }
                }
            }
        }
    }
}
