import pandas as pd
import numpy as np
import os

INPUT_FILE = 'Crop_recommendation_updated.csv'
OUTPUT_FILE = 'extended_hybrid_data.csv'

# Veri setindeki ürünlerin (label) ideal toprak tipleriyle eşleştirilmesi

SOIL_MAPPING = {
    'rice': 'Killi',
    'maize': 'Tinli',
    'chickpea': 'Kumlu',
    'kidneybeans': 'Tinli',
    'pigeonpeas': 'Tinli',
    'mothbeans': 'Kumlu',
    'mungbean': 'Tinli',
    'blackgram': 'Tinli',
    'lentil': 'Kumlu',
    'pomegranate': 'Tinli',
    'banana': 'Tinli',
    'mango': 'Tinli',
    'grapes': 'Kumlu',
    'watermelon': 'Kumlu',
    'muskmelon': 'Kumlu',
    'apple': 'Tinli',
    'orange': 'Kumlu',
    'papaya': 'Tinli',
    'coconut': 'Kumlu',
    'cotton': 'Killi',
    'jute': 'Tinli',
    'coffee': 'Tinli',
    'unsuitable': 'Uygun Degil'
}

def generate_hybrid_csv():
    if not os.path.exists(INPUT_FILE):
        print(f"HATA: '{INPUT_FILE}' dosyası bulunamadı. Lütfen dosyanın proje klasöründe olduğundan emin olun.")
        return

    print(f"'{INPUT_FILE}' okunuyor ve hibrit yapıya dönüştürülüyor...")
    df = pd.read_csv(INPUT_FILE)


    hybrid_df = df[['ph', 'temperature', 'humidity', 'label']].copy()
    hybrid_df.columns = ['pH', 'Sicaklik', 'Nem', 'Sonuc']


    def assign_soil(label):
        return SOIL_MAPPING.get(label, 'Tinli') 

    hybrid_df['Toprak_Tipi'] = hybrid_df['Sonuc'].apply(assign_soil)

    
    unsuitable_mask = hybrid_df['Sonuc'] == 'unsuitable'
    hybrid_df.loc[unsuitable_mask, 'Toprak_Tipi'] = np.random.choice(['Killi', 'Kumlu', 'Tinli'], size=unsuitable_mask.sum())
    hybrid_df.loc[unsuitable_mask, 'Sonuc'] = 'Uygun Degil'


    hybrid_df = hybrid_df[['pH', 'Sicaklik', 'Nem', 'Toprak_Tipi', 'Sonuc']]
    hybrid_df.to_csv(OUTPUT_FILE, index=False)
    
    print(f"BAŞARILI: {len(hybrid_df)} satırlık gerçek veri tabanlı hibrit dosya '{OUTPUT_FILE}' adıyla oluşturuldu.")
    print("Benzersiz Ürün Sayısı:", len(hybrid_df['Sonuc'].unique()))

if __name__ == "__main__":
    generate_hybrid_csv()
