package com.code.tvexplorer.persistance

import android.content.Context
import android.content.SharedPreferences
import com.code.tvexplorer.models.User
import com.code.tvexplorer.repository.FirebaseDBHelper
import com.google.gson.Gson

object LocalStorage {

    private const val PREF_NAME = "local_storage"
    private const val USER_KEY_PREFIX = "user_"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // Save user to SharedPreferences
    fun saveUser(context: Context, user: User): Boolean {
        val sharedPreferences = getSharedPreferences(context)
        if (!doesUserExist(context, user.email)) {
            val userJson = Gson().toJson(user)  // Convert User object to JSON
            sharedPreferences.edit().putString(USER_KEY_PREFIX + user.email, userJson).apply()

            // Save user to Firebase
            FirebaseDBHelper.saveUserToFirebase(user) { success ->
            }

            return true
        }
        return false
    }

    // Update user details in SharedPreferences
    fun updateUser(context: Context, updatedUser: User): Boolean {
        val sharedPreferences = getSharedPreferences(context)
        val userJson = Gson().toJson(updatedUser)  // Convert updated User object to JSON
        val editor = sharedPreferences.edit()

        if (doesUserExist(context, updatedUser.email)) {
            // If user exists, update the user's data
            editor.putString(USER_KEY_PREFIX + updatedUser.email, userJson).apply()

            // Update user in Firebase
            FirebaseDBHelper.updateUserInFirebase(updatedUser) { success ->

            }

            return true
        }
        return false // Return false if the user does not exist
    }

    // Retrieve the user by username from SharedPreferences
    fun getUser(context: Context, email: String?): User? {
        // Check local storage first
        if (email != null) {
            val sharedPreferences = getSharedPreferences(context)
            val userJson = sharedPreferences.getString(USER_KEY_PREFIX + email, null)

            // If the user exists in local storage, return it
            if (userJson != null) {
                return Gson().fromJson(userJson, User::class.java)  // Convert JSON back to User object
            }

            // If not found in local storage, fetch from Firebase
            FirebaseDBHelper.getUserFromFirebase(email) { user ->
                if (user != null) {
                    // Save user to local storage after fetching from Firebase
                    saveUser(context, user)
                }
            }
        }

        return null
    }

    // Get password for a specific user from SharedPreferences
    fun getUserPassword(context: Context, username: String): String? {
        val user = getUser(context, username)
        return user?.hashedPassword
    }

    // Check if a user exists by their username in SharedPreferences
    private fun doesUserExist(context: Context, username: String): Boolean {
        val sharedPreferences = getSharedPreferences(context)
        return sharedPreferences.contains(USER_KEY_PREFIX + username)
    }
}
