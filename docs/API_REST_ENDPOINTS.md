# API REST - Logística de Entregas

## 📋 Base URL

```
Production: https://api.logistica-morales.com/v1
Development: http://localhost:3000/v1
```

## 🔐 Autenticación

Todos los endpoints exceptuando `/login` requieren un token JWT en el header:

```
Authorization: Bearer {token}
```

El token tiene validez de **24 horas**.

---

## 🔑 AUTH ENDPOINTS

### 1. Login
**POST** `/auth/login`

Autentica un usuario y retorna un token JWT.

**Request:**
```json
{
  "email": "conductor@logistica.com",
  "password": "securePassword123"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Login exitoso",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "usuario": {
      "id": "USER_001",
      "nombre": "Juan Pérez",
      "email": "conductor@logistica.com",
      "rol": "conductor",
      "estado": "activo",
      "fotoUrl": "https://storage.com/foto.jpg",
      "telefono": "+51987654321"
    },
    "expiresIn": 86400
  }
}
```

**Response (401 Unauthorized):**
```json
{
  "success": false,
  "message": "Credenciales inválidas",
  "error": {
    "code": "INVALID_CREDENTIALS",
    "details": "Email o contraseña incorrectos"
  }
}
```

**Response (400 Bad Request):**
```json
{
  "success": false,
  "message": "Validación fallida",
  "error": {
    "code": "VALIDATION_ERROR",
    "details": {
      "email": "Email requerido",
      "password": "Contraseña mínimo 8 caracteres"
    }
  }
}
```

---

## 📦 PEDIDOS ENDPOINTS

### 1. Crear Pedido
**POST** `/pedidos`

Crea un nuevo pedido (solo Admin).

**Headers:**
```
Authorization: Bearer {token}
Content-Type: application/json
```

**Request:**
```json
{
  "clienteId": "CLIENT_ABC",
  "numero": "PED-2026-001",
  "direccion": "Av. Principal 123, Lima",
  "referencia": "Casa con reja azul",
  "latitud": -12.0496,
  "longitud": -77.0265,
  "montoTotal": 1000.00,
  "fechaEntregaEstimada": "2026-03-30",
  "observaciones": "Frágil - manejar con cuidado",
  "items": [
    {
      "descripcion": "Descarga tipo A",
      "cantidad": 5,
      "precioUnitario": 200.00
    },
    {
      "descripcion": "Descarga tipo B",
      "cantidad": 2,
      "precioUnitario": 100.00
    }
  ]
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Pedido creado exitosamente",
  "data": {
    "id": "PED_UUID_001",
    "numero": "PED-2026-001",
    "clienteId": "CLIENT_ABC",
    "conductorId": null,
    "direccion": "Av. Principal 123, Lima",
    "referencia": "Casa con reja azul",
    "latitud": -12.0496,
    "longitud": -77.0265,
    "montoTotal": 1000.00,
    "montoLevantado": 0.00,
    "porcentajeLevantado": 0,
    "estado": "pendiente",
    "levantadoTotal": false,
    "observaciones": "Frágil - manejar con cuidado",
    "items": [
      {
        "id": "ITEM_001",
        "descripcion": "Descarga tipo A",
        "cantidad": 5,
        "precioUnitario": 200.00,
        "subtotal": 1000.00,
        "cantidadLevantada": 0,
        "estado": "pendiente"
      }
    ],
    "fechaCreacion": "2026-03-28T10:30:00Z",
    "fechaAsignacion": null,
    "fechaEntregaEstimada": "2026-03-30",
    "fechaEntregaReal": null,
    "createdAt": "2026-03-28T10:30:00Z",
    "updatedAt": "2026-03-28T10:30:00Z"
  }
}
```

**Response (400 Bad Request):**
```json
{
  "success": false,
  "message": "Validación fallida",
  "error": {
    "code": "VALIDATION_ERROR",
    "details": {
      "clienteId": "Cliente no encontrado",
      "numero": "Número de pedido ya existe",
      "montoTotal": "Debe ser mayor a 0"
    }
  }
}
```

**Response (403 Forbidden):**
```json
{
  "success": false,
  "message": "No autorizado",
  "error": {
    "code": "PERMISSION_DENIED",
    "details": "Solo administradores pueden crear pedidos"
  }
}
```

---

### 2. Listar Pedidos
**GET** `/pedidos`

Lista pedidos con filtros y paginación (Admin ve todos, conductor ve sus asignados).

**Query Parameters:**
```
?estado=pendiente
&conductor=COND_ID (opcional)
&cliente=CLIENT_ID (opcional)
&fecha_desde=2026-03-01
&fecha_hasta=2026-03-31
&page=1
&limit=20
&sort=-fecha_creacion (- = descendente)
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Pedidos obtenidos",
  "data": {
    "pedidos": [
      {
        "id": "PED_UUID_001",
        "numero": "PED-2026-001",
        "cliente": {
          "id": "CLIENT_ABC",
          "nombre": "Distribuidora XYZ",
          "telefono": "+51987654321"
        },
        "conductor": {
          "id": "COND_001",
          "nombre": "Juan Pérez"
        },
        "direccion": "Av. Principal 123, Lima",
        "montoTotal": 1000.00,
        "montoLevantado": 500.00,
        "porcentajeLevantado": 50,
        "estado": "parcial",
        "levantadoTotal": false,
        "itemsCount": 2,
        "itemsLevantados": 1,
        "fechaCreacion": "2026-03-28T10:30:00Z",
        "fechaEntregaEstimada": "2026-03-30"
      }
    ],
    "pagination": {
      "total": 45,
      "page": 1,
      "limit": 20,
      "pages": 3
    }
  }
}
```

---

### 3. Obtener Pedido por ID
**GET** `/pedidos/{pedidoId}`

Obtiene detalles completos de un pedido.

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Pedido encontrado",
  "data": {
    "id": "PED_UUID_001",
    "numero": "PED-2026-001",
    "cliente": {
      "id": "CLIENT_ABC",
      "nombre": "Distribuidora XYZ",
      "email": "cliente@dist.com",
      "telefono": "+51987654321",
      "ruc": "20123456789"
    },
    "conductor": {
      "id": "COND_001",
      "nombre": "Juan Pérez",
      "telefono": "+51912345678",
      "estado": "activo"
    },
    "direccion": "Av. Principal 123, Lima",
    "referencia": "Casa con reja azul",
    "latitud": -12.0496,
    "longitud": -77.0265,
    "montoTotal": 1000.00,
    "montoLevantado": 500.00,
    "porcentajeLevantado": 50,
    "estado": "parcial",
    "levantadoTotal": false,
    "observaciones": "Frágil - manejar con cuidado",
    "items": [
      {
        "id": "ITEM_001",
        "descripcion": "Descarga tipo A",
        "cantidad": 5,
        "precioUnitario": 200.00,
        "subtotal": 1000.00,
        "cantidadLevantada": 5,
        "estado": "completado"
      },
      {
        "id": "ITEM_002",
        "descripcion": "Descarga tipo B",
        "cantidad": 2,
        "precioUnitario": 100.00,
        "subtotal": 200.00,
        "cantidadLevantada": 0,
        "estado": "pendiente"
      }
    ],
    "entregas": [
      {
        "id": "ENT_001",
        "cantidad": 5,
        "estado": "completada",
        "fotoUrl": "https://storage.com/entrega1.jpg",
        "observaciones": "Todo en orden",
        "fechaEntrega": "2026-03-28T14:30:00Z",
        "conductor": "Juan Pérez"
      }
    ],
    "historial": [
      {
        "fecha": "2026-03-28T10:30:00Z",
        "estado_anterior": null,
        "estado_nuevo": "pendiente",
        "usuario": "Admin",
        "comentario": "Pedido creado"
      },
      {
        "fecha": "2026-03-28T11:00:00Z",
        "estado_anterior": "pendiente",
        "estado_nuevo": "asignado",
        "usuario": "Admin",
        "comentario": "Asignado a Juan Pérez"
      }
    ],
    "fechaCreacion": "2026-03-28T10:30:00Z",
    "fechaAsignacion": "2026-03-28T11:00:00Z",
    "fechaEntregaEstimada": "2026-03-30",
    "fechaEntregaReal": "2026-03-28T14:35:00Z",
    "createdAt": "2026-03-28T10:30:00Z",
    "updatedAt": "2026-03-28T14:35:00Z"
  }
}
```

**Response (404 Not Found):**
```json
{
  "success": false,
  "message": "Pedido no encontrado",
  "error": {
    "code": "RESOURCE_NOT_FOUND",
    "details": "El pedido con ID PED_INVALID no existe"
  }
}
```

---

### 4. Actualizar Estado de Pedido
**PUT** `/pedidos/{pedidoId}`

Actualiza el estado del pedido (Admin).

**Request:**
```json
{
  "estado": "asignado",
  "observaciones": "Cambio de estado"
}
```

**Estados válidos:** `pendiente`, `asignado`, `en_ruta`, `parcial`, `completado`, `cancelado`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Pedido actualizado",
  "data": {
    "id": "PED_UUID_001",
    "numero": "PED-2026-001",
    "estado": "asignado",
    "updatedAt": "2026-03-28T11:00:00Z"
  }
}
```

**Response (400 Bad Request):**
```json
{
  "success": false,
  "message": "Validación fallida",
  "error": {
    "code": "INVALID_STATE_TRANSITION",
    "details": "No se puede pasar de 'completado' a 'pendiente'"
  }
}
```

---

### 5. Asignar Conductor a Pedido
**POST** `/pedidos/{pedidoId}/asignar`

Asigna un conductor a un pedido (Admin).

**Request:**
```json
{
  "conductorId": "COND_001"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Conductor asignado",
  "data": {
    "id": "PED_UUID_001",
    "numero": "PED-2026-001",
    "conductorId": "COND_001",
    "conductor": {
      "id": "COND_001",
      "nombre": "Juan Pérez",
      "telefono": "+51912345678"
    },
    "estado": "asignado",
    "fechaAsignacion": "2026-03-28T11:00:00Z",
    "updatedAt": "2026-03-28T11:00:00Z"
  }
}
```

**Response (400 Bad Request):**
```json
{
  "success": false,
  "message": "Error de validación",
  "error": {
    "code": "INVALID_CONDUCTOR",
    "details": "El conductor no está activo o no existe"
  }
}
```

---

## 🚗 CONDUCTOR ENDPOINTS

### 1. Obtener Mis Pedidos
**GET** `/conductor/mis-pedidos`

Obtiene los pedidos asignados al conductor autenticado.

**Query Parameters:**
```
?estado=en_ruta,parcial
&sort=-fecha_creacion
&page=1
&limit=20
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Pedidos del conductor",
  "data": {
    "pedidos": [
      {
        "id": "PED_UUID_001",
        "numero": "PED-2026-001",
        "cliente": {
          "id": "CLIENT_ABC",
          "nombre": "Distribuidora XYZ",
          "telefono": "+51987654321"
        },
        "direccion": "Av. Principal 123, Lima",
        "latitud": -12.0496,
        "longitud": -77.0265,
        "montoTotal": 1000.00,
        "montoLevantado": 500.00,
        "porcentajeLevantado": 50,
        "estado": "parcial",
        "levantadoTotal": false,
        "items": [
          {
            "id": "ITEM_001",
            "descripcion": "Descarga tipo A",
            "cantidad": 5,
            "cantidadLevantada": 5,
            "estado": "completado"
          }
        ],
        "proximoItem": {
          "id": "ITEM_002",
          "descripcion": "Descarga tipo B",
          "cantidad": 2,
          "cantidadLevantada": 0
        },
        "distancia": 2.5,
        "tiempoEstimado": 15,
        "fechaCreacion": "2026-03-28T10:30:00Z"
      }
    ],
    "resumen": {
      "pedidosAsignados": 5,
      "pedidosEnRuta": 2,
      "pedidosParciales": 2,
      "pedidosCompletados": 1,
      "montoLevantadoHoy": 2500.00,
      "montoTotalAsignado": 5000.00,
      "porcentajeCompletado": 50
    }
  }
}
```

---

## 📸 ENTREGAS ENDPOINTS

### 1. Crear Entrega (subir foto + observaciones)
**POST** `/entregas`

Registra una entrega con foto y observaciones.

**Headers:**
```
Authorization: Bearer {token}
Content-Type: multipart/form-data
```

**Form Data:**
```
pedidoId: "PED_UUID_001"
itemPedidoId: "ITEM_001"
cantidadLevantada: 5
observaciones: "Cliente satisfecho, entrega completa"
recibidoPor: "Juan Pérez (Cliente)"
dniRecibidor: "12345678"
foto: [Binary file - jpg/png]
fotoFirma: [Binary file - jpg/png] (opcional)
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Entrega registrada exitosamente",
  "data": {
    "id": "ENT_UUID_001",
    "pedidoId": "PED_UUID_001",
    "itemPedidoId": "ITEM_001",
    "conductorId": "COND_001",
    "cantidadLevantada": 5,
    "estado": "completada",
    "fotoUrl": "https://storage.firebase.com/entregas/ENT_001_foto.jpg",
    "fotoFirmaUrl": "https://storage.firebase.com/entregas/ENT_001_firma.jpg",
    "observaciones": "Cliente satisfecho, entrega completa",
    "recibidoPor": "Juan Pérez (Cliente)",
    "dniRecibidor": "12345678",
    "latitud": -12.0496,
    "longitud": -77.0265,
    "fechaEntrega": "2026-03-28T14:30:00Z",
    "createdAt": "2026-03-28T14:30:00Z",
    "updatedAt": "2026-03-28T14:30:00Z"
  }
}
```

**Response (400 Bad Request):**
```json
{
  "success": false,
  "message": "Validación fallida",
  "error": {
    "code": "VALIDATION_ERROR",
    "details": {
      "foto": "Foto requerida",
      "cantidadLevantada": "No puede ser mayor a la cantidad del item",
      "recibidoPor": "Quién recibe es requerido"
    }
  }
}
```

**Response (413 Payload Too Large):**
```json
{
  "success": false,
  "message": "Archivo muy grande",
  "error": {
    "code": "FILE_TOO_LARGE",
    "details": "Máximo 5MB por imagen"
  }
}
```

---

## ❌ Códigos de Error Globales

| Código | Status | Descripción |
|--------|--------|---|
| `VALIDATION_ERROR` | 400 | Datos inválidos o incompletos |
| `INVALID_CREDENTIALS` | 401 | Email/contraseña incorrectos |
| `UNAUTHORIZED` | 401 | Token inválido o expirado |
| `PERMISSION_DENIED` | 403 | Usuario no tiene permisos |
| `RESOURCE_NOT_FOUND` | 404 | Recurso no existe |
| `CONFLICT` | 409 | Conflicto (ej: número de pedido duplicado) |
| `INVALID_STATE_TRANSITION` | 400 | Cambio de estado no permitido |
| `INTERNAL_SERVER_ERROR` | 500 | Error del servidor |
| `SERVICE_UNAVAILABLE` | 503 | Servicio no disponible |

---

## 📝 Response Generic Structure

Todos los endpoints retornan:

```json
{
  "success": true/false,
  "message": "Descripción de la respuesta",
  "data": {...} o null,
  "error": {
    "code": "ERROR_CODE",
    "details": "..." o {...}
  } o null
}
```

---

## 🔄 Rate Limiting

```
100 requests por minuto por usuario
1000 requests por hora por usuario

Headers de respuesta:
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1640000000
```

Si se excede:
```json
{
  "success": false,
  "message": "Límite de rate limit excedido",
  "error": {
    "code": "RATE_LIMIT_EXCEEDED",
    "details": "Intenta nuevamente en 30 segundos"
  }
}
```

---

## 🔐 Seguridad

- **JWT** con RS256
- **HTTPS** obligatorio
- **CORS** configurado
- **SQL Injection** prevención
- **XSS** prevención
- **CSRF** tokens
- **Password hashing** con bcrypt
- **Validación** en cliente y servidor

