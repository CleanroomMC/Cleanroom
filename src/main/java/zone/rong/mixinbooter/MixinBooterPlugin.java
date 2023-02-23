package zone.rong.mixinbooter;

import com.google.common.eventbus.EventBus;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public final class MixinBooterPlugin implements IFMLLoadingPlugin {

    public static final Logger LOGGER = LogManager.getLogger("MixinBooter");

    public MixinBooterPlugin() {
        LOGGER.info("MixinBootstrap Initializing...");
        MixinBootstrap.init();
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return "zone.rong.mixinbooter.MixinBooterPlugin$Container";
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        Object coremodList = data.get("coremodList");
        if (coremodList instanceof List) {
            for (Object coremod : (List) coremodList) {
                try {
                    Field field = coremod.getClass().getField("coreModInstance");
                    field.setAccessible(true);
                    Object theMod = field.get(coremod);
                    if (theMod instanceof IEarlyMixinLoader loader) {
                        for (String mixinConfig : loader.getMixinConfigs()) {
                            if (loader.shouldMixinConfigQueue(mixinConfig)) {
                                LOGGER.info("Adding {} mixin configuration.", mixinConfig);
                                Mixins.addConfiguration(mixinConfig);
                                loader.onMixinConfigQueued(mixinConfig);
                            }
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("Unexpected error", e);
                }
            }
        }
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    public static class Container extends DummyModContainer {

        public Container() {
            super(new ModMetadata());
            ModMetadata meta = this.getMetadata();
            meta.modId = "mixinbooter";
            meta.name = "MixinBooter";
            meta.description = "A Mixin library and loader (with compat).";
            meta.version = "7.0";
            meta.logoFile = "/icon.png";
            meta.authorList.add("Rongmario");
        }

        @Override
        public boolean registerBus(EventBus bus, LoadController controller) {
            bus.register(this);
            return true;
        }

    }

}
