// ============================================================================
// API REST - Node.js/Express Implementation Example
// ============================================================================

/**
 * INSTALACIÓN:
 * npm install express cors dotenv firebase-admin jsonwebtoken bcrypt multer
 * npm install --save-dev nodemon
 */

// ============================================================================
// CONFIGURACIÓN
// ============================================================================

// .env
/*
PORT=3000
FIREBASE_PROJECT_ID=logistica-morales
FIREBASE_PRIVATE_KEY=...
FIREBASE_CLIENT_EMAIL=...
JWT_SECRET=your-secret-key-change-in-production
JWT_EXPIRES=24h
FILE_SIZE_LIMIT=5242880
*/

// ============================================================================
// ESTRUCTURAS BASE
// ============================================================================

// middleware/auth.js
const jwt = require('jsonwebtoken');

const auth = (req, res, next) => {
  try {
    const token = req.headers.authorization?.split(' ')[1];
    
    if (!token) {
      return res.status(401).json({
        success: false,
        message: 'Token requerido',
        error: { code: 'UNAUTHORIZED', details: 'Token no proporcionado' }
      });
    }

    const decoded = jwt.verify(token, process.env.JWT_SECRET);
    req.user = decoded;
    next();
  } catch (error) {
    res.status(401).json({
      success: false,
      message: 'Token inválido',
      error: { code: 'INVALID_TOKEN', details: error.message }
    });
  }
};

const adminOnly = (req, res, next) => {
  if (req.user.rol !== 'admin') {
    return res.status(403).json({
      success: false,
      message: 'Acceso denegado',
      error: { code: 'PERMISSION_DENIED', details: 'Solo administradores' }
    });
  }
  next();
};

const conductorOnly = (req, res, next) => {
  if (req.user.rol !== 'conductor') {
    return res.status(403).json({
      success: false,
      message: 'Acceso denegado',
      error: { code: 'PERMISSION_DENIED', details: 'Solo conductores' }
    });
  }
  next();
};

module.exports = { auth, adminOnly, conductorOnly };

// ============================================================================
// SERVICIOS (Services)
// ============================================================================

// services/authService.js
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
const admin = require('firebase-admin');

const db = admin.firestore();

class AuthService {
  async login(email, password) {
    // Buscar usuario en Firestore
    const userSnapshot = await db.collection('usuarios')
      .where('email', '==', email)
      .limit(1)
      .get();

    if (userSnapshot.empty) {
      throw {
        code: 'INVALID_CREDENTIALS',
        details: 'Email o contraseña incorrectos',
        status: 401
      };
    }

    const userData = userSnapshot.docs[0].data();
    const userId = userSnapshot.docs[0].id;

    // Validar contraseña
    const validPassword = await bcrypt.compare(password, userData.passwordHash);
    if (!validPassword) {
      throw {
        code: 'INVALID_CREDENTIALS',
        details: 'Email o contraseña incorrectos',
        status: 401
      };
    }

    // Generar token JWT
    const token = jwt.sign(
      {
        id: userId,
        email: userData.email,
        rol: userData.rol,
        nombre: userData.nombre
      },
      process.env.JWT_SECRET,
      { expiresIn: process.env.JWT_EXPIRES || '24h' }
    );

    return {
      token,
      expiresIn: 86400,
      usuario: {
        id: userId,
        nombre: userData.nombre,
        email: userData.email,
        rol: userData.rol,
        estado: userData.estado,
        fotoUrl: userData.fotoUrl,
        telefono: userData.telefono
      }
    };
  }
}

module.exports = new AuthService();

// services/pedidoService.js
class PedidoService {
  async crearPedido(data) {
    // Validar cliente existe
    const clienteDoc = await db.collection('clientes').doc(data.clienteId).get();
    if (!clienteDoc.exists) {
      throw {
        code: 'VALIDATION_ERROR',
        details: { clienteId: 'Cliente no encontrado' },
        status: 400
      };
    }

    // Validar número único
    const existePedido = await db.collection('pedidos')
      .where('numero', '==', data.numero)
      .limit(1)
      .get();

    if (!existePedido.empty) {
      throw {
        code: 'CONFLICT',
        details: 'Número de pedido ya existe',
        status: 409
      };
    }

    // Calcular monto total
    const montoTotal = data.items.reduce((sum, item) => 
      sum + (item.cantidad * item.precioUnitario), 0
    );

    // Crear documento
    const pedidoRef = db.collection('pedidos').doc();
    const pedidoData = {
      numero: data.numero,
      clienteId: data.clienteId,
      conductorId: null,
      direccion: data.direccion,
      referencia: data.referencia,
      latitud: data.latitud,
      longitud: data.longitud,
      montoTotal: montoTotal,
      montoLevantado: 0,
      porcentajeLevantado: 0,
      estado: 'pendiente',
      levantadoTotal: false,
      observaciones: data.observaciones,
      fechaCreacion: new Date(),
      fechaAsignacion: null,
      fechaEntregaEstimada: data.fechaEntregaEstimada,
      fechaEntregaReal: null,
      createdAt: new Date(),
      updatedAt: new Date()
    };

    await pedidoRef.set(pedidoData);

    // Crear items como subcollection
    const itemsPromises = data.items.map(item => {
      const itemRef = pedidoRef.collection('items').doc();
      return itemRef.set({
        descripcion: item.descripcion,
        cantidad: item.cantidad,
        precioUnitario: item.precioUnitario,
        subtotal: item.cantidad * item.precioUnitario,
        cantidadLevantada: 0,
        estado: 'pendiente',
        createdAt: new Date(),
        updatedAt: new Date()
      });
    });

    await Promise.all(itemsPromises);

    // Crear entrada en historial
    await db.collection('historial_pedidos').add({
      pedidoId: pedidoRef.id,
      estadoAnterior: null,
      estadoNuevo: 'pendiente',
      usuarioId: null,
      comentario: 'Pedido creado',
      createdAt: new Date()
    });

    return {
      id: pedidoRef.id,
      ...pedidoData
    };
  }

  async obtenerPedidos(filtros = {}, page = 1, limit = 20) {
    let query = db.collection('pedidos');

    if (filtros.estado) {
      query = query.where('estado', '==', filtros.estado);
    }

    if (filtros.conductor) {
      query = query.where('conductorId', '==', filtros.conductor);
    }

    if (filtros.cliente) {
      query = query.where('clienteId', '==', filtros.cliente);
    }

    const snapshot = await query
      .orderBy('fechaCreacion', 'desc')
      .limit(limit * 2)
      .get();

    const pedidos = [];
    for (const doc of snapshot.docs) {
      const items = await this.obtenerItems(doc.id);
      pedidos.push({
        id: doc.id,
        ...doc.data(),
        items
      });
    }

    const start = (page - 1) * limit;
    const end = start + limit;

    return {
      pedidos: pedidos.slice(start, end),
      pagination: {
        total: pedidos.length,
        page,
        limit,
        pages: Math.ceil(pedidos.length / limit)
      }
    };
  }

  async obtenerPedidoById(pedidoId) {
    const doc = await db.collection('pedidos').doc(pedidoId).get();

    if (!doc.exists) {
      throw {
        code: 'RESOURCE_NOT_FOUND',
        details: `Pedido ${pedidoId} no existe`,
        status: 404
      };
    }

    const items = await this.obtenerItems(pedidoId);
    return {
      id: doc.id,
      ...doc.data(),
      items
    };
  }

  async obtenerItems(pedidoId) {
    const snapshot = await db.collection('pedidos')
      .doc(pedidoId)
      .collection('items')
      .get();

    return snapshot.docs.map(doc => ({
      id: doc.id,
      ...doc.data()
    }));
  }

  async asignarConductor(pedidoId, conductorId) {
    // Validar conductor existe y está activo
    const conductorDoc = await db.collection('usuarios').doc(conductorId).get();
    if (!conductorDoc.exists || conductorDoc.data().rol !== 'conductor' || 
        conductorDoc.data().estado !== 'activo') {
      throw {
        code: 'INVALID_CONDUCTOR',
        details: 'Conductor no válido o inactivo',
        status: 400
      };
    }

    // Actualizar pedido
    await db.collection('pedidos').doc(pedidoId).update({
      conductorId: conductorId,
      estado: 'asignado',
      fechaAsignacion: new Date(),
      updatedAt: new Date()
    });

    // Registrar en historial
    await db.collection('historial_pedidos').add({
      pedidoId: pedidoId,
      estadoAnterior: 'pendiente',
      estadoNuevo: 'asignado',
      usuarioId: null,
      comentario: 'Asignado a conductor',
      createdAt: new Date()
    });

    return { success: true };
  }
}

module.exports = new PedidoService();

// ============================================================================
// RUTAS
// ============================================================================

// routes/auth.js
const express = require('express');
const router = express.Router();
const authService = require('../services/authService');

router.post('/login', async (req, res) => {
  try {
    const { email, password } = req.body;

    // Validación
    if (!email || !password) {
      return res.status(400).json({
        success: false,
        message: 'Validación fallida',
        error: {
          code: 'VALIDATION_ERROR',
          details: {
            ...(! email && { email: 'Email requerido' }),
            ...(!password && { password: 'Contraseña requerida' })
          }
        }
      });
    }

    const result = await authService.login(email, password);

    res.json({
      success: true,
      message: 'Login exitoso',
      data: result
    });
  } catch (error) {
    const status = error.status || 500;
    res.status(status).json({
      success: false,
      message: 'Error en login',
      error: {
        code: error.code || 'INTERNAL_ERROR',
        details: error.details || error.message
      }
    });
  }
});

module.exports = router;

// routes/pedidos.js
const express = require('express');
const router = express.Router();
const { auth, adminOnly } = require('../middleware/auth');
const pedidoService = require('../services/pedidoService');

router.post('/', auth, adminOnly, async (req, res) => {
  try {
    const result = await pedidoService.crearPedido(req.body);
    res.status(201).json({
      success: true,
      message: 'Pedido creado exitosamente',
      data: result
    });
  } catch (error) {
    const status = error.status || 500;
    res.status(status).json({
      success: false,
      message: 'Error al crear pedido',
      error: {
        code: error.code || 'INTERNAL_ERROR',
        details: error.details
      }
    });
  }
});

router.get('/', auth, async (req, res) => {
  try {
    const filtros = {};
    if (req.query.estado) filtros.estado = req.query.estado;
    if (req.query.conductor) filtros.conductor = req.query.conductor;

    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 20;

    const result = await pedidoService.obtenerPedidos(filtros, page, limit);
    res.json({
      success: true,
      message: 'Pedidos obtenidos',
      data: result
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: 'Error al obtener pedidos',
      error: { code: 'INTERNAL_ERROR', details: error.message }
    });
  }
});

router.get('/:pedidoId', auth, async (req, res) => {
  try {
    const result = await pedidoService.obtenerPedidoById(req.params.pedidoId);
    res.json({
      success: true,
      message: 'Pedido encontrado',
      data: result
    });
  } catch (error) {
    const status = error.status || 500;
    res.status(status).json({
      success: false,
      message: 'Error al obtener pedido',
      error: { code: error.code || 'INTERNAL_ERROR', details: error.details }
    });
  }
});

router.post('/:pedidoId/asignar', auth, adminOnly, async (req, res) => {
  try {
    const { conductorId } = req.body;
    await pedidoService.asignarConductor(req.params.pedidoId, conductorId);

    res.json({
      success: true,
      message: 'Conductor asignado'
    });
  } catch (error) {
    const status = error.status || 500;
    res.status(status).json({
      success: false,
      message: 'Error al asignar conductor',
      error: { code: error.code || 'INTERNAL_ERROR', details: error.details }
    });
  }
});

module.exports = router;

// ============================================================================
// SERVIDOR PRINCIPAL
// ============================================================================

// server.js
const express = require('express');
const cors = require('cors');
const dotenv = require('dotenv');
const admin = require('firebase-admin');

dotenv.config();

const app = express();

// Inicializar Firebase
admin.initializeApp({
  projectId: process.env.FIREBASE_PROJECT_ID,
  privateKey: process.env.FIREBASE_PRIVATE_KEY,
  clientEmail: process.env.FIREBASE_CLIENT_EMAIL
});

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ limit: '50MB', extended: true }));

// Rutas
app.use('/v1/auth', require('./routes/auth'));
app.use('/v1/pedidos', require('./routes/pedidos'));

// Error handling
app.use((err, req, res, next) => {
  console.error(err);
  res.status(500).json({
    success: false,
    message: 'Error interno del servidor',
    error: {
      code: 'INTERNAL_SERVER_ERROR',
      details: process.env.NODE_ENV === 'development' ? err.message : null
    }
  });
});

// Health check
app.get('/health', (req, res) => {
  res.json({ status: 'ok' });
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`🚀 API ejecutándose en http://localhost:${PORT}/v1`);
});
