package com.bentahsin.antiafk.managers;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.language.Lang;
import com.bentahsin.antiafk.language.SystemLanguageManager;
import com.bentahsin.antiafk.utils.ChatUtil;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Singleton
public class PlayerLanguageManager {

    private final AntiAFKPlugin plugin;
    private final PluginManager plMgr;
    private final Logger logger;
    private final SystemLanguageManager sysLang;
    private FileConfiguration messagesConfig;
    private String prefix;

    @Inject
    public PlayerLanguageManager(AntiAFKPlugin plugin, SystemLanguageManager sysLang) {
        this.plugin = plugin;
        this.plMgr = plugin.getServer().getPluginManager();
        this.logger = plugin.getLogger();
        this.sysLang = sysLang;
        loadMessages();
    }

    /**
     * Yüklenmiş ve renklendirilmiş plugin önekini (prefix) döndürür.
     *
     * @return Plugin öneki.
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * messages.yml dosyasını yükler veya oluşturur.
     * Kritik bir hata durumunda eklentiyi güvenli bir şekilde devre dışı bırakır.
     */
    public void loadMessages() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        try {
            messagesConfig = new YamlConfiguration();
            messagesConfig.load(messagesFile);

            prefix = ChatUtil.color(messagesConfig.getString("plugin_prefix", "&8[&6AntiAFK&8] &r"));
            logger.info(sysLang.getSystemMessage(Lang.MESSAGES_YML_LOADED_SUCCESSFULLY));

        } catch (IOException | InvalidConfigurationException e) {
            logger.log(Level.SEVERE, sysLang.getSystemMessage(Lang.MESSAGES_YML_LOAD_ERROR), e);
            logger.severe(sysLang.getSystemMessage(Lang.PLUGIN_DISABLE_CRITICAL_ERROR));

            plMgr.disablePlugin(plugin);
        }
    }

    /**
     * Belirtilen yoldan mesajı alır, renk kodlarını uygular ve yer tutucuları değiştirir.
     *
     * @param path          messages.yml içindeki mesajın yolu (örn: "error.no_permission").
     * @param replacements  Çiftler halinde yer tutucular ve değerleri (örn: "%player%", "Notch").
     * @return Biçimlendirilmiş son mesaj.
     */
    public String getMessage(String path, String... replacements) {
        if (messagesConfig == null) {
            return "§cError: messages.yml could not be loaded.";
        }

        String message = messagesConfig.getString(path, "&c<Message not found: " + path + ">");

        assert message != null;
        if (message.startsWith("!")) {
            message = message.substring(1);
        } else {
            message = prefix + message;
        }

        if (replacements.length > 0) {
            for (int i = 0; i < replacements.length; i += 2) {
                if (i + 1 < replacements.length) {
                    message = message.replace(replacements[i], replacements[i + 1]);
                }
            }
        }

        return ChatUtil.color(message);
    }

    /**
     * Bir oyuncuya veya konsola biçimlendirilmiş bir mesaj gönderir.
     *
     * @param sender        Mesajın gönderileceği kişi (Player, ConsoleSender).
     * @param path          messages.yml içindeki mesajın yolu.
     * @param replacements  Yer tutucular ve değerleri.
     */
    public void sendMessage(CommandSender sender, String path, String... replacements) {
        sender.sendMessage(getMessage(path, replacements));
    }

    /**
     * Sunucudaki herkese biçimlendirilmiş bir anons mesajı gönderir.
     *
     * @param path          messages.yml içindeki mesajın yolu.
     * @param replacements  Yer tutucular ve değerleri.
     */
    public void broadcastMessage(String path, String... replacements) {
        Bukkit.broadcastMessage(getMessage(path, replacements));
    }

    /**
     * Belirtilen yoldan bir String listesi alır ve her bir elemanını renklendirir.
     * GUI lore'ları için kullanılır.
     *
     * @param path messages.yml içindeki listenin yolu.
     * @return Renklendirilmiş String listesi.
     */
    public List<String> getMessageList(String path) {
        return messagesConfig.getStringList(path).stream()
                .map(ChatUtil::color)
                .collect(Collectors.toList());
    }

    /**
     * messages.yml'den bir metni, placeholder veya prefix işlemi yapmadan ham haliyle alır.
     * Bu, metin üzerinde manuel işlemler yapmak istediğimizde kullanılır.
     * @param path Mesajın yolu.
     * @return Ham metin string'i veya bulunamazsa null.
     */
    public String getRawMessage(String path) {
        if (messagesConfig == null) {
            return "Error: messages.yml not loaded.";
        }
        return messagesConfig.getString(path);
    }

    /**
     * Önceden tamamen formatlanmış bir mesajı, sadece plugin prefix'ini ekleyerek ve
     * renklendirerek tüm sunucuya anons eder.
     * @param formattedMessage Gönderilmeye hazır mesaj.
     */
    public void broadcastFormattedMessage(String formattedMessage) {
        Bukkit.broadcastMessage(prefix + ChatUtil.color(formattedMessage));
    }
}