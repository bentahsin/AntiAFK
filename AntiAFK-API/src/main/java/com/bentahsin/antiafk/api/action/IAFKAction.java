package com.bentahsin.antiafk.api.action;

import org.bukkit.entity.Player;

/**
 * Config dosyasında tanımlanan aksiyonlar dışında,
 * geliştiricilerin kod ile özel aksiyonlar tanımlamasını sağlar.
 * Örn: type: "CUSTOM_MANA_STOP"
 */
@FunctionalInterface
public interface IAFKAction {
    /**
     * Aksiyon tetiklendiğinde çalışır.
     * @param player Hedef oyuncu.
     */
    void execute(Player player);
}