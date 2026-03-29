package com.barraca.conductor.di

import com.barraca.conductor.data.repository.PedidoRepository
import com.barraca.conductor.data.repository.FotoRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt para proporcionar dependencias de repositories
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    /**
     * Proporcionar instancia singleton de PedidoRepository
     */
    @Provides
    @Singleton
    fun providePedidoRepository(): PedidoRepository {
        return PedidoRepository()
    }

    /**
     * Proporcionar instancia singleton de FotoRepository
     */
    @Provides
    @Singleton
    fun provideFotoRepository(): FotoRepository {
        return FotoRepository()
    }
}

/**
 * Módulo Hilt para proporcionar dependencias de API
 */
@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    /**
     * Los servicios de API se proporcionan a través de ApiClient singleton
     * Este módulo puede ampliarse con configuraciones adicionales
     */
    @Provides
    @Singleton
    fun provideApiClient(): com.barraca.conductor.data.api.ConductorApiService {
        return com.barraca.conductor.data.api.ApiClient.apiService
    }
}
