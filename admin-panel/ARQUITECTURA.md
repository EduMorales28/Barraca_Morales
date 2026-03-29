# 🏗️ Arquitectura del Proyecto

## Patrones y Decisiones de Diseño

### Stack Elegido

```
Frontend: React 18 + Vite
Estilos: Tailwind CSS 3
Iconos: Lucide React
Enrutamiento: React Router 6
HTTP: Axios (preparado)
Estado: React Hooks (useState, useContext)
```

**Razones:**
- ⚡ Vite: máxima velocidad en desarrollo
- 🎨 Tailwind: diseño responsive sin CSS duplicado
- 📦 Minimal: Solo lo necesario (sin Redux/Context API innecesarios)
- 🚀 Escalable: estructura prparaaña fácil expansión

---

## Estructura de Carpetas - Explicación

```
src/
├── components/           # Componentes reutilizables
│   ├── Header.jsx       # Componente pequeño/medio
│   ├── Sidebar.jsx      
│   ├── Layout.jsx       # Componente grande (layout)
│   ├── PedidoCard.jsx
│   ├── DashboardStats.jsx
│   ├── PedidosFilters.jsx
│   └── MapComponent.jsx
│
├── pages/               # Páginas/Rutas principales
│   ├── Dashboard.jsx    # "/" - Página principal
│   ├── PedidosList.jsx  # "/pedidos" - Listado
│   ├── CrearPedido.jsx  # "/nuevo-pedido" - Formulario
│   └── AsignarConductor.jsx # "/asignar-conductor/:id"
│
├── hooks/               # Custom Hooks React
│   └── usePedidos.js    # Lógica de estado de pedidos
│       ├── filteredPedidos
│       ├── filterPedidos()
│       └── updatePedido()
│
├── data/                # Datos estáticos/mock
│   └── mockData.js      # Datos de prueba
│       ├── mockPedidos[]
│       ├── mockConductores[]
│       └── mockArticulos[]
│
├── config/              # Configuración
│   └── constants.js     # URLs, valores globales
│
├── styles/              # Estilos globales
│   └── globals.css      # Tailwind + animaciones personalizadas
│
├── App.jsx              # Componente raíz con Router
└── main.jsx             # Entry point
```

### Patrón de Componentes

#### Componente Pequeño (Header, PedidoCard)
```javascript
// Reutilizable
// Props claros
// Lógica mínima
export default function Header({ onMenuClick }) {
  return (
    // JSX simple
  )
}
```

#### Componente Grande (Página)
```javascript
// Más lógica
// State management
// Puede tener sub-componentes
export default function PedidosList() {
  const { filteredPedidos } = usePedidos()
  const [filters, setFilters] = useState({})
  
  return (
    <div>
      <PedidosFilters />
      <PedidoCard /> {/* Reutiliza componente pequeño */}
    </div>
  )
}
```

---

## Flujo de Data

### 1. Desde Mock Data
```
mockData.js
    ↓
usePedidos.js (hook)
    ↓
Página (PedidosList)
    ↓
Componentes (PedidoCard)
    ↓
UI/Usuario
```

### 2. Desde Eventos (Click, Submit)
```
Usuario (Click/Input)
    ↓
Handler en Componente (onClick, onChange)
    ↓
setState | updatePedido()
    ↓
Hooks (usePedidos)
    ↓
Re-render automático
    ↓
UI Actualizada
```

### 3. Próximo: Desde API Real
```
React Component
    ↓
axios.get('/api/pedidos')
    ↓
API Backend (Node.js)
    ↓
Database (Firestore/PostgreSQL)
    ↓
Respuesta JSON
    ↓
setState
    ↓
UI Actualizada
```

---

## Cómo Extender

### Agregar Nueva Página

**1. Crear archivo:**
```javascript
// src/pages/MiPagina.jsx
export default function MiPagina() {
  return (
    <div className="space-y-6">
      <h1>Título</h1>
      {/* Contenido */}
    </div>
  )
}
```

**2. Agregar ruta en App.jsx:**
```javascript
<Route path="/mi-ruta" element={<MiPagina />} />
```

**3. Agregar en Sidebar:**
```javascript
const menuItems = [
  // ... items existentes
  {
    label: 'Mi Página',
    href: '/mi-ruta',
    icon: IconComponent,
  },
]
```

---

### Agregar Nuevo Componente Reutilizable

**1. Crear en src/components:**
```javascript
// src/components/MiComponente.jsx
export default function MiComponente({ prop1, prop2 }) {
  return (
    <div className="...">
      {/* Contenido */}
    </div>
  )
}
```

**2. Importar donde sea necesario:**
```javascript
import MiComponente from '../components/MiComponente'

// Usar
<MiComponente prop1="valor" prop2={valor2} />
```

---

### Agregar Nuevo Hook

**1. Crear en src/hooks:**
```javascript
// src/hooks/useMiHook.js
import { useState, useCallback } from 'react'

export function useMiHook() {
  const [data, setData] = useState([])
  
  const fetchData = useCallback(async () => {
    // Lógica aquí
  }, [])
  
  return { data, fetchData }
}
```

**2. Usar en componente:**
```javascript
import { useMiHook } from '../hooks/useMiHook'

export default function MiComponente() {
  const { data, fetchData } = useMiHook()
  // ... resto del componente
}
```

---

## Integración con API Real

### Paso 1: Crear servicio de API

```javascript
// src/services/api.js
import axios from 'axios'

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:3000/api'

const api = axios.create({
  baseURL: API_URL,
  timeout: 5000,
})

export const pedidoService = {
  getAll: () => api.get('/pedidos'),
  getById: (id) => api.get(`/pedidos/${id}`),
  create: (data) => api.post('/pedidos', data),
  update: (id, data) => api.put(`/pedidos/${id}`, data),
  delete: (id) => api.delete(`/pedidos/${id}`),
}

export const conductorService = {
  getAll: () => api.get('/conductores'),
  assign: (pedidoId, conductorId) => 
    api.post(`/pedidos/${pedidoId}/assign`, { conductorId }),
}

export default api
```

### Paso 2: Actualizar hook

```javascript
// src/hooks/usePedidos.js
import { useState, useEffect } from 'react'
import { pedidoService } from '../services/api'

export function usePedidos() {
  const [pedidos, setPedidos] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  useEffect(() => {
    fetchPedidos()
  }, [])

  const fetchPedidos = async () => {
    setLoading(true)
    try {
      const { data } = await pedidoService.getAll()
      setPedidos(data)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  const createPedido = async (pedidoData) => {
    try {
      const { data } = await pedidoService.create(pedidoData)
      setPedidos([...pedidos, data])
      return data
    } catch (err) {
      throw new Error(err.response?.data?.message || err.message)
    }
  }

  return { pedidos, loading, error, fetchPedidos, createPedido }
}
```

### Paso 3: Usar en componente

```javascript
export default function CrearPedido() {
  const { createPedido, loading } = usePedidos()

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      await createPedido(formData)
      navigate('/pedidos')
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <form onSubmit={handleSubmit}>
      {/* ... */}
      <button disabled={loading}>
        {loading ? 'Creando...' : 'Crear Pedido'}
      </button>
    </form>
  )
}
```

---

## Estados Loading y Error

### Patrón Recomendado

```javascript
export default function MiComponente() {
  const { data, loading, error } = useHook()

  if (loading) {
    return <LoadingSpinner />
  }

  if (error) {
    return <ErrorMessage error={error} />
  }

  return <div>{/* Mostrar data */}</div>
}
```

### Componentes de Utilidad

```javascript
// src/components/LoadingSpinner.jsx
export default function LoadingSpinner() {
  return (
    <div className="flex justify-center items-center min-h-screen">
      <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary" />
    </div>
  )
}

// src/components/ErrorMessage.jsx
export default function ErrorMessage({ error, onRetry }) {
  return (
    <div className="bg-red-50 border border-red-200 rounded-lg p-4">
      <p className="text-red-800 font-medium mb-2">Error</p>
      <p className="text-red-700 text-sm mb-3">{error}</p>
      {onRetry && (
        <button onClick={onRetry} className="text-red-600 font-medium hover:text-red-800">
          Reintentar
        </button>
      )}
    </div>
  )
}
```

---

## Validación y Autenticación

### Form Validation

```javascript
function validateForm(formData) {
  const errors = {}
  
  if (!formData.cliente.trim()) {
    errors.cliente = 'Campo requerido'
  }
  if (!formData.email.includes('@')) {
    errors.email = 'Email inválido'
  }
  if (formData.telefono.length < 10) {
    errors.telefono = 'Teléfono inválido'
  }
  
  return errors
}

// En el componente
const [errors, setErrors] = useState({})

const handleSubmit = (e) => {
  e.preventDefault()
  const formErrors = validateForm(formData)
  
  if (Object.keys(formErrors).length === 0) {
    // Crear pedido
  } else {
    setErrors(formErrors)
  }
}
```

### JWT Auth (Próximo)

```javascript
// src/services/auth.js
export const authService = {
  login: async (email, password) => {
    const { data } = await api.post('/auth/login', { email, password })
    localStorage.setItem('token', data.token)
    return data
  },
  
  logout: () => {
    localStorage.removeItem('token')
  },
  
  getToken: () => localStorage.getItem('token'),
}

// Interceptor en api.js
api.interceptors.request.use(config => {
  const token = authService.getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})
```

---

## Performance

### Code Splitting

```javascript
import { lazy, Suspense } from 'react'

const Dashboard = lazy(() => import('./pages/Dashboard'))
const PedidosList = lazy(() => import('./pages/PedidosList'))

// Usar en Router
<Suspense fallback={<LoadingSpinner />}>
  <Routes>
    <Route path="/" element={<Dashboard />} />
    <Route path="/pedidos" element={<PedidosList />} />
  </Routes>
</Suspense>
```

### Memoization

```javascript
import { memo, useMemo, useCallback } from 'react'

// Evitar re-renders innecesarios
const PedidoCard = memo(({ pedido, onAssign }) => {
  return (/* component */)
})

// Optimizar funciones
const handleFilterChange = useCallback(({ search, estado }) => {
  filterPedidos(search, estado)
}, [])

// Optimizar datos complejos
const filteredData = useMemo(() => {
  return data.filter(...)
}, [data])
```

---

## Testing (Próximo)

```javascript
// src/__tests__/components/PedidoCard.test.jsx
import { render, screen } from '@testing-library/react'
import PedidoCard from '../../components/PedidoCard'

describe('PedidoCard', () => {
  it('renders pedido information', () => {
    const pedido = { id: 'P1', cliente: 'Test', /* ... */ }
    render(<PedidoCard pedido={pedido} />)
    
    expect(screen.getByText('Test')).toBeInTheDocument()
  })
})
```

---

## Deployment

### Vite Build

```bash
npm run build
# Crea carpeta dist/ lista para producir
```

### Opciones de Deploy

1. **Vercel** (recomendado)
   ```bash
   npm i -g vercel
   vercel
   ```

2. **Netlify**
   - Conectar repositorio Git
   - Build command: `npm run build`
   - Publish directory: `dist`

3. **GitHub Pages**
   - Agregar en vite.config.js: `base: '/admin-panel/'`
   - Push a rama `gh-pages`

4. **Servidor propio**
   ```bash
   npm run build
   scp -r dist/* usuario@servidor:/var/www/html
   ```

---

## Checklist de Completitud

- [x] Componentes base (Header, Sidebar, Layout)
- [x] Páginas principales (4 páginas)
- [x] Datos mock en mockData.js
- [x] Estilos Tailwind
- [x] Responsivo
- [x] Estructura para API (servicios, hooks)
- [ ] Autenticación JWT
- [ ] Tests
- [ ] Error handling global
- [ ] Notifications/Toast
- [ ] Dark mode
- [ ] i18n (Internacionalización)

---

**Arquitectura diseñada para ser escalable, mantenible y lista para API real.** 🚀
