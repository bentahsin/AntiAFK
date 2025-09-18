<p align="center">
  <img src="https://i.imgur.com/QK50QUt.png" alt="AntiAFK Başlık" width="550"/>
</p>

<p align="center">
  <a href="https://opensource.org/licenses/MIT"><img src="https://img.shields.io/badge/License-MIT-yellow.svg" alt="License: MIT"></a>
  <a href="https://www.java.com"><img src="https://img.shields.io/badge/Java-8+-blue.svg" alt="Java Version"></a>
  <a href="https://www.spigotmc.org/"><img src="https://img.shields.io/badge/API-Spigot_1.13+-orange.svg" alt="Spigot API"></a>
</p>

# AntiAFK

**AntiAFK**, modern Minecraft sunucuları için tasarlanmış, yeni nesil bir AFK (Away From Keyboard) yönetim aracıdır. Standart zamanlayıcıların yetersiz kaldığı durumlarda, en karmaşık botları ve AFK durumunu atlatmaya yönelik istismar girişimlerini proaktif olarak engellemek için çok katmanlı, sezgisel bir tespit mimarisi kullanır. Sunucu performansını ilk sıraya koyan tasarımıyla, en yoğun sunucularda bile fark edilmeden çalışır.

---

## ✨ Ana Özellikler

### Çok Katmanlı Tespit Mimarisi
AntiAFK, AFK davranışlarını ve bunları taklit eden botları avlamak için dört katmanlı bir analiz uygular:

* **Katman 1: Temel Aktivite İzleme**
    * Oyuncu hareketleri, kamera rotasyonu, sohbet, komut kullanımı, envanter etkileşimleri ve dünya ile etkileşimler dahil olmak üzere 10'dan fazla temel eylem türünü izler. Bu eylemler, temel AFK zamanlayıcısını sıfırlar.
* **Katman 2: Anlamsız Eylem Analizi**
    * Oyuncunun aynı blok koordinatlarında kalarak tekrarladığı hareket dışı eylemleri (örn. aynı bloğa veya havaya tekrar tekrar tıklama, aynı yerde sürekli zıplama/eğilme) sayar. Belirlenen eşik aşıldığında, bu eylemlerin AFK zamanlayıcısını sıfırlaması engellenir.
    * <img src="https://i.imgur.com/MdHmYiN.gif" alt="Anlamsız Eylem Tespiti" width="400"/> 
    * <img src="https://i.imgur.com/1gkH9CF.gif" alt="Anlamsız Eylem Tespiti 2" width="400"/>
* **Katman 3: Makro ve İstismar Tespiti**
    * **Auto-Clicker Analizi:** Tıklamalar arasındaki zaman aralıklarının istatistiksel tutarlılığını analiz ederek insan dışı tıklama modellerini tespit eder.
    * **Konum Değiştirme İstismarı:** Portallar veya komutlar aracılığıyla yapılan sık ve tekrarlı konum değişikliklerini bir AFK atlatma yöntemi olarak tanımlar.
* **Katman 4: Asenkron Davranışsal Analiz**
    * Oyuncuların hareket yörüngelerini asenkron bir görevde sürekli olarak kaydeder ve analiz eder. Geçmiş hareket desenleriyle mevcut yörüngeler arasındaki benzerlikleri karşılaştırarak, önceden belirlenmiş rotalarda hareket eden karmaşık botları tespit eder.
    * <img src="https://i.imgur.com/JfUVizh.gif" alt="Yörüngesel Analiz" width="400"/> 
    * <img src="https://i.imgur.com/FeHWuth.gif" alt="Yörüngesel Analiz 2" width="400"/>

### 🧠 "Learning Modu": Akıllı Rota Deseni Tanıma
Bu sistem, önceden tanımlanmış hareket desenlerini veya rotaları takip eden gelişmiş AFK botlarını tespit etmek için makine öğrenmesi prensiplerinden yararlanır. Sunucu yöneticileri, şüpheli bir botun hareketlerini kaydederek sunucuya özel savunma desenleri oluşturabilir.

* **Asenkron Desen Analizi:** Bir eşleşme, farklı hızlarda gerçekleştirilseler bile iki hareket dizisini etkili bir şekilde karşılaştıran **Dinamik Zaman Bükme (DTW)** algoritması kullanılarak belirlenir. Bu yoğun işlem, sunucu performansını etkilememek için ana iş parçacığı (main thread) dışında çalışır.
* **Yüksek Performanslı Tasarım:** Bellek kullanımını optimize etmek için nesne havuzu (`Apache Commons Pool2`) ve verimli veri serileştirme (`Kryo`, `Gson`) gibi endüstri standardı kütüphaneler kullanılır.
* **Kapsamlı Desen Yönetimi:** `/antiafk pattern` komut seti ile yöneticiler yeni desenleri kaydedebilir, listeleyebilir, silebilir ve formatlar arasında dönüştürebilir.

### Ek Koruma ve Yönetim Sistemleri

* **Turing Testi (Captcha Doğrulaması)**
    * Şüpheli olarak işaretlenen oyunculara, insan doğrulamasını gerektiren, yapılandırılabilir bir soru-cevap testi sunulur.
    * **Şüpheli Mod:** Bir test aktifken, oyuncunun düşük değerli aktiviteleri (hareket, tıklama) yoksayılır, bu da botların testi görmezden gelerek durumu sıfırlamasını engeller.
* **Kademeli Ceza ve Sabıka Sistemi**
    * Oyuncuların AFK kaynaklı cezaları, toplam AFK süreleri ve Captcha istatistikleri kalıcı olarak bir **SQLite** veritabanında saklanır.
    * Tekrar eden AFK davranışları için, ceza sayısına göre artan ciddiyette eylemler (uyarı, kick, geçici ban vb.) yapılandırılabilir.
* **GUI Kontrol Paneli**
    * `/antiafk panel` komutu ile erişilen arayüz, tüm eklenti ayarlarının, WorldGuard bölge kurallarının ve online oyuncuların oyun içinden yönetilmesini sağlar.
    * <img src="https://i.imgur.com/QHWL44L.gif" alt="GUI Panel" width="600"/>
* **Yönetici Komutları**
    * `/antiafk check <oyuncu>`: Bir oyuncunun detaylı sabıka kaydını sorgular.
    * `/antiafk top <time|punishments>`: AFK liderlik tablolarını gösterir.
    * `/antiafk list`: Anlık AFK oyuncuları listeler.
    * `/antiafk reload`: Yapılandırma dosyalarını yeniden yükler.

---

### 🚀 Performans
AntiAFK, sunucu performansı üzerindeki etkisini minimize etmek için aşağıdaki prensiplerle tasarlanmıştır:

* **Asenkron Operasyonlar:** Davranış analizi, desen tanıma ve tüm veritabanı işlemleri gibi potansiyel olarak yavaş operasyonlar, sunucunun ana iş parçacığı (main thread) dışında çalıştırılır.
* **Akıllı Önbellekleme (Caffeine):** Sık erişilen veriler (WorldGuard bölge bilgileri, oyuncu istatistikleri), veritabanı ve API çağrılarını azaltmak için verimli önbelleklerde tutulur.
* **Dağıtılmış Yük:** Periyodik görevler, işlemlerini bir saniyelik zaman dilimine eşit olarak yayarak, yüksek oyuncu sayılarında sunucuda ani performans düşüşlerinin (spikes) önüne geçer.

#### Performans Profili (Spark Analizi)
*Test Ortamı: Paper 1.19.4, 1 online oyuncu, standart sunucu yükü.*

~~~
// Sunucunun genel boşta kalma oranı (daha yüksek daha iyi)
Server thread: 100.00%
└── net.minecraft.server.MinecraftServer.waitUntilNextTick(): 85.37%

// Eklentinin ana iş parçacığı üzerindeki toplam etkisi
AntiAFK (v1.0.2)
└── Server thread: 0.24%
    ├── com.bentahsin.antiafk.tasks.AFKCheckTask.run(): 0.18%
    ├── com.bentahsin.antiafk.listeners.handlers.PlayerMovementListener.onPlayerMove(): 0.04%
    └── Diğer (Komutlar, GUI, Captcha): 0.02%
~~~
**Analiz:** Eklentinin tüm bileşenlerinin ana iş parçacığı üzerindeki toplam etkisi, sadece **~0.24%**'tür. Bu, istatistiksel olarak ihmal edilebilir bir değer olup, eklentinin en yoğun sunucularda bile TPS üzerinde fark edilebilir bir etki yaratmayacağını doğrular.

---

### 🛠️ Gelişmiş Hata Ayıklama (Debug)
AntiAFK, sunucu sahiplerine tam kontrol sağlayan modüler bir hata ayıklama sistemi sunar. `config.yml` üzerinden yalnızca ilgilendiğiniz modülün loglarını açarak konsol kirliliğini önleyebilir ve sorunları hedefe yönelik bir şekilde giderebilirsiniz.

Yeni `config.yml` yapısı:
~~~yaml
# HATA AYIKLAMA (DEBUG)
debug:
  # Ana şalter: Bu 'false' ise, aşağıdaki hiçbir ayarın önemi kalmaz.
  enabled: false

  # Modül bazında debug ayarları
  modules:
    activity_listener: false      # Hangi eylemin AFK sayacını sıfırladığını gösterir.
    behavioral_analysis: false    # Gelişmiş yörünge analizinin adımlarını loglar.
    learning_mode: false          # Desen tanıma (DTW) ve nesne havuzu durumunu gösterir.
    command_registration: false   # Komutların nasıl kaydedildiğini loglar.
    database_queries: false       # Veritabanı/Önbellek (Cache HIT/MISS) okumalarını gösterir.
~~~

---

### 🌍 Uluslararasılaştırma (i18n)
AntiAFK, tam uluslararasılaştırma desteği sunar. Konsol ve sistem mesajlarının dili `config.yml` üzerinden ayarlanabilirken, oyunculara gösterilen tüm mesajlar `messages.yml` dosyasından düzenlenebilir.

**Desteklenen Diller:**
* Türkçe
* İngilizce
* İspanyolca
* Almanca
* Fransızca
* Rusça
* Lehçe

---

### 🔗 Entegrasyonlar
* **WorldGuard:** Bölge bazında özel AFK süreleri ve ceza eylemleri tanımlanmasını sağlar.
* **PlaceholderAPI:** Dinamik verileri (`%antiafk_tag%`, `%antiafk_reason%`, vb.) diğer eklentilere sunar.
* **ProtocolLib (İsteğe Bağlı):** GUI içindeki metin girişi gibi özellikler için daha gelişmiş bir kullanıcı deneyimi sunar.

---

### 📦 Kurulum
1.  Son sürümü [GitHub Releases](https://github.com/bentahsin/AntiAFK/releases) sayfasından indirin.
2.  `.jar` dosyasını sunucunuzun `plugins` klasörüne yerleştirin.
3.  (Önerilen) WorldGuard, PlaceholderAPI, ProtocolLib eklentilerini kurun.
4.  Sunucuyu başlatarak varsayılan yapılandırma dosyalarının (`config.yml`, `messages.yml`, `questions.yml`, `playerdata.db`) oluşturulmasını sağlayın.
5.  Yapılandırma dosyalarını ihtiyaçlarınıza göre düzenleyin ve `/antiafk reload` komutunu kullanın.

---

### 🤝 Katkıda Bulunma ve Destek
Hata bildirimleri, özellik önerileri ve "pull request"ler memnuniyetle karşılanır. Lütfen bir tartışma başlatmak veya sorun bildirmek için [GitHub Issues](https://github.com/bentahsin/antiafk/issues) sayfasını kullanın.