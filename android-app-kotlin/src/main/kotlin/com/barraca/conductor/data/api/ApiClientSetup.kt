// Configuración del Cliente API con JWT
// Archivo: android-app-kotlin/src/main/kotlin/com/barraca/conductor/data/api/ApiClient.kt

package com.barraca.conductor.data.api

import com.barraca.conductor.utils.JwtTokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Factory para crear instancia de Retrofit con JWT Authentication
 * 
 * Configuración:
 * - Logger de HTTP (debug)
 * - JWT Interceptor (auth)
 * - Timeout de 30s
 * - Converter Gson para JSON
 */
object ApiClient {
    
    private var retrofit: Retrofit? = null
    
    fun getRetrofit(
        baseUrl: String,
        tokenManager: JwtTokenManager
    ): Retrofit {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(createOkHttpClient(tokenManager))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }

    private fun createOkHttpClient(tokenManager: JwtTokenManager): OkHttpClient {
        return OkHttpClient.Builder()
            // Logging para debug
            .addInterceptor(createLoggingInterceptor())
            // JWT Authentication
            .addInterceptor(JwtAuthInterceptor(tokenManager))
            // Timeouts
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private fun createLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor { message ->
            Timber.tag("OkHttp").d(message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    fun reset() {
        retrofit = null
    }
}

/**
 * Crear instancia del servicio API con JWT
 * 
 * Uso en Hilt Module:
 * ```
 * @Module
 * @InstallIn(SingletonComponent::class)
 * object NetworkModule {
 *     @Singleton
 *     @Provides
 *     fun provideConductorApiService(
 *         @Named("baseUrl") baseUrl: String,
 *         tokenManager: JwtTokenManager
 *     ): ConductorApiService {
 *         return ApiClient.getRetrofit(baseUrl, tokenManager)
 *             .create(ConductorApiService::class.java)
 *     }
 * }
 * ```
 */
