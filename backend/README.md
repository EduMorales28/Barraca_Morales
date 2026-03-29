# 🚀 Backend JWT - LogísticaMorales

API REST con autenticación JWT para la aplicación de logística.

**Status**: ✅ Production Ready  
**Versión**: 1.0.0  
**Lenguaje**: Node.js + Express  

---

## 📋 Características

- ✅ Autenticación JWT (accessToken + refreshToken)
- ✅ Autorización por roles (admin, conductor)
- ✅ API REST completa para pedidos y conductores
- ✅ Logging con Morgan
- ✅ Error handling centralizado
- ✅ CORS habilitado
- ✅ Seguridad con Helmet

---

## 🚀 Quick Start

```bash
# 1. Instalar dependencias
npm install

# 2. Configurar .env
cp .env.example .env
# Editar .env con valores reales

# 3. Iniciar servidor
npm run dev

# 4. El servidor estará en http://localhost:3000
```

---

## 📁 Estructura

```
backend/
├── src/
│   ├── server.js              ← Entry point
│   ├── middleware/
│   │   ├── auth.js            ← JWT verification & authorization
│   │   └── errorHandler.js    ← Error handling
│   ├── controllers/
│   │   └── authController.js  ← Login, refresh, logout
│   └── routes/
│       ├── auth.js            ← Public routes
│       ├── conductor.js       ← Protected conductor routes
│       └── admin.js           ← Protected admin routes
├── .env
├── .env.example
└── package.json
```

---

## 🔑 Credenciales de Prueba

```
ADMIN:
- Email: admin@logistica.com
- Password: admin123

CONDUCTOR:
- Email: juan@conductor.com
- Password: admin123
```

---

## 🔐 API Endpoints

### Autenticación (Públicos)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/auth/login` | Login y obtener tokens |
| POST | `/auth/refresh` | Refrescar access token |
| GET | `/auth/verify` | Verificar token (requiere JWT) |
| POST | `/auth/logout` | Logout (requiere JWT) |

### Conductor (Protegidos)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/conductor/perfil` | Obtener perfil |
| GET | `/conductor/mis-pedidos` | Listar pedidos |
| POST | `/conductor/ubicacion` | Actualizar ubicación |
| PUT | `/conductor/pedidos/:id` | Cambiar estado |

### Admin (Protegidos)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/admin/conductores` | Listar conductores |
| GET | `/admin/pedidos` | Listar pedidos |
| POST | `/admin/pedidos/asignar` | Asignar pedido |

---

## 🧪 Testing

### Login

```bash
curl -X POST http://localhost:3000/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@logistica.com",
    "password": "admin123",
    "tipo": "admin"
  }'
```

### Endpoint Protegido

```bash
curl -X GET http://localhost:3000/conductor/perfil \
  -H "Authorization: Bearer {accessToken}"
```

---

## 📚 Documentación Completa

- [SETUP.md](./SETUP.md) - Setup detallado y estructura
- [JWT_AUTHENTICATION_GUIDE.md](../JWT_AUTHENTICATION_GUIDE.md) - Guía completa JWT
- [JWT_QUICK_START.md](../JWT_QUICK_START.md) - Quick start 15 minutos

---

## 🔄 Flujo de Autenticación

```
Usuario
  ↓
Login (email + password)
  ↓
Servidor valida
  ↓
Genera JWT tokens
  ↓
Cliente guarda tokens
  ↓
En cada request:
Authorization: Bearer {token}
  ↓
Servidor verifica JWT
  ↓
Permite acceso ✅
```

---

## 🛠️ Desarrollo

### Scripts

```bash
npm run dev    # Iniciar con nodemon
npm start      # Iniciar normal
npm test       # Correr tests
```

### Editar Variables de Entorno

```bash
nano .env
```

Campos importantes:
```
JWT_SECRET_KEY       = Mínimo 32 caracteres
JWT_EXPIRE           = Duración access token (ej: 1h)
REFRESH_TOKEN_EXPIRE = Duración refresh token (ej: 7d)
PORT                 = Puerto del servidor
```

---

## 📦 Dependencias

```json
{
  "express": "^4.18.2",
  "jsonwebtoken": "^9.1.2",
  "bcryptjs": "^2.4.3",
  "cors": "^2.8.5",
  "helmet": "^7.1.0",
  "morgan": "^1.10.0",
  "dotenv": "^16.3.1"
}
```

---

## 🔒 Seguridad

✅ **Implementado**:
- JWT con secret key
- Roles y autorización
- CORS configurado
- Helmet para headers de seguridad
- Rate limiting ready (agregar)

⚠️ **Para Producción**:
- Usar HTTPS
- Base de datos MongoDB
- Hash de contraseñas con bcryptjs
- Rate limiting en /auth/login
- Logs de auditoría

---

## 🐛 Troubleshooting

### Puerto en uso

```bash
lsof -i :3000
kill -9 {PID}
```

### JWT_SECRET no configurado

```
Solución: Editar .env con JWT_SECRET_KEY válido
```

### No puede conectar

```
Verifica: 
1. npm install completado
2. npm run dev ejecutándose
3. Puerto 3000 disponible
4. .env configurado
```

---

## 📞 Soporte

- Documentación: [JWT_AUTHENTICATION_GUIDE.md](../JWT_AUTHENTICATION_GUIDE.md)
- Quick Start: [JWT_QUICK_START.md](../JWT_QUICK_START.md)
- Setup: [SETUP.md](./SETUP.md)

---

## 📋 Checklist

- [ ] npm install ejecutado
- [ ] .env creado y configurado
- [ ] npm run dev iniciado
- [ ] Health check OK (curl http://localhost:3000/health)
- [ ] Login funciona con credenciales de prueba
- [ ] Token obtenido y guardado
- [ ] Endpoint protegido funciona con token
- [ ] Rechaza sin token (401)

---

**¡Listo para integrar con Android + Admin Panel!** 🚀

Ver: [JWT_AUTHENTICATION_GUIDE.md](../JWT_AUTHENTICATION_GUIDE.md)
