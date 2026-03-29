// Rutas para Administradores (Protegidas con JWT + requireAdmin)
// Archivo: src/routes/admin.js

const express = require('express');
const router = express.Router();
const { verifyToken, requireAdmin } = require('../middleware/auth');

/**
 * GET /admin/conductores
 * Obtener lista de conductores
 * Protegido: Requiere token JWT y rol admin
 */
router.get('/conductores', verifyToken, requireAdmin, (req, res) => {
  try {
    // Mock data - en producción consultar BD
    const conductores = [
      {
        id: 'conductor_001',
        nombre: 'Juan García',
        email: 'juan@conductor.com',
        telefono: '+34 666 777 888',
        estado: 'activo',
        tienePedidosActivos: 2,
        vehiculo: {
          placa: 'ABC-1234',
          tipo: 'moto'
        },
        ubicacion: {
          latitud: 40.7128,
          longitud: -74.0060,
          actualizado: new Date()
        }
      },
      {
        id: 'conductor_002',
        nombre: 'María López',
        email: 'maria@conductor.com',
        telefono: '+34 666 777 889',
        estado: 'activo',
        tienePedidosActivos: 1,
        vehiculo: {
          placa: 'XYZ-5678',
          tipo: 'auto'
        },
        ubicacion: {
          latitud: 40.7130,
          longitud: -74.0065,
          actualizado: new Date()
        }
      }
    ];

    res.status(200).json({
      success: true,
      data: {
        conductores: conductores,
        total: conductores.length
      }
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: 'Error obteniendo conductores'
    });
  }
});

/**
 * GET /admin/pedidos
 * Obtener todos los pedidos
 * Protegido: Requiere token JWT y rol admin
 */
router.get('/pedidos', verifyToken, requireAdmin, (req, res) => {
  try {
    const { estado, page = 1, limit = 20 } = req.query;

    // Mock data
    const pedidos = [
      {
        id: '001',
        numero: '12345',
        cliente: 'Juan García',
        direccion: 'Calle Principal 123',
        estado: 'pendiente',
        conductorAsignado: 'conductor_001',
        conductorNombre: 'Juan García',
        montoTotal: 150.00,
        fechaCreacion: new Date(),
        fechaAsignacion: new Date()
      },
      {
        id: '002',
        numero: '12346',
        cliente: 'María López',
        direccion: 'Avenida Central 456',
        estado: 'en_ruta',
        conductorAsignado: 'conductor_002',
        conductorNombre: 'María López',
        montoTotal: 200.00,
        fechaCreacion: new Date(),
        fechaAsignacion: new Date()
      }
    ];

    res.status(200).json({
      success: true,
      data: {
        pedidos: pedidos,
        total: pedidos.length,
        page: page,
        limit: limit
      }
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: 'Error obteniendo pedidos'
    });
  }
});

/**
 * POST /admin/pedidos/asignar
 * Asignar pedido a un conductor y enviar notificación FCM
 * Protegido: Requiere token JWT y rol admin
 */
router.post('/pedidos/asignar', verifyToken, requireAdmin, (req, res) => {
  try {
    const { pedidoId, conductorId } = req.body;

    if (!pedidoId || !conductorId) {
      return res.status(400).json({
        success: false,
        error: 'pedidoId y conductorId requeridos'
      });
    }

    // En producción:
    // 1. Actualizar BD
    // 2. Obtener token FCM del conductor
    // 3. Enviar notificación FCM
    // 4. Enviar email al conductor

    console.log(`📬 Pedido ${pedidoId} asignado a conductor ${conductorId}`);
    console.log(`   Enviando notificación FCM al conductor...`);

    res.status(200).json({
      success: true,
      data: {
        message: 'Pedido asignado y notificación enviada',
        pedidoId: pedidoId,
        conductorId: conductorId,
        asignadoEn: new Date()
      }
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: 'Error asignando pedido'
    });
  }
});

/**
 * PUT /admin/pedidos/:pedidoId
 * Actualizar pedido
 * Protegido: Requiere token JWT y rol admin
 */
router.put('/pedidos/:pedidoId', verifyToken, requireAdmin, (req, res) => {
  try {
    const { pedidoId } = req.params;
    const { estado, cliente, direccion, montoTotal } = req.body;

    console.log(`✏️ Pedido ${pedidoId} actualizado`);

    res.status(200).json({
      success: true,
      data: {
        id: pedidoId,
        actualizado: new Date()
      }
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: 'Error actualizando pedido'
    });
  }
});

/**
 * GET /admin/estadisticas
 * Obtener estadísticas del sistema
 * Protegido: Requiere token JWT y rol admin
 */
router.get('/estadisticas', verifyToken, requireAdmin, (req, res) => {
  try {
    res.status(200).json({
      success: true,
      data: {
        resumen: {
          totalConductores: 12,
          conductoresActivos: 10,
          totalPedidos: 156,
          pedidosPendientes: 24,
          pedidosEnRuta: 18,
          pedidosEntregados: 114,
          tasaEntrega: 73.08
        },
        ultimosDias: {
          entregasHoy: 8,
          entregasAyer: 12,
          promedioEntregasPorDia: 10.5
        }
      }
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: 'Error obteniendo estadísticas'
    });
  }
});

module.exports = router;
