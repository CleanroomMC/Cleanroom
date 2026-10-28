package zone.rong.mixinbooter.fb;

import com.google.common.eventbus.EventBus;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.asm.FMLSanityChecker;
import zone.rong.mixinbooter.Tags;

import java.io.File;

public class FBModContainer extends DummyModContainer {

    private static ModMetadata metadata() {
        ModMetadata meta = new ModMetadata();
        meta.modId = FBMod.ID;
        meta.name = "FermiumBooter";
        meta.version = FBMod.API_VERSION;
        meta.parent = Tags.MOD_ID;
        return meta;
    }

    public FBModContainer() {
        super(metadata());
    }

    @Override
    public File getSource() {
        return FMLSanityChecker.fmlLocation;
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        return true;
    }

}
