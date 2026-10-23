/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.client;

import com.cleanroommc.client.modlist.RenderUtils;
import com.cleanroommc.client.modlist.screen.widget.ModListExtended;
import com.cleanroommc.client.modlist.screen.widget.ModListTextButton;
import com.cleanroommc.discovery.CleanroomModDiscoverer;
import com.cleanroommc.util.CleanroomLog;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenResourcePacks;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.GuiOpenEvent;
import org.lwjgl.input.Keyboard;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Offers to move resource packs that were put in the mods folder to the resourcepacks folder. Shown once per launch,
 * in front of the first screen after loading. Laid out like the mod list.
 */
public final class MisplacedResourcePacksScreen extends GuiScreen {

    private static final ResourceLocation UNKNOWN_PACK = new ResourceLocation("textures/misc/unknown_pack.png");
    private static final int PANEL_WIDTH = 300;

    private static boolean checked;

    private final GuiScreen parent;
    private final List<PackEntry> entries = new ArrayList<>();
    private final List<String> infoLines = new ArrayList<>();

    private @Nullable PackList packList;
    private boolean moved;
    private boolean anyMoved;
    private @Nullable String tooltip;

    private MisplacedResourcePacksScreen(GuiScreen parent, List<File> packs) {
        this.parent = parent;
        for (File pack : packs) {
            this.entries.add(new PackEntry(pack));
        }
    }

    /**
     * Puts this screen in front of the first screen opened after loading, if any resource packs were found among the
     * mods. Only acts once per launch.
     *
     * @param event the screen opening event, at the lowest priority so other mods' replacements are already applied
     */
    public static void onFirstScreen(GuiOpenEvent event) {
        GuiScreen gui = event.getGui();
        // Hiding a connection screen would stall the connection, so wait for the next screen
        if (checked || gui == null || gui instanceof GuiConnecting) {
            return;
        }
        checked = true;
        List<File> packs = CleanroomModDiscoverer.instance().getMisplacedResourcePacks();
        if (!packs.isEmpty()) {
            event.setGui(new MisplacedResourcePacksScreen(gui, packs));
        }
    }

    @Override
    public void initGui() {
        int panelWidth = Math.min(PANEL_WIDTH, this.width - 20);
        int panelLeft = (this.width - panelWidth) / 2;

        String infoKey = this.moved ? "cleanroom.gui.misplaced_packs.failed" : "cleanroom.gui.misplaced_packs.info";
        this.infoLines.clear();
        this.infoLines.addAll(this.fontRenderer.listFormattedStringToWidth(
            I18n.format(this.plural(infoKey)), panelWidth - 20
        ));

        int listTop = 26 + this.infoLines.size() * this.fontRenderer.FONT_HEIGHT + 6;
        int listBottom = this.height - 35;
        this.packList = new PackList(panelWidth - 20, listTop, listBottom);
        this.packList.setSlotXBoundsFromLeft(panelLeft + 10);
        this.packList.replaceEntries(this.entries);

        this.buttonList.clear();
        int buttonY = listBottom + 8;
        if (this.moved) {
            this.addButton(new ModListTextButton(
                panelLeft + 10, buttonY, panelWidth - 20, 20, I18n.format("gui.done"), _ -> this.leave()
            ));
        } else {
            int buttonWidth = (panelWidth - 20 - 4) / 2;
            this.addButton(new ModListTextButton(
                panelLeft + 10, buttonY, buttonWidth, 20, I18n.format("cleanroom.gui.misplaced_packs.move"),
                _ -> this.movePacks()
            ));
            this.addButton(new ModListTextButton(
                panelLeft + 10 + buttonWidth + 4, buttonY, buttonWidth, 20,
                I18n.format("cleanroom.gui.misplaced_packs.ignore"), _ -> this.mc.displayGuiScreen(this.parent)
            ));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        int panelWidth = Math.min(PANEL_WIDTH, this.width - 20);
        int panelLeft = (this.width - panelWidth) / 2;
        int panelRight = panelLeft + panelWidth;
        drawRect(panelLeft, 0, panelRight, this.height, 0x66000000);
        this.drawVerticalLine(panelLeft - 1, -1, this.height, 0xFF707070);
        this.drawVerticalLine(panelRight, -1, this.height, 0xFF707070);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        String title = TextFormatting.BOLD + I18n.format(this.plural("cleanroom.gui.misplaced_packs.title"));
        if (this.entries.size() > 1) {
            title += " " + TextFormatting.GRAY + "(" + this.entries.size() + ")";
        }
        this.drawCenteredString(this.fontRenderer, title, this.width / 2, 10, 0xFFFFFF);

        int y = 26;
        for (String line : this.infoLines) {
            this.drawCenteredString(this.fontRenderer, line, this.width / 2, y, 0xFFFFFF);
            y += this.fontRenderer.FONT_HEIGHT;
        }

        if (this.packList != null) {
            this.packList.drawScreen(mouseX, mouseY, partialTicks);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);

        if (this.tooltip != null) {
            this.drawHoveringText(this.tooltip, mouseX, mouseY);
            this.tooltip = null;
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        if (this.packList != null) {
            this.packList.handleMouseInput();
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        for (PackEntry entry : this.entries) {
            entry.deleteIcon();
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button instanceof ModListTextButton textButton) {
            textButton.onClick();
            return;
        }
        super.actionPerformed(button);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode != Keyboard.KEY_ESCAPE) {
            return;
        }
        if (this.moved) {
            this.leave();
        } else {
            this.mc.displayGuiScreen(this.parent);
        }
    }

    private String plural(String key) {
        return this.entries.size() == 1 ? key : key + ".plural";
    }

    private void movePacks() {
        File target = this.mc.getResourcePackRepository().getDirResourcepacks();
        List<PackEntry> failed = new ArrayList<>();
        for (PackEntry entry : this.entries) {
            try {
                Files.move(entry.file.toPath(), new File(target, entry.file.getName()).toPath());
                this.anyMoved = true;
                entry.deleteIcon();
            } catch (FileAlreadyExistsException e) {
                entry.error = I18n.format("cleanroom.gui.misplaced_packs.exists");
                failed.add(entry);
            } catch (IOException e) {
                CleanroomLog.get().error("Failed to move resource pack {} to {}", entry.file.getName(), target, e);
                entry.error = I18n.format("cleanroom.gui.misplaced_packs.error", e.getClass().getSimpleName());
                failed.add(entry);
            }
        }
        if (failed.isEmpty()) {
            this.leave();
            return;
        }
        this.moved = true;
        this.entries.retainAll(failed);
        this.initGui();
    }

    private void leave() {
        this.mc.displayGuiScreen(this.anyMoved ? new GuiScreenResourcePacks(this.parent) : this.parent);
    }

    private class PackList extends ModListExtended<PackEntry> {

        PackList(int width, int top, int bottom) {
            super(
                MisplacedResourcePacksScreen.this.mc, width, MisplacedResourcePacksScreen.this.height, top, bottom, 26
            );
        }

        @Override
        public int getListWidth() {
            return this.width - 12;
        }

        @Override
        protected int getScrollBarX() {
            return this.right - 6;
        }

    }

    private class PackEntry implements ModListExtended.IListEntry {

        private final File file;
        private final String name;
        private final String description;

        private @Nullable ResourceLocation icon;
        private boolean iconLoaded;
        private @Nullable String error;

        PackEntry(File file) {
            this.file = file;
            this.name = plain(file.getName());
            this.description = readDescription(file);
        }

        private static String readDescription(File file) {
            try (ZipFile zip = new ZipFile(file)) {
                ZipEntry entry = zip.getEntry("pack.mcmeta");
                if (entry == null) {
                    return "";
                }
                try (Reader reader = new InputStreamReader(zip.getInputStream(entry), StandardCharsets.UTF_8)) {
                    JsonObject pack = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonObject("pack");
                    JsonElement description = pack == null ? null : pack.get("description");
                    if (description == null) {
                        return "";
                    }
                    ITextComponent component = ITextComponent.Serializer.jsonToComponent(description.toString());
                    String text = component == null ? "" : component.getUnformattedText();
                    int newline = text.indexOf('\n');
                    return plain(newline < 0 ? text : text.substring(0, newline));
                }
            } catch (IOException | RuntimeException e) {
                CleanroomLog.get().debug("Could not read the description of resource pack {}", file.getName(), e);
                return "";
            }
        }

        /**
         * Packs often colour their file name and description. Every row is drawn in the same colours, so drop them.
         *
         * @param text the text to clean up
         * @return the text without formatting codes or surrounding whitespace
         */
        private static String plain(String text) {
            String stripped = TextFormatting.getTextWithoutFormattingCodes(text);
            return stripped == null ? "" : stripped.trim();
        }

        @Override
        public void drawEntry(
            int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected,
            float partialTicks) {
            GuiScreen screen = MisplacedResourcePacksScreen.this;
            int textWidth = listWidth - 24 - 4;
            String detail = this.error == null ? this.description : this.error;
            TextFormatting detailColour = this.error == null ? TextFormatting.GRAY : TextFormatting.RED;
            this.drawLine(this.name, TextFormatting.WHITE, x + 24, y + 2, textWidth, mouseX, mouseY);
            this.drawLine(detail, detailColour, x + 24, y + 12, textWidth, mouseX, mouseY);

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableBlend();
            screen.mc.getTextureManager().bindTexture(this.icon());
            drawModalRectWithCustomSizedTexture(x + 4, y + 3, 0.0F, 0.0F, 16, 16, 16.0F, 16.0F);
            GlStateManager.disableBlend();
        }

        // Trims with an ellipsis like the mod list; hovering a trimmed line shows all of it
        private void drawLine(String text, TextFormatting colour, int x, int y, int width, int mouseX, int mouseY) {
            MisplacedResourcePacksScreen screen = MisplacedResourcePacksScreen.this;
            FontRenderer font = screen.fontRenderer;
            String shown = text;
            if (font.getStringWidth(text) > width) {
                shown = font.trimStringToWidth(text, width - 8).trim() + "...";
                if (screen.packList != null &&
                    screen.packList.isMouseWithinListBounds(mouseX, mouseY) &&
                    RenderUtils.isMouseWithin(x, y, width, font.FONT_HEIGHT, mouseX, mouseY)) {
                    screen.tooltip = text;
                }
            }
            screen.drawString(font, colour + shown, x, y, 0xFFFFFF);
        }

        private ResourceLocation icon() {
            if (!this.iconLoaded) {
                this.iconLoaded = true;
                this.icon = loadIcon(this.file);
            }
            return this.icon == null ? UNKNOWN_PACK : this.icon;
        }

        private void deleteIcon() {
            if (this.icon != null) {
                MisplacedResourcePacksScreen.this.mc.getTextureManager().deleteTexture(this.icon);
                this.icon = null;
            }
        }

        private @Nullable
        ResourceLocation loadIcon(File file) {
            try (ZipFile zip = new ZipFile(file)) {
                ZipEntry entry = zip.getEntry("pack.png");
                if (entry == null) {
                    return null;
                }
                try (InputStream input = zip.getInputStream(entry)) {
                    DynamicTexture texture = new DynamicTexture(TextureUtil.readBufferedImage(input));
                    return MisplacedResourcePacksScreen.this
                        .mc
                        .getTextureManager()
                        .getDynamicTextureLocation("cleanroom_misplaced_pack", texture);
                }
            } catch (IOException | RuntimeException e) {
                CleanroomLog.get().debug("Could not read the icon of resource pack {}", file.getName(), e);
                return null;
            }
        }

    }

}
