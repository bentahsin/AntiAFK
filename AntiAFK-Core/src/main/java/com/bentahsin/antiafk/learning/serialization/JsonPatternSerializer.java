package com.bentahsin.antiafk.learning.serialization;

import com.bentahsin.antiafk.learning.Pattern;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class JsonPatternSerializer implements ISerializer {
    private final Gson gson;

    public JsonPatternSerializer() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @Override
    public void serialize(Pattern pattern, OutputStream outputStream) throws IOException {
        try (Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
            gson.toJson(pattern, writer);
        }
    }

    @Override
    public Pattern deserialize(InputStream inputStream) throws IOException {
        try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, Pattern.class);
        }
    }

    @Override
    public String getFileExtension() {
        return "json";
    }
}