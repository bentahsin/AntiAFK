package com.bentahsin.antiafk.api;


import com.bentahsin.antiafk.api.action.IAFKAction;
import com.bentahsin.antiafk.api.managers.AIEngineAPI;
import com.bentahsin.antiafk.api.managers.BehaviorAPI;
import com.bentahsin.antiafk.api.managers.PatternAPI;
import com.bentahsin.antiafk.api.managers.TuringAPI;
import com.bentahsin.antiafk.api.models.PlayerAFKStats;
import com.bentahsin.antiafk.api.region.IRegionProvider;
import com.bentahsin.antiafk.api.turing.ICaptcha;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * AntiAFK eklentisi ile etkileşime geçmek için ana API arayüzü.
 * Bu arayüzü kullanarak oyuncuların AFK durumlarını sorgulayabilir veya değiştirebilirsiniz.
 */
@SuppressWarnings("unused")
public interface AntiAFKAPI {

    BehaviorAPI getBehaviorAPI();
    PatternAPI getPatternAPI();
    TuringAPI getTuringAPI();
    AIEngineAPI getAIEngineAPI();

    /**
     * Bir oyuncunun şu anda AFK olup olmadığını kontrol eder.
     * Hem manuel (/afk) hem de otomatik (süre dolumu) AFK durumlarını kapsar.
     *
     * @param player Kontrol edilecek oyuncu.
     * @return Oyuncu AFK ise true, değilse false.
     */
    boolean isAfk(Player player);

    /**
     * Bir oyuncunun kaç saniyedir inaktif olduğunu (hareket etmediğini/konuşmadığını) döndürür.
     *
     * @param player Kontrol edilecek oyuncu.
     * @return Saniye cinsinden inaktiflik süresi.
     */
    long getAfkTime(Player player);

    /**
     * Bir oyuncuyu manuel olarak AFK moduna sokar.
     *
     * @param player AFK yapılacak oyuncu.
     * @param reason AFK sebebi (sohbette görünebilir).
     */
    void setAfk(Player player, String reason);

    /**
     * Bir oyuncunun AFK durumunu kaldırır ve onu tekrar aktif hale getirir.
     *
     * @param player Aktif yapılacak oyuncu.
     */
    void setActive(Player player);

    /**
     * Sisteme yeni bir Captcha türü kaydeder.
     * Bu captcha, config.yml'de ağırlık verilerek kullanılabilir hale gelir.
     * @param captcha Kaydedilecek captcha implementasyonu.
     */
    void registerCaptcha(ICaptcha captcha);

    /**
     * Aktif olan bir Captcha testinin sonucunu sisteme bildirir.
     * Özel Captcha geliştiren eklentiler, test bittiğinde bunu çağırmalıdır.
     *
     * @param player Oyuncu.
     * @param passed Testi geçti mi?
     * @param reason Başarısız olduysa sebebi (Geçtiyse null olabilir).
     */
    void submitCaptchaResult(Player player, boolean passed, String reason);

    /**
     * Bir oyuncuyu AntiAFK kontrollerinden geçici olarak muaf tutar.
     * Örn: Bir minigame başladığında veya oyuncu bir cutscene izlerken.
     *
     * @param player Muaf tutulacak oyuncu.
     * @param pluginName Muafiyeti isteyen eklentinin adı (takip edilebilirlik için).
     */
    void exemptPlayer(Player player, String pluginName);

    /**
     * Oyuncunun üzerindeki geçici muafiyeti kaldırır.
     * Eğer oyuncuyu muaf tutan başka eklentiler varsa, oyuncu muaf kalmaya devam eder.
     * Sadece tüm eklentiler muafiyeti kaldırdığında oyuncu tekrar kontrol edilmeye başlanır.
     *
     * @param player Oyuncu.
     * @param pluginName Muafiyeti kaldıran eklentinin adı.
     */
    void unexemptPlayer(Player player, String pluginName);

    /**
     * Oyuncunun şu anda geçici bir muafiyeti olup olmadığını kontrol eder.
     * @param player Oyuncu.
     * @return Muaf ise true.
     */
    boolean isExempt(Player player);

    /**
     * Bir oyuncunun veritabanındaki istatistiklerini asenkron olarak getirir.
     *
     * @param playerUUID Oyuncunun UUID'si.
     * @return İstatistikleri içeren Future nesnesi.
     */
    CompletableFuture<PlayerAFKStats> getPlayerStats(UUID playerUUID);

    /**
     * Yeni bir bölge sağlayıcısı (Region Provider) kaydeder.
     * AntiAFK, oyuncunun bulunduğu bölgeyi kontrol ederken bu sağlayıcıyı da kullanır.
     *
     * @param provider Kaydedilecek sağlayıcı.
     */
    void registerRegionProvider(IRegionProvider provider);

    /**
     * Sisteme özel bir aksiyon türü kaydeder.
     * Bu sayede config.yml dosyasında 'type: OZEL_AKSIYON' kullanılabilir.
     *
     * @param actionName Config'de kullanılacak isim (Örn: "RPG_MANA_DRAIN").
     * @param action Çalıştırılacak kod bloğu.
     */
    void registerCustomAction(String actionName, IAFKAction action);

    /**
     * Bir oyuncunun şu anki hareketlerinin, belirtilen bot deseniyle
     * ne kadar örtüştüğünü (benzerlik skoru) hesaplar.
     *
     * @param player Analiz edilecek oyuncu.
     * @param patternName Karşılaştırılacak desen adı (dosya adı).
     * @return 0.0 (Benzemiyor) ile 1.0 (Birebir Aynı) arası skor.
     *         Eğer veri yeterli değilse -1 döner.
     */
    double calculateSimilarityScore(Player player, String patternName);

    /**
     * Kayıtlı tüm bot desenlerinin isimlerini döndürür.
     * @return Desen isimleri listesi.
     */
    List<String> getKnownPatternNames();

    /**
     * Oyuncunun şu an sistem tarafından "Şüpheli" (Bot ihtimali yüksek)
     * olarak işaretlenip işaretlenmediğini döndürür.
     * (Captcha çözmesi beklenen durum).
     */
    boolean isSuspicious(Player player);
}