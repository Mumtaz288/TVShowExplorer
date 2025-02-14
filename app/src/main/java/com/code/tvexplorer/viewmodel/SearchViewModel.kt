package com.code.tvexplorer.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.code.tvexplorer.models.TVShow
import com.code.tvexplorer.models.Genre
import com.code.tvexplorer.repository.TMDBRepository
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {

    private val _searchQuery = mutableStateOf("")
    val searchQuery: State<String> = _searchQuery

    private val _searchResults = mutableStateOf<List<TVShow>>(emptyList())
    val searchResults: State<List<TVShow>> = _searchResults

    private val _originalSearchResults = mutableStateOf<List<TVShow>>(emptyList()) // Store the original results
    val originalSearchResults: State<List<TVShow>> = _originalSearchResults

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _allGenres = mutableStateOf<List<Genre>>(emptyList())
    val allGenres: State<List<Genre>> = _allGenres

    private val _selectedGenres = mutableStateOf<Set<Int>>(emptySet())
    val selectedGenres: State<Set<Int>> = _selectedGenres

    // Update search query
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Load genres
    fun loadGenres(token: String) {
        viewModelScope.launch {
            val genres = TMDBRepository.getTVGenres(token)
            _allGenres.value = genres
        }
    }

    // Toggle genre selection
    fun toggleGenreSelection(genreId: Int) {
        // Toggle selected genre
        val updatedGenres = if (_selectedGenres.value.contains(genreId)) {
            _selectedGenres.value - genreId
        } else {
            _selectedGenres.value + genreId
        }
        _selectedGenres.value = updatedGenres

        // Filter search results based on selected genres
        filterResultsByGenres()
    }

    // Perform search based on query
    fun performSearch(query: String, token: String) {
        if (query.isNotEmpty()) {
            _isLoading.value = true
            viewModelScope.launch {
                val response = TMDBRepository.searchTVShows(query, token)
                _originalSearchResults.value = response.results // Store the original results
                _searchResults.value = response.results // Update search results
                filterResultsByGenres() // Apply genre filter after search results are updated
                _isLoading.value = false
            }
        }
    }

    // Filter the search results by selected genres
    private fun filterResultsByGenres() {
        val selected = _selectedGenres.value

        // If no genres are selected, show all results
        if (selected.isEmpty()) {
            _searchResults.value = _originalSearchResults.value // Reset to original results
            return
        }

        // Filter based on genreIds in the TV show data
        _searchResults.value = _originalSearchResults.value.filter { tvShow ->
            tvShow.genreIds.any { it in selected }
        }
    }
}

