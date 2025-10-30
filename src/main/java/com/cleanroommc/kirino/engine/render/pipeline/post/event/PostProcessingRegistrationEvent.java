package com.cleanroommc.kirino.engine.render.pipeline.post.event;

import com.cleanroommc.kirino.engine.render.pipeline.Renderer;
import com.cleanroommc.kirino.engine.render.pipeline.post.subpasses.AbstractPostProcessingPass;
import com.cleanroommc.kirino.engine.render.pipeline.state.PipelineStateObject;
import com.cleanroommc.kirino.engine.render.shader.ShaderRegistry;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import com.cleanroommc.kirino.gl.vao.VAO;
import com.cleanroommc.kirino.utils.Reference;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.List;

public class PostProcessingRegistrationEvent extends Event {
    private final List<Triple<String, ShaderProgram, TriFunction<Renderer, PipelineStateObject, Reference<VAO>, AbstractPostProcessingPass>>> postProcessingEntries = new ArrayList<>();
    private final ShaderRegistry shaderRegistry;

    public PostProcessingRegistrationEvent(ShaderRegistry shaderRegistry) {
        this.shaderRegistry = shaderRegistry;
    }

    public ShaderProgram newShaderProgram(String... shaderRLs) {
        return shaderRegistry.newShaderProgram(shaderRLs);
    }

    public void register(String subpassName, ShaderProgram shaderProgram, TriFunction<Renderer, PipelineStateObject, Reference<VAO>, AbstractPostProcessingPass> subpassCtor) {
        postProcessingEntries.add(Triple.of(subpassName, shaderProgram, subpassCtor));
    }
}
