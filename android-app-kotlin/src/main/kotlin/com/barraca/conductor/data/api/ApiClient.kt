package com.barraca.conductor.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente Retrofit singleton para comunicación con la API
 */
object ApiClient {

    // ==================== CONFIGURACIÓN ====================

    private const val BASE_URL = "http://localhost:3000/v1/" // Cambiar por URL real
    private const val CONNECT_TIMEOUT = 30L
    private const val READ_TIMEOUT = 30L
    private const val WRITE_TIMEOUT = 30L

    private var _retrofit: Retrofit? = null
    private var _apiService: ConductorApiService? = null

    // ==================== PROPIEDADES ====================

    val apiService: ConductorApiService
        get() {
            if (_apiService == null) {
                _apiService = retrofit.create(ConductorApiService::class.java)
            }
            return _apiService!!
        }

    private val retrofit: Retrofit
        get() {
            if (_retrofit == null) {
                _retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(createOkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            return _retrofit!!
        }

    // ==================== MÉTODOS PRIVADOS ====================

    /**
     * Crear OkHttpClient con interceptores y timeouts
     */
    private fun createOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                // Agregar token JWT al header de todas las requests
                val originalRequest = chain.request()
                val requestBuilder = originalRequest.newBuilder()

                // TODO: Obtener token del SharedPreferences o DataStore
                val token = "tu_token_aqui"
                requestBuilder.addHeader("Authorization", "Bearer $token")

                chain.proceed(requestBuilder.build())
            }
            .build()
    }

    /**
     * Reiniciar cliente (útil después de cambiar token)
     */
    fun reset() {
        _retrofit = null
        _apiService = null
    }
}
