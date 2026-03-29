// Rutas de Autenticación
// Archivo: src/routes/auth.js

const express = require('express');
const router = express.Router();
const authController = require('../controllers/authController');
const { verifyToken } = require('../middleware/auth');

/**
 * POST /auth/login
 * Login para conductor o admin
 */
router.post('/login', authController.login);

/**
 * POST /auth/refresh
 * Refrescar access token usando refresh token
 */
router.post('/refresh', authController.refreshToken);

/**
 * GET /auth/verify
 * Verificar que el token es válido
 */
router.get('/verify', verifyToken, authController.verify);

/**
 * POST /auth/logout
 * Cerrar sesión (requiere token válido)
 */
router.post('/logout', verifyToken, authController.logout);

module.exports = router;
