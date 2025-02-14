package com.code.tvexplorer.screens

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
import com.code.tvexplorer.components.BottomNavigationBar
import com.code.tvexplorer.persistance.LocalStorage
import com.code.tvexplorer.persistance.SessionManager

@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current

    // Retrieve user data from session
    val email = SessionManager.getUserEmail(context)
    val user = if (email?.isNotEmpty() == true) LocalStorage.getUser(context, email) else null
    val fullName = user?.fullName ?: "Guest"
    val age = user?.age ?: 18
    val profileImage = user?.profileImageUri
    val country = user?.country ?: "Not Available"
    val city = user?.city ?: "Not Available"

    Scaffold(
        bottomBar = { BottomNavigationBar(navController = navController) },
        content = { paddingValues ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(bottom = paddingValues.calculateBottomPadding())
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
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile Icon",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .clickable { }
                        )
                    }
                }

                Text(text = fullName, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))

                // Display email and age with condition
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (email != null) {
                        Text(text = email, style = MaterialTheme.typography.bodyLarge)
                    }
                    Text(text = getUserAge(age = age), style = MaterialTheme.typography.bodyLarge)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Location: $city, $country", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))

                // Edit Profile Button
                Button(
                    onClick = { navController.navigate("editProfile/$email") }
                ) {
                    Text("Edit Profile")
                }

                Spacer(modifier = Modifier.height(8.dp))

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

// Determine if user is an adult or not
private fun getUserAge(age: Int): String = if (age > 18) "Adult" else "Not Adult"

// Handle user logout
private fun handleLogout(context: Context, navController: NavController) {
    SessionManager.clearSession(context)
    navController.navigate("login") {
        popUpTo(0) { inclusive = true }
    }
}
