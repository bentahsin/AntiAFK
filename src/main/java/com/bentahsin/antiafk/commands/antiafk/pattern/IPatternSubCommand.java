package com.bentahsin.antiafk.commands.antiafk.pattern;

import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * /antiafk pattern komutunun bir alt komutunu (örn: record, list, transform)
 * temsil eden arayüz.
 */
public interface IPatternSubCommand {
    /**
     * Alt komutun adını döndürür (örn: "record").
     */
    String getName();

    /**
     * Alt komutun kullanım formatını açıklayan kısa bir metin.
     */
    String getUsage();

    /**
     * Alt komutu kullanmak için gereken izni döndürür.
     */
    String getPermission();

    /**
     * Alt komutun mantığını çalıştırır.
     * @param sender Komutu gönderen kişi.
     * @param args Bu alt komuta ait argümanlar.
     */
    void execute(CommandSender sender, String[] args);

    /**
     * Alt komut için tab tamamlama listesi sağlar.
     * @param sender Komutu gönderen kişi.
     * @param args Bu alt komuta ait argümanlar.
     * @return Önerilen tamamlama listesi.
     */
    List<String> tabComplete(CommandSender sender, String[] args);
}