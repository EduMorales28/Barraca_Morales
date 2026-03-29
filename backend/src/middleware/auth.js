// Middleware de Autenticación JWT
// Archivo: src/middleware/auth.js

const jwt = require('jsonwebtoken');

/**
 * Middleware para verificar JWT Token
 * Valida que el token sea válido y no haya expirado
 * Agrega info del usuario al request
 */
const verifyToken = (req, res, next) => {
  try {
    // Obtener token del header Authorization
    const authHeader = req.headers.authorization;
    
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      return res.status(401).json({
        success: false,
        error: 'Token no proporcionado. Use: Bearer {token}'
      });
    }

    // Extraer token
    const token = authHeader.substring(7); // Quitar "Bearer "

    // Verificar y decodificar
    const decoded = jwt.verify(token, process.env.JWT_SECRET_KEY);
    
    // Agregar datos del usuario al request para usar en controladores
    req.user = decoded;
    
    next();
  } catch (error) {
    if (error.name === 'TokenExpiredError') {
      return res.status(401).json({
        success: false,
        error: 'Token expirado. Debe refrescar el token.',
        code: 'TOKEN_EXPIRED'
      });
    }
    
    if (error.name === 'JsonWebTokenError') {
      return res.status(401).json({
        success: false,
        error: 'Token inválido'
      });
    }

    res.status(500).json({
      success: false,
      error: 'Error verificando token'
    });
  }
};

/**
 * Middleware para verificar que el usuario es administrador
 * Debe usarse después de verifyToken
 */
const requireAdmin = (req, res, next) => {
  if (!req.user) {
    return res.status(401).json({
      success: false,
      error: 'No autenticado'
    });
  }

  if (req.user.tipo !== 'admin') {
    return res.status(403).json({
      success: false,
      error: 'Acceso denegado. Se requieren permisos de administrador.'
    });
  }

  next();
};

/**
 * Middleware para verificar que el usuario es conductor
 */
const requireConductor = (req, res, next) => {
  if (!req.user) {
    return res.status(401).json({
      success: false,
      error: 'No autenticado'
    });
  }

  if (req.user.tipo !== 'conductor') {
    return res.status(403).json({
      success: false,
      error: 'Acceso denegado. Se requieren permisos de conductor.'
    });
  }

  next();
};

/**
 * Middleware para manejar errores
 */
const errorHandler = (err, req, res, next) => {
  console.error('Error:', err);

  const status = err.status || 500;
  const message = err.message || 'Error interno del servidor';

  res.status(status).json({
    success: false,
    error: message,
    ...(process.env.NODE_ENV === 'development' && { details: err })
  });
};

module.exports = {
  verifyToken,
  requireAdmin,
  requireConductor,
  errorHandler
};
