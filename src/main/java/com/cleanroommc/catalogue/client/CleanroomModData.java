package com.cleanroommc.catalogue.client;

import com.cleanroommc.catalogue.CatalogueConfig;
import com.cleanroommc.catalogue.CatalogueConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.versioning.ArtifactVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Author: MrCrayfish
 */
public class CleanroomModData implements IModData {
    public static final ResourceLocation VERSION_CHECK_ICONS = new ResourceLocation("forge", "textures/gui/version_check_icons.png");
    public static final List<String> LIB_MODS = Arrays.asList(CatalogueConfig.libraryList);
    public static final List<String> IGNORED_DEPENDENCIES = Arrays.asList(CatalogueConfig.ignoredDependenciesList);

    private final @NotNull ModContainer info;
    private final @Nullable ModMetadata metadata;
    private final Type type;
    private final Set<String> dependencies;
    private final Set<String> childMods;
    private final String modId;

    public CleanroomModData(@NotNull ModContainer info) {
        this.info = info;
        this.metadata = info.getMetadata();
        this.type = analyzeType(info);
        this.dependencies = analyzeDependencies(info);
        this.childMods = analyzeChildMods(info);
        this.modId = this.info.getModId();
    }

    @Override
    public Type getType() {
        return this.type;
    }

    @Override
    public String getModId() {
        return this.modId;
    }

    @Override
    public String getDisplayName() {
        return this.info.getName();
    }

    @Override
    public String getVersion() {
        return this.info.getDisplayVersion();
    }

    @Override
    public String getInnerVersion() {
        return this.info.getVersion();
    }

    @Nullable
    @Override
    public String getDescription() {
        return this.metadata != null ? this.metadata.description : null;
    }

    @Nullable
    @Override
    public String getItemIcon() {
        return this.metadata != null ? this.metadata.iconItem : null;
    }

    @Nullable
    @Override
    public String getImageIcon() {
        return this.metadata != null ? this.metadata.iconFile : null;
    }

    @Nullable
    @Override
    public String getLicense() {
        return this.metadata != null ? this.metadata.license : null;
    }

    @Nullable
    @Override
    public String getCredits() {
        return this.metadata != null ? this.metadata.credits : null;
    }

    @Nullable
    @Override
    public String getAuthors() {
        return this.metadata != null ? this.metadata.getAuthorList() : null;
    }

    @Nullable
    @Override
    public String getHomepage() {
        return this.metadata != null ? this.metadata.url : null;
    }

    @Nullable
    @Override
    public String getIssueTracker() {
        return this.metadata != null ? this.metadata.issueTrackerUrl : null;
    }

    @Nullable
    @Override
    public String getBanner() {
        return this.metadata != null ? this.metadata.logoFile : null;
    }

    @Nullable
    @Override
    public String getBackground() {
        return this.metadata != null ? this.metadata.backgroundFile : null;
    }

    @Nullable
    @Override
    public String getChildModNames() {
        return this.metadata != null ? this.metadata.getChildModList() : null;
    }

    @Nullable
    @Override
    public String getParentModName() {
        return this.metadata != null && this.metadata.parentMod != null ? this.metadata.parentMod.getName() : null;
    }

    @Nullable
    @Override
    public Update getUpdate() {
        ForgeVersion.CheckResult result = ForgeVersion.getCleanResult(this.info);
        if (result != null && result.status.shouldDraw()) {
            return new Update(result.status.isAnimated(), result.url, result.status.getSheetOffset(), VERSION_CHECK_ICONS, result.status == ForgeVersion.Status.OUTDATED || result.status == ForgeVersion.Status.BETA_OUTDATED, result.latestFound, result.homepage);
        }
        return null;
    }

    @NotNull
    @Override
    public Set<String> getDependencies() {
        return this.dependencies;
    }

    @NotNull
    @Override
    public Set<String> getChildMods() {
        return this.childMods;
    }

    @Override
    public boolean hasConfig() {
        IModGuiFactory guiFactory = FMLClientHandler.instance().getGuiFactoryFor(this.info);
        if (guiFactory == null) return false;
        return guiFactory.hasConfigGui();
    }

    @Override
    public void openConfigScreen(Minecraft minecraft, GuiScreen parent) {
        try {
            IModGuiFactory guiFactory = FMLClientHandler.instance().getGuiFactoryFor(this.info);
            GuiScreen newScreen = guiFactory.createConfigGui(parent);
            minecraft.displayGuiScreen(newScreen);
        } catch (Exception e) {
            CatalogueConstants.LOG.error("There was a critical issue trying to build the config GUI for {}", this.getModId(), e);
        }
    }

    @Override
    public void drawUpdateIcon(Minecraft minecraft, Update update, int x, int y) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        int vOffset = update.animated() && (System.currentTimeMillis() / 800 & 1) == 1 ? 8 : 0;
        minecraft.getTextureManager().bindTexture(update.textures());
        Gui.drawModalRectWithCustomSizedTexture(x, y, update.texOffset() * 8, vOffset, 8, 8, 64, 16);
    }

    @Nullable
    @Override
    public String getUpdateText(Update update) {
        ForgeVersion.CheckResult result = ForgeVersion.getCleanResult(this.info);
        if (result == null) return null;
        switch (result.status) {
            case BETA:
                return TextFormatting.GOLD + I18n.format("catalogue.gui.beta");
            case AHEAD:
                return TextFormatting.LIGHT_PURPLE + I18n.format("catalogue.gui.ahead", update.latestFound());
            case BETA_OUTDATED:
                if (update.homepage() != null && !update.homepage().isBlank()) {
                    return TextFormatting.GOLD + I18n.format("catalogue.gui.beta_update_available", update.latestFound(), update.homepage());
                } else {
                    return TextFormatting.GOLD + I18n.format("catalogue.gui.beta_update_available_no_page", update.latestFound());
                }
            case OUTDATED:
                if (update.homepage() != null && !update.homepage().isBlank()) {
                    return TextFormatting.GREEN + I18n.format("catalogue.gui.update_available", update.latestFound(), update.homepage());
                } else {
                    return TextFormatting.GREEN + I18n.format("catalogue.gui.update_available_no_page", update.latestFound());
                }
        }
        return null;
    }

    @Nullable
    @Override
    public IResourcePack getResourcePack() {
        return FMLClientHandler.instance().getResourcePackFor(this.getModId());
    }

    private Type analyzeType(@NotNull ModContainer info) {
        if (this.metadata != null && this.metadata.parentMod != null) {
            return Type.CHILD;
        } else if (LIB_MODS.contains(info.getModId())) {
            return Type.LIBRARY;
        } else {
            return Type.DEFAULT;
        }
    }

    private static @NotNull Set<String> analyzeDependencies(@NotNull ModContainer source) {
        List<? extends ArtifactVersion> versions = source.getDependencies();
        return versions.stream()
                .map(ArtifactVersion::getLabel)
                .filter(modid -> !IGNORED_DEPENDENCIES.contains(modid))
                .collect(Collectors.toUnmodifiableSet());
    }

    private static @NotNull Set<String> analyzeChildMods(@NotNull ModContainer source) {
        ModMetadata metadata = source.getMetadata();
        if (metadata == null) return Collections.emptySet();
        return metadata.childMods.stream()
                .filter(Objects::nonNull)
                .map(ModContainer::getModId)
                .collect(Collectors.toUnmodifiableSet());
    }
}
