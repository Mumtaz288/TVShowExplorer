package com.code.tvexplorer.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthEmailException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

object FirebaseAuthHelper {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Register a new user
    fun registerUser(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    // Log in an existing user
    fun loginUser(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        try {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onResult(true, null)
                    } else {
                        val errorMessage = when (task.exception) {
                            is FirebaseAuthInvalidCredentialsException -> "Invalid email or password."
                            is FirebaseAuthInvalidUserException -> "User not found."
                            is FirebaseAuthEmailException -> "Malformed email address."
                            else -> "Login failed: ${task.exception?.message}"
                        }
                        onResult(false, errorMessage)
                    }
                }
        } catch (e: Exception) {
            Log.e("FirebaseAuthHelper", "Error during login: ${e.localizedMessage}")
            onResult(false, "An unexpected error occurred: ${e.localizedMessage}")
        }

    }

}
