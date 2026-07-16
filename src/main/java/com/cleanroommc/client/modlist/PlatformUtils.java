package com.cleanroommc.client.modlist;

import com.cleanroommc.client.modlist.ModListConstants;
import com.cleanroommc.client.modlist.data.CleanroomModData;
import com.cleanroommc.client.modlist.data.IModData;
import com.cleanroommc.client.modlist.data.OptiFineModData;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.ModMetadata;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class PlatformUtils {
    private PlatformUtils() {
    }

    public static List<IModData> getAllModData() {
        ArrayList<ModContainer> containerList = new ArrayList<>();
        FMLClientHandler.instance().addSpecialModEntries(containerList);
        containerList.addAll(Loader.instance().getModList());

        linkChildMods(containerList);

        List<IModData> dataList = new ArrayList<>(containerList.size());
        for (ModContainer container : containerList) {
            if (container == null) continue;
            if ("optifine".equals(container.getModId())) {
                dataList.add(new OptiFineModData(container));
                continue;
            }
            dataList.add(new CleanroomModData(container));
        }
        return dataList;
    }

    private static void linkChildMods(List<ModContainer> containerList) {
        for (ModContainer container : containerList) {
            if (container == null) continue;
            ModMetadata metadata = container.getMetadata();
            if (metadata != null && metadata.parentMod == null && metadata.parent != null && !metadata.parent.isBlank()) {
                ModContainer parentContainer = Loader.instance().getIndexedModList().get(metadata.parent);
                ModMetadata parentMetadata = parentContainer != null ? parentContainer.getMetadata() : null;
                if (parentMetadata != null) {
                    metadata.parentMod = parentContainer;
                    if (!parentMetadata.childMods.contains(container)) {
                        parentMetadata.childMods.add(container);
                    }
                }
            }
        }
    }

    public static File getModDirectory() {
        return Loader.instance().getConfigDir().getParentFile().toPath().resolve("mods").toFile();
    }

    public static Path getConfigDirectory() {
        return Loader.instance().getConfigDir().toPath();
    }

    public static void openFile(File file) {
        try {
            Class<?> oclass = Class.forName("java.awt.Desktop");
            Object object = oclass.getMethod("getDesktop", new Class[0]).invoke(null);
            oclass.getMethod("open", File.class).invoke(object, file);
        } catch (Exception e) {
            ModListConstants.LOG.error("Failed to open '{}' ", file.getAbsolutePath(), e);
        }
    }

    public static boolean isKeyCombo(int keyCode, int comboKeyCode) {
        return keyCode == comboKeyCode && GuiScreen.isCtrlKeyDown() && !GuiScreen.isShiftKeyDown() && !GuiScreen.isAltKeyDown();
    }
}
