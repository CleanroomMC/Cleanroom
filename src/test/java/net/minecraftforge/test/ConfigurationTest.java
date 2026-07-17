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

package net.minecraftforge.test;


import net.minecraft.init.Bootstrap;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeTestRunner;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.*;

@RunWith(ForgeTestRunner.class)
public class ConfigurationTest {

    private Configuration config;
    private ConfigCategory category;

    @BeforeClass
    public static void setupClass()
            throws Exception
    {
        Loader.instance();
        Bootstrap.register();
        Field minecraftHome = net.minecraftforge.fml.relauncher.FMLInjectionData.class.getDeclaredField("minecraftHome");
        minecraftHome.setAccessible(true);
        minecraftHome.set(null, new File("."));
    }

    @Before
    public void setup()
    {
        Property enabledProperty = new Property("enabled", "true", Property.Type.BOOLEAN);
        enabledProperty.setComment("enabled property comment");

        Property backgroundProperty = new Property("background", "0xFFFFFF", Property.Type.COLOR);
        backgroundProperty.setComment("background property comment");

        config = new Configuration();
        category = config.getCategory("defaults");
        category.put(enabledProperty.getName(), enabledProperty);
        category.put(backgroundProperty.getName(), backgroundProperty);
    }

    @Test
    public void testRenameProperty_newNameNotInUse()
    {
        boolean propertyRenamed = config.renameProperty("defaults", "enabled", "defaultEnabled");

        Property enabledProperty = category.get("enabled");
        Property defaultEnabledProperty = category.get("defaultEnabled");

        assertTrue("Property was not renamed", propertyRenamed);
        assertNull("Old property was not removed", enabledProperty);
        assertNotNull("New property was not added", defaultEnabledProperty);
        assertEquals("The property's name was not changed", "defaultEnabled", defaultEnabledProperty.getName());
        assertEquals("The property's value changed", "true", defaultEnabledProperty.getString());
        assertEquals("The property's type was changed", Property.Type.BOOLEAN, defaultEnabledProperty.getType());
        assertEquals("The property's comment was changed", "enabled property comment", defaultEnabledProperty.getComment());
    }

    @Test
    public void testRenameProperty_newNameInUse_replaceExistingProperty()
    {
        boolean propertyRenamed = config.renameProperty("defaults", "enabled", "background");

        Property enabledProperty = category.get("enabled");
        Property backgroundProperty = category.get("background");

        assertTrue("Property was not renamed", propertyRenamed);
        assertNull("Old property was not removed", enabledProperty);
        assertNotNull("New property was not added", backgroundProperty);
        assertEquals("The property's name was not changed", "background", backgroundProperty.getName());
        assertEquals("The property's value changed", "true", backgroundProperty.getString());
        assertEquals("The property's type was changed", Property.Type.BOOLEAN, backgroundProperty.getType());
        assertEquals("The property's comment was changed", "enabled property comment", backgroundProperty.getComment());
    }

    @Test
    public void testStringListMultilineRoundTrip() throws Exception
    {
        File file = File.createTempFile("cleanroom-config", ".cfg");
        file.deleteOnExit();
        String source = "defaults {\n"
                + "    S:values <\n"
                + "        first line\\\n"
                + "        second line\n"
                + "        ordinary\n"
                + "        \\>\n"
                + "        literal " + "\\" + ">\n"
                + "        literal " + "\\\\" + ">\n"
                + "        \\\\>\n"
                + "     >\n"
                + "}\n";
        Files.write(file.toPath(), source.getBytes(StandardCharsets.UTF_8));

        Configuration loaded = new Configuration(file);
        assertArrayEquals(new String[] {
                "first line\nsecond line",
                "ordinary",
                ">",
                "literal \\>",
                "literal \\\\>",
                "\\>"
        }, loaded.getCategory("defaults").get("values").getStringList());

        loaded.save();
        String saved = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        assertTrue(saved.contains("first line\\"));

        Configuration reloaded = new Configuration(file);
        assertArrayEquals(loaded.getCategory("defaults").get("values").getStringList(),
                reloaded.getCategory("defaults").get("values").getStringList());
    }

    @Test
    public void testStringListMultilinePreservesTrailingNewlineAndBackslashes() throws Exception
    {
        File file = File.createTempFile("cleanroom-config", ".cfg");
        file.deleteOnExit();
        Configuration written = new Configuration(file);
        Property values = new Property("values", new String[] {
                "ends with slash " + "\\" + "\nnext",
                "ends with slash " + "\\",
                "ends with two slashes " + "\\\\",
                "ends with newline\n"
        }, Property.Type.STRING);
        written.getCategory("defaults").put(values.getName(), values);
        written.save();

        Configuration reloaded = new Configuration(file);
        assertArrayEquals(values.getStringList(), reloaded.getCategory("defaults").get("values").getStringList());
    }
}
