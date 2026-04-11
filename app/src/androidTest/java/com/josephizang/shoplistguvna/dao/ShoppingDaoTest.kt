package com.josephizang.shoplistguvna.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.josephizang.shoplistguvna.data.local.AppDatabase
import com.josephizang.shoplistguvna.data.local.ShoppingItem
import com.josephizang.shoplistguvna.data.local.ShoppingList
import com.josephizang.shoplistguvna.data.local.ShoppingDao
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShoppingDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: ShoppingDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.shoppingDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    // --- Duplication tests ---

    @Test
    fun duplicateList_copiesAllItems() = runTest {
        val listId = dao.insertList(ShoppingList(name = "Groceries"))
        dao.insertItem(ShoppingItem(listId = listId, name = "Apple", quantity = 2, pricePerUnit = 100.0))
        dao.insertItem(ShoppingItem(listId = listId, name = "Bread", quantity = 1, pricePerUnit = 200.0))

        val newId = dao.duplicateList(listId, "Groceries (copy)")

        val copiedItems = dao.getItemsForListOnce(newId)
        assertThat(copiedItems).hasSize(2)
        assertThat(copiedItems.map { it.name }).containsExactlyElementsIn(listOf("Apple", "Bread"))
    }

    @Test
    fun duplicateList_resetsCheckedState() = runTest {
        val listId = dao.insertList(ShoppingList(name = "Groceries"))
        dao.insertItem(ShoppingItem(listId = listId, name = "Milk", quantity = 1, pricePerUnit = 150.0, isChecked = true))
        dao.insertItem(ShoppingItem(listId = listId, name = "Eggs", quantity = 6, pricePerUnit = 50.0, isChecked = true))

        val newId = dao.duplicateList(listId, "Groceries (copy)")

        val copiedItems = dao.getItemsForListOnce(newId)
        assertThat(copiedItems.none { it.isChecked }).isTrue()
    }

    @Test
    fun duplicateList_createsActiveList() = runTest {
        val listId = dao.insertList(ShoppingList(name = "Archived Source", isArchived = true))
        dao.insertItem(ShoppingItem(listId = listId, name = "Item", quantity = 1, pricePerUnit = 50.0))

        val newId = dao.duplicateList(listId, "Archived Source (copy)")

        val newList = dao.getListById(newId)
        assertThat(newList?.isArchived).isFalse()
    }

    @Test
    fun duplicateList_recalculatesTotalsCorrectly() = runTest {
        val listId = dao.insertList(ShoppingList(name = "Source"))
        dao.insertItem(ShoppingItem(listId = listId, name = "Item A", quantity = 2, pricePerUnit = 100.0))
        dao.insertItem(ShoppingItem(listId = listId, name = "Item B", quantity = 1, pricePerUnit = 300.0))
        dao.calculateAndUpdateListTotal(listId)

        val newId = dao.duplicateList(listId, "Source (copy)")
        val newList = dao.getListById(newId)

        assertThat(newList?.totalEstimated).isEqualTo(500.0)
        assertThat(newList?.totalItems).isEqualTo(2)
        assertThat(newList?.totalBought).isEqualTo(0.0)
    }

    @Test
    fun duplicateList_emptySource_createsEmptyList() = runTest {
        val listId = dao.insertList(ShoppingList(name = "Empty"))

        val newId = dao.duplicateList(listId, "Empty (copy)")

        val copiedItems = dao.getItemsForListOnce(newId)
        assertThat(copiedItems).isEmpty()
        val newList = dao.getListById(newId)
        assertThat(newList?.totalItems).isEqualTo(0)
    }

    @Test
    fun duplicateList_copiedItemsHaveNewListId() = runTest {
        val listId = dao.insertList(ShoppingList(name = "Source"))
        dao.insertItem(ShoppingItem(listId = listId, name = "Widget", quantity = 1, pricePerUnit = 99.0))

        val newId = dao.duplicateList(listId, "Source (copy)")

        val copiedItems = dao.getItemsForListOnce(newId)
        assertThat(copiedItems.all { it.listId == newId }).isTrue()
        assertThat(copiedItems.all { it.id != 0L }).isTrue()
    }

    // --- Item editing tests ---

    @Test
    fun updateItemAndUpdateList_preservesCheckedState() = runTest {
        val listId = dao.insertList(ShoppingList(name = "Test"))
        dao.insertItem(ShoppingItem(listId = listId, name = "Milk", quantity = 1, pricePerUnit = 100.0, isChecked = true))
        val item = dao.getItemsForListOnce(listId).first()

        dao.updateItemAndUpdateList(item.copy(name = "Full Cream Milk", quantity = 2))

        val updated = dao.getItemsForListOnce(listId).first()
        assertThat(updated.name).isEqualTo("Full Cream Milk")
        assertThat(updated.quantity).isEqualTo(2)
        assertThat(updated.isChecked).isTrue()
    }

    @Test
    fun updateItemAndUpdateList_recalculatesListTotal() = runTest {
        val listId = dao.insertList(ShoppingList(name = "Test"))
        dao.insertItem(ShoppingItem(listId = listId, name = "Tea", quantity = 1, pricePerUnit = 200.0))
        dao.calculateAndUpdateListTotal(listId)
        val item = dao.getItemsForListOnce(listId).first()

        dao.updateItemAndUpdateList(item.copy(quantity = 3, pricePerUnit = 200.0))

        val list = dao.getListById(listId)
        assertThat(list?.totalEstimated).isEqualTo(600.0)
    }

    // --- Total calculation tests ---

    @Test
    fun calculateAndUpdateListTotal_sumsAllItems() = runTest {
        val listId = dao.insertList(ShoppingList(name = "Totals Test"))
        dao.insertItem(ShoppingItem(listId = listId, name = "A", quantity = 2, pricePerUnit = 50.0))
        dao.insertItem(ShoppingItem(listId = listId, name = "B", quantity = 1, pricePerUnit = 200.0))

        dao.calculateAndUpdateListTotal(listId)

        val list = dao.getListById(listId)
        assertThat(list?.totalEstimated).isEqualTo(300.0)
        assertThat(list?.totalItems).isEqualTo(2)
    }

    @Test
    fun calculateAndUpdateListTotal_onlyCountsCheckedItemsForBought() = runTest {
        val listId = dao.insertList(ShoppingList(name = "Bought Test"))
        dao.insertItem(ShoppingItem(listId = listId, name = "Checked", quantity = 1, pricePerUnit = 100.0, isChecked = true))
        dao.insertItem(ShoppingItem(listId = listId, name = "Unchecked", quantity = 1, pricePerUnit = 200.0, isChecked = false))

        dao.calculateAndUpdateListTotal(listId)

        val list = dao.getListById(listId)
        assertThat(list?.totalBought).isEqualTo(100.0)
        assertThat(list?.totalEstimated).isEqualTo(300.0)
    }
}
