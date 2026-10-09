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
[Kullanıcı / Kamera] ──> Toprak Fotoğrafı ──> CNN (MobileNetV2) ──> Toprak Türü ──┐
                                                                                  │
[GPS / Konum] ──────────> OpenWeather API ──> Sıcaklık & Nem ─────┐               │
                                                                  ├──> [Random Forest / ANN] ──> Hibrit Karar Katmanı
[Sensör / Manuel Giriş] ─> N-P-K & pH Değerleri ──────────────────┘                            (MainActivity.java)
                                                                                                        │
                                                                                                        ├── % Uyum Skoru
                                                                                                        ├── Ürün Tavsiyesi
                                                                                                        └── Uygunluk Uyarısı
🚀 Kurulum
Gereksinimler
Android Studio (Giraffe veya daha güncel bir sürüm)

Android SDK (API Seviyesi 24+ önerilir)

Python 3.9+ (Model geliştirme/eğitim için)

Aktif OpenWeatherMap API Anahtarı


1. Repoyu Klonla
git clone <repo-url>
cd akilli-tarim-mobil







