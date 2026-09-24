package com.cleanroommc.loader.scripting;

import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.ScriptEngineManager;

public class CleanroomScriptEngineManager extends ScriptEngineManager {

    public CleanroomScriptEngineManager() {
        super(Thread.currentThread().getContextClassLoader());
    }

    public CleanroomScriptEngineManager(ClassLoader loader) {
        initialize();
    }

    private void initialize() {
        NashornScriptEngineFactory js = new NashornScriptEngineFactory();
        for (String name : js.getNames()) {
            this.registerEngineName(name, js);
        }
        for (String mime : js.getMimeTypes()) {
            this.registerEngineMimeType(mime, js);
        }
        for (String ext : js.getExtensions()) {
            this.registerEngineExtension(ext, js);
        }
    }

}
