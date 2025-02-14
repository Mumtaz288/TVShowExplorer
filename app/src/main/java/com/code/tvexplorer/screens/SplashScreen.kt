package com.code.tvexplorer.screens

import android.os.Bundle
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.code.tvexplorer.MainActivity
import com.code.tvexplorer.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    // Delay for splash screen before navigating (set a more reasonable delay)
    LaunchedEffect(true) {
        delay(2000)
        // After the delay, navigate to login and exit the app
        navController.navigate("login") {
            popUpTo("splash") { inclusive = true } // To avoid back stack issues
        }

        // Exit the app by calling finish on MainActivity
        (navController.context as? MainActivity)?.finish()
    }

    // Use remember to store the image resource
    val logo: Painter = painterResource(id = R.drawable.launcher) // Replace with your actual logo resource

    // Animation for logo (scale effect)
    val logoScale by animateFloatAsState(
        targetValue = 1f, // Final scale
        animationSpec = tween(durationMillis = 1000), label = "" // Animation duration
    )

    // Animation for text visibility (fade and slide effect)
    val textOpacity by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 1000), label = "" // Animation duration for opacity
    )

    // Column to vertically arrange the logo, text, and progress indicator
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Display the logo/image at the center with scaling animation
        Image(
            painter = logo,
            contentDescription = "App Logo",
            modifier = Modifier.size(150.dp).graphicsLayer(scaleX = logoScale, scaleY = logoScale) // Apply scale animation
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Animated visibility for the text (fade in and slide up)
        AnimatedVisibility(
            visible = textOpacity > 0f, // Only show if animated value is > 0
            enter = fadeIn(animationSpec = tween(durationMillis = 1000)) +
                    slideInVertically(initialOffsetY = { it / 2 }, animationSpec = tween(durationMillis = 1000)),
            modifier = Modifier.padding(top = 16.dp) // Adjust spacing between text and logo
        ) {
            Text(
                text = "Welcome to TVShow Explorer",
                style = TextStyle(
                    color = Color.Black,
                    fontSize = 20.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Circular progress indicator at the center of the screen
        CircularProgressIndicator(
            modifier = Modifier.size(50.dp), // Adjust size as needed
            color = Color.Blue,
            strokeWidth = 4.dp
        )
    }
}


