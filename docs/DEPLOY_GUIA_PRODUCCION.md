# Guía de Despliegue - API REST en Producción

## 📋 Índice
1. Preparación local
2. Google Cloud Run
3. Heroku
4. Servidor propio (VPS)
5. Configuración de seguridad
6. Monitoreo y logs

---

## 1️⃣ PREPARACIÓN LOCAL

### Crear archivo `.env`
```env
# .env (NO SUBIR A GIT)
PORT=3000
NODE_ENV=production
JWT_SECRET=tu_super_secret_key_muy_largo_y_seguro_123
FIREBASE_PROJECT_ID=logistica-morales
FIREBASE_PRIVATE_KEY=-----BEGIN PRIVATE KEY-----\n...
FIREBASE_CLIENT_EMAIL=firebase-adminsdk@logistica-morales.iam.gserviceaccount.com
DATABASE_URL=postgresql://user:pass@localhost/logistica
CORS_ORIGIN=https://tudominio.com
```

### Actualizar `API_IMPLEMENTATION_NODEJS.js`
```javascript
// Agregar al inicio del archivo
require('dotenv').config();

const express = require('express');
const cors = require('cors');
const admin = require('firebase-admin');
const jwt = require('jsonwebtoken');

const app = express();

// Configuración de seguridad
app.use(cors({
  origin: process.env.CORS_ORIGIN || 'http://localhost:3000',
  credentials: true,
  methods: ['GET', 'POST', 'PUT', 'DELETE'],
  allowedHeaders: ['Content-Type', 'Authorization']
}));

app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ limit: '10mb', extended: true }));

// Rate limiting
const rateLimit = require('express-rate-limit');
const limiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutos
  max: 100, // máximo 100 requests por ventana
  message: 'Demasiadas solicitudes, intenta más tarde'
});

app.use('/v1/', limiter);

// Logger
const logger = require('morgan');
app.use(logger('combined'));

// ... rest del código
```

### Crear `package.json` completo
```json
{
  "name": "logistica-morales-api",
  "version": "1.0.0",
  "description": "API REST para aplicación de logística",
  "main": "API_IMPLEMENTATION_NODEJS.js",
  "scripts": {
    "start": "node API_IMPLEMENTATION_NODEJS.js",
    "dev": "nodemon API_IMPLEMENTATION_NODEJS.js",
    "test": "jest --coverage",
    "lint": "eslint .",
    "deploy:heroku": "git push heroku main",
    "deploy:gcloud": "gcloud app deploy"
  },
  "keywords": ["logistics", "api", "rest", "firebase"],
  "author": "Tu Nombre",
  "license": "MIT",
  "dependencies": {
    "express": "^4.18.2",
    "firebase-admin": "^12.0.0",
    "jsonwebtoken": "^9.1.0",
    "dotenv": "^16.3.1",
    "cors": "^2.8.5",
    "multer": "^1.4.5-lts.1",
    "morgan": "^1.10.0",
    "express-rate-limit": "^7.1.5",
    "helmet": "^7.1.0",
    "compression": "^1.7.4"
  },
  "devDependencies": {
    "nodemon": "^3.0.2",
    "jest": "^29.7.0",
    "supertest": "^6.3.3",
    "eslint": "^8.54.0"
  },
  "engines": {
    "node": "18.x",
    "npm": "9.x"
  }
}
```

---

## 2️⃣ GOOGLE CLOUD RUN

### Requisitos
- Cuenta de Google Cloud
- `gcloud` CLI instalado
- Proyecto de Firebase exitente (logistica-morales)

### Paso 1: Crear archivo `Dockerfile`
```dockerfile
FROM node:18-alpine

WORKDIR /app

# Copiar archivos
COPY package*.json ./
RUN npm ci --only=production

COPY API_IMPLEMENTATION_NODEJS.js .
COPY .env.production .env

# Exponer puerto
EXPOSE 3000

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
  CMD node -e "require('http').get('http://localhost:3000/health', (r) => {if (r.statusCode !== 200) throw new Error(r.statusCode)})"

CMD ["node", "API_IMPLEMENTATION_NODEJS.js"]
```

### Paso 2: Crear `.dockerignore`
```
node_modules
npm-debug.log
.git
.gitignore
.env (pero sí copiar .env.production)
README.md
test
docs
```

### Paso 3: Build y Deploy
```bash
# Autenticar
gcloud auth login

# Configurar proyecto
gcloud config set project logistica-morales

# Build de imagen Docker
gcloud builds submit --tag gcr.io/logistica-morales/api:latest

# Deploy a Cloud Run
gcloud run deploy logistica-api \
  --image gcr.io/logistica-morales/api:latest \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --set-env-vars="NODE_ENV=production,JWT_SECRET=$(openssl rand -base64 32)" \
  --memory 512Mi \
  --cpu 1

# Resultado
# Service [logistica-api] deployed.
# URL: https://logistica-api-xxxxx.a.run.app
```

---

## 3️⃣ HEROKU

### Requisitos
- Cuenta de Heroku
- `heroku` CLI instalado
- Repositorio Git

### Paso 1: Configurar Heroku
```bash
# Login
heroku login

# Crear app
heroku create logistica-api

# Ver URL asignada
heroku open
```

### Paso 2: Configurar variables de entorno
```bash
heroku config:set NODE_ENV=production
heroku config:set JWT_SECRET=$(openssl rand -base64 32)
heroku config:set FIREBASE_PROJECT_ID=logistica-morales
heroku config:set FIREBASE_PRIVATE_KEY="-----BEGIN PRIVATE KEY-----..."
heroku config:set FOAM_CLIENT_EMAIL="firebase-adminsdk@..."
heroku config:set CORS_ORIGIN=https://tudominio.com
```

### Paso 3: Crear `Procfile`
```
web: node API_IMPLEMENTATION_NODEJS.js
```

### Paso 4: Deploy
```bash
# Deployar por primera vez
git push heroku main

# Ver logs
heroku logs --tail

# Rollback si hay error
heroku releases
heroku rollback v5
```

---

## 4️⃣ SERVIDOR PROPIO (VPS)

### Requisitos
- Server Linux (Ubuntu 20.04+ recomendado)
- SSH access
- Dominio propio (ejemplo: api.tulogistica.com)

### Paso 1: Preparar servidor
```bash
# Conectar al servidor
ssh root@tu_ip_servidor

# Actualizar sistema
apt update && apt upgrade -y

# Instalar Node.js 18
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
apt install -y nodejs npm

# Instalar nginx (reverse proxy)
apt install -y nginx

# Instalar PM2 (gestor de procesos)
npm install -g pm2

# Instalar Let's Encrypt (SSL)
apt install -y certbot python3-certbot-nginx
```

### Paso 2: Clonar y configurar aplicación
```bash
# Crear carpeta de app
mkdir -p /var/www/logistica-api
cd /var/www/logistica-api

# Clonar repositorio (o subir archivos)
git clone https://tu-repo.git .

# Instalar dependencias
npm install --production

# Crear archivo .env
cat > .env << EOF
PORT=3000
NODE_ENV=production
JWT_SECRET=$(openssl rand -base64 32)
FIREBASE_PROJECT_ID=logistica-morales
CORS_ORIGIN=https://api.tulogistica.com
EOF

# Cambiar propietario
chown -R www-data:www-data /var/www/logistica-api
```

### Paso 3: Configurar PM2
```bash
# Iniciar aplicación con PM2
pm2 start API_IMPLEMENTATION_NODEJS.js --name "logistica-api"

# Guardar configuración
pm2 save

# Configurar start en reinicio
pm2 startup

# Verificar que está corriendo
pm2 status
```

### Paso 4: Configurar Nginx
```bash
# Crear configuración
cat > /etc/nginx/sites-available/logistica-api << 'EOF'
server {
    listen 80;
    server_name api.tulogistica.com;

    # Redirigir a HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name api.tulogistica.com;

    # Certificados SSL (se crean después)
    ssl_certificate /etc/letsencrypt/live/api.tulogistica.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/api.tulogistica.com/privkey.pem;

    # Configuración SSL
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;

    # Compresión
    gzip on;
    gzip_types text/plain text/css application/json application/javascript;

    # Proxy a Node.js
    location / {
        proxy_pass http://localhost:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;
    }

    # Logs
    access_log /var/log/nginx/logistica-api-access.log;
    error_log /var/log/nginx/logistica-api-error.log;
}
EOF

# Habilitar sitio
ln -s /etc/nginx/sites-available/logistica-api /etc/nginx/sites-enabled/
rm -f /etc/nginx/sites-enabled/default

# Verificar configuración
nginx -t

# Recargar nginx
systemctl reload nginx
```

### Paso 5: Obtener certificado SSL
```bash
# Crear certificado (automático)
certbot certonly --nginx -d api.tulogistica.com

# Configurar renovación automática
certbot renew --dry-run

# Verificar
certbot certificates
```

---

## 5️⃣ CONFIGURACIÓN DE SEGURIDAD

### Headers de Seguridad
```javascript
const helmet = require('helmet');

app.use(helmet());
app.use(helmet.contentSecurityPolicy({
  directives: {
    defaultSrc: ["'self'"],
    scriptSrc: ["'self'"],
  }
}));
```

### Validación de entrada
```javascript
const { body, validationResult } = require('express-validator');

app.post('/v1/auth/login', [
  body('email').isEmail().normalizeEmail(),
  body('password').isLength({ min: 8 })
], (req, res) => {
  const errors = validationResult(req);
  if (!errors.isEmpty()) {
    return res.status(400).json({ errors: errors.array() });
  }
  // ... continuar
});
```

### Variables de entorno seguros
```bash
# Nunca hacer esto:
export JWT_SECRET="algo"

# En su lugar, usar archivos seguros:
# 1. .env en .gitignore
# 2. Variables de entorno del servidor (Heroku, Cloud Run)
# 3. Secret managers (Google Secret Manager, AWS Secrets Manager)
```

### Backup de Firebase
```bash
# Exportar Firestore
gcloud firestore export gs://tu-bucket/backup-$(date +%Y%m%d)

# Programar backups automáticos (en Console)
# Firestore > Backups > Create Schedule
```

---

## 6️⃣ MONITOREO Y LOGS

### Logs en desarrolllador (Cloud Logging)
```bash
# Ver logs de Cloud Run
gcloud logging read "resource.type=cloud_run_revision" \
  --limit 50 \
  --format json

# Logs en tiempo real
gcloud logging read --streaming --limit 50
```

### Alertas
```bash
# Crear alerta para errores 5xx
gcloud alpha monitoring policies create \
  --notification-channels=CHANNEL_ID \
  --display-name="API Errors" \
  --condition-display-name="500 errors" \
  --condition-threshold-value=5
```

### Healthcheck
```javascript
// Agregar endpoint de salud
app.get('/health', (req, res) => {
  res.status(200).json({
    status: 'healthy',
    timestamp: new Date(),
    uptime: process.uptime()
  });
});
```

---

## 📊 Comparativa de Opciones

| Opción | Ventajas | Desventajas | Costo |
|--------|----------|-------------|--------|
| **Google Cloud Run** | Sin servidor, escalado automático, integrado con Firebase | Curva de aprendizaje | $0.00003/request (muy barato) |
| **Heroku** | Muy simple, bueno para MVP | Más caro, sin soporte HTTP/2 | $50-500/mes |
| **VPS propio** | Máximo control, mejor precio | Requiere administración, backups | $5-50/mes |

### Recomendación para producción
✅ **Google Cloud Run** → Mejor balance de costo, confiabilidad y facilidad

---

## ✅ Checklist de Deploy

- [ ] Variables de entorno configuradas correctamente
- [ ] Base de datos backup realizado
- [ ] JWT_SECRET generado de forma segura
- [ ] CORS configurado para tu dominio
- [ ] SSL/HTTPS habilitado
- [ ] Logs configurados
- [ ] Health check funcionando
- [ ] Rate limiting activo
- [ ] Tokens expirados después de 24 horas
- [ ] Endpoint de login probado
- [ ] Endpoint de crear pedido probado
- [ ] Endpoint de entrega con foto probado
- [ ] Reporte de errores configurado
- [ ] Backup automático habilitado

