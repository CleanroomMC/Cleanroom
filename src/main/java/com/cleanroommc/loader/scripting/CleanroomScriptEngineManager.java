package com.cleanroommc.loader.scripting;

import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;
import scala.tools.nsc.interpreter.IMain;

import javax.script.ScriptEngineManager;

public class CleanroomScriptEngineManager extends ScriptEngineManager {

    public CleanroomScriptEngineManager() {
        super(Thread.currentThread().getContextClassLoader());
    }

    public CleanroomScriptEngineManager(ClassLoader loader) {
        initialize();
    }

    private void initialize() {
        IMain.Factory scala = new IMain.Factory();
        for (String name : scala.getNames()) {
            this.registerEngineName(name, scala);
        }
        for (String mime : scala.getMimeTypes()) {
            this.registerEngineMimeType(mime, scala);
        }
        for (String ext : scala.getExtensions()) {
            this.registerEngineExtension(ext, scala);
        }
        var js = new NashornScriptEngineFactory();
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
