package com.bentahsin.antiafk.utils;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.managers.DebugManager;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Discord Webhook'larına asenkron olarak mesaj gönderen yardımcı sınıf.
 */
public final class DiscordWebhookUtil {

    private DiscordWebhookUtil() {}

    /**
     * Discord'a bir webhook mesajı gönderir.
     * @param plugin Ana eklenti örneği (ayarlar ve loglama için).
     * @param message Gönderilecek mesaj.
     */
    public static void sendMessage(AntiAFKPlugin plugin, String message) {
        String webhookUrl = plugin.getConfig().getString("discord_webhook.webhook_url", "");
        String botName = plugin.getConfig().getString("discord_webhook.bot_name", "AntiAFK Guard");
        String avatarUrl = plugin.getConfig().getString("discord_webhook.avatar_url", "");

        if (!plugin.getConfig().getBoolean("discord_webhook.enabled", false) || Objects.requireNonNull(webhookUrl).isEmpty() || webhookUrl.equals("BURAYA_WEBHOOK_URL'NİZİ_YAPIŞTIRIN")) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                URL url = new URL(webhookUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; utf-8");
                connection.setRequestProperty("User-Agent", "AntiAFK-Plugin");
                connection.setDoOutput(true);

                String jsonPayload = createJsonPayload(message, botName, avatarUrl);

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                plugin.getDebugManager().log(DebugManager.DebugModule.ACTIVITY_LISTENER, "Discord webhook sent. Response code: %d", responseCode);

                connection.disconnect();

            } catch (Exception e) {
                plugin.getLogger().warning("Discord webhook mesajı gönderilirken bir hata oluştu: " + e.getMessage());
            }
        });
    }

    /**
     * Discord Webhook API'sinin beklediği JSON formatını oluşturur.
     */
    private static String createJsonPayload(String content, String username, String avatarUrl) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"content\": \"").append(escapeJson(content)).append("\"");
        if (username != null && !username.isEmpty()) {
            json.append(", \"username\": \"").append(escapeJson(username)).append("\"");
        }
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            json.append(", \"avatar_url\": \"").append(escapeJson(avatarUrl)).append("\"");
        }
        json.append("}");
        return json.toString();
    }

    /**
     * JSON içindeki özel karakterlerden kaçınmak için.
     */
    private static String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}