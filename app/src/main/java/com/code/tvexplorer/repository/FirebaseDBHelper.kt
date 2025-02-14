package com.code.tvexplorer.repository

import android.annotation.SuppressLint
import android.util.Log
import com.code.tvexplorer.models.TVShow
import com.code.tvexplorer.models.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase

object FirebaseDBHelper {

    @SuppressLint("StaticFieldLeak")
    private val db = FirebaseFirestore.getInstance()
   // val dv = Firebase.firestore

    fun saveUserToFirebase(user: User, onComplete: (Boolean) -> Unit) {
        // Convert the Set<TVShow> to a list of maps for Firestore
        val favoritesList = user.favorites.map { tvShow ->
            mapOf(
                "id" to tvShow.id,
                "name" to tvShow.name,
                "poster_path" to tvShow.poster_path,
                "backdrop_path" to tvShow.backdrop_path,
                "overview" to tvShow.overview,
                "first_air_date" to tvShow.first_air_date,
                "isFavorite" to tvShow.isFavorite,
                "genreIds" to tvShow.genreIds
            )
        }

        val userMap = mapOf(
            "fullName" to user.fullName,
            "email" to user.email,
            "hashedPassword" to user.hashedPassword,
            "age" to user.age,
            "city" to user.city,
            "country" to user.country,
            "profileImageUri" to user.profileImageUri,
            "favorites" to favoritesList // Storing the list of favorites
        )

        db.collection("users")
            .document(user.email)
            .set(userMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true)
                } else {
                    Log.e("FirebaseDBHelper", "Error saving user: ${task.exception?.message}")
                    onComplete(false)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseDBHelper", "Error saving user details: ${exception.message}")
                onComplete(false)
            }
    }

    // Update an existing user in Firebase
    fun updateUserInFirebase(user: User, onComplete: (Boolean) -> Unit) {
        // Convert Set<TVShow> to List<Map<String, Any>>
        val favoritesList = user.favorites.map { tvShow ->
            mapOf(
                "id" to tvShow.id,
                "name" to tvShow.name,
                "poster_path" to tvShow.poster_path,
                "backdrop_path" to tvShow.backdrop_path,
                "overview" to tvShow.overview,
                "first_air_date" to tvShow.first_air_date,
                "isFavorite" to tvShow.isFavorite,
                "genreIds" to tvShow.genreIds
            )
        }

        val userMap = mapOf(
            "fullName" to user.fullName,
            "email" to user.email,
            "hashedPassword" to user.hashedPassword,
            "age" to user.age,
            "city" to user.city,
            "country" to user.country,
            "profileImageUri" to user.profileImageUri,
            "favorites" to favoritesList
        )

        db.collection("users")
            .document(user.email)
            .update(userMap)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    // Retrieve a user by username from Firebase
    fun getUserFromFirebase(email: String, onComplete: (User?) -> Unit) {
        db.collection("users")
            .document(email)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document != null && document.exists()) {
                        // Get favorites as List<Map<String, Any>> and convert it to Set<TVShow>
                        val favoritesList = document.get("favorites") as? List<Map<String, Any>> ?: emptyList()
                        val favoritesSet = favoritesList.map { tvShowMap ->
                            TVShow(
                                id = tvShowMap["id"] as? Int ?: 0,
                                name = tvShowMap["name"] as? String ?: "",
                                poster_path = tvShowMap["poster_path"] as? String ?: "",
                                backdrop_path = tvShowMap["backdrop_path"] as? String ?: "",
                                overview = tvShowMap["overview"] as? String ?: "",
                                first_air_date = tvShowMap["first_air_date"] as? String ?: "",
                                isFavorite = tvShowMap["isFavorite"] as? Boolean ?: false,
                                genreIds = tvShowMap["genreIds"] as? List<Int> ?: emptyList()
                            )
                        }.toSet()

                        // Convert document to User object and include favoritesSet
                        val user = document.toObject(User::class.java)?.copy(favorites = favoritesSet)
                        onComplete(user)
                    } else {
                        onComplete(null)
                    }
                } else {
                    onComplete(null)
                }
            }
    }


}