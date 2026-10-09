package com.example.flutter_application_1;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;
import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Hibrit Tarım Analiz Sistemi.
 * Temizlenmiş ve optimize edilmiş sürüm.
 */
public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 101;
    private static final int LOCATION_PERMISSION_CODE = 102;
    private static final String WEATHER_API_KEY = "e0742ffb0da4fb0239c003e13f38784e";

    // --- SAYISAL MODEL İÇİN NORMALİZASYON DEĞERLERİ ---
    private final float[] MEANS = {50.5519f, 53.3627f, 48.1491f, 25.6162f, 71.4818f, 6.4694f, 103.4636f};
    private final float[] SCALES = {36.9173f, 32.9859f, 50.6479f, 5.0638f, 22.2638f, 0.7739f, 54.9583f};

    private SoilClassifier visualClassifier;
    private Interpreter numericalInterpreter;
    private List<String> cropLabels = new ArrayList<>();
    private Map<String, String> translationMap = new HashMap<>();

    private FusedLocationProviderClient fusedLocationClient;
    private String aiDetectedSoil = "";

    private ImageView imgView;
    private TextView tvSoilResult, tvFinalResult;
    private EditText etN, etP, etK, etPh, etTemp, etHum, etRain;
    private Spinner soilTypeSpinner;

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        Bitmap photo = (Bitmap) extras.get("data");
                        if (photo != null) {
                            imgView.setImageBitmap(photo);
                            analyzeSoilVisual(photo);
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        initViews();
        setupTranslationMap();

        // Modelleri Başlat
        try {
            visualClassifier = new SoilClassifier(this);
            numericalInterpreter = new Interpreter(loadModelFile("numerical_brain.tflite"));
            loadCropLabels();
            tvFinalResult.setText("Sistem aktif. Analiz yapabilirsiniz.");
        } catch (Exception e) {
            tvFinalResult.setText("Sistem başlatılamadı. Lütfen dosyaları kontrol edin.");
        }

        findViewById(R.id.btnCapture).setOnClickListener(v -> checkPermissionAndLaunchCamera());
        findViewById(R.id.btnAnalyze).setOnClickListener(v -> startNumericalAIAnalysis());
        findViewById(R.id.btnGetWeather).setOnClickListener(v -> checkLocationPermissionAndFetch());
    }

    private void setupTranslationMap() {
        translationMap.put("rice", "Pirinç");
        translationMap.put("maize", "Mısır");
        translationMap.put("chickpea", "Nohut");
        translationMap.put("kidneybeans", "Fasulye");
        translationMap.put("pigeonpeas", "Güvercin Bezelyesi");
        translationMap.put("mothbeans", "Güve Fasulyesi");
        translationMap.put("mungbean", "Maş Fasulyesi");
        translationMap.put("blackgram", "Siyah Mercimek");
        translationMap.put("lentil", "Mercimek");
        translationMap.put("pomegranate", "Nar");
        translationMap.put("banana", "Muz");
        translationMap.put("mango", "Mango");
        translationMap.put("grapes", "Üzüm");
        translationMap.put("watermelon", "Karpuz");
        translationMap.put("muskmelon", "Kavun");
        translationMap.put("apple", "Elma");
        translationMap.put("orange", "Portakal");
        translationMap.put("papaya", "Papaya");
        translationMap.put("coconut", "Hindistan Cevizi");
        translationMap.put("cotton", "Pamuk");
        translationMap.put("jute", "Jüt");
        translationMap.put("coffee", "Kahve");
        translationMap.put("unsuitable", "Tarım İçin Uygun Değil!");
    }

    private void analyzeSoilVisual(Bitmap photo) {
        if (visualClassifier == null) return;

        try {
            aiDetectedSoil = visualClassifier.classify(photo);
            tvSoilResult.setText("AI Teşhisi: " + aiDetectedSoil);

            if (aiDetectedSoil.equals("Killi")) soilTypeSpinner.setSelection(1);
            else if (aiDetectedSoil.equals("Kumlu")) soilTypeSpinner.setSelection(2);
            else if (aiDetectedSoil.equals("Tinli")) soilTypeSpinner.setSelection(3);
        } catch (Exception e) {
            tvSoilResult.setText("Görsel analiz hatası.");
        }
    }

    private void startNumericalAIAnalysis() {
        if (numericalInterpreter == null || cropLabels.isEmpty()) return;

        try {
            float n = (float) parseDoubleSafe(etN);
            float p = (float) parseDoubleSafe(etP);
            float k = (float) parseDoubleSafe(etK);
            float temp = (float) parseDoubleSafe(etTemp);
            float hum = (float) parseDoubleSafe(etHum);
            float ph = (float) parseDoubleSafe(etPh);
            float rain = (float) parseDoubleSafe(etRain);

            float[] rawInputs = {n, p, k, temp, hum, ph, rain};
            float[] normalizedInputs = new float[7];
            for (int i = 0; i < 7; i++) {
                normalizedInputs[i] = (rawInputs[i] - MEANS[i]) / SCALES[i];
            }

            float[][] input = {normalizedInputs};
            float[][] output = new float[1][cropLabels.size()];
            numericalInterpreter.run(input, output);

            int maxIdx = 0;
            for (int i = 0; i < cropLabels.size(); i++) {
                if (output[0][i] > output[0][maxIdx]) maxIdx = i;
            }

            String engLabel = cropLabels.get(maxIdx);
            String trLabel = translationMap.getOrDefault(engLabel, engLabel);
            float confidence = output[0][maxIdx] * 100;

            String soil = (soilTypeSpinner.getSelectedItemPosition() > 0) ?
                    soilTypeSpinner.getSelectedItem().toString() : aiDetectedSoil;

            displayAnalysisResult(trLabel, engLabel, confidence, soil);

        } catch (Exception e) {
            Toast.makeText(this, "Lütfen tüm verileri girin.", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayAnalysisResult(String label, String raw, float confidence, String soil) {
        boolean isCompatible = true;
        if (raw.equals("watermelon") && !soil.equals("Kumlu")) isCompatible = false;
        if (raw.equals("rice") && soil.equals("Kumlu")) isCompatible = false;

        if (raw.equals("unsuitable") || confidence < 15) {
            tvFinalResult.setText("⚠️ ANALİZ UYARISI:\nKoşullar tarım için uygun değildir.\nUyum: %" + String.format("%.1f", confidence));
            tvFinalResult.setTextColor(0xFFFF0000);
        } else if (!isCompatible) {
            tvFinalResult.setText("❌ TOPRAK UYUMSUZLUĞU:\n" + label.toUpperCase() + " için " + soil + " toprak yapısı uygun değildir.");
            tvFinalResult.setTextColor(0xFFD32F2F);
        } else {
            tvFinalResult.setText("🌟 TAVSİYE: " + label.toUpperCase() + "\nUyum: %" + String.format("%.1f", confidence) + "\nToprak: " + soil);
            tvFinalResult.setTextColor(0xFF1B5E20);
        }
    }

    private double parseDoubleSafe(EditText et) throws Exception {
        String s = et.getText().toString().replace(",", ".");
        if (s.isEmpty()) throw new Exception();
        return Double.parseDouble(s);
    }

    private void initViews() {
        imgView = findViewById(R.id.soilImageView);
        tvSoilResult = findViewById(R.id.tvSoilResult);
        tvFinalResult = findViewById(R.id.tvFinalResult);
        etN = findViewById(R.id.etN); etP = findViewById(R.id.etP); etK = findViewById(R.id.etK);
        etPh = findViewById(R.id.etPh); etTemp = findViewById(R.id.etTemp);
        etHum = findViewById(R.id.etHum); etRain = findViewById(R.id.etRain);
        soilTypeSpinner = findViewById(R.id.soilTypeSpinner);

        List<String> list = new ArrayList<>();
        list.add("Toprak Tipi (Seçiniz)"); list.add("Killi"); list.add("Kumlu"); list.add("Tinli");
        ArrayAdapter<String> adp = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
        adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        soilTypeSpinner.setAdapter(adp);
    }

    private MappedByteBuffer loadModelFile(String modelName) throws Exception {
        android.content.res.AssetFileDescriptor fd = getAssets().openFd(modelName);
        FileInputStream is = new FileInputStream(fd.getFileDescriptor());
        return is.getChannel().map(FileChannel.MapMode.READ_ONLY, fd.getStartOffset(), fd.getDeclaredLength());
    }

    private void loadCropLabels() throws Exception {
        BufferedReader r = new BufferedReader(new InputStreamReader(getAssets().open("crop_labels.txt")));
        String line; cropLabels.clear();
        while ((line = r.readLine()) != null) if (!line.trim().isEmpty()) cropLabels.add(line.trim());
        r.close();
    }

    private void checkPermissionAndLaunchCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            launchCamera();
        }
    }

    private void launchCamera() {
        cameraLauncher.launch(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));
    }

    private void checkLocationPermissionAndFetch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        } else {
            fetchLocation();
        }
    }

    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;
        fusedLocationClient.getLastLocation().addOnSuccessListener(loc -> {
            if (loc != null) updateWeather(loc.getLatitude(), loc.getLongitude());
        });
    }

    private void updateWeather(double lat, double lon) {
        ExecutorService ex = Executors.newSingleThreadExecutor();
        Handler h = new Handler(Looper.getMainLooper());
        ex.execute(() -> {
            try {
                URL url = new URL("https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&units=metric&appid=" + WEATHER_API_KEY);
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                BufferedReader r = new BufferedReader(new InputStreamReader(c.getInputStream()));
                StringBuilder res = new StringBuilder(); String l;
                while ((l = r.readLine()) != null) res.append(l);
                JSONObject j = new JSONObject(res.toString());
                double t = j.getJSONObject("main").getDouble("temp");
                double hum = j.getJSONObject("main").getDouble("humidity");
                h.post(() -> {
                    etTemp.setText(String.format(Locale.US, "%.1f", t));
                    etHum.setText(String.format(Locale.US, "%.1f", hum));
                    etRain.setText(String.format(Locale.US, "%.1f", 350.0 + (Math.random() * 250)));
                });
            } catch (Exception e) { e.printStackTrace(); }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == CAMERA_PERMISSION_CODE) launchCamera();
            else if (requestCode == LOCATION_PERMISSION_CODE) fetchLocation();
        }
    }
}