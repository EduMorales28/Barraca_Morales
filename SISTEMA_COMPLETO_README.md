# 🚀 BARRACA MORALES - SISTEMA COMPLETO

## 📦 Estructura Final

```
PruebaAndroid/
├── backend-complete/
│   ├── index.js                    (Servidor Express)
│   ├── package.json                (Dependencias)
│   ├── barraca.db                  (SQLite - automático)
│   ├── uploads/                    (Fotos entregas)
│   └── README.md
│
├── web-admin/
│   ├── src/
│   │   ├── main.jsx
│   │   ├── App.jsx
│   │   ├── index.css
│   │   ├── api.js
│   │   └── pages/
│   │       ├── LoginPage.jsx
│   │       └── DashboardPage.jsx
│   ├── index.html
│   ├── vite.config.js
│   ├── tailwind.config.js
│   ├── postcss.config.js
│   ├── package.json
│   └── README.md
│
└── android-app-kotlin/
    ├── app/
    │   ├── src/main/
    │   │   ├── kotlin/com/barraca/app/
    │   │   │   ├── MainActivity.kt
    │   │   │   ├── Models.kt
    │   │   │   ├── api/
    │   │   │   │   ├── ApiService.kt
    │   │   │   │   └── ApiClient.kt
    │   │   │   ├── viewmodel/
    │   │   │   │   ├── AuthViewModel.kt
    │   │   │   │   └── PedidosViewModel.kt
    │   │   │   └── ui/screens/
    │   │   │       ├── LoginScreen.kt
    │   │   │       └── PedidosScreen.kt
    │   │   ├── res/
    │   │   │   ├── xml/
    │   │   │   │   └── file_paths.xml
    │   │   │   └── values/
    │   │   │       ├── strings.xml
    │   │   │       ├── colors.xml
    │   │   │       └── themes.xml
    │   │   └── AndroidManifest.xml
    │   └── build.gradle.kts
    ├── build.gradle.kts
    ├── settings.gradle.kts
    └── README.md
```

---

## 🟢 EJECUCIÓN

### 1️⃣ BACKEND

```bash
cd backend-complete
npm install
node index.js
```

**Puerto:** 3000  
**Status:** http://localhost:3000/health

---

### 2️⃣ WEB ADMIN

```bash
cd web-admin
npm install
npm run dev
```

**URL:** http://localhost:5173  
**Login:** admin@test.com / 1234

---

### 3️⃣ APP ANDROID

1. Abrir en Android Studio
2. Esperar a que sincronicen dependencias
3. Seleccionar emulador (API 26+)
4. Run 'app'

**Login:** conductor1@test.com / 1234

---

## ✅ VERIFICACIÓN

### Backend
```bash
# Test login
curl -X POST http://localhost:3000/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@test.com","password":"1234"}'
```

### Web
- Abrir http://localhost:5173
- Login con admin@test.com / 1234
- Ver lista de pedidos
- Crear nuevo pedido
- Asignar a conductor

### Android
- Login con conductor1@test.com / 1234
- Ver "Mis pedidos"
- Abrir un pedido
- Ver detalles
- Registrar entrega (foto + observaciones)

---

## 🔗 CONEXIONES

| Sistema | Backend URL | Status |
|---------|------------|--------|
| Web Admin | http://localhost:3000 | ✅ Automática |
| App Android | http://10.0.2.2:3000 | ✅ Automática |
| Emulador conecta a | Localhost del host | ✅ 10.0.2.2 |

---

## 📋 DATOS PRECARGADOS

### Usuarios
```
Admin:
  Email: admin@test.com
  Password: 1234

Conductor 1:
  Email: conductor1@test.com
  Password: 1234

Conductor 2:
  Email: conductor2@test.com
  Password: 1234
```

### Pedidos (3)
- Empresa A → Av. Principal 123 → Juan García (asignado)
- Empresa B → Calle 9 de Julio 456 → Carlos López (asignado)
- Local C → San Isidro 789 → Juan García (pendiente)

---

## 🗄️ BASE DE DATOS

**Tipo:** SQLite  
**Archivo:** `backend-complete/barraca.db`  
**Creación:** Automática al iniciar

### Tablas
- `usuarios` (id, nombre, email, password, rol)
- `pedidos` (id, cliente, direccion, lat, lng, estado, conductor_id)
- `items_pedido` (id, pedido_id, nombre, cantidad)
- `entregas` (id, pedido_id, foto, observaciones, fecha)

---

## 🎯 FLUJOS PRINCIPALES

### Flujo Admin
1. **Login** → admin@test.com / 1234
2. **Dashboard** → Ver todos los pedidos
3. **Crear Pedido** → Llenar datos + items
4. **Asignar** → Seleccionar conductor
5. **Ver Detalles** → Información completa

### Flujo Conductor (Android)
1. **Login** → conductor1@test.com / 1234
2. **Lista** → Mis pedidos asignados
3. **Detalles** → Cliente, dirección, items, mapa
4. **Entregar** → Tomar foto + observaciones
5. **Enviar** → Registra entrega, cambia estado

---

## ⚙️ CONFIGURACIÓN IMPORTANTE

### Cambiar IP (dispositivo físico)

**En `android-app-kotlin/app/src/main/kotlin/com/barraca/app/api/ApiClient.kt`:**

```kotlin
// Cambiar:
private const val BASE_URL = "http://10.0.2.2:3000/"

// Por tu IP local (ej):
private const val BASE_URL = "http://192.168.1.100:3000/"
```

### Cambiar URL Web (otro host)

**En `web-admin/src/api.js`:**

```javascript
// Cambiar:
const API_URL = 'http://localhost:3000'

// Por tu IP local (ej):
const API_URL = 'http://192.168.1.100:3000'
```

---

## 📸 CARACTERÍSTICAS

✅ Autenticación con email/password  
✅ SQLite local (sin servidor externo)  
✅ Cámara integrada en Android  
✅ Subida de fotos multipart  
✅ Geolocalización (lat/lng)  
✅ Estados de pedidos  
✅ Asignación a conductores  
✅ Lista de ítems por pedido  
✅ UI responsiva  
✅ CORS habilitado  
✅ Manejo de errores básico  
✅ Datos precargados

---

## 🛠️ TROUBLESHOOTING

### Backend no inicia
```
→ Verificar que puerto 3000 está disponible
→ Eliminar node_modules: rm -rf node_modules
→ Reinstalar: npm install
```

### Android no conecta
```
→ Verificar emulador esté corriendo
→ Cambiar BASE_URL a IP local si es dispositivo físico
→ Verificar que backend esté corriendo
→ Revisar que AndroidManifest.xml tenga INTERNET permission
```

### Web no carga
```
→ Verificar puerto 5173 disponible
→ Verificar que backend esté en 3000
→ Limpiar cache del navegador
```

### SQLite error
```
→ Eliminar: backend-complete/barraca.db
→ Reiniciar backend (se recreará)
```

---

**Version:** 1.0.0  
**Estado:** ✅ LISTO PARA PRODUCCIÓN LOCAL  
**Última actualización:** 28 Marzo 2026
