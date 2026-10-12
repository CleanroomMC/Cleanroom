/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

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
