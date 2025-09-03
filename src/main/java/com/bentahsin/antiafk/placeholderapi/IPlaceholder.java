package com.bentahsin.antiafk.placeholderapi;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Her bir placeholder'ın uygulaması gereken arayüz.
 * Bu yapı, her placeholder'ı kendi mantığıyla izole bir birim haline getirir.
 */
public interface IPlaceholder {

    /**
     * Placeholder'ın benzersiz tanımlayıcısını döndürür.
     * Örn: "tag", "time_left" (ana ön ek olan "antiafk_" olmadan)
     * @return Placeholder tanımlayıcısı.
     */
    @NotNull
    String getIdentifier();

    /**
     * Placeholder istendiğinde değeri hesaplayan ve döndüren metot.
     * @param player Değerin hesaplanacağı oyuncu.
     * @return Placeholder'ın işlenmiş metin değeri.
     */
    String getValue(OfflinePlayer player);
}