package net.minecraftforge.debug.client.model;

import com.cleanroommc.event.ModelPoseHackTargetRegistryEvent;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraftforge.client.event.EntityModelPoseEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = ModelHackRegistryTest.MODID,name="ForgeTestModelHack")
public class ModelHackRegistryTest {
    public static final String MODID = "forge_test_modelhack";
    public static final String VERSION = "1.0";

    public static final boolean ENABLE = false;
    public static void init(){

    }
    @Subscribe
    public void onEvent(ModelPoseHackTargetRegistryEvent event){
        event.add("net.minecraft.client.model.ModelBiped");
    }
    @Mod.EventBusSubscriber(modid = MODID)
    public static class Handler{
        @SubscribeEvent
        public static void onEvent(EntityModelPoseEvent event){
            ModelBase modelBase=event.getModel();
            if (modelBase instanceof ModelBiped modelBiped){
                modelBiped.bipedHead.rotateAngleX=System.nanoTime()%300;
            }
        }
    }
}
