# Resumen Ejecutivo - Modelo de Datos Logística

## 🎯 Descripción General

Modelo de base de datos diseñado para gestionar logística de una barraca con:
- Asignación de pedidos a conductores
- Entregas parciales y totales
- Seguimiento GPS en tiempo real
- Auditoría completa de transacciones

## 📦 8 Tablas Principales

| Tabla | Propósito | Relaciones |
|-------|-----------|-----------|
| **usuarios** | Conductores y admins | N pedidos, N entregas, N GPS |
| **clientes** | Datos de clientes | 1 → N pedidos |
| **pedidos** | Órdenes de entrega | 1 → N items, 1 → N entregas |
| **items_pedido** | Líneas del pedido | 1 → N entregas |
| **entregas** | Registro de entregas | 1 entrega por item/parcial |
| **productos** | Catálogo (opcional) | N items_pedido |
| **seguimiento_gps** | Ubicación en tiempo real | 1 conductor → N ubicaciones |
| **historial_pedidos** | Auditoría de cambios | 1 pedido → N cambios |

## 🔑 Claves Foráneas Críticas

```
usuarios ──┬──→ pedidos (conductor_id)
           ├──→ entregas (conductor_id)
           └──→ seguimiento_gps

clientes ──────→ pedidos

pedidos ────┬──→ items_pedido
            └──→ entregas

items_pedido ──→ entregas

productos ─────→ items_pedido
```

## 📊 Flujo de Datos

```
1. CREAR PEDIDO
   ├─ Pedido (pendiente, monto_levantado=0)
   └─ Items (estado=pendiente)

2. ASIGNAR CONDUCTOR
   └─ Pedido.conductor_id = {uid}
   └─ Pedido.estado = asignado

3. CONDUCTOR EN RUTA
   ├─ GPS cada 10-15 seg
   └─ Pedido.estado = en_ruta

4. PRIMERA ENTREGA PARCIAL
   ├─ Crear Entrega
   ├─ Item: cantidad_levantada += X
   ├─ Pedido: monto_levantado += X
   ├─ Pedido.estado = parcial
   └─ Historial: estado_anterior → estado_nuevo

5. SEGUNDA ENTREGA COMPLETA
   ├─ Crear Entrega
   ├─ Item: cantidad_levantada = cantidad (completado)
   ├─ Pedido: monto_levantado = monto_total
   ├─ Pedido.estado = completado
   ├─ Pedido.levantadoTotal = true
   └─ Historial: registro
```

## 💾 Datos Críticos

### Pedido
```
montoTotal: 1000
montoLevantado: 500  (actualizado por entregas)
porcentajeLevantado: 50%  (calculado)
estado: parcial  (derivado de monto)
```

### Item
```
cantidad: 5
cantidadLevantada: 2  (actualizado por entrega)
estado: parcial  (50% levantado)
```

### Entrega
```
cantidadLevantada: 2  (documento principal)
fotoUrl: "gs://bucket/foto.jpg"
fotoFirmaUrl: "gs://bucket/firma.jpg"
observaciones: "Cliente no estaba, dejado con vecino"
estado: completada
geo: {lat: -12.049, lng: -77.025}  (GeoPoint)
fecha_entrega: timestamp
```

## 🔐 Controles de Integridad

| Control | Implementación |
|---------|---|
| No duplicar entregas | Usar ID único UUID |
| Validar monto_levantado | Suma de items * cantidad_levantada |
| Estado coherente | ON DELETE CASCADE / SET NULL |
| Auditoría | historial_pedidos automático |
| Permisos | Firestore Rules por rol |

## 📊 Queries Frecuentes

```sql
-- Pedidos de un conductor hoy
SELECT * FROM pedidos 
WHERE conductor_id = ? 
  AND estado IN ('asignado', 'en_ruta', 'parcial')
  AND DATE(fecha_creacion) = TODAY()

-- Entregas completadas del mes
SELECT COUNT(*) FROM entregas
WHERE conductor_id = ? 
  AND estado = 'completada'
  AND fecha_entrega >= DATE_SUB(NOW(), INTERVAL 30 DAY)

-- Porcentaje de levantamiento por pedido
SELECT 
  id,
  (monto_levantado / monto_total) * 100 as porcentaje
FROM pedidos
WHERE estado = 'parcial'
```

## 🎨 Implementación en Firestore

**Ventajas:**
- ✅ Escalabilidad automática
- ✅ Sincronización en tiempo real
- ✅ Offline-first con caché local
- ✅ GeoPoint para queries de proximidad

**Desventajas:**
- ⚠️ Costo por lectura (no por resultado)
- ⚠️ Transacciones limitadas a 25 writes
- ⚠️ No JOIN joins nativos (usar referencias)

## 🔧 Migración a SQL (Si necesario)

Si en el futuro necesitas SQL:
1. Usar base de datos como PostgreSQL
2. Implementar migrations con Liquibase/Flyway
3. Sincronizar con API REST
4. Ver archivo `database_schema.sql`

## 📝 Configuración Recomendada

```
┌─────────────────────────┐
│   Firebase Console      │
│  ├─ Firestore (BD)      │
│  ├─ Storage (Fotos)     │
│  └─ Auth (Usuarios)     │
└─────────────────────────┘
         ↓
┌─────────────────────────┐
│   App Flutter           │
│  ├─ Models              │
│  ├─ Services            │
│  └─ Views               │
└─────────────────────────┘
         ↓
┌─────────────────────────┐
│   Google Maps API       │
│  ├─ Maps Widget         │
│  ├─ Directions          │
│  └─ Geocoding           │
└─────────────────────────┘
```

## 🚀 Próximos Pasos

1. **Crear colecciones en Firestore** (Firebase Console)
2. **Implementar servicios** (lib/services/)
3. **Crear vistas** (lib/views/)
4. **Integrar Google Maps**
5. **Implementar GPS tracking**
6. **Agregar autenticación avanzada**
