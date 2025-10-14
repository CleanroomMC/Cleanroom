package com.cleanroommc.kirino.engine.render.pipeline;

import com.cleanroommc.kirino.engine.render.pipeline.command.LowLevelDC;
import com.cleanroommc.kirino.engine.render.pipeline.state.BlendState;
import com.cleanroommc.kirino.engine.render.pipeline.state.DepthState;
import com.cleanroommc.kirino.engine.render.pipeline.state.PipelineStateObject;
import com.cleanroommc.kirino.engine.render.pipeline.state.RasterState;
import org.lwjgl.opengl.*;

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

    /**
     * Accepts a low-level draw command and submits the corresponding OpenGL command to GPU.
     * Notice, {@link LowLevelDC} with type <code>MULTI_ELEMENTS_INDIRECT_UNIT</code> is an auxiliary command and will be ignored here.
     *
     * @param command The low-level draw command
     */
    public void draw(LowLevelDC command) {
        if (command.type == LowLevelDC.DrawType.ELEMENTS ||
                command.type == LowLevelDC.DrawType.ELEMENTS_INSTANCED ||
                command.type == LowLevelDC.DrawType.MULTI_ELEMENTS_INDIRECT) {

            GL30.glBindVertexArray(command.vao);
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, command.ebo);

            switch (command.type) {
                case ELEMENTS -> {
                    GL11.glDrawElements(command.mode, command.indicesCount, command.elementType, command.eboOffset);
                }
                case ELEMENTS_INSTANCED -> {
                    GL31.glDrawElementsInstanced(command.mode, command.indicesCount, command.elementType, command.eboOffset, command.instanceCount);
                }
                case MULTI_ELEMENTS_INDIRECT -> {
                    GL15.glBindBuffer(GL40.GL_DRAW_INDIRECT_BUFFER, command.idb);
                    GL43.glMultiDrawElementsIndirect(command.mode, command.elementType, command.idbOffset, command.instanceCount, command.idbStride);
                }
            }
        }
    }
}
