// Backend - Express Server con JWT Authentication
// Archivo: src/server.js

const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const morgan = require('morgan');
require('dotenv').config();

// Importar rutas
const authRoutes = require('./routes/auth');
const conductorRoutes = require('./routes/conductor');
const adminRoutes = require('./routes/admin');

// Importar middleware
const errorHandler = require('./middleware/errorHandler');

const app = express();
const PORT = process.env.PORT || 3000;

// ==================== MIDDLEWARE GLOBAL ====================

// Seguridad
app.use(helmet());

// CORS
app.use(cors({
  origin: [
    process.env.ADMIN_PANEL_URL || 'http://localhost:5173',
    'http://localhost:3000'
  ],
  credentials: true,
  optionsSuccessStatus: 200
}));

// Logging
app.use(morgan('combined'));

// Body parsers
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// ==================== HEALTH CHECK ====================

app.get('/health', (req, res) => {
  res.json({ status: 'ok', timestamp: new Date() });
});

// ==================== RUTAS ====================

// Autenticación (sin protección JWT)
app.use('/auth', authRoutes);

// Endpoints protegidos para conductor
app.use('/conductor', conductorRoutes);

// Endpoints protegidos para admin  
app.use('/admin', adminRoutes);

// ==================== MANEJO DE ERRORES ====================

// 404
app.use((req, res) => {
  res.status(404).json({ 
    success: false, 
    error: 'Endpoint no encontrado',
    path: req.path
  });
});

// Error handler global
app.use(errorHandler);

// ==================== INICIAR SERVIDOR ====================

app.listen(PORT, () => {
  console.log(`🚀 Servidor corriendo en puerto ${PORT}`);
  console.log(`📍 Health check: http://localhost:${PORT}/health`);
  console.log(`🔐 JWT_SECRET configurado: ${process.env.JWT_SECRET_KEY ? '✅' : '❌'}`);
});

module.exports = app;
