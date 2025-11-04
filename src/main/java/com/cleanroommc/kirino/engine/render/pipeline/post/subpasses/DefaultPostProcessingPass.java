package com.cleanroommc.kirino.engine.render.pipeline.post.subpasses;

import com.cleanroommc.kirino.engine.render.pipeline.Renderer;
import com.cleanroommc.kirino.engine.render.pipeline.state.PipelineStateObject;
import com.cleanroommc.kirino.gl.vao.VAO;
import com.cleanroommc.kirino.utils.Reference;
import org.jspecify.annotations.NonNull;

public class DefaultPostProcessingPass extends AbstractPostProcessingPass {
    /**
     * @param renderer              A global renderer
     * @param pso                   A pipeline state object (pipeline parameters)
     * @param fullscreenTriangleVao The global fullscreen triangle VAO
     */
    public DefaultPostProcessingPass(@NonNull Renderer renderer, @NonNull PipelineStateObject pso, @NonNull Reference<VAO> fullscreenTriangleVao) {
        super(renderer, pso, fullscreenTriangleVao);
    }
}
