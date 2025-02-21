package com.mobiletreeplantingapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.mobiletreeplantingapp.data.datastore.ThemeMode
import com.mobiletreeplantingapp.data.datastore.ThemePreferences
import com.mobiletreeplantingapp.navigation.RootNavGraph
import com.mobiletreeplantingapp.ui.theme.FirebaseAuthenticationTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var themePreferences: ThemePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        var keepSplashScreen = true
        
        splashScreen.setKeepOnScreenCondition { keepSplashScreen }
        
        // Simulate some loading work
        lifecycleScope.launch {
            delay(2000) // 2 seconds delay
            keepSplashScreen = false
        }

        setContent {
            val currentTheme by themePreferences.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            
            FirebaseAuthenticationTheme(
                darkTheme = when (currentTheme) {
                    ThemeMode.DARK -> true
                    ThemeMode.LIGHT -> false
                    ThemeMode.SYSTEM -> isSystemInDarkTheme()
                }
            ) {
                RootNavGraph(navController = rememberNavController(), context = this)
            }
        }
    }
}