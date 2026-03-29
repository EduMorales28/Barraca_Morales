# Queries y Operaciones Comunes - Logística

## 🎯 Operaciones de Administrador

### Crear un nuevo pedido
```sql
-- 1. Insertar pedido
INSERT INTO pedidos (
  id, numero_pedido, cliente_id, direccion, latitud, longitud,
  monto_total, estado, fecha_creacion, created_at, updated_at
) VALUES (
  UUID(), 
  'PED-2026-001',
  'CLIENT_ABC',
  'Av. Principal 123, Lima',
  -12.0496,
  -77.0265,
  1000.00,
  'pendiente',
  NOW(),
  NOW(),
  NOW()
);

-- 2. Insertar items del pedido
INSERT INTO items_pedido (
  id, pedido_id, descripcion, cantidad, precio_unitario, subtotal, estado,
  created_at, updated_at
) VALUES 
(UUID(), 'PED_ID', 'Descarga A', 5, 200.00, 1000.00, 'pendiente', NOW(), NOW());
```

### Asignar conductor a pedido
```sql
UPDATE pedidos
SET 
  conductor_id = 'COND_001',
  estado = 'asignado',
  fecha_asignacion = NOW(),
  updated_at = NOW()
WHERE id = 'PED_ID';

-- Registrar en historial
INSERT INTO historial_pedidos (
  id, pedido_id, estado_anterior, estado_nuevo, 
  usuario_id, comentario, created_at
) VALUES (
  UUID(), 'PED_ID', 'pendiente', 'asignado',
  'ADMIN_USER', 'Asignado a ' || u.nombre, NOW()
);
```

### Obtener todos los pedidos pendientes
```sql
SELECT 
  p.id,
  p.numero_pedido,
  c.nombre as cliente,
  p.direccion,
  p.monto_total,
  COUNT(ip.id) as items,
  p.estado,
  p.fecha_creacion
FROM pedidos p
JOIN clientes c ON p.cliente_id = c.id
LEFT JOIN items_pedido ip ON p.id = ip.pedido_id
WHERE p.estado IN ('pendiente', 'asignado')
GROUP BY p.id
ORDER BY p.fecha_creacion ASC;
```

---

## 🚗 Operaciones de Conductor

### Obtener mis pedidos de hoy
```sql
SELECT 
  p.id,
  p.numero_pedido,
  c.nombre as cliente,
  c.telefono,
  p.direccion,
  p.latitud,
  p.longitud,
  p.monto_total,
  p.monto_levantado,
  p.porcentaje_levantado,
  p.estado,
  COUNT(ip.id) as items_totales,
  COUNT(CASE WHEN ip.estado = 'completado' THEN 1 END) as items_levantados
FROM pedidos p
JOIN clientes c ON p.cliente_id = c.id
LEFT JOIN items_pedido ip ON p.id = ip.pedido_id
WHERE p.conductor_id = 'COND_ID'
  AND p.estado IN ('asignado', 'en_ruta', 'parcial')
  AND DATE(p.fecha_creacion) = CURDATE()
GROUP BY p.id
ORDER BY p.fecha_creacion ASC;
```

### Cambiar estado a "en ruta"
```sql
UPDATE pedidos
SET 
  estado = 'en_ruta',
  updated_at = NOW()
WHERE id = 'PED_ID'
  AND conductor_id = 'COND_ID';
```

### Registrar entrega (con actualización de montos)
```sql
-- 1. Crear entrega
INSERT INTO entregas (
  id, pedido_id, item_pedido_id, conductor_id,
  cantidad_levantada, fotoUrl, observaciones, estado,
  recibido_por, latitud, longitud, fecha_entrega,
  created_at, updated_at
) VALUES (
  UUID(),
  'PED_ID',
  'ITEM_ID',
  'COND_ID',
  3,  -- cantidad levantada
  'gs://bucket/foto.jpg',
  'Cliente satisfecho',
  'completada',
  'Juan Pérez',
  -12.0496,
  -77.0265,
  NOW(),
  NOW(),
  NOW()
);

-- 2. Actualizar item_pedido
UPDATE items_pedido
SET 
  cantidad_levantada = cantidad_levantada + 3,
  estado = CASE 
    WHEN (cantidad_levantada + 3) >= cantidad THEN 'completado'
    WHEN (cantidad_levantada + 3) > 0 THEN 'parcial'
    ELSE 'pendiente'
  END,
  updated_at = NOW()
WHERE id = 'ITEM_ID';

-- 3. Recalcular pedido
UPDATE pedidos p
SET 
  monto_levantado = (
    SELECT SUM(cantidad_levantada * precio_unitario)
    FROM items_pedido
    WHERE pedido_id = p.id
  ),
  porcentaje_levantado = (
    SELECT (SUM(cantidad_levantada * precio_unitario) / p.monto_total) * 100
    FROM items_pedido
    WHERE pedido_id = p.id
  ),
  estado = CASE 
    WHEN (SELECT SUM(cantidad_levantada * precio_unitario) FROM items_pedido WHERE pedido_id = p.id) = p.monto_total THEN 'completado'
    WHEN (SELECT SUM(cantidad_levantada * precio_unitario) FROM items_pedido WHERE pedido_id = p.id) > 0 THEN 'parcial'
    ELSE 'en_ruta'
  END,
  levantado_total = CASE 
    WHEN (SELECT SUM(cantidad_levantada * precio_unitario) FROM items_pedido WHERE pedido_id = p.id) = p.monto_total THEN TRUE
    ELSE FALSE
  END,
  fecha_entrega_real = CASE 
    WHEN (SELECT SUM(cantidad_levantada * precio_unitario) FROM items_pedido WHERE pedido_id = p.id) = p.monto_total THEN NOW()
    ELSE fecha_entrega_real
  END,
  updated_at = NOW()
WHERE id = 'PED_ID';
```

### Registrar ubicación GPS (cada 10-15 seg)
```sql
INSERT INTO seguimiento_gps (
  id, conductor_id, pedido_id, latitud, longitud, 
  precision, velocidad, created_at
) VALUES (
  UUID(),
  'COND_ID',
  'PED_ID',
  -12.0485,      -- ubicación actual
  -77.0242,
  5.5,           -- precisión en metros
  12.3,          -- velocidad m/s
  NOW()
);
```

### Obtener mi ubicación actual
```sql
SELECT 
  latitud,
  longitud,
  precision,
  velocidad,
  created_at
FROM seguimiento_gps
WHERE conductor_id = 'COND_ID'
ORDER BY created_at DESC
LIMIT 1;
```

### Obtener mis entregas del mes
```sql
SELECT 
  e.id,
  e.pedido_id,
  p.numero_pedido,
  c.nombre as cliente,
  e.cantidad_levantada,
  e.observaciones,
  e.fecha_entrega,
  e.recibido_por
FROM entregas e
JOIN pedidos p ON e.pedido_id = p.id
JOIN clientes c ON p.cliente_id = c.id
WHERE e.conductor_id = 'COND_ID'
  AND e.estado = 'completada'
  AND MONTH(e.fecha_entrega) = MONTH(NOW())
  AND YEAR(e.fecha_entrega) = YEAR(NOW())
ORDER BY e.fecha_entrega DESC;
```

---

## 📊 Reportes y Métricas

### Dashboard: Resumen del día
```sql
SELECT 
  COUNT(DISTINCT p.id) as pedidos_asignados,
  COUNT(DISTINCT CASE WHEN p.estado = 'completado' THEN p.id END) as pedidos_completados,
  SUM(CASE WHEN p.estado = 'completado' THEN p.monto_levantado ELSE 0 END) as monto_levantado_total,
  SUM(p.monto_total) as monto_total_dia,
  ROUND(
    (COUNT(DISTINCT CASE WHEN p.estado = 'completado' THEN p.id END) / 
     COUNT(DISTINCT p.id)) * 100, 2
  ) as porcentaje_completado,
  COUNT(DISTINCT e.id) as entregas_realizadas
FROM pedidos p
LEFT JOIN entregas e ON p.id = e.pedido_id
WHERE p.conductor_id = 'COND_ID'
  AND DATE(p.fecha_creacion) = CURDATE();
```

### Rendimiento de conductor (últimos 30 días)
```sql
SELECT 
  u.nombre as conductor,
  COUNT(DISTINCT p.id) as pedidos_entregados,
  COUNT(e.id) as entregas_completadas,
  SUM(CASE WHEN e.estado = 'completada' THEN e.cantidad_levantada * ip.precio_unitario ELSE 0 END) as monto_levantado,
  ROUND(
    (COUNT(DISTINCT CASE WHEN p.estado = 'completado' THEN p.id END) / 
     COUNT(DISTINCT p.id)) * 100, 2
  ) as porcentaje_exito,
  MAX(e.fecha_entrega) as ultima_entrega
FROM usuarios u
LEFT JOIN pedidos p ON u.id = p.conductor_id
LEFT JOIN entregas e ON p.id = e.pedido_id
LEFT JOIN items_pedido ip ON e.item_pedido_id = ip.id
WHERE u.rol = 'conductor'
  AND p.fecha_creacion >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY u.id, u.nombre
ORDER BY monto_levantado DESC;
```

### Pedidos con entregas pendientes
```sql
SELECT 
  p.id,
  p.numero_pedido,
  c.nombre as cliente,
  p.direccion,
  u.nombre as conductor,
  p.monto_levantado,
  p.monto_total,
  p.porcentaje_levantado,
  p.estado,
  DATEDIFF(NOW(), p.fecha_creacion) as dias_pendiente
FROM pedidos p
JOIN clientes c ON p.cliente_id = c.id
LEFT JOIN usuarios u ON p.conductor_id = u.id
WHERE p.estado IN ('en_ruta', 'parcial')
  AND p.levantado_total = FALSE
ORDER BY p.fecha_creacion ASC;
```

### Clientes por cantidad de pedidos
```sql
SELECT 
  c.id,
  c.nombre,
  c.telefono,
  COUNT(p.id) as cantidad_pedidos,
  SUM(p.monto_total) as monto_total,
  MAX(p.fecha_creacion) as ultima_compra,
  ROUND(
    (SUM(CASE WHEN p.estado = 'completado' THEN 1 ELSE 0 END) / COUNT(p.id)) * 100, 2
  ) as tasa_entregas_exitosas
FROM clientes c
LEFT JOIN pedidos p ON c.id = p.cliente_id
GROUP BY c.id, c.nombre, c.telefono
ORDER BY cantidad_pedidos DESC;
```

---

## 🔍 Consultas de Validación

### Validar integridad: Items vs Entregas
```sql
SELECT 
  ip.id,
  ip.descripcion,
  ip.cantidad,
  COALESCE(SUM(e.cantidad_levantada), 0) as total_entregado,
  ip.cantidad - COALESCE(SUM(e.cantidad_levantada), 0) as pendiente,
  ip.estado as estado_actual,
  CASE 
    WHEN COALESCE(SUM(e.cantidad_levantada), 0) = 0 THEN 'pendiente'
    WHEN COALESCE(SUM(e.cantidad_levantada), 0) < ip.cantidad THEN 'parcial'
    ELSE 'completado'
  END as estado_esperado
FROM items_pedido ip
LEFT JOIN entregas e ON ip.id = e.item_pedido_id
WHERE ip.pedido_id = 'PED_ID'
GROUP BY ip.id
HAVING estado_actual != estado_esperado;
```

### Validar montos: items vs pedido
```sql
SELECT 
  p.id,
  p.numero_pedido,
  p.monto_total,
  SUM(ip.subtotal) as subtotal_items,
  SUM(ip.cantidad_levantada * ip.precio_unitario) as monto_levantado_calculado,
  p.monto_levantado as monto_levantado_registrado,
  CASE 
    WHEN p.monto_levantado = SUM(ip.cantidad_levantada * ip.precio_unitario) 
      THEN 'OK' 
    ELSE 'ERROR'
  END as validacion
FROM pedidos p
LEFT JOIN items_pedido ip ON p.id = ip.pedido_id
GROUP BY p.id
HAVING validacion = 'ERROR';
```

### Registros huérfanos
```sql
-- Items sin pedido
SELECT * FROM items_pedido WHERE pedido_id NOT IN (SELECT id FROM pedidos);

-- Entregas sin pedido o item
SELECT * FROM entregas 
WHERE pedido_id NOT IN (SELECT id FROM pedidos)
  OR item_pedido_id NOT IN (SELECT id FROM items_pedido);

-- GPS sin conductor
SELECT * FROM seguimiento_gps 
WHERE conductor_id NOT IN (SELECT id FROM usuarios WHERE rol = 'conductor');
```

---

## 🗑️ Limpieza y Mantenimiento

### Limpiar GPS de hace mas de 7 días
```sql
DELETE FROM seguimiento_gps
WHERE created_at < DATE_SUB(NOW(), INTERVAL 7 DAY);
```

### Limpiar entregas rechazadas hace 30 días
```sql
-- NOTA: Cambiar a estado 'archivado' antes de eliminar
UPDATE entregas
SET estado = 'rechazada'
WHERE estado = 'rechazada'
  AND created_at < DATE_SUB(NOW(), INTERVAL 30 DAY);

-- Despues, opcionalmente:
-- DELETE FROM entregas WHERE estado = 'rechazada' AND archivado = TRUE;
```

### Actualizar estado de pedidos expirados
```sql
UPDATE pedidos
SET 
  estado = 'cancelado',
  observaciones = 'Auto-cancelado por vencimiento (30 días sin avance)'
WHERE estado IN ('pendiente', 'asignado')
  AND fecha_creacion < DATE_SUB(NOW(), INTERVAL 30 DAY)
  AND monto_levantado = 0;
```
