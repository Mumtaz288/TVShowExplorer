package com.code.tvshowexplorer.models

data class User(
    val fullName: String,
    val email: String,
    val hashedPassword: String,
    val age: Int,
    val city: String,
    val country: String,
    val profileImageUri: String,
    val favorites: Set<TVShow> = mutableSetOf()
)