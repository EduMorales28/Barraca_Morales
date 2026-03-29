# 🏗️ Barraca Admin Panel

Panel administrador web para gestión de pedidos de entrega de materiales de construcción. Construido con **React + Tailwind CSS + Vite**.

## ✨ Características

- 📊 **Dashboard** - Estadísticas y resumen de pedidos
- 📋 **Gestión de Pedidos** - Crear, listar, filtrar pedidos
- 🗺️ **Mapa Integrado** - Visualización de ubicaciones de entrega
- 🚗 **Asignar Conductores** - Sistema de asignación de entregas
- 📱 **Interfaz Responsive** - Compatible con Desktop, Tablet y Mobile
- 🎨 **Diseño Moderno** - Tailwind CSS con colores corporativos

## 🏗️ Estructura de Carpetas

```
admin-panel/
├── src/
│   ├── components/          # Componentes reutilizables
│   │   ├── Header.jsx       # Header principal
│   │   ├── Sidebar.jsx      # Menú lateral
│   │   ├── Layout.jsx       # Layout principal
│   │   ├── PedidoCard.jsx   # Tarjeta de pedido
│   │   ├── DashboardStats.jsx
│   │   ├── PedidosFilters.jsx
│   │   └── MapComponent.jsx
│   ├── pages/               # Páginas principales
│   │   ├── Dashboard.jsx    # Pantalla principal
│   │   ├── PedidosList.jsx  # Listado de pedidos
│   │   ├── CrearPedido.jsx  # Formulario crear pedido
│   │   └── AsignarConductor.jsx
│   ├── hooks/               # Custom hooks
│   │   └── usePedidos.js
│   ├── data/                # Datos mock
│   │   └── mockData.js
│   ├── styles/              # Estilos globales
│   │   └── globals.css
│   ├── App.jsx              # Componente principal
│   └── main.jsx             # Entry point
├── index.html               # HTML template
├── package.json             # Dependencias
├── vite.config.js           # Config Vite
├── tailwind.config.js       # Config Tailwind
└── postcss.config.js        # Config PostCSS
```

## 🚀 Instalación y Uso

### 1. Instalar dependencias

```bash
cd admin-panel
npm install
```

### 2. Ejecutar en desarrollo

```bash
npm run dev
```

La aplicación abrirá automáticamente en http://localhost:5173

### 3. Compilar para producción

```bash
npm run build
```

### 4. Preview de producción

```bash
npm run preview
```

## 📋 Secciones del Panel

### 1. **Dashboard** (`/`)
- Estadísticas generales (Total de pedidos, Pendientes, En reparto, Entregados)
- Tabla de pedidos recientes
- Lista de conductores activos
- KPIs principales (Tasa de entrega, Ingresos, Crecimiento)

### 2. **Pedidos** (`/pedidos`)
- Listado de todos los pedidos
- Filtros por estado (Pendiente, En reparto, Entregado)
- Búsqueda por cliente, dirección o ID
- Tarjetas de pedido con:
  - Información del cliente
  - Dirección y barrio
  - Lista de artículos
  - Conductor asignado
  - Monto total
  - Estado visual

### 3. **Crear Pedido** (`/nuevo-pedido`)
- Formulario para crear nuevos pedidos
- Datos del cliente (nombre, email, teléfono)
- Dirección y barrio de entrega
- Sistema de agregar artículos con cantidad
- Cálculo automático de monto total
- Resumen antes de crear

### 4. **Asignar Conductor** (`/asignar-conductor/:pedidoId`)
- Visualización del pedido a entregar
- Mapa con ubicación de entrega
- Listado de conductores disponibles
- Información detallada de cada conductor:
  - Nombre y vehículo
  - Placa y capacidad
  - Teléfono y estado
- Selección y confirmación de asignación

## 🎨 Temas y Colores

```js
// Colors - tailwind.config.js
primary: '#ef6b3e'      // Naranja corporativo
secondary: '#2d3748'    // Gris oscuro (Sidebar)
success: '#48bb78'      // Verde
warning: '#ed8936'      // Naranja
danger: '#f56565'       // Rojo
```

## 📦 Dependencias Principales

- **React 18.2** - Framework UI
- **React Router 6.20** - Enrutamiento
- **Tailwind CSS 3.3** - Estilos
- **Lucide React 0.305** - Iconos
- **Vite 5.0** - Build tool
- **Date-fns 2.30** - Manejo de fechas
- **Axios 1.6** - Cliente HTTP (preparado)

## 🔌 Integración con API

El proyecto está preparado para conectar con una API REST. Los hooks y servicios ya están estructurados:

```javascript
// usePedidos.js - Hook para estado de pedidos
const { pedidos, filteredPedidos, filterPedidos, updatePedido } = usePedidos()

// Cambiar mockData.js por llamadas a API
const response = await axios.get('/api/pedidos')
```

## 📱 Datos de Prueba (Mock Data)

El proyecto incluye datos de ejemplo en `src/data/mockData.js`:

- **5 Pedidos** con diferentes estados (pendiente, en_reparto, entregado)
- **4 Conductores** disponibles con información completa
- **10 Articulos** de barraca con categorías y precios

Para conectar con una API real, simplemente:

1. Reemplazar `mockData.js` con llamadas HTTP
2. Usar `axios` o `fetch` en los hooks
3. Mantener la misma estructura de datos

## 🎯 Próximas Mejoras

- ✅ Integración con Google Maps (API key necesaria)
- ✅ Autenticación de usuarios
- ✅ Reportes y estadísticas avanzadas
- ✅ Notificaciones en tiempo real
- ✅ Historial de entregas
- ✅ Gestión de conductores
- ✅ Edición de pedidos
- ✅ Export a PDF/Excel

## 🔐 Seguridad

- Variables de entorno para configuración sensible
- Validación de entrada en formularios
- Estructura lista para autenticación JWT
- CORS configurado

## 📊 Estructura de Datos

### Pedido
```javascript
{
  id: 'PED-001',
  cliente: 'Juan García',
  email: 'juan@example.com',
  telefono: '+54 9 11 2345-6789',
  direccion: 'Av. Rivadavia 1800',
  barrio: 'Almagro',
  coordenadas: { lat: -34.6037, lng: -58.3816 },
  monto: 2500.00,
  estado: 'pendiente' | 'en_reparto' | 'entregado',
  fecha: '2026-03-28',
  articulos: [{ id, nombre, cantidad, precio }],
  conductor: { id, nombre, placa, telefono } | null
}
```

### Conductor
```javascript
{
  id: 'COND-001',
  nombre: 'Roberto Martínez',
  telefono: '+54 9 11 1111-1111',
  placa: 'ABC-123',
  modelo: 'Chevrolet S10',
  capacidad: '1500 kg',
  estado: 'activo' | 'disponible' | 'no_disponible',
  pedidosActuales: 2
}
```

## 🛠️ Desarrollo

### Scripts disponibles

```bash
npm run dev      # Desarrollo con hot reload
npm run build    # Build para producción
npm run preview  # Preview del build
npm run lint     # Análisis de código
```

### Agregar nuevas páginas

1. Crear archivo en `src/pages/MiPagina.jsx`
2. Importar en `src/App.jsx`
3. Agregar ruta en Router:

```javascript
<Route path="/mi-ruta" element={<MiPagina />} />
```

### Agregar nuevos componentes

1. Crear en `src/components/MiComponente.jsx`
2. Importar donde sea necesario
3. Usar como:

```javascript
import MiComponente from '../components/MiComponente'
```

## 📞 Soporte

Para consultas técnicas sobre la implementación, revisar:
- README en cada carpeta de componentes
- Comentarios en el código
- Estructura de datos en `mockData.js`

## 📄 Licencia

Proyecto privado - Barraca 2026

---

**Desarrollado con ❤️ usando React + Tailwind CSS**

Happy coding! 🚀
