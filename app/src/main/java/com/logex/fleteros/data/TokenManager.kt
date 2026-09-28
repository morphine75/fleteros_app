package com.logex.fleteros.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Guarda el token y los datos basicos de sesion cifrados en el celular.
 * Requiere la dependencia androidx.security:security-crypto
 */
class TokenManager(private val context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    // Si el archivo cifrado no se puede leer (por ejemplo, quedo restaurado
    // desde una copia de seguridad de Google con una clave de Keystore que
    // ya no existe en este celular), en vez de crashear se borra el archivo
    // viejo y se arranca una sesion limpia, como si fuera la primera vez.
    private val prefs: SharedPreferences = crearPrefsConRecuperacion()

    private fun crearPrefsConRecuperacion(): SharedPreferences {
        return try {
            crearPrefs()
        } catch (e: Exception) {
            context.getSharedPreferences(NOMBRE_ARCHIVO, Context.MODE_PRIVATE).edit().clear().apply()
            context.deleteSharedPreferences(NOMBRE_ARCHIVO)
            crearPrefs()
        }
    }

    private fun crearPrefs(): SharedPreferences {
        return EncryptedSharedPreferences.create(
            context,
            NOMBRE_ARCHIVO,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun guardarSesion(token: String, usuario: Usuario) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putInt(KEY_ID_USUARIO, usuario.id_usuario)
            .putString(KEY_USUARIO, usuario.usuario)
            .putString(KEY_NOMBRE, usuario.nombre)
            .putInt(KEY_NUMERO_GUIA, usuario.numero_guia)
            .putInt(KEY_SUCURSAL, usuario.sucursal)
            .apply()
    }

    fun token(): String? = prefs.getString(KEY_TOKEN, null)

    fun nombreUsuario(): String? = prefs.getString(KEY_NOMBRE, null)

    fun numeroGuia(): Int = prefs.getInt(KEY_NUMERO_GUIA, 0)

    fun haySesion(): Boolean = token() != null

    fun cerrarSesion() {
        // commit() en vez de apply(): necesitamos que el borrado
        // se escriba a disco YA, porque justo despues de esto
        // se cierra la app (finishAndRemoveTask) y el proceso
        // podria morir antes de que un apply() asincrono termine.
        prefs.edit().clear().commit()
    }

    companion object {
        private const val NOMBRE_ARCHIVO = "auth_prefs"
        private const val KEY_TOKEN = "token"
        private const val KEY_ID_USUARIO = "id_usuario"
        private const val KEY_USUARIO = "usuario"
        private const val KEY_NOMBRE = "nombre"
        private const val KEY_NUMERO_GUIA = "numero_guia"
        private const val KEY_SUCURSAL = "sucursal"
    }
}