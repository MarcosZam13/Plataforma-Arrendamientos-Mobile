package com.plataforma.arrendamientos.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.plataforma.arrendamientos.data.model.MockData
import com.plataforma.arrendamientos.data.model.User
import com.plataforma.arrendamientos.data.model.UserRole
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

@Singleton
class AuthRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val USER_ID_KEY = stringPreferencesKey("user_id")
    private val USER_NAME_KEY = stringPreferencesKey("user_name")
    private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
    private val USER_ROLE_KEY = stringPreferencesKey("user_role")
    private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")

    val currentUser: Flow<User?> = context.dataStore.data.map { prefs ->
        val id = prefs[USER_ID_KEY] ?: return@map null
        val nombre = prefs[USER_NAME_KEY] ?: return@map null
        val correo = prefs[USER_EMAIL_KEY] ?: return@map null
        val rolStr = prefs[USER_ROLE_KEY] ?: return@map null
        val rol = runCatching { UserRole.valueOf(rolStr) }.getOrNull() ?: return@map null
        User(id = id, nombre = nombre, correo = correo, rol = rol)
    }

    suspend fun login(correo: String, contrasena: String): Result<User> {
        // Demo authentication - match web app mock users
        val mockUser = MockData.MOCK_USERS.find {
            it.correo.equals(correo, ignoreCase = true)
        }

        return if (mockUser != null && contrasena == "123456") {
            saveUser(mockUser, "demo-token-${mockUser.id}")
            Result.success(mockUser)
        } else {
            Result.failure(Exception("Correo o contraseña incorrectos"))
        }
    }

    suspend fun register(nombre: String, correo: String, contrasena: String, rol: UserRole): Result<User> {
        // Demo registration
        val existingUser = MockData.MOCK_USERS.find {
            it.correo.equals(correo, ignoreCase = true)
        }
        if (existingUser != null) {
            return Result.failure(Exception("Este correo ya está registrado"))
        }

        val newUser = User(
            id = "user-${System.currentTimeMillis()}",
            nombre = nombre,
            correo = correo,
            rol = rol
        )
        saveUser(newUser, "demo-token-${newUser.id}")
        return Result.success(newUser)
    }

    suspend fun logout() {
        context.dataStore.edit { prefs ->
            prefs.remove(USER_ID_KEY)
            prefs.remove(USER_NAME_KEY)
            prefs.remove(USER_EMAIL_KEY)
            prefs.remove(USER_ROLE_KEY)
            prefs.remove(AUTH_TOKEN_KEY)
        }
    }

    suspend fun isLoggedIn(): Boolean {
        return context.dataStore.data.firstOrNull()?.get(AUTH_TOKEN_KEY) != null
    }

    private suspend fun saveUser(user: User, token: String) {
        context.dataStore.edit { prefs ->
            prefs[USER_ID_KEY] = user.id
            prefs[USER_NAME_KEY] = user.nombre
            prefs[USER_EMAIL_KEY] = user.correo
            prefs[USER_ROLE_KEY] = user.rol.name
            prefs[AUTH_TOKEN_KEY] = token
        }
    }
}
