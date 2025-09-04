# AntiAFK

**AntiAFK**, modern Minecraft sunucuları için tasarlanmış, AFK (Away From Keyboard) oyuncu yönetimini kapsamlı ve performans odaklı bir yaklaşımla ele alan bir eklentidir. Standart zamanlayıcı tabanlı sistemlerin ötesine geçerek, AFK durumunu atlatmaya yönelik çeşitli pasif ve aktif yöntemleri engellemek için çok katmanlı bir tespit mimarisi kullanır.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java Version](https://img.shields.io/badge/Java-8+-blue.svg)](https://www.java.com)
[![Spigot API](https://img.shields.io/badge/API-Spigot_1.13+-orange.svg)](https://www.spigotmc.org/)

---

## Özellikler

### Tespit Mimarisi
AntiAFK, AFK davranışlarını ve bunları taklit eden botları tespit etmek için dört katmanlı bir analiz uygular:

*   **Katman 1: Temel Aktivite İzleme**
    *   Oyuncu hareketleri, kamera rotasyonu, sohbet, komut kullanımı, envanter etkileşimleri ve dünya ile etkileşimler dahil olmak üzere 10'dan fazla temel eylem türünü izler. Bu eylemler, temel AFK zamanlayıcısını sıfırlar.
*   **Katman 2: Anlamsız Eylem Analizi**
    *   Oyuncunun aynı blok koordinatlarında kalarak tekrarladığı hareket dışı eylemleri (örn. aynı bloğa veya havaya tekrar tekrar tıklama) sayar. Belirlenen eşik aşıldığında, bu eylemlerin AFK zamanlayıcısını sıfırlaması engellenir.
*   **Katman 3: Makro ve İstismar Tespiti**
    *   **Auto-Clicker Analizi:** Tıklamalar arasındaki zaman aralıklarının istatistiksel tutarlılığını analiz ederek insan dışı tıklama modellerini tespit eder.
    *   **Konum Değiştirme İstismarı:** Portallar veya komutlar aracılığıyla yapılan sık ve tekrarlı konum değişikliklerini bir AFK atlatma yöntemi olarak tanımlar.
*   **Katman 4: Asenkron Davranışsal Analiz**
    *   Oyuncuların hareket yörüngelerini asenkron bir görevde sürekli olarak kaydeder ve analiz eder. Geçmiş hareket desenleriyle mevcut yörüngeler arasındaki benzerlikleri karşılaştırarak, önceden belirlenmiş rotalarda hareket eden karmaşık botları tespit eder.

### Ek Koruma Sistemleri

*   **Turing Testi (Captcha Doğrulaması)**
    *   Katman 2 veya Katman 4 tarafından şüpheli olarak işaretlenen oyunculara, insan doğrulamasını gerektiren, yapılandırılabilir bir soru-cevap testi sunulur.
    *   **Şüpheli Mod:** Bir test aktifken, oyuncunun düşük değerli aktiviteleri (hareket, tıklama) yoksayılır, bu da botların testi görmezden gelerek durumu sıfırlamasını engeller.
    *   Başarısızlık durumunda uygulanacak eylemler (örn. `kick`) tamamen yapılandırılabilir.
*   **Kademeli Ceza ve Sabıka Sistemi**
    *   Oyuncuların AFK kaynaklı cezaları, toplam AFK süreleri ve Captcha istatistikleri kalıcı olarak bir **SQLite** veritabanında saklanır.
    *   Tekrar eden AFK davranışları için, ceza sayısına göre artan ciddiyette eylemler (uyarı, kick, geçici ban vb.) yapılandırılabilir.
    *   Belirlenen bir süre boyunca ceza almayan oyuncuların sabıka kayıtları otomatik olarak sıfırlanır.

### Yönetim ve Yapılandırma

*   **GUI Kontrol Paneli:** `/antiafk panel` komutu ile erişilen bir arayüz, tüm eklenti ayarlarının, WorldGuard bölge kurallarının ve online oyuncuların oyun içinden yönetilmesini sağlar.
*   **Yönetici Komutları:**
    *   `/afk check <oyuncu>`: Bir oyuncunun detaylı sabıka kaydını ve mevcut durumunu sorgular.
    *   `/afktop <time|punishments>`: Sunucudaki en yüksek AFK süresine veya ceza sayısına sahip oyuncuları listeler.
    *   `/afklist`: Anlık olarak AFK durumunda olan tüm oyuncuları listeler.
*   **Esnek Yapılandırma:** Tüm metinler, komutlar, anonslar, cezalar ve tespit mekanizmaları `config.yml`, `messages.yml` ve `questions.yml` dosyaları üzerinden detaylı olarak ayarlanabilir.

### 🚀 Performans

AntiAFK, sunucu performansı üzerindeki etkisini minimize etmek için aşağıdaki prensiplerle tasarlanmıştır:

*   **Asenkron Operasyonlar:** Davranış analizi ve tüm veritabanı yazma işlemleri gibi potansiyel olarak yavaş operasyonlar, sunucunun ana iş parçacığı (main thread) dışında çalıştırılır. Bu, disk G/Ç veya yoğun CPU kullanımının sunucu tick hızını (TPS) etkilemesini engeller.
*   **Akıllı Önbellekleme (Caffeine):** Sık erişilen veriler (WorldGuard bölge bilgileri, oyuncu istatistikleri), veritabanı ve API çağrılarını azaltmak için erişim/yazma süresine göre otomatik olarak temizlenen (self-evicting) verimli önbelleklerde tutulur.
*   **Dağıtılmış Yük:** Periyodik görevler (örn. `AFKCheckTask`), işlemlerini bir saniyelik zaman dilimine eşit olarak yayarak, yüksek oyuncu sayılarında sunucuda ani performans düşüşlerinin (spikes) önüne geçer.

#### Performans Profili (Spark Analizi)

Aşağıdaki veriler, eklentinin optimize edilmiş yapısını [Spark Profiler](https://spark.lucko.me/) aracılığıyla doğrulamaktadır.

*Test Ortamı: Paper 1.19.4, 1 online oyuncu, standart sunucu yükü.*

```
// Sunucunun genel boşta kalma oranı (higher is better)
Server thread: 100.00%
└── net.minecraft.server.MinecraftServer.waitUntilNextTick(): 85.37%

// Eklentinin ana iş parçacığı üzerindeki toplam etkisi
AntiAFK (v1.0.2)
└── Server thread: 0.24%
    ├── com.bentahsin.antiafk.tasks.AFKCheckTask.run(): 0.18%
    ├── com.bentahsin.antiafk.listeners.handlers.PlayerMovementListener.onPlayerMove(): 0.04%
    └── Diğer (Komutlar, GUI, Captcha): 0.02%
```

**Analiz:**

*   Sunucunun zamanının **%85'inden fazlasını boşta** geçirmesi, eklentinin genel sunucu sağlığını olumsuz etkilemediğini göstermektedir.
*   Eklentinin tüm bileşenlerinin ana iş parçacığı üzerindeki toplam etkisi, sadece **~0.24%**'tür. Bu, istatistiksel olarak ihmal edilebilir bir değer olup, eklentinin en yoğun sunucularda bile TPS üzerinde fark edilebilir bir etki yaratmayacağını doğrular.
*   En yüksek pasif yüke sahip olan `AFKCheckTask`'ın etkisi (%0.18), oyuncu listesi önbellekleme ve dağıtılmış yük stratejileri sayesinde minimumda tutulmaktadır.

Bu metrikler, AntiAFK'nın performans odaklı tasarımının somut bir kanıtıdır.

---

### Entegrasyonlar
*   **WorldGuard:** Bölge bazında özel AFK süreleri ve ceza eylemleri tanımlanmasını sağlar.
*   **PlaceholderAPI:** Dinamik verileri (`%antiafk_tag%`, `%antiafk_reason%`, vb.) diğer ekenltere sunar.
*   **ProtocolLib (İsteğe Bağlı):** GUI içindeki metin girişi gibi özellikler için daha gelişmiş bir kullanıcı deneyimi sunar.

---

### Kurulum
1.  Son sürümü `releases` bölümünden indirin.
2.  `.jar` dosyasını sunucunuzun `plugins` klasörüne yerleştirin.
3.  (Önerilen) WorldGuard, PlaceholderAPI, ProtocolLib eklentilerini kurun.
4.  Sunucuyu başlatarak varsayılan yapılandırma dosyalarının (`config.yml`, `messages.yml`, `questions.yml`, `playerdata.db`) oluşturulmasını sağlayın.
5.  Yapılandırma dosyalarını ihtiyaçlarınıza göre düzenleyin ve `/antiafk reload` komutunu kullanın.

---

### Katkıda Bulunma
Hata bildirimleri, özellik önerileri ve "pull request"ler memnuniyetle karşılanır. Lütfen bir "issue" açarak tartışmaya başlayın.
