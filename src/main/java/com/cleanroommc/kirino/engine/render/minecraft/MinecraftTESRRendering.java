package com.cleanroommc.kirino.engine.render.minecraft;

import com.cleanroommc.kirino.utils.ReflectionUtils;
import com.google.common.base.Preconditions;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

import java.util.Map;
import java.util.function.Function;

public class MinecraftTESRRendering {
    private final MinecraftCulling cullingPatch;
    private final Function<RenderGlobal, Map<Integer, DestroyBlockProgress>> damagedBlocks;

    @SuppressWarnings("unchecked")
    public MinecraftTESRRendering(MinecraftCulling cullingPatch) {
        this.cullingPatch = cullingPatch;
        damagedBlocks = (Function<RenderGlobal, Map<Integer, DestroyBlockProgress>>) ReflectionUtils.getDeclaredFieldGetter(RenderGlobal.class, "damagedBlocks", "field_72738_E");

        Preconditions.checkNotNull(damagedBlocks);
    }

    private int renderEntitiesStartupCounter = 2;

    private void preRenderDamagedBlocks() {
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 0.5F);
        GlStateManager.doPolygonOffset(-1.0F, -10.0F);
        GlStateManager.enablePolygonOffset();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableAlpha();
        GlStateManager.pushMatrix();
    }

    private void postRenderDamagedBlocks() {
        GlStateManager.disableAlpha();
        GlStateManager.doPolygonOffset(0.0F, 0.0F);
        GlStateManager.disablePolygonOffset();
        GlStateManager.enableAlpha();
        GlStateManager.depthMask(true);
        GlStateManager.popMatrix();
    }

    /**
     * Must call {@link MinecraftCulling#collectEntitiesInView(Entity, ICamera, ChunkProviderClient, float)} before this method call.
     */
    public void renderTESRs(
            Entity renderViewEntity,
            ICamera camera,
            WorldClient world,
            FontRenderer fontRenderer,
            EntityRenderer entityRenderer,
            TextureManager textureManager,
            RayTraceResult objectMouseOver,
            RenderGlobal renderGlobal,
            float partialTicks,
            int forgeRenderPass) {

        if (renderEntitiesStartupCounter > 0) {
            if (forgeRenderPass > 0) {
                return;
            }
            renderEntitiesStartupCounter--;
            return;
        }

        double viewPosX = renderViewEntity.prevPosX + (renderViewEntity.posX - renderViewEntity.prevPosX) * (double) partialTicks;
        double viewPosY = renderViewEntity.prevPosY + (renderViewEntity.posY - renderViewEntity.prevPosY) * (double) partialTicks;
        double viewPosZ = renderViewEntity.prevPosZ + (renderViewEntity.posZ - renderViewEntity.prevPosZ) * (double) partialTicks;

        TileEntityRendererDispatcher.instance.prepare(world, textureManager, fontRenderer, renderViewEntity, objectMouseOver, partialTicks);
        TileEntityRendererDispatcher.staticPlayerX = viewPosX;
        TileEntityRendererDispatcher.staticPlayerY = viewPosY;
        TileEntityRendererDispatcher.staticPlayerZ = viewPosZ;

        entityRenderer.enableLightmap();

        RenderHelper.enableStandardItemLighting();

        TileEntityRendererDispatcher.instance.preDrawBatch();

        for (TileEntity tileEntity : cullingPatch.tileEntitiesInView) {
            if (!tileEntity.shouldRenderInPass(forgeRenderPass) || !camera.isBoundingBoxInFrustum(tileEntity.getRenderBoundingBox())) {
                continue;
            }

            TileEntityRendererDispatcher.instance.render(tileEntity, partialTicks, -1);
        }

        TileEntityRendererDispatcher.instance.drawBatch(forgeRenderPass);

        preRenderDamagedBlocks();

        for (DestroyBlockProgress destroyBlockProgress : damagedBlocks.apply(renderGlobal).values()) {
            BlockPos blockPos = destroyBlockProgress.getPosition();

            if (world.getBlockState(blockPos).getBlock().hasTileEntity()) {
                TileEntity tileEntity = world.getTileEntity(blockPos);

                if (tileEntity instanceof TileEntityChest chest) {
                    if (chest.adjacentChestXNeg != null) {
                        blockPos = blockPos.offset(EnumFacing.WEST);
                        tileEntity = world.getTileEntity(blockPos);
                    } else if (chest.adjacentChestZNeg != null) {
                        blockPos = blockPos.offset(EnumFacing.NORTH);
                        tileEntity = world.getTileEntity(blockPos);
                    }
                }

                IBlockState blockState = world.getBlockState(blockPos);

                if (tileEntity != null && blockState.hasCustomBreakingProgress()) {
                    TileEntityRendererDispatcher.instance.render(tileEntity, partialTicks, destroyBlockProgress.getPartialBlockDamage());
                }
            }
        }

        postRenderDamagedBlocks();
        entityRenderer.disableLightmap();
    }
}
