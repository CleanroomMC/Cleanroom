package com.cleanroommc.cleanroom.modpatch;

import com.cleanroommc.cleanroom.modpatch.transformer.ModTransformers;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import org.spongepowered.asm.mixin.edit.MixinEdits;
import top.outlands.foundation.boot.ActualClassLoader;
import top.outlands.foundation.boot.TransformerHolder;
import top.outlands.foundation.function.ExplicitTransformerFunction;

public final class ModPatchTransformer {

    private ModPatchTransformer() {}

    public static byte[] transform(String name, byte[] bytes) {
        if (bytes == null || name.startsWith("com.cleanroommc.cleanroom.")) return bytes;
        if (isJava9OrHigher(bytes)) return bytes;
        bytes = ModTransformers.transform(name, bytes);
        return bytes;
    }

    private static boolean isJava9OrHigher(byte[] bytes) {
        return (short) (((bytes[6] & 0xFF) << 8) | (bytes[7] & 0xFF)) > 52;
    }

    public static void install() {
        if (FMLLaunchHandler.side().isClient()) {
            MixinEdits.of("org.embeddedt.vintagefix.mixin.bugfix.tab_complete_ddos.ChatTabCompleterMixin").cancel();
        }
        TransformerHolder holder = ActualClassLoader.getTransformerHolder();
        ExplicitTransformerFunction previous = holder.runExplicitTransformersFunction;
        holder.runExplicitTransformersFunction = (name, bytes) -> transform(name, previous.apply(name, bytes));
    }
}
