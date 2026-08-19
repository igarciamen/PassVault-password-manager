package com.passvault.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passvault.app.domain.PasswordEntry
import com.passvault.app.domain.PasswordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PasswordListViewModel @Inject constructor(
    private val repository: PasswordRepository
) : ViewModel() {

    private val allPasswords: StateFlow<List<PasswordEntry>> = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<String>> = repository.getCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _showOnlyFavorites = MutableStateFlow(false)
    val showOnlyFavorites: StateFlow<Boolean> = _showOnlyFavorites.asStateFlow()

    val passwords: StateFlow<List<PasswordEntry>> = combine(
        allPasswords, _selectedCategory, _showOnlyFavorites
    ) { all, category, onlyFavorites ->
        all
            .filter { category == null || it.category == category }
            .filter { !onlyFavorites || it.isFavorite }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun toggleShowOnlyFavorites() {
        _showOnlyFavorites.value = !_showOnlyFavorites.value
    }

    fun toggleFavorite(entry: PasswordEntry) {
        viewModelScope.launch {
            repository.save(entry.copy(isFavorite = !entry.isFavorite))
        }
    }

    fun delete(entry: PasswordEntry) {
        viewModelScope.launch {
            repository.delete(entry)
        }
    }
}