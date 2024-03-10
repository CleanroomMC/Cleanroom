package net.minecraftforge.fml.common;

import com.google.common.eventbus.EventBus;
import net.minecraftforge.common.ForgeVersion;

public class CleanroomContainer {
    public static class Cleanroom extends DummyModContainer{
        public Cleanroom() {
            super(new ModMetadata());
            ModMetadata meta = this.getMetadata();
            meta.modId = "cleanroom";
            meta.name = "Cleanroom";
            meta.description = "Cleanroom Minecraft.";
            meta.version = ForgeVersion.getVersion();
            meta.authorList.add("CleanroomMC");
        }

        @Override
        public boolean registerBus(EventBus bus, LoadController controller) {
            return true;
        }
    }

    @SuppressWarnings("unused")
    public static class MixinContainer extends DummyModContainer{
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

    public static class ConfigAnytimeContainer extends DummyModContainer{
        public ConfigAnytimeContainer() {
            super(new ModMetadata());
            ModMetadata meta = this.getMetadata();
            meta.modId = "configanytime";
            meta.name = "ConfigAnytime";
            meta.description = "Allows Forge configurations to be setup at any point in time.";
            meta.version = "2.0";
            meta.authorList.add("Rongmario");
        }

        @Override
        public boolean registerBus(EventBus bus, LoadController controller) {
            return true;
        }
    }
}
