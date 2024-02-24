package net.minecraftforge.common.config.utils;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.common.util.JsonUtils;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;

public class JsonConfigUtils {
    public static void readFromJson(Configuration configuration, JsonObject jsonObject){
        checkAndCreateCategory(configuration, null, jsonObject);
        String loadedVersion = jsonObject.has(Configuration.CONFIG_VERSION_MARKER) ? jsonObject.get(Configuration.CONFIG_VERSION_MARKER).getAsString() : configuration.getDefinedConfigVersion();

        Multimap<String,String> location2key = HashMultimap.create();
        for(String s:JsonUtils.walkAndGetAllEntryPath(jsonObject,"\\"+Configuration.CATEGORY_SPLITTER)){
            String[] s1=s.split("\\"+Configuration.CATEGORY_SPLITTER);
            String s2=s1[s1.length-1];
            location2key.put(s.substring(0,s.length()-s2.length()-1),s2);
        }
        for(String cateName:location2key.keys()){
            ConfigElement configElement=new ConfigElement(configuration.getCategory(cateName));
            for(IConfigElement iConfigElement:configElement.getChildElements()){
                for(String childName:location2key.get(cateName)){
                    if (childName.equals(iConfigElement.getName())){
                        JsonElement jsonElement = JsonUtils.walkAndGetTheElement(jsonObject,cateName+"."+childName, "\\"+Configuration.CATEGORY_SPLITTER);
                        if (jsonElement.isJsonArray()){
                            iConfigElement.set(JsonUtils.getRawValueArray(jsonElement.getAsJsonArray()));
                        }else iConfigElement.set(JsonUtils.getRawValue(jsonElement));
                    }
                }
            }
        }

        configuration.setLoadedConfigVersion(loadedVersion);
    }
    public static JsonObject writeToJson(Configuration configuration){
        JsonObject jsonObject = new JsonObject();
        for(String path:configuration.getCategories().keySet()){
            ConfigCategory category = configuration.getCategory(path);
            for(String name:category.keySet()){
                Property property = category.get(name);
                if (property.isList()){
                    JsonArray array = new JsonArray();
                    for(String value : property.getStringList()){
                        array.add(new JsonPrimitive(value));
                    }
                    JsonUtils.walkAndSetTheElement(jsonObject, array, path+Configuration.CATEGORY_SPLITTER + name, "\\"+Configuration.CATEGORY_SPLITTER);
                }else {
                    JsonUtils.walkAndSetTheElement(jsonObject, new JsonPrimitive(property.getString()), path+Configuration.CATEGORY_SPLITTER + name, "\\"+Configuration.CATEGORY_SPLITTER);
                }
            }
        }
        return jsonObject;
    }
    public static void checkAndCreateCategory(Configuration configuration, ConfigCategory parent, JsonObject json){
        for(Map.Entry<String,JsonElement> entry:json.entrySet()){
            if (entry.getValue().isJsonObject()){
                String key = entry.getKey().toLowerCase(Locale.ENGLISH);
                String qualifiedName = ConfigCategory.getQualifiedName(key, parent);
                ConfigCategory cat = configuration.getCategories().get(qualifiedName);
                if (cat == null)
                {
                    cat = new ConfigCategory(key, parent);
                    configuration.getCategories().put(qualifiedName, cat);
                }
                checkAndCreateCategory(configuration, cat, entry.getValue().getAsJsonObject());
            }
        }
    }
}
