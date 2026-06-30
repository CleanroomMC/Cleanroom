package zone.rong.mixinbooter.util;

import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.objectweb.asm.commons.Remapper;
import org.spongepowered.asm.obfuscation.mapping.remap.Unmapper;

public class Srg2NotchRemapper extends Remapper implements Unmapper {

    @Override
    public String map(String typeName) {
        return FMLDeobfuscatingRemapper.INSTANCE.map(typeName);
    }

    @Override
    public String mapMethodName(String owner, String name, String desc) {
        return FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(owner, name, desc);
    }

    @Override
    public String mapFieldName(String owner, String name, String desc) {
        return FMLDeobfuscatingRemapper.INSTANCE.mapFieldName(owner, name, desc);
    }

    @Override
    public String unmap(String typeName) {
        return FMLDeobfuscatingRemapper.INSTANCE.unmap(typeName);
    }

}
