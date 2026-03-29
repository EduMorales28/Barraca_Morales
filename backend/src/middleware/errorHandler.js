// Middleware para manejo de errores
// Archivo: src/middleware/errorHandler.js

/**
 * Middleware global para manejar errores
 * Se ejecuta al final de la cadena de middlewares
 */
const errorHandler = (err, req, res, next) => {
  console.error('🔴 Error:', err.message);
  console.error('Stack:', err.stack);

  const status = err.status || 500;
  const message = err.message || 'Error interno del servidor';

  // Información adicional en desarrollo
  const details = process.env.NODE_ENV === 'development' 
    ? { message, stack: err.stack }
    : { message };

  res.status(status).json({
    success: false,
    error: message,
    status: status,
    ...(process.env.NODE_ENV === 'development' && { details })
  });
};

module.exports = errorHandler;
