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

package net.minecraftforge.fml.common.launcher;

import java.io.File;
import java.util.List;

import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;

import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;

public class FMLServerTweaker extends FMLTweaker {

    public FMLServerTweaker()
    {
        Launch.blackboard.put("fml.side", "server");
    }

    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile)
    {
        super.acceptOptions(args, gameDir, assetsDir, profile);

        if (System.getProperty("log4j.configurationFile") == null)
        {
            System.setProperty("log4j.configurationFile", "log4j2_server.xml");
            ((LoggerContext) LogManager.getContext(false)).reconfigure();
        }
    }

    @Override
    public String getLaunchTarget()
    {
        return "net.minecraft.server.MinecraftServer";
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader)
    {
        FMLLaunchHandler.configureForServerLaunch(classLoader, this);
        FMLLaunchHandler.appendCoreMods();
    }
}
