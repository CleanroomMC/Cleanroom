package com.cleanroommc.kirino.engine.shader;

import com.cleanroommc.kirino.gl.shader.Shader;
import com.cleanroommc.kirino.gl.shader.ShaderType;
import com.cleanroommc.kirino.utils.MinecraftResourceUtils;

import java.util.HashMap;
import java.util.Map;

public class ShaderRegistry {
    // key: rl
    private final Map<String, Shader> shaders = new HashMap<>();

//    public static boolean loadShader(String rl, ShaderType shaderType) {
//        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
//        StackTraceElement caller = null;
//        if (stackTrace.length > 2) caller = stackTrace[2];
//
//        String source = MinecraftResourceUtils.read(rl, true);
//
//        Shader shader = shaders.get(rl);
//        if (shader == null) {
//            shader = new Shader(source, rl, shaderType);
//
//            return true;
//        }
//
//        return false;
//    }
}
