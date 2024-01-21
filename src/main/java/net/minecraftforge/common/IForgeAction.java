package net.minecraftforge.common;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IForgeAction {
    IForgeAction EMPTY=new IForgeAction() {};

    /**
     * @return The ArmPose used for render.<br>
     * for example {@link ModelBiped.ArmPose#BOW_AND_ARROW} for {@link net.minecraft.init.Items#BOW}
     */
    @SideOnly(Side.CLIENT)
    default ModelBiped.ArmPose getBipedArmPose(){
        return ModelBiped.ArmPose.ITEM;
    }

    /**
     * @param stack the stack is being used by player.
     * @param eatingParticleCount count.<br>Use finish:16.<br>When the usage time does not exceed 25 hours, one quarter of every 4 is 4, and the rest is 0
     */
    default void updateItemUse(ItemStack stack, int eatingParticleCount){}
}
