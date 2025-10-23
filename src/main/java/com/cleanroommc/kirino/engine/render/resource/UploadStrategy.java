package com.cleanroommc.kirino.engine.render.resource;

public enum UploadStrategy {
    /**
     * High performance strategy
     */
    PERSISTENT,
    /**
     * Performance heavy strategy. Only use it for debug purposes
     */
    TEMPORARY
}
