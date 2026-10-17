/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.component;

import com.google.common.base.Optional;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import org.jspecify.annotations.Nullable;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.GameData;
import net.minecraftforge.registries.RegistryManager;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * A set of required component values, removed default components, and/or block/item state properties
 * that can both test an {@link ItemStack} (ingredients, {@code /clear}, advancement predicates) and
 * apply to one ({@code /give}, {@code /replaceitem}, recipe outputs, loot functions).
 */
public final class DataComponentPredicate implements Predicate<ItemStack> {

    public static final DataComponentPredicate EMPTY = new DataComponentPredicate(Collections.emptyMap(),
        Collections.emptySet(), Collections.emptyMap()
    );
    private static final Pattern PROPERTY_NAME = Pattern.compile("^[a-z0-9_]+$");

    private final Map<DataComponentType<?>, Object> expected;
    private final Set<DataComponentType<?>> removed;
    private final Map<String, String> stateProperties;

    private DataComponentPredicate(Map<DataComponentType<?>, Object> expected, Set<DataComponentType<?>> removed,
        Map<String, String> stateProperties
    ) {
        this.expected = expected.isEmpty()
            ? Collections.emptyMap()
            : Collections.unmodifiableMap(new Reference2ObjectArrayMap<>(expected));
        this.removed = removed.isEmpty()
            ? Collections.emptySet()
            : Collections.unmodifiableSet(new ReferenceArraySet<>(removed));
        this.stateProperties = stateProperties.isEmpty()
            ? Collections.emptyMap()
            : Collections.unmodifiableMap(new LinkedHashMap<>(stateProperties));
    }

    public static Builder builder() {
        return new Builder();
    }

    /* Parses a bracketed command component/property list such as [damage=10,!max_damage] or [color=red]. */
    public static DataComponentPredicate parseBracketSpec(String spec) throws ComponentFormatException {
        String s = spec.trim();
        if (s.startsWith("[") && s.endsWith("]")) {
            s = s.substring(1, s.length() - 1).trim();
        }
        if (s.isEmpty()) {
            return EMPTY;
        }
        Builder builder = builder();
        int i = 0;
        while (i < s.length()) {
            int comma = nextTopLevelComma(s, i);
            String token = s.substring(i, comma).trim();
            i = comma + 1;
            if (token.isEmpty()) {
                continue;
            }
            if (token.startsWith("!")) {
                String typeName = token.substring(1).trim();
                DataComponentType<?> type = resolveType(typeName);
                if (type == null) {
                    throw new ComponentFormatException("Unknown data component type: " + typeName);
                }
                builder.without(type);
                continue;
            }
            int eq = token.indexOf('=');
            if (eq <= 0) {
                throw new ComponentFormatException("Expected 'component=value' in '" + token + "'");
            }
            String typeName = token.substring(0, eq).trim();
            String rawValue = token.substring(eq + 1).trim();
            DataComponentType<?> type = resolveType(typeName);
            if (type == null) {
                if (typeName.indexOf(':') < 0 && PROPERTY_NAME.matcher(typeName).matches() && !rawValue.isEmpty()) {
                    builder.withStateProperty(typeName, rawValue);
                    continue;
                }
                throw new ComponentFormatException("Unknown data component type: " + typeName);
            }
            NbtCodec<?> codec = type.nbtCodec();
            if (codec == null) {
                throw new ComponentFormatException("Component type " + type + " has no persistent codec");
            }
            Object decoded = codec.read(parseValueTag(rawValue));
            builder.expectRaw(type, decoded);
        }
        return builder.build();
    }

    public static DataComponentPredicate fromJson(JsonObject json) throws JsonSyntaxException {
        if (json == null || json.size() == 0) {
            return EMPTY;
        }
        Builder builder = builder();
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("!")) {
                String typeName = key.substring(1);
                DataComponentType<?> type = resolveType(typeName);
                if (type == null) {
                    throw new JsonSyntaxException("Unknown data component type '" + typeName + "'");
                }
                builder.without(type);
                continue;
            }
            DataComponentType<?> type = resolveType(key);
            if (type == null) {
                throw new JsonSyntaxException("Unknown data component type '" + key + "'");
            }
            NbtCodec<?> codec = type.nbtCodec();
            if (codec == null) {
                throw new JsonSyntaxException("Component type '" + key + "' has no persistent codec");
            }
            try {
                Object decoded = codec.read(jsonToNbt(entry.getValue()));
                builder.expectRaw(type, decoded);
            } catch (ComponentFormatException e) {
                throw new JsonSyntaxException("Invalid value for component '" + key + "': " + e.getMessage(), e);
            }
        }
        return builder.build();
    }

    @Nullable
    public static DataComponentType<?> resolveType(String rawName) {
        String name = rawName.trim();
        if (name.isEmpty()) {
            return null;
        }
        ForgeRegistry<DataComponentType<?>> reg = RegistryManager.ACTIVE.getRegistry(GameData.DATA_COMPONENT_TYPES);
        if (name.indexOf(':') < 0) {
            ResourceLocation cleanroomKey = new ResourceLocation("cleanroom", name);
            if (reg != null && reg.containsKey(cleanroomKey)) {
                return reg.getValue(cleanroomKey);
            }
            DataComponentType<?> builtin = builtinByName(cleanroomKey);
            if (builtin != null) {
                return builtin;
            }
            ResourceLocation mcKey = new ResourceLocation("minecraft", name);
            if (reg != null && reg.containsKey(mcKey)) {
                return reg.getValue(mcKey);
            }
            return null;
        }
        ResourceLocation key = new ResourceLocation(name);
        if (reg != null && reg.containsKey(key)) {
            return reg.getValue(key);
        }
        return builtinByName(key);
    }

    @Nullable
    private static DataComponentType<?> builtinByName(ResourceLocation key) {
        if (!"cleanroom".equals(key.getNamespace())) {
            return null;
        }
        return switch (key.getPath()) {
            case "damage" -> DataComponents.DAMAGE;
            case "max_damage" -> DataComponents.MAX_DAMAGE;
            case "max_stack_size" -> DataComponents.MAX_STACK_SIZE;
            case "legacy_variant" -> DataComponents.LEGACY_VARIANT;
            case "block_state" -> DataComponents.BLOCK_STATE;
            case "item_state" -> DataComponents.ITEM_STATE;
            case "map_id" -> DataComponents.MAP_ID;
            default -> null;
        };
    }

    private static NBTBase parseValueTag(String raw) throws ComponentFormatException {
        if (raw.isEmpty()) {
            throw new ComponentFormatException("Empty component value");
        }
        if ("true".equalsIgnoreCase(raw) || "false".equalsIgnoreCase(raw)) {
            return new NBTTagByte((byte) (Boolean.parseBoolean(raw) ? 1 : 0));
        }
        try {
            return new NBTTagInt(Integer.parseInt(raw));
        } catch (NumberFormatException ignored) {
            /* try next numeric width */
        }
        try {
            return new NBTTagLong(Long.parseLong(raw));
        } catch (NumberFormatException ignored) {
            /* try next numeric width */
        }
        try {
            return new NBTTagDouble(Double.parseDouble(raw));
        } catch (NumberFormatException ignored) {
            /* try next numeric width */
        }
        if (raw.startsWith("{")) {
            try {
                return JsonToNBT.getTagFromJson(raw);
            } catch (NBTException e) {
                throw new ComponentFormatException("Invalid compound component value: " + e.getMessage(), e);
            }
        }
        if (raw.length() >= 2 && raw.startsWith("\"") && raw.endsWith("\"")) {
            return new NBTTagString(raw.substring(1, raw.length() - 1));
        }
        return new NBTTagString(raw);
    }

    private static NBTBase jsonToNbt(JsonElement element) throws ComponentFormatException {
        if (element == null || element.isJsonNull()) {
            throw new ComponentFormatException("Component value cannot be null");
        }
        if (element.isJsonPrimitive()) {
            JsonPrimitive p = element.getAsJsonPrimitive();
            if (p.isBoolean()) {
                return new NBTTagByte((byte) (p.getAsBoolean() ? 1 : 0));
            }
            if (p.isNumber()) {
                Number n = p.getAsNumber();
                double d = n.doubleValue();
                long l = n.longValue();
                if (d == (double) l) {
                    if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) {
                        return new NBTTagInt((int) l);
                    }
                    return new NBTTagLong(l);
                }
                return new NBTTagDouble(d);
            }
            return new NBTTagString(p.getAsString());
        }
        if (element.isJsonObject()) {
            NBTTagCompound tag = new NBTTagCompound();
            for (Map.Entry<String, JsonElement> e : element.getAsJsonObject().entrySet()) {
                tag.setTag(e.getKey(), jsonToNbt(e.getValue()));
            }
            return tag;
        }
        if (element.isJsonArray()) {
            NBTTagList list = new NBTTagList();
            for (JsonElement child : element.getAsJsonArray()) {
                list.appendTag(jsonToNbt(child));
            }
            return list;
        }
        throw new ComponentFormatException("Unsupported JSON element: " + element);
    }

    private static int nextTopLevelComma(String s, int start) {
        int depth = 0;
        boolean inQuotes = false;
        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"' && (i == 0 || s.charAt(i - 1) != '\\')) {
                inQuotes = !inQuotes;
            } else if (!inQuotes) {
                if (c == '{' || c == '[') {
                    depth++;
                } else if (c == '}' || c == ']') {
                    depth = Math.max(0, depth - 1);
                } else if (c == ',' && depth == 0) {
                    return i;
                }
            }
        }
        return s.length();
    }

    public boolean isEmpty() {
        return expected.isEmpty() && removed.isEmpty() && stateProperties.isEmpty();
    }

    public Map<DataComponentType<?>, Object> expected() {
        return expected;
    }

    public Set<DataComponentType<?>> removed() {
        return removed;
    }

    public Map<String, String> stateProperties() {
        return stateProperties;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public boolean test(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        for (Map.Entry<DataComponentType<?>, Object> entry : expected.entrySet()) {
            DataComponentType<?> type = entry.getKey();
            if (!stack.has(type) || !Objects.equals(stack.get(type), entry.getValue())) {
                return false;
            }
        }
        for (DataComponentType<?> type : removed) {
            if (stack.has(type)) {
                return false;
            }
        }
        for (Map.Entry<String, String> entry : stateProperties.entrySet()) {
            String propName = entry.getKey();
            String propVal = entry.getValue();
            IProperty itemProp = stack.getItem().getItemState().getProperty(propName);
            if (itemProp != null) {
                Comparable current = stack.getItemState().getValue(itemProp);
                if (!propVal.equals(itemProp.getName(current))) {
                    return false;
                }
                continue;
            }
            if (stack.getItem() instanceof ItemBlock ib) {
                IProperty blockProp = ib.getBlock().getBlockState().getProperty(propName);
                IBlockState bs = stack.get(DataComponents.BLOCK_STATE);
                if (blockProp != null && bs != null) {
                    Comparable current = bs.getValue(blockProp);
                    if (!propVal.equals(blockProp.getName(current))) {
                        return false;
                    }
                    continue;
                }
            }
            return false;
        }
        return true;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void applyTo(ItemStack stack) {
        for (Map.Entry<DataComponentType<?>, Object> entry : expected.entrySet()) {
            DataComponentType<?> type = entry.getKey();
            if (type != DataComponents.DAMAGE && type != DataComponents.LEGACY_VARIANT) {
                stack.set((DataComponentType) type, entry.getValue());
            }
        }
        for (DataComponentType<?> type : removed) {
            stack.remove(type);
        }
        for (Map.Entry<DataComponentType<?>, Object> entry : expected.entrySet()) {
            DataComponentType<?> type = entry.getKey();
            if (type == DataComponents.DAMAGE || type == DataComponents.LEGACY_VARIANT) {
                stack.set((DataComponentType) type, entry.getValue());
            }
        }
        for (Map.Entry<String, String> entry : stateProperties.entrySet()) {
            String propName = entry.getKey();
            String propVal = entry.getValue();
            IProperty itemProp = stack.getItem().getItemState().getProperty(propName);
            if (itemProp != null) {
                Optional<?> parsed = itemProp.parseValue(propVal);
                if (!parsed.isPresent()) {
                    throw new IllegalArgumentException("Invalid value '" + propVal + "' for item property '" +
                            propName + "'"
                    );
                }
                stack.setItemState(stack.getItemState().withProperty(itemProp, (Comparable) parsed.get()));
                continue;
            }
            if (stack.getItem() instanceof ItemBlock ib) {
                IProperty blockProp = ib.getBlock().getBlockState().getProperty(propName);
                IBlockState bs = stack.get(DataComponents.BLOCK_STATE);
                if (blockProp != null && bs != null) {
                    Optional<?> parsed = blockProp.parseValue(propVal);
                    if (!parsed.isPresent()) {
                        throw new IllegalArgumentException("Invalid value '" + propVal + "' for block property '" +
                                propName + "'"
                        );
                    }
                    stack.set(DataComponents.BLOCK_STATE, bs.withProperty(blockProp, (Comparable) parsed.get()));
                    continue;
                }
            }
            throw new IllegalArgumentException("Unknown component or state property '" + propName + "' on " +
                    stack.getItem().getRegistryName()
            );
        }
    }

    @Override
    public boolean equals(Object o) {
        return this == o ||
            (o instanceof DataComponentPredicate other &&
                expected.equals(other.expected) &&
                removed.equals(other.removed) &&
                stateProperties.equals(other.stateProperties));
    }

    @Override
    public int hashCode() {
        return Objects.hash(expected, removed, stateProperties);
    }

    public static final class Builder {

        private final Map<DataComponentType<?>, Object> expected = new Reference2ObjectArrayMap<>();
        private final Set<DataComponentType<?>> removed = new ReferenceArraySet<>();
        private final Map<String, String> stateProperties = new LinkedHashMap<>();

        private Builder() { }

        public <T> Builder expect(DataComponentType<T> type, T value) {
            Objects.requireNonNull(type, "type");
            Objects.requireNonNull(value, "value");
            removed.remove(type);
            expected.put(type, value);
            return this;
        }

        Builder expectRaw(DataComponentType<?> type, Object value) {
            Objects.requireNonNull(type, "type");
            Objects.requireNonNull(value, "value");
            removed.remove(type);
            expected.put(type, value);
            return this;
        }

        public Builder without(DataComponentType<?> type) {
            Objects.requireNonNull(type, "type");
            expected.remove(type);
            removed.add(type);
            return this;
        }

        public Builder withStateProperty(String name, String value) {
            stateProperties.put(Objects.requireNonNull(name), Objects.requireNonNull(value));
            return this;
        }

        public DataComponentPredicate build() {
            if (expected.isEmpty() && removed.isEmpty() && stateProperties.isEmpty()) {
                return EMPTY;
            }
            return new DataComponentPredicate(expected, removed, stateProperties);
        }

    }

}
