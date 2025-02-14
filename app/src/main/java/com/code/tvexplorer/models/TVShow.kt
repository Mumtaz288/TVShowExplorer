package com.code.tvexplorer.models

data class TVShow(
    val id: Int,
    val name: String,
    val poster_path: String,
    val backdrop_path: String,
    var overview: String,
    var first_air_date: String,
    var isFavorite: Boolean = false,
    var genreIds: List<Int>
)