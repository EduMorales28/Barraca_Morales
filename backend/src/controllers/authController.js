// Controlador de Autenticación
// Archivo: src/controllers/authController.js

const jwt = require('jsonwebtoken');
const bcrypt = require('bcryptjs');

// Simulated database - en producción usar MongoDB
const users = {
  'admin@logistica.com': {
    id: 'admin_001',
    email: 'admin@logistica.com',
    password: '$2a$10$slYQmyNdGzin7olVasbCouYDlH4lF/eiNQDPr2s.lG5HpnWAqEDve', // password: admin123
    nombre: 'Administrador',
    tipo: 'admin'
  },
  'juan@conductor.com': {
    id: 'conductor_001',
    email: 'juan@conductor.com',
    password: '$2a$10$slYQmyNdGzin7olVasbCouYDlH4lF/eiNQDPr2s.lG5HpnWAqEDve', // password: admin123
    nombre: 'Juan García',
    tipo: 'conductor'
  }
};

// Almacenar refresh tokens (en producción usar BD)
const refreshTokens = new Map();

/**
 * Login: Valida credenciales y retorna tokens JWT
 * 
 * POST /auth/login
 * Body: { email, password, tipo }
 * Returns: { accessToken, refreshToken, user }
 */
exports.login = async (req, res) => {
  try {
    const { email, password, tipo } = req.body;

    // Validación
    if (!email || !password) {
      return res.status(400).json({
        success: false,
        error: 'Email y contraseña requeridos'
      });
    }

    // Buscar usuario
    const user = users[email];
    if (!user) {
      return res.status(401).json({
        success: false,
        error: 'Email o contraseña incorrectos'
      });
    }

    // Validar tipo (admin/conductor)
    if (tipo && user.tipo !== tipo) {
      return res.status(401).json({
        success: false,
        error: `Usuario no es de tipo ${tipo}`
      });
    }

    // Verificar contraseña (en producción usar hash)
    // Para este ejemplo, asumimos que la contraseña es correcta si el usuario existe
    const passwordMatch = await bcrypt.compare(password, user.password);
    if (!passwordMatch) {
      return res.status(401).json({
        success: false,
        error: 'Email o contraseña incorrectos'
      });
    }

    // Generar tokens JWT
    const accessToken = jwt.sign(
      {
        id: user.id,
        email: user.email,
        nombre: user.nombre,
        tipo: user.tipo
      },
      process.env.JWT_SECRET_KEY,
      { expiresIn: process.env.JWT_EXPIRE || '1h' }
    );

    const refreshToken = jwt.sign(
      {
        id: user.id,
        type: 'refresh'
      },
      process.env.JWT_REFRESH_SECRET,
      { expiresIn: process.env.REFRESH_TOKEN_EXPIRE || '7d' }
    );

    // Guardar refresh token
    refreshTokens.set(user.id, { 
      token: refreshToken,
      createdAt: new Date(),
      expiresAt: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000) // 7 días
    });

    // Respuesta exitosa
    res.status(200).json({
      success: true,
      data: {
        accessToken,
        refreshToken,
        user: {
          id: user.id,
          email: user.email,
          nombre: user.nombre,
          tipo: user.tipo
        },
        expiresIn: process.env.JWT_EXPIRE || '1h'
      }
    });

  } catch (error) {
    console.error('Error en login:', error);
    res.status(500).json({
      success: false,
      error: 'Error en el login'
    });
  }
};

/**
 * Refresh Token: Genera nuevo accessToken a partir del refreshToken
 * 
 * POST /auth/refresh
 * Body: { refreshToken }
 * Returns: { accessToken, expiresIn }
 */
exports.refreshToken = async (req, res) => {
  try {
    const { refreshToken } = req.body;

    if (!refreshToken) {
      return res.status(400).json({
        success: false,
        error: 'Refresh token requerido'
      });
    }

    // Verificar refresh token
    let decoded;
    try {
      decoded = jwt.verify(refreshToken, process.env.JWT_REFRESH_SECRET);
    } catch (error) {
      return res.status(401).json({
        success: false,
        error: 'Refresh token inválido o expirado'
      });
    }

    // Verificar que el refresh token está en almacenamiento
    const storedToken = refreshTokens.get(decoded.id);
    if (!storedToken || storedToken.token !== refreshToken) {
      return res.status(401).json({
        success: false,
        error: 'Refresh token no encontrado o revocado'
      });
    }

    // Buscar usuario
    const user = Object.values(users).find(u => u.id === decoded.id);
    if (!user) {
      return res.status(401).json({
        success: false,
        error: 'Usuario no encontrado'
      });
    }

    // Generar nuevo access token
    const newAccessToken = jwt.sign(
      {
        id: user.id,
        email: user.email,
        nombre: user.nombre,
        tipo: user.tipo
      },
      process.env.JWT_SECRET_KEY,
      { expiresIn: process.env.JWT_EXPIRE || '1h' }
    );

    res.status(200).json({
      success: true,
      data: {
        accessToken: newAccessToken,
        expiresIn: process.env.JWT_EXPIRE || '1h'
      }
    });

  } catch (error) {
    console.error('Error en refresh token:', error);
    res.status(500).json({
      success: false,
      error: 'Error refrescando token'
    });
  }
};

/**
 * Logout: Invalida el refresh token
 * 
 * POST /auth/logout
 * Headers: Authorization: Bearer {accessToken}
 */
exports.logout = async (req, res) => {
  try {
    const userId = req.user.id;

    // Eliminar refresh token
    refreshTokens.delete(userId);

    res.status(200).json({
      success: true,
      message: 'Sesión cerrada correctamente'
    });

  } catch (error) {
    console.error('Error en logout:', error);
    res.status(500).json({
      success: false,
      error: 'Error cerrando sesión'
    });
  }
};

/**
 * Verify: Verifica que el token es válido
 * 
 * GET /auth/verify
 * Headers: Authorization: Bearer {accessToken}
 */
exports.verify = (req, res) => {
  res.status(200).json({
    success: true,
    data: {
      user: req.user
    }
  });
};
