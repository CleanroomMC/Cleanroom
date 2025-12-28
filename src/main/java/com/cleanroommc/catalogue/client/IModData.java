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

    String getVersion();

    String getInnerVersion();

    @Nullable
    String getDescription();

    @Nullable
    String getItemIcon();

    @Nullable
    String getImageIcon();

    @Nullable
    String getLicense();

    @Nullable
    String getCredits();

    @Nullable
    String getAuthors();

    @Nullable
    String getHomepage();

    @Nullable
    String getIssueTracker();

    @Nullable
    String getBanner();

    @Nullable
    String getBackground();

    @Nullable
    String getChildModNames();

    @Nullable
    String getParentModName();

    @Nullable
    Update getUpdate();

    @Nullable
    IResourcePack getResourcePack();

    @NotNull
    Set<String> getDependencies(); //TODO lazily

    @NotNull
    Set<String> getChildMods();

    boolean hasConfig();

    void openConfigScreen(Minecraft minecraft, GuiScreen parent);

    void drawUpdateIcon(Minecraft minecraft, Update update, int x, int y);

    @Nullable
    String getUpdateText(Update update);

    record Update(boolean animated, String url, int texOffset, ResourceLocation textures, boolean updatable,
                  String latestFound, String homepage) {
    }

    enum Type {
        DEFAULT(TextFormatting.RESET),
        LIBRARY(TextFormatting.DARK_GRAY),
        CHILD(TextFormatting.AQUA);

        private final TextFormatting style;

        Type(TextFormatting style) {
            this.style = style;
        }

        public TextFormatting getStyle() {
            return this.style;
        }
    }
}
