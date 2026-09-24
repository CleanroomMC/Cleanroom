# Side annotation strippers for the vanilla Minecraft jar
#
# Format: <class> [<member> [<descriptor>]] (SRG names, '.' or '/' separated)

# Whole Classes
net/minecraft/client/renderer/block/model/ModelResourceLocation
net/minecraft/util/BlockRenderLayer
net/minecraft/util/math/Vec2f

# net.minecraft.block.SoundType
net/minecraft/block/SoundType func_185845_c()Lnet/minecraft/util/SoundEvent;                                     # getBreakSound
net/minecraft/block/SoundType func_185846_f()Lnet/minecraft/util/SoundEvent;                                     # getHitSound

# net.minecraft.entity.EntityLiving
net/minecraft/entity/EntityLiving func_70603_bj()F                                                               # getRenderSizeModifier

# net.minecraft.item.crafting
net/minecraft/item/crafting/IRecipe func_194133_a(II)Z                                                           # canFit
net/minecraft/item/crafting/IRecipe func_193358_e()Ljava/lang/String;                                            # getGroup
net/minecraft/item/crafting/Ingredient func_193365_a()[Lnet/minecraft/item/ItemStack;                            # getMatchingStacks

# net.minecraft.nbt.CompressedStreamTools
net/minecraft/nbt/CompressedStreamTools func_74797_a(Ljava/io/File;)Lnet/minecraft/nbt/NBTTagCompound;            # read
net/minecraft/nbt/CompressedStreamTools func_74793_a(Lnet/minecraft/nbt/NBTTagCompound;Ljava/io/File;)V           # safeWrite
net/minecraft/nbt/CompressedStreamTools func_74795_b(Lnet/minecraft/nbt/NBTTagCompound;Ljava/io/File;)V           # write

# net.minecraft.potion.Potion
net/minecraft/potion/Potion func_76398_f()Z                                                                       # isBadEffect

# net.minecraft.tileentity.TileEntity
net/minecraft/tileentity/TileEntity func_145835_a(DDD)D                                                           # getDistanceSq

# net.minecraft.util.EnumFacing
net/minecraft/util/EnumFacing func_176739_a(Ljava/lang/String;)Lnet/minecraft/util/EnumFacing;                    # byName
net/minecraft/util/EnumFacing func_176730_m()Lnet/minecraft/util/math/Vec3i;                                      # getDirectionVec
net/minecraft/util/EnumFacing func_176737_a(FFF)Lnet/minecraft/util/EnumFacing;                                   # getFacingFromVector
net/minecraft/util/EnumFacing func_176732_a(Lnet/minecraft/util/EnumFacing$Axis;)Lnet/minecraft/util/EnumFacing;  # rotateAround
net/minecraft/util/EnumFacing func_176744_n()Lnet/minecraft/util/EnumFacing;                                      # rotateX
net/minecraft/util/EnumFacing func_176738_p()Lnet/minecraft/util/EnumFacing;                                      # rotateZ
net/minecraft/util/EnumFacing$Axis func_176717_a(Ljava/lang/String;)Lnet/minecraft/util/EnumFacing$Axis;          # byName

# net.minecraft.util.IntIdentityHashBiMap
net/minecraft/util/IntIdentityHashBiMap func_186812_a()V                                                          # clear

# net.minecraft.util.math.Vec3d
net/minecraft/util/math/Vec3d func_72431_c(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;        # crossProduct
net/minecraft/util/math/Vec3d func_189986_a(FF)Lnet/minecraft/util/math/Vec3d;                                    # fromPitchYaw
net/minecraft/util/math/Vec3d func_189984_a(Lnet/minecraft/util/math/Vec2f;)Lnet/minecraft/util/math/Vec3d;       # fromPitchYaw
net/minecraft/util/math/Vec3d func_189985_c()D                                                                    # lengthSquared

# net.minecraft.util.text
net/minecraft/util/text/ITextComponent func_150254_d()Ljava/lang/String;                                          # getFormattedText
net/minecraft/util/text/Style func_150218_j()Ljava/lang/String;                                                   # getFormattingCode

# net.minecraft.world.BossInfo
net/minecraft/world/BossInfo func_186745_a(Lnet/minecraft/world/BossInfo$Color;)V                                 # setColor
net/minecraft/world/BossInfo func_186746_a(Lnet/minecraft/world/BossInfo$Overlay;)V                               # setOverlay

# net.minecraft.world.World
net/minecraft/world/World func_72848_b(Lnet/minecraft/world/IWorldEventListener;)V                                # removeEventListener
