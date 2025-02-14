package com.code.tvexplorer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.rememberNavController
import com.code.tvexplorer.navigation.TVExplorerNavHost
import com.code.tvexplorer.persistance.SessionManager
import com.code.tvexplorer.ui.theme.TVExplorerTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    private val ACCESS_TOKEN =
        "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI2MTVkNTEwZTQyZTk1M2FiZWI2Y2I5ZjkwNjg5ZTNiOCIsIm5iZiI6MTczNTg5ODQ0NS4wMzcsInN1YiI6IjY3NzdiNTRkNDk2ZGQ5NTJjODcyNGYxMyIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.GBbyH9WFOjPkpO0kPw2-DepXO4F7y5Qbyq5jPijNQOc"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContent {
            TVExplorerTheme {
                val navController = rememberNavController()
                val context = this@MainActivity

                // Check for session ID on launch and auto-login if exists
                LaunchedEffect(Unit) {
                    val sessionId = SessionManager.getSessionId(context)
                    if (sessionId != null) {
                        // If session exists, navigate to home and save session
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        // If no session exists, navigate to login
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                }

                // Pass the access token securely to the navigation host
                TVExplorerNavHost(
                    navController = navController,
                    apiKey = ACCESS_TOKEN, // Pass the access token
                )
            }
        }
    }
}