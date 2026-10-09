/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.client.modlist.data;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import java.util.Set;
import javax.annotation.Nullable;

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

    /**
     * @param updatable   Whether the mod is outdated and the icon is clickable.
     * @param latestFound Latest version found in json.
     * @param url         URL to download page.
     */
    record CheckResult(boolean updatable,
        boolean animated,
        int texOffset,
        ResourceLocation textures,
        @Nullable String latestFound,
        @Nullable String url
    ) { }

}
