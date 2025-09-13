package com.bentahsin.antiafk.utils;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.language.Lang;
import com.bentahsin.antiafk.language.SystemLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.logging.Level;

/**
 * Komutlarla ilgili genel yardımcı metotları içeren bir utility sınıfı.
 */
public final class CommandUtil {

    /**
     * Bu sınıfın bir örneğinin oluşturulmasını engellemek için private constructor.
     */
    private CommandUtil() {}

    /**
     * Bir komutu sunucunun komut haritasından dinamik olarak kaldırır.
     * Bu, diğer eklentilerin aynı komut adını kullanabilmesi için gereklidir.
     * @param plugin Loglama ve dil yönetimi için ana eklenti örneği.
     * @param commandName Kaldırılacak komutun adı (alias olmadan).
     */
    public static void unregister(AntiAFKPlugin plugin, String commandName) {
        SystemLanguageManager sysLang = plugin.getSystemLanguageManager();
        try {
            // Sunucunun komut haritasına (commandMap) erişmek için reflection kullanıyoruz.
            Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            bukkitCommandMap.setAccessible(true);
            CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());

            // Komut haritasının içindeki 'knownCommands' adlı asıl listeye erişiyoruz.
            Field knownCommandsField = commandMap.getClass().getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Command> knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);

            // Komutu ve tüm takma adlarını (alias) haritadan kaldırıyoruz.
            Command command = knownCommands.remove(commandName.toLowerCase());
            if (command != null) {
                for (String alias : command.getAliases()) {
                    knownCommands.remove(alias.toLowerCase());
                }
                plugin.getLogger().info(sysLang.getSystemMessage(Lang.COMMAND_UNREGISTERED_SUCCESS, commandName));
            }

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, sysLang.getSystemMessage(Lang.COMMAND_UNREGISTER_ERROR, commandName), e);
        }
    }
}