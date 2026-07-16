package com.cleanroommc.client.modlist.data;

import com.cleanroommc.client.modlist.ModListConfig;
import com.cleanroommc.client.modlist.ModListConstants;
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

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class CleanroomModData implements IModData {
    private static final ResourceLocation VERSION_CHECK_ICONS = new ResourceLocation("forge", "textures/gui/version_check_icons.png");
    private static final List<String> LIB_MODS = Arrays.asList(ModListConfig.libraryList);
    private static final List<String> IGNORED_DEPENDENCIES = Arrays.asList(ModListConfig.ignoredDependenciesList);

    private final ModContainer info;
    private final @Nullable ModMetadata metadata;
    private final Type type;
    private final Set<String> dependencies;
    private final Set<String> childMods;
    private final String modId;

    public CleanroomModData(ModContainer info) {
        this.info = info;
        this.metadata = info.getMetadata();
        this.type = this.analyzeType(info);
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
        if (this.metadata != null && !this.metadata.iconItem.isBlank()) return this.metadata.iconItem;
        Map<String, String> props = this.info.getCustomModProperties();
        return props != null ? props.get("iconItem") : null;
    }

    @Nullable
    @Override
    public String getImageIcon() {
        if (this.metadata != null && !this.metadata.iconFile.isBlank()) return this.metadata.iconFile;
        Map<String, String> props = this.info.getCustomModProperties();
        return props != null ? props.get("iconFile") : null;
    }

    @Nullable
    @Override
    public String getLicense() {
        if (this.metadata != null && !this.metadata.license.isBlank()) return this.metadata.license;
        Map<String, String> props = this.info.getCustomModProperties();
        return props != null ? props.get("license") : null;
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
        if (this.metadata != null && !this.metadata.issueTrackerUrl.isBlank()) return this.metadata.issueTrackerUrl;
        Map<String, String> props = this.info.getCustomModProperties();
        return props != null ? props.get("issueTrackerUrl") : null;
    }

    @Nullable
    @Override
    public String getBanner() {
        return this.metadata != null ? this.metadata.logoFile : null;
    }

    @Nullable
    @Override
    public String getBackground() {
        if (this.metadata != null && !this.metadata.backgroundFile.isBlank()) return this.metadata.backgroundFile;
        Map<String, String> props = this.info.getCustomModProperties();
        return props != null ? props.get("backgroundFile") : null;
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

    @Override
    public Set<String> getDependencies() {
        return this.dependencies;
    }

    @Override
    public Set<String> getChildMods() {
        return this.childMods;
    }

    @Override
    public boolean hasConfig() {
        IModGuiFactory factory = FMLClientHandler.instance().getGuiFactoryFor(this.info);
        return factory != null && factory.hasConfigGui();
    }

    @Override
    public void openConfigScreen(Minecraft minecraft, GuiScreen parent) {
        try {
            IModGuiFactory factory = FMLClientHandler.instance().getGuiFactoryFor(this.info);
            if (factory == null) return;
            minecraft.displayGuiScreen(factory.createConfigGui(parent));
        } catch (Exception e) {
            ModListConstants.LOG.error("Failed to build config GUI for mod '{}'", this.getModId(), e);
        }
    }

    @Nullable
    @Override
    public CheckResult getCheckResult() {
        ForgeVersion.CheckResult result = ForgeVersion.getCleanResult(this.info);
        if (result != null && result.status.shouldDraw()) {
            return new CheckResult(
                    result.status == ForgeVersion.Status.OUTDATED || result.status == ForgeVersion.Status.BETA_OUTDATED,
                    result.status.isAnimated(),
                    result.status.getSheetOffset(),
                    VERSION_CHECK_ICONS,
                    result.latestFound,
                    result.homepage
            );
        }
        return null;
    }

    @Override
    public void drawCheckIcon(Minecraft minecraft, CheckResult result, int x, int y) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        int vOffset = result.animated() && (System.currentTimeMillis() / 800 & 1) == 1 ? 8 : 0;
        minecraft.getTextureManager().bindTexture(result.textures());
        Gui.drawModalRectWithCustomSizedTexture(x, y, result.texOffset() * 8, vOffset, 8, 8, 64, 16);
        GlStateManager.disableBlend();
    }

    @Nullable
    @Override
    public String getCheckText(CheckResult update) {
        ForgeVersion.CheckResult result = ForgeVersion.getCleanResult(this.info);
        if (result == null) return null;

        boolean hasPage = update.url() != null && !update.url().isBlank();
        return switch (result.status) {
            case BETA -> TextFormatting.GOLD + I18n.format("catalogue.gui.beta");
            case AHEAD -> TextFormatting.LIGHT_PURPLE + I18n.format("catalogue.gui.ahead", update.latestFound());
            case BETA_OUTDATED -> TextFormatting.GOLD + (hasPage ?
                    I18n.format("catalogue.gui.beta_update_available", update.latestFound(), update.url()) :
                    I18n.format("catalogue.gui.beta_update_available_no_page", update.latestFound()));
            case OUTDATED -> TextFormatting.GREEN + (hasPage ?
                    I18n.format("catalogue.gui.update_available", update.latestFound(), update.url()) :
                    I18n.format("catalogue.gui.update_available_no_page", update.latestFound()));
            default -> null;
        };
    }

    @Nullable
    @Override
    public IResourcePack getResourcePack() {
        return FMLClientHandler.instance().getResourcePackFor(this.getModId());
    }

    private Type analyzeType(ModContainer info) {
        if (this.metadata != null && this.metadata.parentMod != null) {
            return Type.CHILD;
        } else if (LIB_MODS.contains(info.getModId())) {
            return Type.LIBRARY;
        } else {
            return Type.DEFAULT;
        }
    }

    private static Set<String> analyzeDependencies(ModContainer source) {
        return source.getDependencies().stream()
                .map(ArtifactVersion::getLabel)
                .filter(modId -> !IGNORED_DEPENDENCIES.contains(modId))
                .collect(Collectors.toUnmodifiableSet());
    }

    private static Set<String> analyzeChildMods(ModContainer source) {
        ModMetadata metadata = source.getMetadata();
        if (metadata == null) return Collections.emptySet();
        return metadata.childMods.stream()
                .filter(Objects::nonNull)
                .map(ModContainer::getModId)
                .collect(Collectors.toUnmodifiableSet());
    }
}
