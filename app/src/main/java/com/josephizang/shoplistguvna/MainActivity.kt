package com.josephizang.shoplistguvna

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.josephizang.shoplistguvna.data.ShoppingRepository
import com.josephizang.shoplistguvna.data.local.AppDatabase
import com.josephizang.shoplistguvna.presentation.HomeScreen
import com.josephizang.shoplistguvna.presentation.HomeViewModel
import com.josephizang.shoplistguvna.presentation.HomeViewModelFactory
import com.josephizang.shoplistguvna.presentation.ListDetailScreen
import com.josephizang.shoplistguvna.presentation.ListDetailViewModel
import com.josephizang.shoplistguvna.presentation.ListDetailViewModelFactory
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import com.josephizang.shoplistguvna.presentation.ArchivedListsScreen
import com.josephizang.shoplistguvna.presentation.SplashScreen
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.currentBackStackEntryAsState
import com.josephizang.shoplistguvna.data.UserPreferencesRepository
import com.josephizang.shoplistguvna.presentation.SettingsScreen
import kotlinx.coroutines.launch
import com.josephizang.shoplistguvna.ui.theme.ShopListGuvnaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(this)
        val repository = ShoppingRepository(database.shoppingDao())
        val userPreferencesRepository = UserPreferencesRepository(this)

        setContent {
            val isDarkMode by userPreferencesRepository.isDarkMode.collectAsState(initial = true)
            val isTotalsVisible by userPreferencesRepository.isTotalsVisible.collectAsState(initial = true)

            ShopListGuvnaTheme(darkTheme = isDarkMode) {
                val navController = rememberNavController()
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStackEntry?.destination?.route
                val scope = rememberCoroutineScope()

                Scaffold(
                    bottomBar = {
                        // Only show bottom bar on top-level screens
                        if (currentRoute == "home" || currentRoute == "settings" || currentRoute == "history") {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                            ) {
                                NavigationBarItem(
                                    selected = currentRoute == "home",
                                    onClick = {
                                        navController.navigate("home") {
                                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { 
                                        Icon(
                                            if (currentRoute == "home") Icons.Filled.Home else Icons.Outlined.Home, 
                                            contentDescription = "Home"
                                        ) 
                                    },
                                    label = { Text("Home") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        unselectedIconColor = Color.Gray,
                                        unselectedTextColor = Color.Gray,
                                        indicatorColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.15f)
                                    )
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "history",
                                    onClick = {
                                        navController.navigate("history") {
                                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { 
                                        Icon(
                                            if (currentRoute == "history") Icons.Filled.DateRange else Icons.Outlined.DateRange, 
                                            contentDescription = "History"
                                        ) 
                                    },
                                    label = { Text("History") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        unselectedIconColor = Color.Gray,
                                        unselectedTextColor = Color.Gray,
                                        indicatorColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.15f)
                                    )
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "settings",
                                    onClick = {
                                        navController.navigate("settings") {
                                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { 
                                        Icon(
                                            if (currentRoute == "settings") Icons.Filled.Settings else Icons.Outlined.Settings, 
                                            contentDescription = "Settings"
                                        ) 
                                    },
                                    label = { Text("Settings") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        unselectedIconColor = Color.Gray,
                                        unselectedTextColor = Color.Gray,
                                        indicatorColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.15f)
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "splash",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("splash") {
                            SplashScreen(
                                onNavigateToHome = {
                                    navController.navigate("home") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("home") {
                            val viewModel: HomeViewModel = viewModel(
                                factory = HomeViewModelFactory(repository)
                            )
                            HomeScreen(
                                viewModel = viewModel,
                                isTotalsVisible = isTotalsVisible,
                                onNavigateToList = { listId ->
                                    navController.navigate("list/$listId")
                                }
                            )
                        }

                        composable("history") {
                            val viewModel: HomeViewModel = viewModel(
                                factory = HomeViewModelFactory(repository)
                            )
                            ArchivedListsScreen(
                                viewModel = viewModel,
                                isTotalsVisible = isTotalsVisible,
                                onNavigateToList = { listId ->
                                    navController.navigate("list/$listId")
                                }
                            )
                        }

                        composable("settings") {
                            SettingsScreen(
                                isDarkMode = isDarkMode,
                                onToggleDarkMode = { enabled ->
                                    scope.launch {
                                        userPreferencesRepository.setDarkMode(enabled)
                                    }
                                },
                                isTotalsVisible = isTotalsVisible,
                                onToggleTotalsVisible = { enabled ->
                                    scope.launch {
                                        userPreferencesRepository.setTotalsVisible(enabled)
                                    }
                                }
                            )
                        }

                        composable(
                            route = "list/{listId}",
                            arguments = listOf(navArgument("listId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val listId = backStackEntry.arguments?.getLong("listId") ?: return@composable
                            val viewModel: ListDetailViewModel = viewModel(
                                factory = ListDetailViewModelFactory(repository, listId)
                            )
                            ListDetailScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
