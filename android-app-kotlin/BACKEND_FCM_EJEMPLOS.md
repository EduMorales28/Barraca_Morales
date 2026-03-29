# 📤 Backend: Enviar Notificaciones FCM - Guía Implementación

## Resumen Rápido

tu backend necesita:
1. **Librería FCM** (firebase-admin SDK)
2. **Credenciales Firebase** (google-services.json equivalente)
3. **Token del conductor** (guardado cuando se sincroniza en Android)
4. **Lógica de envío** (cuando se asigna un pedido)

---

## Paso 1: Obtener Credenciales Firebase

### Opción A: Cuenta de Servicio (Para Backend)

1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Tu Proyecto → Configuración ⚙️
3. "Cuentas de Servicio" tab
4. "Generar nueva clave privada"
5. Se descarga: `serviceAccountKey.json`
6. Coloca en tu backend (git ignore!)

```json
// serviceAccountKey.json (NUNCA compartir)
{
  "type": "service_account",
  "project_id": "tu-proyecto",
  "private_key_id": "...",
  "private_key": "-----BEGIN PRIVATE KEY-----\n...",
  "client_email": "firebase-adminsdk-xxx@tu-proyecto.iam.gserviceaccount.com",
  "client_id": "...",
  "auth_uri": "https://accounts.google.com/o/oauth2/auth",
  ...
}
```

---

## Paso 2: Instalar SDK en tu Backend

### Node.js + Express

```bash
npm install firebase-admin express
```

### Python + Flask

```bash
pip install firebase-admin flask
```

### Java + Spring Boot

```gradle
implementation 'com.google.firebase:firebase-admin:9.2.0'
```

---

## 🔔 EJEMPLOS LISTOS PARA COPIAR

### Node.js (Express)

```javascript
// firebase-config.js
const admin = require('firebase-admin');
const serviceAccount = require('./serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: 'https://tu-proyecto.firebaseio.com'
});

module.exports = admin;

// ==========================================

// notificaciones.js
const admin = require('./firebase-config');
const db = admin.firestore();

/**
 * Enviar notificación cuando se asigna un pedido
 */
async function enviarNotificacionPedidoAsignado(conductorId, pedido) {
  try {
    // 1. Obtener token FCM del conductor
    const conductorDoc = await db.collection('conductores').doc(conductorId).get();
    const conductorData = conductorDoc.data();
    
    if (!conductorData) {
      console.error(`Conductor ${conductorId} no encontrado`);
      return { success: false, error: 'Conductor no encontrado' };
    }

    const token = conductorData.tokenFCM;
    if (!token) {
      console.error(`Token FCM no encontrado para conductor ${conductorId}`);
      return { success: false, error: 'Token FCM no disponible' };
    }

    // 2. Preparar mensaje
    const message = {
      notification: {
        title: `Nueva Asignación - Pedido #${pedido.numero}`,
        body: `Cliente: ${pedido.cliente}`
      },
      data: {
        tipo: 'pedido_asignado',
        pedidoId: pedido.id,
        numero: pedido.numero,
        cliente: pedido.cliente,
        direccion: pedido.direccion,
        monto: pedido.montoTotal.toString(),
        timestamp: Date.now().toString()
      },
      android: {
        priority: 'high',
        notification: {
          sound: 'default',
          channelId: 'pedidos_canal',
          clickAction: 'FLUTTER_NOTIFICATION_CLICK'
        }
      },
      token: token
    };

    // 3. Enviar mediante FCM
    const response = await admin.messaging().send(message);
    console.log(`Notificación enviada a ${conductorId}: ${response}`);

    return { success: true, messageId: response };
  } catch (error) {
    console.error(`Error enviando notificación: ${error.message}`);
    return { success: false, error: error.message };
  }
}

/**
 * Enviar notificación de actualización de estado
 */
async function enviarNotificacionActualizacionPedido(conductorId, pedido, nuevoEstado) {
  const estadoTexto = {
    'pendiente': 'Pendiente',
    'en_ruta': 'En Ruta',
    'parcial': 'Entrega Parcial',
    'entregado': 'Entregado'
  }[nuevoEstado] || nuevoEstado;

  const message = {
    notification: {
      title: 'Actualización de Pedido',
      body: `Pedido #${pedido.numero} → ${estadoTexto}`
    },
    data: {
      tipo: 'pedido_actualizado',
      pedidoId: pedido.id,
      estado: nuevoEstado
    },
    token: (await db.collection('conductores').doc(conductorId).get()).data().tokenFCM
  };

  return admin.messaging().send(message);
}

/**
 * Enviar a múltiples conductores (ej: todos en una zona)
 */
async function enviarNotificacionAGrupo(topic, titulo, cuerpo, datos) {
  const message = {
    notification: { title: titulo, body: cuerpo },
    data: datos,
    topic: topic  // ← En lugar de "token"
  };

  return admin.messaging().send(message);
}

module.exports = {
  enviarNotificacionPedidoAsignado,
  enviarNotificacionActualizacionPedido,
  enviarNotificacionAGrupo
};

// ==========================================

// routes/pedidos.js
const express = require('express');
const router = express.Router();
const { enviarNotificacionPedidoAsignado } = require('../notificaciones');
const db = require('../firebase-config').firestore();

/**
 * POST /api/pedidos/asignar
 * Asignar pedido a un conductor
 */
router.post('/asignar', async (req, res) => {
  try {
    const { pedidoId, conductorId } = req.body;

    // 1. Obtener datos del pedido
    const pedidoDoc = await db.collection('pedidos').doc(pedidoId).get();
    const pedido = pedidoDoc.data();

    // 2. Actualizar estado
    await db.collection('pedidos').doc(pedidoId).update({
      estado: 'asignado',
      conductorId: conductorId,
      fechaAsignacion: new Date()
    });

    // 3. Enviar notificación
    await enviarNotificacionPedidoAsignado(conductorId, pedido);

    // 4. Respuesta
    res.json({ 
      success: true, 
      message: 'Pedido asignado y notificación enviada' 
    });

  } catch (error) {
    console.error(error);
    res.status(500).json({ 
      success: false,
      error: error.message 
    });
  }
});

module.exports = router;
```

### Python (Flask + firebase-admin)

```python
# firebase_config.py
import firebase_admin
from firebase_admin import credentials, messaging, firestore

# Inicializar Firebase
cred = credentials.Certificate('serviceAccountKey.json')
firebase_admin.initialize_app(cred)

db = firestore.client()

# ==========================================

# notificaciones.py
from firebase_admin import messaging
from firebase_config import db
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

def enviar_notificacion_pedido_asignado(conductor_id, pedido):
    """
    Enviar notificación cuando se asigna un pedido
    """
    try:
        # 1. Obtener token FCM del conductor
        conductor_ref = db.collection('conductores').document(conductor_id)
        conductor_doc = conductor_ref.get()
        
        if not conductor_doc.exists:
            logger.error(f'Conductor {conductor_id} no encontrado')
            return {'success': False, 'error': 'Conductor no encontrado'}
        
        conductor_data = conductor_doc.to_dict()
        token = conductor_data.get('tokenFCM')
        
        if not token:
            logger.error(f'Token FCM no encontrado para {conductor_id}')
            return {'success': False, 'error': 'Token FCM no disponible'}

        # 2. Preparar mensaje
        message = messaging.Message(
            notification=messaging.Notification(
                title=f"Nueva Asignación - Pedido #{pedido['numero']}",
                body=f"Cliente: {pedido['cliente']}"
            ),
            data={
                'tipo': 'pedido_asignado',
                'pedidoId': pedido['id'],
                'numero': pedido['numero'],
                'cliente': pedido['cliente'],
                'direccion': pedido['direccion'],
                'monto': str(pedido['montoTotal']),
                'timestamp': str(int(time.time() * 1000))
            },
            android=messaging.AndroidConfig(
                priority='high',
                notification=messaging.AndroidNotification(
                    sound='default',
                    channel_id='pedidos_canal',
                    click_action='FLUTTER_NOTIFICATION_CLICK'
                )
            ),
            token=token
        )

        # 3. Enviar
        response = messaging.send(message)
        logger.info(f'Notificación enviada a {conductor_id}: {response}')
        return {'success': True, 'messageId': response}

    except Exception as e:
        logger.error(f'Error enviando notificación: {str(e)}')
        return {'success': False, 'error': str(e)}

def enviar_notificacion_actualizacion_pedido(conductor_id, pedido, nuevo_estado):
    """Enviar notificación de actualización"""
    estado_texto = {
        'pendiente': 'Pendiente',
        'en_ruta': 'En Ruta',
        'parcial': 'Entrega Parcial',
        'entregado': 'Entregado'
    }.get(nuevo_estado, nuevo_estado)

    conductor_doc = db.collection('conductores').document(conductor_id).get()
    token = conductor_doc.to_dict().get('tokenFCM')

    message = messaging.Message(
        notification=messaging.Notification(
            title='Actualización de Pedido',
            body=f"Pedido #{pedido['numero']} → {estado_texto}"
        ),
        data={
            'tipo': 'pedido_actualizado',
            'pedidoId': pedido['id'],
            'estado': nuevo_estado
        },
        token=token
    )

    return messaging.send(message)

def enviar_a_grupo(topic, titulo, cuerpo, datos):
    """Enviar notificación a un grupo (topic)"""
    message = messaging.Message(
        notification=messaging.Notification(
            title=titulo,
            body=cuerpo
        ),
        data=datos,
        topic=topic
    )
    return messaging.send(message)

# ==========================================

# routes.py (Flask)
from flask import Flask, request, jsonify
from notificaciones import enviar_notificacion_pedido_asignado
from firebase_config import db

app = Flask(__name__)

@app.route('/api/pedidos/asignar', methods=['POST'])
def asignar_pedido():
    """
    POST /api/pedidos/asignar
    Asignar pedido a conductor y enviar notificación
    """
    try:
        data = request.json
        pedido_id = data.get('pedidoId')
        conductor_id = data.get('conductorId')

        # 1. Obtener pedido
        pedido_doc = db.collection('pedidos').document(pedido_id).get()
        pedido = pedido_doc.to_dict()

        # 2. Actualizar estado
        db.collection('pedidos').document(pedido_id).update({
            'estado': 'asignado',
            'conductorId': conductor_id,
            'fechaAsignacion': firestore.SERVER_TIMESTAMP
        })

        # 3. Enviar notificación
        result = enviar_notificacion_pedido_asignado(conductor_id, pedido)

        return jsonify({
            'success': True,
            'message': 'Pedido asignado y notificación enviada',
            'result': result
        }), 200

    except Exception as e:
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

if __name__ == '__main__':
    app.run(debug=True)
```

### cURL (REST API - Sin SDK)

```bash
#!/bin/bash

# Obtener Server Key de Firebase Console
SERVER_KEY="AAAAabc123..."  # De Firebase Console → Cloud Messaging → Server Key

CONDUCTOR_TOKEN="eF8s9d..."  # Token del conductor

curl -X POST https://fcm.googleapis.com/fcm/send \
  -H "Authorization: key=$SERVER_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "notification": {
      "title": "Nueva Asignación - Pedido #12345",
      "body": "Cliente: Juan García"
    },
    "data": {
      "tipo": "pedido_asignado",
      "pedidoId": "001",
      "numero": "12345",
      "cliente": "Juan García"
    },
    "to": "'$CONDUCTOR_TOKEN'"
  }'
```

---

## 📱 Integración Android - Recibir Notificación

Cuando tu backend envía:

```
Backend envía → FCM → App Android
                       ↓
            ConductorFirebaseMessagingService
            .onMessageReceived(message)
                       ↓
            Procesa según "tipo": pedido_asignado
                       ↓
            Muestra notificación visual
                       ↓
            Usuario toca → abre MainActivity
                       ↓
            navegaA("pedido/001")
```

---

## 🔄 Endpoint Android para Token

Tu backend debe tener:

```
POST /api/conductor/{conductorId}/token-fcm
Content-Type: application/json

{
  "token": "eF8s9d..."
}
```

**Guardarlo en BD**:

```javascript
// Node.js
router.post('/conductor/:conductorId/token-fcm', async (req, res) => {
  const { conductorId } = req.params;
  const { token } = req.body;

  await db.collection('conductores').doc(conductorId).update({
    tokenFCM: token,
    ultimaActualizacionToken: new Date()
  });

  res.json({ success: true });
});
```

---

## ✅ Checklist de Implementación

- [ ] 1. Descargar serviceAccountKey.json
- [ ] 2. Instalar firebase-admin en backend
- [ ] 3. Crear función `enviarNotificacionPedidoAsignado()`
- [ ] 4. Crear endpoint `POST /conductor/{id}/token-fcm`
- [ ] 5. Guardar token en BD cuando llega
- [ ] 6. Cuando se asigna pedido:
  - [ ] Buscar conductor_id → token_fcm
  - [ ] Llamar enviarNotificacionPedidoAsignado()
  - [ ] Log "notificación enviada"
- [ ] 7. Probar con app Android local
- [ ] 8. Ver notificación en conductor

---

## 🧪 Testing

### Test 1: Obtener Server Key

Firebase Console → Proyecto → Configuración → Cloud Messaging → Server Key (copiar)

### Test 2: Obtener Token Conductor

En app Android, después de login:

```kotlin
val tokenManager = FCMTokenManager(context)
Log.d("FCM", tokenManager.obtenerTokenLocal())  // Ver en Logcat
```

### Test 3: Enviar Manual

```bash
# Reemplazar con valores reales
curl -X POST https://fcm.googleapis.com/fcm/send \
  -H "Authorization: key=AAAA..." \
  -H "Content-Type: application/json" \
  -d '{
    "notification": { "title": "Test", "body": "Funciona" },
    "to": "eF8s..."
  }'
```

### Test 4: Verificar Logs

Android:
```bash
adb logcat | grep "FCMService\|Notificación"
```

Backend:
```bash
# Si ves "Notificación enviada: abc123" → ✅ Funciona
```

---

## 🔗 Integración Completa

```
1. Usuario inicia sesión en app
   └─ Obtiene token FCM
   └─ Envía al backend
   └─ Backend guarda en BD

2. Admin asigna pedido en panel
   └─ Backend recibe asignación
   └─ Busca token del conductor
   └─ Llama FCM.send(token, notificación)
   └─ FCM entrega al dispositivo

3. Conductor recibe notificación
   └─ App abre ConductorFirebaseMessagingService
   └─ Muestra notificación visual
   └─ Usuario toca
   └─ App abre detalles del pedido
```

---

**Estado**: ✅ Production Ready  
**Última actualización**: Marzo 2026  
**Versión**: 1.0.0
