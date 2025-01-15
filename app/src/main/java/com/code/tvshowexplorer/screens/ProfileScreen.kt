package com.code.tvshowexplorer.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.code.tvshowexplorer.components.BottomNavigationBar
import com.code.tvshowexplorer.persistance.LocalStorage
import com.code.tvshowexplorer.persistance.SessionManager

@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current

    // Get user data from session (or use default values if not found)
    val email = SessionManager.getUserEmail(context)
    val user = if (email?.isNotEmpty() == true) LocalStorage.getUser(context, email) else null
    val fullName = user?.fullName ?: "Guest"
    val age = user?.age ?: 18
    val profileImage = user?.profileImageUri
    val country = user?.country ?: "Not Available"
    val city = user?.city ?: "Not Available"

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        },
        content = { paddingValues ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(bottom = paddingValues.calculateBottomPadding()) // Padding for bottom navigation
            ) {
                // Profile Image (either user image or default icon)
                Card(
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(16.dp)
                        .size(120.dp)
                ) {
                    if (!profileImage.isNullOrEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(profileImage),
                            contentDescription = "Profile Image",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(120.dp) // Keep the overall size of the Box
                                .padding(16.dp)
                                .clip(CircleShape)
                                .clickable {
                                }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person, // Use the default person icon
                                contentDescription = "Profile Icon",
                                modifier = Modifier
                                    .size(60.dp)
                            )
                        }

                    }
                }

                // User Details
                Text(text = fullName, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (email != null) {
                        Text(text = email, style = MaterialTheme.typography.bodyLarge)
                    }
                    Text(text = getUserAge(age = age), style = MaterialTheme.typography.bodyLarge)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Location Info
                Text(text = "Location: $city, $country", style = MaterialTheme.typography.bodyLarge)

                Spacer(modifier = Modifier.height(16.dp))

                // Logout Button
                Button(
                    onClick = { handleLogout(context, navController) }
                ) {
                    Text("Logout")
                }
            }
        }
    )
}


private fun getUserAge(age: Int): String {
    return if (age > 18) "Adult" else "Not Adult"
}

// Logout Function
private fun handleLogout(context: Context, navController: NavController) {
    // Use SessionManager to clear session ID and user data
    SessionManager.clearSession(context)

    // Navigate back to the Login Screen
    navController.navigate("login") {
        // Remove all back stack entries to prevent navigating back to Profile
        popUpTo(0) { inclusive = true }
    }
}
