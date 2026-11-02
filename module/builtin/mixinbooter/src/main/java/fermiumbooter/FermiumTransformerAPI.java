package fermiumbooter;

import org.spongepowered.asm.service.MixinService;
import zone.rong.mixinbooter.Tags;

// Blocking other transformers is not supported, calls are for ABI compatibility only
public abstract class FermiumTransformerAPI {

    public static void blockTransformer(String name) {
        MixinService.getService().getLogger(Tags.MOD_NAME)
                .warn("Ignoring request to block transformer {}, transformer blocking is not supported.", name);
    }

    public static void blockTransformerFromClasses(String name, String... classes) {
        blockTransformer(name);
    }

}
