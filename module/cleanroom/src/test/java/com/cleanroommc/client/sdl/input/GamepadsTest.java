package com.cleanroommc.client.sdl.input;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GamepadsTest {

    private static int unsigned(short value) {
        return value & 0xFFFF;
    }

    @Test
    void rumbleStrengthClampsToUnsignedShortRange() {
        assertEquals(0, unsigned(Gamepad.toStrength(0.0F)));
        assertEquals(65535, unsigned(Gamepad.toStrength(1.0F)));
        assertEquals(65535, unsigned(Gamepad.toStrength(2.0F)));
        assertEquals(0, unsigned(Gamepad.toStrength(-1.0F)));
        assertEquals(32768, unsigned(Gamepad.toStrength(0.5F)));
    }

}
