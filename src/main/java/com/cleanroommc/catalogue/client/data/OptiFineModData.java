package com.cleanroommc.catalogue.client.data;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiVideoSettings;
import net.minecraftforge.fml.common.ModContainer;

import javax.annotation.Nullable;

public class OptiFineModData extends CleanroomModData {
    private final String version;

    public OptiFineModData(ModContainer info) {
        super(info);
        this.version = this.getDisplayVersion();
    }

    private String getDisplayVersion() {
        String version = this.getInnerVersion();
        return version.substring(version.indexOf("OptiFine_1.12.2_") + 16);
    }

    @Override
    public String getVersion() {
        return this.version;
    }

    @Nullable
    @Override
    public String getDescription() {
        // Copied from https://www.optifine.net/home
        return """
                OptiFine is a Minecraft optimization mod.
                It allows Minecraft to run faster and look better with full support for HD textures and many configuration options.
                """;
    }

    @Nullable
    @Override
    public String getItemIcon() {
        return "minecraft:command_block";
    }

    @Nullable
    @Override
    public String getLicense() {
        return "All Rights Reserved";
    }

    @Nullable
    @Override
    public String getAuthors() {
        return "sp614x";
    }

    @Nullable
    @Override
    public String getHomepage() {
        return "https://www.optifine.net/home";
    }

    @Nullable
    @Override
    public String getIssueTracker() {
        return "https://github.com/sp614x/optifine/issues";
    }

    @Override
    public boolean hasConfig() {
        return true;
    }

    @Override
    public void openConfigScreen(Minecraft minecraft, GuiScreen parent) {
        minecraft.displayGuiScreen(new GuiVideoSettings(parent, minecraft.gameSettings));
    }
}
