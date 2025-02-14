package com.code.tvexplorer.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.code.tvexplorer.components.BottomNavigationBar
import com.code.tvexplorer.components.TVShowItemGrid
import com.code.tvexplorer.models.TVShow
import com.code.tvexplorer.persistance.LocalStorage
import com.code.tvexplorer.persistance.SessionManager
import com.code.tvexplorer.repository.FirebaseDBHelper
import com.code.tvexplorer.repository.FirebaseDBHelper.updateUserInFirebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(navController: NavController) {

    val context = LocalContext.current

    val username = SessionManager.getUserEmail(context)
    val currentUser =
        if (username?.isNotEmpty() == true) LocalStorage.getUser(context, username) else null
    val favoriteShows = currentUser?.favorites ?: emptyList()

    // Function to toggle the favorite status
    val onFavoriteClick: (TVShow) -> Unit = { tvShow ->

        // Ensure currentUser is not null
        if (currentUser != null) {
            // Get the current list of favorite TV show IDs (assuming it's a Set of IDs)
            var updatedFavorites = currentUser.favorites.toMutableSet()

            // Toggle the favorite state
            if (updatedFavorites.contains(tvShow)) {
                // Remove from favorites
                updatedFavorites.remove(tvShow)
            } else {
                // Add to favorites
                updatedFavorites.add(tvShow)
            }

            // Update user favorites and save it to LocalStorage
            val updatedUser = currentUser.copy(favorites = updatedFavorites)
            val isSaved = LocalStorage.updateUser(context, updatedUser)

            if (isSaved) {
                updateUserInFirebase(updatedUser, {
                    Toast.makeText(context, "Favorites updated", Toast.LENGTH_SHORT).show()
                })
            } else {
                Toast.makeText(context, "Failed to update favorites", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Favorite TV Shows") },
            )
        },
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { paddingValues ->
        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Padding for bottom bar
                .padding(16.dp)
        ) {
            if (favoriteShows.isEmpty()) {
                // Show message if no favorites
                Text(
                    text = "No favorites added yet.",
                    fontSize = 18.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(favoriteShows.size) { index ->
                        val fList = favoriteShows.toList()
                        TVShowItemGrid(
                            tvShow = fList[index], onClick = { tvShowId ->
                                navController.navigate("details/$tvShowId")
                            },
                            onFavoriteClick = onFavoriteClick,
                            isFavorite = true
                        )
                    }
                }
            }
        }
    }
}
