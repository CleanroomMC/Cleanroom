package com.cleanroommc.catalogue.client.screen;

import com.cleanroommc.catalogue.CatalogueConstants;
import com.cleanroommc.catalogue.Utils;
import com.cleanroommc.catalogue.client.Branding;
import com.cleanroommc.catalogue.client.ClientHelper;
import com.cleanroommc.catalogue.client.IModData;
import com.cleanroommc.catalogue.client.ImageInfo;
import com.cleanroommc.catalogue.client.screen.widget.*;
import com.cleanroommc.catalogue.platform.ClientServices;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CatalogueModListScreen extends GuiScreen implements DropdownMenuHandler {
    private static final Favourites FAVOURITES = new Favourites();
    private static final Comparator<ModListEntry> SORT_ALPHABETICALLY = Comparator.comparing(o -> o.getData().getDisplayName());
    private static final Comparator<ModListEntry> SORT_ALPHABETICALLY_REVERSED = SORT_ALPHABETICALLY.reversed();
    private static final Comparator<ModListEntry> SORT_FAVOURITES_FIRST = Comparator.comparing(ModListEntry::getData, Comparator.comparing(data -> FAVOURITES.has(data.getModId()))).reversed().thenComparing(SORT_ALPHABETICALLY);
    private static final MutableObject<String> OPTION_QUERY = new MutableObject<>("");
    private static final MutableBoolean OPTION_HIDE_LIBRARIES = new MutableBoolean(true);
    private static final MutableBoolean OPTION_CONFIGS_ONLY = new MutableBoolean(false);
    private static final MutableBoolean OPTION_UPDATES_ONLY = new MutableBoolean(false);
    private static final MutableBoolean OPTION_FAVOURITES_ONLY = new MutableBoolean(false);
    private static final MutableObject<Comparator<ModListEntry>> OPTION_SORT = new MutableObject<>(SORT_ALPHABETICALLY);
    private static final ResourceLocation MISSING_BANNER = Utils.resource("textures/gui/missing_banner.png");
    private static final ResourceLocation MISSING_BACKGROUND = Utils.resource("textures/gui/missing_background.png");
    private static final ResourceLocation MINECRAFT_LOGO = Utils.resource("textures/gui/minecraft.png");
    private static final ImageInfo MISSING_BANNER_INFO = new ImageInfo(MISSING_BANNER, 120, 120, () -> {
    });
    private static final Map<String, ImageInfo> BANNER_CACHE = new HashMap<>();
    private static final Map<String, ImageInfo> IMAGE_ICON_CACHE = new HashMap<>();
    private static final Map<String, ItemStack> ITEM_ICON_CACHE = new HashMap<>();
    private static final Map<String, IModData> CACHED_MODS = new HashMap<>();
    private static final Pattern MOD_ID_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]{1,63}$");
    private static final Supplier<Pair<Integer, Integer>> COUNTS = Suppliers.memoize(() -> {
        int[] counts = new int[2];
        CACHED_MODS.forEach((modId, data) -> counts[data.isLibrary() ? 1 : 0]++);
        return Pair.of(counts[0], counts[1]);
    });
    private static final Map<String, SearchFilter> SEARCH_FILTERS = ImmutableMap.<String, SearchFilter>builder()
            .put("dependencies", new SearchFilter((query, data) -> {
                IModData target = CACHED_MODS.get(query.toLowerCase(Locale.ENGLISH));
                return target != null && target.getDependencies().contains(data.getModId());
            }))
            .put("dependents", new SearchFilter((query, data) -> {
                return data.getDependencies().contains(query.toLowerCase(Locale.ENGLISH));
            })).build();
    private static final TextFormatting SEARCH_FILTER_KEY = TextFormatting.GOLD;
    private static final TextFormatting SEARCH_FILTER_VALUE = TextFormatting.WHITE;
    private static ImageInfo cachedBackground;
    private static boolean loaded = false;

    private final GuiScreen parentScreen;
    private CatalogueTextButton optionsButton;
    private CatalogueTextField searchTextField;
    private ModList modList;
    private IModData selectedModData;
    private CatalogueIconButton modFolderButton;
    private CatalogueIconButton configButton;
    private CatalogueIconButton websiteButton;
    private CatalogueIconButton issueButton;

    private List<String> activeTooltip;
    private int tooltipYOffset;
    private StringList descriptionList;
    private @Nullable DropdownMenu menu;

    private long lastClickTime;

    public CatalogueModListScreen(GuiScreen parent) {
        super();
        this.parentScreen = parent;
        if (!loaded) {
            ClientServices.PLATFORM.getAllModData().forEach(data -> CACHED_MODS.put(data.getModId(), data));
            CACHED_MODS.put("minecraft", new MinecraftModData()); // Override minecraft
            BANNER_CACHE.put("minecraft", new ImageInfo(MINECRAFT_LOGO, 1024, 256, () -> {
            }));
            FAVOURITES.load();
            loaded = true;
        }
    }

    @Override
    public void setMenu(@Nullable DropdownMenu menu) {
        if (this.menu != null && this.menu != menu) {
            this.menu.hide();
        }
        this.menu = menu;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.searchTextField = new CatalogueTextField(0, this.fontRenderer, 11, 25, 148, 20) {
            @Override
            public int getWidth() {
                if (this.getText().startsWith("@")) {
                    return super.getWidth() - 16;
                }
                return super.getWidth();
            }
        };
        this.searchTextField.setFormatter(this::formatQuery);
        this.searchTextField.setMaxStringLength(128);
        this.searchTextField.setText(OPTION_QUERY.getValue());
        this.searchTextField.setResponder(s -> {
            if (!OPTION_QUERY.getValue().equals(s)) {
                OPTION_QUERY.setValue(s);
                this.updateSearchFieldSuggestion(s);
                this.modList.filterAndUpdateList();
                this.updateSelectedModList();
            }
        });

        this.modList = new ModList();
        this.modList.setSlotXBoundsFromLeft(10);

        this.addButton(new CatalogueTextButton(1, 10, modList.bottom + 8, 127, 20, I18n.format("gui.back")));
        this.modFolderButton = this.addButton(new CatalogueIconButton(2, 140, modList.bottom + 8, 0, 0));

        int padding = 10;
        int contentLeft = this.modList.right + 12 + padding;
        int contentWidth = this.width - contentLeft - padding;
        int buttonWidth = (contentWidth - padding) / 3;

        this.configButton = this.addButton(new CatalogueIconButton(3, contentLeft, 105, 10, 0, buttonWidth, I18n.format("catalogue.gui.config")));
        this.configButton.visible = false;

        this.websiteButton = this.addButton(new CatalogueIconButton(4, contentLeft + buttonWidth + 5, 105, 20, 0, buttonWidth, I18n.format("catalogue.gui.website")));
        this.websiteButton.visible = false;

        this.issueButton = this.addButton(new CatalogueIconButton(5, contentLeft + buttonWidth + buttonWidth + 10, 105, 30, 0, buttonWidth, I18n.format("catalogue.gui.submit_bug")));
        this.issueButton.visible = false;

        this.descriptionList = new StringList(contentWidth + padding * 2, 50, contentLeft - padding, 130);

        this.optionsButton = this.addButton(new CatalogueIconButton(6, this.modList.right - 16, 6, 40, 0, 16, 16));

        this.modList.filterAndUpdateList();

        // Resizing window causes all widgets to be recreated, therefore need to update selected info
        if (this.selectedModData != null) {
            this.setSelectedModData(this.selectedModData);
            this.updateSelectedModList();
            ModListEntry entry = this.modList.getEntryFromInfo(this.selectedModData);
            if (entry != null) {
                this.modList.centerScrollOn(entry);
            }
        }
        this.updateSearchFieldSuggestion(this.searchTextField.getText());
    }

    @Override
    public void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 1:
                this.mc.displayGuiScreen(parentScreen);
                break;
            case 2:
                try {
                    Desktop.getDesktop().open(ClientServices.PLATFORM.getModDirectory());
                } catch (Exception e) {
                    CatalogueConstants.LOG.error("Problem opening mods folder", e);
                }
                break;
            case 3:
                if (this.selectedModData != null) {
                    this.selectedModData.openConfigScreen(this.mc, this);
                }
                break;
            case 4:
                this.openLink(this.selectedModData.getHomepage());
                break;
            case 5:
                this.openLink(this.selectedModData.getIssueTracker());
                break;
            case 6:
                DropdownMenu menu = DropdownMenu.builder(this)
                        .setMinItemSize(100, 16)
                        .setAlignment(DropdownMenu.Alignment.BELOW_RIGHT)
                        .addMenu(I18n.format("catalogue.gui.filters"), DropdownMenu.builder(this)
                                .setMinItemSize(60, 16)
                                .setAlignment(DropdownMenu.Alignment.END_TOP)
                                .addCheckbox(I18n.format("catalogue.gui.filters.configs_only"), OPTION_CONFIGS_ONLY, newValue -> {
                                    this.modList.filterAndUpdateList();
                                    return false;
                                })
                                .addCheckbox(I18n.format("catalogue.gui.filters.updates_only"), OPTION_UPDATES_ONLY, newValue -> {
                                    this.modList.filterAndUpdateList();
                                    return false;
                                })
                                .addCheckbox(I18n.format("catalogue.gui.filters.favourites"), OPTION_FAVOURITES_ONLY, newValue -> {
                                    this.modList.filterAndUpdateList();
                                    return false;
                                }))
                        .addMenu(I18n.format("catalogue.gui.sort"), DropdownMenu.builder(this)
                                .setMinItemSize(60, 16)
                                .setAlignment(DropdownMenu.Alignment.END_TOP)
                                .addItem(I18n.format("catalogue.gui.sort.alphabetically"), () -> {
                                    OPTION_SORT.setValue(SORT_ALPHABETICALLY);
                                    this.modList.filterAndUpdateList();
                                })
                                .addItem(I18n.format("catalogue.gui.sort.alphabetically_reverse"), () -> {
                                    OPTION_SORT.setValue(SORT_ALPHABETICALLY_REVERSED);
                                    this.modList.filterAndUpdateList();
                                })
                                .addItem(I18n.format("catalogue.gui.sort.favourites_first"), () -> {
                                    OPTION_SORT.setValue(SORT_FAVOURITES_FIRST);
                                    this.modList.filterAndUpdateList();
                                }))
                        .addCheckbox(I18n.format("catalogue.gui.hide_libraries"), OPTION_HIDE_LIBRARIES, newValue -> {
                            this.modList.filterAndUpdateList();
                            return false;
                        }).build();
                menu.toggle(button);
                break;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.activeTooltip = null;

        boolean inMenu = this.menu != null;
        this.drawDefaultBackground();
        int disableableMouseX = inMenu ? -1000 : mouseX;
        int disableableMouseY = inMenu ? -1000 : mouseY;
        this.drawModList(disableableMouseX, disableableMouseY, partialTicks);
        this.drawModInfo(disableableMouseX, disableableMouseY, partialTicks);
        super.drawScreen(disableableMouseX, disableableMouseY, partialTicks);

        if (OPTION_QUERY.getValue().startsWith("@")) {
            int iconX = this.searchTextField.x + this.searchTextField.width - 15;
            int iconY = this.searchTextField.y + (this.searchTextField.height - 10) / 2;
            this.mc.getTextureManager().bindTexture(CatalogueIconButton.TEXTURE);
            drawModalRectWithCustomSizedTexture(iconX, iconY, 20, 10, 10, 10, 64, 64);

            if (this.menu == null && ClientHelper.isMouseWithin(iconX, iconY, 10, 10, mouseX, mouseY)) {
                this.setActiveTooltip(I18n.format("catalogue.gui.advanced_search.info"));
            }
        }

        Optional<IModData> optional = Optional.ofNullable(CACHED_MODS.get(CatalogueConstants.MOD_ID));
        optional.ifPresent(this::loadAndCacheLogo);
        ImageInfo bannerInfo = BANNER_CACHE.get(CatalogueConstants.MOD_ID);
        if (bannerInfo != null) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableBlend();
            this.mc.getTextureManager().bindTexture(bannerInfo.resource());
            drawScaledCustomSizeModalRect(10, 9, 0.0F, 0.0F, bannerInfo.width(), bannerInfo.height(), 10, 10, bannerInfo.width(), bannerInfo.height());
            GlStateManager.disableBlend();
        }

        if (this.menu != null) {
            this.menu.drawScreen(this.mc, mouseX, mouseY, partialTicks);
        } else {
            if (ClientHelper.isMouseWithin(10, 9, 10, 10, mouseX, mouseY)) {
                this.setActiveTooltip(I18n.format("catalogue.gui.info"));
                this.tooltipYOffset = 10;
            }

            if (this.optionsButton.isMouseOver()) {
                this.setActiveTooltip(I18n.format("catalogue.gui.options"));
                this.tooltipYOffset = 10;
            }

            if (this.modFolderButton.isMouseOver()) {
                this.setActiveTooltip(I18n.format("catalogue.gui.open_mods_folder"));
            }
        }

        if (this.activeTooltip != null) {
            this.drawHoveringText(this.activeTooltip, mouseX, mouseY + this.tooltipYOffset);
            this.tooltipYOffset = 0;
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
        // Menu widget
        if (this.menu != null) {
            if (!this.menu.mousePressed(this.mc, mouseX, mouseY)) {
                this.setMenu(null);
            }
            return;
        }

        // Mod List
        if (this.modList.mouseClicked(mouseX, mouseY, button)) return;

        // Catalogue button
        if (ClientHelper.isMouseWithin(10, 9, 10, 10, mouseX, mouseY) && button == 0) {
            this.openLink("https://www.curseforge.com/minecraft/mc-mods/catalogue");
            return;
        }

        // Version check button
        if (this.selectedModData != null) {
            int contentLeft = this.modList.right + 12 + 10;
            String version = this.selectedModData.getVersion();
            int versionWidth = this.fontRenderer.getStringWidth(version);
            if (ClientHelper.isMouseWithin(contentLeft + versionWidth + 5, 92, 8, 8, mouseX, mouseY)) {
                IModData.Update update = this.selectedModData.getUpdate();
                if (update != null && update.homepage() != null && !update.homepage().isBlank() && update.updatable()) {
                    this.openLink(update.homepage());
                }
            }
        }

        // Search Text Field
        this.searchTextField.mouseClicked(mouseX, mouseY, button);
        if (ClientHelper.isMouseWithin(this.searchTextField.x, this.searchTextField.y, this.searchTextField.width, this.searchTextField.height, mouseX, mouseY)) {
            // Right click to empty
            if (button == 1) {
                this.searchTextField.setText("");
                return;
            }
            // Left click to apply suggestions
            if (button == 0) {
                String text = this.searchTextField.getText();
                long currentTine = Minecraft.getSystemTime();
                if (!text.isEmpty() && currentTine - this.lastClickTime < 250L && !this.searchTextField.getIsTextTruncated()) {
                    text += this.searchTextField.getSuggestion();
                    this.searchTextField.setText(text);
                    this.lastClickTime = currentTine;
                    return;
                }
                this.lastClickTime = currentTine;
            }
        }

        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int button) {
        if (this.modList.mouseReleased(mouseX, mouseY, button)) return;
        super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    protected void keyTyped(char typedChar, int key) throws IOException {
        if (isKeyComboCtrlF(key) && !this.searchTextField.isFocused()) {
            this.searchTextField.setFocused(true);
            return;
        }
        if (this.searchTextField.textboxKeyTyped(typedChar, key)) return;
        super.keyTyped(typedChar, key);
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
        this.modList.drawScreen(mouseX, mouseY, partialTicks);
        this.searchTextField.drawTextBox();

        String modsLabel = TextFormatting.BOLD + I18n.format("catalogue.gui.mod_list");
        String countLabel = TextFormatting.GRAY + "(" + CACHED_MODS.size() + ")";
        String title = modsLabel + " " + countLabel;
        int titleWidth = this.fontRenderer.getStringWidth(title);
        int titleLeft = this.modList.left + (this.modList.width - titleWidth) / 2;
        drawString(this.fontRenderer, title, titleLeft, 10, 0xFFFFFF);

        int countLabelWidth = this.fontRenderer.getStringWidth(countLabel);
        if (ClientHelper.isMouseWithin(titleLeft + titleWidth - countLabelWidth, 10, countLabelWidth, this.fontRenderer.FONT_HEIGHT, mouseX, mouseY)) {
            Pair<Integer, Integer> counts = COUNTS.get();
            List<String> lines = List.of(
                    I18n.format("catalogue.gui.mod_count", counts.getLeft()),
                    I18n.format("catalogue.gui.library_count", counts.getRight())
            );
            this.setActiveTooltip(lines);
            this.tooltipYOffset = 10;
        }
    }

    private class ModList extends CatalogueListExtended {
        private static final Predicate<IModData> SEARCH_PREDICATE = data -> {
            String query = OPTION_QUERY.getValue();
            if (query.startsWith("@")) {
                return performSearchFilter(query, data);
            }
            return data.getDisplayName()
                    .toLowerCase(Locale.ENGLISH)
                    .contains(query.toLowerCase(Locale.ENGLISH));
        };
        private static final Predicate<IModData> FILTER_PREDICATE = data -> {
            // We ignore filters when using special query
            String query = OPTION_QUERY.getValue();
            if (query.startsWith("@")) {
                return true;
            }
            if (OPTION_CONFIGS_ONLY.booleanValue() && !data.hasConfig()) {
                return false;
            }
            if (OPTION_UPDATES_ONLY.booleanValue() && (data.getUpdate() == null || !data.getUpdate().updatable())) {
                return false;
            }
            if (OPTION_HIDE_LIBRARIES.booleanValue() && data.isLibrary()) {
                return false;
            }
            if (OPTION_FAVOURITES_ONLY.booleanValue() && !FAVOURITES.has(data.getModId())) {
                return false;
            }
            return true;
        };
        private boolean hideFavourites;
        private List<ModListEntry> children = Lists.newArrayList();
        private ModListEntry selected;

        public ModList() {
            super(CatalogueModListScreen.this.mc, 150, CatalogueModListScreen.this.height, 46, CatalogueModListScreen.this.height - 35, 26);
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            ClientHelper.scissor(this.getListLeft(), this.top, this.width, this.bottom - this.top);
            super.drawScreen(mouseX, mouseY, partialTicks);
            GL11.glDisable(GL11.GL_SCISSOR_TEST);

            if (this.children.isEmpty()) {
                String text = I18n.format("catalogue.gui.no_mods");
                int left = this.left + this.width / 2;
                int top = this.top + (this.bottom - this.top - CatalogueModListScreen.this.fontRenderer.FONT_HEIGHT) / 2;
                drawCenteredString(CatalogueModListScreen.this.fontRenderer, text, left, top, 0xFFFFFFFF);
            }
        }

        public void filterAndUpdateList() {
            List<ModListEntry> entries = CACHED_MODS.values().stream()
                    .filter(SEARCH_PREDICATE)
                    .filter(FILTER_PREDICATE)
                    .map(data -> new ModListEntry(data, this))
                    .sorted(OPTION_SORT.getValue())
                    .collect(Collectors.toList());
            this.replaceEntries(entries);
            this.clampAmountScrolled();
        }

        public ModListEntry getEntryFromInfo(IModData data) {
            return this.children.stream().filter(entry -> entry.data == data).findFirst().orElse(null);
        }

        protected void centerScrollOn(ModListEntry pEntry) {
            this.setAmountScrolled((float) (this.children.indexOf(pEntry) * this.slotHeight + this.slotHeight / 2 - (this.bottom - this.top) / 2));
        }

        protected void clearEntries() {
            this.children.clear();
            this.selected = null;
        }

        protected void replaceEntries(Collection<ModListEntry> entries) {
            this.clearEntries();
            this.children.addAll(entries);
        }

        @Override
        public IGuiListEntry getListEntry(int index) {
            return this.children.get(index);
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
            return this.children.size();
        }

        public ModListEntry getSelected() {
            return this.selected;
        }

        public void setSelected(@Nullable ModListEntry selected) {
            this.selected = selected;
        }

        @Override
        protected boolean isSelected(int slotIndex) {
            return Objects.equals(this.getSelected(), this.children.get(slotIndex));
        }

        @Override
        protected void drawContainerBackground(@NotNull Tessellator tessellator) {
            if (ClientHelper.isPlayingGame()) {
                drawRect(this.left, this.top, this.right, this.bottom, 0x66000000);
                return;
            }
            super.drawContainerBackground(tessellator);
        }

        @Override
        protected void overlayBackground(int startY, int endY, int startAlpha, int endAlpha) {
        }

        @Override
        public void handleMouseInput() {
            this.hideFavourites = Mouse.getEventDWheel() != 0;
            super.handleMouseInput();
        }

        @Override
        public boolean mouseReleased(int mouseX, int mouseY, int button) {
            if (this.hideFavourites) {
                this.hideFavourites = false;
            }
            return super.mouseReleased(mouseX, mouseY, button);
        }

        public boolean isMouseOver() {
            return ClientHelper.isMouseWithin(this.getListLeft(), this.top, this.width, this.bottom - this.top, this.mouseX, this.mouseY);
        }

        public boolean shouldHideFavourites() {
            return this.hideFavourites;
        }
    }

    private static boolean performSearchFilter(String query, IModData data) {
        if (!query.startsWith("@"))
            return false;

        int end = query.indexOf(":");
        if (end == -1)
            return false;

        String type = query.substring(1, end).toLowerCase(Locale.ENGLISH);
        if (!SEARCH_FILTERS.containsKey(type))
            return false;

        String value = query.substring(end + 1);
        return SEARCH_FILTERS.get(type).predicate().test(value, data);
    }

    private String formatQuery(String partial, int displayPos) {
        String query = OPTION_QUERY.getValue();
        if (!query.startsWith("@")) {
            return partial;
        }

        int split = query.indexOf(":");
        if (split == -1) {
            return SEARCH_FILTER_KEY + partial + TextFormatting.RESET;
        }

        if (displayPos > split) {
            return SEARCH_FILTER_VALUE + partial + TextFormatting.RESET;
        }

        if (displayPos + partial.length() < split) {
            return SEARCH_FILTER_KEY + partial + TextFormatting.RESET;
        }

        split = partial.indexOf(":");
        if (split == -1) {
            return SEARCH_FILTER_KEY + partial + TextFormatting.RESET;
        }

        return SEARCH_FILTER_KEY + partial.substring(0, split + 1) +
                SEARCH_FILTER_VALUE + partial.substring(split + 1) + TextFormatting.RESET;
    }

    private class ModListEntry implements CatalogueListExtended.IGuiListEntry {
        private final IModData data;
        private final ModList list;
        private final PinnedButton button;
        private ItemStack icon;

        public ModListEntry(IModData data, ModList list) {
            this.data = data;
            this.list = list;
            this.button = new PinnedButton(data.getModId());
            this.icon = this.getItemIcon();
        }

        @Override
        public void drawEntry(int index, int left, int top, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean hovered, float partialTicks) {
            // Draws mod name and version
            boolean inOptionsMenu = CatalogueModListScreen.this.menu != null;
            boolean drawFavouriteIcon = !inOptionsMenu && !this.list.shouldHideFavourites() && ClientHelper.isMouseWithin(left + rowWidth - rowHeight - 4, top, rowHeight + 4, rowHeight, mouseX, mouseY) || FAVOURITES.has(this.data.getModId());
            drawString(CatalogueModListScreen.this.fontRenderer, this.getFormattedModName(drawFavouriteIcon), left + 24, top + 2, 0xFFFFFF);
            drawString(CatalogueModListScreen.this.fontRenderer, TextFormatting.GRAY + this.data.getVersion(), left + 24, top + 12, 0xFFFFFF);

            // Draw image icon or fallback to item icon
            this.drawIcon(top, left);

            // Draws an icon if there is an update for the mod
            IModData.Update update = this.data.getUpdate();
            if (update != null) {
                int iconLeft = left + rowWidth - 8 - 9 + (drawFavouriteIcon ? -14 : 0);
                this.data.drawUpdateIcon(mc, update, iconLeft, top + 7);
            }

            if (drawFavouriteIcon) {
                this.button.x = left + rowWidth - this.button.width - 8;
                this.button.y = top + (rowHeight - this.button.height) / 2;
                this.button.drawButton(CatalogueModListScreen.this.mc, mouseX, mouseY, partialTicks);
                if (!inOptionsMenu && this.button.isMouseOver()) {
                    String label = !FAVOURITES.has(this.data.getModId()) ?
                            I18n.format("catalogue.gui.favourite") :
                            I18n.format("catalogue.gui.remove_favourite");
                    CatalogueModListScreen.this.setActiveTooltip(label);
                }
            }
        }

        private void drawIcon(int top, int left) {
            CatalogueModListScreen.this.loadAndCacheIcon(this.data);

            ImageInfo iconInfo = IMAGE_ICON_CACHE.get(this.data.getModId());
            if (iconInfo != null) {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.enableBlend();
                mc.getTextureManager().bindTexture(iconInfo.resource());
                drawScaledCustomSizeModalRect(left + 4, top + 3, 0.0F, 0.0F, iconInfo.width(), iconInfo.height(), 16, 16, iconInfo.width(), iconInfo.height());
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
                CatalogueConstants.LOG.debug("Failed to draw icon for mod '{}'", this.data.getModId(), e);
                ITEM_ICON_CACHE.put(this.data.getModId(), new ItemStack(Blocks.GRASS));
                this.icon = new ItemStack(Blocks.GRASS);
            }
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
            String itemIcon = data.getItemIcon();
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
                } catch (Exception e) {
                    CatalogueConstants.LOG.debug("Failed to get customized item icon for mod '{}'", this.data.getModId(), e);
                }
            }

            // If the mod doesn't specify an item to use, Catalogue will attempt to get an item from the mod
            Optional<ItemStack> optional = ForgeRegistries.ITEMS.getValuesCollection().stream().filter(item -> item.getRegistryName().getNamespace().equals(this.data.getModId())).map(ItemStack::new).findFirst();
            if (optional.isPresent()) {
                ItemStack item = optional.get();
                if (!item.isEmpty()) {
                    // If the item is in a creative tab, Catalogue will use the tab's icon
                    if (item.getItem().getCreativeTab() != null) {
                        try {
                            ItemStack tabItem = item.getItem().getCreativeTab().getIcon();
                            if (tabItem != null && !tabItem.isEmpty() && tabItem.getItem().getRegistryName().getNamespace().equals(this.data.getModId())) {
                                item = tabItem;
                            }
                        } catch (Exception e) {
                            CatalogueConstants.LOG.debug("Failed to get creative tab icon for mod '{}'", this.data.getModId(), e);
                        }
                    }
                    ITEM_ICON_CACHE.put(this.data.getModId(), item);
                    return item;
                }
            }

            return new ItemStack(Blocks.GRASS);
        }

        private String getFormattedModName(boolean favouriteIconVisible) {
            String name = this.data.getDisplayName();
            int paddingEnd = 4;
            int trimWidth = this.list.getListWidth() - 24 - paddingEnd;
            IModData.Update update = this.data.getUpdate();
            if (update != null) {
                trimWidth -= 12;
            }
            if (favouriteIconVisible) {
                trimWidth -= 18;
            }
            if (CatalogueModListScreen.this.fontRenderer.getStringWidth(name) > trimWidth) {
                name = CatalogueModListScreen.this.fontRenderer.trimStringToWidth(name, trimWidth - 8).trim() + "...";
            }
            if (this.data.isLibrary()) {
                return TextFormatting.DARK_GRAY + name;
            }
            return name;
        }

        public IModData getData() {
            return this.data;
        }

        @Override
        public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
            if (mouseEvent == 1) {
                DropdownMenu menu = DropdownMenu.builder(CatalogueModListScreen.this)
                        .setMinItemSize(0, 16)
                        .setAlignment(DropdownMenu.Alignment.BELOW_LEFT)
                        .addItem(I18n.format("catalogue.gui.show_dependencies"), () -> {
                            String filter = "@dependencies:" + this.data.getModId();
                            CatalogueModListScreen.this.searchTextField.setText(filter);
                        })
                        .addItem(I18n.format("catalogue.gui.show_dependents"), () -> {
                            String filter = "@dependents:" + this.data.getModId();
                            CatalogueModListScreen.this.searchTextField.setText(filter);
                        }).build();
                menu.toggle(mouseX, mouseY);
                return true;
            } else if (mouseEvent == 0 && !this.button.mousePressed(CatalogueModListScreen.this.mc, mouseX, mouseY)) {
                CatalogueModListScreen.this.setSelectedModData(this.data);
                this.list.setSelected(this);
                return true;
            }
            return false;
        }

        @Override
        public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {
        }

        @Override
        public void updatePosition(int slotIndex, int x, int y, float partialTicks) {
        }

        private class PinnedButton extends GuiButton {
            private static final ResourceLocation TEXTURE = new ResourceLocation(CatalogueConstants.MOD_ID, "textures/gui/icons.png");

            private final String modId;

            public PinnedButton(String modId) {
                super(0, 0, 0, 10, 10, "");
                this.modId = modId;
            }

            @Override
            public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTick) {
                if (!this.visible) return;
                this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
                this.mouseDragged(mc, mouseX, mouseY);
                int textureU = FAVOURITES.has(this.modId) ? 10 : 0;
                mc.getTextureManager().bindTexture(TEXTURE);
                drawModalRectWithCustomSizedTexture(this.x, this.y, textureU, 10, 10, 10, 64, 64);
            }

            @Override
            public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
                if (super.mousePressed(mc, mouseX, mouseY) && !CatalogueModListScreen.this.modList.hideFavourites) {
                    FAVOURITES.toggle(this.modId);
                    ModListEntry.this.list.filterAndUpdateList();
                    this.playPressSound(mc.getSoundHandler());
                    return true;
                }
                return false;
            }

            @Override
            public boolean isMouseOver() {
                return super.isMouseOver() && CatalogueModListScreen.this.modList.isMouseOver();
            }
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
        int listRight = this.modList.right;
        this.drawVerticalLine(listRight + 11, -1, this.height, 0xFF707070);
        drawRect(listRight + 12, 0, this.width, this.height, 0x66000000);
        this.descriptionList.drawScreen(mouseX, mouseY, partialTicks);

        int contentLeft = listRight + 12 + 10;
        int contentWidth = this.width - contentLeft - 10;

        if (this.selectedModData != null) {
            this.drawBackground(this.width - contentLeft + 10, listRight + 12, 0);

            // Draw mod logo
            this.drawBanner(contentWidth, contentLeft, 10, this.width - (listRight + 12 + 10) - 10, 50);

            // Draw mod name
            GlStateManager.pushMatrix();
            GlStateManager.translate(contentLeft, 70, 0);
            GlStateManager.scale(2.0F, 2.0F, 2.0F);
            drawString(this.fontRenderer, this.selectedModData.getDisplayName(), 0, 0, 0xFFFFFF);
            GlStateManager.popMatrix();

            // Draw mod id
            String modId = TextFormatting.DARK_GRAY + I18n.format("catalogue.gui.mod_id", this.selectedModData.getModId());
            int modIdWidth = this.fontRenderer.getStringWidth(modId);
            drawString(this.fontRenderer, modId, contentLeft + contentWidth - modIdWidth, 92, 0xFFFFFF);

            // Draw version
            String displayVersion = this.selectedModData.getVersion();
            this.drawStringWithLabel("catalogue.gui.version", displayVersion, contentLeft, 92, contentWidth, mouseX, mouseY, TextFormatting.GRAY, TextFormatting.WHITE);

            // Draw inner version if the display version is different from it
            int versionWidth = this.fontRenderer.getStringWidth(I18n.format("catalogue.gui.version", displayVersion));
            String innerVersion = this.selectedModData.getInnerVersion();
            if (!displayVersion.equals(innerVersion) && ClientHelper.isMouseWithin(contentLeft, 92, versionWidth, this.fontRenderer.FONT_HEIGHT, mouseX, mouseY)) {
                this.setActiveTooltip(I18n.format("catalogue.gui.inner_version", innerVersion));
            }

            // Draws an icon if there is an update for the mod
            IModData.Update update = this.selectedModData.getUpdate();
            if (update != null && update.url() != null && !update.url().isBlank()) {
                this.selectedModData.drawUpdateIcon(this.mc, update, contentLeft + versionWidth + 5, 92);
                if (ClientHelper.isMouseWithin(contentLeft + versionWidth + 5, 92, 8, 8, mouseX, mouseY)) {
                    String message = this.selectedModData.getUpdateText(update);
                    this.setActiveTooltip(message);
                }
            }

            // Draw fade from the bottom
            drawGradientRect(listRight + 12, this.height - 50, this.width, this.height, 0x00000000, 0x66000000);

            int labelOffset = this.height - 18;

            // Draw license
            String license = this.selectedModData.getLicense();
            if (!license.isBlank()) {
                this.drawStringWithLabel("catalogue.gui.licenses", license, contentLeft, labelOffset, contentWidth, mouseX, mouseY, TextFormatting.GRAY, TextFormatting.WHITE);
                labelOffset -= 15;
            }

            // Draw credits
            String credits = this.selectedModData.getCredits();
            if (!credits.isBlank()) {
                this.drawStringWithLabel("catalogue.gui.credits", credits, contentLeft, labelOffset, contentWidth, mouseX, mouseY, TextFormatting.GRAY, TextFormatting.WHITE);
                labelOffset -= 15;
            }

            // Draw authors
            String authors = this.selectedModData.getAuthors();
            if (!authors.isBlank()) {
                this.drawStringWithLabel("catalogue.gui.authors", authors, contentLeft, labelOffset, contentWidth, mouseX, mouseY, TextFormatting.GRAY, TextFormatting.WHITE);
            }
        } else {
            String message = TextFormatting.GRAY + I18n.format("catalogue.gui.no_selection");
            drawCenteredString(this.fontRenderer, message, contentLeft + contentWidth / 2, this.height / 2 - 5, 0xFFFFFF);
        }
    }

    private class StringList extends CatalogueListExtended {
        private final List<StringEntry> entries = Lists.newArrayList();

        public StringList(int width, int height, int left, int top) {
            super(CatalogueModListScreen.this.mc, width, height, top, top + height, 10);
            this.setSlotXBoundsFromLeft(left + 8);
            this.visible = false;
        }

        public void setTextFromInfo(IModData data) {
            this.entries.clear();
            this.visible = true;
            if (data.getDescription().trim().isBlank()) {
                this.visible = false;
                return;
            }
            List<String> lines = CatalogueModListScreen.this.fontRenderer.listFormattedStringToWidth(data.getDescription().trim(), this.getListWidth());
            for (String line : lines) {
                this.entries.add(new StringEntry(line.replace("\n", "").replace("\r", "").trim()));
            }
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            ClientHelper.scissor(this.left, this.top, this.width, this.bottom - this.top);
            super.drawScreen(mouseX, mouseY, partialTicks);
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }

        @Override
        protected void drawContainerBackground(@Nullable Tessellator tessellator) {
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
        protected int getRowTop(int pIndex) {
            return super.getRowTop(pIndex) + 4;
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
        protected void overlayBackground(int startY, int endY, int startAlpha, int endAlpha) {
        }
    }

    private class StringEntry implements CatalogueListExtended.IGuiListEntry {
        private final String line;

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
        String colon = ":";
        if (formatted.contains("：")) colon = "：";
        String label = formatted.substring(0, formatted.indexOf(colon) + 1);
        String content = formatted.substring(formatted.indexOf(colon) + 1);
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

    private ImageInfo getBanner(String modId) {
        // Try getting the banner for the mod
        ImageInfo bannerInfo = BANNER_CACHE.get(modId);
        if (bannerInfo != null) return bannerInfo;

        // Try using the icon image for the banner
        ImageInfo iconInfo = IMAGE_ICON_CACHE.get(modId);
        if (iconInfo != null) {
            // Hack to make icon fill max banner height
            int expandedWidth = iconInfo.width() * 10;
            int expandedHeight = iconInfo.height() * 10;
            return new ImageInfo(iconInfo.resource(), expandedWidth, expandedHeight, iconInfo.unregister());
        }

        // Fallback and just use missing banner
        return MISSING_BANNER_INFO;
    }

    private void loadAndCacheLogo(IModData data) {
        if (BANNER_CACHE.containsKey(data.getModId())) return;

        // Fills an empty logo as logo may not be present
        BANNER_CACHE.put(data.getModId(), null);

        // Load the banner resource if present
        Branding.BANNER.loadResource(data).ifPresent(info -> {
            BANNER_CACHE.put(data.getModId(), info);
        });
    }

    private void loadAndCacheIcon(IModData data) {
        if (IMAGE_ICON_CACHE.containsKey(data.getModId())) return;

        // Fills an empty icon as icon may not be present
        IMAGE_ICON_CACHE.put(data.getModId(), null);

        // Load the icon branding
        Branding.ICON.loadResource(data).ifPresentOrElse(info -> {
            IMAGE_ICON_CACHE.put(data.getModId(), info);
        }, () -> {
            // If no icon, try and use the loaded banner if a square
            ImageInfo bannerInfo = BANNER_CACHE.get(data.getModId());
            if (bannerInfo != null) {
                if (bannerInfo.width() == bannerInfo.height()) {
                    IMAGE_ICON_CACHE.put(data.getModId(), bannerInfo);
                }
            } else {
                // Otherwise temporarily load the banner, use if square, otherwise free the resource
                Branding.BANNER.loadResource(data).ifPresent(info -> {
                    if (info.width() == info.height()) {
                        IMAGE_ICON_CACHE.put(data.getModId(), info);
                        BANNER_CACHE.put(data.getModId(), info); // Saves loading later
                    } else {
                        info.unregister().run();
                    }
                });
            }
        });
    }

    private void reloadBackground(IModData data) {
        Branding.BACKGROUND.loadResource(data).ifPresentOrElse(info -> {
            cachedBackground = info;
        }, () -> {
            if (cachedBackground != null) {
                cachedBackground.unregister().run();
                cachedBackground = null;
            }
        });
    }

    /**
     * Draws the background that is visible when a mod is selected. Backgrounds are programmatically
     * faded out to the bottom of the image.
     *
     * @param contentWidth the widget of the content area
     * @param contentLeft  the x position of the content area
     * @param contentTop   the y position of the content area
     */
    @SuppressWarnings("SameParameterValue")
    private void drawBackground(int contentWidth, int contentLeft, int contentTop) {
        if (this.selectedModData == null) return;

        ResourceLocation texture = cachedBackground != null ? cachedBackground.resource() : MISSING_BACKGROUND;
        this.mc.getTextureManager().bindTexture(texture);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBuffer();
        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        builder.pos(contentLeft, contentTop, this.zLevel).tex(0, 0).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        builder.pos(contentLeft, contentTop + 128, this.zLevel).tex(0, 1).color(0.0F, 0.0F, 0.0F, 0.0F).endVertex();
        builder.pos(contentLeft + contentWidth, contentTop + 128, this.zLevel).tex(1, 1).color(0.0F, 0.0F, 0.0F, 0.0F).endVertex();
        builder.pos(contentLeft + contentWidth, contentTop, this.zLevel).tex(1, 0).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        tessellator.draw();

        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.shadeModel(GL11.GL_FLAT);
    }

    @SuppressWarnings("SameParameterValue")
    private void drawBanner(int contentWidth, int x, int y, int maxWidth, int maxHeight) {
        if (this.selectedModData != null) {
            ImageInfo info = this.getBanner(this.selectedModData.getModId());
            int displayWidth = info.width();
            int displayHeight = info.height();
            if (info.width() > maxWidth) {
                displayWidth = maxWidth;
                displayHeight = (displayWidth * info.height()) / info.width();
            }
            if (displayHeight > maxHeight) {
                displayHeight = maxHeight;
                displayWidth = (displayHeight * info.width()) / info.height();
            }

            x += (contentWidth - displayWidth) / 2;
            y += (maxHeight - displayHeight) / 2;

            // Fix for minecraft logo
            if (info.resource() == MINECRAFT_LOGO) {
                y += 8;
            }

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableBlend();
            this.mc.getTextureManager().bindTexture(info.resource());
            drawScaledCustomSizeModalRect(x, y, 0.0F, 0.0F, info.width(), info.height(), displayWidth, displayHeight, info.width(), info.height());

            GlStateManager.disableBlend();
        }
    }

    private void setActiveTooltip(String content) {
        this.activeTooltip = this.fontRenderer.listFormattedStringToWidth(content, 200);
        this.tooltipYOffset = 0;
    }

    private void setActiveTooltip(List<String> activeTooltip) {
        this.activeTooltip = activeTooltip;
        this.tooltipYOffset = 0;
    }

    /**
     * Sets the selected mod data. This handles loading the logo and background, updates the states
     * of widgets, like the config button enable state (if the mod has a config), and the description
     * test.
     *
     * @param data the mod data to set as selected
     */
    private void setSelectedModData(IModData data) {
        this.selectedModData = data;
        this.loadAndCacheLogo(data);
        this.reloadBackground(data);
        this.configButton.visible = true;
        this.websiteButton.visible = true;
        this.issueButton.visible = true;

        this.configButton.enabled = data.hasConfig();
        this.websiteButton.enabled = !data.getHomepage().isBlank();
        this.issueButton.enabled = !data.getIssueTracker().isBlank();

        int contentLeft = this.modList.right + 12 + 10;
        int contentWidth = this.width - contentLeft - 10;
        int labelCount = this.getFooterTextElementCount(data);
        this.descriptionList.setWidth(contentWidth);
        this.descriptionList.setHeight(this.height - 135 - labelCount * 15 - 9);
        this.descriptionList.setSlotXBoundsFromLeft(contentLeft);
        this.descriptionList.setTextFromInfo(data);
        this.descriptionList.setAmountScrolled(0);
    }

    /**
     * Gets the count of the footer text elements. This is used to corrrectly set the height of
     * the description widget.
     *
     * @param data the mod data
     * @return the count of footer text elements
     */
    private int getFooterTextElementCount(IModData data) {
        int count = 0;
        if (!data.getLicense().isBlank()) count++;
        if (!data.getCredits().isBlank()) count++;
        if (!data.getAuthors().isBlank()) count++;
        return count;
    }

    @Override
    public void onGuiClosed() {
        FAVOURITES.save();
    }

    private void updateSelectedModList() {
        ModListEntry selectedEntry = this.modList.getEntryFromInfo(this.selectedModData);
        if (selectedEntry != null) {
            this.modList.setSelected(selectedEntry);
        }
    }

    private void updateSearchFieldSuggestion(String value) {
        if (value.isEmpty()) {
            this.searchTextField.setSuggestion(I18n.format("catalogue.gui.search") + "...");
        } else if (value.startsWith("@")) {
            // Mark as special search
            int end = value.indexOf(":");
            if (end != -1) {
                String type = value.substring(1, end);
                Optional<String> optional = SEARCH_FILTERS.keySet().stream().filter(filter -> {
                    return filter.startsWith(type.toLowerCase(Locale.ENGLISH));
                }).min(Comparator.comparing(String::length));
                if (optional.isPresent()) {
                    int length = type.length();
                    this.searchTextField.setSuggestion(optional.get().substring(length));
                } else {
                    this.searchTextField.setSuggestion("");
                }
            } else {
                this.searchTextField.setSuggestion("");
            }
        } else {
            Optional<IModData> optional = CACHED_MODS.values().stream().filter(data -> {
                return data.getDisplayName().toLowerCase(Locale.ENGLISH).startsWith(value.toLowerCase(Locale.ENGLISH));
            }).min(Comparator.comparing(IModData::getDisplayName));
            if (optional.isPresent()) {
                int length = value.length();
                String displayName = optional.get().getDisplayName();
                this.searchTextField.setSuggestion(displayName.substring(length));
            } else {
                this.searchTextField.setSuggestion("");
            }
        }
    }

    /**
     * Creates a confirmation screen to open a link
     *
     * @param url the url to open
     */
    private void openLink(@Nullable String url) {
        if (url == null) return;
        Style style = new Style().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        this.handleComponentClick(new TextComponentString("").setStyle(style));
    }

    private static boolean isKeyComboCtrlF(int keyID) {
        return keyID == Keyboard.KEY_F && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown();
    }

    private record SearchFilter(BiPredicate<String, IModData> predicate) {
    }

    private static class Favourites {
        private final Set<String> mods = new HashSet<>();
        private boolean needsSave;
        private Path file;

        public void toggle(String modId) {
            if (!this.mods.remove(modId)) {
                this.mods.add(modId);
            }
            this.needsSave = true;
        }

        public boolean has(String modId) {
            return this.mods.contains(modId);
        }

        private void init() {
            try {
                Path configDir = ClientServices.PLATFORM.getConfigDirectory();
                Path file = configDir.resolve("catalogue_favourites.txt");
                if (!Files.exists(file)) {
                    Files.createFile(file);
                }
                this.file = file;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void load() {
            try {
                this.init();
                this.mods.clear();
                Predicate<String> modIdRegex = MOD_ID_PATTERN.asMatchPredicate();
                Files.readAllLines(file).forEach(s -> {
                    if (modIdRegex.test(s) && ClientServices.PLATFORM.isModLoaded(s)) {
                        this.mods.add(s);
                    }
                });
                // Save immediately to remove invalid lines
                this.needsSave = true;
                this.save();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void save() {
            if (!this.needsSave)
                return;

            try {
                this.needsSave = false;
                this.init();
                Files.write(this.file, this.mods, StandardCharsets.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
