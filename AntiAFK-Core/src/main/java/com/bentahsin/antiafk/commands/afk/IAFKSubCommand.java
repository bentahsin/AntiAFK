package com.bentahsin.antiafk.commands.afk;

import org.bukkit.command.CommandSender;
import java.util.List;

/**
 * /afk komutunun bir alt komutunu temsil eden arayüz.
 */
public interface IAFKSubCommand {
    /**
     * Alt komutun adını döndürür. Bu, dahili kayıt için kullanılır.
     * @return Komut adı.
     */
    String getName();

    /**
     * Komutu kullanmak için gereken izni döndürür.
     * @return İzin string'i.
     */
    String getPermission();

    /**
     * Alt komutun mantığını çalıştırır.
     * @param sender Komutu gönderen kişi.
     * @param args Komuta ait argümanlar (örn: AFK sebebi).
     */
    void execute(CommandSender sender, String[] args);

    /**
     * Alt komut için tab tamamlama listesi sağlar.
     * @param sender Komutu gönderen kişi.
     * @param args Alt komuta ait argümanlar.
     * @return Önerilen tamamlama listesi.
     */
    List<String> tabComplete(CommandSender sender, String[] args);
}