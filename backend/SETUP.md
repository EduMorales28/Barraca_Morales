# Backend JWT - Estructura y Setup Completo

## 📁 Estructura de Carpetas

```
backend/
├── src/
│   ├── server.js                    ← Entry point
│   ├── config/                      ← (Opcional) Configuración
│   │   ├── database.js              ← MongoDB connection
│   │   └── jwt.js                   ← JWT secrets
│   ├── middleware/
│   │   ├── auth.js                  ← Verificación JWT
│   │   └── errorHandler.js          ← Manejo de errores
│   ├── controllers/
│   │   ├── authController.js        ← Login, refresh, logout
│   │   ├── conductorController.js   ← Pedidos, ubicación
│   │   └── adminController.js       ← Admin operations
│   ├── routes/
│   │   ├── auth.js                  ← Rutas públicas
│   │   ├── conductor.js             ← Rutas conductor (protegidas)
│   │   └── admin.js                 ← Rutas admin (protegidas)
│   └── models/                      ← (Opcional) BD models
│       ├── User.js
│       ├── Pedido.js
│       └── RefreshToken.js
│
├── .env                             ← Variables de entorno
├── .env.example                     ← Template .env
├── package.json
└── README.md
```

---

## 🔧 Setup Inicial

### 1. Crear carpetas

```bash
mkdir -p backend/src/{config,middleware,controllers,routes,models}
cd backend
```

### 2. Archivo .env

```bash
# Copiar template
cp .env.example .env

# Editar con valores reales
nano .env
```

Contenido mínimo:

```env
PORT=3000
JWT_SECRET_KEY=tu_secret_key_super_secreto_minimo_32_caracteres
JWT_REFRESH_SECRET=tu_refresh_secret_super_secreto_minimo_32_caracteres
JWT_EXPIRE=1h
REFRESH_TOKEN_EXPIRE=7d
MONGODB_URI=mongodb://localhost:27017/logistica-morales
```

### 3. Instalar dependencias

```bash
npm install
```

### 4. Iniciar

```bash
npm run dev
```

---

## 📋 Archivos Incluidos

### ✅ server.js

**Responsabilidad**: Entry point principal

**Contiene**:
- Express app setup
- Middleware global (CORS, Helmet, Morgan)
- Rutas
- Error handler

**Usa**:
- `./routes/auth.js`
- `./routes/conductor.js`
- `./routes/admin.js`
- `./middleware/errorHandler.js`

---

### ✅ middleware/auth.js

**Responsabilidad**: Verificar JWT y validar permisos

**Exports**:
```javascript
verifyToken      // Verifica JWT valid y no expirado
requireAdmin     // Verifica que user.tipo === 'admin'
requireConductor // Verifica que user.tipo === 'conductor'
errorHandler     // Maneja excepciones
```

**Uso en rutas**:
```javascript
// Cualquier endpoint
router.get('/endpoint', verifyToken, controller.method);

// Solo admin
router.get('/admin/endpoint', verifyToken, requireAdmin, controller.method);

// Solo conductor
router.get('/conductor/endpoint', verifyToken, requireConductor, controller.method);
```

---

### ✅ middleware/errorHandler.js

**Responsabilidad**: Catch global de errores

**Uso**:
```javascript
// En server.js, al final
app.use(errorHandler);
```

---

### ✅ controllers/authController.js

**Responsabilidad**: Lógica de autenticación

**Exports**:
```javascript
login        // POST /auth/login - Validar credenciales, generar tokens
refreshToken // POST /auth/refresh - Nuevo token usando refresh token
logout       // POST /auth/logout - Invalidar refresh token
verify       // GET /auth/verify - Verificar que el token es válido
```

**Internacionales (en memoria):**
```javascript
users        // Base de datos de usuarios (en producción: MongoDB)
refreshTokens // Almacén de refresh tokens (en producción: MongoDB)
```

---

### ✅ routes/auth.js

**Responsabilidad**: Definir rutas públicas de autenticación

**Rutas**:
```
POST   /auth/login      → authController.login
POST   /auth/refresh    → authController.refreshToken
GET    /auth/verify     → verifyToken, authController.verify
POST   /auth/logout     → verifyToken, authController.logout
```

---

### ✅ routes/conductor.js

**Responsabilidad**: Endpoints para conductores (protegidos)

**Rutas**:
```
GET    /conductor/perfil           → Obtener perfil
GET    /conductor/mis-pedidos      → Listar pedidos
GET    /conductor/pedidos/:id      → Detalles pedido
POST   /conductor/ubicacion        → Actualizar ubicación
PUT    /conductor/pedidos/:id      → Cambiar estado pedido
POST   /conductor/entregas         → Registrar entrega
```

**Protección**: Todas requieren JWT + verifyToken

---

### ✅ routes/admin.js

**Responsabilidad**: Endpoints administrativos (protegidos)

**Rutas**:
```
GET    /admin/conductores          → Listar conductores
GET    /admin/pedidos              → Listar pedidos
POST   /admin/pedidos/asignar      → Asignar pedido
PUT    /admin/pedidos/:id          → Editar pedido
GET    /admin/estadisticas         → Reportes
```

**Protección**: Todas requieren JWT + admin role

---

## 🚀 Iniciar Servidor

### Desarrollo (con auto-reload)

```bash
npm run dev
```

Requiere **nodemon** instalado (ya incluido en dependencies)

### Producción

```bash
npm start
```

### Logs esperados

```
🚀 Servidor corriendo en puerto 3000
📍 Health check: http://localhost:3000/health
🔐 JWT_SECRET configurado: ✅
```

---

## 🔌 Endpoints API

### Autenticación (Públicos)

#### POST /auth/login

```bash
curl -X POST http://localhost:3000/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@logistica.com",
    "password": "admin123",
    "tipo": "admin"
  }'
```

**Response** (200):
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGc...",
    "refreshToken": "eyJhbGc...",
    "user": {
      "id": "admin_001",
      "email": "admin@logistica.com",
      "nombre": "Administrador",
      "tipo": "admin"
    },
    "expiresIn": "1h"
  }
}
```

---

#### POST /auth/refresh

```bash
curl -X POST http://localhost:3000/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"eyJhbGc..."}'
```

**Response** (200):
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGc...",
    "expiresIn": "1h"
  }
}
```

---

#### GET /auth/verify

```bash
curl -X GET http://localhost:3000/auth/verify \
  -H "Authorization: Bearer eyJhbGc..."
```

**Response** (200):
```json
{
  "success": true,
  "data": {
    "user": {
      "id": "admin_001",
      "email": "admin@logistica.com",
      "nombre": "Administrador",
      "tipo": "admin"
    }
  }
}
```

---

#### POST /auth/logout

```bash
curl -X POST http://localhost:3000/auth/logout \
  -H "Authorization: Bearer eyJhbGc..."
```

**Response** (200):
```json
{
  "success": true,
  "message": "Sesión cerrada correctamente"
}
```

---

### Conductor (Protegidos - requieren JWT)

#### GET /conductor/perfil

```bash
curl -X GET http://localhost:3000/conductor/perfil \
  -H "Authorization: Bearer eyJhbGc..."
```

---

#### GET /conductor/mis-pedidos

```bash
curl -X GET "http://localhost:3000/conductor/mis-pedidos?page=1&limit=20" \
  -H "Authorization: Bearer eyJhbGc..."
```

---

### Admin (Protegidos - requieren JWT + admin role)

#### GET /admin/conductores

```bash
curl -X GET http://localhost:3000/admin/conductores \
  -H "Authorization: Bearer eyJhbGc..."
```

---

#### POST /admin/pedidos/asignar

```bash
curl -X POST http://localhost:3000/admin/pedidos/asignar \
  -H "Authorization: Bearer eyJhbGc..." \
  -H "Content-Type: application/json" \
  -d '{
    "pedidoId": "001",
    "conductorId": "conductor_001"
  }'
```

---

## 🧪 Testing Script

```bash
#!/bin/bash

# Test Backend JWT

# 1. Login
echo "🔐 Testing login..."
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:3000/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@logistica.com","password":"admin123","tipo":"admin"}')

TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)
echo "✅ Token obtenido: $TOKEN"

# 2. Usar token
echo ""
echo "💻 Testing protected endpoint..."
curl -s -X GET http://localhost:3000/conductor/perfil \
  -H "Authorization: Bearer $TOKEN" | jq .

# 3. Logout
echo ""
echo "🚪 Testing logout..."
curl -s -X POST http://localhost:3000/auth/logout \
  -H "Authorization: Bearer $TOKEN" | jq .

echo ""
echo "✅ Todos los tests completados"
```

Guardar como `test.sh` y ejecutar:

```bash
chmod +x test.sh
./test.sh
```

---

## 📦 Dependencias

```json
{
  "express": "Framework web",
  "jsonwebtoken": "JWT generation/verification",
  "bcryptjs": "Password hashing",
  "cors": "Cross-origin requests",
  "helmet": "Security headers",
  "morgan": "HTTP logging",
  "dotenv": "Environment variables"
}
```

---

## 🔐 Seguridad en Producción

### ✅ Implementar en producción:

```javascript
// 1. Usar MongoDB para persistencia
// 2. Hash de contraseñas con bcryptjs
// 3. Variables de entorno seguras
// 4. HTTPS obligatorio
// 5. Rate limiting en /auth/login
// 6. Validación de entrada (express-validator)
// 7. Tokens en cookies httpOnly
// 8. CSRF protection
// 9. Logs de auditoría
// 10. Key rotation periódicamente
```

---

## 🆘 Troubleshooting

### Erro: Port already in use

```bash
# Encontrar proceso en puerto 3000
lsof -i :3000

# Matar proceso
kill -9 {PID}

# O cambiar puerto
PORT=3001 npm run dev
```

### Error: JWT_SECRET no configurado

```
Causa: .env no tiene JWT_SECRET_KEY
Solución: Agregar a .env
```

### Error: Cannot find module

```bash
# Reinstalar
npm install

# Limpiar cache
npm cache clean --force
```

---

## 📚 Siguiente Paso

1. ✅ Backend setup (este archivo)
2. 📖 Leer: [JWT_AUTHENTICATION_GUIDE.md](JWT_AUTHENTICATION_GUIDE.md)
3. 📖 Leer: [JWT_QUICK_START.md](JWT_QUICK_START.md)
4. 🧪 Test todos los endpoints
5. 🔌 Conectar Android + Admin Panel

---

**Status**: ✅ Ready for Integration  
**Versión**: 1.0.0  
**Actualizado**: Marzo 2026
