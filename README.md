# LogisticaMorales

Aplicación Flutter para gestión de logística con autenticación, mapas, navegación multi-pantalla, gestión de estado e integración con API.

## Características

- ✅ Autenticación con Firebase
- ✅ Integración con Google Maps
- ✅ Gestión de estado con GetX
- ✅ Navegación multi-pantalla
- ✅ API REST con Dio
- ✅ Base de datos Firestore
- ✅ Almacenamiento en Firebase Storage

## Requisitos Previos

- Flutter 3.11.4 o superior
- Dart 3.11.4 o superior
- Android SDK (para compilar apps Android)
- Emulador Android o dispositivo físico

## Instalación

### 1. Clonar o descargar el proyecto
```bash
cd /Users/eduardomorales/Desktop/PruebaAndroid
```

### 2. Instalar dependencias
```bash
source ~/.zshrc
flutter pub get
```

### 3. Ejecutar el proyecto
```bash
flutter run -d android
```

## Estructura del Proyecto

```
lib/
├── config/
│   ├── app_bindings.dart       # Inyección de dependencias con GetX
│   └── routes.dart              # Rutas de navegación
├── controllers/                 # Controladores GetX (lógica)
├── models/                      # Modelos de datos
│   └── user_model.dart
├── services/                    # Servicios (API, Firebase, etc.)
│   └── auth_service.dart
├── views/                       # Vistas (pantallas)
│   └── home_view.dart
├── widgets/                     # Componentes reutilizables
├── utils/                       # Funciones auxiliares
├── firebase_options.dart        # Configuración de Firebase
└── main.dart                    # Punto de entrada
```

## Tareas de VS Code

Las siguientes tareas están disponibles en el editor:

- **Flutter: Pub Get** - Instalar/actualizar dependencias
- **Flutter: Analyze** - Analizar código
- **Flutter: Build Debug APK** - Compilar APK de debug
- **Flutter: Run Android** - Ejecutar en Android
- **Flutter: Clean** - Limpiar build

## Configuración de Firebase

⚠️ **IMPORTANTE**: Actualizar `lib/firebase_options.dart` con tus credenciales de Firebase:

1. Ir a [Firebase Console](https://console.firebase.google.com/)
2. Crear un nuevo proyecto o usar uno existente
3. Agregar app Android
4. Descargar `google-services.json` en `android/app/`
5. Copiar las credenciales en `firebase_options.dart`

## Dependencias Principales

- **get**: Gestión de estado y navegación
- **firebase_core**: SDK de Firebase
- **firebase_auth**: Autenticación
- **cloud_firestore**: Base de datos
- **google_maps_flutter**: Mapas
- **geolocator**: Geolocalización
- **dio**: Networking HTTP
- **provider**: Gestión de estado alternativa
- **shared_preferences**: Almacenamiento local

## Desarrollo

### Agregar nueva pantalla

1. Crear archivo en `lib/views/`
2. Crear controlador en `lib/controllers/`
3. Agregar ruta en `lib/config/routes.dart`
4. Registrar en `AppBindings` si es necesario

### Agregar novo modelo

1. Crear archivo en `lib/models/`
2. Incluir conversión JSON (fromJson, toJson)

## Testing

Para ejecutar pruebas:
```bash
flutter test
```

## Compilación a Producción

### Compilar APK de Release
```bash
flutter build apk --release
```

### Compilar Bundle de Android
```bash
flutter build appbundle --release
```

## Documentación Adicional

- [Flutter Documentation](https://docs.flutter.dev/)
- [GetX Package](https://pub.dev/packages/get)
- [Firebase Flutter](https://firebase.google.com/docs/flutter/setup)
- [Google Maps Flutter](https://pub.dev/packages/google_maps_flutter)

## Licencia

Proyecto privado de LogisticaMorales

## Contacto

Para preguntas o soporte, contactar al equipo de desarrollo.
