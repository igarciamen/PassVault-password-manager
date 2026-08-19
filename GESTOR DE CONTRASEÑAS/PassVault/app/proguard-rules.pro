# Bloque 7 — reglas de ProGuard/R8 para la build de release.

# SQLCipher usa JNI (puente nativo): si R8 renombra o elimina estas clases,
# la base de datos cifrada deja de poder abrirse.
-keep class net.zetetic.database.** { *; }
-keep class net.sqlcipher.** { *; }

# WorkManager instancia los Workers por reflexión usando su nombre de clase
# completo — si R8 lo renombra, WorkManager no lo encuentra en tiempo de
# ejecución y el borrado automático del portapapeles deja de funcionar.
-keep class com.passvault.app.security.ClipboardClearWorker { *; }

# Entidad de Room: se mantiene por seguridad, aunque Room ya suele resolver
# esto solo a través de sus reglas de consumidor incluidas en la librería.
-keep class com.passvault.app.data.PasswordEntryEntity { *; }

# Room, Hilt, Compose y Kotlin Coroutines ya incluyen sus propias reglas de
# consumidor dentro de sus artefactos (.aar) — no hace falta añadirlas aquí
# a mano.