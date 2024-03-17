package net.minecraftforge.event.utils;

import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * ResourceLocationInitEvent is fired whenever a {@link net.minecraft.util.ResourceLocation} is to be init.
 * <br>
 * This event is not {@link net.minecraftforge.fml.common.eventhandler.Cancelable}. <br>
 * <br>
 * This event does not have a result. {@link HasResult}<br>
 * <br>
 * This event is fired on the {@link net.minecraftforge.common.MinecraftForge#EVENT_BUS}.<br>
 **/
public class ResourceLocationInitEvent extends Event {
    private String namespace;
    private String path;
    public ResourceLocationInitEvent(String namespace, String path){
        this.namespace = namespace;
        this.path = path;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
