package com.code.tvshowexplorer.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavigationBar(navController: NavController) {
    // Get current destination from navController
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // Home Navigation Item
        NavigationBarItem(
            selected = currentRoute == "home",  // Check if Home is selected
            onClick = {
                if (currentRoute != "home") {
                    navController.navigate("home")
                }
            },
            label = { Text("Home") },
            icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Home") }
        )
        // Search Navigation Item
        NavigationBarItem(
            selected = currentRoute == "search",  // Check if Search is selected
            onClick = {
                if (currentRoute != "search") {
                    navController.navigate("search")
                }
            },
            label = { Text("Search") },
            icon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") }
        )

        NavigationBarItem(
            selected = currentRoute == "favorites",  // Check if Search is selected
            onClick = {
                if (currentRoute != "favorites") {
                    navController.navigate("favorites")
                }
            },
            label = { Text("Favorites") },
            icon = { Icon(imageVector = Icons.Default.Favorite, contentDescription = "Favorites") }
        )

        // Profile Navigation Item
        NavigationBarItem(
            selected = currentRoute == "profile",  // Check if Profile is selected
            onClick = {
                if (currentRoute != "profile") {
                    navController.navigate("profile") {
                        popUpTo("profile") { inclusive = true } // Prevent going back to this screen
                    }
                }
            },
            label = { Text("Profile") },
            icon = { Icon(imageVector = Icons.Default.Person, contentDescription = "Profile") }
        )
    }
}

