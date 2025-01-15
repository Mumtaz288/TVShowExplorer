package com.code.tvshowexplorer.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.code.tvshowexplorer.screens.FavoritesScreen
import com.code.tvshowexplorer.screens.HomeScreen
import com.code.tvshowexplorer.screens.LoginScreen
import com.code.tvshowexplorer.screens.ProfileScreen
import com.code.tvshowexplorer.screens.RegisterScreen
import com.code.tvshowexplorer.screens.SearchScreen
import com.code.tvshowexplorer.screens.SplashScreen
import com.code.tvshowexplorer.screens.TVShowDetailsScreen

@Composable
fun TVShowExplorerNavHost(
    navController: NavHostController,
    apiKey: String, // API Key for login
) {
    NavHost(navController = navController, startDestination ="splash") {

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
    }
}
