package com.cleanroommc.compute.types;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class OpenCLTypeDeserializer implements JsonDeserializer<OpenCLType> {

    @Override
    public OpenCLType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!json.isJsonPrimitive() || !json.getAsJsonPrimitive().isString()) {
            throw new JsonParseException("OpenCLType must be a string primitive");
        }
        String s = json.getAsString();
        try {
            return parse(s);
        } catch (Exception e) {
            throw new JsonParseException("Failed to parse OpenCLType: " + s, e);
        }
    }

    private OpenCLType parse(String s) {
        if (s.isEmpty()) {
            throw new IllegalArgumentException("Empty OpenCLType string");
        }
        if (s.startsWith("pipe")) {
            return new PipeType(s.substring(5));
        }
        if (s.startsWith("image")) {
            int dimensions = Character.getNumericValue(s.charAt(5));
            String sub = s.substring(7, s.length() - 1);
            boolean array = sub.startsWith("_array");
            boolean fromBuffer = sub.startsWith("_buffer");
            boolean msaa = sub.contains("msaa");
            boolean depth = sub.contains("depth");
            return new ImageType(dimensions, fromBuffer, array, msaa, depth);
        }
        if (s.endsWith("*")) {
            return new BufferType(parse(s.substring(0, s.length() - 1)));
        }

        int i = s.length() - 1;
        while (i >= 0 && Character.isDigit(s.charAt(i))) {
            i--;
        }
        if (i < s.length() - 1 && i >= 0) {
            String primitiveName = s.substring(0, i + 1);
            String lengthStr = s.substring(i + 1);
            for (OpenCLPrimitive p : OpenCLPrimitive.values()) {
                if (p.toString().equals(primitiveName)) {
                    return new VectorType(p, Integer.parseInt(lengthStr));
                }
            }
        }

        for (OpenCLPrimitive p : OpenCLPrimitive.values()) {
            if (p.toString().equals(s)) {
                return p;
            }
        }

        throw new IllegalArgumentException("Unknown OpenCLType: " + s);
    }
}
