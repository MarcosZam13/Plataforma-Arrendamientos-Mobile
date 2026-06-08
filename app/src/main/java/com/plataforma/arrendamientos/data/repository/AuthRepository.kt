package com.plataforma.arrendamientos.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.plataforma.arrendamientos.data.model.User
import com.plataforma.arrendamientos.data.model.UserRole
import com.plataforma.arrendamientos.data.remote.ApiService
import com.plataforma.arrendamientos.data.remote.LoginRequest
import com.plataforma.arrendamientos.data.remote.RegisterRequest
import com.plataforma.arrendamientos.di.TokenHolder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

@Singleton
class AuthRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: ApiService,
    private val tokenHolder: TokenHolder
) {
    private val USER_ID_KEY    = stringPreferencesKey("user_id")
    private val USER_NAME_KEY  = stringPreferencesKey("user_name")
    private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
    private val USER_ROLE_KEY  = stringPreferencesKey("user_role")
    private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")

    val currentUser: Flow<User?> = context.dataStore.data.map { prefs ->
        val id     = prefs[USER_ID_KEY]    ?: return@map null
        val nombre = prefs[USER_NAME_KEY]  ?: return@map null
        val correo = prefs[USER_EMAIL_KEY] ?: return@map null
        val rolStr = prefs[USER_ROLE_KEY]  ?: return@map null
        val rol    = runCatching { UserRole.valueOf(rolStr) }.getOrNull() ?: return@map null
        User(id = id, nombre = nombre, correo = correo, rol = rol)
    }

    /** Restaura el JWT al TokenHolder al arrancar la app (llamar desde Application/MainActivity). */
    suspend fun restoreToken() {
        context.dataStore.data.firstOrNull()?.get(AUTH_TOKEN_KEY)?.let {
            tokenHolder.token = it
        }
    }

    suspend fun login(correo: String, contrasena: String): Result<User> {
        return try {
            val response = apiService.login(LoginRequest(correo, contrasena))
            if (response.isSuccessful) {
                val body = response.body()
                    ?: return Result.failure(Exception("Respuesta vacía del servidor"))
                val token = body.getToken()
                if (token.isBlank())
                    return Result.failure(Exception("El servidor no devolvió un token"))
                tokenHolder.token = token
                val user = User(
                    id     = body.getUserId(),
                    nombre = body.nombre.ifBlank { correo },
                    correo = body.getEmail().ifBlank { correo },
                    rol    = roleFromString(body.getRole())
                )
                saveUser(user, token)
                Result.success(user)
            } else {
                val msg = when (response.code()) {
                    401  -> "🔐 Correo o contraseña incorrectos"
                    404  -> "👤 Usuario no encontrado"
                    429  -> "⏳ Demasiados intentos, esperá un momento"
                    503  -> "🏖️ El servidor está de vacaciones, volvé pronto"
                    else -> "Error ${response.code()}: ${response.message()}"
                }
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("🌴 Sin conexión con el servidor. ¿El backend agarró vacaciones?"))
        }
    }

    suspend fun register(nombre: String, correo: String, contrasena: String, rol: UserRole): Result<User> {
        return try {
            val rolStr = if (rol == UserRole.DUENO) "dueno" else "arrendatario"
            val response = apiService.register(RegisterRequest(nombre, correo, contrasena, rolStr))
            if (response.isSuccessful) {
                val body = response.body()
                    ?: return Result.failure(Exception("Respuesta vacía del servidor"))
                val token = body.getToken()
                tokenHolder.token = token.ifBlank { null }.let { token }
                val user = User(
                    id     = body.getUserId().ifBlank { "user-${System.currentTimeMillis()}" },
                    nombre = body.nombre.ifBlank { nombre },
                    correo = body.getEmail().ifBlank { correo },
                    rol    = rol
                )
                saveUser(user, token.ifBlank { "pending-${user.id}" })
                Result.success(user)
            } else {
                val msg = when (response.code()) {
                    409  -> "📧 Este correo ya está registrado"
                    400  -> "📋 Datos de registro inválidos"
                    503  -> "🏖️ El servidor de usuarios está de descanso"
                    else -> "Error ${response.code()}: ${response.message()}"
                }
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("🌴 Sin conexión con el servidor"))
        }
    }

    fun findUserByEmail(correo: String): User? = null  // ya no usamos mock

    suspend fun loginOrRegisterWithGoogle(
        nombre: String,
        correo: String,
        googleId: String,
        rol: UserRole
    ): Result<User> {
        // Google Sign-In aún requiere integración Firebase ↔ MS Usuarios.
        // Por ahora persiste sesión local sin token real.
        val user = User(
            id     = "google-$googleId",
            nombre = nombre,
            correo = correo,
            rol    = rol
        )
        saveUser(user, "google-token-${user.id}")
        return Result.success(user)
    }

    suspend fun logout() {
        tokenHolder.token = null
        context.dataStore.edit { prefs ->
            prefs.remove(USER_ID_KEY)
            prefs.remove(USER_NAME_KEY)
            prefs.remove(USER_EMAIL_KEY)
            prefs.remove(USER_ROLE_KEY)
            prefs.remove(AUTH_TOKEN_KEY)
        }
    }

    suspend fun isLoggedIn(): Boolean =
        context.dataStore.data.firstOrNull()?.get(AUTH_TOKEN_KEY) != null

    private fun roleFromString(rol: String): UserRole =
        if (rol.contains("dueno") || rol.contains("dueño") || rol.contains("arrendador"))
            UserRole.DUENO else UserRole.INQUILINO

    private suspend fun saveUser(user: User, token: String) {
        context.dataStore.edit { prefs ->
            prefs[USER_ID_KEY]    = user.id
            prefs[USER_NAME_KEY]  = user.nombre
            prefs[USER_EMAIL_KEY] = user.correo
            prefs[USER_ROLE_KEY]  = user.rol.name
            prefs[AUTH_TOKEN_KEY] = token
        }
    }
}
