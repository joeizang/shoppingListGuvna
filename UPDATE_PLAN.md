# Plan: ShopList Guvna - "Remaining Budget" Update

## Objective
Update the application to track the "Remaining Total" for shopping lists. When items are checked off (bought), they should be "removed" from the pending total, allowing the user to see how much money is still needed vs. how much was the original estimated total.

**Constraint:** ABSOLUTE DATA PRESERVATION. No existing lists or items will be lost.

## 1. Database Schema Migration (Safe Update)
We will safely upgrade the database from **Version 3** to **Version 4**.

*   **Change:** Add a new column `totalBought` (Double) to the `shopping_lists` table.
*   **Why `totalBought`?** Storing the "Start Amount" (`totalEstimated`) and the "Spent Amount" (`totalBought`) gives us maximum flexibility. We can easily calculate "Remaining" as `totalEstimated - totalBought`.
*   **Migration Strategy by `MIGRATION_3_4`:**
    1.  `ALTER TABLE shopping_lists ADD COLUMN totalBought REAL NOT NULL DEFAULT 0.0`
    2.  `UPDATE shopping_lists SET totalBought = (SELECT COALESCE(SUM(quantity * pricePerUnit), 0) FROM shopping_items WHERE shopping_items.listId = shopping_lists.id AND shopping_items.isChecked = 1)`
    *   *Result:* All your existing lists on the phone will instantly have the correct "Spent" and "Remaining" calculations applied to them.

## 2. Code Updates

### A. Data Layer (`ShoppingEntities.kt` & `ShoppingDao.kt`)
1.  **Entity**: Update `ShoppingList` data class to include `val totalBought: Double = 0.0`.
2.  **DAO Logic**: Update the `calculateAndUpdateListTotal` function.
    *   *Current:* Calculates Sum of all items -> `totalEstimated`.
    *   *New:* Will calculate:
        *   Sum of *all* items -> `totalEstimated`
        *   Sum of *checked* items -> `totalBought`
    *   Then updates the specific list record with both values.

### B. UI Layer (`HomeScreen.kt`)
1.  Update `ShoppingListCard`.
2.  **Visual Change:**
    *   **Original Total**: Still visible (maybe slightly smaller or labelled "Est").
    *   **New "Remaining" Total**: Prominently displayed.
    *   *Logic:* `val remaining = list.totalEstimated - list.totalBought`
    *   *Example:* If List is 5,000 NGN and you check off items worth 2,000 NGN, it will show **Pending: 3,000 NGN**.

## 3. Execution Plan
1.  **Modify Entity**: Add field to `ShoppingEntities.kt`.
2.  **Write Migration**: Add `MIGRATION_3_4` in `AppDatabase.kt` and bump version.
3.  **Update Logic**: Modify `ShoppingDao.kt` SQL queries and helper functions.
4.  **Update UI**: Modify `HomeScreen.kt` to show the new "Remaining" calculation.
5.  **Verify**: Build and run. The migration will run automatically on the next launch, preserving all data.
