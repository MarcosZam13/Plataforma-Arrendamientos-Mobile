package com.plataforma.arrendamientos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.plataforma.arrendamientos.ui.navigation.AppNavigation
import com.plataforma.arrendamientos.ui.theme.PlataformaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
}
