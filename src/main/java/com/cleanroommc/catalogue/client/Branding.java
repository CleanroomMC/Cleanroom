package com.cleanroommc.catalogue.client;

import com.cleanroommc.catalogue.CatalogueConstants;
import com.cleanroommc.catalogue.Utils;
import com.cleanroommc.catalogue.exception.InvalidBrandingImageException;
import com.cleanroommc.catalogue.exception.ModResourceNotFoundException;
import com.cleanroommc.catalogue.platform.ClientServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;

/// @author MrCrayfish
public record Branding(String prefix, int imageWidth, int imageHeight,
                       BiPredicate<BufferedImage, Branding> predicate,
                       Function<IModData, String> locator, boolean override) {
    public static final Branding ICON = new Branding("icon", ClientServices.PLATFORM.getIconLimit(), ClientServices.PLATFORM.getIconLimit(), ImagePredicate.SQUARE.and(ClientServices.PLATFORM.getEnableIconLimit() ? ImagePredicate.LESS_THAN_OR_EQUAL : ImagePredicate.ANY), IModData::getImageIcon, false);
    public static final Branding BANNER = new Branding("banner", ClientServices.PLATFORM.getBannerLimit().maxWidth(), ClientServices.PLATFORM.getBannerLimit().maxHeight(), ClientServices.PLATFORM.getEnableBannerLimit() ? ImagePredicate.LESS_THAN_OR_EQUAL : ImagePredicate.ANY, IModData::getBanner, false);
    public static final Branding BACKGROUND = new Branding("background", 512, 256, ImagePredicate.EQUAL, IModData::getBackground, true);

    public Optional<ImageInfo> loadResource(IModData data) {
        String resource = this.locator.apply(data);
        if (resource == null || resource.isBlank()) return Optional.empty();

        String modId = data.getModId();
        BufferedImage image;
        try {
            IResourcePack resourcePack = data.getResourcePack();
            if (this.equals(Branding.BANNER) && resourcePack != null && !resource.startsWith("/")) {
                image = resourcePack.getPackImage();
            } else {
                resource = resource.startsWith("/") ? resource : "/" + resource;
                image = ClientServices.PLATFORM.loadImageFromModResource(modId, resource);
            }
            this.predicate.test(image, this); // An InvalidBrandingImageException will be thrown if anything is wrong
            DynamicTexture texture = new DynamicTexture(image);
            ResourceLocation id = this.override ? Utils.resource(this.prefix) :
                    Utils.resource("%s/%s".formatted(this.prefix, data.getModId()));
            Minecraft.getMinecraft().getTextureManager().loadTexture(id, texture);
            return Optional.of(new ImageInfo(id, image.getWidth(), image.getHeight(), () -> {
                Minecraft.getMinecraft().getTextureManager().deleteTexture(id);
            }));
        } catch (InvalidBrandingImageException e) {
            CatalogueConstants.LOG.error("Invalid {} branding resource '{}' for mod '{}'", this.prefix, resource, modId, e);
        } catch (ModResourceNotFoundException e) {
            CatalogueConstants.LOG.error("Unable to locate the {} branding resource '{}' for mod '{}'", this.prefix, resource, modId, e);
        } catch (IOException e) {
            CatalogueConstants.LOG.error("An error occurred when loading the {} branding resource '{}' for mod '{}'", this.prefix, resource, modId, e);
        }

        return Optional.empty();
    }

    public record BannerLimit(int maxWidth, int maxHeight) {
    }
}
