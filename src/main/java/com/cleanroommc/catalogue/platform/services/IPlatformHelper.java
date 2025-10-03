package com.cleanroommc.catalogue.platform.services;

import com.cleanroommc.catalogue.client.Branding;
import com.cleanroommc.catalogue.client.IModData;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface IPlatformHelper {
    List<IModData> getAllModData();

    File getModDirectory();

    Path getConfigDirectory();

    BufferedImage loadImageFromModResource(String modid, String resource) throws IOException;

    boolean isModLoaded(String modId);

    boolean getEnableBannerLimit();

    Branding.BannerLimit getBannerLimit();
}
