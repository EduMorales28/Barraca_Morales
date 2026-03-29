#!/usr/bin/env bash

# BARRACA MORALES - Script de Setup

echo "🚀 BARRACA MORALES - Setup Automático"
echo "=====================================\n"

# Backend
echo "1️⃣ Instalando Backend..."
cd backend-complete
npm install
echo "✅ Backend listo"
echo "\nPara iniciar: cd backend-complete && node index.js\n"

# Web
echo "2️⃣ Instalando Web Admin..."
cd ../web-admin
npm install
echo "✅ Web Admin lista"
echo "\nPara iniciar: cd web-admin && npm run dev\n"

echo "3️⃣ App Android lista en: android-app-kotlin/"
echo "✅ Abrir en Android Studio\n"

echo "=====================================\n"
echo "🎉 COMANDOS PARA EJECUTAR:\n"
echo "BACKEND:  cd backend-complete && node index.js"
echo "WEB:      cd web-admin && npm run dev"
echo "ANDROID:  Abrir en Android Studio → Run 'app'\n"
echo "Credenciales:"
echo "  Admin: admin@test.com / 1234"
echo "  Conductor: conductor1@test.com / 1234"
