# 📖 Documentación de Componentes

## 🎨 Sistema de Componentes

El panel admin utiliza un sistema modular de componentes React con Tailwind CSS.

---

## 📦 Componentes Principales

### Header.jsx
**Ubicación responsable**: Encabezado principal del panel

```javascript
<Header onMenuClick={() => setSidebarOpen(!sidebarOpen)} />
```

**Props:**
- `onMenuClick` (function) - Callback cuando se hace click en el botón de menú

**Características:**
- Botón de toggle del sidebar
- Notificaciones con badge
- Menú de usuario
- Nombre y rol del usuario actual

---

### Sidebar.jsx
**Ubicación responsable**: Menú de navegación lateral

```javascript
<Sidebar isOpen={sidebarOpen} />
```

**Props:**
- `isOpen` (boolean) - Controla si el sidebar está expandido

**Características:**
- Logo de la barraca
- Menú de navegación (Dashboard, Pedidos, Nuevo Pedido, Conductores)
- Indicador de página activa
- Versión del app

**Ítems del menú:**
- Dashboard `/`
- Pedidos `/pedidos`
- Nuevo Pedido `/nuevo-pedido`
- Conductores `/conductores`
- Configuración `/configuracion`

---

### PedidoCard.jsx
**Ubicación responsable**: Tarjeta individual de un pedido

```javascript
<PedidoCard pedido={pedidoData} onAssignClick={(id) => console.log(id)} />
```

**Props:**
- `pedido` (object) - Datos del pedido
- `onAssignClick` (function) - Callback para asignar conductor

**Datos esperados (pedido):**
```javascript
{
  id: 'PED-001',
  cliente: 'Juan García',
  direccion: 'Av. Rivadavia 1800',
  barrio: 'Almagro',
  monto: 2500.00,
  estado: 'pendiente' | 'en_reparto' | 'entregado',
  fecha: '2026-03-28',
  articulos: [
    { nombre: 'Cemento 50kg', cantidad: 5 },
    { nombre: 'Arena gruesa', cantidad: 2 }
  ],
  conductor: {
    nombre: 'Roberto Martínez',
    placa: 'ABC-123'
  } | null
}
```

**Características:**
- Muestra resumen del pedido
- Código de color según estado
- Lista truncada de artículos
- Información del conductor asignado
- Botón para asignar conductor (si no tiene)

---

### DashboardStats.jsx
**Ubicación responsable**: Tarjetas de estadísticas del dashboard

```javascript
<DashboardStats stats={{
  totalPedidos: 24,
  pendientes: 8,
  enReparto: 5,
  entregados: 11
}} />
```

**Props:**
- `stats` (object) - Objeto con estadísticas (valores por defecto incluidos)

**Valores esperados:**
```javascript
{
  totalPedidos: number,
  pendientes: number,
  enReparto: number,
  entregados: number
}
```

**Características:**
- 4 tarjetas de estadísticas
- Iconos con colores distintivos
- Valores grandes y legibles
- Tendencias visuales

---

### PedidosFilters.jsx
**Ubicación responsable**: Barra de filtros para listado de pedidos

```javascript
<PedidosFilters onFilterChange={({ search, estado }) => {}} />
```

**Props:**
- `onFilterChange` (function) - Callback cuando cambian los filtros

**Emite:**
```javascript
{
  search: string,    // Texto de búsqueda
  estado: string     // Estado seleccionado
}
```

**Características:**
- Búsqueda por cliente/dirección/ID
- Filtro por estado (Todos, Pendientes, En Reparto, Entregados)
- Botones para más filtros y descargar
- Botón para crear nuevo pedido

---

### MapComponent.jsx
**Ubicación responsable**: Visualización de mapa de entrega

```javascript
<MapComponent 
  direccion="Av. Rivadavia 1800"
  coordenadas={{ lat: -34.6037, lng: -58.3816 }}
/>
```

**Props:**
- `direccion` (string) - Dirección de entrega
- `coordenadas` (object) - Coordenadas GPS { lat, lng }

**Características:**
- Placeholder para Google Maps (preparado para integración)
- Muestra dirección y coordenadas
- Botón para abrir en Google Maps
- Aviso de próxima integración

**Próxima integración:**
```javascript
import { useJsApiLoader, GoogleMap, Marker } from '@react-google-maps/api'
```

---

## 🎯 Páginas Principales

### Dashboard.jsx (`/`)

**Propósito:** Página principal con estadísticas y resumen

**Componentes utilizados:**
- Header
- Sidebar
- DashboardStats
- Tabla de pedidos recientes
- Tarjetas de estadísticas adicionales

**Datos:**
- Usa `mockPedidos` de `mockData.js`
- Calcula estadísticas sobre la marcha

**Funcionalidades:**
- Resumen visual de comportamiento
- Tabla con últimos 5 pedidos
- Lista de conductores activos
- KPIs principales

---

### PedidosList.jsx (`/pedidos`)

**Propósito:** Listado y gestión de todos los pedidos

**Componentes utilizados:**
- PedidosFilters
- PedidoCard (grid)

**Estado local:**
```javascript
const { filteredPedidos, filterPedidos } = usePedidos()
```

**Funcionalidades:**
- Búsqueda en tiempo real
- Filtrado por estado
- Grid responsive de pedidos
- Navegación a asignación de conductor
- Paginación (estructura preparada)

**Handlers:**
- `handleFilterChange()` - Actualiza filtros
- `handleAssignClick()` - Navega a asignar conductor

---

### CrearPedido.jsx (`/nuevo-pedido`)

**Propósito:** Formulario para crear nuevos pedidos

**Estados manejados:**
```javascript
const [formData, setFormData] = useState({
  cliente: '',
  email: '',
  telefono: '',
  direccion: '',
  barrio: '',
  articulos: []
})

const [articulosSeleccionados, setArticulosSeleccionados] = useState([])
const [articuloActual, setArticuloActual] = useState({
  id: null,
  cantidad: 1
})
```

**Funcionalidades:**
- Formulario con validación básica
- Agregar múltiples artículos
- Cálculo automático de monto total
- Preview de artículos antes de crear
- Eliminar artículos del carrito
- Confirmación y creación de pedido

**Validaciones:**
- Cliente requerido
- Teléfono requerido
- Dirección requerida
- Barrio requerido
- Mínimo un artículo

---

### AsignarConductor.jsx (`/asignar-conductor/:pedidoId`)

**Propósito:** Página para asignar un conductor a un pedido específico

**Parámetros de ruta:**
```javascript
const { pedidoId } = useParams()
```

**Estados:**
```javascript
const [conductorSeleccionado, setConductorSeleccionado] = useState(null)
const [asignado, setAsignado] = useState(false)
```

**Funcionalidades:**
- Muestra detalles del pedido
- Mapa con ubicación de entrega
- Listado de conductores disponibles
- Selección de conductor (UI con highlight)
- Confirmación de asignación
- Confirmación visual después de asignar

**Información del conductor mostrada:**
- Nombre completo
- Modelo de vehículo
- Placa
- Capacidad
- Teléfono
- Estado

---

## 🪝 Custom Hooks

### usePedidos.js

**Propósito:** Gestión del estado de pedidos

```javascript
const { pedidos, filteredPedidos, filterPedidos, updatePedido } = usePedidos()
```

**Métodos:**
- `filterPedidos(search, estado)` - Filtra pedidos por búsqueda y estado
- `updatePedido(pedidoId, updates)` - Actualiza datos de un pedido
- `pedidos` - Array completo de pedidos
- `filteredPedidos` - Pedidos después de aplicar filtros

**Ejemplo de uso:**
```javascript
const { filteredPedidos, filterPedidos } = usePedidos()

const handleFilterChange = ({ search, estado }) => {
  filterPedidos(search, estado)
}
```

---

## 🎨 Tailwind Utilities Personalizadas

En `globals.css` se definen:

```css
/* Animaciones */
@keyframes slideIn { /* Desliza de izquierda a derecha */ }
@keyframes fadeIn { /* Desvanece entrada */ }

.animate-slideIn { animation: slideIn 0.3s ease-out; }
.animate-fadeIn { animation: fadeIn 0.3s ease-out; }
```

**Uso:**
```javascript
<div className="animate-fadeIn">
  Contenido
</div>
```

---

## 📋 Estructura de Props Comunes

### Objeto Pedido
```typescript
interface Pedido {
  id: string                      // "PED-001"
  cliente: string                 // "Juan García"
  email: string                   // "juan@example.com"
  telefono: string                // "+54 9 11 2345-6789"
  direccion: string               // "Av. Rivadavia 1800"
  barrio: string                  // "Almagro"
  coordenadas: {                  // Ubicación GPS
    lat: number
    lng: number
  }
  monto: number                   // 2500.00
  estado: 'pendiente' | 'en_reparto' | 'entregado'
  fecha: string                   // "2026-03-28"
  articulos: Array<{
    id: number
    nombre: string
    cantidad: number
    precio: number
  }>
  conductor: null | {
    id: string
    nombre: string
    placa: string
    telefono: string
  }
}
```

### Objeto Conductor
```typescript
interface Conductor {
  id: string                      // "COND-001"
  nombre: string                  // "Roberto Martínez"
  telefono: string                // "+54 9 11 1111-1111"
  placa: string                   // "ABC-123"
  modelo: string                  // "Chevrolet S10"
  capacidad: string               // "1500 kg"
  estado: 'activo' | 'disponible' | 'no_disponible'
  pedidosActuales: number         // 2
}
```

---

## 🔄 Flujo de Data

```
Página (Dashboard)
    ↓
useHook (usePedidos)
    ↓
mockData.js (o API)
    ↓
Componentes (PedidoCard, DashboardStats)
    ↓
Eventos (onClick, onChange)
    ↓
Actualizar estado
    ↓
Re-render
```

---

## ✅ Checklist de Integración con API

Para conectar con una API real:

- [ ] Reemplazar imports de `mockData.js` con llamadas HTTP
- [ ] Usar `axios` o `fetch` en hooks
- [ ] Mantener estructura de datos igual
- [ ] Agregar validación de errores
- [ ] Agregar loading states
- [ ] Configurar variables de entorno (.env)
- [ ] Implementar autenticación JWT
- [ ] Agregar timeout y reintentos
- [ ] Actualizar README con endpoints

---

**Última actualización:** 28 de Marzo de 2026
