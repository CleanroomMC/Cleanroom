package com.cleanroommc.cleanmix.service;

import com.cleanroommc.discovery.CleanroomModDiscoverer;
import com.google.common.base.Strings;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.extensibility.IRemapper;

import org.spongepowered.asm.obfuscation.mapping.mcp.Srg2McpRemapper;
import org.spongepowered.asm.obfuscation.mapping.remap.CleanroomRemapper;
import org.spongepowered.asm.service.mojang.AbstractMixinServiceLaunchWrapper;

import java.io.File;
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
        String devConfigs = System.getProperty("crl.dev.mixin");
        if (!Strings.isNullOrEmpty(devConfigs)) {
            for (String singleMixinConfig : devConfigs.split(",")) {
                Mixins.addConfiguration(singleMixinConfig.trim());
            }
        }
    }

    @Override
    protected String resolveSourceId(URI source) {
        if ("file".equals(source.getScheme())) {
            try {
                String modId = CleanroomModDiscoverer.instance().modFromSource(new File(source));
                if (modId != null) {
                    return modId;
                }
            } catch (IllegalArgumentException ignored) { }
        }
        return null;
    }

}
