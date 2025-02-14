package com.code.tvexplorer.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.code.tvexplorer.models.User
import com.code.tvexplorer.persistance.LocalStorage
import com.code.tvexplorer.repository.FirebaseDBHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavController, userEmail: String) {
    val context = LocalContext.current
    var user by remember { mutableStateOf<User?>(null) }

    // Load user details
    LaunchedEffect(userEmail) {
        user = LocalStorage.getUser(context, userEmail)
    }

    // User editable fields
    var fullName by remember { mutableStateOf(user?.fullName ?: "") }
    var age by remember { mutableStateOf(user?.age.toString()) }
    var city by remember { mutableStateOf(user?.city ?: "") }
    var country by remember { mutableStateOf(user?.country ?: "") }
    var profileImageUri by remember { mutableStateOf(user?.profileImageUri ?: "") }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            profileImageUri = uri.toString()
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile image
                Card(
                    shape = CircleShape,
                    modifier = Modifier
                        .size(120.dp)
                        .clickable {
                            imagePickerLauncher.launch("image/*")
                        }
                ) {
                    if (profileImageUri.isNotEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(profileImageUri),
                            contentDescription = "Profile Image",
                            modifier = Modifier.clip(CircleShape)
                        )
                    } else {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Profile Icon",
                            modifier = Modifier.size(120.dp)
                        )
                    }
                }

                // Text fields for user data
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") }
                )
                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it },
                    label = { Text("Age") }
                )
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("City") }
                )
                OutlinedTextField(
                    value = country,
                    onValueChange = { country = it },
                    label = { Text("Country") }
                )

                Spacer(Modifier.height(16.dp))

                // Save button
                Button(
                    onClick = {
                        user?.let {
                            it.fullName = fullName
                            it.age = age.toInt()
                            it.city = city
                            it.country = country
                            it.profileImageUri = profileImageUri

                            // Update user in local storage and Firebase
                            LocalStorage.updateUser(context, it)
                            FirebaseDBHelper.updateUserInFirebase(it) { success ->
                                if (success) {
                                    navController.popBackStack()
                                } else {
                                    // Handle update failure
                                }
                            }
                        }
                    }
                ) {
                    Text("Save Changes")
                }
            }

        }
    )
}
