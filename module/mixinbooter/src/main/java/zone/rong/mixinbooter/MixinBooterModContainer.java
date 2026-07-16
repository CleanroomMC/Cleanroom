package zone.rong.mixinbooter;

import com.google.common.eventbus.EventBus;
import net.minecraftforge.common.ForgeEarlyConfig;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.MetadataCollection;
import net.minecraftforge.fml.common.ModMetadata;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class MixinBooterModContainer extends DummyModContainer {

    public MixinBooterModContainer() {
        super(loadMetadata());
        ModMetadata meta = getMetadata();
        if (ForgeEarlyConfig.CUSTOM_BUILT_IN_MOD_VERSION) {
            meta.version = ForgeEarlyConfig.MIXIN_BOOTER_VERSION;
        }
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        bus.register(this);
        return true;
    }

    private static ModMetadata loadMetadata() {
        Map<String, Object> fallbackData = new HashMap<>();
        fallbackData.put("name", Tags.MOD_NAME);
        fallbackData.put("version", ForgeEarlyConfig.CUSTOM_BUILT_IN_MOD_VERSION ? ForgeEarlyConfig.MIXIN_BOOTER_VERSION : Tags.VERSION);

        InputStream inputStream = MixinBooterModContainer.class.getClassLoader().getResourceAsStream("mixinbooter.info");
        try {
            return MetadataCollection.from(inputStream, "mixinbooter.info").getMetadataForId(Tags.MOD_ID, fallbackData);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignored) { }
            }
        }
    }

}
