# 🚀 Guía de Inicio Rápido - Barraca Admin Panel

## 5 minutos para que funcione

### Paso 1: Instalar dependencias
```bash
cd admin-panel
npm install
```

**Tiempo estimado:** 2-3 minutos (dependiendo de conexión)

### Paso 2: Ejecutar en desarrollo
```bash
npm run dev
```

**Resultado:** La aplicación abrirá automáticamente en http://localhost:5173

### ✅ ¡Listo!

Ya puedes:
- Ver el Dashboard en `/`
- Ver listado de pedidos en `/pedidos`
- Crear un nuevo pedido en `/nuevo-pedido`
- Asignar conductor a un pedido

---

## 📍 Dónde navegar primero

1. **Dashboard** - Bienvenida y estadísticas
2. **Pedidos** - Ver todos los pedidos de prueba
3. **Crear Pedido** - Formulario para nuevo pedido
4. **Asignar Conductor** - Click en "Asignar Conductor" en cualquier tarjeta

---

## 📁 Archivos Importantes

| Archivo | Para Qué |
|---------|----------|
| `src/App.jsx` | Rutas principales |
| `src/pages/` | Las 4 pantallas principales |
| `src/components/` | Componentes reutilizables |
| `src/data/mockData.js` | Datos de prueba |
| `tailwind.config.js` | Colores y temas |

---

## 🎯 Estructura de menú

```
Dashboard          /
├─ Pedidos         /pedidos
├─ Nuevo Pedido    /nuevo-pedido
├─ Conductores     /conductores
└─ Configuración   /configuracion
```

---

## 🔄 Datos de Prueba Incluidos

- **5 Pedidos:** con diferentes estados y clientes
- **4 Conductores:** disponibles y ocupados
- **10 Artículos:** con precios y categorías

Todos los datos vienen en `src/data/mockData.js`

---

## 💡 Próximos pasos

1. **Aplicar estilos personalizados:**
   - Editar colores en `tailwind.config.js`
   - Cambiar logo en Header y Sidebar

2. **Conectar con API:**
   - Reemplazar mockData con axios
   - Agregar autenticación

3. **Agregar nuevas pantallas:**
   - Crear archivo en `src/pages/`
   - Agregar ruta en `App.jsx`

---

## 🐛 Troubleshooting

### "Port 5173 ya está en uso"
```bash
npm run dev -- --port 5174
```

### "Error con Tailwind"
```bash
rm -rf node_modules package-lock.json
npm install
npm run dev
```

### "No se ve el sidebar"
Asegúrate que `Layout.jsx` está importado correctamente en `App.jsx`

---

## 📚 Documentación Completa

- [README.md](README.md) - Documentación general
- [COMPONENTES.md](COMPONENTES.md) - Detalle de cada componente
- [.env.example](.env.example) - Variables de entorno

---

**¡Ahora sí! Comenzá a explorar el panel.** 🎉
