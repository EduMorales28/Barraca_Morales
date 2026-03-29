import express from 'express';
import cors from 'cors';
import multer from 'multer';
import path from 'path';
import { fileURLToPath } from 'url';
import bcryptjs from 'bcryptjs';
import jwt from 'jsonwebtoken';
import sqlite3 from 'sqlite3';
import fs from 'fs';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const app = express();
const PORT = 3000;
const JWT_SECRET = process.env.JWT_SECRET || 'barraca-morales-secret-dev';

// MIDDLEWARE
app.use(cors());
app.use(express.json());
app.use(express.static('uploads'));

// MULTER - Subida de archivos
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    const dir = path.join(__dirname, 'uploads');
    if (!fs.existsSync(dir)) fs.mkdirSync(dir);
    cb(null, dir);
  },
  filename: (req, file, cb) => {
    cb(null, Date.now() + '-' + file.originalname);
  }
});
const upload = multer({ storage });

function createToken(user) {
  return jwt.sign(
    {
      id: user.id,
      nombre: user.nombre,
      email: user.email,
      rol: user.rol
    },
    JWT_SECRET,
    { expiresIn: '12h' }
  );
}

function authenticateToken(req, res, next) {
  const authorization = req.headers.authorization || '';
  const token = authorization.startsWith('Bearer ') ? authorization.slice(7) : null;

  if (!token) {
    return res.status(401).json({ error: 'Token requerido' });
  }

  try {
    req.user = jwt.verify(token, JWT_SECRET);
    next();
  } catch {
    return res.status(401).json({ error: 'Token inválido o vencido' });
  }
}

function requireRole(...roles) {
  return (req, res, next) => {
    if (!req.user || !roles.includes(req.user.rol)) {
      return res.status(403).json({ error: 'No tienes permisos para realizar esta acción' });
    }

    next();
  };
}

// DATABASE
const db = new sqlite3.Database(path.join(__dirname, 'barraca.db'), (err) => {
  if (err) console.error('DB Error:', err);
  else console.log('✅ SQLite conectada');
});

db.run('PRAGMA foreign_keys = ON');

// CREAR TABLAS
function initDatabase() {
  db.serialize(() => {
    // Usuarios
    db.run(`
      CREATE TABLE IF NOT EXISTS usuarios (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        nombre TEXT,
        email TEXT UNIQUE,
        password TEXT,
        rol TEXT,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP
      )
    `);

    // Clientes
    db.run(`
      CREATE TABLE IF NOT EXISTS clientes (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        nombre TEXT UNIQUE,
        direccion TEXT,
        telefono TEXT,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP
      )
    `);

    db.run(`
      CREATE TABLE IF NOT EXISTS proveedores (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        nombre TEXT UNIQUE,
        rut TEXT,
        direccion TEXT,
        telefono TEXT,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP
      )
    `);

    db.run(`ALTER TABLE proveedores ADD COLUMN rut TEXT`, () => {});
    db.run(`ALTER TABLE proveedores ADD COLUMN direccion TEXT`, () => {});

    db.run(`
      CREATE TABLE IF NOT EXISTS articulos (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        codigo TEXT UNIQUE,
        nombre TEXT,
        proveedor_id INTEGER,
        precio REAL DEFAULT 0,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY(proveedor_id) REFERENCES proveedores(id)
      )
    `);

    db.run(`ALTER TABLE articulos ADD COLUMN proveedor_id INTEGER`, () => {});

    // Pedidos
    db.run(`
      CREATE TABLE IF NOT EXISTS pedidos (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        cliente TEXT,
        direccion TEXT,
        lat REAL,
        lng REAL,
        estado TEXT DEFAULT 'pendiente',
        levantado TEXT DEFAULT 'total',
        levantado_en_mostrador TEXT,
        sin_levantado_mostrador INTEGER DEFAULT 0,
        conductor_id INTEGER,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY(conductor_id) REFERENCES usuarios(id)
      )
    `);

    db.run(`ALTER TABLE pedidos ADD COLUMN levantado_en_mostrador TEXT`, () => {});
    db.run(`ALTER TABLE pedidos ADD COLUMN sin_levantado_mostrador INTEGER DEFAULT 0`, () => {});
    db.run(`ALTER TABLE pedidos ADD COLUMN created_by INTEGER`, () => {});
    db.run(`ALTER TABLE pedidos ADD COLUMN accepted_at DATETIME`, () => {});

    // Items de pedidos
    db.run(`
      CREATE TABLE IF NOT EXISTS items_pedido (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        pedido_id INTEGER,
        codigo TEXT,
        nombre TEXT,
        cantidad INTEGER,
        precio REAL DEFAULT 0,
        FOREIGN KEY(pedido_id) REFERENCES pedidos(id)
      )
    `);

    db.run(`ALTER TABLE items_pedido ADD COLUMN codigo TEXT`, () => {});
    db.run(`ALTER TABLE items_pedido ADD COLUMN precio REAL DEFAULT 0`, () => {});

    db.run(`
      CREATE TABLE IF NOT EXISTS notificaciones (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        usuario_id INTEGER NOT NULL,
        pedido_id INTEGER,
        tipo TEXT,
        mensaje TEXT,
        leida INTEGER DEFAULT 0,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY(usuario_id) REFERENCES usuarios(id),
        FOREIGN KEY(pedido_id) REFERENCES pedidos(id)
      )
    `);

    // Entregas
    db.run(`
      CREATE TABLE IF NOT EXISTS entregas (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        pedido_id INTEGER,
        foto TEXT,
        observaciones TEXT,
        fecha DATETIME DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY(pedido_id) REFERENCES pedidos(id)
      )
    `);

    // DATOS PRECARGADOS
    const adminPassword = bcryptjs.hashSync('1234', 10);
    const conductorPassword = bcryptjs.hashSync('1234', 10);

    // Admin
    db.run(`
      INSERT OR IGNORE INTO usuarios (nombre, email, password, rol)
      VALUES ('Admin', 'admin@test.com', ?, 'admin')
    `, [adminPassword]);

    // Conductores
    db.run(`
      INSERT OR IGNORE INTO usuarios (nombre, email, password, rol)
      VALUES ('Juan García', 'conductor1@test.com', ?, 'conductor')
    `, [conductorPassword]);

    db.run(`
      INSERT OR IGNORE INTO usuarios (nombre, email, password, rol)
      VALUES ('Carlos López', 'conductor2@test.com', ?, 'conductor')
    `, [conductorPassword]);

    db.run(`
      INSERT OR IGNORE INTO clientes (nombre, direccion, telefono)
      VALUES ('Empresa A', 'Av. Principal 123, CABA', '2900 111 111')
    `);

    db.run(`
      INSERT OR IGNORE INTO clientes (nombre, direccion, telefono)
      VALUES ('Empresa B', 'Calle 9 de Julio 456, CABA', '2900 222 222')
    `);

    db.run(`
      INSERT OR IGNORE INTO clientes (nombre, direccion, telefono)
      VALUES ('Local C', 'San Isidro 789, CABA', '2900 333 333')
    `);

    db.run(`
      INSERT OR IGNORE INTO proveedores (nombre, rut, direccion, telefono)
      VALUES ('Hormigones del Sur', '214567890012', 'Ruta 1 km 24, Montevideo', '2901 111 111')
    `);

    db.run(`
      INSERT OR IGNORE INTO proveedores (nombre, rut, direccion, telefono)
      VALUES ('Acero Norte', '217654320019', 'Camino Norte 456, Canelones', '2901 222 222')
    `);

    db.run(`
      INSERT OR IGNORE INTO proveedores (nombre, rut, direccion, telefono)
      VALUES ('Pinturas Centro', '210001110017', 'Av. Italia 999, Montevideo', '2901 333 333')
    `);

    db.run(`
      INSERT OR IGNORE INTO clientes (nombre, direccion)
      SELECT DISTINCT cliente, direccion
      FROM pedidos
      WHERE cliente IS NOT NULL AND TRIM(cliente) <> ''
    `, () => {});

    db.run(`
      INSERT OR IGNORE INTO articulos (codigo, nombre, proveedor_id, precio)
      VALUES ('ART-001', 'Caja A', 1, 1200)
    `);
    db.run(`
      INSERT OR IGNORE INTO articulos (codigo, nombre, proveedor_id, precio)
      VALUES ('ART-002', 'Caja B', 1, 950)
    `);
    db.run(`
      INSERT OR IGNORE INTO articulos (codigo, nombre, proveedor_id, precio)
      VALUES ('ART-003', 'Paquete X', 2, 3500)
    `);
    db.run(`
      INSERT OR IGNORE INTO articulos (codigo, nombre, proveedor_id, precio)
      VALUES ('ART-004', 'Producto Y', 3, 780)
    `);
    db.run(`
      INSERT OR IGNORE INTO articulos (codigo, nombre, proveedor_id, precio)
      VALUES ('ART-005', 'Producto Z', 3, 1420)
    `);

    // Pedidos con items
    setTimeout(() => {
      db.get('SELECT COUNT(*) as count FROM pedidos', (err, row) => {
        if (row.count === 0) {
          // Pedido 1
          db.run(`
            INSERT INTO pedidos (cliente, direccion, lat, lng, estado, conductor_id)
            VALUES ('Empresa A', 'Av. Principal 123, CABA', -34.603728, -58.381592, 'asignado', 2)
          `, function() {
            db.run(`
                INSERT INTO items_pedido (pedido_id, codigo, nombre, cantidad, precio)
                VALUES (?, 'ART-001', 'Caja A', 10, 1200)
              `, [this.lastID]);
            db.run(`
                INSERT INTO items_pedido (pedido_id, codigo, nombre, cantidad, precio)
                VALUES (?, 'ART-002', 'Caja B', 5, 950)
              `, [this.lastID]);
          });

          // Pedido 2
          db.run(`
            INSERT INTO pedidos (cliente, direccion, lat, lng, estado, conductor_id)
            VALUES ('Empresa B', 'Calle 9 de Julio 456, CABA', -34.609034, -58.371099, 'asignado', 3)
          `, function() {
            db.run(`
              INSERT INTO items_pedido (pedido_id, codigo, nombre, cantidad, precio)
              VALUES (?, 'ART-003', 'Paquete X', 3, 3500)
            `, [this.lastID]);
          });

          // Pedido 3
          db.run(`
            INSERT INTO pedidos (cliente, direccion, lat, lng, estado, conductor_id, levantado)
            VALUES ('Local C', 'San Isidro 789, CABA', -34.477149, -58.537872, 'pendiente', 2, 'parcial')
          `, function() {
            db.run(`
              INSERT INTO items_pedido (pedido_id, codigo, nombre, cantidad, precio)
              VALUES (?, 'ART-004', 'Producto Y', 15, 780)
            `, [this.lastID]);
            db.run(`
              INSERT INTO items_pedido (pedido_id, codigo, nombre, cantidad, precio)
              VALUES (?, 'ART-005', 'Producto Z', 8, 1420)
            `, [this.lastID]);
          });

          console.log('✅ Datos precargados');
        }
      });
    }, 500);
  });
}

initDatabase();

// LOGS MIDDLEWARE
app.use((req, res, next) => {
  console.log(`[${new Date().toLocaleTimeString()}] ${req.method} ${req.path}`);
  next();
});

// ==================== RUTAS ====================

app.get('/', (req, res) => {
  res.json({
    nombre: 'Barraca Morales API',
    estado: 'OK',
    health: '/health'
  });
});

// LOGIN
app.post('/login', (req, res) => {
  const { email, password } = req.body;

  db.get('SELECT * FROM usuarios WHERE email = ?', [String(email || '').trim().toLowerCase()], (err, user) => {
    if (err) return res.status(500).json({ error: 'DB Error' });
    if (!user) return res.status(401).json({ error: 'Usuario no encontrado' });

    const passwordValid = bcryptjs.compareSync(password, user.password);
    if (!passwordValid) return res.status(401).json({ error: 'Contraseña incorrecta' });

    res.json({
      id: user.id,
      nombre: user.nombre,
      email: user.email,
      rol: user.rol,
      token: createToken(user)
    });
  });
});

// USUARIOS
app.get('/usuarios', authenticateToken, requireRole('admin'), (req, res) => {
  db.all('SELECT id, nombre, email, rol, created_at FROM usuarios ORDER BY nombre COLLATE NOCASE ASC', (err, users) => {
    if (err) return res.status(500).json({ error: 'DB Error' });
    res.json(users);
  });
});

app.post('/usuarios', authenticateToken, requireRole('admin'), (req, res) => {
  const { nombre, email, password, rol } = req.body;
  const allowedRoles = ['admin', 'conductor', 'creador_pedidos'];

  if (!nombre || !String(nombre).trim()) {
    return res.status(400).json({ error: 'El nombre es obligatorio' });
  }

  if (!email || !String(email).trim()) {
    return res.status(400).json({ error: 'El email es obligatorio' });
  }

  if (!password || String(password).length < 4) {
    return res.status(400).json({ error: 'La contraseña debe tener al menos 4 caracteres' });
  }

  if (!allowedRoles.includes(rol)) {
    return res.status(400).json({ error: 'Rol inválido' });
  }

  const nombreLimpio = String(nombre).trim();
  const emailLimpio = String(email).trim().toLowerCase();
  const passwordHash = bcryptjs.hashSync(String(password), 10);

  db.run(
    'INSERT INTO usuarios (nombre, email, password, rol) VALUES (?, ?, ?, ?)',
    [nombreLimpio, emailLimpio, passwordHash, rol],
    function(err) {
      if (err) {
        if (err.message?.includes('UNIQUE constraint failed: usuarios.email')) {
          return res.status(409).json({ error: 'Ya existe un usuario con ese email' });
        }
        return res.status(500).json({ error: 'DB Error' });
      }

      db.get(
        'SELECT id, nombre, email, rol, created_at FROM usuarios WHERE id = ?',
        [this.lastID],
        (selectErr, user) => {
          if (selectErr) return res.status(500).json({ error: 'DB Error' });
          res.status(201).json(user);
        }
      );
    }
  );
});

// CLIENTES
app.get('/clientes', authenticateToken, (req, res) => {
  db.all(
    'SELECT id, nombre, direccion, telefono FROM clientes ORDER BY nombre COLLATE NOCASE ASC',
    (err, clientes) => {
      if (err) return res.status(500).json({ error: 'DB Error' });
      res.json(clientes);
    }
  );
});

app.post('/clientes', authenticateToken, requireRole('admin', 'creador_pedidos'), (req, res) => {
  const { nombre, direccion, telefono } = req.body;

  if (!nombre || !String(nombre).trim()) {
    return res.status(400).json({ error: 'El nombre del cliente es obligatorio' });
  }

  db.run(
    'INSERT INTO clientes (nombre, direccion, telefono) VALUES (?, ?, ?)',
    [String(nombre).trim(), direccion || null, telefono || null],
    function(err) {
      if (err) {
        if (err.message.includes('UNIQUE')) {
          return res.status(409).json({ error: 'Ya existe un cliente con ese nombre' });
        }
        return res.status(500).json({ error: 'DB Error' });
      }

      db.get('SELECT id, nombre, direccion, telefono FROM clientes WHERE id = ?', [this.lastID], (getErr, cliente) => {
        if (getErr) return res.status(500).json({ error: 'DB Error' });
        res.status(201).json(cliente);
      });
    }
  );
});

app.get('/proveedores', authenticateToken, (req, res) => {
  db.all(
    'SELECT id, nombre, rut, direccion, telefono FROM proveedores ORDER BY nombre COLLATE NOCASE ASC',
    (err, proveedores) => {
      if (err) return res.status(500).json({ error: 'DB Error' });
      res.json(proveedores);
    }
  );
});

app.post('/proveedores', authenticateToken, requireRole('admin'), (req, res) => {
  const { nombre, rut, direccion, telefono } = req.body;

  if (!nombre || !String(nombre).trim()) {
    return res.status(400).json({ error: 'El nombre del proveedor es obligatorio' });
  }

  db.run(
    'INSERT INTO proveedores (nombre, rut, direccion, telefono) VALUES (?, ?, ?, ?)',
    [String(nombre).trim(), rut || null, direccion || null, telefono || null],
    function(err) {
      if (err) {
        if (err.message.includes('UNIQUE')) {
          return res.status(409).json({ error: 'Ya existe un proveedor con ese nombre' });
        }
        return res.status(500).json({ error: 'DB Error' });
      }

      db.get('SELECT id, nombre, rut, direccion, telefono FROM proveedores WHERE id = ?', [this.lastID], (getErr, proveedor) => {
        if (getErr) return res.status(500).json({ error: 'DB Error' });
        res.status(201).json(proveedor);
      });
    }
  );
});

app.get('/articulos', authenticateToken, (req, res) => {
  const { proveedor_id } = req.query;
  const whereClause = proveedor_id ? 'WHERE a.proveedor_id = ?' : '';
  const params = proveedor_id ? [proveedor_id] : [];

  db.all(
    `SELECT a.id, a.codigo, a.nombre, a.proveedor_id, a.precio, p.nombre as proveedor_nombre
     FROM articulos a
     LEFT JOIN proveedores p ON a.proveedor_id = p.id
     ${whereClause}
     ORDER BY a.nombre COLLATE NOCASE ASC`,
    params,
    (err, articulos) => {
      if (err) return res.status(500).json({ error: 'DB Error' });
      res.json(articulos);
    }
  );
});

app.post('/articulos', authenticateToken, requireRole('admin'), (req, res) => {
  const { codigo, nombre, proveedor_id, precio } = req.body;

  if (!nombre || !String(nombre).trim()) {
    return res.status(400).json({ error: 'El nombre del item es obligatorio' });
  }

  db.run(
    'INSERT INTO articulos (codigo, nombre, proveedor_id, precio) VALUES (?, ?, ?, ?)',
    [codigo || null, String(nombre).trim(), proveedor_id || null, precio ?? 0],
    function(err) {
      if (err) {
        if (err.message.includes('UNIQUE')) {
          return res.status(409).json({ error: 'Ya existe un item con ese código' });
        }
        return res.status(500).json({ error: 'DB Error' });
      }

      db.get(
        `SELECT a.id, a.codigo, a.nombre, a.proveedor_id, a.precio, p.nombre as proveedor_nombre
         FROM articulos a
         LEFT JOIN proveedores p ON a.proveedor_id = p.id
         WHERE a.id = ?`,
        [this.lastID],
        (getErr, articulo) => {
          if (getErr) return res.status(500).json({ error: 'DB Error' });
          res.status(201).json(articulo);
        }
      );
    }
  );
});

// PEDIDOS - LISTAR
app.get('/pedidos', authenticateToken, (req, res) => {
  let whereClause = '';
  let params = [];

  if (req.user.rol === 'conductor') {
    whereClause = 'WHERE p.conductor_id = ?';
    params = [req.user.id];
  } else if (req.user.rol === 'creador_pedidos') {
    whereClause = 'WHERE p.created_by = ?';
    params = [req.user.id];
  }

  db.all(`
    SELECT p.*, u.nombre as conductor_nombre, creador.nombre as creador_nombre
    FROM pedidos p
    LEFT JOIN usuarios u ON p.conductor_id = u.id
    LEFT JOIN usuarios creador ON p.created_by = creador.id
    ${whereClause}
    ORDER BY p.created_at DESC
  `, params, (err, pedidos) => {
    if (err) return res.status(500).json({ error: 'DB Error' });

    // Cargar items para cada pedido
    const resultado = [];
    let processed = 0;

    if (pedidos.length === 0) return res.json([]);

    pedidos.forEach(pedido => {
      db.all('SELECT * FROM items_pedido WHERE pedido_id = ?', [pedido.id], (err, items) => {
        resultado.push({ ...pedido, items: items || [] });
        processed++;
        if (processed === pedidos.length) {
          res.json(resultado);
        }
      });
    });
  });
});

// PEDIDOS - CREAR
app.post('/pedidos', authenticateToken, requireRole('admin', 'creador_pedidos'), (req, res) => {
  const {
    cliente,
    direccion,
    lat,
    lng,
    levantado,
    levantado_en_mostrador,
    sin_levantado_mostrador,
    items
  } = req.body;

  const clienteNormalizado = String(cliente || '').trim();

  if (!clienteNormalizado) {
    return res.status(400).json({ error: 'El cliente es obligatorio' });
  }

  db.run(
    'INSERT OR IGNORE INTO clientes (nombre, direccion) VALUES (?, ?)',
    [clienteNormalizado, direccion || null],
    () => {}
  );

  db.run(`
    INSERT INTO pedidos (
      cliente,
      direccion,
      lat,
      lng,
      levantado,
      levantado_en_mostrador,
      sin_levantado_mostrador,
      created_by
    )
    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
  `, [
    clienteNormalizado,
    direccion,
    lat ?? null,
    lng ?? null,
    levantado,
    levantado_en_mostrador || null,
    sin_levantado_mostrador ? 1 : 0,
    req.user.id
  ], function(err) {
    if (err) return res.status(500).json({ error: 'DB Error' });

    const pedido_id = this.lastID;

    // Agregar items
    items?.forEach(item => {
      db.run(
        'INSERT OR IGNORE INTO articulos (codigo, nombre, precio) VALUES (?, ?, ?)',
        [item.codigo || null, item.nombre, item.precio ?? 0],
        () => {}
      );

      db.run(`
        INSERT INTO items_pedido (pedido_id, codigo, nombre, cantidad, precio)
        VALUES (?, ?, ?, ?, ?)
      `, [pedido_id, item.codigo || null, item.nombre, item.cantidad, item.precio ?? 0]);
    });

    res.status(201).json({ id: pedido_id, mensaje: 'Pedido creado' });
  });
});

// PEDIDOS - ASIGNAR
app.post('/pedidos/:id/asignar', authenticateToken, requireRole('admin'), (req, res) => {
  const { conductor_id } = req.body;
  const { id } = req.params;

  db.run(`
    UPDATE pedidos SET conductor_id = ?, estado = 'asignado'
    WHERE id = ?
  `, [conductor_id, id], function(err) {
    if (err) return res.status(500).json({ error: 'DB Error' });
    res.json({ mensaje: 'Pedido asignado' });
  });
});

app.post('/pedidos/:id/aceptar', authenticateToken, requireRole('conductor'), (req, res) => {
  const { id } = req.params;

  db.get(
    `SELECT p.*, u.nombre as conductor_nombre
     FROM pedidos p
     LEFT JOIN usuarios u ON p.conductor_id = u.id
     WHERE p.id = ?`,
    [id],
    (err, pedido) => {
      if (err) return res.status(500).json({ error: 'DB Error' });
      if (!pedido) return res.status(404).json({ error: 'Pedido no encontrado' });
      if (!pedido.conductor_id || Number(pedido.conductor_id) !== Number(req.user.id)) {
        return res.status(403).json({ error: 'No puedes aceptar un pedido asignado a otro conductor' });
      }
      if (pedido.estado === 'aceptado') {
        return res.status(400).json({ error: 'El pedido ya fue aceptado' });
      }

      db.run(
        `UPDATE pedidos SET estado = 'aceptado', accepted_at = CURRENT_TIMESTAMP WHERE id = ?`,
        [id],
        function(updateErr) {
          if (updateErr) return res.status(500).json({ error: 'DB Error' });

          const destinatarios = new Set();
          if (pedido.created_by) {
            destinatarios.add(Number(pedido.created_by));
          }

          db.all(`SELECT id FROM usuarios WHERE rol = 'admin'`, (adminsErr, admins) => {
            if (adminsErr) return res.status(500).json({ error: 'DB Error' });

            admins.forEach((admin) => destinatarios.add(Number(admin.id)));

            const mensaje = `El pedido #${pedido.id} de ${pedido.cliente} fue aceptado por ${pedido.conductor_nombre || 'el conductor asignado'}`;
            const destinatariosArray = Array.from(destinatarios);

            if (destinatariosArray.length === 0) {
              return res.json({ mensaje: 'Pedido aceptado' });
            }

            let processed = 0;
            destinatariosArray.forEach((usuarioId) => {
              db.run(
                `INSERT INTO notificaciones (usuario_id, pedido_id, tipo, mensaje, leida)
                 VALUES (?, ?, 'pedido_aceptado', ?, 0)`,
                [usuarioId, pedido.id, mensaje],
                (notificationErr) => {
                  if (notificationErr) return res.status(500).json({ error: 'DB Error' });
                  processed += 1;
                  if (processed === destinatariosArray.length) {
                    res.json({ mensaje: 'Pedido aceptado' });
                  }
                }
              );
            });
          });
        }
      );
    }
  );
});

// PEDIDOS - ACTUALIZAR
app.put('/pedidos/:id', authenticateToken, requireRole('admin'), (req, res) => {
  const {
    cliente,
    direccion,
    lat,
    lng,
    estado,
    levantado,
    levantado_en_mostrador,
    sin_levantado_mostrador
  } = req.body;
  const { id } = req.params;

  db.run(`
    UPDATE pedidos
    SET cliente = ?, direccion = ?, lat = ?, lng = ?, estado = ?, levantado = ?, levantado_en_mostrador = ?, sin_levantado_mostrador = ?
    WHERE id = ?
  `, [
    cliente,
    direccion,
    lat ?? null,
    lng ?? null,
    estado,
    levantado,
    levantado_en_mostrador || null,
    sin_levantado_mostrador ? 1 : 0,
    id
  ], function(err) {
    if (err) return res.status(500).json({ error: 'DB Error' });
    res.json({ mensaje: 'Pedido actualizado' });
  });
});

// MIS PEDIDOS - Conductor
app.get('/mis-pedidos/:conductor_id', authenticateToken, requireRole('conductor'), (req, res) => {
  const { conductor_id } = req.params;

  if (Number(conductor_id) !== Number(req.user.id)) {
    return res.status(403).json({ error: 'No puedes consultar los pedidos de otro conductor' });
  }

  db.all(`
    SELECT p.*, u.nombre as conductor_nombre, creador.nombre as creador_nombre
    FROM pedidos p
    LEFT JOIN usuarios u ON p.conductor_id = u.id
    LEFT JOIN usuarios creador ON p.created_by = creador.id
    WHERE p.conductor_id = ?
    ORDER BY p.created_at DESC
  `, [conductor_id], (err, pedidos) => {
    if (err) return res.status(500).json({ error: 'DB Error' });

    const resultado = [];
    let processed = 0;

    if (pedidos.length === 0) return res.json([]);

    pedidos.forEach(pedido => {
      db.all('SELECT * FROM items_pedido WHERE pedido_id = ?', [pedido.id], (err, items) => {
        resultado.push({ ...pedido, items: items || [] });
        processed++;
        if (processed === pedidos.length) {
          res.json(resultado);
        }
      });
    });
  });
});

app.get('/notificaciones', authenticateToken, (req, res) => {
  db.all(
    `SELECT id, usuario_id, pedido_id, tipo, mensaje, leida, created_at
     FROM notificaciones
     WHERE usuario_id = ?
     ORDER BY created_at DESC
     LIMIT 30`,
    [req.user.id],
    (err, notifications) => {
      if (err) return res.status(500).json({ error: 'DB Error' });
      res.json(notifications);
    }
  );
});

app.post('/notificaciones/:id/leer', authenticateToken, (req, res) => {
  const { id } = req.params;

  db.run('UPDATE notificaciones SET leida = 1 WHERE id = ? AND usuario_id = ?', [id, req.user.id], function(err) {
    if (err) return res.status(500).json({ error: 'DB Error' });
    if (this.changes === 0) return res.status(404).json({ error: 'Notificación no encontrada' });
    res.json({ mensaje: 'Notificación marcada como leída' });
  });
});

// ENTREGAS - CREAR
app.post('/entregas', authenticateToken, requireRole('admin', 'conductor'), upload.single('foto'), (req, res) => {
  const { pedido_id, observaciones } = req.body;
  const foto = req.file ? req.file.filename : null;

  db.run(`
    INSERT INTO entregas (pedido_id, foto, observaciones)
    VALUES (?, ?, ?)
  `, [pedido_id, foto, observaciones], function(err) {
    if (err) return res.status(500).json({ error: 'DB Error' });

    // Cambiar estado a entregado
    db.run('UPDATE pedidos SET estado = ? WHERE id = ?', ['entregado', pedido_id], (err) => {
      if (err) return res.status(500).json({ error: 'DB Error' });
      res.status(201).json({ id: this.lastID, mensaje: 'Entrega registrada' });
    });
  });
});

// ENTREGA - OBTENER
app.get('/entregas/:pedido_id', authenticateToken, (req, res) => {
  const { pedido_id } = req.params;

  db.get('SELECT * FROM entregas WHERE pedido_id = ?', [pedido_id], (err, entrega) => {
    if (err) return res.status(500).json({ error: 'DB Error' });
    res.json(entrega || {});
  });
});

// HEALTH CHECK
app.get('/health', (req, res) => {
  res.json({ status: 'OK' });
});

// INICIAR SERVIDOR
app.listen(PORT, () => {
  console.log(`\n🚀 Backend ejecutándose en http://localhost:${PORT}\n`);
  console.log('Credenciales de prueba:');
  console.log('  Admin:       admin@test.com / 1234');
  console.log('  Conductor 1: conductor1@test.com / 1234');
  console.log('  Conductor 2: conductor2@test.com / 1234\n');
});
