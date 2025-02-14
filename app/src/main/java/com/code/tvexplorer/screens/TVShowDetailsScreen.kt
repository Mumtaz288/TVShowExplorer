package com.code.tvexplorer.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.code.tvexplorer.components.BottomNavigationBar
import com.code.tvexplorer.models.TVShow
import com.code.tvexplorer.repository.TMDBRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TVShowDetailsScreen(
    tvShowId: Int,
    token: String,
    onBack: () -> Unit,
    navController: NavController
) {
    var tvShowDetails by remember { mutableStateOf<Pair<String, TVShow>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(tvShowId) {
        coroutineScope.launch {
            try {
                tvShowDetails = TMDBRepository.getTVShowDetails(tvShowId, token)
            } catch (e: Exception) {
                errorMessage = "Failed to load details: ${e.message}"
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "TV Show Details",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val screenWidth = constraints.maxWidth

            // Background Image (Poster) - It takes up the entire screen and adjusts to the screen size
            tvShowDetails?.second?.poster_path?.let { backdropPath ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.TopCenter)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter("https://image.tmdb.org/t/p/w500$backdropPath"),
                        contentDescription = "Background Image",
                        modifier = Modifier
                            .fillMaxSize()
                    )
                }
            }

            // Gradient overlay for fading effect at the bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp) // Height of gradient fade
                    .align(Alignment.BottomCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)),
                            startY = 0f,
                            endY = 1000f
                        )
                    )
            )

            // Content Box with semi-transparent background to make text readable
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)) // Semi-transparent background
                    .padding(16.dp)
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    errorMessage != null -> {
                        Text(
                            text = errorMessage ?: "Error loading data",
                            color = Color.Red,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    tvShowDetails != null -> {
                        val tvShow = tvShowDetails?.second

                        // Scrollable Content with LazyColumn
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                                .padding(16.dp)
                        ) {
                            item {
                                tvShow?.let {
                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Poster Image (for display within content area)
                                    if (tvShow.poster_path.isNotEmpty()) {
                                        Image(
                                            painter = rememberAsyncImagePainter("https://image.tmdb.org/t/p/w500${tvShow.poster_path}"),
                                            contentDescription = tvShow.name,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(250.dp)
                                                .clip(RoundedCornerShape(32.dp))
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Title
                                    Row (
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .align(alignment = Alignment.Center),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = tvShow.name.takeIf { it.isNotBlank() }
                                                ?: "TV Show Name",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Genres
                                    Text(
                                        text = "Genres: ",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = tvShowDetails?.first ?: "No genres available",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Overview
                                    Text(
                                        text = "Overview:",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = tvShow.overview,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White
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
