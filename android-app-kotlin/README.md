# 🚗 Android App - Conductor (Kotlin + MVVM + Compose)

App Android para conductores de entrega con integración Google Maps, cámara y sincronización en tiempo real.

## 📋 Requisitos

- Android Studio Flamingo+
- Kotlin 1.8+
- Mínimo API 26 (Android 8.0)

## 🏗️ Stack Tecnológico

- **Kotlin** - Lenguaje principal
- **Jetpack Compose** - UI moderna
- **MVVM** - Arquitectura
- **Retrofit 2** - Networking
- **Room** - Base de datos local
- **Hilt** - Inyección de dependencias
- **Google Maps** - Mapas
- **Coil** - Carga de imágenes
- **Coroutines** - Programación asincrónica

## 📁 Estructura de Carpetas

```
src/main/
├── kotlin/com/barraca/conductor/
│   ├── di/                          # Inyección de dependencias
│   ├── data/
│   │   ├── api/                     # Retrofit services
│   │   ├── model/                   # Data classes
│   │   ├── repository/              # Data layer
│   │   └── local/                   # Room database
│   ├── domain/                      # Business logic
│   ├── ui/
│   │   ├── screens/                 # Pantallas principales
│   │   ├── composables/             # Componentes reutilizables
│   │   ├── navigation/              # Navigation setup
│   │   └── theme/                   # Tema de diseño
│   ├── viewmodel/                   # ViewModels MVVM
│   ├── utils/                       # Utilidades
│   └── MainActivity.kt              # Entry point
│
└── res/
    ├── drawable/
    ├── mipmap/
    └── values/
```

---

## 🚀 Cómo Usar

### 1. Clonar/Abrir proyecto
```bash
# En Android Studio: File > Open > android-app-kotlin
```

### 2. Configurar Firebase/Backend
Edita `ApiClient.kt` con tu URL de servidor

### 3. Agregar Google Maps API Key
En `AndroidManifest.xml`, reemplaza tu clave API

### 4. Ejecutar en emulador
```bash
./gradlew build
./gradlew installDebug
```

---

## 📱 Pantallas

### 1. **Lista de Pedidos** - `PedidosScreen`
- Grid/Lista de pedidos asignados
- Estado visual (pendiente, en proceso, completado)
- Búsqueda y filtros
- Pull-to-refresh

### 2. **Detalle de Pedido** - `PedidoDetailScreen`
- Información del cliente
- Dirección y mapa interactivo
- Lista de artículos
- Acciones (foto, observaciones)

### 3. **Marcar Entregado** - `EntregaScreen`
- Cámara para foto de entrega
- Firma del cliente
- Observaciones
- Botón subir

---

## 🔑 Características Clave

✅ Listado de pedidos con estado
✅ Detalle completo de pedido
✅ Google Maps integrado
✅ Captura de fotos
✅ Sincronización backend
✅ Manejo de errores
✅ Offline-first (Room)
✅ Inyección de dependencias (Hilt)

---

## 📚 Documentación Incluida

- `SETUP.md` - Configuración paso a paso
- `COMPONENTES.md` - Detalles de cada componente
- `ARQUITECTURA.md` - Decisiones de diseño

---

## 📞 Contacto

Para dudas de implementación, revisar archivos comentados.

---

Ver carpeta `src/main/kotlin/com/barraca/conductor/` para el código completo.
