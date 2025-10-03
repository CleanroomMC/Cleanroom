package com.cleanroommc.catalogue.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Author: MrCrayfish
 */
public interface IModData {
    Type getType();

    String getModId();

    String getDisplayName();

    @NotNull
    String getVersion();

    @NotNull
    String getInnerVersion();

    @NotNull
    String getDescription();

    @NotNull
    String getItemIcon();

    @NotNull
    String getImageIcon();

    @NotNull
    String getLicense();

    @NotNull
    String getCredits();

    @NotNull
    String getAuthors();

    @NotNull
    String getHomepage();

    @NotNull
    String getIssueTracker();

    @NotNull
    String getBanner();

    @NotNull
    String getBackground();

    @Nullable
    Update getUpdate();

    @Nullable
    IResourcePack getResourcePack();

    Set<String> getDependencies(); //TODO lazily

    boolean hasConfig();

    boolean isLibrary();

    void openConfigScreen(Minecraft minecraft, GuiScreen parent);

    void drawUpdateIcon(Minecraft minecraft, Update update, int x, int y);

    @NotNull
    String getUpdateText(Update update);

    record Update(boolean animated, String url, int texOffset, ResourceLocation textures, boolean updatable,
                  String latestFound, String homepage) {
    }

    enum Type {
        DEFAULT(TextFormatting.RESET),
        LIBRARY(TextFormatting.DARK_GRAY),
        GENERATED(TextFormatting.AQUA);

        private final TextFormatting style;

        Type(TextFormatting style) {
            this.style = style;
        }

        public TextFormatting getStyle() {
            return this.style;
        }
    }
}
