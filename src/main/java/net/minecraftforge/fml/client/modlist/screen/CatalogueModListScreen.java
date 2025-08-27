package net.minecraftforge.fml.client.modlist.screen;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.modlist.ClientHelper;
import net.minecraftforge.fml.client.modlist.screen.widget.CatalogueCheckBoxButton;
import net.minecraftforge.fml.client.modlist.screen.widget.CatalogueIconButton;
import net.minecraftforge.fml.client.modlist.screen.widget.CatalogueListExtended;
import net.minecraftforge.fml.client.modlist.screen.widget.CatalogueTextField;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class CatalogueModListScreen extends GuiScreen {
    private static final ResourceLocation MISSING_BANNER = new ResourceLocation(ForgeVersion.MOD_ID, "textures/gui/missing_banner.png");
    private static final ResourceLocation MISSING_BACKGROUND = new ResourceLocation(ForgeVersion.MOD_ID, "textures/gui/missing_background.png");
    private static final ResourceLocation VERSION_CHECK_ICONS = new ResourceLocation(ForgeVersion.MOD_ID, "textures/gui/version_check_icons.png");
    private static final ResourceLocation MINECRAFT_LOGO = new ResourceLocation(ForgeVersion.MOD_ID, "textures/gui/minecraft.png");
    private static final ImageInfo MISSING_BANNER_INFO = new ImageInfo(MISSING_BANNER, new Dimension(120, 120));
    private static final Map<String, ImageInfo> BANNER_CACHE = new HashMap<>();
    private static final Map<String, ImageInfo> IMAGE_ICON_CACHE = new HashMap<>();
    private static final Map<String, ItemStack> ITEM_ICON_CACHE = new HashMap<>();
    private static final Map<String, ModContainer> CACHED_MODS = new HashMap<>();
    private static ResourceLocation cachedBackground;
    private static boolean loaded = false;
    private static String lastSearch = "";

    private final GuiScreen parentScreen;
    private CatalogueTextField searchTextField;
    private ModList modList;
    private ModContainer selectedModData;
    private CatalogueIconButton modFolderButton;
    private CatalogueIconButton configButton;
    private CatalogueIconButton websiteButton;
    private CatalogueIconButton issueButton;
    private CatalogueCheckBoxButton updatesButton;

    private List<String> activeTooltip;
    private int tooltipYOffset;
    private StringList descriptionList;

    private long lastClickTime;

    public CatalogueModListScreen(GuiScreen parent) {
        super();
        this.parentScreen = parent;
        if(!loaded) {
            Loader.instance().getActiveModList().forEach(data -> CACHED_MODS.put(data.getModId(), data));
            CACHED_MODS.put("minecraft", new MinecraftContainer()); // Override minecraft
            CACHED_MODS.put("catalogue", new CatalogueContainer());
            BANNER_CACHE.put("minecraft", new ImageInfo(MINECRAFT_LOGO, new Dimension(1024, 256)));
            loaded = true;
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        this.searchTextField = new CatalogueTextField(0, this.fontRenderer, 11, 25, 148, 20);
        this.searchTextField.setCanLoseFocus(true);
        this.searchTextField.setEnableBackgroundDrawing(true);
        this.searchTextField.setText(lastSearch);

        this.modList = new ModList();
        this.modList.setSlotXBoundsFromLeft(10);
        this.modList.registerScrollButtons(7, 8);

        this.buttonList.add(new GuiButton(1, 10, modList.bottom + 8, 127, 20, I18n.format("gui.back")));
        this.modFolderButton = this.addButton(new CatalogueIconButton(2, 140, modList.bottom + 8, 0, 0));

        int padding = 10;
        int contentLeft = this.modList.right + 12 + padding;
        int contentWidth = this.width - contentLeft - padding;
        int buttonWidth = (contentWidth - padding) / 3;

        this.configButton = this.addButton(new CatalogueIconButton(3, contentLeft, 105, 10, 0, buttonWidth, I18n.format("fml.menu.mods.config")));
        this.configButton.visible = false;

        this.websiteButton = this.addButton(new CatalogueIconButton(4, contentLeft + buttonWidth + 5, 105, 20, 0, buttonWidth, I18n.format("fml.menu.mods.website")));
        this.websiteButton.visible = false;

        this.issueButton = this.addButton(new CatalogueIconButton(5, contentLeft + buttonWidth + buttonWidth + 10, 105, 30, 0, buttonWidth, I18n.format("fml.menu.mods.issue")));
        this.issueButton.visible = false;

        this.descriptionList = new StringList(contentWidth + padding * 2, 50, contentLeft - padding, 130);
        this.descriptionList.registerScrollButtons(9, 10);

        this.updatesButton = this.addButton(new CatalogueCheckBoxButton(6, this.modList.right - 14, 7, false));

        this.modList.filterAndUpdateList(this.searchTextField.getText());

        // Resizing window causes all widgets to be recreated, therefore need to update selected info
        if (this.selectedModData != null) {
            this.setSelectedModData(this.selectedModData);
            this.updateSelectedModList();
            ModListEntry entry = this.modList.getEntryFromInfo(this.selectedModData);
            if (entry != null) {
                this.modList.centerScrollOn(entry);
            }
        }
        this.updateSearchField(this.searchTextField.getText());
    }

    @Override
    public void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 1:
                mc.displayGuiScreen(parentScreen);
                break;
            case 2:
                try {
                    Desktop.getDesktop().open(Loader.instance().getConfigDir().getParentFile().toPath().resolve("mods").toFile());
                } catch (Exception e) {
                    FMLLog.log.error("Problem opening mods folder", e);
                }
                break;
            case 3:
                if (!this.selectedModData.getModId().equals("minecraft")) {
                    try {
                        IModGuiFactory guiFactory = FMLClientHandler.instance().getGuiFactoryFor(this.selectedModData);
                        GuiScreen newScreen = guiFactory.createConfigGui(this);
                        this.mc.displayGuiScreen(newScreen);
                    } catch (Exception e) {
                        FMLLog.log.error("There was a critical issue trying to build the config GUI for {}", this.selectedModData.getModId(), e);
                    }
                } else {
                    this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
                }
                break;
            case 4:
                this.openLink(0, this.selectedModData);
                break;
            case 5:
                this.openLink(1, this.selectedModData);
                break;
            case 6:
                this.modList.filterAndUpdateList(this.searchTextField.getText());
                this.updateSelectedModList();
                break;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.activeTooltip = null;
        this.drawDefaultBackground();
        this.drawModList(mouseX, mouseY, partialTicks);
        this.drawModInfo(mouseX, mouseY, partialTicks);
        super.drawScreen(mouseX, mouseY, partialTicks);

        Optional<ModContainer> optional = Optional.ofNullable(CACHED_MODS.get("catalogue"));
        optional.ifPresent(this::loadAndCacheLogo);
        ImageInfo imageInfo = BANNER_CACHE.get("catalogue");
        if (imageInfo != null) {
            Dimension size = imageInfo.size();
            this.mc.getTextureManager().bindTexture(imageInfo.resource());
            ClientHelper.blit(10, 9, 10, 10, 0.0F, 0.0F, size.width, size.height, size.width, size.height);
        }

        if (ClientHelper.isMouseWithin(10, 9, 10, 10, mouseX, mouseY)) {
            this.setActiveTooltip(I18n.format("fml.menu.mods.info"));
            this.tooltipYOffset = 10;
        }

        if (this.modFolderButton.isMouseOver()) {
            this.setActiveTooltip(I18n.format("fml.button.open.mods.folder"));
        }

        if (this.activeTooltip != null) {
            this.drawHoveringText(this.activeTooltip, mouseX, mouseY + this.tooltipYOffset);
            this.tooltipYOffset = 0;
        }
    }

    @Override
    protected void keyTyped(char typedChar, int key) throws IOException {
        if (this.searchTextField.textboxKeyTyped(typedChar, key)) {
            String s = this.searchTextField.getText();
            this.updateSearchField(s);
            this.modList.filterAndUpdateList(s);
            this.updateSelectedModList();
            lastSearch = s;
            return;
        }

        super.keyTyped(typedChar, key);
    }

    @Override
    public void handleKeyboardInput() throws IOException {
        if (Keyboard.getEventKey() == Keyboard.KEY_F && (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))) {
            if(!this.searchTextField.isFocused()) {
                this.searchTextField.setFocused(true);
            }
            return;
        }

        super.handleKeyboardInput();
    }

    private class ModList extends CatalogueListExtended {
        private List<ModListEntry> entries = Lists.<ModListEntry>newArrayList();
        private int selectedIndex = -1;

        public ModList() {
            super(CatalogueModListScreen.this.mc, 150, CatalogueModListScreen.this.height, 46, CatalogueModListScreen.this.height - 35, 26);
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            ClientHelper.scissor(this.getListLeft(), this.top, this.width, this.bottom - this.top);
            super.drawScreen(mouseX, mouseY, partialTicks);
            GL11.glDisable(GL11.GL_SCISSOR_TEST);

            if (this.entries.isEmpty()) {
                String text = I18n.format("fml.menu.mods.nomods");
                int left = this.left + this.width / 2;
                int top = this.top + (this.bottom - this.top - CatalogueModListScreen.this.fontRenderer.FONT_HEIGHT) / 2;
                drawCenteredString(CatalogueModListScreen.this.fontRenderer, text, left, top, 0xFFFFFFFF);
            }
        }

        @Override
        protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
            selectedIndex = slotIndex;
            ModListEntry entry = entries.get(slotIndex);
            CatalogueModListScreen.this.setSelectedModData(entry.data);
        }

        public void filterAndUpdateList(String text) {
            List<ModListEntry> entries = CACHED_MODS.values().stream()
                    .filter(data -> data.getName().toLowerCase(Locale.ENGLISH).contains(text.toLowerCase(Locale.ENGLISH)))
                    .filter(data -> !updatesButton.selected() || shouldUpdate(ForgeVersion.getCleanResult(data)))
                    .map(data -> new ModListEntry(data, this))
                    .sorted(Comparator.comparing(entry -> entry.data.getName()))
                    .collect(Collectors.toList());
            this.entries = entries;
            this.selectMod(this.getEntryFromInfo(selectedModData));
            this.setAmountScrolled(0);
        }

        public ModListEntry getEntryFromInfo(ModContainer data) {
            return this.entries.stream().filter(entry -> entry.data == data).findFirst().orElse(null);
        }

        public void selectMod(int selectedIndex) {
            this.selectedIndex = selectedIndex;
        }

        public void selectMod(IGuiListEntry entry) {
            this.selectMod(this.getEntryIndex(entry));
        }

        public void centerScrollOn(IGuiListEntry entry) {
            this.setAmountScrolled(this.slotHeight * this.getEntryIndex(entry));
        }

        @Override
        public IGuiListEntry getListEntry(int index) {
            return this.entries.get(index);
        }

        public int getEntryIndex(IGuiListEntry entry) {
            return this.entries.indexOf(entry);
        }

        @Override
        protected int getScrollBarX() {
            return this.left + this.width - 6;
        }

        @Override
        public int getListLeft() {
            return this.left;
        }

        @Override
        public int getListRight() {
            return this.getListLeft() + this.getListWidth();
        }

        @Override
        public int getListWidth() {
            return this.width - (this.isScrollBarVisible() ? 6 : 0);
        }

        @Override
        protected int getSize() {
            return this.entries.size();
        }

        @Override
        protected boolean isSelected(int slotIndex) {
            return slotIndex == selectedIndex;
        }

        @Override
        protected void drawTopAndBottom(Tessellator tessellator) {
        }

        @Override
        protected void overlayBackground(int startY, int endY, int startAlpha, int endAlpha) {
        }
    }

    private class ModListEntry implements CatalogueListExtended.IGuiListEntry {
        private final ModContainer data;
        private final ModList list;
        private ItemStack icon;

        public ModListEntry(ModContainer data, ModList list) {
            this.data = data;
            this.list = list;
            this.icon = this.getItemIcon();
        }

        @Override
        public void drawEntry(int index, int left, int top, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean hovered, float partialTicks) {
            // Draws mod name and version
            drawString(fontRenderer, this.getFormattedModName(), left + 24, top + 2, 0xFFFFFF);
            drawString(fontRenderer, TextFormatting.GRAY + this.data.getDisplayVersion(), left + 24, top + 12, 0xFFFFFF);

            // Draw image icon or fallback to item icon
            this.drawIcon(top, left);

            // Draws an icon if there is an update for the mod
            ForgeVersion.CheckResult update = ForgeVersion.getCleanResult(this.data);
            if (shouldDraw(update)) {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                mc.getTextureManager().bindTexture(VERSION_CHECK_ICONS);
                int vOffset = update.status.isAnimated() && (System.currentTimeMillis() / 800 & 1) == 1 ? 8 : 0;
                ClientHelper.blit(left + rowWidth - 8 - 10, top + 6, update.status.getSheetOffset() * 8, vOffset, 8, 8, 64, 16);
            }
        }

        private void drawIcon(int top, int left) {
            CatalogueModListScreen.this.loadAndCacheIcon(this.data);

            ImageInfo iconInfo = IMAGE_ICON_CACHE.get(this.data.getModId());
            if (iconInfo != null) {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.enableBlend();
                Dimension size = iconInfo.size();
                mc.getTextureManager().bindTexture(iconInfo.resource());
                ClientHelper.blit(left + 4, top + 3, 16, 16, 0.0F, 0.0F, size.width, size.height, size.width, size.height);
                GlStateManager.disableBlend();
                return;
            }
            try {
                GlStateManager.enableDepth();
                RenderHelper.enableGUIStandardItemLighting();
                CatalogueModListScreen.this.mc.getRenderItem().renderItemIntoGUI(this.icon, left + 4, top + 2);
                GlStateManager.disableDepth();
                RenderHelper.disableStandardItemLighting();
            } catch (Exception e) {
                // Attempt to catch exceptions when rendering item. Sometime level instance isn't checked for null
                FMLLog.log.debug("Failed to draw icon for mod '{}'", this.data.getModId());
                ITEM_ICON_CACHE.put(this.data.getModId(), new ItemStack(Blocks.GRASS));
                this.icon = new ItemStack(Blocks.GRASS);
            }
        }

        @Override
        public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
            this.list.selectMod(slotIndex);
            return true;
        }

        private ItemStack getItemIcon() {
            if (ITEM_ICON_CACHE.containsKey(this.data.getModId())) {
                return ITEM_ICON_CACHE.get(this.data.getModId());
            }

            // Put grass as default item icon
            ITEM_ICON_CACHE.put(this.data.getModId(), new ItemStack(Blocks.GRASS));

            // Minecraft is a grass block
            if (this.data.getModId().equals("minecraft")) return new ItemStack(Blocks.GRASS);

            // Special case for Forge to set item icon to anvil
            if (this.data.getModId().equals("forge")) {
                ItemStack anvil = new ItemStack(Blocks.ANVIL);
                ITEM_ICON_CACHE.put("forge", anvil);
                return anvil;
            }

            // Gets the raw item icon resource string
            ModMetadata metadata = this.data.getMetadata();
            if (metadata != null && !metadata.autogenerated) {
                String itemIcon = metadata.iconItem;
                if (!itemIcon.isBlank()) {
                    try {
                        // 0:mod id 1:item name (2:metadata)
                        String[] parts = itemIcon.split(":");
                        Item item = Item.getByNameOrId(parts[0] + ":" + parts[1]);
                        if (item != null) {
                            int meta = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
                            ItemStack itemStack = new ItemStack(item, 1, meta);
                            ITEM_ICON_CACHE.put(this.data.getModId(), itemStack);
                            return itemStack;
                        }
                    } catch (Exception ignored) {}
                }
            }

            // If the mod doesn't specify an item to use, Catalogue will attempt to get an item from the mod
            Optional<ItemStack> optional = ForgeRegistries.ITEMS.getValuesCollection().stream().filter(item -> item.getRegistryName().getNamespace().equals(this.data.getModId())).map(ItemStack::new).findFirst();
            if (optional.isPresent()) {
                ItemStack item = optional.get();
                if (!item.isEmpty()) {
                    // If the item is in a creative tab, Catalogue will use the tab's icon
                    if (item.getItem().getCreativeTab() != null) {
                        ItemStack tabItem = item.getItem().getCreativeTab().getIcon();
                        if (tabItem != null && !tabItem.isEmpty() && tabItem.getItem().getRegistryName().getNamespace().equals(this.data.getModId())) {
                            item = tabItem;
                        }
                    }
                    ITEM_ICON_CACHE.put(this.data.getModId(), item);
                    return item;
                }
            }

            return new ItemStack(Blocks.GRASS);
        }

        private String getFormattedModName() {
            String name = this.data.getName();
            int width = this.list.getListWidth() - (this.list.getMaxScroll() > 0 ? 30 : 24);
            if (CatalogueModListScreen.this.fontRenderer.getStringWidth(name) > width) {
                name = CatalogueModListScreen.this.fontRenderer.trimStringToWidth(name, width - 10) + "...";
            }
            if (this.data.getModId().equals("forge") || this.data.getModId().equals("minecraft") || this.data.getModId().equals("cleanroom")) {
                return TextFormatting.DARK_GRAY + name;
            }
            return name;
        }

        @Override
        public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {
        }

        @Override
        public void updatePosition(int slotIndex, int x, int y, float partialTicks) {
        }
    }

    private class StringList extends CatalogueListExtended {
        private List<StringEntry> entries = Lists.<StringEntry>newArrayList();

        public StringList(int width, int height, int left, int top) {
            super(CatalogueModListScreen.this.mc, width, height, top, top + height, 10);
            this.setSlotXBoundsFromLeft(left + 8);
            this.visible = false;
        }

        public void setTextFromInfo(ModContainer data) {
            this.entries.clear();
            this.visible = true;
            if (data.getMetadata().description.trim().isBlank()) {
                this.visible = false;
                return;
            }
            String description = data.getMetadata().description.trim();
            List<String> lines = CatalogueModListScreen.this.fontRenderer.listFormattedStringToWidth(description, this.getListWidth());

            for (String line : lines) {
                String cleanLine = line.replace("\n", "").replace("\r", "").trim();
                this.entries.add(new StringEntry(cleanLine));
            }
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            ClientHelper.scissor(this.left, this.top, this.width, this.bottom - this.top);
            super.drawScreen(mouseX, mouseY, partialTicks);
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }

        @Override
        protected void drawContainerBackground(Tessellator tessellator) {
            int x = this.left;
            int y = this.top;
            int width = this.width;
            int height = this.height;
            drawRect(x, y + 1, x + 1, y + height - 1, 0x77000000);
            drawRect(x + 1, y, x + width - 1, y + height, 0x77000000);
            drawRect(x + width - 1, y + 1, x + width, y + height - 1, 0x77000000);
        }

        @Override
        protected int getScrollBarX() {
            return this.left + this.width - 7;
        }

        @Override
        public int getListLeft() {
            return this.left + 8;
        }

        @Override
        public int getListWidth() {
            return this.width - 16;
        }

        @Override
        protected int getRowTop(int $$0) {
            return super.getRowTop($$0) + 4;
        }

        @Override
        public int getMaxScroll() {
            return Math.max(0, this.getContentHeight() - (this.height - 12));
        }

        @Override
        protected int getSize() {
            return this.entries.size();
        }

        @Override
        public IGuiListEntry getListEntry(int index) {
            return this.entries.get(index);
        }

        @Override
        protected void drawTopAndBottom(Tessellator tessellator) {
        }

        @Override
        protected void overlayBackground(int startY, int endY, int startAlpha, int endAlpha) {
        }
    }

    private class StringEntry implements CatalogueListExtended.IGuiListEntry {
        private String line;

        public StringEntry(String line) {
            this.line = line;
        }

        @Override
        public void drawEntry(int index, int left, int top, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean hovered, float partialTicks) {
            drawString(CatalogueModListScreen.this.fontRenderer, this.line, left, top, 0xFFFFFF);
        }

        @Override
        public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
            return true;
        }

        @Override
        public void updatePosition(int slotIndex, int x, int y, float partialTicks) {
        }

        @Override
        public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {
        }
    }


    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        this.modList.handleMouseInput();
        this.descriptionList.handleMouseInput();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        // Catalogue button
        if (ClientHelper.isMouseWithin(10, 9, 10, 10, mouseX, mouseY) && button == 0) {
            this.openLink("https://www.curseforge.com/minecraft/mc-mods/catalogue");
            return;
        }

        // Version check button
        if (this.selectedModData != null) {
            int contentLeft = this.modList.right + 12 + 10;
            String version = I18n.format("fml.menu.mods.info.version", this.selectedModData.getDisplayVersion());
            int versionWidth = this.fontRenderer.getStringWidth(version);
            if (ClientHelper.isMouseWithin(contentLeft + versionWidth + 5, 92, 8, 8, mouseX, mouseY)) {
                ForgeVersion.CheckResult update = ForgeVersion.getCleanResult(this.selectedModData);
                if (shouldUpdate(update) && update.homepage != null) {
                    this.openLink(update.homepage);
                }
            }
        }

        // Search Text Field
        if (ClientHelper.isMouseWithin(this.searchTextField.x, this.searchTextField.y, this.searchTextField.width, this.searchTextField.height, mouseX, mouseY)) {
            // Right click to empty
            if (button == 1) {
                this.searchTextField.setText("");
                this.updateSearchField("");
                this.modList.filterAndUpdateList("");
                lastSearch = "";
                return;
            }
            // Left click to apply suggestions
            if (button == 0) {
                String text = this.searchTextField.getText();
                long currentTine = Minecraft.getSystemTime();
                if (!text.isEmpty() && currentTine - this.lastClickTime < 250L && !this.searchTextField.getIsTextTruncated()) {
                    text += this.searchTextField.getSuggestion();
                    this.searchTextField.setText(text);
                    this.updateSearchField(text);
                    this.modList.filterAndUpdateList(text);
                    lastSearch = text;
                    this.lastClickTime = currentTine;
                    return;
                }
                this.lastClickTime = currentTine;
            }
        }
        this.searchTextField.mouseClicked(mouseX, mouseY, button);

        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        this.searchTextField.updateCursorCounter();
    }

    /**
     * Draws everything considered left of the screen; title, search bar and mod list.
     *
     * @param mouseX       the current mouse x position
     * @param mouseY       the current mouse y position
     * @param partialTicks the partial ticks
     */
    private void drawModList(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(VERSION_CHECK_ICONS);
        ClientHelper.blit(this.modList.right - 24, 10, 24, 0, 8, 8, 64, 16);
        GlStateManager.disableBlend();

        this.modList.drawScreen(mouseX, mouseY, partialTicks);
        this.searchTextField.drawTextBox();

        String modsLabel = TextFormatting.BOLD + I18n.format("fml.menu.mods.title");
        String countLabel = TextFormatting.GRAY + "(" + CACHED_MODS.size() + ")";
        String title = modsLabel + " " + countLabel;
        int titleWidth = this.fontRenderer.getStringWidth(title);
        int titleLeft = this.modList.left + (this.modList.width - titleWidth) / 2;
        drawString(this.fontRenderer, title, titleLeft, 10, 0xFFFFFF);

        if (ClientHelper.isMouseWithin(this.modList.right - 14, 7, 14, 14, mouseX, mouseY)) {
            this.setActiveTooltip(I18n.format("fml.menu.mods.filterupdates"));
            this.tooltipYOffset = 10;
        }
    }

    /**
     * Draws everything considered right of the screen; logo, mod title, description and more.
     *
     * @param mouseX       the current mouse x position
     * @param mouseY       the current mouse y position
     * @param partialTicks the partial ticks
     */
    private void drawModInfo(int mouseX, int mouseY, float partialTicks) {
        this.drawVerticalLine(this.modList.right + 11, -1, this.height, 0xFF707070);
        drawRect(this.modList.right + 12, 0, this.width, this.height, 0x66000000);
        this.descriptionList.drawScreen(mouseX, mouseY, partialTicks);

        int contentLeft = this.modList.right + 12 + 10;
        int contentWidth = this.width - contentLeft - 10;

        if (this.selectedModData != null) {
            this.drawBackground(this.width - contentLeft + 10, this.modList.right + 12, 0);

            // Draw mod logo
            this.drawBanner(contentWidth, contentLeft, 10, this.width - (this.modList.right + 12 + 10) - 10, 50);

            // Draw mod name
            GlStateManager.pushMatrix();
            GlStateManager.translate(contentLeft, 70, 0);
            GlStateManager.scale(2.0F, 2.0F, 2.0F);
            drawString(this.fontRenderer, this.selectedModData.getName(), 0, 0, 0xFFFFFF);
            GlStateManager.popMatrix();

            // Draw version
            String modId = TextFormatting.DARK_GRAY + I18n.format("fml.menu.mods.info.modid", this.selectedModData.getModId());
            int modIdWidth = this.fontRenderer.getStringWidth(modId);
            drawString(this.fontRenderer, modId, contentLeft + contentWidth - modIdWidth, 92, 0xFFFFFF);

            // Draw version
            String displayVersion = this.selectedModData.getDisplayVersion();
            this.drawStringWithLabel("fml.menu.mods.info.version", displayVersion, contentLeft, 92, contentWidth, mouseX, mouseY, TextFormatting.GRAY, TextFormatting.WHITE);

            // Draw inner version if the display version is different from it
            int versionWidth = this.fontRenderer.getStringWidth(I18n.format("fml.menu.mods.info.version", displayVersion));
            String innerVersion = this.selectedModData.getVersion();
            if (!displayVersion.equals(innerVersion) && ClientHelper.isMouseWithin(contentLeft, 92, versionWidth, this.fontRenderer.FONT_HEIGHT, mouseX, mouseY)) {
                this.setActiveTooltip(innerVersion);
            }

            // Draws an icon if there is an update for the mod
            ForgeVersion.CheckResult update = ForgeVersion.getCleanResult(this.selectedModData);
            if (shouldDraw(update) && update.url != null) {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                this.mc.getTextureManager().bindTexture(VERSION_CHECK_ICONS);
                int vOffset = update.status.isAnimated() && (System.currentTimeMillis() / 800 & 1) == 1 ? 8 : 0;
                ClientHelper.blit(contentLeft + versionWidth + 5, 92, update.status.getSheetOffset() * 8, vOffset, 8, 8, 64, 16);
                if (ClientHelper.isMouseWithin(contentLeft + versionWidth + 5, 92, 8, 8, mouseX, mouseY)) {
                    switch (update.status) {
                        case BETA:
                            this.setActiveTooltip(TextFormatting.GOLD + I18n.format("fml.menu.mods.info.beta"));
                            break;
                        case AHEAD:
                            this.setActiveTooltip(TextFormatting.LIGHT_PURPLE + I18n.format("fml.menu.mods.info.ahead", update.latestFound));
                            break;
                        case BETA_OUTDATED:
                            if (update.homepage != null) {
                                this.setActiveTooltip(TextFormatting.GOLD + I18n.format("fml.menu.mods.info.betaupdateavailable", update.latestFound, update.homepage));
                            } else {
                                this.setActiveTooltip(TextFormatting.GOLD + I18n.format("fml.menu.mods.info.betaupdateavailablenopage", update.latestFound));
                            }
                            break;
                        case OUTDATED:
                            if (update.homepage != null) {
                                this.setActiveTooltip(TextFormatting.GREEN + I18n.format("fml.menu.mods.info.updateavailable", update.latestFound, update.homepage));
                            } else {
                                this.setActiveTooltip(TextFormatting.GREEN + I18n.format("fml.menu.mods.info.updateavailablenopage", update.latestFound));
                            }
                            break;
                    }
                }
            }

            ModMetadata metadata = selectedModData.getMetadata();
            if (metadata != null && !metadata.autogenerated) {

                // Draw fade from the bottom
                drawGradientRect(this.modList.right + 12, this.height - 50, this.width, this.height, 0x00000000, 0x66000000);

                int labelOffset = this.height - 18;

                // Draw license
                String license = metadata.license;
                if (!license.isBlank()) {
                    this.drawStringWithLabel("fml.menu.mods.info.license", license, contentLeft, labelOffset, contentWidth, mouseX, mouseY, TextFormatting.GRAY, TextFormatting.WHITE);
                    labelOffset -= 15;
                }

                // Draw credits
                String credits = metadata.credits;
                if (!credits.isBlank()) {
                    this.drawStringWithLabel("fml.menu.mods.info.credits", credits, contentLeft, labelOffset, contentWidth, mouseX, mouseY, TextFormatting.GRAY, TextFormatting.WHITE);
                    labelOffset -= 15;
                }

                // Draw authors
                String authors = metadata.getAuthorList();
                if (!authors.isBlank()) {
                    this.drawStringWithLabel("fml.menu.mods.info.authors", authors, contentLeft, labelOffset, contentWidth, mouseX, mouseY, TextFormatting.GRAY, TextFormatting.WHITE);
                }
            }
        } else {
            String message = TextFormatting.GRAY + I18n.format("fml.menu.mods.noselection");
            drawCenteredString(this.fontRenderer, message, contentLeft + contentWidth / 2, this.height / 2 - 5, 0xFFFFFF);
        }
    }

    /**
     * Draws a string and prepends a label. If the formed string and label is longer than the
     * specified max width, it will automatically be trimmed and allows the user to hover the
     * string with their mouse to read the full contents.
     *
     * @param format   a string to prepend to the content
     * @param text     the string to render
     * @param x        the x position
     * @param y        the y position
     * @param maxWidth the maximum width the string can render
     * @param mouseX   the current mouse x position
     * @param mouseY   the current mouse u position
     */
    @SuppressWarnings("SameParameterValue")
    private void drawStringWithLabel(String format, String text, int x, int y, int maxWidth, int mouseX, int mouseY, TextFormatting labelColor, TextFormatting contentColor) {
        String formatted = I18n.format(format, text); // Attempting to keep Forge's lang since it's already support many languages
        String label = formatted.substring(0, formatted.indexOf(":") + 1);
        String content = formatted.substring(formatted.indexOf(":") + 1);
        if (this.fontRenderer.getStringWidth(formatted) > maxWidth) {
            content = this.fontRenderer.trimStringToWidth(content, maxWidth - this.fontRenderer.getStringWidth(label) - 7) + "...";
            String credits = labelColor + label;
            credits += contentColor + content;
            drawString(this.fontRenderer, credits, x, y, 0xFFFFFF);
            if (ClientHelper.isMouseWithin(x, y, maxWidth, 9, mouseX, mouseY)) { // Sets the active tool tip if string is too long so users can still read it
                this.setActiveTooltip(text);
            }
        } else {
            drawString(this.fontRenderer, labelColor + label + contentColor + content, x, y, 0xFFFFFF);
        }
    }

    private void loadAndCacheLogo(ModContainer data) {
        if (BANNER_CACHE.containsKey(data.getModId())) return;

        // Fills an empty logo as logo may not be present
        BANNER_CACHE.put(data.getModId(), null);

        // Attempts to load the real logo
        ModMetadata metadata = data.getMetadata();
        if (metadata == null) return;
        String banner = metadata.logoFile;
        if (banner.isBlank()) return;

        IResourcePack resourcePack = FMLClientHandler.instance().getResourcePackFor(data.getModId());
        BufferedImage image = null;
        try {
            if (resourcePack != null && !banner.startsWith("/")) {
                image = resourcePack.getPackImage();
            } else {
                if (!banner.startsWith("/")) {
                    banner = "/" + banner;
                }
                InputStream is = getClass().getResourceAsStream(banner);
                if (is != null) image = TextureUtil.readBufferedImage(is);
            }
            if (image == null) return;
            TextureManager manager = this.mc.getTextureManager();
            ResourceLocation resource = manager.getDynamicTextureLocation("modlogo", this.createLogoTexture(image, metadata.logoBlur));
            Dimension size = new Dimension(image.getWidth(), image.getHeight());
            BANNER_CACHE.put(data.getModId(), new ImageInfo(resource, size));
        } catch (IOException ignored) {
        }
    }

    private void loadAndCacheIcon(ModContainer data) {
        if (IMAGE_ICON_CACHE.containsKey(data.getModId())) return;

        // Fills an empty icon as icon may not be present
        IMAGE_ICON_CACHE.put(data.getModId(), null);

        ModMetadata metadata = data.getMetadata();
        if (metadata == null) return;

        // Attempts to load the real icon
        String iconFile = metadata.iconFile;
        if (!iconFile.isBlank()) {
            if (!iconFile.startsWith("/")) {
                iconFile = "/" + iconFile;
            }
            BufferedImage image = null;
            try (InputStream is = getClass().getResourceAsStream(iconFile)) {
                if (is != null) image = TextureUtil.readBufferedImage(is);
                if (image != null) {
                    TextureManager manager = this.mc.getTextureManager();
                    ResourceLocation resource = manager.getDynamicTextureLocation("catalogueicon", this.createLogoTexture(image, metadata.logoBlur));
                    Dimension size = new Dimension(image.getWidth(), image.getHeight());
                    IMAGE_ICON_CACHE.put(data.getModId(), new ImageInfo(resource, size));
                    return;
                }
            } catch (IOException ignored) {
            }
        }

        // Attempts to use the logo file if it's a square
        String logoFile = metadata.logoFile;
        if (!logoFile.isBlank()) {
            IResourcePack resourcePack = FMLClientHandler.instance().getResourcePackFor(data.getModId());
            BufferedImage image = null;
            try {
                if (resourcePack != null && !logoFile.startsWith("/")) {
                    image = resourcePack.getPackImage();
                } else {
                    if (!logoFile.startsWith("/")) {
                        logoFile = "/" + logoFile;
                    }
                    InputStream is = getClass().getResourceAsStream(logoFile);
                    if (is != null) image = TextureUtil.readBufferedImage(is);
                }
                if (image == null || image.getWidth() != image.getHeight()) return;

                /* The first selected mod will have its logo cached before the icon, so we
                 * can just use the logo instead of loading the image again. */
                String modId = data.getModId();
                if (BANNER_CACHE.containsKey(modId)) {
                    IMAGE_ICON_CACHE.put(modId, BANNER_CACHE.get(modId));
                    return;
                }

                /* Since the icon will be same as the logo, we can cache into both icon and logo cache */
                TextureManager manager = this.mc.getTextureManager();
                DynamicTexture texture = this.createLogoTexture(image, metadata.logoBlur);
                Dimension size = new Dimension(image.getWidth(), image.getHeight());
                ResourceLocation resource = manager.getDynamicTextureLocation("catalogueicon", texture);
                IMAGE_ICON_CACHE.put(modId, new ImageInfo(resource, size));
                BANNER_CACHE.put(modId, new ImageInfo(resource, size));
            } catch (IOException ignored) {
            }
        }
    }

    private void loadAndCacheBackground(ModContainer data) {
        // Deletes the last cached background since they are large images
        if (cachedBackground != null) {
            this.mc.getTextureManager().deleteTexture(cachedBackground);
        }
        cachedBackground = null;

        ModMetadata metadata = data.getMetadata();
        if (metadata == null) return;
        String background = metadata.backgroundFile;
        if(background.isBlank()) return;

        if (!background.startsWith("/")) {
            background = "/" + background;
        }

        BufferedImage image = null;
        try (InputStream is = getClass().getResourceAsStream(background)) {
            if (is != null) image = TextureUtil.readBufferedImage(is);
            if (image == null || image.getWidth() != 512 || image.getHeight() != 256) return;
            TextureManager textureManager = this.mc.getTextureManager();
            cachedBackground = textureManager.getDynamicTextureLocation("cataloguebackground", this.createLogoTexture(image, false));
        } catch(IOException ignored) {
        }
    }

    private DynamicTexture createLogoTexture(BufferedImage image, boolean smooth) {
        return new DynamicTexture(image) {
            @Override
            public void updateDynamicTexture() {
                TextureUtil.uploadTextureImageAllocate(this.getGlTextureId(), image, smooth, false);
            }
        };
    }

    @SuppressWarnings("SameParameterValue")
    private void drawBackground(int contentWidth, int x, int y) {
        if(this.selectedModData == null) return;

        ResourceLocation texture = cachedBackground != null ? cachedBackground : MISSING_BACKGROUND;
        this.mc.getTextureManager().bindTexture(texture);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBuffer();
        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        builder.pos(x, y, this.zLevel).tex(0, 0).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        builder.pos(x, y + 128, this.zLevel).tex(0, 1).color(0.0F, 0.0F, 0.0F, 0.0F).endVertex();
        builder.pos(x + contentWidth, y + 128, this.zLevel).tex(1, 1).color(0.0F, 0.0F, 0.0F, 0.0F).endVertex();
        builder.pos(x + contentWidth, y, this.zLevel).tex(1, 0).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        tessellator.draw();

        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.shadeModel(GL11.GL_FLAT);
    }

    @SuppressWarnings("SameParameterValue")
    private void drawBanner(int contentWidth, int x, int y, int maxWidth, int maxHeight) {
        if (this.selectedModData != null) {
            ImageInfo bannerInfo = this.getBanner(this.selectedModData.getModId());
            Dimension size = bannerInfo.size();
            int width = size.width;
            int height = size.height;
            if (size.width > maxWidth) {
                width = maxWidth;
                height = (width * size.height) / size.width;
            }
            if (height > maxHeight) {
                height = maxHeight;
                width = (height * size.width) / size.height;
            }

            x += (contentWidth - width) / 2;
            y += (maxHeight - height) / 2;

            // Fix for minecraft logo
            if (bannerInfo.resource() == MINECRAFT_LOGO) {
                y += 8;
            }

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableBlend();
            this.mc.getTextureManager().bindTexture(bannerInfo.resource());
            ClientHelper.blit(x, y, width, height, 0.0F, 0.0F, size.width, size.height, size.width, size.height);

            GlStateManager.disableBlend();
        }
    }

    private ImageInfo getBanner(String modId) {
        // Try getting the banner for the mod
        ImageInfo bannerInfo = BANNER_CACHE.get(modId);
        if (bannerInfo != null) return bannerInfo;

        // Try using the icon image for the banner
        ImageInfo iconInfo = IMAGE_ICON_CACHE.get(modId);
        if (iconInfo != null) {
            // Hack to make icon fill max banner height
            Dimension size = iconInfo.size();
            Dimension newSize = new Dimension(size.width * 10, size.height * 10);
            return new ImageInfo(iconInfo.resource(), newSize);
        }

        // Fallback and just use missing banner
        return MISSING_BANNER_INFO;
    }

    private void setActiveTooltip(String content) {
        this.activeTooltip = this.fontRenderer.listFormattedStringToWidth(content, 200);
        this.tooltipYOffset = 0;
    }

    private void setSelectedModData(ModContainer data) {
        this.selectedModData = data;
        this.loadAndCacheLogo(data);
        this.loadAndCacheBackground(data);
        this.configButton.visible = true;
        this.websiteButton.visible = true;
        this.issueButton.visible = true;

        if (!this.selectedModData.getModId().equals("minecraft")) {
            this.configButton.enabled = false;
            IModGuiFactory guiFactory = FMLClientHandler.instance().getGuiFactoryFor(data);
            if (guiFactory != null) {
                this.configButton.enabled = guiFactory.hasConfigGui();
            }
        } else {
            this.configButton.enabled = true;
        }

        ModMetadata metadata = data.getMetadata();
        if (metadata != null && !metadata.autogenerated) {
            this.websiteButton.enabled = !metadata.url.isBlank();
            this.issueButton.enabled = !metadata.issueTrackerUrl.isBlank();
        }

        int contentLeft = this.modList.right + 12 + 10;
        int contentWidth = this.width - contentLeft - 10;
        int labelCount = this.getLabelCount(data);
        this.descriptionList.setWidth(contentWidth);
        this.descriptionList.setHeight(this.height - 135 - labelCount * 15 - 9);
        this.descriptionList.setSlotXBoundsFromLeft(contentLeft);
        this.descriptionList.setTextFromInfo(data);
        this.descriptionList.setAmountScrolled(0);
    }

    private int getLabelCount(ModContainer selectedModInfo) {
        int count = 0;
        ModMetadata metadata = selectedModInfo.getMetadata();
        if (metadata != null && !metadata.autogenerated) {
            if (!metadata.license.isBlank()) count++;
            if (!metadata.credits.isBlank()) count++;
            if (!metadata.authorList.isEmpty()) count++;
        }
        return count;
    }

    private void updateSelectedModList() {
        ModListEntry selectedEntry = this.modList.getEntryFromInfo(this.selectedModData);
        if (selectedEntry != null) {
            this.modList.selectMod(selectedEntry);
        }
    }

    private void updateSearchField(String value) {
        if (value.isEmpty()) {
            this.searchTextField.setSuggestion(I18n.format("fml.menu.mods.searchwithdots"));
        } else {
            Optional<ModContainer> optional = CACHED_MODS.values().stream().filter(data -> {
                return data.getName().toLowerCase(Locale.ENGLISH).startsWith(value.toLowerCase(Locale.ENGLISH));
            }).min(Comparator.comparing(ModContainer::getName));
            if (optional.isPresent()) {
                int length = value.length();
                String displayName = optional.get().getName();
                this.searchTextField.setSuggestion(displayName.substring(length));
            } else {
                this.searchTextField.setSuggestion("");
            }
        }
    }

    /**
     * Opens a link with a url defined in the mod's info
     */
    private void openLink(int key, @Nullable ModContainer configurable) {
        if (configurable != null) {
            ModMetadata metadata = configurable.getMetadata();
            // The config button is only enabled when checked, so it is unnecessary to check again.
            switch (key) {
                case 0:
                    this.openLink(metadata.url);
                    break;
                case 1:
                    this.openLink(metadata.issueTrackerUrl);
                    break;
            }
        }
    }

    private void openLink(String url) {
        Style style = new Style().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        this.handleComponentClick(new TextComponentString("").setStyle(style));
    }

    private record Dimension(int width, int height) {}

    private record ImageInfo(ResourceLocation resource, Dimension size) {}

    private boolean shouldDraw(ForgeVersion.CheckResult update) {
        return update != null && update.status.shouldDraw();
    }

    private boolean shouldUpdate(ForgeVersion.CheckResult update) {
        if (update == null) return false;
        ForgeVersion.Status status = update.status;
        return status == ForgeVersion.Status.OUTDATED || status == ForgeVersion.Status.BETA_OUTDATED;
    }
}
