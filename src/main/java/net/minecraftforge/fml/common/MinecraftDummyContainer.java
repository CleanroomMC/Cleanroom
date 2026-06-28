/*
 * Minecraft Forge
 * Copyright (c) 2016-2020.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.minecraftforge.fml.common;

import java.io.File;
import java.security.cert.Certificate;
import java.util.List;

import com.google.common.eventbus.EventBus;
import net.minecraftforge.fml.common.versioning.VersionParser;
import net.minecraftforge.fml.common.versioning.VersionRange;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nullable;

public class MinecraftDummyContainer extends DummyModContainer
{

    private VersionRange staticRange;
    public MinecraftDummyContainer(String actualMCVersion)
    {
        super(new ModMetadata());
        ModMetadata meta = this.getMetadata();
        meta.modId = "minecraft";
        meta.name = "Minecraft";
        meta.version = actualMCVersion;
        // Description provided by minecraft.wiki (CC BY-NC-SA 3.0)
        meta.description = "Minecraft is a 3D sandbox adventure game developed by Mojang Studios where players can interact with a fully customizable three-dimensional world made of blocks and entities. Its diverse gameplay options allow players to choose the way they play, creating countless possibilities.";
        meta.authorList = List.of("Mojang AB");
        meta.url = "https://www.minecraft.net";
        meta.issueTrackerUrl = "https://bugs.mojang.com/browse/MC";
        meta.license = "All Rights Reserved (https://www.minecraft.net/en-us/eula)";

        staticRange = VersionParser.parseRange("["+actualMCVersion+"]");
    }

    @Override
    public boolean isImmutable()
    {
        return true;
    }

    @Override
    public File getSource()
    {
        return new File("minecraft.jar");
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller)
    {
        return true;
    }

    public VersionRange getStaticVersionRange()
    {
        return staticRange;
    }

    @Override
    @Nullable
    public Certificate getSigningCertificate()
    {
        if (FMLLaunchHandler.side() != Side.CLIENT)
            return null;

        try
        {
            Class<?> cbr = Class.forName("net.minecraft.client.ClientBrandRetriever", false, getClass().getClassLoader());
            Certificate[] certificates = cbr.getProtectionDomain().getCodeSource().getCertificates();
            return certificates != null ? certificates[0] : null;
        }
        catch (Exception ignored){} // Errors don't matter just return null.
        return null;
    }
}
