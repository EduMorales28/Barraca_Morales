# Estructura Firestore - Logística Barraca

## 📁 Colecciones Firestore

```
firestore/
├── usuarios/
│   └── {uid}
│       ├── nombre
│       ├── email
│       ├── rol (admin/conductor)
│       ├── estado (activo/inactivo)
│       ├── telefono
│       ├── foto_url
│       ├── created_at
│       └── updated_at
│
├── clientes/
│   └── {id}
│
├── pedidos/
│   └── {id}
│       ├── numero_pedido
│       ├── cliente_id (referencia)
│       ├── conductor_id (referencia)
│       ├── direccion
│       ├── geo (GeoPoint: lat, lng)
│       ├── monto_total
│       ├── monto_levantado
│       ├── estado (pendiente/asignado/en_ruta/parcial/completado)
│       ├── levantado_total (boolean)
│       ├── items_count
│       ├── items_levantados
│       ├── porcentaje_levantado
│       ├── observaciones
│       ├── fecha_creacion
│       ├── fecha_asignacion
│       ├── fecha_entrega_estimada
│       ├── fecha_entrega_real
│       ├── updated_at
│       │
│       └── subcollection: items/
│           └── {id}
│               ├── descripcion
│               ├── cantidad
│               ├── precio_unitario
│               ├── subtotal
│               ├── cantidad_levantada
│               ├── estado (pendiente/parcial/completado)
│               └── updated_at
│
├── entregas/
│   └── {id}
│       ├── pedido_id (referencia)
│       ├── item_pedido_id (referencia)
│       ├── conductor_id (referencia)
│       ├── cantidad_levantada
│       ├── foto_url (Firebase Storage)
│       ├── foto_firma_url (Firebase Storage)
│       ├── observaciones
│       ├── estado (planeada/en_ruta/completada/rechazada)
│       ├── recibido_por
│       ├── dni_recibidor
│       ├── geo (GeoPoint de entrega)
│       ├── fecha_programada
│       ├── fecha_entrega
│       ├── hora_llegada
│       ├── hora_salida
│       ├── created_at
│       └── updated_at
│
├── seguimiento_gps/
│   ├── {conductor_id}/
│   │   └── {timestamp}
│   │       ├── geo (GeoPoint)
│   │       ├── velocidad
│   │       ├── precisión
│   │       ├── pedido_id
│   │       └── created_at
│
├── historial_pedidos/
│   └── {id}
│       ├── pedido_id (referencia)
│       ├── estado_anterior
│       ├── estado_nuevo
│       ├── usuario_id (referencia)
│       ├── comentario
│       └── created_at
│
└── configuracion/
    └── app
        ├── version
        ├── ultima_actualizacion
        └── variables_globales
```

## 🔐 Reglas de Seguridad Firestore

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Usuarios - Solo el admin y el mismo usuario
    match /usuarios/{uid} {
      allow read: if request.auth.uid == uid || 
                     get(/databases/$(database)/documents/usuarios/$(request.auth.uid)).data.rol == 'admin';
      allow update, delete: if request.auth.uid == uid || 
                               get(/databases/$(database)/documents/usuarios/$(request.auth.uid)).data.rol == 'admin';
      allow create: if request.auth != null;
    }
    
    // Clientes - Solo admin
    match /clientes/{document=**} {
      allow read, write: if get(/databases/$(database)/documents/usuarios/$(request.auth.uid)).data.rol == 'admin';
    }
    
    // Pedidos
    match /pedidos/{document=**} {
      // Conductores ven sus pedidos asignados
      // Admin ve todos
      allow read: if request.auth != null && 
                     (get(/databases/$(database)/documents/usuarios/$(request.auth.uid)).data.rol == 'admin' ||
                      resource.data.conductor_id == request.auth.uid);
      
      // Admin puede crear y actualizar
      allow create, update: if get(/databases/$(database)/documents/usuarios/$(request.auth.uid)).data.rol == 'admin';
      
      // Conductor puede actualizar estado de sus pedidos
      allow update: if resource.data.conductor_id == request.auth.uid && 
                       request.resource.data.diff(resource.data).affectedKeys().hasOnly(['estado', 'monto_levantado', 'porcentaje_levantado']);
    }
    
    // Entregas
    match /entregas/{document=**} {
      allow read: if request.auth != null;
      allow create, update: if request.auth.uid == resource.data.conductor_id || 
                               get(/databases/$(database)/documents/usuarios/$(request.auth.uid)).data.rol == 'admin';
    }
    
    // Seguimiento GPS - Solo el conductor y admin
    match /seguimiento_gps/{conductor_id}/{document=**} {
      allow read, write: if request.auth.uid == conductor_id || 
                            get(/databases/$(database)/documents/usuarios/$(request.auth.uid)).data.rol == 'admin';
    }
  }
}
```

## 📊 Índices Compuestos Recomendados

Para Firestore, crear estos índices en la consola:

```
1. Colección: pedidos
   - conductor_id (Ascending)
   - estado (Ascending)
   - fecha_creacion (Descending)

2. Colección: entregas
   - conductor_id (Ascending)
   - fecha_entrega (Descending)

3. Colección: seguimiento_gps/{conductor_id}
   - created_at (Descending)

4. Colección: pedidos
   - estado (Ascending)
   - fecha_creacion (Descending)
```

## 🔄 Operaciones de Sincronización

### Cuando se crea un Pedido
```
- Crear documento en /pedidos/{id}
- Inicializar monto_levantado = 0
- Crear subcollection /pedidos/{id}/items
- Crear documento en /historial_pedidos con acción "creado"
```

### Cuando se asigna a Conductor
```
1. Actualizar /pedidos/{id}:
   - conductor_id = {uid_conductor}
   - estado = "asignado"
   - fecha_asignacion = now()

2. Crear entrada en /historial_pedidos:
   - estado_anterior: "pendiente"
   - estado_nuevo: "asignado"
   - usuario_id: {admin_uid}

3. OPCIONAL: Crear notificación para conductor
```

### Cuando se completa Entrega
```
1. Crear /entregas/{id}
2. Actualizar /pedidos/{id}/items/{item_id}:
   - cantidad_levantada += amount
   - estado = calcular_estado()

3. Recalcular /pedidos/{id}:
   - monto_levantado = suma(items.cantidad_levantada * precio)
   - porcentaje_levantado = (monto_levantado / monto_total) * 100
   - estado = calcular_estado_pedido()
   - Si levantado_total = true, fecha_entrega_real = now()

4. Crear entrada en /historial_pedidos
```

### Rastreo GPS en Tiempo Real
```
- Cada 10-15 segundos:
  POST /seguimiento_gps/{conductor_id}/{timestamp}
  {
    geo: GeoPoint(lat, lng),
    velocidad: m/s,
    precisión: metros,
    pedido_id: "ref"
  }

- Limpiar datos viejos (>7 días) con Cloud Function
```

## 💡 Queries Comunes

### Obtener pedidos de un conductor (hoy)
```javascript
db.collection('pedidos')
  .where('conductor_id', '==', conductorId)
  .where('estado', 'in', ['asignado', 'en_ruta', 'parcial'])
  .orderBy('fecha_creacion', 'desc')
  .get()
```

### Obtener entregas completadas (último mes)
```javascript
db.collection('entregas')
  .where('conductor_id', '==', conductorId)
  .where('fecha_entrega', '>=', hace30dias)
  .where('estado', '==', 'completada')
  .orderBy('fecha_entrega', 'desc')
  .get()
```

### Ubicación actual del conductor (GPS)
```javascript
db.collection('seguimiento_gps')
  .doc(conductorId)
  .collection('ubicaciones')
  .orderBy('created_at', 'desc')
  .limit(1)
  .get()
```

### Pedidos con entrega parcial
```javascript
db.collection('pedidos')
  .where('estado', '==', 'parcial')
  .orderBy('fecha_creacion', 'desc')
  .get()
```

## 📱 Sincronización Offline

Para sincronización offline en Firestore:

```dart
// Habilitar persistencia
await Firebase.initializeApp();
FirebaseFirestore.instance.enableNetwork();
FirebaseFirestore.instance.disableNetwork(); // para offline

// Los cambios se sincronizan cuando hay conexión
db.collection('entregas').add(data); // funciona offline
```

## ⚠️ Consideraciones de Costo

- **Lecturas**: 1 por query (aunque devuelva 0 o 1000 docs)
- **Escrituras**: 1 por documento
- **Borrados**: 1 por documento

**Optimizar:**
- Usar subcollections para datos con muchos items
- Batches de escritura (hasta 500 operaciones)
- Índices compuestos solo si necesario
