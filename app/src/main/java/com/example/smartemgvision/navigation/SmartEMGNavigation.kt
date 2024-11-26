package com.example.smartemgvision.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.smartemgvision.IntelligentInteractionScreen
import com.example.smartemgvision.ui.screens.WelcomeScreen


@Composable
fun SmartEMGNavigation(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Routes.WELCOME,
        modifier = modifier
    ) {
        composable(Routes.WELCOME) {
            WelcomeScreen(
                onNavigateToInteraction = { navController.navigate(Routes.INTELLIGENT_INTERACTION) }
            )
        }
        composable(Routes.INTELLIGENT_INTERACTION) {
            IntelligentInteractionScreen(
                onBack = { navController.popBackStack() }
            )
        }

    }
}