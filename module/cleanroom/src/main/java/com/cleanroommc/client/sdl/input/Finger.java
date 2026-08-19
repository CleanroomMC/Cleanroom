package com.cleanroommc.client.sdl.input;

/** One touch finger. Coordinates are normalized {@code 0..1}. */
public record Finger(long id, float x, float y, float pressure) { }
