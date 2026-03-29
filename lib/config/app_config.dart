import 'package:flutter/foundation.dart';

class AppConfig {
  static const String _apiBaseUrlFromDefine = String.fromEnvironment('API_BASE_URL');

  static String get apiBaseUrl {
    // For production/live builds, pass --dart-define=API_BASE_URL=https://tu-api
    if (_apiBaseUrlFromDefine.trim().isNotEmpty) {
      return _apiBaseUrlFromDefine.trim();
    }

    if (kIsWeb) {
      return 'http://localhost:3000';
    }

    switch (defaultTargetPlatform) {
      case TargetPlatform.android:
        // Android emulator cannot reach host localhost directly.
        return 'http://10.0.2.2:3000';
      default:
        return 'http://localhost:3000';
    }
  }
}
