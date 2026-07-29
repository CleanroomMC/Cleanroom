package com.cleanroommc.client.modlist.data;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.Set;

/// <h1>Experimental</h1>
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
    IResourcePack getResourcePack();

    Set<String> getDependencies(); //TODO lazily

    Set<String> getChildMods();

    boolean hasConfig();

    void openConfigScreen(Minecraft minecraft, GuiScreen parent);

    @Nullable
    CheckResult getCheckResult();

    void drawCheckIcon(Minecraft minecraft, CheckResult result, int x, int y);

    @Nullable
    String getCheckText(CheckResult result);

    /**
     * @param updatable   Whether the mod is outdated and the icon is clickable.
     * @param latestFound Latest version found in json.
     * @param url         URL to download page.
     */
    record CheckResult(
            boolean updatable,
            boolean animated,
            int texOffset,
            ResourceLocation textures,
            @Nullable String latestFound,
            @Nullable String url
    ) {
    }

    enum Type {
        DEFAULT(TextFormatting.WHITE),
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
