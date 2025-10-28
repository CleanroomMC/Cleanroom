package com.cleanroommc.kirino.engine.render.pipeline.pass.subpasses;

import com.cleanroommc.kirino.engine.render.pipeline.Renderer;
import com.cleanroommc.kirino.engine.render.pipeline.state.PipelineStateObject;
import com.cleanroommc.kirino.gl.vao.VAO;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.atomic.AtomicReference;

public class ToneMappingPass extends PostProcessingPass {
    /**
     * @param renderer              A global renderer
     * @param pso                   A pipeline state object (pipeline parameters)
     * @param fullscreenTriangleVao The global fullscreen triangle VAO
     */
    public ToneMappingPass(@NonNull Renderer renderer, @NonNull PipelineStateObject pso, @NonNull AtomicReference<VAO> fullscreenTriangleVao) {
        super(renderer, pso, fullscreenTriangleVao);
    }
}
