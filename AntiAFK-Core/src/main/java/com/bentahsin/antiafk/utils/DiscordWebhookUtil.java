package com.bentahsin.antiafk.utils;
import com.bentahsin.antiafk.managers.ConfigManager;
import com.bentahsin.antiafk.managers.DebugManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Discord Webhook'larına asenkron olarak mesaj gönderen servis.
 * Bu sınıf Guice tarafından yönetilir ve bir singleton olarak çalışır.
 */
@Singleton
public final class DiscordWebhookUtil {

    private final ConfigManager configManager;
    private final DebugManager debugManager;
    private final Logger logger;

    @Inject
    public DiscordWebhookUtil(ConfigManager configManager, DebugManager debugManager, Logger logger) {
        this.configManager = configManager;
        this.debugManager = debugManager;
        this.logger = logger;
    }

    /**
     * Discord'a bir webhook mesajı gönderir.
     * @param message Gönderilecek mesaj.
     */
    public void sendMessage(String message) {
        boolean isEnabled = configManager.isDiscordWebhookEnabled();
        String webhookUrl = configManager.getDiscordWebhookUrl();
        String botName = configManager.getDiscordBotName();
        String avatarUrl = configManager.getDiscordAvatarUrl();

        boolean isUrlValid = !Objects.requireNonNull(webhookUrl).isEmpty() && !webhookUrl.equals("BURAYA_WEBHOOK_URL'NİZİ_YAPIŞTIRIN");
        debugManager.log(DebugManager.DebugModule.ACTIVITY_LISTENER, "Webhook check started. Enabled: %b, URL Valid: %b", isEnabled, isUrlValid);

        if (!isEnabled || !isUrlValid) {
            debugManager.log(DebugManager.DebugModule.ACTIVITY_LISTENER, "Webhook sending aborted due to invalid config.");
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                String jsonPayload = createJsonPayload(message, botName, avatarUrl);
                debugManager.log(DebugManager.DebugModule.ACTIVITY_LISTENER, "Attempting to send webhook to URL. Payload: %s", jsonPayload);

                URL url = new URL(webhookUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; utf-8");
                connection.setRequestProperty("User-Agent", "AntiAFK-Plugin/1.0");
                connection.setDoOutput(true);
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                debugManager.log(DebugManager.DebugModule.ACTIVITY_LISTENER, "Discord webhook response received. Code: %d", responseCode);

                if (responseCode < 200 || responseCode >= 300) {
                    logger.warning("Discord webhook returned a non-successful status code: " + responseCode);
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                        StringBuilder response = new StringBuilder();
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                        logger.warning("Discord API Error Response: " + response);
                    }
                }

                connection.disconnect();

            } catch (Exception e) {
                logger.log(Level.SEVERE, "Discord webhook mesajı gönderilirken kritik bir hata oluştu:", e);
            }
        });
    }

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