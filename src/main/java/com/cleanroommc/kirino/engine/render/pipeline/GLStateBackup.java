package com.cleanroommc.kirino.engine.render.pipeline;

import com.cleanroommc.kirino.engine.render.pipeline.state.PipelineStateObject;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * It covers all the options that {@link PipelineStateObject} provides.
 */
public final class GLStateBackup {
    private final IntBuffer intBuf = BufferUtils.createIntBuffer(1);
    private final FloatBuffer floatBuf = BufferUtils.createFloatBuffer(2);
    private final ByteBuffer colorMaskBuf = BufferUtils.createByteBuffer(4);

    // shader program
    private int shaderProgram;

    // blend
    private boolean blendEnabled;
    private final int[] blendFunc = new int[4]; // srcRGB, dstRGB, srcAlpha, dstAlpha
    private final int[] blendEq = new int[2]; // eqRGB, eqAlpha
    private final boolean[] colorMask = new boolean[4];

    // depth
    private boolean depthEnabled;
    private boolean depthWrite;
    private int depthFunc;

    // raster
    private boolean cullEnabled;
    private int cullFace;
    private int frontFace;
    private boolean polyOffsetEnabled;
    private final int[] polygonMode = new int[2]; // front, back
    private float polyOffsetFactor;
    private float polyOffsetUnits;

    private boolean stored = false;

    public boolean isStored() {
        return stored;
    }

    public void storeStates() {
        // blend
        blendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
        intBuf.clear();
        GL11C.glGetIntegerv(GL14.GL_BLEND_SRC_RGB, intBuf);
        blendFunc[0] = intBuf.get(0);
        intBuf.clear();
        GL11C.glGetIntegerv(GL14.GL_BLEND_DST_RGB, intBuf);
        blendFunc[1] = intBuf.get(0);
        intBuf.clear();
        GL11C.glGetIntegerv(GL14.GL_BLEND_SRC_ALPHA, intBuf);
        blendFunc[2] = intBuf.get(0);
        intBuf.clear();
        GL11C.glGetIntegerv(GL14.GL_BLEND_DST_ALPHA, intBuf);
        blendFunc[3] = intBuf.get(0);
        intBuf.clear();
        GL11C.glGetIntegerv(GL20.GL_BLEND_EQUATION_RGB, intBuf);
        blendEq[0] = intBuf.get(0);
        intBuf.clear();
        GL11C.glGetIntegerv(GL20.GL_BLEND_EQUATION_ALPHA, intBuf);
        blendEq[1] = intBuf.get(0);
        colorMaskBuf.clear();
        GL11C.glGetBooleanv(GL11.GL_COLOR_WRITEMASK, colorMaskBuf);
        colorMask[0] = colorMaskBuf.get(0) != 0;
        colorMask[1] = colorMaskBuf.get(1) != 0;
        colorMask[2] = colorMaskBuf.get(2) != 0;
        colorMask[3] = colorMaskBuf.get(3) != 0;

        // depth
        depthEnabled = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        depthWrite = GL11C.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        intBuf.clear();
        GL11C.glGetIntegerv(GL11.GL_DEPTH_FUNC, intBuf);
        depthFunc = intBuf.get(0);

        // raster
        cullEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        intBuf.clear();
        GL11C.glGetIntegerv(GL11.GL_CULL_FACE_MODE, intBuf);
        cullFace = intBuf.get(0);
        intBuf.clear();
        GL11C.glGetIntegerv(GL11.GL_FRONT_FACE, intBuf);
        frontFace = intBuf.get(0);
        polyOffsetEnabled = GL11.glIsEnabled(GL11.GL_POLYGON_OFFSET_FILL);
        GL11C.glGetIntegerv(GL11.GL_POLYGON_MODE, polygonMode);
        floatBuf.clear();
        GL11C.glGetFloatv(GL11.GL_POLYGON_OFFSET_FACTOR, floatBuf);
        polyOffsetFactor = floatBuf.get(0);
        floatBuf.clear();
        GL11C.glGetFloatv(GL11.GL_POLYGON_OFFSET_UNITS, floatBuf);
        polyOffsetUnits = floatBuf.get(0);

        // shader program
        intBuf.clear();
        GL11C.glGetIntegerv(GL20.GL_CURRENT_PROGRAM, intBuf);
        shaderProgram = intBuf.get(0);

        stored = true;
    }

    public void restoreStates() {
        // blend
        if (blendEnabled) {
            GL11.glEnable(GL11.GL_BLEND);
        } else {
            GL11.glDisable(GL11.GL_BLEND);
        }
        GL14.glBlendFuncSeparate(blendFunc[0], blendFunc[1], blendFunc[2], blendFunc[3]);
        GL20.glBlendEquationSeparate(blendEq[0], blendEq[1]);
        GL11.glColorMask(colorMask[0], colorMask[1], colorMask[2], colorMask[3]);

        // depth
        if (depthEnabled) {
            GL11.glEnable(GL11.GL_DEPTH_TEST);
        } else {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
        }
        GL11.glDepthFunc(depthFunc);
        GL11.glDepthMask(depthWrite);

        // raster
        if (cullEnabled) {
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glCullFace(cullFace);
        } else {
            GL11.glDisable(GL11.GL_CULL_FACE);
        }
        GL11.glFrontFace(frontFace);
        GL11.glPolygonMode(GL11.GL_FRONT, polygonMode[0]);
        GL11.glPolygonMode(GL11.GL_BACK, polygonMode[1]);
        if (polyOffsetEnabled) {
            GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
            GL11.glPolygonOffset(polyOffsetFactor, polyOffsetUnits);
        } else {
            GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
        }

        // shader program
        GL20.glUseProgram(shaderProgram);
    }
}
