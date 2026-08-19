package com.passvault.app.di

import com.passvault.app.data.PasswordRepositoryImpl
import com.passvault.app.domain.PasswordRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindPasswordRepository(impl: PasswordRepositoryImpl): PasswordRepository
}