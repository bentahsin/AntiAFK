package com.bentahsin.antiafk.api.region;

import org.bukkit.Location;
import java.util.List;

/**
 * AntiAFK'ya özel bölge desteği eklemek için uygulanması gereken arayüz.
 * Örn: Lands, GriefPrevention veya özel zindan eklentileri için.
 */
public interface IRegionProvider {

    /**
     * Sağlayıcının benzersiz adı (örn: "Lands", "GriefPrevention").
     * Debug loglarında görünür.
     * @return Sağlayıcı adı.
     */
    String getName();

    /**
     * Verilen lokasyondaki aktif bölge isimlerini döndürür.
     * AntiAFK bu isimleri config.yml'deki 'region_overrides' bölümüyle eşleştirir.
     *
     * @param location Kontrol edilecek lokasyon.
     * @return Bölge isimleri listesi (boş olabilir, null olmamalı).
     */
    List<String> getRegionNames(Location location);
}