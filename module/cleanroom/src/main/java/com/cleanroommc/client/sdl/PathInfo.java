package com.cleanroommc.client.sdl;

/**
 * Snapshot from {@code SDL_GetPathInfo}.
 *
 * @param kind file, directory, or other
 * @param size bytes, for files
 * @param createdNs SDL time, or {@code 0}
 * @param modifiedNs SDL time, or {@code 0}
 * @param accessedNs SDL time, or {@code 0}
 */
public record PathInfo(PathKind kind, long size, long createdNs, long modifiedNs, long accessedNs) { }
