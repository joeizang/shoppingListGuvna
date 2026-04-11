package com.josephizang.shoplistguvna.test

import com.josephizang.shoplistguvna.data.local.ShoppingItem
import com.josephizang.shoplistguvna.data.local.ShoppingList

object TestDataBuilders {

    fun aList(
        name: String = "Test List",
        isArchived: Boolean = false
    ) = ShoppingList(name = name, isArchived = isArchived)

    fun anItem(
        listId: Long,
        name: String = "Test Item",
        quantity: Int = 1,
        pricePerUnit: Double = 100.0,
        isChecked: Boolean = false
    ) = ShoppingItem(
        listId = listId,
        name = name,
        quantity = quantity,
        pricePerUnit = pricePerUnit,
        isChecked = isChecked
    )

    /** Inserts a list and items via the DAO and returns the list ID. */
    suspend fun insertListWithItems(
        app: TestApplication,
        listName: String = "Test List",
        items: List<Pair<String, Double>> = listOf("Item A" to 100.0, "Item B" to 200.0)
    ): Long {
        val dao = app.container.database.shoppingDao()
        val listId = dao.insertList(aList(name = listName))
        items.forEach { (name, price) ->
            dao.insertItem(anItem(listId = listId, name = name, pricePerUnit = price))
        }
        dao.calculateAndUpdateListTotal(listId)
        return listId
    }
}
