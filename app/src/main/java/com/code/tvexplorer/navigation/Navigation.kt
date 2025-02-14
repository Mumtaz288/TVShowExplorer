package com.code.tvexplorer.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.code.tvexplorer.screens.EditProfileScreen
import com.code.tvexplorer.screens.FavoritesScreen
import com.code.tvexplorer.screens.HomeScreen
import com.code.tvexplorer.screens.LoginScreen
import com.code.tvexplorer.screens.ProfileScreen
import com.code.tvexplorer.screens.RegisterScreen
import com.code.tvexplorer.screens.SearchScreen
import com.code.tvexplorer.screens.SplashScreen
import com.code.tvexplorer.screens.TVShowDetailsScreen

@Composable
fun TVExplorerNavHost(
    navController: NavHostController,
    apiKey: String, // API Key for login
) {
    NavHost(navController = navController, startDestination = "splash") {

        composable("splash") {
            SplashScreen(navController = navController)
        }

        composable("login") {
            LoginScreen(navController = navController)
        }

        composable("register") {
            RegisterScreen(navController = navController)
        }

        composable("home") {
            HomeScreen(token = apiKey, onTVShowClick = { tvShowId ->
                navController.navigate("details/$tvShowId")
            }, navController)
        }

        composable("details/{tvShowId}") { backStackEntry ->
            val tvShowId = backStackEntry.arguments?.getString("tvShowId")?.toInt()
            tvShowId?.let {
                TVShowDetailsScreen(
                    tvShowId,
                    token = apiKey,
                    onBack = { navController.popBackStack() },
                    navController
                )
            }
        }

        composable("favorites") {
            FavoritesScreen(navController = navController)
        }

        composable("search") {
            SearchScreen(navController = navController, token = apiKey)
        }

        composable("profile") {
            ProfileScreen(navController)
        }

        composable("editProfile/{userEmail}") { backStackEntry ->
            val userEmail = backStackEntry.arguments?.getString("userEmail")
            if (userEmail != null) {
                EditProfileScreen(navController, userEmail)
            } else {
                // Handle null case or navigate back
                navController.popBackStack()
            }
        }

    }
}
