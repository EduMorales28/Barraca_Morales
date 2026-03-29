-- ============================================================================
-- ESQUEMA DE BASE DE DATOS - LOGÍSTICA BARRACA
-- ============================================================================

-- 1. TABLA USUARIOS (ADMIN / CONDUCTOR)
-- ============================================================================
CREATE TABLE usuarios (
  id VARCHAR(36) PRIMARY KEY,
  nombre VARCHAR(100) NOT NULL,
  email VARCHAR(100) UNIQUE NOT NULL,
  telefono VARCHAR(20),
  rol ENUM('admin', 'conductor') NOT NULL,
  estado ENUM('activo', 'inactivo') NOT NULL DEFAULT 'activo',
  foto_url VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  INDEX idx_rol (rol),
  INDEX idx_estado (estado),
  INDEX idx_email (email)
);

-- 2. TABLA CLIENTES
-- ============================================================================
CREATE TABLE clientes (
  id VARCHAR(36) PRIMARY KEY,
  nombre VARCHAR(100) NOT NULL,
  email VARCHAR(100),
  telefono VARCHAR(20),
  razon_social VARCHAR(150),
  ruc VARCHAR(20) UNIQUE,
  direccion_default VARCHAR(255),
  estado ENUM('activo', 'inactivo') NOT NULL DEFAULT 'activo',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  INDEX idx_nombre (nombre),
  INDEX idx_telefono (telefono),
  INDEX idx_ruc (ruc)
);

-- 3. TABLA PRODUCTOS/ITEMS
-- ============================================================================
CREATE TABLE productos (
  id VARCHAR(36) PRIMARY KEY,
  nombre VARCHAR(150) NOT NULL,
  descripcion TEXT,
  unidad VARCHAR(20),
  estado ENUM('activo', 'inactivo') NOT NULL DEFAULT 'activo',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  INDEX idx_nombre (nombre)
);

-- 4. TABLA PEDIDOS
-- ============================================================================
CREATE TABLE pedidos (
  id VARCHAR(36) PRIMARY KEY,
  numero_pedido VARCHAR(50) UNIQUE NOT NULL,
  cliente_id VARCHAR(36) NOT NULL,
  conductor_id VARCHAR(36),
  
  -- Dirección
  direccion VARCHAR(255) NOT NULL,
  referencia VARCHAR(255),
  
  -- Ubicación GPS
  latitud DECIMAL(10, 8),
  longitud DECIMAL(11, 8),
  
  -- Montos
  monto_total DECIMAL(10, 2) NOT NULL DEFAULT 0,
  monto_levantado DECIMAL(10, 2) NOT NULL DEFAULT 0,
  
  -- Estados
  estado_pedido ENUM(
    'pendiente',      -- Creado, sin asignar
    'asignado',       -- Asignado a conductor
    'en_ruta',        -- Conductor en camino
    'parcial',        -- Entrega parcial
    'completado',     -- Totalmente levantado
    'cancelado'
  ) NOT NULL DEFAULT 'pendiente',
  
  -- Levantamiento
  levantado_total BOOLEAN NOT NULL DEFAULT FALSE,
  porcentaje_levantado DECIMAL(5, 2) NOT NULL DEFAULT 0,
  
  -- Observaciones
  observaciones TEXT,
  
  -- Fechas
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  fecha_asignacion TIMESTAMP NULL,
  fecha_entrega_estimada DATE,
  fecha_entrega_real TIMESTAMP NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE RESTRICT,
  FOREIGN KEY (conductor_id) REFERENCES usuarios(id) ON DELETE SET NULL,
  
  INDEX idx_numero (numero_pedido),
  INDEX idx_cliente (cliente_id),
  INDEX idx_conductor (conductor_id),
  INDEX idx_estado (estado_pedido),
  INDEX idx_fecha_creacion (fecha_creacion),
  INDEX idx_levantado (levantado_total)
);

-- 5. TABLA ITEMS_PEDIDO (Líneas del pedido)
-- ============================================================================
CREATE TABLE items_pedido (
  id VARCHAR(36) PRIMARY KEY,
  pedido_id VARCHAR(36) NOT NULL,
  producto_id VARCHAR(36),
  
  descripcion VARCHAR(255) NOT NULL,
  cantidad DECIMAL(10, 3) NOT NULL,
  precio_unitario DECIMAL(10, 2) NOT NULL,
  subtotal DECIMAL(12, 2) NOT NULL,
  
  -- Levantamiento
  cantidad_levantada DECIMAL(10, 3) NOT NULL DEFAULT 0,
  estado_item ENUM(
    'pendiente',    -- No levantado
    'parcial',      -- Levantado parcialmente
    'completado'    -- Totalmente levantado
  ) NOT NULL DEFAULT 'pendiente',
  
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  FOREIGN KEY (pedido_id) REFERENCES pedidos(id) ON DELETE CASCADE,
  FOREIGN KEY (producto_id) REFERENCES productos(id) ON DELETE SET NULL,
  
  INDEX idx_pedido (pedido_id),
  INDEX idx_estado (estado_item)
);

-- 6. TABLA ENTREGAS (Registro de entregas/levantamientos)
-- ============================================================================
CREATE TABLE entregas (
  id VARCHAR(36) PRIMARY KEY,
  pedido_id VARCHAR(36) NOT NULL,
  item_pedido_id VARCHAR(36),
  conductor_id VARCHAR(36) NOT NULL,
  
  -- Datos de entrega
  cantidad_levantada DECIMAL(10, 3),
  
  -- Documentación
  foto_url VARCHAR(500),
  foto_firma_url VARCHAR(500),
  observaciones TEXT,
  
  -- Estado de entrega
  estado ENUM(
    'planeada',
    'en_ruta',
    'completada',
    'rechazada'
  ) NOT NULL DEFAULT 'planeada',
  
  -- Recepción
  recibido_por VARCHAR(100),
  dni_recibidor VARCHAR(20),
  
  -- Fecha/Hora
  fecha_programada DATETIME,
  fecha_entrega TIMESTAMP NULL,
  hora_llegada TIMESTAMP NULL,
  hora_salida TIMESTAMP NULL,
  
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  FOREIGN KEY (pedido_id) REFERENCES pedidos(id) ON DELETE CASCADE,
  FOREIGN KEY (item_pedido_id) REFERENCES items_pedido(id) ON DELETE SET NULL,
  FOREIGN KEY (conductor_id) REFERENCES usuarios(id) ON DELETE RESTRICT,
  
  INDEX idx_pedido (pedido_id),
  INDEX idx_conductor (conductor_id),
  INDEX idx_estado (estado),
  INDEX idx_fecha_entrega (fecha_entrega)
);

-- 7. TABLA HISTORIAL DE ESTADOS (Auditoría)
-- ============================================================================
CREATE TABLE historial_pedidos (
  id VARCHAR(36) PRIMARY KEY,
  pedido_id VARCHAR(36) NOT NULL,
  estado_anterior VARCHAR(50),
  estado_nuevo VARCHAR(50) NOT NULL,
  usuario_id VARCHAR(36),
  comentario TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  FOREIGN KEY (pedido_id) REFERENCES pedidos(id) ON DELETE CASCADE,
  FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL,
  
  INDEX idx_pedido (pedido_id),
  INDEX idx_fecha (created_at)
);

-- 8. TABLA SEGUIMIENTO GPS (Ubicación en tiempo real)
-- ============================================================================
CREATE TABLE seguimiento_gps (
  id VARCHAR(36) PRIMARY KEY,
  pedido_id VARCHAR(36),
  conductor_id VARCHAR(36) NOT NULL,
  
  latitud DECIMAL(10, 8) NOT NULL,
  longitud DECIMAL(11, 8) NOT NULL,
  precisión DECIMAL(5, 2),
  
  velocidad DECIMAL(5, 2),
  
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  FOREIGN KEY (pedido_id) REFERENCES pedidos(id) ON DELETE SET NULL,
  FOREIGN KEY (conductor_id) REFERENCES usuarios(id) ON DELETE CASCADE,
  
  INDEX idx_conductor_fecha (conductor_id, created_at),
  INDEX idx_pedido (pedido_id)
);

-- ============================================================================
-- ÍNDICES DE RENDIMIENTO ADICIONALES
-- ============================================================================
CREATE INDEX idx_pedidos_estado_conductor ON pedidos(estado_pedido, conductor_id);
CREATE INDEX idx_pedidos_estado_fecha ON pedidos(estado_pedido, fecha_creacion);
CREATE INDEX idx_entregas_pedido_estado ON entregas(pedido_id, estado);

-- ============================================================================
-- VISTAS ÚTILES
-- ============================================================================

-- Vista: Pedidos pendientes por conductor
CREATE VIEW v_pedidos_pendientes_conductor AS
SELECT 
  p.id,
  p.numero_pedido,
  c.nombre as cliente,
  u.nombre as conductor,
  p.direccion,
  p.monto_total,
  p.monto_levantado,
  p.porcentaje_levantado,
  p.estado_pedido,
  p.fecha_creacion
FROM pedidos p
JOIN clientes c ON p.cliente_id = c.id
LEFT JOIN usuarios u ON p.conductor_id = u.id
WHERE p.estado_pedido IN ('asignado', 'en_ruta', 'parcial')
ORDER BY p.fecha_creacion ASC;

-- Vista: Resumen de entregas por conductor
CREATE VIEW v_entregas_conductor AS
SELECT 
  u.id,
  u.nombre as conductor,
  COUNT(DISTINCT e.pedido_id) as pedidos_entregados,
  COUNT(e.id) as entregas_realizadas,
  SUM(CASE WHEN e.estado = 'completada' THEN 1 ELSE 0 END) as entregas_exitosas,
  MAX(e.fecha_entrega) as ultima_entrega
FROM usuarios u
LEFT JOIN entregas e ON u.id = e.conductor_id
WHERE u.rol = 'conductor'
GROUP BY u.id, u.nombre;
