package com.cleanroommc.kirino.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import org.jspecify.annotations.NonNull;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

public class MinecraftResourceUtils {
    @NonNull
    public static String read(String rl, boolean keepNewLineSymbol) {
        return read(new ResourceLocation(rl.toLowerCase(Locale.ROOT)), keepNewLineSymbol);
    }

    @NonNull
    public static String read(ResourceLocation rl, boolean keepNewLineSymbol) {
        InputStream stream = null;
        try {
            IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(rl);
            stream = resource.getInputStream();
        } catch (Exception ignored) {
            return "";
        }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                if (keepNewLineSymbol) {
                    builder.append("\n");
                }
            }
            reader.close();
            return builder.toString();
        } catch (Exception ignored) {
            return "";
        }
    }
}
