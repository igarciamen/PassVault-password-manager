package com.passvault.app.di

import android.content.Context
import androidx.room.Room
import com.passvault.app.data.PassVaultDatabase
import com.passvault.app.data.PasswordDao
import com.passvault.app.security.PassphraseManager
import com.passvault.app.security.UnlockSessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providePassVaultDatabase(
        @ApplicationContext context: Context,
        passphraseManager: PassphraseManager,
        // ===== INICIO CAMBIOS BLOQUE 4 =====
        unlockSessionManager: UnlockSessionManager
        // ===== FIN CAMBIOS BLOQUE 4 =====
    ): PassVaultDatabase {
        System.loadLibrary("sqlcipher")

        // ===== INICIO CAMBIOS BLOQUE 4 =====
        val masterKey = checkNotNull(unlockSessionManager.getSessionKey()) {
            "PassVaultDatabase solicitada sin sesión desbloqueada. " +
                    "La navegación debe garantizar login/creación de contraseña " +
                    "maestra ANTES de llegar a cualquier pantalla que use Room."
        }
        val passphrase = passphraseManager.getOrCreateDatabasePassphrase(masterKey)
        // ===== FIN CAMBIOS BLOQUE 4 =====
        val factory = SupportOpenHelperFactory(passphrase)

        return Room.databaseBuilder(
            context,
            PassVaultDatabase::class.java,
            "passvault_v2.db" // Bloque 4: nueva BD, el esquema de cifrado de la passphrase cambió
        )
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    @Provides
    fun providePasswordDao(database: PassVaultDatabase): PasswordDao = database.passwordDao()
}