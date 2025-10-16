package com.cleanroommc.kirino.utils;

import net.minecraft.client.resources.AbstractResourcePack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLFolderResourcePack;
import net.minecraftforge.fml.common.Loader;
import org.jspecify.annotations.NonNull;

import java.io.*;
import java.lang.reflect.Field;

public final class MinecraftResourceUtils {
    private static FMLFolderResourcePack devEnvHack() {
        FMLFolderResourcePack resourcePack = new FMLFolderResourcePack(Loader.instance().getIndexedModList().get("forge"));
        try {
            Field field = ReflectionUtils.findDeclaredField(AbstractResourcePack.class, "resourcePackFile", "field_110597_b");
            field.setAccessible(true);
            File current = (File) field.get(resourcePack);
            while (current != null && !current.getName().equals("projects")) {
                current = current.getParentFile();
            }
            File repo = current.getParentFile();
            File assets = new File(repo, "src/main/resources");
            field.set(resourcePack, assets);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
        return resourcePack;
    }

    @NonNull
    public static String readText(ResourceLocation rl, boolean keepNewLineSymbol) {

        InputStream stream;
        try {
            //FMLFolderResourcePack resourcePack = new FMLFolderResourcePack(Loader.instance().getIndexedModList().get(rl.getNamespace()));
            FMLFolderResourcePack resourcePack = devEnvHack();
            stream = resourcePack.getInputStream(rl);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                if (keepNewLineSymbol) {
                    builder.append('\n');
                }
            }
            reader.close();
            return builder.toString();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}
