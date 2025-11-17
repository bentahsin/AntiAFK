package com.bentahsin.antiafk.commands.antiafk;

import org.bukkit.command.CommandSender;
import java.util.List;

/**
 * AntiAFK komutunun bir alt komutunu temsil eden arayüz.
 * Her alt komut (reload, panel vb.) bu arayüzü uygulamalıdır.
 */
public interface ISubCommand {
    /**
     * Alt komutun adını döndürür (örn: "reload").
     * @return Komut adı.
     */
    String getName();

    /**
     * Komutu kullanmak için gereken izni döndürür.
     * @return İzin string'i veya izin gerekmiyorsa null.
     */
    String getPermission();

    /**
     * Alt komutun mantığını çalıştırır.
     * @param sender Komutu gönderen kişi.
     * @param args Alt komuta ait argümanlar.
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