// Bu test dosyası, uygulamamızın ana sınıfı olan CropPredictorApp'ı referans alır.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

// Ana uygulama dosyamızı import ediyoruz
import 'package:flutter_application_1/main.dart'; 

void main() {
  // Test, form ekranının düzgün bir şekilde açılıp açılmadığını kontrol eder.
  testWidgets('Uygulamanın başarıyla başlatılması', (WidgetTester tester) async {
    // Uygulamamızın ana widget'ını (CropPredictorApp) build ediyoruz.
    await tester.pumpWidget(const CropPredictorApp());

    // Ana bileşenlerin (AppBar başlığı ve Analiz Et butonu) ekranda olup olmadığını kontrol et.
    
    // 1. AppBar Başlığını bul
    expect(find.text('AI Destekli Tarım Analizi'), findsOneWidget);

    // 2. Analiz Et butonunu bul (Buton texti veya ikonu ile)
    expect(find.text('ANALİZ ET'), findsOneWidget);
    
    // 3. Giriş kutucuklarını (Örn: Azot) bul
    expect(find.byType(TextFormField), findsNWidgets(7));
  });
}