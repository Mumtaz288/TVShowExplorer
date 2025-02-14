package com.code.tvexplorer.screens

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.code.tvexplorer.repository.TMDBRepository
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    token: String,
    onTVShowClick: (Int) -> Unit,
    navController: NavController
) {
    val context = LocalContext.current
    val tvShows = remember { mutableStateListOf<TVShow>() }
    val coroutineScope = rememberCoroutineScope()
    val isLoading = remember { mutableStateOf(true) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val selectedFilter = remember { mutableStateOf("Popular") }
    val isMenuExpanded = remember { mutableStateOf(false) }
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isLoading.value)

    // Network status listener
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    // Network status state
    val isNetworkAvailable = remember { mutableStateOf(isNetworkAvailable(context)) }

    // Refresh and Fetch TV Shows
    fun fetchTVShows(filter: String) {
        coroutineScope.launch {
            isLoading.value = true
            errorMessage.value = null
            try {
                // Fetch shows based on filter
                val shows = when (filter) {
                    "Airing Today" -> TMDBRepository.getAiringTodayTVShows(token)
                    "On TV" -> TMDBRepository.getOnTVShows(token)
                    "Top Rated" -> TMDBRepository.getTopRatedTVShows(token)
                    else -> TMDBRepository.getPopularTVShows(token)
                }
                if (shows.results.isEmpty()) {
                    errorMessage.value = "No TV shows found."
                } else {
                    tvShows.clear()
                    tvShows.addAll(shows.results.filter { it.name.isNotBlank() })
                }
            } catch (e: Exception) {
                // Show error or fall back to cache if offline
                errorMessage.value = if (isNetworkAvailable.value) {
                    "Failed to load TV shows: ${e.message}"
                } else {
                    "No internet connection. Showing cached data."
                }
            } finally {
                isLoading.value = false
            }
        }
    }

    // Handle swipe refresh
    fun onRefresh() {
        isLoading.value = true
        errorMessage.value = null
        fetchTVShows(selectedFilter.value)
    }

    // Favorite Management
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
            if (LocalStorage.updateUser(context, updatedUser)) {
                Toast.makeText(context, "Favorites updated", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to update favorites", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun isInFavorite(tvShow: TVShow): Boolean {
        val username = SessionManager.getUserEmail(context)
        val currentUser = LocalStorage.getUser(context, username)
        return currentUser?.favorites?.contains(tvShow) == true
    }

    // Refresh based on network connectivity
    LaunchedEffect(isNetworkAvailable.value) {
        isNetworkAvailable.value = isNetworkAvailable(context)
        if (isNetworkAvailable.value) {
            fetchTVShows(selectedFilter.value) // Automatically fetch TV shows if network is available
        }
    }

    LaunchedEffect(Unit) {
        fetchTVShows(selectedFilter.value)
    }

    // Scaffold UI
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TV Shows") },
                actions = {
                    IconButton(onClick = { isMenuExpanded.value = !isMenuExpanded.value }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Filter")
                    }
                    DropdownMenu(
                        expanded = isMenuExpanded.value,
                        onDismissRequest = { isMenuExpanded.value = false }
                    ) {
                        listOf("Popular", "Airing Today", "On TV", "Top Rated").forEach { filter ->
                            DropdownMenuItem(
                                text = { Text(filter) },
                                onClick = {
                                    selectedFilter.value = filter
                                    fetchTVShows(filter)
                                    isMenuExpanded.value = false
                                }
                            )
                        }
                    }
                }
            )
        },
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { innerPadding ->
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = { onRefresh() }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    when {
                        isLoading.value -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        errorMessage.value != null -> {
                            Text(
                                text = errorMessage.value!!,
                                fontSize = 18.sp,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(16.dp)
                            )
                        }

                        tvShows.isEmpty() -> {
                            Text(
                                text = "No TV shows available. Pull to refresh.",
                                fontSize = 18.sp,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(16.dp)
                            )
                        }

                        else -> {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(tvShows.size) { index ->
                                    TVShowItemGrid(
                                        tvShow = tvShows[index],
                                        onClick = onTVShowClick,
                                        onFavoriteClick = onFavoriteClick,
                                        isFavorite = isInFavorite(tvShow = tvShows[index])
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
