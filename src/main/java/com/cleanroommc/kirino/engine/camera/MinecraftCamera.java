package com.cleanroommc.kirino.engine.camera;

import com.cleanroommc.kirino.utils.reflection.ReflectionUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.nio.FloatBuffer;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public class MinecraftCamera implements ICamera {
    private final Supplier<FloatBuffer> projectionBuffer;
    private final Supplier<FloatBuffer> viewRotationBuffer;
    private final Function<Minecraft, Float> partialTicksPaused;

    @SuppressWarnings("unchecked")
    public MinecraftCamera() {
        projectionBuffer = (Supplier<FloatBuffer>) ReflectionUtils.getDeclaredFieldGetter(ActiveRenderInfo.class, "PROJECTION", "field_178813_c");
        viewRotationBuffer = (Supplier<FloatBuffer>) ReflectionUtils.getDeclaredFieldGetter(ActiveRenderInfo.class, "MODELVIEW", "field_178812_b");
        partialTicksPaused = (Function<Minecraft, Float>) ReflectionUtils.getDeclaredFieldGetter(Minecraft.class, "renderPartialTicksPaused", "field_193996_ah");

        Objects.requireNonNull(projectionBuffer);
        Objects.requireNonNull(viewRotationBuffer);
        Objects.requireNonNull(partialTicksPaused);
    }

    public double getPartialTicks() {
        Minecraft minecraft = Minecraft.getMinecraft();
        return minecraft.isGamePaused() ? partialTicksPaused.apply(minecraft) : minecraft.getRenderPartialTicks();
    }

    @Override
    public Matrix4f getProjectionMatrix() {
        return new Matrix4f(projectionBuffer.get());
    }

    @Override
    public FloatBuffer getProjectionBuffer() {
        return projectionBuffer.get();
    }

    @Override
    public Matrix4f getViewRotationMatrix() {
        return new Matrix4f(viewRotationBuffer.get());
    }

    @Override
    public FloatBuffer getViewRotationBuffer() {
        return viewRotationBuffer.get();
    }

    @Override
    public Vector3f getWorldOffset() {
        Entity camera = Minecraft.getMinecraft().getRenderViewEntity();
        if (camera == null) {
            camera = Minecraft.getMinecraft().player;
        }
        double partialTicks = getPartialTicks();
        double camX = camera.lastTickPosX + (camera.posX - camera.lastTickPosX) * partialTicks;
        double camY = camera.lastTickPosY + (camera.posY - camera.lastTickPosY) * partialTicks;
        double camZ = camera.lastTickPosZ + (camera.posZ - camera.lastTickPosZ) * partialTicks;
        return new Vector3f((float)camX, (float)camY, (float)camZ);
    }
}
