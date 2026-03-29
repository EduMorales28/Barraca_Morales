// Rutas para Conductores (Protegidas con JWT)
// Archivo: src/routes/conductor.js

const express = require('express');
const router = express.Router();
const { verifyToken, requireConductor } = require('../middleware/auth');

/**
 * GET /conductor/perfil
 * Obtener perfil del conductor logueado
 * Protegido: Requiere token JWT
 */
router.get('/perfil', verifyToken, (req, res) => {
  try {
    const user = req.user;
    
    res.status(200).json({
      success: true,
      data: {
        id: user.id,
        email: user.email,
        nombre: user.nombre,
        tipo: user.tipo,
        estado: 'activo',
        vehiculo: {
          placa: 'ABC-1234',
          tipo: 'moto'
        },
        ubicacion: {
          latitud: 40.7128,
          longitud: -74.0060,
          actualizado: new Date()
        }
      }
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: 'Error obteniendo perfil'
    });
  }
});

/**
 * GET /conductor/mis-pedidos
 * Obtener lista de pedidos asignados al conductor
 * Protegido: Requiere token JWT
 */
router.get('/mis-pedidos', verifyToken, (req, res) => {
  try {
    const { estado, page = 1, limit = 20 } = req.query;
    const conductorId = req.user.id;

    // Mock data - en producción consultar BD
    const pedidos = [
      {
        id: '001',
        numero: '12345',
        cliente: 'Juan García',
        direccion: 'Calle Principal 123',
        estado: 'pendiente',
        montoTotal: 150.00,
        items: [
          { nombre: 'Paquete A', cantidad: 1, precio: 100 }
        ],
        fechaAsignacion: new Date(),
        ubicacion: {
          latitud: 40.7128,
          longitud: -74.0060
        }
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
 * GET /conductor/pedidos/:pedidoId
 * Obtener detalle de un pedido
 * Protegido: Requiere token JWT
 */
router.get('/pedidos/:pedidoId', verifyToken, (req, res) => {
  try {
    const { pedidoId } = req.params;

    // Mock data
    const pedido = {
      id: pedidoId,
      numero: '12345',
      cliente: 'Juan García',
      direccion: 'Calle Principal 123',
      telefono: '+34 666 777 888',
      estado: 'pendiente',
      montoTotal: 150.00,
      pagado: false,
      items: [
        { nombre: 'Paquete A', cantidad: 1, precio: 100 }
      ],
      ubicacion: {
        latitud: 40.7128,
        longitud: -74.0060
      },
      detalles: 'Frágil - cuidar durante transporte',
      fechaAsignacion: new Date()
    };

    res.status(200).json({
      success: true,
      data: pedido
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: 'Error obteniendo pedido'
    });
  }
});

/**
 * POST /conductor/ubicacion
 * Actualizar ubicación GPS del conductor
 * Protegido: Requiere token JWT
 */
router.post('/ubicacion', verifyToken, (req, res) => {
  try {
    const { latitud, longitud } = req.body;
    const conductorId = req.user.id;

    if (!latitud || !longitud) {
      return res.status(400).json({
        success: false,
        error: 'Latitud y longitud requeridas'
      });
    }

    // En producción guardar en BD
    console.log(`📍 Ubicación actualizada para ${conductorId}: ${latitud}, ${longitud}`);

    res.status(200).json({
      success: true,
      data: {
        message: 'Ubicación actualizada',
        timestamp: new Date()
      }
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: 'Error actualizando ubicación'
    });
  }
});

/**
 * PUT /conductor/pedidos/:pedidoId
 * Actualizar estado de un pedido
 * Protegido: Requiere token JWT
 */
router.put('/pedidos/:pedidoId', verifyToken, (req, res) => {
  try {
    const { pedidoId } = req.params;
    const { estado, observaciones } = req.body;

    if (!estado) {
      return res.status(400).json({
        success: false,
        error: 'Estado requerido'
      });
    }

    console.log(`📦 Pedido ${pedidoId} actualizado a: ${estado}`);

    res.status(200).json({
      success: true,
      data: {
        id: pedidoId,
        estado: estado,
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
 * POST /conductor/entregas
 * Registrar entrega completada
 * Protegido: Requiere token JWT
 */
router.post('/entregas', verifyToken, (req, res) => {
  try {
    const { pedidoId, recibidoPor, dniRecibidor, observaciones } = req.body;
    const conductorId = req.user.id;

    if (!pedidoId || !recibidoPor) {
      return res.status(400).json({
        success: false,
        error: 'Datos incompletos'
      });
    }

    console.log(`✅ Entrega registrada: ${pedidoId} recibido por ${recibidoPor}`);

    res.status(200).json({
      success: true,
      data: {
        id: 'entrega_' + Date.now(),
        pedidoId: pedidoId,
        estado: 'entregado',
        fechaEntrega: new Date()
      }
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: 'Error registrando entrega'
    });
  }
});

module.exports = router;
