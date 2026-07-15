package com.bentahsin.antiafk.config;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractYamlConfig {

    protected final JavaPlugin plugin;
    private final String fileName;
    private YamlDocument document;

    protected AbstractYamlConfig(JavaPlugin plugin, String fileName) {
        this.plugin = plugin;
        this.fileName = fileName;
    }

    public final void reload() {
        try {
            if (document == null) {
                File file = new File(plugin.getDataFolder(), fileName);
                document = YamlDocument.create(file, plugin.getResource(fileName),
                        generalSettings(), loaderSettings(), dumperSettings(), updaterSettings());
            } else {
                document.reload();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load configuration file '" + fileName + "'", e);
        }
        onLoad();
    }

    public final void save() {
        try {
            document.save();
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save configuration file '" + fileName + "': " + e.getMessage());
        }
    }

    protected abstract void onLoad();

    protected YamlDocument getDocument() {
        return document;
    }

    protected GeneralSettings generalSettings() {
        return GeneralSettings.DEFAULT;
    }

    protected LoaderSettings loaderSettings() {
        return LoaderSettings.builder().setAutoUpdate(true).build();
    }

    protected DumperSettings dumperSettings() {
        return DumperSettings.DEFAULT;
    }

    protected UpdaterSettings updaterSettings() {
        return UpdaterSettings.DEFAULT;
    }

    protected String getString(String path, String def) {
        return document.getString(path, def);
    }

    protected int getInt(String path, int def) {
        return document.getInt(path, def);
    }

    protected long getLong(String path, long def) {
        return document.getLong(path, def);
    }

    protected double getDouble(String path, double def) {
        return document.getDouble(path, def);
    }

    protected boolean getBoolean(String path, boolean def) {
        return document.getBoolean(path, def);
    }

    protected List<String> getStringList(String path) {
        return new ArrayList<>(document.getStringList(path));
    }

    protected Section getSection(String path) {
        return document.getSection(path);
    }

    @SuppressWarnings("unchecked")
    protected List<Map<String, Object>> getMapList(String path) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<?, ?> map : document.getMapList(path)) {
            result.add((Map<String, Object>) map);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    protected List<Map<String, String>> getStringMapList(String path) {
        List<Map<String, String>> result = new ArrayList<>();
        for (Map<?, ?> map : document.getMapList(path)) {
            result.add((Map<String, String>) map);
        }
        return result;
    }
}
