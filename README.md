# 🌱 Yapay Zeka Destekli Akıllı Tarım ve Ürün Öneri Sistemi

Tarımsal verimliliği artırmak ve yanlış arazi kullanımının önüne geçmek amacıyla geliştirilmiş hibrit yapay zeka tabanlı bir karar destek mobil uygulamasıdır. Sistem; hem kameradan alınan toprak dokusu fotoğrafını hem de sensör/çevre verilerini (N-P-K, pH, sıcaklık, nem, yağış) birlikte analiz ederek en uygun ürün tavsiyesini ve toprak uygunluk durumunu belirler.

---

## ✨ Özellikler

- 📸 **Görsel Toprak Teşhisi** — Kameradan alınan toprak fotoğrafından doku sınıflandırması (Killi, Kumlu, Tınlı)
- 🌦️ **Dinamik Hava Durumu Entegrasyonu** — GPS konumu ile anlık sıcaklık ve nem verilerinin otomatik çekilmesi (OpenWeatherMap API)
- 🧠 **Hibrit Karar Mekanizması** — Görsel ve sayısal modellerin ortak değerlendirmesi ile çapraz doğrulama
- 🌾 **Ürün Öneri Motoru** — 23 farklı bitki türü arasından en yüksek verim sağlayacak ürün tahmini
- ⚠️ **Uygunsuzluk ve Risk Tespiti** — Tarıma elverişsiz toprak veya toprak-ürün uyuşmazlığında anlık uyarı sistemi
- ⚡ **Cihaz Üstü Çıkarım (On-Device Inference)** — TFLite modelleri sayesinde hızlı ve optimize mobil çalışma

---

## 🏗️ Teknoloji Stack

| Katman | Teknoloji |
| :--- | :--- |
| **Mobil (Android)** | Java, XML, Material Design, Fused Location Provider |
| **Yapay Zeka & ML** | Python 3.x, TensorFlow, Keras, Scikit-learn, Pandas, NumPy |
| **Model Mimarisi** | MobileNetV2 (CNN - Transfer Learning), Random Forest Classifier, ANN |
| **Mobil AI Formatı** | TensorFlow Lite (TFLite), Joblib |
| **Harici Servisler** | OpenWeatherMap REST API, HttpURLConnection |
| **Derleme & Yönetim** | Android Gradle (KTS), Android Studio |

---

## 🗄️ Sistem ve Veri Akış Şeması

```text
[Kamera] ──────> Toprak Fotoğrafı ──> CNN (MobileNetV2) ─> Toprak Türü ──┐
                                                                          │
[GPS/Konum] ───> OpenWeather API ───> Sıcaklık & Nem ────┐               ├──> [Hibrit Karar Katmanı] ──> Sonuç & Öneri
                                                         ├──> [RF / ANN] ─┘    (MainActivity.java)
[Veri Girişi] ─> N-P-K & pH Değerleri ───────────────────┘
```

---

## 🚀 Kurulum

### Gereksinimler
- Android Studio (Giraffe veya daha güncel bir sürüm)
- Android SDK (API Seviyesi 24+ önerilir)
- Python 3.9+ (Model geliştirme/eğitim için)
- Aktif OpenWeatherMap API Anahtarı

---

### 1. Repoyu Klonla
```bash
git clone <repo-url>
cd akilli-tarim-mobil
```

---

### 2. Yapay Zeka Modellerini Derleme (Opsiyonel / Model Geliştirici)
Modelleri sıfırdan eğitmek ve `.tflite` formatına dönüştürmek isterseniz:

```bash
cd ai_models

# Gerekli Python kütüphanelerini kurun
pip install tensorflow scikit-learn pandas numpy joblib

# CNN ve Random Forest modellerini eğitin
python train_cnn_model_v2.py
python hybrid_model_training.py

# Modelleri TFLite formatına çevirin
python converter.py
```

---

### 3. Mobil Uygulamayı Başlatma (Android Studio)
1. **Android Studio**'yu açın ve `File > Open` seçeneğiyle proje kök dizinini seçin.
2. Gradle senkronizasyonunun (`Gradle Sync`) tamamlanmasını bekleyin.
3. OpenWeatherMap API anahtarınızı `MainActivity.java` veya ilgili yapılandırma dosyasındaki alana girin:
   ```java
   private static final String API_KEY = "BURAYA_API_KEY_YAZIN";
   ```
4. `.tflite` uzantılı model dosyalarının `app/src/main/assets/` klasöründe yer aldığından emin olun.
5. Bir Android emülatör veya fiziksel cihaz bağlayarak **Run (Shift + F10)** butonuna basın.

---

## 📱 Kullanım Akışı

1. **Toprak Teşhisi:** "Fotoğraf Analizi Yap" butonuna tıklayarak toprak fotoğrafı çekin veya galeriden yükleyin (Model toprak tipini Killi/Kumlu/Tınlı olarak belirler).
2. **Çevresel Veriler:** "Konumu Kullan" butonuna basarak anlık hava durumu verilerini (Sıcaklık, Nem) otomatik doldurun ya da Azot (N), Fosfor (P), Potasyum (K) ve pH değerlerini manuel girin.
3. **Hibrit Analiz:** "Hibrit Analizi Başlat" butonuna tıklayarak hem görsel hem de kimyasal parametrelerin çapraz sorgulanmasını sağlayın.
4. **Sonuç Raporu:** Önerilen ürün (Örn: Nohut, Mısır, Karpuz, Kahve), uyumluluk yüzdesi (%) ve varsa tarımsal uyarıları görüntüleyin.

---

## 📁 Proje Yapısı

```text
akilli-tarim/
├── android/                        # Android Mobil Uygulama
│   ├── app/
│   │   ├── src/
│   │   │   └── main/
│   │   │       ├── assets/         # .tflite optimize modelleri
│   │   │       ├── java/           # Java kaynak kodları (MainActivity, API Client vb.)
│   │   │       ├── res/            # XML layout'lar ve arayüz bileşenleri
│   │   │       └── AndroidManifest.xml
│   │   └── build.gradle.kts
│   └── build.gradle.kts
├── ai_models/                      # Yapay Zeka & Model Eğitim Kodları
│   ├── train_cnn_model_v2.py       # Toprak dokusu teşhisi için MobileNetV2 (CNN) eğitimi
│   ├── hybrid_model_training.py    # Sayısal veri (N-P-K, pH vb.) ile ürün öneri modeli
│   ├── converter.py                # Keras/H5 modellerini TFLite formatına çevirici
│   └── dataset/                    # 2700 satırlık tarımsal eğitim veri seti
└── README.md
```

---

## 🔬 Geliştirici Notları

- **Model Başarımı:** Uygulama, hibrit çıkarım mekanizması ile toprak analizi ve ürün tahmininde yaklaşık **%90 doğruluk oranı** ile çalışmaktadır.
- **TFLite Opcode Optimizasyonu:** Model mobilleştirilirken eski Android sürümleriyle tam uyumluluk sağlanması amacıyla Opcode sürümleri optimize edilmiştir.
- **Gerekli İzinler:** Uygulamanın tam fonksiyonel çalışabilmesi için cihaz üzerinde `CAMERA`, `ACCESS_FINE_LOCATION` ve `INTERNET` izinlerinin verilmiş olması gereklidir.