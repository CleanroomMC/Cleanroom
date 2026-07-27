package com.cleanroommc.cleanroom.compute.programs;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import net.minecraft.util.ResourceLocation;
import org.jspecify.annotations.NonNull;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

/**
 * Used to compare the source code with the previous version,
 * to prevent the cache from causing problems when OCL code gets updated.
 * @param cache The cache map
 */
public record ProgramCacheIntegrityTable(Map<String, byte[]> cache) implements Serializable {
    private static final HashFunction hash = Hashing.fingerprint2011();

    public ProgramCacheIntegrityTable() {
        this(new Object2ObjectAVLTreeMap<>());
    }

    public byte[] put(@NonNull ResourceLocation key, @NonNull String value) {
        return cache.put(key.toString(), hash.hashBytes(value.getBytes()).asBytes());
    }

    public boolean contains(@NonNull ResourceLocation key) {
        return cache.containsKey(key.toString());
    }

    public boolean compare(ResourceLocation key, String value) {
        if (!contains(key))
            return false;
        return Arrays.equals(cache.get(key.toString()), hash.hashBytes(value.getBytes()).asBytes());
    }
}
