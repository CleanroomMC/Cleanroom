package com.cleanroommc.client.modlist.data;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.IResourcePack;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

public class MinecraftModData implements IModData {
    @Override
    public Type getType() {
        return Type.LIBRARY;
    }

    @Override
    public String getModId() {
        return "minecraft";
    }

    @Override
    public String getDisplayName() {
        return "Minecraft";
    }

    @Override
    public String getVersion() {
        return "1.12.2";
    }

    @Override
    public String getInnerVersion() {
        return this.getVersion();
    }

    @Override
    public String getDescription() {
        // Description provided by minecraft.wiki (CC BY-NC-SA 3.0)
        return "Minecraft is a 3D sandbox adventure game developed by Mojang Studios where players can interact with a fully customizable three-dimensional world made of blocks and entities. Its diverse gameplay options allow players to choose the way they play, creating countless possibilities.";
    }

    @Nullable
    @Override
    public String getItemIcon() {
        return null;
    }

    @Nullable
    @Override
    public String getImageIcon() {
        return null;
    }

    @Nullable
    @Override
    public String getLicense() {
        return "All Rights Reserved (https://www.minecraft.net/en-us/eula)";
    }

    @Nullable
    @Override
    public String getCredits() {
        return null;
    }

    @Nullable
    @Override
    public String getAuthors() {
        return "Mojang AB";
    }

    @Nullable
    @Override
    public String getHomepage() {
        return "https://www.minecraft.net";
    }

    @Nullable
    @Override
    public String getIssueTracker() {
        return "https://bugs.mojang.com/browse/MC";
    }

    @Nullable
    @Override
    public String getBanner() {
        return null;
    }

    @Nullable
    @Override
    public String getBackground() {
        return null;
    }

    @Nullable
    @Override
    public String getChildModNames() {
        return null;
    }

    @Nullable
    @Override
    public String getParentModName() {
        return null;
    }

    @Nullable
    @Override
    public CheckResult getCheckResult() {
        return null;
    }

    @Nullable
    @Override
    public IResourcePack getResourcePack() {
        return null;
    }

    @Override
    public Set<String> getDependencies() {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getChildMods() {
        return Collections.emptySet();
    }

    @Override
    public boolean hasConfig() {
        return true;
    }

    @Override
    public void openConfigScreen(Minecraft minecraft, GuiScreen parent) {
        minecraft.displayGuiScreen(new GuiOptions(parent, minecraft.gameSettings));
    }

    @Override
    public void drawCheckIcon(Minecraft minecraft, CheckResult result, int x, int y) {
    }

    @Nullable
    @Override
    public String getCheckText(CheckResult result) {
        return null;
    }
}
