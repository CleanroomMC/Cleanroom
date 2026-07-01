package com.cleanroommc.cleanmix.service;

import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.extensibility.IRemapper;

import org.spongepowered.asm.obfuscation.mapping.mcp.Srg2McpRemapper;
import org.spongepowered.asm.obfuscation.mapping.remap.CleanroomRemapper;
import org.spongepowered.asm.service.mojang.AbstractMixinServiceLaunchWrapper;

import java.net.URI;

public class CleanMixService extends AbstractMixinServiceLaunchWrapper {

    private boolean initialized;

    @Override
    public String getName() {
        return "CleanMix";
    }

    @Override
    public MixinEnvironment.CompatibilityLevel getMaxCompatibilityLevel() {
        return MixinEnvironment.CompatibilityLevel.JAVA_25;
    }

    @Override
    public void init() {
        if (this.initialized) {
            return;
        }
        this.initialized = true;
        super.init();
        IRemapper remapper = isDevelopment() ?
                new Srg2McpRemapper(MixinEnvironment.getDefaultEnvironment()) :
                new CleanroomRemapper<>(FMLDeobfuscatingRemapper.INSTANCE);
        MixinEnvironment.getDefaultEnvironment().getRemappers().add(remapper);
    }

    @Override
    protected String resolveSourceId(URI source) {
        if ("file".equals(source.getScheme())) {
            try {
                // Load CleanroomModDiscoverer
            } catch (IllegalArgumentException ignored) { }
        }
        return null;
    }

}
