package com.josephizang.shoplistguvna.data

import com.josephizang.shoplistguvna.data.local.ShoppingDao
import com.josephizang.shoplistguvna.data.local.ShoppingItem
import com.josephizang.shoplistguvna.data.local.ShoppingList
import kotlinx.coroutines.flow.Flow

class ShoppingRepository(private val shoppingDao: ShoppingDao) {

    val last10Lists: Flow<List<ShoppingList>> = shoppingDao.getLast10Lists()
    val allLists: Flow<List<ShoppingList>> = shoppingDao.getAllLists()

    fun getItemsForList(listId: Long): Flow<List<ShoppingItem>> = shoppingDao.getItemsForList(listId)

    suspend fun getListById(id: Long): ShoppingList? = shoppingDao.getListById(id)

    suspend fun createList(name: String): Long {
        return shoppingDao.insertList(ShoppingList(name = name))
    }

    suspend fun addItem(item: ShoppingItem) {
        shoppingDao.insertItemAndUpdateList(item)
    }

    suspend fun updateItem(item: ShoppingItem) {
        shoppingDao.updateItemAndUpdateList(item)
    }
    
    suspend fun deleteItem(item: ShoppingItem) {
        shoppingDao.deleteItem(item)
        shoppingDao.calculateAndUpdateListTotal(item.listId)
    }

    suspend fun deleteList(list: ShoppingList) {
        shoppingDao.deleteList(list)
    }
}
