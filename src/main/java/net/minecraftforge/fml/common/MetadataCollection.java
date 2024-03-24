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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.google.gson.JsonObject;
import net.minecraftforge.fml.common.versioning.ArtifactVersion;
import net.minecraftforge.fml.common.versioning.VersionParser;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import javax.annotation.Nullable;

public class MetadataCollection
{
    private String modListVersion;
    private ModMetadata[] modList;
    private Map<String, ModMetadata> metadatas = Maps.newHashMap();

    public static MetadataCollection from(@Nullable InputStream inputStream, String sourceName)
    {
        if (inputStream == null)
        {
            return new MetadataCollection();
        }

        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        try
        {
            MetadataCollection collection;
            JsonElement rootElement = JsonParser.parseReader(reader);
            if (rootElement.isJsonArray())
            {
                collection = new MetadataCollection();
                JsonArray jsonList = rootElement.getAsJsonArray();
                collection.modList = new ModMetadata[jsonList.size()];
                int i = 0;
                for (JsonElement mod : jsonList)
                {
                    collection.modList[i++] = decodeMetaData(mod.getAsJsonObject());
                }
            }
            else
            {
                JsonObject jsonObject = rootElement.getAsJsonObject();
                collection = new MetadataCollection();
                collection.modListVersion = jsonObject.get("modListVersion").getAsString(); // This field is never used.
                JsonArray modListArray = jsonObject.get("modList").getAsJsonArray();
                int size = modListArray.size();
                ModMetadata[] modList = new ModMetadata[size];
                for(int i = 0; i < size; i++){
                    modList[i] = decodeMetaData(modListArray.get(i).getAsJsonObject());
                }
                collection.modList = modList;
                // do not decode for `metadatas`
            }
            collection.parseModMetadataList();
            return collection;
        }
        catch (JsonParseException e)
        {
            FMLLog.log.error("The mcmod.info file in {} cannot be parsed as valid JSON. It will be ignored", sourceName, e);
            return new MetadataCollection();
        }
    }

    private void parseModMetadataList()
    {
        for (ModMetadata modMetadata : modList)
        {
            metadatas.put(modMetadata.modId, modMetadata);
        }
    }

    public ModMetadata getMetadataForId(String modId, Map<String, Object> extraData)
    {
        if (!metadatas.containsKey(modId))
        {
            ModMetadata dummy = new ModMetadata();
            dummy.modId = modId;
            dummy.name = (String) extraData.get("name");
            dummy.version = (String) extraData.get("version");
            dummy.autogenerated = true;
            metadatas.put(modId, dummy);
        }
        return metadatas.get(modId);
    }

    @Deprecated
    public static class ArtifactVersionAdapter extends TypeAdapter<ArtifactVersion>
    {

        @Override
        public void write(JsonWriter out, ArtifactVersion value) throws IOException
        {
            // no op - we never write these out
        }

        @Override
        public ArtifactVersion read(JsonReader in) throws IOException
        {
            return VersionParser.parseVersionReference(in.nextString());
        }

    }

    public static ModMetadata decodeMetaData(JsonObject json){
        ModMetadata metadata = new ModMetadata();

        //basic message
        metadata.modId = json.get("modid").getAsString();
        metadata.name = json.get("name").getAsString();

        //optional message
        if (json.has("description")) metadata.description = json.get("description").getAsString();
        if (json.has("credits")) metadata.credits = json.get("credits").getAsString();
        if (json.has("url")) metadata.url = json.get("url").getAsString();
        if (json.has("updateJSON")) metadata.updateJSON = json.get("updateJSON").getAsString();
        if (json.has("logoFile")) metadata.logoFile = json.get("logoFile").getAsString();
        if (json.has("version")) metadata.version = json.get("version").getAsString();
        if (json.has("parent")) metadata.parent = json.get("parent").getAsString();
        if (json.has("useDependencyInformation")) metadata.useDependencyInformation = json.get("useDependencyInformation").getAsBoolean();
        if (metadata.useDependencyInformation){
            if (json.has("requiredMods")){
                for(JsonElement element : json.getAsJsonArray("requiredMods")){
                    metadata.requiredMods.add(VersionParser.parseVersionReference(element.getAsString()));
                }
            }
            if (json.has("dependencies")){
                for(JsonElement element : json.getAsJsonArray("dependencies")){
                    metadata.dependencies.add(VersionParser.parseVersionReference(element.getAsString()));
                }
            }
            if (json.has("dependants")){
                for(JsonElement element : json.getAsJsonArray("dependants")){
                    metadata.dependants.add(VersionParser.parseVersionReference(element.getAsString()));
                }
            }
        }
        if (json.has("authorList")){
            for(JsonElement element : json.getAsJsonArray("authorList")){
                metadata.authorList.add(element.getAsString());
            }
        }
        if (json.has("screenshots")){ // this field was never used
            JsonArray array = json.getAsJsonArray("screenshots");
            int size = array.size();
            String[] screenshots = new String[size];
            for (int i = 0; i < size; i++){
                screenshots[i] = array.get(i).getAsString();
            }
            metadata.screenshots = screenshots;
        }else metadata.screenshots = new String[0];
        if (json.has("updateUrl")){ // this field is out of date
            metadata.updateUrl = json.get("updateUrl").getAsString();
            FMLLog.log.warn("{} is using a deprecated field 'updateUrl' in mcmod.info. Never really used for anything and format is undefined. See updateJSON for replacement.", metadata.modId);
        }

        return metadata;
    }
}
