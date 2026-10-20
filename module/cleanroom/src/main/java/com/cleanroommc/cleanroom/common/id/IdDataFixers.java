/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.cleanroom.common.id;

import com.cleanroommc.common.CleanroomVersion;

import net.minecraft.block.Block;
import net.minecraft.block.BlockJukebox;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.tileentity.TileEntityFlowerPot;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.IFixableData;
import net.minecraftforge.common.util.CompoundDataFixer;
import net.minecraftforge.common.util.ModFixs;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.VillagerRegistry;

import java.lang.reflect.Field;

/**
 * DataFixer stages and centralized TileEntity/Entity NBT normalizers for Cleanroom Wide IDs,
 * converting legacy Forge numeric IDs to registry names and avoiding scattered per-class patches.
 */
@SuppressWarnings("deprecation")
public final class IdDataFixers {

    public static final int VERSION = 1;

    private static final Field BEACON_PRIMARY;
    private static final Field BEACON_SECONDARY;
    private static final Field PISTON_STATE;
    private static final Field FALLING_TILE;

    static {
        BEACON_PRIMARY = findField(TileEntityBeacon.class, "primaryEffect", "field_146013_m");
        BEACON_SECONDARY = findField(TileEntityBeacon.class, "secondaryEffect", "field_146010_n");
        PISTON_STATE = findField(TileEntityPiston.class, "pistonState", "field_174932_a");
        FALLING_TILE = findField(EntityFallingBlock.class, "fallTile", "field_175132_d");
    }

    private IdDataFixers() { }

    private static Field findField(Class<?> owner, String mcpName, String srgName) {
        try {
            Field f = owner.getDeclaredField(mcpName);
            f.setAccessible(true);
            return f;
        } catch (NoSuchFieldException e) {
            try {
                Field f = owner.getDeclaredField(srgName);
                f.setAccessible(true);
                return f;
            } catch (NoSuchFieldException ex) {
                throw new ExceptionInInitializerError(ex);
            }
        }
    }

    public static void register(CompoundDataFixer fixer) {
        ModFixs modFixs = fixer.init(CleanroomVersion.MOD_ID, VERSION);
        modFixs.registerFix(FixTypes.BLOCK_ENTITY, new IFixableData() {

            @Override
            public int getFixVersion() {
                return VERSION;
            }

            @Override
            public NBTTagCompound fixTagCompound(NBTTagCompound compound) {
                return fixBlockEntityTag(compound);
            }

        });
        modFixs.registerFix(FixTypes.ENTITY, new IFixableData() {

            @Override
            public int getFixVersion() {
                return VERSION;
            }

            @Override
            public NBTTagCompound fixTagCompound(NBTTagCompound compound) {
                return fixEntityTag(compound);
            }

        });
    }

    public static NBTTagCompound fixBlockEntityTag(NBTTagCompound compound) {
        if (compound.hasKey("PrimaryName", 8) || compound.hasKey("Primary", 99)) {
            fixBeaconEffectTag(compound, "PrimaryName", "Primary");
        }
        if (compound.hasKey("SecondaryName", 8) || compound.hasKey("Secondary", 99)) {
            fixBeaconEffectTag(compound, "SecondaryName", "Secondary");
        }
        if (!compound.hasKey("RecordItem", 10) && compound.getInteger("Record") > 0) {
            int recordId = compound.getInteger("Record");
            Item recordItem = LegacyIds.item(recordId);
            if (recordItem != null) {
                compound.setTag("RecordItem", new ItemStack(recordItem).writeToNBT(new NBTTagCompound()));
                compound.removeTag("Record");
            } else {
                ResourceLocation missingRecord = LegacyIds.itemName(recordId);
                if (missingRecord != null) {
                    NBTTagCompound recordTag = new NBTTagCompound();
                    recordTag.setString("id", missingRecord.toString());
                    recordTag.setByte("Count", (byte) 1);
                    recordTag.setShort("Damage", (short) 0);
                    compound.setTag("RecordItem", recordTag);
                    compound.removeTag("Record");
                }
            }
        }
        if (!compound.hasKey("Item", 8) && compound.hasKey("Item", 99)) {
            int legacyItemId = compound.getInteger("Item");
            Item item = LegacyIds.item(legacyItemId);
            if (item != null) {
                ResourceLocation rl = Item.REGISTRY.getNameForObject(item);
                if (rl != null) {
                    compound.setString("Item", rl.toString());
                }
            }
        }
        if (compound.hasKey("blockState", 10)) {
            IBlockState state = NBTUtil.readBlockState(compound.getCompoundTag("blockState"));
            compound.setInteger("blockId", Block.getIdFromBlock(state.getBlock()));
            compound.setInteger("blockData", state.getBlock().getMetaFromState(state));
        } else if (compound.hasKey("blockId", 99)) {
            int legacyBlockId = compound.getInteger("blockId");
            int meta = compound.getInteger("blockData");
            Block block = LegacyIds.block(legacyBlockId);
            if (block != null && block != Blocks.AIR) {
                compound.setInteger("blockId", Block.getIdFromBlock(block));
                compound.setTag("blockState", NBTUtil.writeBlockState(new NBTTagCompound(), block.getStateFromMeta(meta)));
            } else {
                compound.setInteger("blockId", 0);
            }
        }
        return compound;
    }

    public static NBTTagCompound fixEntityTag(NBTTagCompound compound) {
        if (!compound.hasKey("Block", 8) && (compound.hasKey("TileID", 99) || compound.hasKey("Tile", 99))) {
            int legacyId = compound.hasKey("TileID", 99)
                ? compound.getInteger("TileID")
                : (compound.getByte("Tile") & 0xFF);
            Block block = LegacyIds.block(legacyId);
            if (block != null) {
                ResourceLocation rl = Block.REGISTRY.getNameForObject(block);
                if (rl != null) {
                    compound.setString("Block", rl.toString());
                }
            }
        }
        if (!compound.hasKey("carried", 8) && compound.hasKey("carried", 99)) {
            Block block = LegacyIds.block(compound.getShort("carried") & 0xFFFF);
            if (block != null) {
                ResourceLocation rl = Block.REGISTRY.getNameForObject(block);
                if (rl != null) {
                    compound.setString("carried", rl.toString());
                }
            }
        }
        if (!compound.hasKey("DisplayTile", 8) && compound.hasKey("DisplayTile", 99)) {
            Block block = LegacyIds.block(compound.getInteger("DisplayTile"));
            if (block != null) {
                ResourceLocation rl = Block.REGISTRY.getNameForObject(block);
                if (rl != null) {
                    compound.setString("DisplayTile", rl.toString());
                }
            }
        }
        if (!compound.hasKey("inTile", 8) && compound.hasKey("inTile", 99)) {
            Block block = LegacyIds.block(compound.getByte("inTile") & 0xFF);
            if (block != null) {
                ResourceLocation rl = Block.REGISTRY.getNameForObject(block);
                if (rl != null) {
                    compound.setString("inTile", rl.toString());
                }
            }
        }
        if (compound.hasKey("ProfessionName", 8)) {
            VillagerRegistry.VillagerProfession prof = ForgeRegistries.VILLAGER_PROFESSIONS.getValue(
                new ResourceLocation(compound.getString("ProfessionName"))
            );
            if (prof != null) {
                compound.setInteger("Profession", VillagerRegistry.getId(prof));
            }
        }
        return compound;
    }

    private static void fixBeaconEffectTag(NBTTagCompound compound, String nameKey, String idKey) {
        if (compound.hasKey(nameKey, 8)) {
            Potion potion = Potion.REGISTRY.getObject(new ResourceLocation(compound.getString(nameKey)));
            compound.setInteger(idKey, potion == null ? -1 : Potion.getIdFromPotion(potion));
        } else if (compound.hasKey(idKey, 99)) {
            int rawId = compound.getInteger(idKey);
            if (rawId > 0) {
                Potion potion = LegacyIds.potion(rawId);
                compound.setInteger(idKey, potion == null ? -1 : Potion.getIdFromPotion(potion));
                LegacyIds.writePotionName(compound, nameKey, potion);
            }
        }
    }

    public static void beforeTileEntityRead(TileEntity te, NBTTagCompound compound) {
        if (te instanceof TileEntityBeacon ||
            te instanceof TileEntityPiston ||
            te instanceof TileEntityFlowerPot ||
            te instanceof BlockJukebox.TileEntityJukebox) {
            fixBlockEntityTag(compound);
        }
    }

    public static void afterTileEntityWrite(TileEntity te, NBTTagCompound compound) {
        if (te instanceof TileEntityBeacon) {
            try {
                Potion primary = (Potion) BEACON_PRIMARY.get(te);
                Potion secondary = (Potion) BEACON_SECONDARY.get(te);
                LegacyIds.writePotionName(compound, "PrimaryName", primary);
                LegacyIds.writePotionName(compound, "SecondaryName", secondary);
            } catch (IllegalAccessException ignored) {
            }
        } else if (te instanceof TileEntityPiston) {
            try {
                IBlockState state = (IBlockState) PISTON_STATE.get(te);
                if (state != null) {
                    compound.setTag("blockState", NBTUtil.writeBlockState(new NBTTagCompound(), state));
                }
            } catch (IllegalAccessException ignored) {
            }
        }
    }

    public static void beforeEntityRead(Entity entity, NBTTagCompound compound) {
        fixEntityTag(compound);
    }

    public static void afterEntityRead(Entity entity, NBTTagCompound compound) {
        if (entity instanceof EntityFallingBlock && compound.hasKey("BlockState", 10)) {
            try {
                FALLING_TILE.set(entity, NBTUtil.readBlockState(compound.getCompoundTag("BlockState")));
            } catch (IllegalAccessException ignored) {
            }
        } else if (entity instanceof EntityEnderman enderman && compound.hasKey("carriedState", 10)) {
            enderman.setHeldBlockState(NBTUtil.readBlockState(compound.getCompoundTag("carriedState")));
        } else if (entity instanceof EntityMinecart minecart &&
            compound.getBoolean("CustomDisplayTile") &&
            compound.hasKey("DisplayState", 10)) {
            minecart.setDisplayTile(NBTUtil.readBlockState(compound.getCompoundTag("DisplayState")));
        }
    }

    public static void afterEntityWrite(Entity entity, NBTTagCompound compound) {
        if (entity instanceof EntityFallingBlock falling && falling.getBlock() != null) {
            compound.setTag("BlockState", NBTUtil.writeBlockState(new NBTTagCompound(), falling.getBlock()));
        } else if (entity instanceof EntityEnderman enderman && enderman.getHeldBlockState() != null) {
            compound.setTag("carriedState", NBTUtil.writeBlockState(new NBTTagCompound(), enderman.getHeldBlockState()));
        } else if (entity instanceof EntityMinecart minecart && minecart.hasDisplayTile()) {
            compound.setTag("DisplayState", NBTUtil.writeBlockState(new NBTTagCompound(), minecart.getDisplayTile()));
        } else if (entity instanceof EntityVillager villager) {
            ResourceLocation rl = villager.getProfessionForge().getRegistryName();
            if (rl != null) {
                compound.setString("ProfessionName", rl.toString());
            }
        } else if (entity instanceof EntityZombieVillager zombieVillager) {
            ResourceLocation rl = zombieVillager.getForgeProfession().getRegistryName();
            if (rl != null) {
                compound.setString("ProfessionName", rl.toString());
            }
        } else if (entity instanceof EntityArrow && compound.hasKey("inTile", 8)) {
            Block b = Block.getBlockFromName(compound.getString("inTile"));
            if (b != null) {
                compound.setTag("inBlockState",
                    NBTUtil.writeBlockState(new NBTTagCompound(), b.getStateFromMeta(compound.getByte("inData") & 0xFF))
                );
            }
        }
    }

}
