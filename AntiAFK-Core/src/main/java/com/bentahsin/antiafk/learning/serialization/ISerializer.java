package com.bentahsin.antiafk.learning.serialization;

import com.bentahsin.antiafk.api.learning.Pattern;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Hareket desenlerini (Pattern) diske yazma ve okuma işlemlerini soyutlayan arayüz.
 */
public interface ISerializer {
    /**
     * Bir Pattern nesnesini bir OutputStream'e yazar.
     * @param pattern Kaydedilecek desen.
     * @param outputStream Yazılacak olan stream.
     * @throws IOException Yazma sırasında bir hata oluşursa.
     */
    void serialize(Pattern pattern, OutputStream outputStream) throws IOException;

    /**
     * Bir InputStream'den okuyarak bir Pattern nesnesi oluşturur.
     * @param inputStream Okunacak olan stream.
     * @return Okunan desen.
     * @throws IOException Okuma sırasında bir hata oluşursa.
     */
    Pattern deserialize(InputStream inputStream) throws IOException;

    /**
     * Bu serileştiricinin dosya uzantısını döndürür (örn: "json").
     * @return Dosya uzantısı.
     */
    String getFileExtension();
}