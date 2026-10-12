/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

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
