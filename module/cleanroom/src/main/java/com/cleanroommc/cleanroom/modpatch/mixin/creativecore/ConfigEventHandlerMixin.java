/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.cleanroom.modpatch.mixin.creativecore;

import com.creativemd.creativecore.common.config.event.ConfigEventHandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Mixin(value = ConfigEventHandler.class, remap = false)
public class ConfigEventHandlerMixin {

    @Redirect(method = "saveClientFields", at = @At(value = "NEW", target = "(Ljava/io/File;)Ljava/io/FileWriter;"))
    private static FileWriter fixCharset(File file) throws IOException {
        return new FileWriter(file, StandardCharsets.UTF_8);
    }

    @Redirect(
        method = "save(Ljava/lang/String;Lnet/minecraftforge/fml/relauncher/Side;)V",
        at = @At(value = "NEW", target = "(Ljava/io/File;)Ljava/io/FileWriter;")
    )
    private static FileWriter fixCharset2(File file) throws IOException {
        return new FileWriter(file, StandardCharsets.UTF_8);
    }

}
