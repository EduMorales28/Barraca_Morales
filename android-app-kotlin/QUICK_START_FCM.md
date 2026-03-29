# ⚡ Quick Start - FCM en 10 Minutos

## 🎯 Objetivo
Que el conductor reciba notificación en Android cuando admin asigna pedido.

---

## Paso 1: Preparación Firebase (2 min)

### 1.1 Descargar google-services.json

```
1. Ir a https://console.firebase.google.com
2. Selecciona tu proyecto (o crea uno)
3. ⚙️ Configuración → Pestaña "General"
4. Sección "Aplicaciones" → Busca "Android"
5. Si no existe, click "Agregar app"
6. Descarga "google-services.json"
7. Copia a: /Users/eduardomorales/Desktop/PruebaAndroid/android-app-kotlin/
```

### 1.2 Obtener Server Key

```
De la misma consola:
⚙️ Configuración → Cloud Messaging
Copia "Server Key" (la larga terminada en =)
Guarda en un archivo: serverKey.txt
```

---

## Paso 2: Android Ready (1 min)

Los archivos ya están creados:
- ✅ ConductorFirebaseMessagingService.kt
- ✅ FCMTokenManager.kt
- ✅ FCMTokenSynchronizer.kt
- ✅ build.gradle.kts (actualizado)
- ✅ AndroidManifest.xml (actualizado)

### Compilar

```bash
cd /Users/eduardomorales/Desktop/PruebaAndroid
./gradlew clean build

# Espera: BUILD SUCCESSFUL ✅
```

---

## Paso 3: Backend (5 min)

### Opción A: Node.js + Express

```bash
cd tu-proyecto-backend
npm install firebase-admin
```

Crea `notificaciones.js`:

```javascript
const admin = require('firebase-admin');
const serviceAccount = require('./serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

exports.enviarNotificacion = async (conductorId, pedido) => {
  const conductor = await db.collection('conductores').doc(conductorId).get();
  const token = conductor.data().tokenFCM;

  const message = {
    notification: {
      title: `Pedido #${pedido.numero}`,
      body: `Cliente: ${pedido.cliente}`
    },
    data: {
      tipo: 'pedido_asignado',
      pedidoId: pedido.id
    },
    token: token
  };

  return admin.messaging().send(message);
};
```

Usa en tu endpoint de asignar:

```javascript
app.post('/api/pedidos/asignar', async (req, res) => {
  const { pedidoId, conductorId } = req.body;
  const pedido = { id: pedidoId, numero: '12345', cliente: 'Juan' };
  
  await require('./notificaciones').enviarNotificacion(conductorId, pedido);
  
  res.json({ success: true });
});
```

### Opción B: Python + Flask

```bash
pip install firebase-admin flask
```

Crea `app.py`:

```python
from firebase_admin import messaging, firestore
import firebase_admin
from firebase_admin import credentials

cred = credentials.Certificate('serviceAccountKey.json')
firebase_admin.initialize_app(cred)
db = firestore.client()

def enviar_notificacion(conductor_id, pedido):
    conductor = db.collection('conductores').document(conductor_id).get()
    token = conductor.to_dict()['tokenFCM']

    message = messaging.Message(
        notification=messaging.Notification(
            title=f"Pedido #{pedido['numero']}",
            body=f"Cliente: {pedido['cliente']}"
        ),
        data={'tipo': 'pedido_asignado', 'pedidoId': pedido['id']},
        token=token
    )

    return messaging.send(message)

from flask import Flask, request

app = Flask(__name__)

@app.route('/api/pedidos/asignar', methods=['POST'])
def asignar():
    data = request.json
    pedido = {'id': data['pedidoId'], 'numero': '12345', 'cliente': 'Juan'}
    enviar_notificacion(data['conductorId'], pedido)
    return {'success': True}

if __name__ == '__main__':
    app.run(port=3000)
```

### Opción C: cURL (sin SDK)

```bash
FIREBASE_SERVER_KEY="AAAA..."  # Tu server key
CONDUCTOR_TOKEN="eF8s9d..."   # Token del conductor

curl -X POST https://fcm.googleapis.com/fcm/send \
  -H "Authorization: key=$FIREBASE_SERVER_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "notification": {
      "title": "Pedido #12345",
      "body": "Cliente: Juan"
    },
    "data": {"tipo": "pedido_asignado", "pedidoId": "001"},
    "to": "'$CONDUCTOR_TOKEN'"
  }'
```

---

## Paso 4: Testing (2 min)

### 4.1 Iniciar emulador

```bash
emulator -avd Pixel_7_API_34 &    # O dejar abierto en Android Studio
```

### 4.2 Deploy app

```bash
flutter run -d android
```

### 4.3 Login en app

```
1. Abre app en emulador
2. Ingresa credenciales
3. Espera 2 segundos
4. Token debería estar en backend
```

### 4.4 Enviar notificación

```bash
# Ejemplo: obtener token del logcat
adb logcat | grep "Token"

# Copiar token que aparece: eF8s9d...

# Luego enviar:
bash scriptEnvio.sh  # O curl command arriba
```

### 4.5 Verificar

```
✅ ¿Aparece notificación en emulador?
✅ ¿Dice el nombre del cliente?
✅ ¿Al hacer tap abre la app?
```

---

## 🐛 Si no funciona

### Síntoma: "No aparece notificación"

```bash
# Ver logs Android
adb logcat | grep "FCM\|Notif\|Message"

# Esperado:
# I/ConductorFirebaseMessagingService: Mensaje recibido
# I/ConductorFirebaseMessagingService: Notificación mostrada
```

### Síntoma: "No se obtiene token"

```bash
# Compilaste con google-services.json?
ls -la android-app-kotlin/google-services.json

# Si no existe:
# ❌ Descargalo de Firebase Console
# ❌ Compila de nuevo
```

### Síntoma: "Backend no recibe token"

```bash
# ¿La URL está correcta?
# Para emulador: http://10.0.2.2:3000
# Para físico: http://192.168.1.x:3000
```

---

## 📋 Checklist de Éxito

```
ANDROID:
☐ google-services.json en proyecto
☐ App compila sin errores
☐ App abre sin crashes
☐ Logcat muestra "Token obtenido: eF8s..."

BACKEND:
☐ Backend corriendo (puerto 3000 u otro)
☐ Endpoint POST /conductor/{id}/token-fcm existe
☐ Recibe token y lo guarda
☐ Endpoint POST /api/pedidos/asignar existe

TESTING:
☐ Envías notificación vía curl / backend
☐ App recibe en ConductorFirebaseMessagingService
☐ Notificación visual aparece
☐ Al tocar, abre detalles del pedido
```

---

## 📚 Documentación Completa

Si necesitas más detalles:

- **Arquitectura completa**: [FCM_SETUP_COMPLETO.md](FCM_SETUP_COMPLETO.md)
- **Ejemplos backend**: [BACKEND_FCM_EJEMPLOS.md](BACKEND_FCM_EJEMPLOS.md)
- **Testing detallado**: [TESTING_INTEGRACION_FCM.md](TESTING_INTEGRACION_FCM.md)
- **Índice de archivos**: [INDICE_FCM.md](INDICE_FCM.md)

---

## ⏱️ Tiempo Total

```
Paso 1 (Firebase):    2 min
Paso 2 (Android):     1 min  (ya hecho ✅)
Paso 3 (Backend):     5 min
Paso 4 (Testing):     2 min
─────────────────
Total:               10 min
```

---

## 🎉 Una vez funcione

```
✅ Conductor recibe notificaciones en tiempo real
✅ Admin puede asignar desde panel
✅ App muestra detalles del pedido automáticamente
✅ Sistema de logística funcional end-to-end

Siguiente paso: 
→ Integrar más tipos de notificaciones
→ Agregar acciones (aceptar/rechazar)
→ Tracking en tiempo real
```

---

**Casa**: ⚡ Rápido y Funcional  
**Versión**: 1.0.0  
**Próxima lectura**: FCM_SETUP_COMPLETO.md para entender qué pasó
