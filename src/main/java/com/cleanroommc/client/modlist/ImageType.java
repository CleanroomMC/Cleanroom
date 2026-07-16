package com.cleanroommc.client.modlist;

import com.cleanroommc.client.modlist.ModListConstants;
import com.cleanroommc.client.modlist.data.IModData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackFileNotFoundException;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.Function;

public enum ImageType {
    ICON("icon", IModData::getImageIcon) {
        @Override
        protected boolean validate(IModData data, BufferedImage image) {
            if (image.getWidth() != image.getHeight()) {
                ModListConstants.LOG.warn("Invalid icon image for mod '{}': image must be a square", data.getModId());
                return false;
            }
            return true;
        }
    },
    BANNER("banner", IModData::getBanner) {
        @Override
        protected BufferedImage readImage(IModData data, String resource) throws IOException {
            IResourcePack resourcePack = data.getResourcePack();
            if (resourcePack != null && !resource.startsWith("/")) {
                return resourcePack.getPackImage();
            }
            return super.readImage(data, resource);
        }
    },
    BACKGROUND("background", IModData::getBackground);

    private final String type;
    private final Function<IModData, String> resourceLocator;

    ImageType(String type, Function<IModData, String> resourceLocator) {
        this.type = type;
        this.resourceLocator = resourceLocator;
    }

    public Optional<ImageInfo> load(IModData data) {
        String resource = this.resourceLocator.apply(data);
        if (resource == null || resource.isBlank()) return Optional.empty();

        try {
            BufferedImage image = this.readImage(data, resource);
            if (image == null) {
                ModListConstants.LOG.warn("Failed to locate {} image resource '{}' for mod '{}'", this.type, resource, data.getModId());
                return Optional.empty();
            }
            return this.validate(data, image) ? Optional.of(this.registerTexture(data, image)) : Optional.empty();
        } catch (ResourcePackFileNotFoundException e) {
            // Remove stack trace if getPackImage errored
            ModListConstants.LOG.warn("Failed to locate {} image {} for mod '{}'", this.type, e.getMessage(), data.getModId());
            return Optional.empty();
        } catch (IOException e) {
            ModListConstants.LOG.warn("Failed to load {} image resource '{}' for mod '{}'", this.type, resource, data.getModId(), e);
            return Optional.empty();
        }
    }

    @Nullable
    protected BufferedImage readImage(IModData data, String resource) throws IOException {
        String normalizedResource = resource.startsWith("/") ? resource : "/" + resource;
        try (InputStream is = this.getClass().getResourceAsStream(normalizedResource)) {
            return is != null ? TextureUtil.readBufferedImage(is) : null;
        }
    }

    protected boolean validate(IModData data, BufferedImage image) {
        return true;
    }

    private ImageInfo registerTexture(IModData data, BufferedImage image) {
        ResourceLocation id = ModListConstants.resource("%s/%s".formatted(this.type, data.getModId()));
        final TextureManager manager = Minecraft.getMinecraft().getTextureManager();
        manager.loadTexture(id, new DynamicTexture(image));
        return new ImageInfo(id, image.getWidth(), image.getHeight(), () -> manager.deleteTexture(id));
    }
}
