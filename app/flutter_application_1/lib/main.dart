import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';

void main() {
  runApp(const CropPredictorApp());
}

// Uygulamanın temel yapısı
class CropPredictorApp extends StatelessWidget {
  const CropPredictorApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'AI Destekli Tarım Analizi',
      theme: ThemeData(
        primarySwatch: Colors.green,
        visualDensity: VisualDensity.adaptivePlatformDensity,
        useMaterial3: true,
      ),
      home: const PredictionScreen(),
    );
  }
}

// Veri girişlerini ve sonucu tutacak durumlu widget (StatefulWidget)
class PredictionScreen extends StatefulWidget {
  const PredictionScreen({super.key});

  @override
  State<PredictionScreen> createState() => _PredictionScreenState();
}

class _PredictionScreenState extends State<PredictionScreen> {
  // ----------------------------------------------------
  // Durum Değişkenleri (State Variables)
  // ----------------------------------------------------
  
  // Form verilerini tutmak için Map. Başlangıçta örnek veriler atanmıştır.
  final Map<String, double> _formData = {
    'N': 90.0, 
    'P': 42.0,
    'K': 43.0,
    'temperature': 25.0,
    'humidity': 80.0,
    'ph': 6.5,
    'rainfall': 200.0,
  };

  bool _isLoading = false; 
  final GlobalKey<FormState> _formKey = GlobalKey<FormState>(); 

  // ----------------------------------------------------
  // Fonksiyonlar
  // ----------------------------------------------------
  
  // Giriş kutucuklarını oluşturmak için yardımcı widget
  Widget _buildInputField(String label, String keyName) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8.0),
      child: TextFormField(
        decoration: InputDecoration(
          labelText: '$label ($keyName)', // Interpolasyon parantezleri temizlendi
          border: const OutlineInputBorder(),
        ),
        keyboardType: const TextInputType.numberWithOptions(decimal: true),
        initialValue: _formData[keyName]?.toString(), 
        validator: (value) {
          if (value == null || value.isEmpty) {
            return 'Bu alan boş bırakılamaz.';
          }
          if (double.tryParse(value) == null) {
            return 'Geçerli bir sayı giriniz.';
          }
          return null;
        },
        onSaved: (value) {
          _formData[keyName] = double.tryParse(value!) ?? 0.0;
        },
      ),
    );
  }
  
  // API isteğini gönderen ana fonksiyon
  Future<void> _analyzeSoil() async {
    if (!_formKey.currentState!.validate()) {
      return; 
    }
    _formKey.currentState!.save(); 

    setState(() {
      _isLoading = true;
    });

    // 🚨 IP ADRESİNİZİ KONTROL EDİN
    // Flask sunucunuzun çıktısındaki IP adresi: 192.168.1.3 olmalıdır.
    const String apiUrl = 'http://192.168.1.9:5000/predict';

    try {
      final Map<String, dynamic> requestBody = {
        'N': _formData['N'],
        'P': _formData['P'],
        'K': _formData['K'],
        'temperature': _formData['temperature'],
        'humidity': _formData['humidity'],
        'ph': _formData['ph'],
        'rainfall': _formData['rainfall'],
      };
      
      final response = await http.post(
        Uri.parse(apiUrl),
        headers: {'Content-Type': 'application/json; charset=UTF-8'},
        body: jsonEncode(requestBody),
      ).timeout(const Duration(seconds: 15)); 

      if (response.statusCode == 200) {
        final responseJson = jsonDecode(response.body);
        final durum = responseJson['durum'];
        final tahmin = responseJson['tahmin'];

        if (durum == 'success') {
            _showResultDialog(true, tahmin); 
        } else if (durum == 'warning' && tahmin == 'unsuitable') {
            _showResultDialog(false, null); 
        } else {
            _showErrorDialog("Beklenmeyen Cevap", "Sunucudan hatalı veya eksik bir JSON formatı geldi.");
        }
      } else {
        _showErrorDialog("Sunucu Hatası", "API erişilemiyor veya hata kodu döndü: ${response.statusCode}");
      }
    } catch (e) {
      // Artık print() kullanmıyoruz, sadece hata dialogunu gösteriyoruz.
      _showErrorDialog("Bağlantı Hatası", "Sunucuya ulaşılamadı. IP adresini, portu (5000) ve güvenlik duvarını kontrol edin.\nHata Detayı: $e"); 
    } finally {
      setState(() {
        _isLoading = false;
      });
    }
  }

  // Hata Dialog'u
  void _showErrorDialog(String title, String content) {
    showDialog(
      context: context,
      builder: (BuildContext context) {
        return AlertDialog(
          backgroundColor: Colors.orange.shade50,
          title: Text('⚠️ $title', style: TextStyle(color: Colors.orange.shade700, fontWeight: FontWeight.bold)),
          content: Text(content),
          actions: <Widget>[
            TextButton(
              onPressed: () => Navigator.of(context).pop(),
              child: const Text('Tamam', style: TextStyle(color: Colors.orange)),
            ),
          ],
        );
      },
    );
  }

  // Sonuç ekranını gösteren dialog (Senaryo A ve B)
  void _showResultDialog(bool success, String? cropName) {
    showDialog(
      context: context,
      builder: (BuildContext context) {
        return AlertDialog(
          backgroundColor: success ? Colors.green.shade50 : Colors.red.shade50,
          title: Text(
            success ? '✅ BAŞARILI ANALİZ' : '❌ UYARI',
            style: TextStyle(color: success ? Colors.green.shade700 : Colors.red.shade700, fontWeight: FontWeight.bold),
          ),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(
                success ? 'Bu Toprak Uygundur' : 'Toprak Tarıma Uygun Değil / Verimsiz',
                style: const TextStyle(fontSize: 18),
              ),
              if (success && cropName != null) ...[
                const SizedBox(height: 10),
                Text(
                  // Ürün adının ilk harfini büyütme
                  '${cropName[0].toUpperCase()}${cropName.substring(1).toLowerCase()}',
                  style: TextStyle(
                    fontSize: 30,
                    fontWeight: FontWeight.w900,
                    color: Colors.green.shade900,
                  ),
                ),
                const SizedBox(height: 10),
              ],
            ],
          ),
          actions: <Widget>[
            TextButton(
              onPressed: () => Navigator.of(context).pop(),
              child: Text('Kapat', style: TextStyle(color: success ? Colors.green : Colors.red)),
            ),
          ],
        );
      },
    );
  }

  // Arayüz (Build Metodu)
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('AI Destekli Tarım Analizi'),
        backgroundColor: Colors.green,
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16.0),
        child: Form(
          key: _formKey,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: <Widget>[
              const Text(
                'Toprak Verilerini Giriniz:',
                style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 10),
              
              // 7 Giriş Kutucuğu
              _buildInputField('Azot', 'N'),
              _buildInputField('Fosfor', 'P'),
              _buildInputField('Potasyum', 'K'),
              _buildInputField('Sıcaklık (°C)', 'temperature'),
              _buildInputField('Nem (%)', 'humidity'),
              _buildInputField('pH Değeri', 'ph'),
              _buildInputField('Yağış (mm)', 'rainfall'),

              const SizedBox(height: 20),
              
              // Analiz Et Butonu
              ElevatedButton.icon(
                onPressed: _isLoading ? null : _analyzeSoil, 
                icon: _isLoading 
                    ? const SizedBox(
                        width: 20,
                        height: 20,
                        child: CircularProgressIndicator(color: Colors.white, strokeWidth: 2),
                      )
                    : const Icon(Icons.analytics),
                label: Text(_isLoading ? 'ANALİZ EDİLİYOR...' : 'ANALİZ ET'),
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.green,
                  foregroundColor: Colors.white,
                  padding: const EdgeInsets.symmetric(vertical: 15),
                  textStyle: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                ),
              ),
              
              const SizedBox(height: 20),
              const Center(
                child: Text(
                  'Lütfen sunucunuzun (192.168.1.9:5000/predict) çalıştığından emin olun.',
                  style: TextStyle(fontSize: 14, color: Colors.blueGrey),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}