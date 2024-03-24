package net.minecraftforge.event.items;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * This event is fired on the {@link net.minecraftforge.common.MinecraftForge#EVENT_BUS}.
 **/
public class CreativeTabEvent extends Event {
    private final CreativeTabs tab;
    public CreativeTabEvent(CreativeTabs tabIn){
        this.tab = tabIn;
    }

    private static class SubItems extends CreativeTabEvent{
        private NonNullList<ItemStack> subItems;

        public SubItems(CreativeTabs tabIn, NonNullList<ItemStack> subItemsIn) {
            super(tabIn);
            this.subItems = subItemsIn;
        }

        public NonNullList<ItemStack> getSubItems() {
            return subItems;
        }

        public void add(ItemStack stack){
            subItems.add(stack);
        }
    }

    /**
     * This event is {@link Cancelable}.<br>
     * <br>
     * This event does not have a result. {@link HasResult}<br>
     * **/
    @Cancelable
    public static class PreSubItems extends SubItems{
        public PreSubItems(CreativeTabs tabIn, NonNullList<ItemStack> subItemsIn) {
            super(tabIn, subItemsIn);
        }
    }

    /**
     * this event will not be fired if {@link PreSubItems} is canceled.
     * <br>
     * This event is not {@link Cancelable}.<br>
     * <br>
     * This event does not have a result. {@link HasResult}<br>
     * **/
    public static class PostSubItems extends SubItems{
        public PostSubItems(CreativeTabs tabIn, NonNullList<ItemStack> subItemsIn) {
            super(tabIn, subItemsIn);
        }
    }
}
