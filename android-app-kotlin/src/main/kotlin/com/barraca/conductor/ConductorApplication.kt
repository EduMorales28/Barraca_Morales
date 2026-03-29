package com.barraca.conductor

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class para Hilt
 * Hilt requiere una Application anotada con @HiltAndroidApp
 */
@HiltAndroidApp
class ConductorApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Inicializaciones globales aquí si es necesario
    }
}
