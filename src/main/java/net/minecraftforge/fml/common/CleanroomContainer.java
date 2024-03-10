package net.minecraftforge.fml.common;

public class CleanroomContainer {
    public static class Cleanroom extends DummyModContainer{
        public Cleanroom() {
            super(new ModMetadata());
            ModMetadata meta = this.getMetadata();
            meta.modId = "cleanroom";
            meta.name = "Cleanroom";
            meta.description = "Cleanroom Minecraft.";
            meta.version = ForgeVersion.forge;
            meta.authorList.add("CleanroomMC");
        }

        @Override
        public boolean registerBus(EventBus bus, LoadController controller) {
            return true;
        }
    }

    public MixinContainer() {
        super(new ModMetadata());
        ModMetadata meta = this.getMetadata();
        meta.modId = "mixinbooter";
        meta.name = "MixinBooter";
        meta.description = "A Mixin library and loader.";
        meta.version = "10.0";
        meta.authorList.add("Rongmario");
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        return true;
    }
}
