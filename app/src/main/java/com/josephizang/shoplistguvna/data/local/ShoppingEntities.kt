package com.josephizang.shoplistguvna.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "shopping_lists")
data class ShoppingList(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Date = Date(),
    val totalEstimated: Double = 0.0, // Cached total for summary views
    val totalItems: Int = 0 // Cached item count
)

@Entity(
    tableName = "shopping_items",
    foreignKeys = [
        ForeignKey(
            entity = ShoppingList::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [androidx.room.Index("listId")]
)
data class ShoppingItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val listId: Long,
    val name: String,
    val quantity: Int,
    val pricePerUnit: Double,
    val isChecked: Boolean = false
) {
    val totalPrice: Double
        get() = quantity * pricePerUnit
}
