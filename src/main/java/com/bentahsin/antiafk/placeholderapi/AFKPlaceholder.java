package com.bentahsin.antiafk.placeholderapi;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.placeholderapi.placeholders.ReasonPlaceholder;
import com.bentahsin.antiafk.placeholderapi.placeholders.TagPlaceholder;
import com.bentahsin.antiafk.placeholderapi.placeholders.TimeAfkPlaceholder;
import com.bentahsin.antiafk.placeholderapi.placeholders.TimeLeftPlaceholder;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * AntiAFK için PlaceholderAPI genişletmesini yöneten ana sınıf.
 * Bu sınıf, gelen istekleri ilgili placeholder sınıflarına yönlendirir.
 */
public class AFKPlaceholder extends PlaceholderExpansion {

    private final AntiAFKPlugin plugin;
    private final Map<String, IPlaceholder> placeholders = new HashMap<>();

    public AFKPlaceholder(AntiAFKPlugin plugin) {
        this.plugin = plugin;
        registerPlaceholders();
    }

    /**
     * Tüm placeholder'ları oluşturur ve kaydeder.
     * Yeni bir placeholder eklemek için tek yapmanız gereken bu metoda eklemektir.
     */
    private void registerPlaceholders() {
        addPlaceholder(new TagPlaceholder(plugin));
        addPlaceholder(new TimeAfkPlaceholder(plugin));
        addPlaceholder(new TimeLeftPlaceholder(plugin));
        addPlaceholder(new ReasonPlaceholder(plugin));
    }

    private void addPlaceholder(IPlaceholder placeholder) {
        this.placeholders.put(placeholder.getIdentifier().toLowerCase(), placeholder);
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "antiafk";
    }

    @Override
    @NotNull
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    @NotNull
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    @Nullable
    public String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        if (offlinePlayer == null) {
            return null;
        }

        IPlaceholder placeholder = placeholders.get(params.toLowerCase());

        if (placeholder != null) {
            return placeholder.getValue(offlinePlayer);
        }

        return null;
    }
}