import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import Layout from './components/Layout'
import Dashboard from './pages/Dashboard'
import PedidosList from './pages/PedidosList'
import CrearPedido from './pages/CrearPedido'
import AsignarConductor from './pages/AsignarConductor'

function App() {
  return (
    <Router>
      <Routes>
        <Route element={<Layout />}>
          <Route path="/" element={<Dashboard />} />
          <Route path="/pedidos" element={<PedidosList />} />
          <Route path="/nuevo-pedido" element={<CrearPedido />} />
          <Route path="/asignar-conductor/:pedidoId" element={<AsignarConductor />} />
        </Route>
      </Routes>
    </Router>
  )
}

export default App
