package com.cleanroommc.cleanmix.service;

import com.cleanroommc.cleanmix.CleanMixModContainer;
import com.cleanroommc.common.CleanroomEnvironment;
import com.cleanroommc.discovery.CleanroomModDiscoverer;
import com.google.common.base.Strings;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraftforge.fml.common.launcher.FMLTweaker;
import net.minecraftforge.fml.relauncher.CoreModManager;
import org.spongepowered.asm.launch.platform.container.ContainerHandleURI;
import org.spongepowered.asm.launch.platform.container.IContainerHandle;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.extensibility.IRemapper;

import org.spongepowered.asm.obfuscation.mapping.mcp.Srg2McpRemapper;
import org.spongepowered.asm.obfuscation.mapping.remap.CleanroomRemapper;
import org.spongepowered.asm.service.mojang.AbstractMixinServiceLaunchWrapper;
import org.spongepowered.asm.service.mojang.MixinAuditFile;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;

public class CleanMixService extends AbstractMixinServiceLaunchWrapper {

    private boolean initialized;

    @Override
    public String getName() {
        return "CleanMix";
    }

    @Override
    public String getSideName() {
        return CleanroomEnvironment.side().name();
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
    public IContainerHandle getPrimaryContainer() {
        return new ContainerHandleURI(CleanMixModContainer.location().toURI());
    }

    /**
     * @see CoreModManager#handleLaunch(File, LaunchClassLoader, FMLTweaker)
     * @see CleanroomModDiscoverer#discoverMixinMods() 
     */
    @Override
    public Collection<IContainerHandle> getMixinContainers() {
        return Collections.emptyList();
    }

    @Override
    protected MixinAuditFile createAuditLog() {
        return new MixinAuditFile("cleanmix.log", "cleanmix.auditTrail");
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
