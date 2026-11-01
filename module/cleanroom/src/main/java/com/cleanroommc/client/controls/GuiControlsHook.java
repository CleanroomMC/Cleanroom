package com.cleanroommc.client.controls;

import it.unimi.dsi.fastutil.ints.IntArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyModifier;

public class GuiControlsHook {

    public static final String TRANSLATE_LAYOUT = "cleanroom.gui.controls.layout";

    private static final IntArrayList maskPriority = IntArrayList.of(
        KeyModifier.MASK_SHIFT, KeyModifier.MASK_CONTROL, KeyModifier.MASK_CONTROL | KeyModifier.MASK_SHIFT,
        KeyModifier.MASK_ALT, KeyModifier.MASK_ALT | KeyModifier.MASK_SHIFT,
        KeyModifier.MASK_ALT | KeyModifier.MASK_CONTROL,
        KeyModifier.MASK_ALT | KeyModifier.MASK_CONTROL | KeyModifier.MASK_SHIFT
    );

    public enum SortMode implements Translate {

        CATEGORY,
        NAME,
        KEY;

        private static final String TRANSLATE_KEY = "cleanroom.gui.controls.sort";
        private static final SortMode[] modes = { CATEGORY, NAME, KEY };

        private final String modeKey = "cleanroom.gui.controls.mode." + this.name().toLowerCase();

        public static int compareKey(KeyBinding bind1, KeyBinding bind2) {
            if ((bind1.getKeyModifiersMask() | bind2.getKeyModifiersMask()) == KeyModifier.MASK_NONE ||
                bind1.getKeyModifiersMask() == bind2.getKeyModifiersMask()) {
                String name1 = bind1.getDisplayName();
                String name2 = bind2.getDisplayName();
                return name1.equals(name2)
                    ? I18n.format(bind1.getKeyDescription()).compareTo(I18n.format(bind2.getKeyDescription()))
                    : (name1.length() == name2.length()
                        ? name1.compareTo(name2)
                        : (name1.length() > name2.length() ? 1 : -1));
            } else if (bind1.getKeyModifiersMask() == KeyModifier.MASK_NONE &&
                bind2.getKeyModifiersMask() != KeyModifier.MASK_NONE) {
                return -1;
            } else if (bind1.getKeyModifiersMask() != KeyModifier.MASK_NONE &&
                bind2.getKeyModifiersMask() == KeyModifier.MASK_NONE) {
                return 1;
            } else {
                return Integer.compare(
                    maskPriority.indexOf(bind1.getKeyModifiersMask()),
                    maskPriority.indexOf(bind2.getKeyModifiersMask())
                );
            }
        }

        public boolean byCategory() {
            return this == CATEGORY;
        }

        public boolean byName() {
            return this == NAME;
        }

        public boolean byKey() {
            return this == KEY;
        }

        public SortMode next(GuiButton button) {
            return modes[(this.ordinal() + 1) % modes.length].updateButton(button);
        }

        public String buttonKey() {
            return TRANSLATE_KEY;
        }

        public String modeKey() {
            return modeKey;
        }

        private SortMode updateButton(GuiButton button) {
            button.displayString = translateText();
            return this;
        }

    }

    public enum FilterMode implements Translate {

        DEFAULT,
        CONFLICT,
        FREE;

        private static final String TRANSLATE_KEY = "cleanroom.gui.controls.filter";
        private static final FilterMode[] modes = { DEFAULT, CONFLICT, FREE };

        private final String modeKey = "cleanroom.gui.controls.mode." + this.name().toLowerCase();

        public FilterMode next(GuiButton button) {
            return modes[(this.ordinal() + 1) % modes.length].updateButton(button);
        }

        public boolean allow(KeyBinding key) {
            return switch (this) {
                case CONFLICT -> {
                    if (key.getKeyCode() != 0) {
                        for (KeyBinding keybinding : Minecraft.getMinecraft().gameSettings.keyBindings) {
                            if (key != keybinding && key.conflicts(keybinding)) {
                                yield true;
                            }
                        }
                    }
                    yield false;
                }
                case FREE -> key.getKeyCode() == 0;
                default -> true;
            };
        }

        public String buttonKey() {
            return TRANSLATE_KEY;
        }

        public String modeKey() {
            return modeKey;
        }

        private FilterMode updateButton(GuiButton button) {
            button.displayString = translateText();
            return this;
        }

    }

    public enum SearchMode implements Translate {

        NAME,
        CATEGORY,
        KEY;

        private static final String TRANSLATE_KEY = "cleanroom.gui.controls.search";
        private static final SearchMode[] modes = { NAME, CATEGORY, KEY };

        private final String modeKey = "cleanroom.gui.controls.mode." + this.name().toLowerCase();

        public SearchMode next(GuiButton button) {
            return modes[(this.ordinal() + 1) % modes.length].updateButton(button);
        }

        public String buttonKey() {
            return TRANSLATE_KEY;
        }

        public String modeKey() {
            return modeKey;
        }

        public boolean allow(KeyBinding key, String searchText) {
            return switch (this) {
                case NAME -> I18n.format(key.getKeyDescription()).toLowerCase().contains(searchText.toLowerCase());
                case CATEGORY -> I18n.format(key.getKeyCategory()).toLowerCase().contains(searchText.toLowerCase());
                case KEY -> key.getDisplayName().toLowerCase().contains(searchText.toLowerCase());
            };
        }

        private SearchMode updateButton(GuiButton button) {
            button.displayString = translateText();
            return this;
        }

    }

    interface Translate {

        String buttonKey();

        String modeKey();

        default String translateText() {
            return I18n.format(this.buttonKey(), I18n.format(this.modeKey()));
        }

    }

}
