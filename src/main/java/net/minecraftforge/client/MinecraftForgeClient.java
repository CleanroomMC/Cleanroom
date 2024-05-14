/*
 * Minecraft Forge
 * Copyright (c) 2016-2020.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.minecraftforge.client;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;


public class MinecraftForgeClient
{
    public static int getRenderPass()
    {
        return ForgeHooksClient.renderPass;
    }
    
    public static BlockRenderLayer getRenderLayer()
    {
        return ForgeHooksClient.renderLayer.get();
    }

    public static Locale getLocale()
    {
        return Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getJavaLocale();
    }

    private static BitSet stencilBits = new BitSet(8);
    static
    {
        stencilBits.set(0,8);
    }

    public static int reserveStencilBit()
    {
        int bit = stencilBits.nextSetBit(0);
        if (bit >= 0)
        {
            stencilBits.clear(bit);
        }
        return bit;
    }

    public static void releaseStencilBit(int bit)
    {
        if (bit >= 0 && bit < stencilBits.length())
        {
            stencilBits.set(bit);
        }
    }

    private static final LoadingCache<Pair<World, BlockPos>, ChunkCache> regionCache = CacheBuilder.newBuilder()
        .maximumSize(500)
        .concurrencyLevel(5)
        .expireAfterAccess(1, TimeUnit.SECONDS)
        .build(new CacheLoader<Pair<World, BlockPos>, ChunkCache>()
        {
            @Override
            public ChunkCache load(Pair<World, BlockPos> key)
            {
                return new ChunkCache(key.getLeft(), key.getRight().add(-1, -1, -1), key.getRight().add(16, 16, 16), 1);
            }
        });

    public static void onRebuildChunk(World world, BlockPos position, ChunkCache cache)
    {
        regionCache.put(Pair.of(world, position), cache);
    }

    public static ChunkCache getRegionRenderCache(World world, BlockPos pos)
    {
        int x = pos.getX() & ~0xF;
        int y = pos.getY() & ~0xF;
        int z = pos.getZ() & ~0xF;
        return regionCache.getUnchecked(Pair.of(world, new BlockPos(x, y, z)));
    }

    public static void clearRenderCache()
    {
        regionCache.invalidateAll();
        regionCache.cleanUp();
    }

    private static HashMap<ResourceLocation, Supplier<BufferedImage>> bufferedImageSuppliers = new HashMap<ResourceLocation, Supplier<BufferedImage>>();
    public static void registerImageLayerSupplier(ResourceLocation resourceLocation, Supplier<BufferedImage> supplier)
    {
        bufferedImageSuppliers.put(resourceLocation, supplier);
    }
    
    /**
     * Returns a BufferedImage for the specified resource location.
     * If a supplier has been registered for the resource location, it will be used to create the BufferedImage.
     * Otherwise, the BufferedImage will be loaded from the resource manager.
     *
     * @param resourceLocation the resource location
     * @param resourceManager the resource manager
     * @return the BufferedImage for the specified resource location
     * @throws IOException if an I/O error occurs
     */
    @Nonnull
    public static BufferedImage getImageLayer(ResourceLocation resourceLocation, IResourceManager resourceManager) throws IOException
    {
        Supplier<BufferedImage> supplier = bufferedImageSuppliers.get(resourceLocation);
        if (supplier != null)
            return supplier.get();

        try (IResource iresource1 = resourceManager.getResource(resourceLocation))
        {
            return TextureUtil.readBufferedImage(iresource1.getInputStream());
        }
    }
}
