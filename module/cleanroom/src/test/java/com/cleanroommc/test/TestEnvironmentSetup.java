package com.cleanroommc.test;

import com.cleanroommc.common.CleanroomEnvironment;
import net.minecraftforge.fml.relauncher.Side;
import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.LauncherSessionListener;

/**
 * Tests run without a tweaker, so nothing sets the side.
 * Anything touching FMLLaunchHandler fails its static init without this.
 */
public final class TestEnvironmentSetup implements LauncherSessionListener {

    @Override
    public void launcherSessionOpened(LauncherSession session) {
        CleanroomEnvironment.setSide(Side.CLIENT); // Fixme: when server?
    }

}
