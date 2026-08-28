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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@ForgeTestRunner.Isolated
public class ConfigurationTest {

    private Configuration config;
    private ConfigCategory category;

    @BeforeAll
    public static void setupClass()
    {
        Loader.instance();
        Bootstrap.register();
    }

    @BeforeEach
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

        assertTrue(propertyRenamed, "Property was not renamed");
        assertNull(enabledProperty, "Old property was not removed");
        assertNotNull(defaultEnabledProperty, "New property was not added");
        assertEquals("defaultEnabled", defaultEnabledProperty.getName(), "The property's name was not changed");
        assertEquals("true", defaultEnabledProperty.getString(), "The property's value changed");
        assertEquals(Property.Type.BOOLEAN, defaultEnabledProperty.getType(), "The property's type was changed");
        assertEquals("enabled property comment", defaultEnabledProperty.getComment(), "The property's comment was changed");
    }

    @Test
    public void testRenameProperty_newNameInUse_replaceExistingProperty()
    {
        boolean propertyRenamed = config.renameProperty("defaults", "enabled", "background");

        Property enabledProperty = category.get("enabled");
        Property backgroundProperty = category.get("background");

        assertTrue(propertyRenamed, "Property was not renamed");
        assertNull(enabledProperty, "Old property was not removed");
        assertNotNull(backgroundProperty, "New property was not added");
        assertEquals("background", backgroundProperty.getName(), "The property's name was not changed");
        assertEquals("true", backgroundProperty.getString(), "The property's value changed");
        assertEquals(Property.Type.BOOLEAN, backgroundProperty.getType(), "The property's type was changed");
        assertEquals("enabled property comment", backgroundProperty.getComment(), "The property's comment was changed");
    }
}
