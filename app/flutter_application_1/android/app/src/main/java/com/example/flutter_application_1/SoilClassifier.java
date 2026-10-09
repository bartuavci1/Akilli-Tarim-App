package com.example.flutter_application_1;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import org.tensorflow.lite.Interpreter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Bu sınıf TensorFlow Lite modelini yükler ve görüntü analizi yapmaya yarar.
 */
public class SoilClassifier {
    private Interpreter interpreter;
    private final int INPUT_SIZE = 224;

    public SoilClassifier(Context context) throws IOException {
        // Assets klasöründeki tflite modelini yükler.
        interpreter = new Interpreter(loadModelFile(context));
    }

    private MappedByteBuffer loadModelFile(Context context) throws IOException {
        // Model dosyasını assets klasöründen okur.
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd("soil_model.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public String classify(Bitmap bitmap) {
        // Görüntüyü modelin beklediği boyuta (224x224) getirir.
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);
        ByteBuffer byteBuffer = convertBitmapToByteBuffer(scaledBitmap);

        // Tahmin sonuçları için yer ayırır (3 sınıf: Killi, Kumlu, Tinli)
        float[][] output = new float[1][3];
        interpreter.run(byteBuffer, output);

        // En yüksek olasılığa sahip sınıfın indeksini bulmaya yarar.
        int maxIndex = 0;
        for (int i = 0; i < 3; i++) {
            if (output[0][i] > output[0][maxIndex]) {
                maxIndex = i;
            }
        }

        // Sınıf isimlerini bir dizide tanımlama
        String[] classes = {"Killi", "Kumlu", "Tinli"};
        return classes[maxIndex];
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        // Pikselleri 4 byte float değerine çevirmek için buffer ayarlamaya yarar.
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * 3);
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[INPUT_SIZE * INPUT_SIZE];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        int pixel = 0;
        for (int i = 0; i < INPUT_SIZE; i++) {
            for (int j = 0; j < INPUT_SIZE; j++) {
                final int val = intValues[pixel++];
                // Pikselleri 0-1 arasına normalize ederek ekle.
                byteBuffer.putFloat(((val >> 16) & 0xFF) / 255.0f); // Kırmızı
                byteBuffer.putFloat(((val >> 8) & 0xFF) / 255.0f);  // Yeşil
                byteBuffer.putFloat((val & 0xFF) / 255.0f);         // Mavi
            }
        }
        return byteBuffer;
    }
}