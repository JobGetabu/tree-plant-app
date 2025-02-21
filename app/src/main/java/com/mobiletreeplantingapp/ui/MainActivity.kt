package com.mobiletreeplantingapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.mobiletreeplantingapp.navigation.RootNavGraph
import com.mobiletreeplantingapp.ui.theme.FirebaseAuthenticationTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FirebaseAuthenticationTheme {
                RootNavGraph(navController = rememberNavController(), context = this)
            }
        }
    }
}