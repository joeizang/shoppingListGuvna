package com.josephizang.shoplistguvna.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingDao {

    // --- Lists ---
    @Query("SELECT * FROM shopping_lists WHERE isArchived = 0 ORDER BY createdAt DESC LIMIT 10")
    fun getLast10ActiveLists(): Flow<List<ShoppingList>>

    @Query("SELECT * FROM shopping_lists WHERE isArchived = 0 ORDER BY createdAt DESC")
    fun getActiveLists(): Flow<List<ShoppingList>>

    @Query("SELECT * FROM shopping_lists WHERE isArchived = 1 ORDER BY createdAt DESC")
    fun getArchivedLists(): Flow<List<ShoppingList>>

    @Query("UPDATE shopping_lists SET isArchived = :isArchived WHERE id = :listId")
    suspend fun updateListArchivedStatus(listId: Long, isArchived: Boolean)

    @Query("SELECT * FROM shopping_lists WHERE id = :id")
    suspend fun getListById(id: Long): ShoppingList?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: ShoppingList): Long

    @Update
    suspend fun updateList(list: ShoppingList)

    @Delete
    suspend fun deleteList(list: ShoppingList)

    // --- Items ---
    @Query("SELECT * FROM shopping_items WHERE listId = :listId")
    fun getItemsForList(listId: Long): Flow<List<ShoppingItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ShoppingItem)

    @Update
    suspend fun updateItem(item: ShoppingItem)

    @Delete
    suspend fun deleteItem(item: ShoppingItem)

    // Helper to update list total when items change
    @Transaction
    suspend fun insertItemAndUpdateList(item: ShoppingItem) {
        insertItem(item)
        calculateAndUpdateListTotal(item.listId)
    }

    @Transaction
    suspend fun updateItemAndUpdateList(item: ShoppingItem) {
        updateItem(item)
        calculateAndUpdateListTotal(item.listId)
    }
    
    @Query("SELECT SUM(quantity * pricePerUnit) FROM shopping_items WHERE listId = :listId")
    suspend fun getListTotal(listId: Long): Double?

    @Query("SELECT COUNT(*) FROM shopping_items WHERE listId = :listId")
    suspend fun getListItemCount(listId: Long): Int

    @Query("SELECT SUM(quantity * pricePerUnit) FROM shopping_items WHERE listId = :listId AND isChecked = 1")
    suspend fun getListBoughtTotal(listId: Long): Double?

    @Query("UPDATE shopping_lists SET totalEstimated = :total, totalBought = :bought, totalItems = :count WHERE id = :listId")
    suspend fun updateListTotalAndCount(listId: Long, total: Double, bought: Double, count: Int)

    suspend fun calculateAndUpdateListTotal(listId: Long) {
        val total = getListTotal(listId) ?: 0.0
        val bought = getListBoughtTotal(listId) ?: 0.0
        val count = getListItemCount(listId)
        updateListTotalAndCount(listId, total, bought, count)
    }
}
