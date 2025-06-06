package net.minecraftforge.client.model;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface IForgeArmPose {
    IForgeArmPose EMPTY = new IForgeArmPose() {};
    default void setRotationAnglesLeftHand(ModelBiped modelBiped,float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
    }
    default void setRotationAnglesRightHand(ModelBiped modelBiped,float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
    }
}
