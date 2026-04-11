# Revised Implementation Plan: List Duplication, Item Editing, and E2E Coverage

## Status

This is the implementation-ready revision of the original plan.

The main correction is structural:

- **Feature work is ready to implement now**
- **Instrumentation/E2E coverage requires a testability refactor first**

The previous version treated the E2E work as mostly additive. That was not accurate for the current app. The app currently creates its database and preferences repositories inside `MainActivity`, owns biometric prompt logic inside `BiometricAuthScreen`, and hardcodes splash timing inside `SplashScreen`. Reliable instrumentation tests need those concerns to become injectable.

---

## Scope

This document covers three workstreams:

1. **Feature 1: Duplicate an existing shopping list**
2. **Feature 2: Edit individual items in a list**
3. **Instrumentation/E2E test coverage for critical user flows**

It does **not** promise literal "full coverage across all app features" in the first pass. The first pass should focus on:

- app launch and navigation
- create list
- add item / edit item / delete item
- archive / unarchive
- duplicate list
- settings persistence that affects visible UI behavior

---

## Current-State Constraints

These constraints are based on the codebase as it exists today:

- `ShoppingDao` already owns total recalculation helpers.
- `ListDetailViewModel` already refreshes list metadata after item mutations.
- `AddItemSheet` exists but needs cleanup before reuse as a shared add/edit form.
- There is no custom `Application` class or dependency container yet.
- `MainActivity` currently builds `AppDatabase`, `ShoppingRepository`, and `UserPreferencesRepository` directly.
- `SplashScreen` owns its delay internally.
- `BiometricAuthScreen` owns biometric prompt behavior internally.

Because of that, the implementation should be split into **features first** and **testability refactor second**.

---

# Feature 1: Duplicate an Existing List

## Outcome

Users can duplicate a non-empty shopping list from the list card overflow menu. The duplicated list:

- gets a derived name such as `"Groceries (copy)"`
- is always created as an active list
- copies all items into the new list
- resets all copied items to `isChecked = false`
- recalculates list totals from the copied items

## Product Rules

- Empty lists cannot be duplicated.
- Duplicate should be available from both active and archived list cards.
- Duplicating an archived list creates a new **active** list.
- Duplicate naming collisions are acceptable in the first pass. If `"Groceries (copy)"` already exists, create another list with the same name rather than blocking the action.

## Data Layer — `ShoppingDao.kt`

Add:

```kotlin
@Query("SELECT * FROM shopping_items WHERE listId = :listId")
suspend fun getItemsForListOnce(listId: Long): List<ShoppingItem>
```

```kotlin
@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun insertItems(items: List<ShoppingItem>)
```

```kotlin
@Transaction
suspend fun duplicateList(sourceListId: Long, newListName: String): Long {
    val newListId = insertList(
        ShoppingList(
            name = newListName,
            isArchived = false
        )
    )

    val copiedItems = getItemsForListOnce(sourceListId).map { item ->
        item.copy(
            id = 0,
            listId = newListId,
            isChecked = false
        )
    }

    if (copiedItems.isNotEmpty()) {
        insertItems(copiedItems)
    }

    calculateAndUpdateListTotal(newListId)
    return newListId
}
```

Notes:

- No schema change is required.
- Guard `insertItems()` behind `isNotEmpty()` to avoid depending on Room behavior for empty bulk inserts.
- The DAO transaction remains the right place for this work because list creation, item copy, and total recomputation should succeed or fail together.

## Repository — `ShoppingRepository.kt`

Add:

```kotlin
suspend fun duplicateList(sourceListId: Long, newListName: String): Long {
    return shoppingDao.duplicateList(sourceListId, newListName)
}
```

No extra logic is required here.

## ViewModel — `HomeViewModel.kt`

Add:

```kotlin
fun duplicateList(list: ShoppingList) {
    if (list.totalItems == 0) return

    viewModelScope.launch {
        repository.duplicateList(list.id, "${list.name} (copy)")
    }
}
```

Optional hardening for a later pass:

- add a per-list in-flight guard if double-tap duplication becomes a real issue

For the first pass, closing the menu on click plus the empty-list guard is sufficient.

## UI — `HomeScreen.kt` and `ArchivedListsScreen.kt`

Update `ShoppingListCard` to support an overflow menu:

- add `onDuplicate: () -> Unit`
- add `onDelete: () -> Unit`
- add local `menuExpanded` state
- add `MoreVert` icon button
- show `DropdownMenu` with:
  - `Duplicate`, enabled only when `list.totalItems > 0`
  - `Delete`

Wire both callbacks from:

- `HomeScreen`
- `ArchivedListsScreen`

## Delete Behavior

The current app already supports deleting a list at the data/viewmodel level, but the card UI does not expose it.

For this iteration:

- show `Delete` in the overflow menu
- allow delete without confirmation to stay aligned with current app simplicity

If product safety becomes a concern, a confirmation dialog can be added later. It is **not** a blocker for this feature.

## Edge Cases

| Case | Handling |
|---|---|
| Empty source list | Disable duplicate in UI and return early in `HomeViewModel` |
| Archived source list | New copy is created with `isArchived = false` |
| Source items include checked items | Copied items are always reset to unchecked |
| Copy transaction fails midway | `@Transaction` rolls back the new list row |
| Existing list already named `"X (copy)"` | Create another list with the same derived name |

## Acceptance Criteria

- Overflow menu appears on list cards in Home and History.
- Duplicate is disabled for empty lists.
- Duplicating a populated list creates a new card on Home.
- Opening the duplicated list shows the same item names, quantities, and prices.
- All copied items are unchecked.
- The duplicated list totals match the copied item values.

---

# Feature 2: Edit Individual Items

## Outcome

Users can edit an existing item from the list detail screen without losing its checked state or breaking list totals.

## Product Rules

- Editing should reuse the same basic form structure as adding an item.
- `name` must not be blank.
- `quantity` must be a valid integer greater than or equal to `1`.
- `price` may remain `0.0`, but it must parse as a decimal input.
- `isChecked` must be preserved during edit.

## Important Baseline Fix

Before generalizing the add form into a shared add/edit sheet, clean up the current form behavior:

- the add sheet button should render visible button text
- quantity and price validation should be explicit, not silent coercion

This is part of Feature 2, not optional polish.

## ViewModel — `ListDetailViewModel.kt`

Add:

```kotlin
fun updateItem(item: ShoppingItem, newName: String, newQuantity: Int, newPricePerUnit: Double) {
    viewModelScope.launch {
        repository.updateItem(
            item.copy(
                name = newName,
                quantity = newQuantity,
                pricePerUnit = newPricePerUnit
            )
        )
        loadListMetadata()
    }
}
```

This fits the existing architecture because:

- repository update already recalculates totals atomically
- the full `ShoppingItem` is already available in the UI
- `listId` and `isChecked` are preserved through `copy()`

## UI — `DetailScreen.kt`

Replace `AddItemSheet` with a reusable `ItemFormSheet`.

Suggested shape:

```kotlin
@Composable
fun ItemFormSheet(
    title: String,
    actionLabel: String,
    initialName: String = "",
    initialQuantity: String = "1",
    initialPrice: String = "",
    onDismiss: () -> Unit,
    onSubmit: (String, Int, Double) -> Unit
)
```

This shape is preferable to separate nullable `onAdd` and `onUpdate` callbacks because it keeps the sheet focused on form behavior, not feature mode branching.

### Form Behavior

- use `remember(initialName, initialQuantity, initialPrice)` for field reset
- show button text: `"Add"` or `"Save"`
- disable submit when:
  - `name.isBlank()`
  - `quantity.toIntOrNull()` is `null` or `< 1`
  - `price.toDoubleOrNull()` is `null`
- surface invalid state in the relevant `TextField`

### `ShoppingItemRow`

Add:

- `onEdit: () -> Unit`
- edit icon button beside delete

Layout note:

- the current row is tight at `56.dp`
- if edit + delete feel cramped, increase row height rather than forcing icon overlap

### `ListDetailScreen`

Add local state:

```kotlin
var itemToEdit by remember { mutableStateOf<ShoppingItem?>(null) }
```

Behavior:

- tapping the FAB opens add mode
- tapping edit opens edit mode prefilled from the selected item
- save calls `viewModel.updateItem(...)`
- cancel dismisses without mutation

## Edge Cases

| Case | Handling |
|---|---|
| Blank item name | Disable submit |
| Invalid quantity | Disable submit and mark field invalid |
| Invalid price | Disable submit and mark field invalid |
| Checked item edited | Preserve `isChecked` from original item |
| Edit dismissed | No mutation occurs |
| Item edited after archive/unarchive flow | Repository update behavior remains unchanged |

## Acceptance Criteria

- Each item row exposes an edit action.
- Edit sheet opens with current item values populated.
- Saving changes updates the visible row.
- Checked items remain checked after edit.
- List total and spent total refresh correctly after edit.
- Canceling edit leaves the item unchanged.

---

# Testability Refactor Required Before E2E

## Why This Is Required

Reliable instrumentation tests are not practical until app dependencies stop being created directly inside UI entry points.

Today:

- `MainActivity` builds the database and repositories directly.
- splash timing is hardcoded in the composable
- biometric auth is hardcoded in the composable
- DataStore is hidden behind a top-level property delegate

Before writing broad E2E coverage, move those into an injectable app-level dependency object.

## Required Refactor

Create:

- `ShopListGuvnaApplication`
- `AppContainer` or `AppDependencies`

The container should own:

- database factory or singleton database instance
- `ShoppingRepository`
- `UserPreferencesRepository`
- splash delay value
- biometric auth handler

### Biometric Seam

Extract:

```kotlin
interface BiometricAuthHandler {
    fun authenticate(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    )

    fun canAuthenticate(context: Context): Boolean
}
```

Reason:

- tests need to bypass or simulate biometric outcomes
- `BiometricAuthScreen` should orchestrate UI state, not build prompt internals

### Splash Seam

Replace hardcoded splash delay with:

- `appContainer.splashDelayMs`

### Database Seam

Move database creation behind:

- `appContainer.databaseProvider`

For tests, use in-memory Room.

### Preferences/DataStore Seam

Refactor `UserPreferencesRepository` so it depends on an injected `DataStore<Preferences>` rather than a `Context`.

That allows test code to provide an isolated store instance.

## Deliverables

- custom `Application` class registered in manifest
- activity reads dependencies from application container
- splash screen receives delay as input
- biometric screen receives handler or uses container-provided handler
- preferences repository can be instantiated with a supplied `DataStore`

This refactor is the real gate to instrumentation stability.

---

# Instrumentation / E2E Plan

## Goal

Cover the critical user journeys with a maintainable first-pass test suite.

## Non-Goal

Do not try to prove every visual detail or every edge case through instrumentation alone. Use a layered approach:

- DAO/repository tests for data correctness
- ViewModel tests for mutation logic where practical
- instrumentation tests for navigation and user-visible flow

## Test Dependencies

Add instrumentation dependencies through `libs.versions.toml` and `app/build.gradle.kts`:

- `androidx.test:core-ktx`
- `androidx.test:runner`
- `androidx.test:rules`
- `androidx.navigation:navigation-testing`
- `androidx.arch.core:core-testing`
- `org.jetbrains.kotlinx:kotlinx-coroutines-test`
- `androidx.room:room-testing`
- `com.google.truth:truth`

Keep versions aligned with the app's existing Compose and AndroidX stack.

## Instrumentation Infrastructure

Create:

- `TestRunner`
- `TestApplication`
- `BaseE2ETest`
- `ComposeTestExtensions`
- `TestDataBuilders`

`TestApplication` should override:

- biometric handler
- splash delay
- database provider
- DataStore provider

`BaseE2ETest` should:

- expose the compose rule
- clear DB state before each test
- reset or isolate preference state before each test

## Semantics Tags

Add stable `testTag`s only where they improve test robustness. Do not tag every composable by default.

Recommended required tags:

- `splash_screen`
- `auth_screen`
- `home_screen`
- `history_screen`
- `settings_screen`
- `list_detail_screen`
- `create_list_dialog`
- `list_name_input`
- `item_form_sheet`
- `item_name_input`
- `item_quantity_input`
- `item_price_input`
- `dark_mode_toggle`
- `show_totals_toggle`
- `empty_home_state`

Prefer content descriptions and visible text where those are already stable and user-facing.

## First-Pass Instrumentation Coverage

### Smoke / Navigation

- splash appears
- splash advances to auth or home based on configured test handler
- auth success reaches home
- bottom nav switches among Home, History, and Settings
- navigating into a list and back works

### Core CRUD

- create list from Home
- open list detail
- add item
- edit item
- delete item
- archive list
- unarchive list

### New Feature Coverage

- duplicate option is visible for populated list
- duplicate is disabled for empty list
- duplicating a populated list creates an active copy
- duplicated items are copied and reset unchecked

### Settings Coverage

- totals visibility toggle affects list cards
- dark mode toggle persists state at least across navigation or activity recreation if practical

## Tests Better Suited Below E2E

Prefer DAO/repository or ViewModel-level tests for:

- duplicate transaction correctness
- copied item unchecked reset
- totals recalculation logic
- edit preserving `isChecked`

These are high-signal data behaviors and cheaper to verify below instrumentation level.

---

# Recommended Delivery Order

## Phase 1: Feature Delivery

1. Implement list duplication in DAO, repository, viewmodel, and card UI.
2. Implement item editing and clean up the shared item form.
3. Manually verify both features in the running app.

## Phase 2: Testability Refactor

1. Introduce `ShopListGuvnaApplication`.
2. Add `AppContainer` / `AppDependencies`.
3. Move database, preferences, splash delay, and biometric behavior behind the container.
4. Update manifest and activity wiring.

## Phase 3: Low-Level Tests

1. Add DAO/repository tests for duplication behavior.
2. Add ViewModel or repository-focused tests for item editing behavior if useful.

## Phase 4: Instrumentation Foundation

1. Add runner, test application, builders, and helpers.
2. Add stable test tags.
3. Prove the harness with smoke tests.

## Phase 5: Instrumentation Coverage

1. Add navigation tests.
2. Add create/add/edit/delete/archive/unarchive tests.
3. Add duplicate-list tests.
4. Add settings tests.

---

# Readiness Verdict

This revised plan is ready for implementation.

That readiness depends on following the split described above:

- **Features can be implemented immediately**
- **Instrumentation coverage starts only after the container/testability refactor**

If implementation begins in that order, the plan matches the current codebase and is realistic in scope.
