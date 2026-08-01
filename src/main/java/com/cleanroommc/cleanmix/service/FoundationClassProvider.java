package com.cleanroommc.cleanmix.service;

import net.minecraft.launchwrapper.Launch;
import org.spongepowered.asm.service.IClassProvider;

import java.net.URL;

final class FoundationClassProvider implements IClassProvider {

    @Override
    @Deprecated
    public URL[] getClassPath() {
        return Launch.classLoader.getSources().toArray(new URL[0]);
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        return Launch.classLoader.findClass(name);
    }

    @Override
    public Class<?> findClass(String name, boolean initialize) throws ClassNotFoundException {
        return Class.forName(name, initialize, Launch.classLoader);
    }

    @Override
    public Class<?> findAgentClass(String name, boolean initialize) throws ClassNotFoundException {
        return Class.forName(name, initialize, Launch.class.getClassLoader());
    }

}
