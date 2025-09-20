package com.cleanroommc.kirino.engine.render.camera;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.nio.FloatBuffer;

public class FreeCamera implements ICamera{
    @Override
    public Matrix4f getProjectionMatrix() {
        return null;
    }

    @Override
    public FloatBuffer getProjectionBuffer() {
        return null;
    }

    @Override
    public Matrix4f getViewRotationMatrix() {
        return null;
    }

    @Override
    public FloatBuffer getViewRotationBuffer() {
        return null;
    }

    @Override
    public Vector3f getWorldOffset() {
        return null;
    }
}
