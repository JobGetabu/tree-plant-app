package com.mobiletreeplantingapp.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.firebase.auth.FirebaseUser
import com.mobiletreeplantingapp.data.repository.AuthRepositoryImpl
import com.mobiletreeplantingapp.data.datastore.OnboardingPreferences
import com.mobiletreeplantingapp.ui.screen.onboarding.OnboardingScreen

@Composable
fun RootNavGraph(
    navController: NavHostController, 
    context: Context,
    onboardingPreferences: OnboardingPreferences = OnboardingPreferences(context)
) {
    val authManager = AuthRepositoryImpl(context)
    val user: FirebaseUser? = authManager.getCurrentUser()
    val onboardingCompleted by onboardingPreferences.isOnboardingCompleted.collectAsState(initial = false)

    NavHost(
        navController = navController,
        route = Graph.ROOT,
        startDestination = if (!onboardingCompleted) Graph.ONBOARDING else if (user == null) Graph.LOGIN else Graph.MAIN
    ) {
        composable(route = Graph.ONBOARDING) {
            OnboardingScreen(
                onFinishOnboarding = {
                    navController.navigate(Graph.LOGIN) {
                        popUpTo(Graph.ROOT) { inclusive = true }
                    }
                }
            )
        }
        loginGraph(navController = navController)
        composable(route = Graph.MAIN) {
            MainScreen(rootNavController = navController)
        }
    }
}

object Graph {
    const val ROOT = "root_graph"
    const val ONBOARDING = "onboarding_graph"
    const val LOGIN = "login_graph"
    const val MAIN = "main_graph"
}