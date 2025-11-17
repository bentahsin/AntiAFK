package com.bentahsin.antiafk.platform;

import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Oyuncudan metin girdisi isteme gibi platforma özgü (Java/Bedrock)
 * uyumluluk işlevlerini soyutlayan arayüz.
 */
public interface IInputCompatibility {

    /**
     * Bir oyuncudan metin girdisi ister. Oyuncunun platformuna göre
     * en uygun yöntemi (Anvil, Sohbet vb.) seçer.
     *
     * @param player      Girdi istenecek oyuncu.
     * @param title       Oyuncuya gösterilecek başlık veya talimat.
     * @param initialText Anvil GUI gibi arayüzler için başlangıç metni.
     * @param onConfirm   Girdi başarıyla alındığında çalıştırılacak fonksiyon.
     * @param onCancel    Oyuncu işlemi iptal ettiğinde çalıştırılacak fonksiyon.
     */
    void promptForInput(Player player, String title, String initialText, Consumer<String> onConfirm, Runnable onCancel);

    /**
     * Başlangıç metni olmayan bir girdi isteme metodu.
     */
    void promptForInput(Player player, String title, Consumer<String> onConfirm, Runnable onCancel);

    /**
     * Bir oyuncunun Bedrock (Geyser) istemcisinden bağlanıp bağlanmadığını kontrol eder.
     * @param playerUUID Kontrol edilecek oyuncunun UUID'si.
     * @return Oyuncu Bedrock'tan bağlıysa true, değilse false.
     */
    boolean isBedrockPlayer(UUID playerUUID);
}