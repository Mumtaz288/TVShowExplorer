package com.code.tvexplorer.screens

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.code.tvexplorer.models.User
import com.code.tvexplorer.persistance.LocalStorage
import com.code.tvexplorer.persistance.SessionManager
import com.code.tvexplorer.repository.FirebaseAuthHelper
import com.code.tvexplorer.repository.FirebaseDBHelper
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    navController: NavController
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Login", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))

                // Username and Password fields
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") })
                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    isLoading = true
                    errorMessage = null  // Reset error message
                    coroutineScope.launch {
                        try {
                            FirebaseAuthHelper.loginUser(email, hashPassword(password)) { success, error ->
                                if (success) {
                                    val user = LocalStorage.getUser(context, email)

                                    if (user != null) {
                                        // If user is found in local storage, save session and navigate to home
                                        SessionManager.saveSessionId(context, email)

                                        // Navigate to home screen
                                        navController.navigate("home") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                } else {
                                    // Validate login locally if Firebase login fails
                                    val result = validateLogin(context, email, password)
                                    if (result) {
                                        // Save session
                                        SessionManager.saveSessionId(context, email)

                                        // Navigate to home screen
                                        navController.navigate("home") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    } else {
                                        errorMessage = error ?: "Invalid email or password"
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            // Catch any exception during the login process
                            errorMessage = "Login failed: ${e.localizedMessage}"
                        } finally {
                            isLoading = false
                        }
                    }
                }) {
                    Text("Login")
                }

                if (isLoading) {
                    CircularProgressIndicator()
                }

                errorMessage?.let {
                    Text(it, color = Color.Red)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Don't have an account? Text
                ClickableText(
                    text = AnnotatedString("Don't have an account? Register"),
                    onClick = {
                        navController.navigate("register") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Blue)
                )
            }
        }
    }
}

fun validateLogin(context: Context, username: String, password: String): Boolean {
    val hashedPassword = hashPassword(password)
    return try {
        val storedPassword = LocalStorage.getUserPassword(context = context, username)
        storedPassword == hashedPassword
    } catch (e: Exception) {
        false
    }
}