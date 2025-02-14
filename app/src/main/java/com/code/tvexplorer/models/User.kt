package com.code.tvexplorer.models

data class User(
    var fullName: String,
    val email: String,
    val hashedPassword: String,
    var age: Int,
    var city: String,
    var country: String,
    var profileImageUri: String,
    val favorites: Set<TVShow> = mutableSetOf()
)