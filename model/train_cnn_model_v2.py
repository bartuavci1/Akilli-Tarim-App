import tensorflow as tf
from tensorflow.keras.applications import MobileNetV2
from tensorflow.keras.layers import Dense, GlobalAveragePooling2D, Dropout, BatchNormalization
from tensorflow.keras.models import Model
from tensorflow.keras.preprocessing.image import ImageDataGenerator
from tensorflow.keras.optimizers import Adam
from tensorflow.keras.callbacks import ModelCheckpoint, EarlyStopping, ReduceLROnPlateau
from sklearn.utils import class_weight
import numpy as np
import joblib
import os

# --- AYARLAR ---
# Klasör yapısı: toprak_resimleri/Killi, toprak_resimleri/Kumlu, toprak_resimleri/Tinli
DATA_PATH = 'toprak_resimleri/' 
MODEL_SAVE_PATH = 'soil_vision_model.h5'
CLASSES_SAVE_PATH = 'soil_classes.pkl'

IMG_SIZE = (224, 224)
BATCH_SIZE = 16 
EPOCHS = 50

def train_professional_model():
    if not os.path.exists(DATA_PATH):
        print(f"HATA: '{DATA_PATH}' klasörü bulunamadı! Lütfen resimleri kontrol edin.")
        return

   
    datagen = ImageDataGenerator(
        rescale=1./255,
        rotation_range=40,
        width_shift_range=0.2,
        height_shift_range=0.2,
        shear_range=0.2,
        zoom_range=0.3,
        horizontal_flip=True,
        vertical_flip=True,
        brightness_range=[0.6, 1.4], 
        validation_split=0.2
    )

    train_gen = datagen.flow_from_directory(
        DATA_PATH,
        target_size=IMG_SIZE,
        batch_size=BATCH_SIZE,
        class_mode='categorical',
        subset='training',
        shuffle=True
    )

    val_gen = datagen.flow_from_directory(
        DATA_PATH,
        target_size=IMG_SIZE,
        batch_size=BATCH_SIZE,
        class_mode='categorical',
        subset='validation',
        shuffle=False
    )

    labels = train_gen.classes
    class_indices = train_gen.class_indices
    cw = class_weight.compute_class_weight(
        class_weight='balanced',
        classes=np.unique(labels),
        y=labels
    )
    class_weights_dict = dict(enumerate(cw))
    print(f"📊 Sınıf Ağırlıkları (Dengeleme): {class_weights_dict}")

   # (Fine-Tuning)
    base_model = MobileNetV2(weights='imagenet', include_top=False, input_shape=(224, 224, 3))
    
 
    base_model.trainable = True
    fine_tune_at = 120 
    for layer in base_model.layers[:fine_tune_at]:
        layer.trainable = False

    x = base_model.output
    x = GlobalAveragePooling2D()(x)
    x = BatchNormalization()(x)
    x = Dense(512, activation='relu')(x)
    x = Dropout(0.5)(x)
    x = Dense(256, activation='relu')(x)
    x = Dropout(0.3)(x)
    predictions = Dense(train_gen.num_classes, activation='softmax')(x)

    model = Model(inputs=base_model.input, outputs=predictions)


    model.compile(optimizer=Adam(learning_rate=1e-5), loss='categorical_crossentropy', metrics=['accuracy'])

    callbacks = [
        ModelCheckpoint(MODEL_SAVE_PATH, save_best_only=True, monitor='val_accuracy', verbose=1),
        EarlyStopping(monitor='val_loss', patience=8, restore_best_weights=True, verbose=1),
        ReduceLROnPlateau(monitor='val_loss', factor=0.5, patience=3, min_lr=1e-7, verbose=1)
    ]

    # EĞİTİM BAŞLATMA
   
    model.fit(
        train_gen,
        validation_data=val_gen,
        epochs=EPOCHS,
        class_weight=class_weights_dict, 
        callbacks=callbacks
    )

    # Sınıf listesini kaydetme 
    joblib.dump(list(class_indices.keys()), CLASSES_SAVE_PATH)
    print(f" En iyi model başarıyla kaydedildi: {MODEL_SAVE_PATH}")

if __name__ == "__main__":
    train_professional_model()