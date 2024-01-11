package net.minecraftforge.client.event;

import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EntityModelPoseEvent extends EntityEvent {
    private ModelBase model;
    private float limbSwing;
    private float limbSwingAmount;
    private float ageInTicks;
    private  float netHeadYaw;
    private float headPitch;
    private  float scaleFactor;
    public EntityModelPoseEvent(ModelBase modelBase,float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        super(entityIn);
        this.model=modelBase;
        this.limbSwing=limbSwing;
        this.limbSwingAmount=limbSwingAmount;
        this.ageInTicks=ageInTicks;
        this.netHeadYaw=netHeadYaw;
        this.headPitch=headPitch;
        this.scaleFactor=scaleFactor;
    }

    public ModelBase getModel() {
        return model;
    }

    public float getAgeInTicks() {
        return ageInTicks;
    }

    public float getLimbSwing() {
        return limbSwing;
    }

    public float getHeadPitch() {
        return headPitch;
    }

    public float getLimbSwingAmount() {
        return limbSwingAmount;
    }

    public float getNetHeadYaw() {
        return netHeadYaw;
    }

    public float getScaleFactor() {
        return scaleFactor;
    }
}
