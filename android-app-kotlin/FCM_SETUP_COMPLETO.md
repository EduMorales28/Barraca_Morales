# 🔔 Firebase Cloud Messaging (FCM) - Guía Completa

## Visión General

**Firebase Cloud Messaging (FCM)** permite enviar notificaciones push desde tu backend a los conductores Android.

```
Admin Panel          Backend API               Android Conductor
    │                    │                          │
    ├─ Asignar Pedido ─→ Envia a FCM ──────────→  Recibe notificación
    │                    │                          │
    └─ Llamada API       └─ Almacena token ────→  Recibe en app
```

---

## 🏗️ Arquitectura

### Flujo Completo (Happy Path)

```
1. Conductor instala app
   │
   ├→ FCM obtiene token único (ej: "eF8s9d..." - 200+ caracteres)
   │
   ├→ App envía token al backend
   │
   ├→ Backend almacena en BD: conductor → token FCM
   │
   
2. Admin asigna pedido
   │
   ├→ Backend recibe asignación
   │
   ├→ Backend busca: conductor_id → token_fcm
   │
   ├→ Backend llama: FCM API.sendMessage(token, notificación)
   │
   ├→ FCM entrega a dispositivo
   │
   ├→ App recibe en ConductorFirebaseMessagingService
   │
   └→ Conductor ve notificación

3. Conductor toca notificación
   │
   ├→ App abre MainActivity
   │
   └→ Navega a PedidoDetailScreen automáticamente
```

---

## 📦 Componentes Android (Ya Implementados)

### 1. ConductorFirebaseMessagingService.kt
- Recibe todas las notificaciones FCM
- Procesa según tipo (asignación, actualización, alerta)
- Muestra notificación

### 2. FCMTokenManager.kt
- Obtiene token FCM del dispositivo
- Guarda localmente
- Suscribe/desuscribe de topics

### 3. FCMTokenSynchronizer.kt
- Sincroniza el token con el backend
- Verifica si es nuevo antes de enviar
- Manda al endpoint `/conductor/{id}/token-fcm`

### 4. ConductorApiService
- Endpoint: `POST /conductor/{conductorId}/token-fcm`
- Recibe: `{ "token": "eF8s9d..." }`
- Backend lo almacena

---

## 🔧 Setup Android (Paso a Paso)

### Paso 1: Agregar Google Play Services Plugin

✅ Ya agregado en `build.gradle.kts`:
```gradle
id 'com.google.gms.google-services'
```

### Paso 2: Agregar Firebase Dependencies

✅ Ya agregadas en `build.gradle.kts`:
```gradle
implementation platform('com.google.firebase:firebase-bom:32.7.0')
implementation 'com.google.firebase:firebase-messaging-ktx'
implementation 'com.google.firebase:firebase-auth-ktx'
implementation 'com.google.firebase:firebase-firestore-ktx'
```

### Paso 3: Descargar google-services.json

1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Crea proyecto (o usa existente)
3. Agrega app Android
4. Descarga `google-services.json`
5. Colócalo en: `android-app-kotlin/` (raíz)

⚠️ **Importante**: Sin este archivo, el proyecto **no compilará**

### Paso 4: Compilar

```bash
cd android-app-kotlin
./gradlew build
```

Debería compilar sin errores si google-services.json está presente.

### Paso 5: Sincronizar Token Después de Login

En tu `LoginViewModel` o `MainActivity`, después de login exitoso:

```kotlin
val tokenManager = FCMTokenManager(context)
val tokenSync = FCMTokenSynchronizer(context, apiService)

// Obtener y sincronizar token
tokenSync.sincronizarToken(conductorId)
```

---

## 📱 Android - Recibiendo Notificaciones

### Cuando llega una notificación:

1. **App Cerrada (Background)**
   - FCM muestra notificación automáticamente
   - Usuario toca → abre MainActivity
   - Intent extra: `pedidoId` y `accion`

2. **App Abierta (Foreground)**
   - `ConductorFirebaseMessagingService.onMessageReceived()` se ejecuta
   - Opción 1: Mostrar como notificación normal
   - Opción 2: Actualizar UI en tiempo real
   - Opción 3: Ambas

### Ejemplos de Mensajes

#### Opción A: Notificación (Simple)
```json
{
  "notification": {
    "title": "Nuevo Pedido Asignado",
    "body": "Se te asignó pedido #12345"
  },
  "to": "eF8s9d..."  // Token del conductor
}
```

#### Opción B: Datos (Personalizado)
```json
{
  "data": {
    "tipo": "pedido_asignado",
    "pedidoId": "001",
    "titulo": "Nuevo Pedido",
    "cuerpo": "Se te asignó pedido #12345"
  },
  "to": "eF8s9d..."
}
```

#### Opción C: Ambos (Híbrido - Recomendado)
```json
{
  "notification": {
    "title": "Nuevo Pedido",
    "body": "Pedido #12345 asignado"
  },
  "data": {
    "tipo": "pedido_asignado",
    "pedidoId": "001",
    "cliente": "Juan García"
  },
  "to": "eF8s9d..."
}
```

---

## 🔌 Backend - Enviando Notificaciones

### Requisitos

1. **Token del conductor** (obtenido del endpoint `/conductor/{id}/token-fcm`)
2. **Firebase Server Key** (de Firebase Console)
3. **Librería FCM** (admin-sdk)

### Node.js (Express + firebase-admin)

```bash
npm install firebase-admin
```

**Código**:

```javascript
const admin = require('firebase-admin');
const serviceAccount = require('./firebase-service-account-key.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

async function enviarNotificacionPedido(conductorToken, numero_pedido, cliente) {
  const message = {
    notification: {
      title: 'Nueva Asignación de Pedido',
      body: `Pedido #${número_pedido} - ${cliente}`
    },
    data: {
      tipo: 'pedido_asignado',
      pedidoId: pedido_id,
      cliente: cliente,
      timestamp: Date.now().toString()
    },
    token: conductorToken,
    android: {
      priority: 'high',
      notification: {
        sound: 'default',
        channelId: 'pedidos_canal'
      }
    }
  };

  try {
    const response = await admin.messaging().send(message);
    console.log('Notificación enviada:', response);
    return response;
  } catch (error) {
    console.error('Error enviando notificación:', error);
    throw error;
  }
}

// Uso:
app.post('/api/pedidos/asignar', async (req, res) => {
  const { conductorId, pedidoId, cliente } = req.body;

  try {
    // 1. Obtener token del conductor
    const conductor = await db.collection('conductores').doc(conductorId).get();
    const token = conductor.data().tokenFCM;

    // 2. Enviar notificación
    await enviarNotificacionPedido(token, pedidoId, cliente);

    // 3. Guardar en BD, actualizar estado, etc.
    // ...

    res.json({ success: true });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});
```

### Python (Flask + firebase-admin)

```bash
pip install firebase-admin
```

**Código**:

```python
import firebase_admin
from firebase_admin import credentials, messaging
from flask import Flask, request, jsonify

app = Flask(__name__)

# Inicializar Firebase
cred = credentials.Certificate('firebase-service-account-key.json')
firebase_admin.initialize_app(cred)

def enviar_notificacion_pedido(conductor_token, numero_pedido, cliente):
    message = messaging.Message(
        notification=messaging.Notification(
            title='Nueva Asignación de Pedido',
            body=f'Pedido #{numero_pedido} - {cliente}'
        ),
        data={
            'tipo': 'pedido_asignado',
            'pedidoId': numero_pedido,
            'cliente': cliente
        },
        token=conductor_token,
        android=messaging.AndroidConfig(
            priority='high',
            notification=messaging.AndroidNotification(
                sound='default',
                channel_id='pedidos_canal'
            )
        )
    )

    try:
        response = messaging.send(message)
        print(f'Notificación enviada: {response}')
        return response
    except Exception as e:
        print(f'Error: {str(e)}')
        raise

@app.route('/api/pedidos/asignar', methods=['POST'])
def asignar_pedido():
    data = request.json
    conductor_id = data.get('conductorId')
    pedido_id = data.get('pedidoId')
    cliente = data.get('cliente')

    try:
        # 1. Obtener token del conductor
        from firebase_admin import firestore
        db = firestore.client()
        doc = db.collection('conductores').document(conductor_id).get()
        token = doc.get('tokenFCM')

        # 2. Enviar notificación
        enviar_notificacion_pedido(token, pedido_id, cliente)

        # 3. Guardar en BD, actualizar estado, etc.
        # ...

        return jsonify({'success': True}), 200
    except Exception as e:
        return jsonify({'error': str(e)}), 500

if __name__ == '__main__':
    app.run(debug=True)
```

### Usando REST API (Sin SDK)

```bash
curl -X POST https://fcm.googleapis.com/fcm/send \
  -H "Authorization: key=TU_SERVER_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "notification": {
      "title": "Nueva Asignación",
      "body": "Pedido #12345"
    },
    "data": {
      "tipo": "pedido_asignado",
      "pedidoId": "001"
    },
    "to": "eF8s9d..."
  }'
```

---

## 🗄️ Base de Datos - Guardar Tokens

### MongoDB

```javascript
// Esquema conductor
{
  _id: ObjectId(),
  nombre: "Juan García",
  email: "juan@example.com",
  estado: "activo",
  tokenFCM: "eF8s9d...",              // ← Token FCM
  ultimaActualizacionToken: Date(),
  topics: ["pedidos_caba", "pedidos_zona1"]
}

// Índices para búsqueda rápida
db.conductores.createIndex({ "tokenFCM": 1 })
```

### PostgreSQL

```sql
CREATE TABLE conductores (
    id UUID PRIMARY KEY,
    nombre VARCHAR(255),
    email VARCHAR(255),
    token_fcm VARCHAR(500),               -- ← Token FCM
    ultima_actualizacion_token TIMESTAMP,
    estado VARCHAR(50)
);

CREATE INDEX idx_token_fcm ON conductores(token_fcm);
```

### Firebase Firestore

```javascript
// Documento: conductores/{conductorId}
{
  nombre: "Juan García",
  email: "juan@example.com",
  estado: "activo",
  tokenFCM: "eF8s9d...",
  ultimaActualizacionToken: Timestamp.now(),
  topics: ["pedidos_caba"]
}
```

---

## 📋 Flujo Completo: De Admin a Conductor

### 1. Conductor Inicia Sesión

```
App         Backend         Firebase
│             │                │
├─ login ────→│                │
│             ├─ check creds   │
│             ├─ auth OK       │
│             │                │
│             ├─ (pendiente)   │
│             │  token FCM     │
│             │                │
└─ recibe token
   │
   ├─ obtiene token FCM
   │
   ├─ POST /conductor/123/token-fcm
   │      { "token": "eF8s9d..." }
   │
   └─ Backend guarda en BD
```

### 2. Admin Asigna Pedido

```
Panel Admin      Backend      Firebase      Conductor App
     │             │            │                │
     ├─ Asignar ──→│            │                │
     │             ├─ Buscar    │                │
     │             │  conductor │                │
     │             ├─ Leer BD   │                │
     │             │  token     │                │
     │             │                             │
     │             ├─ Prepare mensaje ─────────→│
     │             │                             ├─ onMessageReceived()
     │             │             ←─────────────←│ (FCM Service)
     │             ├─ log "enviado"             │
     │             │                          ┌─┴─┐
     │             │                          │   │
     │             │                       (muestra notificación)
     │             │                          │
     └─────────────┴────────────────────────────┘
         (Respuesta OK)
```

### 3. Conductor Toca Notificación

```
Notificación [Nuevo Pedido]
    │
    └─ Click
        │
        └─ Intent a MainActivity
            │
            └─ Intent extra: "pedidoId" = "001"
                │
                └─ MainActivity navega a PedidoDetailScreen
                    │
                    ├─ Carga detalles del pedido
                    ├─ Muestra ubicación, artículos, etc.
                    └─ Conductor ve información completa
```

---

## 🔐 Seguridad

### Firebase Console (Admin)

1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Proyecto → Configuración → Cuentas de Servicio
3. Descarga JSON con credenciales
4. **⚠️ Nunca compartir este archivo**

### API Keys

```
Backend Server Key (usado para enviar)
     ├─ Privado ✓
     └─ Solo en servidor
     
Android API Key (usado por cliente para analytics, etc)
     ├─ Público (está en código del app)
     └─ Pero restringida a:
        - Solo Android
        - Certificado SHA-1 específico
```

### Validación de Tokens

En Android, cuando guardas el token:

```kotlin
// ✅ Correcto: El token viene de FCM, es seguro
val token = FirebaseMessaging.getInstance().token
```

No confundir con:
- JWT tokens (para auth) - 1000+ caracteres
- FCM tokens - 200+ caracteres, parecen números/letras

---

## 🧪 Testing

### Test 1: Obtener Token FCM

```kotlin
// En tu app, en MainActivity o después de login
val tokenManager = FCMTokenManager(context)
CoroutineScope(Dispatchers.IO).launch {
    val token = tokenManager.obtenerToken()
    Log.d("FCM", "Token: $token")
    // Ve a Logcat y busca "Token:"
}
```

### Test 2: Guardar en Preferencias

```kotlin
val prefs = context.getSharedPreferences("fcm_preferences", Context.MODE_PRIVATE)
val token = prefs.getString("fcm_token_key", null)
Log.d("FCM", "Token guardado: $token")
```

### Test 3: Enviar Notificación Manual (Firebase Console)

1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Proyecto → Engagement → Cloud Messaging
3. "Send first message"
4. Título: "Test", Cuerpo: "Esto es un test"
5. App: "logistica_morales" (tu app)
6. Enviar
7. Debería llegar a todos los dispositivos

### Test 4: Enviar desde Node.js

```javascript
// node send-test.js (con código Node.js arriba)
// Debería ver: "Notificación enviada: abc123..."
```

---

## 📊 Monitoreo

### Firebase Console - Estadísticas

```
Engagement → Cloud Messaging
    ├─ Mensajes enviados: XXX
    ├─ Tasa de recepción: XX%
    ├─ Dispositivos alcanzados: XXX
    └─ Errores: X
```

### Backend Logs

```
[2026-03-28 14:32:10] Notificación enviada: b32d9f... → conductor 123
[2026-03-28 14:32:11] FCM response: success
[2026-03-28 14:32:12] Token actualizado: conductor 123
```

### Android Logs

```
D/FCMService: Mensaje recibido
D/FCMService: Notificación mostrada - Nuevo Pedido
D/FCMTokenManager: Token obtenido: eF8s9d...
```

---

## ⚠️ Troubleshooting

| Problema | Causa | Solución |
|----------|-------|----------|
| FCM no recibe | google-services.json falta | Descargar de Firebase Console |
| Token null | Firebase no inicializado | Asegurar build.gradle tiene FCM dep |
| Notificación no llega | Token incorrecto en BD | Verificar endpoint `/token-fcm` funciona |
| App no abre | Intent mal configurado | Verificar MainActivity recibe Intent extras |
| Rate limiting | Muchos mensajes rápido | Esperar, FCM tiene límites |

---

## 🎯 Casos de Uso Implementados Aquí

✅ Envío cuando se asigna un pedido
✅ Recepción en Android
✅ Muestra notificación
✅ Abre app cuando toca
✅ Navega a pedido automáticamente

---

## 📚 Documentación Adicional

- [Firebase Admin SDK](https://firebase.google.com/docs/admin/setup)
- [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging)
- [FCM en Android](https://developer.android.com/google/firebase/cloud-messaging)

---

**Estado**: ✅ Production Ready  
**Última actualización**: Marzo 2026  
**Versión**: 1.0.0
