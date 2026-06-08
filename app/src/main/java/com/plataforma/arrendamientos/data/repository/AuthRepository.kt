package com.plataforma.arrendamientos.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.plataforma.arrendamientos.BuildConfig
import com.plataforma.arrendamientos.data.model.User
import com.plataforma.arrendamientos.data.model.UserRole
import com.plataforma.arrendamientos.di.TokenHolder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

// ─── DTOs de respuesta del MS de Usuarios ────────────────────────────────────

@Serializable
private data class UsuarioDto(
    val id: String = "",
    val nombre: String = "",
    val correo: String = "",
    val rol: String = "",
    val telefono: String? = null,
    val avatar: String? = null
)

@Serializable
private data class LoginResponseDto(
    val token: String = "",
    val refreshToken: String = "",
    val usuario: UsuarioDto = UsuarioDto()
)

@Serializable
private data class ErrorResponseDto(
    val message: String = "",
    val error: String = ""
)

@Singleton
class AuthRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tokenHolder: TokenHolder
) {
    private val USER_ID_KEY    = stringPreferencesKey("user_id")
    private val USER_NAME_KEY  = stringPreferencesKey("user_name")
    private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
    private val USER_ROLE_KEY  = stringPreferencesKey("user_role")
    private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")

    init {
        // Restore token from DataStore so Retrofit calls work after app restart
        kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
            context.dataStore.data.firstOrNull()?.get(AUTH_TOKEN_KEY)?.let {
                tokenHolder.token = it
            }
        }
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    private val json   = Json { ignoreUnknownKeys = true; isLenient = true }
    private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()

    val currentUser: Flow<User?> = context.dataStore.data.map { prefs ->
        val id     = prefs[USER_ID_KEY]    ?: return@map null
        val nombre = prefs[USER_NAME_KEY]  ?: return@map null
        val correo = prefs[USER_EMAIL_KEY] ?: return@map null
        val rolStr = prefs[USER_ROLE_KEY]  ?: return@map null
        val rol    = runCatching { UserRole.valueOf(rolStr.uppercase()) }.getOrNull() ?: return@map null
        User(id = id, nombre = nombre, correo = correo, rol = rol)
    }

    // ─── Login ────────────────────────────────────────────────────────────────
    suspend fun login(correo: String, contrasena: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val body = """{"correo":"${correo.trim().lowercase()}","contrasena":"$contrasena"}"""
                val request = Request.Builder()
                    .url("${BuildConfig.API_BASE_URL}auth/login")
                    .addHeader("Ocp-Apim-Subscription-Key", BuildConfig.APIM_SUBSCRIPTION_KEY)
                    .post(body.toRequestBody(JSON_MEDIA))
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    val dto = json.decodeFromString<LoginResponseDto>(responseBody)
                    val user = dto.toUser()
                    saveUser(user, dto.token)
                    Result.success(user)
                } else {
                    val error = runCatching {
                        json.decodeFromString<ErrorResponseDto>(responseBody)
                    }.getOrNull()
                    val msg = when (response.code) {
                        401  -> "🔐 Correo o contraseña incorrectos"
                        404  -> "👤 Usuario no encontrado"
                        429  -> "⏳ Demasiados intentos, esperá un momento"
                        503  -> "🏖️ El servidor está de vacaciones, volvé pronto"
                        else -> error?.message?.ifBlank { error.error }
                            ?: "💥 Error inesperado (${response.code}). Intentá de nuevo."
                    }
                    Result.failure(Exception(msg))
                }
            } catch (e: SocketTimeoutException) {
                Result.failure(Exception("⏳ El servidor tardó demasiado. Intentá de nuevo en un momento."))
            } catch (e: UnknownHostException) {
                Result.failure(Exception("📡 Sin conexión a internet. Verificá tu red."))
            } catch (e: Exception) {
                Result.failure(Exception("No se pudo conectar al servidor. Verificá tu conexión."))
            }
        }
    }

    // ─── Registro ─────────────────────────────────────────────────────────────
    suspend fun register(nombre: String, correo: String, contrasena: String, rol: UserRole): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val rolStr = rol.name.lowercase()
                val body = """{"nombre":"${nombre.trim()}","correo":"${correo.trim().lowercase()}","contrasena":"$contrasena","rol":"$rolStr"}"""
                val request = Request.Builder()
                    .url("${BuildConfig.API_BASE_URL}auth/registro")
                    .addHeader("Ocp-Apim-Subscription-Key", BuildConfig.APIM_SUBSCRIPTION_KEY)
                    .post(body.toRequestBody(JSON_MEDIA))
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    val dto = json.decodeFromString<LoginResponseDto>(responseBody)
                    val user = dto.toUser()
                    saveUser(user, dto.token)
                    Result.success(user)
                } else {
                    val error = runCatching {
                        json.decodeFromString<ErrorResponseDto>(responseBody)
                    }.getOrNull()
                    val msg = when (response.code) {
                        409  -> "📧 Este correo ya está registrado"
                        400  -> "📋 Datos de registro inválidos"
                        503  -> "🏖️ El servidor de usuarios está de descanso"
                        else -> error?.message?.ifBlank { error.error }
                            ?: "💥 Error inesperado (${response.code}). Intentá de nuevo."
                    }
                    Result.failure(Exception(msg))
                }
            } catch (e: SocketTimeoutException) {
                Result.failure(Exception("⏳ El servidor tardó demasiado. Intentá de nuevo en un momento."))
            } catch (e: UnknownHostException) {
                Result.failure(Exception("📡 Sin conexión a internet. Verificá tu red."))
            } catch (e: Exception) {
                Result.failure(Exception("No se pudo conectar al servidor. Verificá tu conexión."))
            }
        }
    }

    // ─── Google Sign-In (mock — pendiente implementación real) ────────────────
    fun findUserByEmail(correo: String): User? = null

    suspend fun loginOrRegisterWithGoogle(
        nombre: String,
        correo: String,
        googleId: String,
        rol: UserRole
    ): Result<User> {
        return Result.failure(Exception("Inicio de sesión con Google no está disponible aún."))
    }

    // ─── Logout ───────────────────────────────────────────────────────────────
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

    suspend fun getAuthToken(): String? =
        context.dataStore.data.firstOrNull()?.get(AUTH_TOKEN_KEY)

    // ─── Helpers ──────────────────────────────────────────────────────────────
    private fun LoginResponseDto.toUser(): User {
        val rolNorm = when (usuario.rol.trim().lowercase()) {
            "dueno"     -> UserRole.DUENO
            "inquilino" -> UserRole.INQUILINO
            else        -> UserRole.INQUILINO
        }
        return User(
            id     = usuario.id,
            nombre = usuario.nombre,
            correo = usuario.correo,
            rol    = rolNorm
        )
    }

    private suspend fun saveUser(user: User, token: String) {
        tokenHolder.token = token
        context.dataStore.edit { prefs ->
            prefs[USER_ID_KEY]    = user.id
            prefs[USER_NAME_KEY]  = user.nombre
            prefs[USER_EMAIL_KEY] = user.correo
            prefs[USER_ROLE_KEY]  = user.rol.name
            prefs[AUTH_TOKEN_KEY] = token
        }
    }
}
