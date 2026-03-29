# ⚡ JWT - Quick Start (15 minutos)

## 🎯 Objetivo
Setup JWT authentication funcional en backend + Android + web en 15 minutos.

---

## Paso 1: Backend (5 min)

### 1.1 Crear .env

```bash
cd backend
cp .env.example .env
```

Edita `.env`:
```env
PORT=3000
JWT_SECRET_KEY=tu_secret_super_secreto_32_caracteres_minimo
JWT_REFRESH_SECRET=otro_secret_diferente_32_caracteres
JWT_EXPIRE=1h
REFRESH_TOKEN_EXPIRE=7d
```

### 1.2 Instalar dependencias

```bash
npm install
```

### 1.3 Iniciar servidor

```bash
npm run dev
```

✅ Debe decir: "🚀 Servidor corriendo en puerto 3000"

### 1.4 Verify

```bash
curl http://localhost:3000/health

# Response: { "status": "ok" }
```

---

## Paso 2: Test Backend (5 min)

### 2.1 Login

```bash
curl -X POST http://localhost:3000/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@logistica.com",
    "password": "admin123",
    "tipo": "admin"
  }'
```

✅ Respuesta:
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJ...",
    "refreshToken": "eyJ...",
    "user": { ... }
  }
}
```

**Copiar el `accessToken`**

### 2.2 Usar Token

```bash
# Guardar token en variable
TOKEN="eyJ..."

# Usar en endpoint protegido
curl -X GET http://localhost:3000/conductor/perfil \
  -H "Authorization: Bearer $TOKEN"
```

✅ Respuesta:
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

---

## Paso 3: Android Setup (3 min)

### 3.1 Archivos ya creados

Los siguientes archivos ya están listos:

```
✅ JwtTokenManager.kt        (almacena tokens)
✅ JwtAuthInterceptor.kt     (agrega JWT a requests)
✅ ApiClientSetup.kt         (configura Retrofit)
✅ LoginViewModel.kt         (maneja login)
```

### 3.2 Configurar Base URL

Edita `ConductorApiService.kt` y verifica:

```kotlin
// Para emulador:
private const val BASE_URL = "http://10.0.2.2:3000/"

// Para dispositivo:
private const val BASE_URL = "http://192.168.1.x:3000/"
```

### 3.3 Integrar en tu LoginScreen

```kotlin
@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    // Email input
    TextField(
        value = loginViewModel.email,
        onValueChange = { loginViewModel.updateEmail(it) }
    )
    
    // Password input
    TextField(
        value = loginViewModel.password,
        onValueChange = { loginViewModel.updatePassword(it) }
    )
    
    // Login button
    Button(onClick = { loginViewModel.login() }) {
        Text("Login")
    }
    
    // Error message
    if (loginViewModel.errorMessage.isNotEmpty()) {
        Text(loginViewModel.errorMessage, color = Color.Red)
    }
    
    // Success - navigate to home
    LaunchedEffect(loginViewModel.loginSuccess) {
        if (loginViewModel.loginSuccess) {
            // navigateTo("home")
        }
    }
}
```

---

## Paso 4: Admin Panel Setup (2 min)

### 4.1 Archivos ya creados

```
✅ authService.js           (API + localStorage)
✅ LoginScreen.vue          (UI)
✅ useAuth.js               (composable)
```

### 4.2 Integrar en router

Edita `main.js` o `router.js`:

```javascript
import { createRouter, createWebHistory } from 'vue-router';
import LoginScreen from '@/components/LoginScreen.vue';
import Dashboard from '@/components/Dashboard.vue';

const routes = [
  { path: '/login', component: LoginScreen },
  { 
    path: '/dashboard', 
    component: Dashboard,
    meta: { requiresAuth: true }
  }
];

export default createRouter({
  history: createWebHistory(),
  routes
});
```

### 4.3 Guard de rutas

```javascript
// router.js
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('accessToken');
  
  if (to.meta.requiresAuth && !token) {
    next('/login');
  } else {
    next();
  }
});
```

---

## 🧪 Testing

### Test 1: Backend

```bash
# Login
curl -X POST http://localhost:3000/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@logistica.com","password":"admin123","tipo":"admin"}'

# ✅ Debe devolver tokens
```

### Test 2: Android

```bash
# En tu LoginScreen Compose
# 1. Ingresa admin@logistica.com / admin123
# 2. Click Login
# 3. Verifica en Logcat:
#    "✅ Login exitoso para: admin@logistica.com"
# 4. Debe navegar a Home
```

### Test 3: Web

```bash
# 1. Abre http://localhost:5173/login
# 2. Ingresa admin@logistica.com / admin123
# 3. Debe redirigir a /dashboard
# 4. Verifica en localStorage:
#    localStorage.getItem('accessToken')
```

---

## ✅ Checklist

```
BACKEND:
☐ npm install completado
☐ npm run dev ejecutándose
☐ Health check OK
☐ Login devuelve tokens
☐ Endpoint protegido funciona con token
☐ Rechaza requests sin token (401)

ANDROID:
☐ Archivos Kotlin creados
☐ Base URL correcta (10.0.2.2:3000 o 192.168.1.x:3000)
☐ LoginViewModel listo
☐ Login funciona sin errores
☐ Token guardado en SharedPreferences

WEB:
☐ authService.js listo
☐ LoginScreen.vue integrado
☐ useAuth.js funciona
☐ Login redirige a dashboard
☐ Token guardado en localStorage

INTEGRACIÓN:
☐ Android puede hacer requests con JWT
☐ Web puede hacer requests con JWT
☐ Logout limpia tokens
☐ Endpoints protegidos requieren JWT
```

---

## 🐛 Si no funciona

### Backend no inicia

```
Causa: jwt-secret no configurado

Solución:
1. Verifica .env tiene JWT_SECRET_KEY
2. No puede estar vacío
3. Debe tener mínimo 32 caracteres
```

### Android no puede conectar

```
Causa: URL base incorrecta

Solución:
- Emulador: http://10.0.2.2:3000
- Dispositivo: http://192.168.1.x:3000
```

### Login no funciona

```
Causa: Credenciales incorrectas

Solución:
Usa credenciales de prueba:
- admin@logistica.com / admin123
- juan@conductor.com / admin123
```

### Endpoint protegido devuelve 401

```
Causa: Token no enviado o inválido

Solución:
1. Verifica Authorization header
2. Formato debe ser: Bearer {token}
3. Token no puede estar expirado
```

---

## 📝 Comandos Útiles

```bash
# Backend
npm run dev           # Iniciar en desarrollo
npm start            # Iniciar normal
npm test             # Correr tests

# Decodificar JWT (ver contenido)
# Ir a https://jwt.io
# Pegar el token en "Encoded"

# Curl para testing
curl -X GET http://localhost:3000/conductor/perfil \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

---

## 📊 Flujo Rápido

```
1. Usuario abre app / web
   ↓
2. Ve pantalla de login
   ↓
3. Ingresa email + password
   ↓
4. Click "Login"
   ↓
5. App/Web → POST /auth/login al backend
   ↓
6. Backend valida credenciales
   ↓
7. Genera accessToken + refreshToken
   ↓
8. App/Web recibe y guarda en storage local
   ↓
9. App/Web redirige a Home/Dashboard
   ↓
10. En cada request, interceptor agrega:
    Authorization: Bearer {token}
   ↓
11. Backend verifica JWT
    ↓
12. Si válido → permite request
    Si expirado → devuelve 401
    Si inválido → devuelve 401
```

---

**🎉 Ahora tienes JWT funcional en:**
- ✅ Backend Node.js
- ✅ Android Kotlin
- ✅ Admin Panel Vue

**Tiempo: ~15 minutos** ⏱️

**Siguiente:** Leer JWT_AUTHENTICATION_GUIDE.md para detalles completos

