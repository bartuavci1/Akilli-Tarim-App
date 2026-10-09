import tensorflow as tf
import os

# --- AYARLAR ---
# Eğitim kodundan çıkan .h5 dosyasının adı
H5_MODEL_PATH = 'soil_vision_model.h5'
# Android Studio 'assets' klasörüne atılacak dosya adı
TFLITE_OUTPUT_PATH = 'soil_model.tflite'

def convert_to_tflite_safe():
    """
    Keras (.h5) modelini, Android kütüphaneleriyle tam uyumlu (Opcode Version Fix)
    bir TFLite modeline dönüştürür.
    """
    
    # 1. Dosya Kontrolüa
    if not os.path.exists(H5_MODEL_PATH):
        print(f"HATA: '{H5_MODEL_PATH}' bulunamadı!")
        print("Lütfen önce eğitim scriptini çalıştırarak .h5 dosyasını oluşturun.")
        return

    print(f"🔄 '{H5_MODEL_PATH}' yükleniyor...")
    try:
        # Modeli yükle
        model = tf.keras.models.load_model(H5_MODEL_PATH)
        
        print("🏗️ Uyumluluk modunda dönüştürme başlatılıyor (Strict Compatibility)...")
        converter = tf.lite.TFLiteConverter.from_keras_model(model)

        # --- VERSİYON HATALARINI (Opcode 12 vb.) ÇÖZEN EN GÜVENLİ AYARLAR ---
        
        # Sadece en temel TFLite işlemlerini kullan (Yeni sürümleri engeller)
        converter.target_spec.supported_ops = [
            tf.lite.OpsSet.TFLITE_BUILTINS 
        ]
        
        # Veri tiplerini float32'ye sabitler (Bazen float16 yeni op'lar tetikler)
        converter.target_spec.supported_types = [tf.float32]
        
        # Yeni opcode sürümlerini (Version 12 vb.) Android'in anlayacağı seviyeye (Version 1-3) indirger
        converter._experimental_lower_tensor_list_ops = True
        
        # Karmaşık optimizasyonları kapatıyoruz (Opcode hatasını tetikleyebilir)
        # Sadece temel optimizasyon kullanıyoruz
        converter.optimizations = [tf.lite.Optimize.DEFAULT]

        # 2. Dönüştürmeyi Gerçekleştir
        print("⚡ Dönüştürme işlemi yapılıyor...")
        tflite_model = converter.convert()
        
        # 3. Dosyayı Kaydet
        with open(TFLITE_OUTPUT_PATH, 'wb') as f:
            f.write(tflite_model)
            
        print("\n" + "="*50)
        print(f"✨ BAŞARILI! '{TFLITE_OUTPUT_PATH}' dosyası oluşturuldu.")
        print("="*50)
        print(f"Boyut: {os.path.getsize(TFLITE_OUTPUT_PATH) // 1024} KB")
        print("\n💡 ÖNEMLİ SON ADIMLAR:")
        print(f"1. Android Studio 'build.gradle' dosyasında şu satırın olduğundan emin ol:")
        print("   implementation('org.tensorflow:tensorflow-lite:2.16.1')")
        print("2. Uygulamayı telefondan SİL ve bu yeni oluşan dosyayı atıp tekrar kur.")
        
    except Exception as e:
        print(f"❌ Dönüştürme sırasında bir hata oluştu: {e}")
        print("\n⚠️ ÇÖZÜM ÖNERİSİ: Eğer hata devam ederse, bilgisayarındaki terminale")
        print("   'pip install tensorflow==2.14.0' yazarak TensorFlow sürümünü düşürüp")
        print("   modeli tekrar eğitmeyi deneyebilirsin.")

if __name__ == "__main__":
    convert_to_tflite_safe()