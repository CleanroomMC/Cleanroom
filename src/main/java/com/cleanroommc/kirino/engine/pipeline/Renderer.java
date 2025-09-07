package com.cleanroommc.kirino.engine.pipeline;

import com.cleanroommc.kirino.engine.pipeline.command.DrawCommand;
import com.cleanroommc.kirino.engine.pipeline.state.BlendState;
import com.cleanroommc.kirino.engine.pipeline.state.DepthState;
import com.cleanroommc.kirino.engine.pipeline.state.PipelineStateObject;
import com.cleanroommc.kirino.engine.pipeline.state.RasterState;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;

public final class Renderer {
    public void bindPipeline(PipelineStateObject pso) {
        pso.shaderProgram().use();
        applyBlend(pso.blendState());
        applyDepth(pso.depthState());
        applyRaster(pso.rasterState());
    }

    private void applyRaster(RasterState r) {
        if (r.cullEnable()) {
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glCullFace(r.cullFace());
        } else {
            GL11.glDisable(GL11.GL_CULL_FACE);
        }
        GL11.glFrontFace(r.frontFace());
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, r.polygonMode());
        if (r.polygonOffset()) {
            GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
            GL11.glPolygonOffset(r.polygonOffsetFactor(), r.polygonOffsetUnits());
        } else {
            GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
        }
    }

    private void applyDepth(DepthState d) {
        if (d.depthTest()) {
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthFunc(d.depthFunc());
        } else {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
        }
        GL11.glDepthMask(d.depthWrite());
    }

    private void applyBlend(BlendState b) {
        if (!b.enabled()) {
            GL11.glDisable(GL11.GL_BLEND);
            return;
        }
        GL11.glEnable(GL11.GL_BLEND);
        if (b.separate()) {
            GL14.glBlendFuncSeparate(b.srcRGB(), b.dstRGB(), b.srcAlpha(), b.dstAlpha());
            GL20.glBlendEquationSeparate(b.eqRGB(), b.eqAlpha());
        } else {
            GL11.glBlendFunc(b.srcRGB(), b.dstRGB());
            GL14.glBlendEquation(b.eqRGB());
        }
        int mask = b.colorWriteMask();
        boolean r = (mask & 0b0001) != 0;
        boolean g = (mask & 0b0010) != 0;
        boolean _b = (mask & 0b0100) != 0;
        boolean a = (mask & 0b1000) != 0;
        GL11.glColorMask(r, g, _b, a);
    }

    public void draw(DrawCommand command) {

    }

    public void drawMultiIndirect() {

    }
}
