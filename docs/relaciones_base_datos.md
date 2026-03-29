# Modelo de Base de Datos - Logística Barraca

## 📊 Relaciones y Claves Foráneas

### Diagrama de Relaciones

```
usuarios (admin/conductor)
    ├─── pedidos (conductor_id) ────┐
    │                                 │
    ├─── entregas (conductor_id)    │
    │                                 │
    └─── seguimiento_gps             │
         (conductor_id)               │
                                      │
clientes ────────────────────────────┤
    (cliente_id en pedidos)          │
                                      │
productos ─── items_pedido  ─────────┤
              (producto_id)           │
                                      │
          ┌───────────────────────────┘
          │
        pedidos
          ├─── items_pedido ───┐
          │                     │
          ├─── entregas ────────┤─── recibe items_pedido_id
          │                     │
          └─── historial_pedidos
          └─── seguimiento_gps
```

## 🔑 Claves Foráneas

### 1. usuarios → pedidos
- **Relación**: 1 Conductor : N Pedidos
- **FK**: `pedidos.conductor_id` → `usuarios.id`
- **Acción**: ON DELETE SET NULL (cuando se elimina conductor, pedido queda sin asignar)
- **Filtro**: Solo usuarios con rol = 'conductor'

### 2. clientes → pedidos
- **Relación**: 1 Cliente : N Pedidos
- **FK**: `pedidos.cliente_id` → `clientes.id`
- **Acción**: ON DELETE RESTRICT (no permite eliminar cliente si tiene pedidos)

### 3. pedidos → items_pedido
- **Relación**: 1 Pedido : N Items
- **FK**: `items_pedido.pedido_id` → `pedidos.id`
- **Acción**: ON DELETE CASCADE (al eliminar pedido, se eliminan sus items)

### 4. productos → items_pedido
- **Relación**: 1 Producto : N Items (Opcional)
- **FK**: `items_pedido.producto_id` → `productos.id`
- **Acción**: ON DELETE SET NULL (item queda sin referencia a producto)

### 5. pedidos → entregas
- **Relación**: 1 Pedido : N Entregas
- **FK**: `entregas.pedido_id` → `pedidos.id`
- **Acción**: ON DELETE CASCADE

### 6. items_pedido → entregas
- **Relación**: 1 Item : N Entregas (Opcional)
- **FK**: `entregas.item_pedido_id` → `items_pedido.id`
- **Acción**: ON DELETE SET NULL

### 7. usuarios → entregas
- **Relación**: 1 Conductor : N Entregas
- **FK**: `entregas.conductor_id` → `usuarios.id`
- **Acción**: ON DELETE RESTRICT (no permite eliminar conductor si tiene entregas)

### 8. usuarios → seguimiento_gps
- **Relación**: 1 Conductor : N Ubicaciones
- **FK**: `seguimiento_gps.conductor_id` → `usuarios.id`
- **Acción**: ON DELETE CASCADE

## 📋 Estados y Flujos

### Estados de Pedidos
```
pendiente → asignado → en_ruta → parcial → completado
                    ↓
                cancelado
```

### Estados de Items
```
pendiente → parcial → completado
```

### Estados de Entregas
```
planeada → en_ruta → completada
              ↓
           rechazada
```

## 💾 Estructura de Datos Crítica

### Pedido
- **ID**: UUID único
- **Monto Levantado**: Suma de `items_pedido.cantidad * precio_unitario` levantado
- **Porcentaje**: `(monto_levantado / monto_total) * 100`
- **Estado Calculado**: 
  - `pendiente`: si `monto_levantado = 0`
  - `parcial`: si `0 < monto_levantado < monto_total`
  - `completado`: si `monto_levantado = monto_total`

### Item Pedido
- **Almacena**: producto, cantidad, precio unitario
- **Cantidad Levantada**: se actualiza cada vez que hay entrega
- **Estado Automático**: Basado en cantidad levantada vs cantidad total

### Entrega
- **Foto**: URL en Firebase Storage
- **Firma**: URL en Firebase Storage
- **GPS**: Ubicación donde se entregó (lat, lng)
- **Auditoría**: Quién recibió, cuándo, hora exacta

## 🔄 Ejemplo de Flujo Completo

### Crear Pedido
```
1. Crear pedido (estado: 'pendiente')
   - cliente_id: ABC
   - monto_total: 1000
   - 3 items x 333.33 cada uno

2. Asignar conductor (estado: 'asignado')
   - conductor_id: COND001
   - fecha_asignacion: NOW()

3. Conductor en ruta (estado: 'en_ruta')
   - Insertar seguimiento_gps cada 10-15 seg

4. Primera entrega parcial
   - Crear entrega (cantidad_levantada: 500)
   - Actualizar item 1: cantidad_levantada = 333.33
   - Pedido estado→'parcial', monto_levantado=500, %=50%

5. Segunda entrega
   - Crear entrega (cantidad_levantada: 500)
   - Actualizar item 2 y 3: cantidad_levantada completa
   - Pedido estado→'completado', monto_levantado=1000, %=100%

6. Crear historial_pedidos (auditoría)
```

## 🎯 Índices para Performance

**Consultados frecuentemente:**
- `pedidos(conductor_id, estado_pedido)` - Pedidos de un conductor
- `pedidos(estado_pedido, fecha_creacion)` - Pedidos nuevos por estado
- `entregas(conductor_id, fecha_entrega)` - Entregas completadas
- `seguimiento_gps(conductor_id, created_at)` - GPS en tiempo real

## 📱 Implementación en Firestore (NoSQL)

Ver archivo: `firestore_structure.md`
