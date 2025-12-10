package com.josephizang.shoplistguvna.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.josephizang.shoplistguvna.data.ShoppingRepository
import com.josephizang.shoplistguvna.data.local.ShoppingList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: ShoppingRepository) : ViewModel() {

    val activeLists: StateFlow<List<ShoppingList>> = repository.activeLists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedLists: StateFlow<List<ShoppingList>> = repository.archivedLists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun unarchiveList(listId: Long) {
        viewModelScope.launch {
            repository.setListArchived(listId, false)
        }
    }

    fun createList(name: String) {
        viewModelScope.launch {
            repository.createList(name)
        }
    }
    
    fun deleteList(list: ShoppingList) {
        viewModelScope.launch { 
            repository.deleteList(list)
        }
    }
}

class HomeViewModelFactory(private val repository: ShoppingRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
