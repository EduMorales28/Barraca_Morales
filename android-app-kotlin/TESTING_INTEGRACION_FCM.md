# 🚀 Testing & Integración FCM - Guía Paso a Paso

## 📋 Pre-requisitos

- [ ] Android Studio con proyecto abierto
- [ ] Emulador o dispositivo físico con Google Play Services
- [ ] Proyecto Firebase creado en console.firebase.google.com
- [ ] google-services.json descargado y colocado en `/android-app-kotlin/`
- [ ] Backend corriendo (local o remoto)

---

## Fase 1: Verificar Setup Android

### 1.1 Verificar google-services.json

```bash
# Desde raíz del proyecto
ls -la android-app-kotlin/google-services.json

# Debe mostrarse como:
# -rw-r--r--  ... google-services.json

# Si NO existe:
# ❌ Descargalo de Firebase Console
# ❌ Cópialo a android-app-kotlin/
```

### 1.2 Verificar Dependencies en build.gradle.kts

```kotlin
// Buscar estas líneas (Build → Build Variants o abrir archivo):
plugins {
    id 'com.google.gms.google-services'  // ← DEBE EXISTIR
}

dependencies {
    // Firebase dependencies:
    implementation 'com.google.firebase:firebase-bom:32.7.0'
    implementation 'com.google.firebase:firebase-messaging-ktx'
    implementation 'com.google.firebase:firebase-auth-ktx'
    implementation 'com.google.firebase:firebase-firestore-ktx'
}
```

### 1.3 Verificar AndroidManifest.xml

```xml
<!-- En <manifest> level -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- En <application> -->
<service
    android:name=".services.ConductorFirebaseMessagingService"
    android:exported="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>
```

### 1.4 Compilar Proyecto

```bash
# Option 1: Android Studio
Build → Build Project (Ctrl+F9)

# Option 2: Terminal
cd /Users/eduardomorales/Desktop/PruebaAndroid
./gradlew clean build

# Debe mostrar:
# ✅ BUILD SUCCESSFUL in Xs
```

---

## Fase 2: Testing en Emulador/Dispositivo

### 2.1 Iniciar Emulador

```bash
# Si tienes emulador configurado:
emulator -avd Pixel_7_API_34 &

# O usar desde Android Studio: AVD Manager
```

### 2.2 Deploy App

```bash
# Opción 1: Android Studio
Run → Run 'app'

# Opción 2: Terminal
cd /Users/eduardomorales/Desktop/PruebaAndroid
flutter run -d android

# Esperar a que aparezca en Logcat
```

### 2.3 Verificar Token en Logcat

```bash
# Terminal separada:
adb logcat | grep -i "fcm\|token\|notif"

# Esperado después de login:
# I/FCMTokenManager: Token obtenido: eF8s9d...
# I/FCMTokenSynchronizer: Sincronizando token...
# I/FCMTokenSynchronizer: Token sincronizado exitosamente
```

**Si ves error "FCM: Provider not installed":**
- [ ] Configurar Google Play Services en emulador
- [ ] O usar dispositivo físico con Play Services

---

## Fase 3: Prueba de Envío Manual

### 3.1 Obtener Firebase Server Key

1. Ve a https://console.firebase.google.com
2. Tu Proyecto → Configuración ⚙️
3. Pestaña "Cloud Messaging"
4. Copia "Server Key" (la larga)

```bash
# Copiar a variable
FIREBASE_KEY="AAAA..."
```

### 3.2 Obtener Token del Conductor

**Opción A: Desde Logcat**

```bash
# Después de loguear en app:
adb logcat | grep "Token obtenido"

# Output:
# I/FCMTokenManager: Token obtenido: eF8s9d...
```

Copia ese token: `eF8s9d...`

**Opción B: Desde DatabaseConsole (si usas Firestore)**

1. Firebase Console → Firestore Database
2. Collection: `conductores`
3. Document: `{conductorId}`
4. Campo: `tokenFCM` → copiar valor

**Opción C: Desde Backend (si tienes endpoint que devuelve token)**

```bash
curl -X GET http://localhost:3000/api/conductor/001
# Respuesta:
# {
#   "id": "001",
#   "nombre": "Juan",
#   "tokenFCM": "eF8s9d..."
# }
```

### 3.3 Enviar Notificación de Prueba

```bash
#!/bin/bash

# Reemplazar estos valores:
FIREBASE_SERVER_KEY="AAAA...copied..."
CONDUCTOR_TOKEN="eF8s9d...copied..."

curl -X POST "https://fcm.googleapis.com/fcm/send" \
  -H "Authorization: key=$FIREBASE_SERVER_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "notification": {
      "title": "🔔 Test FCM - Éxito",
      "body": "Notificación de prueba"
    },
    "data": {
      "tipo": "test",
      "mensaje": "Funciona!"
    },
    "to": "'$CONDUCTOR_TOKEN'"
  }'

# Respuesta esperada:
# {
#   "multicast_id": 8123456789,
#   "success": 1,
#   "failure": 0,
#   "canonical_ids": 0,
#   "results": [
#     {
#       "message_id": "0:1234567890@fcm.googleapis.com"
#     }
#   ]
# }
```

### 3.4 Verificar en App

**En Emulador/Dispositivo:**
- [ ] ¿Aparece notificación en la barra de notificaciones?
- [ ] ¿Dice "Test FCM - Éxito"?
- [ ] ¿Al hacer tap, abre la app?

**En Logcat:**
```bash
adb logcat | grep "ConductorFirebaseMessagingService"

# Esperado:
# I/ConductorFirebaseMessagingService: Mensaje recibido: {tipo=test}
# I/ConductorFirebaseMessagingService: Notificación mostrada
```

---

## Fase 4: Prueba con Backend Real

### 4.1 Verificar Endpoint Backend

```bash
# Verificar que tu backend tiene el endpoint
curl -X GET http://localhost:3000/api/health

# Si responde, está corriendo ✅
# Si no responde, iniciar backend (nodejs/python)

# Ejemplo Node.js:
node server.js
# Output: "Servidor escuchando en puerto 3000"

# Ejemplo Python:
python app.py
# Output: "Running on http://localhost:5000"
```

### 4.2 Verificar Endpoint Token

```bash
# Endpoint para recibir tokens
curl -X POST "http://localhost:3000/api/conductor/001/token-fcm" \
  -H "Content-Type: application/json" \
  -d '{
    "token": "eF8s9d..."
  }'

# Respuesta esperada:
# {"success": true}
```

### 4.3 Configurar Backend en App

Editar [ConductorApiService.kt](ConductorApiService.kt):

```kotlin
// Buscar esta línea:
private const val BASE_URL = "http://10.0.2.2:3000/"  // Para emulador
// o
private const val BASE_URL = "http://192.168.1.x:3000/"  // Para dispositivo físico
```

**Nota para emulador:**
- `http://10.0.2.2:3000` = localhost del host desde emulador
- `http://localhost:3000` NO funciona en emulador

**Nota para dispositivo físico:**
- `http://192.168.1.x:3000` = IP de tu máquina en red local
- Encuentra tu IP: `ipconfig getifaddr en0` (macOS)

### 4.4 Flujo Completo de Prueba

**Paso 1: Iniciar Stack**

```bash
# Terminal 1: Backend
cd tu-proyecto-backend
npm start
# Output: "Servidor en puerto 3000"

# Terminal 2: Emulador
emulator -avd Pixel_7_API_34 &

# Terminal 3: App Android
cd /Users/eduardomorales/Desktop/PruebaAndroid
flutter run -d android
```

**Paso 2: Login en App**

1. Abre app en emulador
2. Ingresa credenciales
3. Espera a que sincronice token (~2 segundos)
4. Verifica en Logcat:
```bash
adb logcat | grep "FCMToken"
# I/FCMTokenSynchronizer: Token sincronizado exitosamente
```

**Paso 3: Asignar Pedido Desde Backend**

```bash
# Opción A: Desde Terminal
curl -X POST "http://localhost:3000/api/pedidos/asignar" \
  -H "Content-Type: application/json" \
  -d '{
    "pedidoId": "001",
    "conductorId": "123",
    "numero": "12345",
    "cliente": "Juan García",
    "direccion": "Calle Principal 123"
  }'

# Opción B: Desde Panel Admin (si tiene interfaz)

# Opción C: Desde Postman
# POST http://localhost:3000/api/pedidos/asignar
# Body JSON: {...}
```

**Paso 4: Verificar Notificación**

Emulador:
- [ ] ¿Aparece notificación en barra?
- [ ] ¿Dice "Nueva Asignación - Pedido #12345"?
- [ ] ¿Toque abre app con detalles del pedido?

Logcat:
```bash
adb logcat | grep "ConductorFirebaseMessagingService"
# I/ConductorFirebaseMessagingService: Procesando pedido_asignado
# I/ConductorFirebaseMessagingService: Notificación mostrada
```

---

## 🐛 Troubleshooting

### Síntoma: "No aparece notificación"

**Check List:**

```
1. ¿Compiló sin errores?
   → adb logcat | grep "error"
   
2. ¿Token generator funciona?
   → adb logcat | grep "FCMTokenManager"
   → Debe mostrar: "Token obtenido: eF8s9d..."
   
3. ¿Backend recibe token?
   → Ver logs del backend
   → Buscar: "Token recibido: eF8s8d..."
   
4. ¿Backend enviando notificación?
   → Ver logs del backend
   → Buscar: "Notificación enviada:"
   
5. ¿FCM entregó al dispositivo?
   → Usar Firebase Console → Messaging → Diagnostics
   → O ver response del curl (success: 1)
   
6. ¿Service registrado en Manifest?
   → Android Studio → AndroidManifest.xml
   → Buscar: <service name="ConductorFirebaseMessagingService"
   → Debe existir con <intent-filter action="com.google.firebase.MESSAGING_EVENT"
```

### Síntoma: "error: Provider not installed"

```
Causa: Google Play Services no en emulador

Soluciones:

1. Descargar Play Services (Android Studio):
   → AVD Manager → Editar emulador
   → Google APIs incluir

2. Usar dispositivo físico real
   → More reliable para testing FCM

3. Usar imagen con Play Services incluida
   → "Google APIs Intel x86 Atom System Image"
```

### Síntoma: "No puede conectar a backend"

```
Causa: URL incorrecta en ConductorApiService

Para emulador:
- BASE_URL debe ser: "http://10.0.2.2:3000/"
- http://localhost NO funciona

Para dispositivo físico:
- BASE_URL debe ser: "http://192.168.1.x:3000/"
- Encuentra 192.168.1.x con: ipconfig getifaddr en0

Verificar:
adb logcat | grep "Request"
→ Debe mostrar URL que está usando
```

### Síntoma: "google-services.json: file not found"

```
Solución:
1. Descargar de Firebase Console
2. Copiar a: /android-app-kotlin/google-services.json
3. Verificar en terminal:
   ls -la android-app-kotlin/google-services.json
4. Clean + Rebuild:
   ./gradlew clean build
```

---

## ✅ Validación Final

```bash
# Script para verificar todo

#!/bin/bash

echo "🔍 Verificando FCM Setup..."
echo ""

# 1. google-services.json
echo "1️⃣  google-services.json:"
if [ -f "android-app-kotlin/google-services.json" ]; then
    echo "   ✅ Presente"
else
    echo "   ❌ No existe - descargalo de Firebase Console"
fi

# 2. build.gradle.kts
echo ""
echo "2️⃣  build.gradle.kts (google-services plugin):"
if grep -q "com.google.gms.google-services" android-app-kotlin/build.gradle.kts; then
    echo "   ✅ Plugin presente"
else
    echo "   ❌ Agregar: id 'com.google.gms.google-services'"
fi

# 3. Firebase dependencies
echo ""
echo "3️⃣  Firebase dependencies:"
if grep -q "firebase-messaging" android-app-kotlin/build.gradle.kts; then
    echo "   ✅ firebase-messaging presente"
else
    echo "   ❌ Agregar firebase-messaging"
fi

# 4. AndroidManifest.xml
echo ""
echo "4️⃣  AndroidManifest.xml:"
if grep -q "POST_NOTIFICATIONS" android-app-kotlin/src/main/AndroidManifest.xml; then
    echo "   ✅ Permiso POST_NOTIFICATIONS presente"
else
    echo "   ❌ Agregar permiso POST_NOTIFICATIONS"
fi

if grep -q "ConductorFirebaseMessagingService" android-app-kotlin/src/main/AndroidManifest.xml; then
    echo "   ✅ Servicio ConductorFirebaseMessagingService registrado"
else
    echo "   ❌ Registrar ConductorFirebaseMessagingService"
fi

# 5. Archivos de código
echo ""
echo "5️⃣  Archivos Kotlin:"
for file in "ConductorFirebaseMessagingService.kt" "FCMTokenManager.kt" "FCMTokenSynchronizer.kt"; do
    if [ -f "android-app-kotlin/src/main/kotlin/com/example/logistica_morales/services/$file" ]; then
        echo "   ✅ $file"
    else
        echo "   ❌ $file no existe"
    fi
done

echo ""
echo "==========================================="
echo "Si todos están ✅, estás listo para testing"
echo "==========================================="
```

---

## 🎯 Testing Checklist

```
ANTES DE COMPILAR:
☐ google-services.json descargado y colocado
☐ build.gradle.kts con google-services plugin
☐ Firebase dependencies en build.gradle.kts
☐ POST_NOTIFICATIONS permission en Manifest
☐ ConductorFirebaseMessagingService registrado
☐ 3 archivos Kotlin creados (ConductorFirebaseMessagingService, FCMTokenManager, FCMTokenSynchronizer)

DESPUÉS DE COMPILAR:
☐ App compila sin errores
☐ App se instala en emulador/dispositivo
☐ App abre sin crashes

DURANTE TESTING:
☐ Usuario puede loguear
☐ Token se obtiene de Firebase (ver en Logcat)
☐ Token se sincroniza a backend (endpoint 200 OK)
☐ Backend puede obtener token (BD o Firestore)
☐ Notificación se envía desde backend
☐ App recibe en ConductorFirebaseMessagingService
☐ Notificación visual aparece en dispositivo
☐ Usuario puede hacer tap en notificación
☐ App abre detalles del pedido correcto

INTEGRACIÓN COMPLETA:
☐ Admin panel asigna pedido
☐ Backend automáticamente envía notificación
☐ Conductor recibe sin hacer nada
☐ Notificación es en tiempo real (< 5 segundos)
```

---

**Estado**: ✅ Testing Ready  
**Última actualización**: Marzo 2026  
**Versión**: 1.0.0
