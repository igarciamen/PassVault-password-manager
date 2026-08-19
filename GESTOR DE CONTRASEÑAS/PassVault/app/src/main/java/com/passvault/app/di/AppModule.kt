package com.passvault.app.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Módulo genérico para providers que no encajen en un módulo más
 * específico (DatabaseModule, RepositoryModule, SecurityModule...).
 * Vacío por ahora, a propósito.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule