package com.cleanroommc.loader.scripting;

import org.mozilla.javascript.engine.RhinoScriptEngineFactory;
import scala.tools.nsc.interpreter.Scripted;

import javax.script.ScriptEngineManager;

public class CleanroomScriptEngineManager extends ScriptEngineManager {

    public CleanroomScriptEngineManager() {
        super(Thread.currentThread().getContextClassLoader());
    }

    public CleanroomScriptEngineManager(ClassLoader loader) {
        initialize();
    }

    private void initialize() {
        var scala = new Scripted.Factory();
        for (String name : scala.getNames()) {
            this.registerEngineName(name, scala);
        }
        for (String mime : scala.getMimeTypes()) {
            this.registerEngineMimeType(mime, scala);
        }
        for (String ext : scala.getExtensions()) {
            this.registerEngineExtension(ext, scala);
        }
        var js = new RhinoScriptEngineFactory();
        // TODO: evaluate if this will hurt anything
        this.registerEngineName("nashorn", js);
        this.registerEngineName("Nashorn", js);
        this.registerEngineName("js", js);
        this.registerEngineName("JS", js);
        this.registerEngineName("ecmascript", js);
        this.registerEngineName("ECMAScript", js);
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
