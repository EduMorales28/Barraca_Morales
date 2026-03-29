# Configuración del Proyecto Android

## Requisitos Previos

- **Android Studio**: Flamingo (2023.2.1) o superior
- **JDK**: 11 o superior
- **Kotlin**: 1.8+
- **Gradle**: 8.0+
- **SDK Target**: 34 (Android 14)
- **Min SDK**: 26 (Android 8.0)

## Configuración Inicial

### 1. Google Maps API

Necesitas obtener una clave de API de Google Maps:

1. Ve a [Google Cloud Console](https://console.cloud.google.com/)
2. Crea un nuevo proyecto
3. Habilita la API de Google Maps
4. Crea una clave de API para Android
5. Reemplaza en `AndroidManifest.xml`:

```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="TU_CLAVE_AQUI" />
```

### 2. Configurar URL de API

En `ApiClient.kt`, reemplaza `BASE_URL`:

```kotlin
private const val BASE_URL = "http://tu-servidor.com/v1/"
```

### 3. Permisos en Runtime (Android 6+)

Para cámara y ubicación, solicita permisos dinámicamente:

```kotlin
val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION
val cameraPermission = Manifest.permission.CAMERA
```

Usa `ActivityResultContracts` para solicitar permisos:

```kotlin
val requestPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestPermission()
) { isGranted ->
    // Manejar resultado
}
```

## Compilar y Ejecutar

### Compilar Debug APK

```bash
cd android-app-kotlin
./gradlew assembleDebug
```

El APK se generará en: `app/build/outputs/apk/debug/`

### Ejecutar en Emulador

```bash
./gradlew installDebug
adb shell am start -n com.barraca.conductor/.MainActivity
```

### Ejecutar en Dispositivo Físico

1. Conecta el dispositivo vía USB
2. Habilita "Opciones de Desarrollador" y "Depuración USB"
3. Ejecuta:

```bash
./gradlew installDebug
```

## Estructura de Gradle

**build.gradle.kts** incluye:

- **Compile SDK**: 34
- **Target SDK**: 34
- **Min SDK**: 26
- **Compose Version**: 1.6.0
- **Kotlin Compiler Extension**: 1.5.3

### Agregar Nueva Dependencia

Edita `build.gradle.kts` y agrega en `dependencies`:

```gradle
implementation 'group:artifact:version'
```

Luego sincroniza:

```bash
./gradlew sync
```

## BuildTypes

### Debug

- Minify: deshabilitado
- Debuggable: true
- Útil para desarrollo y pruebas

### Release

- Minify: habilitado (ProGuard)
- Debuggable: false
- Listo para publicar en Play Store

## Archivos de Configuración

### local.properties

Genera automáticamente con tu SDK path:

```properties
sdk.dir=/path/to/android/sdk
```

### gradle.properties

Configuraciones de caché y memoria:

```properties
org.gradle.jvmargs=-Xmx4096m
```

## Debugging

### Logs

Usa Timber para logging:

```kotlin
Timber.d("Debug message")
Timber.e(exception, "Error message")
```

### Android Studio Debugger

1. Añade breakpoints (Shift+Cmd+F8)
2. Ejecuta en modo Debug
3. Inspecciona variables en tiempo real

### Logcat

```bash
adb logcat | grep "com.barraca.conductor"
```

## Publicación

### Generar Build Release

```bash
./gradlew bundleRelease
```

Se genera: `app/build/outputs/bundle/release/app-release.aab`

### Prerequisitos para Play Store

1. Cuenta de Google Play Developer ($25)
2. App signing key
3. Información de privacidad (GDPR)
4. Screenshots y descripciones

## Troubleshooting

### Error: "Compilation failed"

```bash
./gradlew clean
./gradlew build
```

### Error: "API Key invalid"

Verifica en AndroidManifest.xml la clave de Google Maps

### Error: "SDK not found"

Ejecuta Android Studio > Tools > SDK Manager y descarga el SDK necesario

### Error: "Gradle sync failed"

```bash
./gradlew --refresh-dependencies
```

## Environment Variables

### macOS

Agrega a `~/.zshrc`:

```bash
export ANDROID_SDK_ROOT=$HOME/Library/Android/sdk
export PATH=$PATH:$ANDROID_SDK_ROOT/platform-tools
export PATH=$PATH:$ANDROID_SDK_ROOT/tools
```

Ejecuta:

```bash
source ~/.zshrc
```

## Emulador

### Crear Emulador

```bash
android create avd --name "Conductor" --target android-34 --device "Pixel 4"
```

### Ejecutar Emulador

```bash
emulator -avd Conductor
```

### Listar Dispositivos

```bash
adb devices
```

---

Para más información, ver [Documentación Oficial de Android](https://developer.android.com/docs)
