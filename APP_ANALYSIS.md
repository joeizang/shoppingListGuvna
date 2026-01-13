# ShopList Guvna - Application Analysis

## 1. Overview
**ShopList Guvna** is a native Android application built with Kotlin, designed to manage shopping lists with a focus on price estimation and tracking. The application targets Android API level 26 (Oreo) up to 35, ensuring modern feature support while maintaining backward compatibility.

## 2. Architecture
The application follows a modern **MVVM (Model-View-ViewModel)** architecture pattern, leveraging Android Architecture Components.

*   **UI Layer (View):** Built entirely using **Jetpack Compose** (Material3).
    *   `HomeScreen`: Displays active shopping lists in a tile layout.
    *   `DetailScreen`: (Implied) Shows items within a specific list.
    *   `ArchivedListsScreen`: (Implied) Shows lists that have been finished/archived.
*   **Presentation Layer (ViewModel):**
    *   `HomeViewModel`: manages the state for the home screen (active/archived lists, creating lists).
    *   Uses `StateFlow` to expose data reactively to the UI.
*   **Data Layer (Repository & Data Source):**
    *   `ShoppingRepository`: Acts as the single source of truth, mediating between the ViewModel and the Database.
    *   `ShoppingDao`: Defines the database interactions.
    *   **Room Database**: The underlying persistence solution.

## 3. Data Storage & Persistence
The local data is stored in a structured relational database using **SQLite**, abstracted via the **Room Persistence Library**.

### Database Schema
The database (`AppDatabase`) consists of two primary related tables:

#### A. `shopping_lists` (Table)
Stores the header information for each shopping list.
*   **id** (`Long`, Primary Key, Auto-increment): Unique identifier for the list.
*   **name** (`String`): User-defined name of the shopping list.
*   **createdAt** (`Date`): Timestamp of creation.
*   **totalEstimated** (`Double`): Cached sum of all items in the list.
*   **totalItems** (`Int`): Cached count of items in the list.
*   **isArchived** (`Boolean`): Flag to differentiate between active and past lists.

#### B. `shopping_items` (Table)
Stores individual items belonging to a purchase list.
*   **id** (`Long`, Primary Key, Auto-increment): Unique identifier for the item.
*   **listId** (`Long`, Foreign Key): Links the item to a specific `ShoppingList`.
    *   *Constraint:* Cascade Delete (Deleting a list deletes all its items).
*   **name** (`String`): name of the product/item.
*   **quantity** (`Int`): Number of units.
*   **pricePerUnit** (`Double`): Cost per single unit.
*   **isChecked** (`Boolean`): Status of the item (bought vs pending).

### Data Synchronization
*   **Reactive Data:** The DAO returns `Flow<List<T>>`, allowing the UI to verify updates immediately when the database changes.
*   **Automatic calculations:** The `ShoppingDao` contains transaction logic (`insertItemAndUpdateList`, `updateItemAndUpdateList`) to automatically recalculate and update the `totalEstimated` and `totalItems` fields on the `ShoppingList` parent whenever an item is added or modified. This prevents the "n+1" query problem when displaying list summaries.

## 4. Key Files & Structure
*   `data/local/ShoppingEntities.kt`: Defines the data models.
*   `data/local/ShoppingDao.kt`: Contains the SQL queries and transaction logic.
*   `presentation/HomeScreen.kt`: Main UI entry point showing list tiles.
*   `presentation/HomeViewModel.kt`: Logic for fetching lists and creating new ones.

## 5. Current Feature State
*   **List Management:** Users can create new named lists and view them in a grid.
*   **Archiving:** Support for archiving lists (separating active vs. history).
*   **Currency Support:** Formatted for Nigerian Naira (NGN).
*   **Visuals:**
    *   Deterministic color generation for list cards based on their ID.
    *   Privacy/Visibility toggle for total amounts on the home screen.
