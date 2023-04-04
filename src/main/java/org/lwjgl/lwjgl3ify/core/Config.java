package org.lwjgl.lwjgl3ify.core;



import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Config {

    public static final String[] DEFAULT_EXTENSIBLE_ENUMS = new String[] {
            // From EnumHelper
            "net.minecraft.item.EnumAction", "net.minecraft.item.ItemArmor$ArmorMaterial",
            "net.minecraft.entity.item.EntityPainting$EnumArt", "net.minecraft.entity.EnumCreatureAttribute",
            "net.minecraft.entity.EnumCreatureType",
            "net.minecraft.world.gen.structure.StructureStrongholdPieces$Stronghold$Door",
            "net.minecraft.enchantment.EnumEnchantmentType", "net.minecraft.entity.Entity$EnumEntitySize",
            "net.minecraft.block.BlockPressurePlate$Sensitivity",
            "net.minecraft.util.MovingObjectPosition$MovingObjectType", "net.minecraft.world.EnumSkyBlock",
            "net.minecraft.entity.player.EntityPlayer$EnumStatus", "net.minecraft.item.Item$ToolMaterial",
            "net.minecraft.item.EnumRarity",
            //
            "net.minecraftforge.event.terraingen.PopulateChunkEvent$Populate$EventType",
            "net.minecraftforge.event.terraingen.InitMapGenEvent$EventType",
            "net.minecraftforge.event.terraingen.OreGenEvent$GenerateMinable$EventType",
            "net.minecraftforge.event.terraingen.DecorateBiomeEvent$Decorate$EventType",
            // From GTNH crashes
            "vswe.stevesfactory.Localization", "vswe.stevesfactory.blocks.ClusterMethodRegistration",
            "vswe.stevesfactory.blocks.ConnectionBlockType", "vswe.stevesfactory.components.ComponentType",
            "vswe.stevesfactory.components.ConnectionSet", "vswe.stevesfactory.components.ConnectionOption",
            "ic2.core.init.InternalName", "gregtech.api.enums.Element", "gregtech.api.enums.OrePrefixes",
            "net.minecraft.client.audio.MusicTicker$MusicType", "org.bukkit.Material",
            "buildcraft.api.transport.IPipeTile.PipeType", "thaumcraft.common.entities.golems.EnumGolemType", };

    private static final Set<String> EXTENSIBLE_ENUMS = new HashSet<>(Arrays.asList(DEFAULT_EXTENSIBLE_ENUMS));
    private static boolean configLoaded = false;

    public static boolean MIXIN_STBI_TEXTURE_LOADING = true;
    public static boolean MIXIN_STBI_TEXTURE_STICHING = true;
    public static boolean MIXIN_STBI_IGNORE_FASTCRAFT = false;

    public static boolean DEBUG_PRINT_KEY_EVENTS = false;
    public static boolean DEBUG_PRINT_MOUSE_EVENTS = false;

    public static boolean SHOW_JAVA_VERSION = true;
    public static boolean SHOW_LWJGL_VERSION = true;

    public static boolean WINDOW_START_MAXIMIZED = false, WINDOW_START_FOCUSED = true, WINDOW_START_ICONIFIED = false;
    public static boolean WINDOW_DECORATED = true;
    public static boolean OPENGL_DEBUG_CONTEXT = false;
    public static boolean OPENGL_SRGB_CONTEXT = false;
    public static boolean OPENGL_DOUBLEBUFFER = true;
    public static boolean OPENGL_CONTEXT_NO_ERROR = false;

    public static boolean INPUT_INVERT_WHEEL = false;
    public static double INPUT_SCROLL_SPEED = 1.0;

    public static String X11_CLASS_NAME = "minecraft";
    public static String COCOA_FRAME_NAME = "minecraft";



    public static final String CATEGORY_MIXIN = "mixin";
    public static final String CATEGORY_CORE = "core";
    public static final String CATEGORY_DEBUG = "debug";
    public static final String CATEGORY_WINDOW = "window";
    public static final String CATEGORY_INPUT = "input";
    public static final String CATEGORY_GLCONTEXT = "openglContext";


}
