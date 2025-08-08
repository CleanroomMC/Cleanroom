package net.minecraftforge.common.util;

import net.minecraft.util.ResourceLocation;

public class ResourceLocationHelper {

    /**
     * Create a resourceLocation under the default namespace.
     * For example :
     *     ResourceLocationHelper.create("minecraft", value)
     *     ResourceLocationHelper.of("minecraft").create(value)
     *     new ResourceLocation(value)
     * is equal
     *
     * Modder could create an instance of ResourceLocationHelper. It is very helpful.
     * And, you could also use the static method at your api.
     *
     * @param defaultNamespace the default namespace
     * @param value the value
     * @return the resource location
     */
    public static ResourceLocation create(String defaultNamespace, String value) {
        int idx = value.indexOf(':');
        return idx > 0 ? new ResourceLocation(value.substring(0, idx), value.substring(idx +1)) : new ResourceLocation(defaultNamespace, value);
    }

    public ResourceLocation create(String value) {
        return create(this.defaultNamespace, value);
    }

    public static ResourceLocationHelper of(String defaultNamespace) {
        return new ResourceLocationHelper(defaultNamespace);
    }
  
    private final String defaultNamespace;

    private ResourceLocationHelper(String defaultNamespace) {
        this.defaultNamespace = defaultNamespace;
    }
}
