package net.minecraftforge.fml.common;

import com.google.common.base.Strings;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.discovery.ModCandidate;
import net.minecraftforge.fml.relauncher.MixinBooterPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.extensibility.MixinContextQuery;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Set;

public final class FMLContextQuery extends MixinContextQuery {

    private static final FMLContextQuery INSTANCE = new FMLContextQuery();

    public static void init() { }

    private final ASMDataTable asmDataTable;

    private FMLContextQuery() {
        super();
        ASMDataTable asmDataTable = null;
        try {
            Field modApiManager$dataTable = ModAPIManager.class.getDeclaredField("dataTable");
            modApiManager$dataTable.setAccessible(true);
            asmDataTable = (ASMDataTable) modApiManager$dataTable.get(ModAPIManager.INSTANCE);
        } catch (ReflectiveOperationException e) {
            FMLLog.log.fatal("Not able to reflect ModAPIManager#dataTable", e);
        }
        this.asmDataTable = asmDataTable;
    }

    private static String getResourceName(IMixinConfig config) {
        String resource = Launch.classLoader.getResource(config.getName()).getPath();
        if (resource.contains("!/")) {
            String filePath = resource.split("!/")[0];
            String[] parts = filePath.split("/");
            if (parts.length != 0) {
                return parts[parts.length - 1];
            }
        }
        return null;
    }

    @Override
    public String getOwner(IMixinConfig config) {
        if (this.asmDataTable == null) {
            return getSanitizedModIdFromResource(config);
        } else {
            return getCandidates(config).stream()
                    .map(ModCandidate::getContainedMods)
                    .flatMap(Collection::stream)
                    .map(ModContainer::getModId)
                    .filter(modId -> !Strings.isNullOrEmpty(modId))
                    .findFirst()
                    .orElseGet(() -> getSanitizedModIdFromResource(config));
        }
    }

    @Override
    public String getLocation(IMixinConfig config) {
        if (this.asmDataTable == null) {
            return getSanitizedModIdFromResource(config);
        } else {
            return getCandidates(config).stream()
                    .map(ModCandidate::getClassPathRoot)
                    .map(File::getName)
                    .findFirst()
                    .orElse(null);
        }
    }

    private Set<ModCandidate> getCandidates(IMixinConfig config) {
        String pkg = config.getMixinPackage();
        pkg = pkg.charAt(pkg.length() - 1) == '.' ? pkg.substring(0, pkg.length() - 1) : pkg;
        return this.asmDataTable.getCandidatesFor(pkg);
    }

    private String getSanitizedModIdFromResource(IMixinConfig config) {
        String baseModId = getResourceName(config);
        if (baseModId == null) {
            return null;
        }
        if (baseModId.endsWith(".jar") || baseModId.endsWith(".zip")) {
            baseModId = baseModId.substring(0, baseModId.length() - 4);
        }
        // Wipe Minecraft Versioning
        baseModId = baseModId.replaceAll("1\\.12(\\.2)?", "");
        StringBuilder sanitizedModId = new StringBuilder();
        for (int i = 0; i < baseModId.length(); i++) {
            char character = baseModId.charAt(i);
            if ((character >= 'a' && character <= 'z') || (character >= 'A' && character <= 'Z') || (i > 0 && character >= '0' && character <= '9')) {
                sanitizedModId.append(character);
            } else {
                sanitizedModId.append('_');
            }
        }
        return sanitizedModId.toString();
    }

}