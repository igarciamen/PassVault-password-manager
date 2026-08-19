package com.passvault.app.di

import android.content.Context
import com.passvault.app.security.AutoLockPreferenceManager
import com.passvault.app.security.BiometricKeyManager
import com.passvault.app.security.BiometricPreferenceManager
import com.passvault.app.security.ExportManager
import com.passvault.app.security.ImportManager
import com.passvault.app.security.KeyStoreManager
import com.passvault.app.security.MasterPasswordManager
import com.passvault.app.security.PassphraseManager
import com.passvault.app.security.RecoveryCodeManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {

    @Provides
    @Singleton
    fun provideKeyStoreManager(): KeyStoreManager = KeyStoreManager()

    @Provides
    @Singleton
    fun providePassphraseManager(
        @ApplicationContext context: Context,
        keyStoreManager: KeyStoreManager
    ): PassphraseManager = PassphraseManager(context, keyStoreManager)

    @Provides
    @Singleton
    fun provideMasterPasswordManager(@ApplicationContext context: Context): MasterPasswordManager =
        MasterPasswordManager(context)

    @Provides
    @Singleton
    fun provideRecoveryCodeManager(
        @ApplicationContext context: Context,
        keyStoreManager: KeyStoreManager
    ): RecoveryCodeManager = RecoveryCodeManager(context, keyStoreManager)

    @Provides
    @Singleton
    fun provideBiometricKeyManager(): BiometricKeyManager = BiometricKeyManager()

    @Provides
    @Singleton
    fun provideBiometricPreferenceManager(@ApplicationContext context: Context): BiometricPreferenceManager =
        BiometricPreferenceManager(context)

    @Provides
    @Singleton
    fun provideAutoLockPreferenceManager(@ApplicationContext context: Context): AutoLockPreferenceManager =
        AutoLockPreferenceManager(context)

    @Provides
    @Singleton
    fun provideExportManager(): ExportManager = ExportManager()

    @Provides
    @Singleton
    fun provideImportManager(): ImportManager = ImportManager()
}