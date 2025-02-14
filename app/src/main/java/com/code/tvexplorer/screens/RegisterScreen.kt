package com.code.tvexplorer.screens

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.code.tvexplorer.models.User
import com.code.tvexplorer.persistance.LocalStorage
import com.code.tvexplorer.persistance.SessionManager
import kotlinx.coroutines.launch
import java.io.File
import java.security.MessageDigest
import android.location.Geocoder
import android.widget.Toast
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.Manifest
import android.app.Activity
import android.location.Location
import android.provider.ContactsContract.CommonDataKinds.Email
import androidx.core.location.LocationManagerCompat.getCurrentLocation
import coil.compose.rememberAsyncImagePainter
import com.code.tvexplorer.repository.FirebaseAuthHelper
import com.code.tvexplorer.repository.FirebaseDBHelper
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth

@Composable
fun RegisterScreen(navController: NavController) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Handle image picking logic
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        profileImageUri = uri
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && profileImageUri != null) {
            profileImageUri = profileImageUri
        }
    }

    // Handle location permission and fetching
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getCurrentLocation(context) { cty, cntry ->
                city = cty
                country = cntry
            }
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Request location permission on Compose screen load
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentLocation(context) { cty, cntry ->
                city = cty
                country = cntry
            }
        } else {
            locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    fun createUser(fullName: String, email: String, password: String, age: Int, city: String, country: String, profileUrl: String) : User {
        return User(fullName, email, password, age, city, country, profileUrl)
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                // Profile Image or Icon
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(120.dp)
                        .padding(16.dp)
                        .clip(CircleShape)
                        .clickable { showBottomSheet = true }
                ) {
                    profileImageUri?.let { uri ->
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = "Profile Image",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } ?: run {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Default Profile Icon",
                            tint = Color.Gray,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }

                if (showBottomSheet) {
                    ShowImagePickerBottomSheet(
                        context = context,
                        imagePickerLauncher = imagePickerLauncher,
                        cameraLauncher = cameraLauncher,
                        onDismiss = { showBottomSheet = false }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Form Fields
                TextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = age,
                    onValueChange = { age = it },
                    label = { Text("Age") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Register Button
                Button(
                    onClick = {isLoading = true
                        coroutineScope.launch {
                            if (validateInputs(fullName, email, password, age)) {
                                FirebaseAuthHelper.registerUser(email, hashPassword(password)) { success, error ->
                                    isLoading = false
                                    if (success) {
                                        val user = createUser(
                                            fullName,
                                            email,
                                            password,
                                            age.toInt(),
                                            city,
                                            country,
                                            profileImageUri.toString()
                                        )

                                        FirebaseDBHelper.saveUserToFirebase(user) { saveSuccess ->
                                            if (saveSuccess) {
                                                LocalStorage.saveUser(context, user)
                                                Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT).show()
                                                navController.navigate("login") {
                                                    popUpTo("register") { inclusive = true }
                                                }
                                            } else {
                                                errorMessage = "Failed to save user details"
                                            }
                                        }
                                    } else {
                                        // Assign a valid error message only if it's null or not empty.
                                        errorMessage = error ?: "Registration failed"
                                    }
                                }
                            } else {
                                isLoading = false
                                errorMessage = "Please fill in all fields correctly."
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Register")
                }

                if (isLoading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator()
                }

                errorMessage?.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(it, color = Color.Red)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Already have an account? Text
                ClickableText(
                    text = AnnotatedString("Already have an account? Login"),
                    onClick = {
                        navController.navigate("login") {
                            popUpTo("register") { inclusive = true }
                        }
                    },
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Blue)
                )
            }
        }
    }
}

fun getCurrentLocation(context: Context, updateLocation: (String, String) -> Unit) {
    // Check if permission is granted
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        == PackageManager.PERMISSION_GRANTED
    ) {

        // Get the location
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->

            if (location != null) {
                try {
                    // Use Geocoder to get address from location
                    val geocoder = Geocoder(context)
                    val addressList =
                        geocoder.getFromLocation(location.latitude, location.longitude, 1)

                    // If we got an address, update the location
                    if (!addressList.isNullOrEmpty()) {
                        val address = addressList.first()
                        val city = address.locality ?: "Unknown"
                        val country = address.countryName ?: "Unknown"
                        updateLocation(city, country)
                    } else {
                        Toast.makeText(context, "Address not found", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        "Error retrieving address: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(context, "Location is null", Toast.LENGTH_SHORT).show()
            }
        }
    } else {
        // Request location permission if not granted
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            100
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowImagePickerBottomSheet(
    context: Context,
    imagePickerLauncher: ManagedActivityResultLauncher<String, Uri?>,
    cameraLauncher: ManagedActivityResultLauncher<Uri, Boolean>,
    onDismiss: () -> Unit
) {
    val options = listOf("Gallery", "Camera")
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Select an Option", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            options.forEachIndexed { index, option ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            when (index) {
                                0 -> imagePickerLauncher.launch("image/*")
                                1 -> {
                                    val imageFile = File(context.cacheDir, "profile_image.jpg")
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        imageFile
                                    )
                                    cameraLauncher.launch(uri)
                                }
                            }
                            onDismiss()
                        }
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = if (index == 0) Icons.Default.Person else Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(option, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

fun validateInputs(
    fullName: String,
    email: String,
    password: String,
    age: String
): Boolean {
    return fullName.isNotBlank() &&
            email.isNotBlank() &&
            android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
            password.length >= 6 &&
            age.toIntOrNull() != null
}

fun hashPassword(password: String): String {
    val messageDigest = MessageDigest.getInstance("SHA-256")
    val hashBytes = messageDigest.digest(password.toByteArray())
    return bytesToHex(hashBytes)
}

fun bytesToHex(bytes: ByteArray): String {
    val hexString = StringBuilder()
    for (byte in bytes) {
        val hex = String.format("%02x", byte)
        hexString.append(hex)
    }
    return hexString.toString()
}