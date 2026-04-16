package com.plataforma.arrendamientos.di

import com.plataforma.arrendamientos.data.repository.AuthRepository
import com.plataforma.arrendamientos.data.repository.DataRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // Repositories are @Singleton and @Inject constructor, so Hilt provides them automatically.
    // This module can hold network/db bindings when backend is connected.
}
