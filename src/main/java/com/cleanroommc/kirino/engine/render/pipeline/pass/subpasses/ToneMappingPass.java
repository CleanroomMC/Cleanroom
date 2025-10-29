package com.cleanroommc.kirino.engine.render.pipeline.pass.subpasses;

import com.cleanroommc.kirino.engine.render.pipeline.Renderer;
import com.cleanroommc.kirino.engine.render.pipeline.state.PipelineStateObject;
import com.cleanroommc.kirino.gl.vao.VAO;
import com.cleanroommc.kirino.utils.Reference;
import org.jspecify.annotations.NonNull;

public class ToneMappingPass extends AbstractPostProcessingPass {
    /**
     * @param renderer              A global renderer
     * @param pso                   A pipeline state object (pipeline parameters)
     * @param fullscreenTriangleVao The global fullscreen triangle VAO
     */
    public ToneMappingPass(@NonNull Renderer renderer, @NonNull PipelineStateObject pso, @NonNull Reference<VAO> fullscreenTriangleVao) {
        super(renderer, pso, fullscreenTriangleVao);
    }
}
