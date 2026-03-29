## 🚀 BACKEND - Instrucciones

```bash
cd backend-complete
npm install
node index.js
```

Servidor en: `http://localhost:3000`

**Credenciales precargadas:**
- Admin: `admin@test.com` / `1234`
- Conductor 1: `conductor1@test.com` / `1234`
- Conductor 2: `conductor2@test.com` / `1234`

**Prueba de login:**
```bash
curl -X POST http://localhost:3000/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@test.com","password":"1234"}'
```
