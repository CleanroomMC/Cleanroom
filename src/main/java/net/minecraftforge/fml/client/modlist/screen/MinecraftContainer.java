package net.minecraftforge.fml.client.modlist.screen;

import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.ModMetadata;

import java.util.List;

public class MinecraftContainer extends DummyModContainer {
    public MinecraftContainer() {
        super(new ModMetadata());
        ModMetadata meta = this.getMetadata();
        meta.modId = "minecraft";
        meta.name = "Minecraft";
        // Description provided by minecraft.wiki (CC BY-NC-SA 3.0)
        meta.description = "Minecraft is a 3D sandbox adventure game developed by Mojang Studios where players can interact with a fully customizable three-dimensional world made of blocks and entities. Its diverse gameplay options allow players to choose the way they play, creating countless possibilities.";
        meta.version = ForgeVersion.mcVersion;
        meta.authorList = List.of("Mojang AB");
        meta.url = "https://www.minecraft.net";
        meta.issueTrackerUrl = "https://bugs.mojang.com/projects/MC/issues";
        meta.license = "All Rights Reserved";
    }
}
