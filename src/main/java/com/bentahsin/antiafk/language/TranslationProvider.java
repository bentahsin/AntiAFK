package com.bentahsin.antiafk.language;

/**
 * Belirli bir dildeki çevirileri sağlamak için bir arayüz.
 * Her desteklenen dil için bu arayüzü uygulayan bir sınıf/enum oluşturulacak.
 */
@FunctionalInterface
public interface TranslationProvider {
    /**
     * Belirtilen anahtar için bu dildeki çeviriyi döndürür.
     * @param key Çeviri anahtarı.
     * @return Çevrilmiş ham metin.
     */
    String get(Lang key);
}