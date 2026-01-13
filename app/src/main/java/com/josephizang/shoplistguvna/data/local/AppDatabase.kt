package com.josephizang.shoplistguvna.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [ShoppingList::class, ShoppingItem::class], version = 4, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shoppingDao(): ShoppingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add totalItems column
                db.execSQL("ALTER TABLE shopping_lists ADD COLUMN totalItems INTEGER NOT NULL DEFAULT 0")
                // Add index to shopping_items
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_shopping_items_listId` ON `shopping_items` (`listId`)")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add isArchived column
                db.execSQL("ALTER TABLE shopping_lists ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Add totalBought column (default 0.0)
                db.execSQL("ALTER TABLE shopping_lists ADD COLUMN totalBought REAL NOT NULL DEFAULT 0.0")

                // 2. Populate totalBought based on existing checked items
                //    We calculate the sum of (quantity * pricePerUnit) for all items where isChecked = 1 (true)
                //    and update the corresponding list in shopping_lists.
                //    We use a nested SELECT to do this efficiently.
                
                db.execSQL("""
                    UPDATE shopping_lists 
                    SET totalBought = (
                        SELECT COALESCE(SUM(quantity * pricePerUnit), 0.0)
                        FROM shopping_items
                        WHERE shopping_items.listId = shopping_lists.id
                        AND shopping_items.isChecked = 1
                    )
                """)
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "shoplist_guvna_db"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
