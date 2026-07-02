package zone.rong.mixinbooter;

import com.google.common.eventbus.EventBus;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;

public class MixinBooterModContainer extends DummyModContainer {

    public MixinBooterModContainer() {
        super(new ModMetadata());
        ModMetadata meta = getMetadata();
        meta.modId = Tags.MOD_ID;
        meta.name = Tags.MOD_NAME;
        meta.description = "A Mixin library and loader.";
        meta.version = Tags.VERSION;
        meta.authorList.add("Rongmario");
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        bus.register(this);
        return true;
    }

}
