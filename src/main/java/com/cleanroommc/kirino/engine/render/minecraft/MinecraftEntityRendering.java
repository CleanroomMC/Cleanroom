package com.cleanroommc.kirino.engine.render.minecraft;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class MinecraftEntityRendering {
    private final MinecraftCulling cullingPatch;

    public MinecraftEntityRendering(MinecraftCulling cullingPatch) {
        this.cullingPatch = cullingPatch;
    }

    private int renderEntitiesStartupCounter = 2;

    private int countEntitiesTotal = 0;
    private int countEntitiesRendered = 0;
    private int countEntitiesHidden = 0;

    private boolean isEntityOutlineActive(Entity entityIn, Entity viewer, EntityPlayerSP player, ICamera camera, GameSettings gameSettings) {
        boolean flag = viewer instanceof EntityLivingBase && ((EntityLivingBase) viewer).isPlayerSleeping();

        if (entityIn == viewer && gameSettings.thirdPersonView == 0 && !flag) {
            return false;
        } else if (entityIn.isGlowing()) {
            return true;
        } else if (player.isSpectator() && gameSettings.keyBindSpectatorOutlines.isKeyDown() && entityIn instanceof EntityPlayer) {
            return entityIn.ignoreFrustumCheck || camera.isBoundingBoxInFrustum(entityIn.getEntityBoundingBox()) || entityIn.isRidingOrBeingRiddenBy(player);
        } else {
            return false;
        }
    }

    /**
     * Must call {@link MinecraftCulling#collectEntitiesInView(Entity, ICamera, ChunkProviderClient, float)} before this method call.
     */
    public void renderEntities(
            Entity renderViewEntity,
            Entity pointedEntity,
            EntityPlayerSP player,
            ICamera camera,
            GameSettings gameSettings,
            WorldClient world,
            FontRenderer fontRenderer,
            RenderManager renderManager,
            EntityRenderer entityRenderer,
            float partialTicks,
            int forgeRenderPass) {

        if (this.renderEntitiesStartupCounter > 0) {
            if (forgeRenderPass > 0) {
                return;
            }
            renderEntitiesStartupCounter--;
            return;
        }

        if (forgeRenderPass == 0) {
            countEntitiesTotal = world.getLoadedEntityList().size();
            countEntitiesRendered = 0;
            countEntitiesHidden = 0;
        }

        double viewPosX = renderViewEntity.prevPosX + (renderViewEntity.posX - renderViewEntity.prevPosX) * (double) partialTicks;
        double viewPosY = renderViewEntity.prevPosY + (renderViewEntity.posY - renderViewEntity.prevPosY) * (double) partialTicks;
        double viewPosZ = renderViewEntity.prevPosZ + (renderViewEntity.posZ - renderViewEntity.prevPosZ) * (double) partialTicks;

        renderManager.cacheActiveRenderInfo(world, fontRenderer, renderViewEntity, pointedEntity, gameSettings, partialTicks);
        renderManager.setRenderPosition(viewPosX, viewPosY, viewPosZ);

        entityRenderer.enableLightmap();

        for (int i = 0; i < world.weatherEffects.size(); ++i) {
            Entity entity = world.weatherEffects.get(i);
            if (!entity.shouldRenderInPass(forgeRenderPass)) {
                continue;
            }

            countEntitiesRendered++;
            if (entity.isInRangeToRender3d(viewPosX, viewPosY, viewPosZ)) {
                renderManager.renderEntityStatic(entity, partialTicks, false);
            }
        }

        List<Entity> outlineEntities = new ArrayList<>();
        List<Entity> multipassEntities = new ArrayList<>();

        BlockPos.PooledMutableBlockPos blockPos = BlockPos.PooledMutableBlockPos.retain();
        for (Entity entity : cullingPatch.entitiesInView) {
            if (!entity.shouldRenderInPass(forgeRenderPass)) {
                continue;
            }

            boolean flag = renderManager.shouldRender(entity, camera, viewPosX, viewPosY, viewPosZ) || entity.isRidingOrBeingRiddenBy(player);

            if (flag) {
                boolean flag1 = renderViewEntity instanceof EntityLivingBase && ((EntityLivingBase) renderViewEntity).isPlayerSleeping();

                if ((entity != renderViewEntity || gameSettings.thirdPersonView != 0 || flag1) &&
                        (entity.posY < 0.0D || entity.posY >= 256.0D || world.isBlockLoaded(blockPos.setPos(entity)))) {
                    countEntitiesRendered++;
                    renderManager.renderEntityStatic(entity, partialTicks, false);

                    if (isEntityOutlineActive(entity, renderViewEntity, player, camera, gameSettings)) {
                        outlineEntities.add(entity);
                    }

                    if (renderManager.isRenderMultipass(entity)) {
                        multipassEntities.add(entity);
                    }
                }
            }
        }
        blockPos.release();

        if (!multipassEntities.isEmpty()) {
            for (Entity entity : multipassEntities) {
                renderManager.renderMultipass(entity, partialTicks);
            }
        }

        // todo: outline impl
//        if (forgeRenderPass == 0) {
//            if (this.isRenderEntityOutlines() && (!outlineEntities.isEmpty() || this.entityOutlinesRendered)) {
//                this.world.profiler.endStartSection("entityOutlines");
//                this.entityOutlineFramebuffer.framebufferClear();
//                this.entityOutlinesRendered = !outlineEntities.isEmpty();
//
//                if (!outlineEntities.isEmpty()) {
//                    GlStateManager.depthFunc(519);
//                    GlStateManager.disableFog();
//                    this.entityOutlineFramebuffer.bindFramebuffer(false);
//                    RenderHelper.disableStandardItemLighting();
//                    this.renderManager.setRenderOutlines(true);
//
//                    for (int j = 0; j < outlineEntities.size(); ++j) {
//                        this.renderManager.renderEntityStatic(outlineEntities.get(j), partialTicks, false);
//                    }
//
//                    this.renderManager.setRenderOutlines(false);
//                    RenderHelper.enableStandardItemLighting();
//                    GlStateManager.depthMask(false);
//                    this.entityOutlineShader.render(partialTicks);
//                    GlStateManager.enableLighting();
//                    GlStateManager.depthMask(true);
//                    GlStateManager.enableFog();
//                    GlStateManager.enableBlend();
//                    GlStateManager.enableColorMaterial();
//                    GlStateManager.depthFunc(515);
//                    GlStateManager.enableDepth();
//                    GlStateManager.enableAlpha();
//                }
//
//                this.mc.getFramebuffer().bindFramebuffer(false);
//            }
//        }

        entityRenderer.disableLightmap();
    }
}
