package fermiumbooter.util;

import zone.rong.mixinbooter.service.ModDiscoverer;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public abstract class FermiumJarScanner {

    // Some mods read this reflectively despite it being internal
    static final Map<String, ModInfo> presentMods = new HashMap<String, ModInfo>() {

        @Override
        public int size() {
            return ModDiscoverer.getPresentMods().size();
        }

        @Override
        public boolean isEmpty() {
            return ModDiscoverer.getPresentMods().isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return key instanceof String && ModDiscoverer.isModPresent((String) key);
        }

        @Override
        public ModInfo get(Object key) {
            if (!this.containsKey(key)) {
                return null;
            }
            String modId = (String) key;
            return new ModInfo(modId, ModDiscoverer.getModVersion(modId), ModDiscoverer.getModName(modId));
        }

        @Override
        public ModInfo getOrDefault(Object key, ModInfo defaultValue) {
            ModInfo info = this.get(key);
            return info == null ? defaultValue : info;
        }

        @Override
        public Set<String> keySet() {
            return ModDiscoverer.getPresentMods();
        }

        @Override
        public Collection<ModInfo> values() {
            return this.keySet().stream().map(this::get).collect(Collectors.toList());
        }

        @Override
        public Set<Map.Entry<String, ModInfo>> entrySet() {
            return this.keySet().stream()
                    .map(modId -> new AbstractMap.SimpleImmutableEntry<>(modId, this.get(modId)))
                    .collect(Collectors.toSet());
        }

        @Override
        public void forEach(BiConsumer<? super String, ? super ModInfo> action) {
            this.keySet().forEach(modId -> action.accept(modId, this.get(modId)));
        }

    };

    public static boolean isModPresent(String modID) {
        return ModDiscoverer.isModPresent(modID);
    }

}
