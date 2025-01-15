package com.code.tvshowexplorer.repository

import com.code.tvshowexplorer.models.Genre
import com.code.tvshowexplorer.models.TVShow
import com.code.tvshowexplorer.models.TVShowResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

object TMDBRepository {

    private const val BASE_URL = "https://api.themoviedb.org/3/"
    private val client = OkHttpClient()

    suspend fun searchTVShows(query: String, token: String): TVShowResponse = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$BASE_URL/search/tv?query=$query")
            .addHeader("accept", "application/json")
            .addHeader("Authorization", "Bearer $token")
            .build()

        getTVShows(request)
    }

    suspend fun getTVGenres(token: String): List<Genre> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$BASE_URL/genre/tv/list")
            .addHeader("accept", "application/json")
            .addHeader("Authorization", "Bearer $token")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw Exception("Failed to fetch genres: ${response.code}")
                }

                val responseBody = response.body?.string()
                    ?: throw Exception("Response body is null")

                val json = JSONObject(responseBody)
                val genres = json.getJSONArray("genres")

                List(genres.length()) { index ->
                    val genre = genres.getJSONObject(index)
                    Genre(
                        id = genre.getInt("id"),
                        name = genre.getString("name")
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getPopularTVShows(token: String): TVShowResponse = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$BASE_URL/discover/tv?sort_by=popularity.desc")
            .addHeader("accept", "application/json")
            .addHeader("Authorization", "Bearer $token")
            .build()
        getTVShows(request)
    }

    suspend fun getAiringTodayTVShows(token: String): TVShowResponse = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$BASE_URL/tv/airing_today")
            .addHeader("accept", "application/json")
            .addHeader("Authorization", "Bearer $token")
            .build()
        getTVShows(request)
    }

    suspend fun getOnTVShows(token: String): TVShowResponse = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$BASE_URL/tv/on_the_air")
            .addHeader("accept", "application/json")
            .addHeader("Authorization", "Bearer $token")
            .build()
        getTVShows(request)
    }

    suspend fun getTopRatedTVShows(token: String): TVShowResponse = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$BASE_URL/tv/top_rated")
            .addHeader("accept", "application/json")
            .addHeader("Authorization", "Bearer $token")
            .build()
        getTVShows(request)
    }

    private suspend fun getTVShows(request: Request): TVShowResponse {
        return withContext(Dispatchers.IO) {
            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw Exception("Failed to fetch data: ${response.code}")
                    }

                    val responseBody = response.body?.string()
                        ?: throw Exception("Response body is null")

                    val json = JSONObject(responseBody)
                    val results = json.getJSONArray("results")

                    val tvShows = List(results.length()) { index ->
                        val item = results.getJSONObject(index)
                        TVShow(
                            id = item.getInt("id"),
                            name = item.optString("name", "Unknown"),
                            poster_path = item.optString("poster_path", ""),
                            backdrop_path = item.optString("backdrop_path", ""),
                            overview = item.optString("overview", "No overview available"),
                            first_air_date = item.optString("first_air_date", ""),
                            genreIds = getGenreIdsFromJson(item)
                        )
                    }

                    TVShowResponse(tvShows)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                TVShowResponse(emptyList())
            }
        }
    }

    private fun getGenreIdsFromJson(json: JSONObject): List<Int> {
        val genreIds = mutableListOf<Int>()
        val genreIdsArray = json.optJSONArray("genre_ids")

        genreIdsArray?.let {
            for (i in 0 until it.length()) {
                try {
                    genreIds.add(it.getInt(i))
                } catch (e: Exception) {
                    e.printStackTrace()
                    // In case genre id is not available, we can skip that
                }
            }
        }

        return genreIds
    }

    private fun getGenreList(json: JSONObject): String {
        val genresArray = json.getJSONArray("genres")
        val genreNames = mutableListOf<String>()

        // Loop through the genres array and collect the names
        for (i in 0 until genresArray.length()) {
            val genre = genresArray.getJSONObject(i)
            genreNames.add(genre.optString("name", "Unknown"))
        }

        // Join the genre names with a comma and return the result
        return genreNames.joinToString(", ")
    }

    suspend fun getTVShowDetails(tvShowId: Int, token: String): Pair<String, TVShow>? = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$BASE_URL/tv/$tvShowId")
            .addHeader("accept", "application/json")
            .addHeader("Authorization", "Bearer $token")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw Exception("Failed to fetch data: ${response.code}")
                }

                val responseBody = response.body?.string()
                    ?: throw Exception("Response body is null")

                val json = JSONObject(responseBody)

                // Call getGenreList to process the genres
                val genres = getGenreList(json)

                // Create and return the TVShow object
                val tvShow = TVShow(
                    id = json.getInt("id"),
                    name = json.optString("name", "Unknown"),
                    poster_path = json.optString("poster_path", ""),
                    backdrop_path = json.optString("backdrop_path", ""),
                    overview = json.optString("overview", "No overview available"),
                    first_air_date = json.optString("first_air_date", ""),
                    genreIds = emptyList(),  // You can add more logic to populate genreIds if needed
                )

                // Return the TV show details in a map
                return@use (genres to tvShow)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}
