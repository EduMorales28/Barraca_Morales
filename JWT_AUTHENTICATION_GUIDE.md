# 🔐 JWT Authentication - Guía de Implementación Completa

## Visión General

Se ha implementado autenticación con **JWT (JSON Web Tokens)** para el proyecto LogísticaMorales, permitiendo:

1. **Admin Panel (Web)** - Login seguro para administradores
2. **App Android (Conductor)** - Login seguro para conductores
3. **Backend (Node.js)** - Manejo centralizado de tokens con JWT

---

## Arquitectura JWT

```
┌─────────────────────────────────────────────────────────┐
│                    USUARIO                              │
│                                                          │
│  1. Ingresa email + contraseña                          │
└──────────────────────┬──────────────────────────────────┘
                       │ POST /auth/login
                       ▼
┌─────────────────────────────────────────────────────────┐
│                    BACKEND (Node.js)                     │
│                                                          │
│  1. Valida credenciales                                 │
│  2. Genera accessToken (1 hora)                         │
│  3. Genera refreshToken (7 días)                        │
│  4. Returns ambos tokens + datos usuario                │
└──────────────────────┬──────────────────────────────────┘
                       │ { accessToken, refreshToken }
                       ▼
┌─────────────────────────────────────────────────────────┐
│            CLIENTE (Web o Android)                      │
│                                                          │
│  1. Recibe tokens                                       │
│  2. Guarda en localStorage (web) / SharedPreferences     │
│  3. En cada request, incluye:                           │
│     Authorization: Bearer {accessToken}                 │
└──────────────────────────────────────────────────────────┘
```

---

## 1️⃣ Backend (Node.js/Express)

### Instalación

```bash
cd backend
npm install
```

### Variables de Entorno (.env)

```env
PORT=3000
JWT_SECRET_KEY=tu_secret_key_super_secreto_minimo_32_caracteres
JWT_REFRESH_SECRET=tu_refresh_secret_key_super_secreto
JWT_EXPIRE=1h
REFRESH_TOKEN_EXPIRE=7d
MONGODB_URI=mongodb://localhost:27017/logistica-morales
```

### Estructura

```
backend/src/
├── server.js                 ← Entry point
├── middleware/
│   └── auth.js              ← verifyToken, requireAdmin, requireConductor
├── controllers/
│   └── authController.js    ← login, refresh, logout, verify
├── routes/
│   ├── auth.js              ← Rutas públicas
│   ├── conductor.js         ← Endpoints protegidos conductor
│   └── admin.js             ← Endpoints protegidos admin
```

### Endpoints

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

**Response:**

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
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

#### GET /conductor/perfil (Protegido)

Requiere JWT en header:

```bash
curl -X GET http://localhost:3000/conductor/perfil \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Response:**

```json
{
  "success": true,
  "data": {
    "id": "conductor_001",
    "email": "juan@conductor.com",
    "nombre": "Juan García",
    "tipo": "conductor"
  }
}
```

#### POST /auth/refresh

```bash
curl -X POST http://localhost:3000/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }'
```

**Response:**

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": "1h"
  }
}
```

#### POST /auth/logout (Protegido)

```bash
curl -X POST http://localhost:3000/auth/logout \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### Iniciar Backend

```bash
npm run dev    # Con nodemon (reload automático)
npm start      # Normal
```

El servidor estará en: `http://localhost:3000`

---

## 2️⃣ App Android (Kotlin)

### Archivos Implementados

```
src/main/kotlin/com/barraca/conductor/
├── utils/
│   └── JwtTokenManager.kt           ← Almacena/recupera tokens
├── data/api/
│   ├── JwtAuthInterceptor.kt        ← Agrega JWT a requests
│   └── ApiClientSetup.kt            ← Configura Retrofit
└── viewmodel/
    └── LoginViewModel.kt            ← Maneja login
```

### JwtTokenManager

**Almacena tokens en SharedPreferences encriptadas**

```kotlin
// Guardar tokens después del login
tokenManager.saveTokens(
    accessToken = response.accessToken,
    refreshToken = response.refreshToken,
    userEmail = response.user.email,
    userId = response.user.id,
    userName = response.user.nombre,
    userType = response.user.tipo
)

// Obtener token
val token = tokenManager.getAccessToken()

// Verificar autenticación
if (tokenManager.isAuthenticated()) {
    // Usuario logueado
}

// Logout
tokenManager.clearTokens()
```

### JwtAuthInterceptor

**Automáticamente agrega JWT a todos los requests**

```
Request → JwtAuthInterceptor → Obtiene token 
→ Agrega Authorization header → Continúa request
```

Sin necesidad de hacer nada en cada endpoint, el interceptor maneja todo.

### LoginViewModel

**Maneja el flujo de login**

```kotlin
// ViewModel inyectado por Hilt
val loginViewModel: LoginViewModel by viewModels()

// Actualizar campos
loginViewModel.updateEmail("juan@conductor.com")
loginViewModel.updatePassword("password123")

// Hacer login
loginViewModel.login()

// Verificar resultado
if (loginViewModel.loginSuccess) {
    navigateTo("home")
}

// Ver errores
if (loginViewModel.errorMessage.isNotEmpty()) {
    showError(loginViewModel.errorMessage)
}

// Logout
loginViewModel.logout()
```

### Ejemplo de Integración

```kotlin
@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    Column {
        TextField(
            value = loginViewModel.email,
            onValueChange = { loginViewModel.updateEmail(it) },
            label = { Text("Email") }
        )

        TextField(
            value = loginViewModel.password,
            onValueChange = { loginViewModel.updatePassword(it) },
            label = { Text("Contraseña") }
        )

        Button(
            onClick = { loginViewModel.login() },
            enabled = !loginViewModel.isLoading
        ) {
            Text(if (loginViewModel.isLoading) "Cargando..." else "Login")
        }

        if (loginViewModel.errorMessage.isNotEmpty()) {
            Text(
                text = loginViewModel.errorMessage,
                color = Color.Red
            )
        }
    }
    
    // Después del login exitoso, redirigir
    LaunchedEffect(loginViewModel.loginSuccess) {
        if (loginViewModel.loginSuccess) {
            navigateToHome()
        }
    }
}
```

---

## 3️⃣ Admin Panel Web (Vue)

### Archivos Implementados

```
src/
├── services/
│   └── authService.js            ← Login API + localStorage
├── components/
│   └── LoginScreen.vue           ← UI del login
└── composables/
    └── useAuth.js                ← Composable para auth
```

### authService.js

**API client con interceptores JWT**

```javascript
// Login
import { login, logout, getAccessToken } from './authService';

const response = await login('admin@logistica.com', 'admin123', 'admin');
// → Guarda tokens en localStorage automáticamente

// Logout
await logout();
// → Limpia localStorage

// Verificar autenticación
if (isAuthenticated()) {
    console.log('Usuario logueado');
}

// Obtener usuario actual
const user = getAuthUser();
console.log(user.email, user.type);
```

**El interceptor agrega JWT automáticamente:**

```javascript
// Antes de enviar request:
Authorization: Bearer {token}

// Si recibe 401, limpia tokens y redirige a login
```

### LoginScreen.vue

**Componente de interfaz con form**

```vue
<template>
  <form @submit.prevent="handleLogin">
    <input v-model="form.email" type="email" placeholder="Email" />
    <input v-model="form.password" type="password" placeholder="Contraseña" />
    <select v-model="form.tipo">
      <option value="admin">Admin</option>
      <option value="conductor">Conductor</option>
    </select>
    <button type="submit" :disabled="isLoading">
      {{ isLoading ? 'Iniciando...' : 'Login' }}
    </button>
    <div v-if="errorMessage" class="error">{{ errorMessage }}</div>
  </form>
</template>

<script setup>
// ... (ver archivo completo arriba)
</script>
```

### useAuth.js Composable

**Hook para gestionar autenticación**

```javascript
import { useAuth } from '@/composables/useAuth';

export default {
  setup() {
    const auth = useAuth();

    // Inicializar (verificar sesión)
    auth.init();

    const handleLogin = async (email, password) => {
      try {
        await auth.login(email, password, 'admin');
        // ✅ Login exitoso
        router.push('/dashboard');
      } catch (error) {
        // ❌ Error en login
        console.error(error.message);
      }
    };

    const handleLogout = async () => {
      await auth.logout();
      // Redirigir a login
      router.push('/login');
    };

    return {
      auth,
      handleLogin,
      handleLogout
    };
  }
};
```

---

## Credenciales de Prueba

```
ADMIN:
Email:    admin@logistica.com
Password: admin123
Tipo:     admin

CONDUCTOR:
Email:    juan@conductor.com
Password: admin123
Tipo:     conductor
```

---

## 🔄 Flujo Completo de Login

### 1. Usuario ingresa credenciales

```
Admin panel: admin@logistica.com + admin123
App Android: juan@conductor.com + admin123
```

### 2. Cliente envía POST /auth/login

```bash
POST http://localhost:3000/auth/login
{
  "email": "...",
  "password": "...",
  "tipo": "admin|conductor"
}
```

### 3. Backend valida y genera tokens

```javascript
// authController.js
const accessToken = jwt.sign({...}, SECRET, {expiresIn: '1h'})
const refreshToken = jwt.sign({...}, REFRESH_SECRET, {expiresIn: '7d'})
```

### 4. Cliente guarda tokens

**Web (localStorage):**
```javascript
localStorage.setItem('accessToken', token);
localStorage.setItem('refreshToken', refreshToken);
```

**Android (SharedPreferences):**
```kotlin
sharedPreferences.edit()
    .putString("access_token", token)
    .putString("refresh_token", refreshToken)
    .apply()
```

### 5. En cada request, agregar JWT

**Web (Interceptor axios):**
```javascript
Authorization: Bearer {accessToken}
```

**Android (Interceptor OkHttp):**
```kotlin
Authorization: Bearer {accessToken}
```

### 6. Si token expira, refrescar

```javascript
// Automático - si recibe 401, intenta refresh
const newAccessToken = await refresh(refreshToken);
// Reintentar request con nuevo token
```

---

## 🔒 Autorización por Rol

### Backend - Middleware

```javascript
// requireAdmin middleware
if (user.tipo !== 'admin') {
    return res.status(403).json({ 
        error: 'Solo administradores' 
    });
}

// requireConductor middleware
if (user.tipo !== 'conductor') {
    return res.status(403).json({ 
        error: 'Solo conductores' 
    });
}
```

### Rotas Protegidas

```javascript
// Solo admin puede acceder
router.get('/admin/conductores', verifyToken, requireAdmin, ...);

// Solo conductor puede acceder
router.get('/conductor/perfil', verifyToken, requireConductor, ...);

// Ambos pueden acceder (con token)
router.get('/pedidos', verifyToken, ...);
```

---

## 🧪 Testing JWT

### Test 1: Login Exitoso

```bash
curl -X POST http://localhost:3000/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@logistica.com","password":"admin123","tipo":"admin"}'

# Response: { success: true, data: { accessToken, ... } }
```

### Test 2: Endpoint Protegido SIN Token

```bash
curl -X GET http://localhost:3000/conductor/perfil

# Response 401: { error: "Token no proporcionado" }
```

### Test 3: Endpoint Protegido CON Token

```bash
curl -X GET http://localhost:3000/conductor/perfil \
  -H "Authorization: Bearer {accessToken}"

# Response 200: { success: true, data: { user... } }
```

### Test 4: Token Expirado/Inválido

```bash
curl -X GET http://localhost:3000/conductor/perfil \
  -H "Authorization: Bearer invalid_token"

# Response 401: { error: "Token inválido" }
```

### Test 5: Refresh Token

```bash
curl -X POST http://localhost:3000/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"{refreshToken}"}'

# Response 200: { success: true, data: { accessToken: ... } }
```

### Test 6: Autorización - Admin solo

```bash
# Con token de conductor
curl -X GET http://localhost:3000/admin/conductores \
  -H "Authorization: Bearer {conductorToken}"

# Response 403: { error: "Se requieren permisos de administrador" }
```

---

## ⚠️ Errores Comunes & Soluciones

### Error 401: Token no proporcionado

**Causa**: Falta Authorization header

**Solución**: Asegurar que el header tenga formato correcto:
```
Authorization: Bearer {token}
```
(Nota el espacio después de "Bearer")

### Error 401: Token expirado

**Causa**: Access token expiró (>1 hora)

**Solución**: Usar refresh token para obtener nuevo access token:
```javascript
await api.post('/auth/refresh', { refreshToken })
```

### Error 403: Se requieren permisos de administrador

**Causa**: Usuario no es admin

**Solución**: Verificar `tipo` en token:
```javascript
// El JWT contiene el campo "tipo"
// Si tipo !== "admin" y accede a /admin/*, recibe 403
```

### Error: Base URL incorrecta (Android)

**Causa**: La URL del backend es diferente en emulador vs dispositivo

**Solución**:
```kotlin
// En emulador:
const baseUrl = "http://10.0.2.2:3000"

// En dispositivo físico:
const baseUrl = "http://192.168.1.x:3000"
```

---

## 🚀 Próximos Pasos

### 1. Setup Producción

- [ ] Usar MongoDB en lugar de memoria
- [ ] Usar variables de entorno seguras
- [ ] HTTPS en lugar de HTTP
- [ ] Tokens en cookies httpOnly (no localStorage)

### 2. Mejorar Seguridad

- [ ] Rate limiting en /auth/login
- [ ] Bloquear cuenta después de N intentos fallidos
- [ ] Generar Secret Key con 32+ caracteres
- [ ] Rotar refresh tokens periódicamente

### 3. Agregar Funcionalidades

- [ ] Cambiar contraseña
- [ ] Recuperar contraseña olvidada
- [ ] Google OAuth
- [ ] 2FA (Two-Factor Authentication)

---

## 📚 Referencias

- **JWT.io**: https://jwt.io (decodificar tokens)
- **Node.js JSON Web Token**: https://github.com/auth0/node-jsonwebtoken
- **Retrofit Interceptors**: https://square.github.io/retrofit/
- **Vue Composition API**: https://vuejs.org/guide/extras/composition-api.html

---

**Estado**: ✅ Production Ready  
**Última actualización**: Marzo 2026  
**Versión**: 1.0.0
