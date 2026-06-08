package com.plataforma.arrendamientos

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.plataforma.arrendamientos.ui.navigation.AppNavigation
import com.plataforma.arrendamientos.ui.theme.PlataformaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* resultado ignorado: si el usuario rechaza, las notifs simplemente no aparecen */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        setContent {
            var isDarkTheme by remember { mutableStateOf(prefs.getBoolean("dark_mode", false)) }
            PlataformaTheme(
                darkTheme = isDarkTheme,
                onToggleTheme = {
                    isDarkTheme = !isDarkTheme
                    prefs.edit().putBoolean("dark_mode", isDarkTheme).apply()
                }
            ) {
                AppNavigation()
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
