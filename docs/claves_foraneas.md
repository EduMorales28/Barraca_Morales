# Referencia Rápida - Relaciones y Claves Foráneas

## 🔗 Todas las Relaciones

### 1️⃣ USUARIOS → PEDIDOS
```
┌──────────────┐     ┌──────────────┐
│   usuarios   │     │   pedidos    │
├──────────────┤     ├──────────────┤
│ id (PK)      │────→│ conductor_id │ (FK)
│ nombre       │     │ cliente_id   │ (FK → clientes)
│ rol          │     │ estado       │
│ estado       │     │ monto_total  │
│              │     │ ...          │
└──────────────┘     └──────────────┘

Tipo: 1 CONDUCTOR : N PEDIDOS
Acción Eliminar: SET NULL
```

### 2️⃣ CLIENTES → PEDIDOS
```
┌──────────────┐     ┌──────────────┐
│   clientes   │     │   pedidos    │
├──────────────┤     ├──────────────┤
│ id (PK)      │────→│ cliente_id   │ (FK)
│ nombre       │     │ numero_ped   │
│ email        │     │ direccion    │
│ ruc          │     │              │
└──────────────┘     └──────────────┘

Tipo: 1 CLIENTE : N PEDIDOS
Acción Eliminar: RESTRICT
```

### 3️⃣ PEDIDOS → ITEMS_PEDIDO
```
┌──────────────┐     ┌──────────────────┐
│   pedidos    │     │  items_pedido    │
├──────────────┤     ├──────────────────┤
│ id (PK)      │────→│ pedido_id (FK)   │
│ numero_ped   │     │ descripcion      │
│ monto_total  │     │ cantidad         │
│              │     │ cantidad_levant  │
└──────────────┘     └──────────────────┘

Tipo: 1 PEDIDO : N ITEMS
Acción Eliminar: CASCADE
Nota: Items son componentes del pedido
```

### 4️⃣ PRODUCTOS → ITEMS_PEDIDO (OPCIONAL)
```
┌──────────────┐     ┌──────────────────┐
│  productos   │     │  items_pedido    │
├──────────────┤     ├──────────────────┤
│ id (PK)      │────→│ producto_id (FK) │
│ nombre       │     │ descripcion      │
│ unidad       │     │ cantidad         │
│ estado       │     │                  │
└──────────────┘     └──────────────────┘

Tipo: 1 PRODUCTO : N ITEMS
Acción Eliminar: SET NULL
Nota: Opcional, item puede no tener producto ref
```

### 5️⃣ PEDIDOS → ENTREGAS
```
┌──────────────┐     ┌──────────────────┐
│   pedidos    │     │   entregas       │
├──────────────┤     ├──────────────────┤
│ id (PK)      │────→│ pedido_id (FK)   │
│ numero_ped   │     │ conductor_id     │
│ estado       │     │ cantidad_levant  │
│              │     │ foto_url         │
└──────────────┘     └──────────────────┘

Tipo: 1 PEDIDO : N ENTREGAS
Acción Eliminar: CASCADE
Nota: Pueden haber varias entregas por pedido
```

### 6️⃣ ITEMS_PEDIDO → ENTREGAS
```
┌──────────────────┐     ┌──────────────────┐
│  items_pedido    │     │   entregas       │
├──────────────────┤     ├──────────────────┤
│ id (PK)          │────→│ item_pedido_id   │
│ cantidad_levant  │     │ cantidad_levant  │
│ estado           │     │ fotoUrl          │
│                  │     │                  │
└──────────────────┘     └──────────────────┘

Tipo: 1 ITEM : N ENTREGAS
Acción Eliminar: SET NULL
Nota: Una entrega puede registrar parcialmente
```

### 7️⃣ USUARIOS → ENTREGAS
```
┌──────────────┐     ┌──────────────────┐
│   usuarios   │     │   entregas       │
├──────────────┤     ├──────────────────┤
│ id (PK)      │────→│ conductor_id     │
│ nombre       │     │ fecha_entrega    │
│ rol='cond'   │     │ estado           │
│              │     │                  │
└──────────────┘     └──────────────────┘

Tipo: 1 CONDUCTOR : N ENTREGAS
Acción Eliminar: RESTRICT
```

### 8️⃣ USUARIOS → SEGUIMIENTO_GPS
```
┌──────────────┐     ┌──────────────────┐
│   usuarios   │     │ seguimiento_gps  │
├──────────────┤     ├──────────────────┤
│ id (PK)      │────→│ conductor_id     │
│ nombre       │     │ latitud          │
│ rol='cond'   │     │ longitud         │
│              │     │ timestamp        │
└──────────────┘     └──────────────────┘

Tipo: 1 CONDUCTOR : N UBICACIONES
Acción Eliminar: CASCADE
```

### 9️⃣ PEDIDOS → HISTORIAL_PEDIDOS
```
┌──────────────┐     ┌──────────────────┐
│   pedidos    │     │ historial_ped    │
├──────────────┤     ├──────────────────┤
│ id (PK)      │────→│ pedido_id (FK)   │
│              │     │ estado_anterior  │
│              │     │ estado_nuevo     │
│              │     │ usuario_id       │
└──────────────┘     └──────────────────┘

Tipo: 1 PEDIDO : N CAMBIOS
Acción Eliminar: CASCADE
Nota: Auditoria de cambios
```

## 📋 Tabla de Resumen

| ID | De | A | Cardinalidad | FK Del | Acción Del | Descripción |
|----|----|----|---|---|---|---|
| 1 | usuarios | pedidos | 1:N | conductor_id | SET NULL | Asignación conductor |
| 2 | clientes | pedidos | 1:N | cliente_id | RESTRICT | Referencia cliente |
| 3 | pedidos | items_pedido | 1:N | pedido_id | CASCADE | Líneas del pedido |
| 4 | productos | items_pedido | 1:N | producto_id | SET NULL | Ref producto (opt) |
| 5 | pedidos | entregas | 1:N | pedido_id | CASCADE | Entregas del pedido |
| 6 | items_pedido | entregas | 1:N | item_pedido_id | SET NULL | Entrega de item |
| 7 | usuarios | entregas | 1:N | conductor_id | RESTRICT | Conductor entrega |
| 8 | usuarios | seguimiento_gps | 1:N | conductor_id | CASCADE | GPS conductor |
| 9 | pedidos | historial_pedidos | 1:N | pedido_id | CASCADE | Auditoría cambios |

## 🎯 Reglas de Integridad

### Cuando se CREA un Pedido
```
✓ cliente_id debe existir en clientes
✗ conductor_id puede ser NULL
✓ monto_total >= 0
✓ crear automáticamente 1+ items_pedido
```

### Cuando se ASIGNA Conductar
```
✓ conductor_id existe en usuarios
✓ conductor_id.rol = 'conductor'
✓ crear entrada en historial_pedidos
```

### Cuando se ENTREGA
```
✓ pedido_id existe
✓ conductor_id existe
✓ conductor_id = pedido_id.conductor_id (opt)
✓ cantidad_levantada <= item.cantidad
✓ actualizar item.cantidad_levantada
✓ recalcular pedido.monto_levantado
✓ crear entrada en historial_pedidos
```

### Cuando se ELIMINA
```
usuario: SET NULL en pedidos, pero RESTRICT en entregas
cliente: RESTRICT (prevenir eliminar con pedidos)
pedido: CASCADE (elimina items y entregas)
item: CASCADE en entregas
```

## 💡 Ejemplos SQL

### Obtener todas las entregas de un pedido
```sql
SELECT e.*, u.nombre as conductor
FROM entregas e
JOIN usuarios u ON e.conductor_id = u.id
WHERE e.pedido_id = 'PEDIDO_123'
ORDER BY e.fecha_entrega DESC;
```

### Validar integridad: Items vs Entregas
```sql
SELECT 
  ip.id,
  ip.cantidad,
  SUM(e.cantidad_levantada) as total_entregado,
  ip.cantidad - SUM(e.cantidad_levantada) as faltante
FROM items_pedido ip
LEFT JOIN entregas e ON ip.id = e.item_pedido_id
GROUP BY ip.id
HAVING faltante > 0;
```

### Entregas por conductor (últimos 7 días)
```sql
SELECT 
  u.nombre as conductor,
  COUNT(DISTINCT e.pedido_id) as pedidos,
  COUNT(e.id) as entregas,
  MAX(e.fecha_entrega) as ultima
FROM usuarios u
JOIN entregas e ON u.id = e.conductor_id
WHERE u.rol = 'conductor'
  AND e.fecha_entrega >= DATE_SUB(NOW(), INTERVAL 7 DAY)
GROUP BY u.id, u.nombre
ORDER BY entregas DESC;
```

## 📊 Índices para Optimización

```sql
-- Consulta: Pedidos de un conductor
CREATE INDEX idx_pedidos_conductor_estado 
  ON pedidos(conductor_id, estado_pedido);

-- Consulta: Entregas recientes
CREATE INDEX idx_entregas_conductor_fecha
  ON entregas(conductor_id, fecha_entrega DESC);

-- Consulta: GPS en tiempo real
CREATE INDEX idx_gps_conductor_fecha
  ON seguimiento_gps(conductor_id, created_at DESC);

-- Consulta: Items de un pedido
CREATE INDEX idx_items_pedido
  ON items_pedido(pedido_id);
```

## 🔍 Validaciones en Aplicación

```dart
// Validar que cantidad_levantada <= cantidad
assert(entrega.cantidadLevantada <= item.cantidad);

// Validar que monto_levantado = suma items
double sumaItems = items
  .map((i) => i.cantidadLevantada * i.precioUnitario)
  .reduce((a, b) => a + b);
assert(pedido.montoLevantado == sumaItems);

// Validar conductor existe
assert(conductorId != null && usuario.rol == 'conductor');
```
