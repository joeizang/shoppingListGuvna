# ShopList Guvna

A modern, intuitive shopping list application for Android built with Jetpack Compose.

## Features

-   **Shopping Lists**: Easily create and manage multiple shopping lists.
-   **Item Management**: Add items with quantity and unit price. The app automatically calculates the total price for each item.
-   **Smart Totals**: View the estimated total cost for each shopping list. You can toggle the visibility of these totals in the Settings.
-   **Visuals**:
    -   **Dark Mode**: One-step toggle for Dark Mode, with a high-contrast dark theme by default.
    -   **Brick Red Theme**: A premium "Brick Red" color scheme inspired by modern music apps.
    -   **Dynamic Cards**: List cards are assigned unique pastel accent colors for easy identification.
-   **Settings**: Dedicated settings screen to manage preferences like Dark Mode and Total Visibility.

## Tech Stack

-   **Language**: Kotlin
-   **UI Framework**: Jetpack Compose (Material3)
-   **Navigation**: Jetpack Navigation Compose
-   **Persistence**:
    -   **Room Database**: For storing lists and items locally.
    -   **DataStore Preferences**: For storing user settings (Dark Mode, etc.).
-   **Architecture**: MVVM (Model-View-ViewModel)

## Getting Started

1.  Clone the repository.
2.  Open in Android Studio.
3.  Sync Gradle project.
4.  Run on an emulator or device (Minimum SDK 26).

## License

[Add License Here]
