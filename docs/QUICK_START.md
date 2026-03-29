# ⚡ Quick Start - Logística Morales

## 🚀 5 Minutos para Empezar

### 1️⃣ Clonar/Abrir el Proyecto
```bash
cd /Users/eduardomorales/Desktop/PruebaAndroid
flutter pub get
```

### 2️⃣ Ejecutar la App
```bash
flutter run -d android
```

### 3️⃣ Testear API Localmente (Node.js)
```bash
# En otra terminal, en la carpeta del API
cd docs/
node API_IMPLEMENTATION_NODEJS.js

# Probar login
curl -X POST http://localhost:3000/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"conductor@logistica.com","password":"securePassword123"}'
```

---

## 📁 Archivos Importantes

| Archivo | Propósito |
|---------|-----------|
| `lib/main.dart` | Punto de entrada principal |
| `lib/firebase_options.dart` | Credenciales Firebase |
| `lib/services/auth_service.dart` | Servicio de autenticación |
| `pubspec.yaml` | Dependencias del proyecto |
| `pubspec.lock` | Versiones exactas instaladas |
| `docs/API_REST_ENDPOINTS.md` | Documentación de API |
| `docs/API_EJEMPLOS_CURL_FLUTTER.md` | Ejemplos de uso |
| `docs/ROADMAP_COMPLETO.md` | Plan de desarrollo |

---

## 🛠️ Comandos Útiles Flutter

```bash
# Verificar que todo está bien
flutter doctor

# Limpiar y recompilaciones
flutter clean

# Analizar código
flutter analyze

# Ejecutar tests
flutter test

# Construir APK para testing
flutter build apk --debug

# Construir APK para producción
flutter build apk --release
```

---

## 📱 Estructura de la App

```
Splash Screen (App startup)
        ↓
   Login Screen (Inicia sesión)
        ↓
   Dashboard (Menú principal)
        ├─→ Admin: Ver/crear pedidos
        └─→ Conductor: Ver mis tareas
```

---

## 🔐 Credenciales de Testing

### Firebase (Google)
- **Proyecto**: logistica-morales
- **Credencial**: En `lib/firebase_options.dart`
- **Autenticación**: Firebase Auth + JWT

### API REST (Local)
```json
{
  "email": "admin@logistica.com",
  "password": "adminPass123"
}
```

**Roles**: admin, conductor, cliente

---

## 📊 Status de Implementación

### ✅ Completado (Backend)
- API REST con 5 endpoints documentados
- Especificación OpenAPI 3.0
- Ejemplo de implementación Node.js
- Autenticación con JWT
- Colecciones Postman
- Guía de seguridad
- Guía de despliegue

### 🔄 En Desarrollo (Frontend)
- Estructura base de Flutter con GetX
- Servicio de autenticación Firebase
- Modelos de datos Dart
- Home view básica

### ⏳ Por Hacer
1. Interfaz de login/registro
2. Servicio de API (Dio)
3. Pantalla de pedidos
4. Integración de mapas
5. Seguimiento GPS
6. Captura de fotos

---

## 🐛 Troubleshooting Rápido

### La app no inicia
```bash
# 1. Verificar que Flutter está instalado
flutter --version

# 2. Limpiar caché
flutter clean

# 3. Obtener dependencias nuevamente
flutter pub get

# 4. Ver errores detallados
flutter run -v
```

### Error de Firebase
```bash
# Verificar que google-services.json está en:
# android/app/google-services.json

# Si falta, descargar desde Firebase Console:
# 1. Ir a https://console.firebase.google.com
# 2. Proyecto: logistica-morales
# 3. Aplicación Android
# 4. Descargar google-services.json
# 5. Copiarlo a android/app/
```

### Emulador no conecta
```bash
# Ver dispositivos disponibles
flutter devices

# Si no aparece, abrir Android Studio
# Tools > Device Manager > Crear dispositivo nuevo

# Si aún no funciona:
flutter clean
flutter pub get
```

---

## 📞 Contacto Rápido

**Documentación completa**: Ver archivos en `/docs/`

| Necesidad | Archivo |
|-----------|---------|
| Entender la API | `API_REST_ENDPOINTS.md` |
| Ejemplos con cURL | `API_EJEMPLOS_CURL_FLUTTER.md` |
| Desplegar a producción | `DEPLOY_GUIA_PRODUCCION.md` |
| Ver todo el plan | `ROADMAP_COMPLETO.md` |
| Estructura de BD | `firestore_structure.md` |

---

## ✨ Tips Pro

### 1. Usar Postman para testing
```bash
# Importar collection
1. Abrir Postman
2. File > Import
3. Seleccionar docs/Postman_Collection.json
4. Ejecutar requests
```

### 2. Ver logs en tiempo real
```bash
# Flutter
flutter run -d android -v

# Firebase (desde Console)
# https://console.firebase.google.com > Logs
```

### 3. Debuggear con VS Code
```json
// .vscode/launch.json
{
  "version": "0.2.0",
  "configurations": [
    {
      "name": "Flutter: Run App",
      "type": "dart",
      "request": "launch",
      "program": "lib/main.dart"
    }
  ]
}
```

### 4. Usar GetX DevTools
```dart
// En main.dart
GetMaterialApp(
  enableLog: true,
  logWriterCallback: logFunction,
  // ...
)
```

---

## 🎯 Próximo Paso

**Ahora deberías**:
1. ✅ Ejecutar `flutter run` y ver la app corriendo
2. ⏳ Implementar el servicio de API (lib/services/api_service.dart)
3. ⏳ Crear LoginView
4. ⏳ Integrar con Node.js API desplegada

---

## 📚 Recursos Útiles

- **Flutter Docs**: https://flutter.dev/docs
- **Firebase for Flutter**: https://firebase.flutter.dev
- **GetX Documentation**: https://github.com/jonataslaw/getx
- **Google Maps Flutter**: https://pub.dev/packages/google_maps_flutter

---

**¡Éxito con el desarrollo de Logística Morales! 🚀**

