package com.josephizang.shoplistguvna.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.josephizang.shoplistguvna.data.ShoppingRepository
import com.josephizang.shoplistguvna.data.local.ShoppingItem
import com.josephizang.shoplistguvna.data.local.ShoppingList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ListDetailViewModel(
    private val repository: ShoppingRepository,
    private val listId: Long
) : ViewModel() {

    private val _currentList = MutableStateFlow<ShoppingList?>(null)
    val currentList: StateFlow<ShoppingList?> = _currentList.asStateFlow()

    val items: StateFlow<List<ShoppingItem>> = repository.getItemsForList(listId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadListMetadata()
    }

    private fun loadListMetadata() {
        viewModelScope.launch {
            _currentList.value = repository.getListById(listId)
        }
    }

    fun addItem(name: String, quantity: Int, price: Double) {
        viewModelScope.launch {
            val item = ShoppingItem(
                listId = listId,
                name = name,
                quantity = quantity,
                pricePerUnit = price
            )
            repository.addItem(item)
            loadListMetadata() // Refresh total
        }
    }

    fun toggleItemChecked(item: ShoppingItem) {
        viewModelScope.launch {
            repository.updateItem(item.copy(isChecked = !item.isChecked))
        }
    }
    
    fun deleteItem(item: ShoppingItem) {
        viewModelScope.launch {
            repository.deleteItem(item)
            loadListMetadata()
        }
    }
}

class ListDetailViewModelFactory(
    private val repository: ShoppingRepository,
    private val listId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ListDetailViewModel(repository, listId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
