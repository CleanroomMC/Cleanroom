package com.cleanroommc.catalogue.platform;

import com.cleanroommc.catalogue.CatalogueConfig;
import com.cleanroommc.catalogue.client.Branding;
import com.cleanroommc.catalogue.client.CleanroomModData;
import com.cleanroommc.catalogue.client.IModData;
import com.cleanroommc.catalogue.platform.services.IPlatformHelper;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraftforge.fml.common.Loader;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Author: MrCrayfish
 */
public class CleanroomPlatformHelper implements IPlatformHelper {

    @Override
    public List<IModData> getAllModData() {
        return Loader.instance().getActiveModList().stream().map(CleanroomModData::new).collect(Collectors.toList());
    }

    @Override
    public File getModDirectory() {
        return Loader.instance().getConfigDir().getParentFile().toPath().resolve("mods").toFile();
    }

    @Override
    public Path getConfigDirectory() {
        return Loader.instance().getConfigDir().toPath();
    }

    @Override
    public BufferedImage loadImageFromModResource(String modid, String resource) throws IOException {
        InputStream is = getClass().getResourceAsStream(resource);
        return is != null ? TextureUtil.readBufferedImage(is) : null;
    }

    @Override
    public boolean isModLoaded(String modId) {
        return Loader.isModLoaded(modId);
    }

    @Override
    public boolean getEnableBannerLimit() {
        return CatalogueConfig.enableBannerLimit;
    }

    @Override
    public Branding.BannerLimit getBannerLimit() {
        return new Branding.BannerLimit(CatalogueConfig.bannerMaxWidth, CatalogueConfig.bannerMaxHeight);
    }
}
