package com.code.tvshowexplorer.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavController
import com.code.tvshowexplorer.components.BottomNavigationBar
import com.code.tvshowexplorer.components.TVShowItemGrid
import com.code.tvshowexplorer.models.TVShow
import com.code.tvshowexplorer.persistance.LocalStorage
import com.code.tvshowexplorer.persistance.SessionManager
import com.code.tvshowexplorer.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavController, token: String) {
    // Get the context and ViewModelStoreOwner
    val context = LocalContext.current
    val viewModelStoreOwner = LocalViewModelStoreOwner.current

    // Manually create the ViewModel using ViewModelProvider
    val viewModel: SearchViewModel =
        ViewModelProvider(viewModelStoreOwner!!).get(SearchViewModel::class.java)

    // Observe the states from the ViewModel
    val searchQuery by viewModel.searchQuery
    val searchResults by viewModel.searchResults
    val isLoading by viewModel.isLoading
    val allGenres by viewModel.allGenres
    val selectedGenres by viewModel.selectedGenres

    // Function to perform search when button is clicked
    fun performSearch(query: String) {
        viewModel.performSearch(query, token)
    }

    LaunchedEffect(Unit) {
        viewModel.loadGenres(token)
    }

    // Manage the favorite state for each TV Show in the search results
    var favorites by remember { mutableStateOf(setOf<Int>()) }

    // Function to toggle the favorite status
    val onFavoriteClick: (TVShow) -> Unit = { tvShow ->
        val username = SessionManager.getUserEmail(context)
        val currentUser = LocalStorage.getUser(context, username)

        if (currentUser != null) {
            val updatedFavorites = currentUser.favorites.toMutableSet()

            if (updatedFavorites.contains(tvShow)) {
                updatedFavorites.remove(tvShow)
            } else {
                updatedFavorites.add(tvShow)
            }

            val updatedUser = currentUser.copy(favorites = updatedFavorites)
            val isSaved = LocalStorage.updateUser(context, updatedUser)

            if (isSaved) {
                Toast.makeText(context, "Favorites updated", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to update favorites", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Scaffold to manage Top Bar, Bottom Navigation, and content
    Scaffold(
        bottomBar = {
            // Bottom Navigation Bar
            BottomNavigationBar(navController)
        },
        content = { paddingValues ->
            // Main content of the screen, adjusted with padding for Top App Bar
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = 16.dp,
                        top = 16.dp,
                        end = 16.dp,
                        bottom = paddingValues.calculateBottomPadding()
                    )
            ) {
                // Row for TextField and Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Search TextField
                    TextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        label = { Text("Search TV Shows") },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )

                    // Search Button with Icon
                    Button(onClick = { performSearch(searchQuery) }) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Genre Filter Row
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(allGenres) { genre ->
                        FilterChip(
                            selected = selectedGenres.contains(genre.id),
                            onClick = { viewModel.toggleGenreSelection(genre.id) },
                            label = { Text(genre.name) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Show loading spinner if searching
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Display search results (if any)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(searchResults.size) { index ->
                        // TV Show Item Grid
                        TVShowItemGrid(
                            tvShow = searchResults[index],
                            onClick = { tvShowId ->
                                navController.navigate("details/$tvShowId")
                            },
                            onFavoriteClick = onFavoriteClick,
                            isFavorite = favorites.contains(searchResults[index].id)
                        )
                    }
                }
            }
        }
    )
}
