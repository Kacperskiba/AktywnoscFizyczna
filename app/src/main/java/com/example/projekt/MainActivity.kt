package com.example.projekt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.projekt.ui.screens.*
import com.example.projekt.ui.theme.ActivityJournalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ActivityJournalTheme {
                MainApp()
            }
        }
    }
}

object NavRoutes {
    const val HOME = "home"
    const val ACTIVITY = "activity"
    const val HISTORY = "history"
    const val ACTIVITY_DETAIL = "activity_detail/{activityId}"
    const val PHOTO_GALLERY = "photo_gallery"
    const val SETTINGS = "settings"

    fun activityDetail(activityId: Long) = "activity_detail/$activityId"
}

data class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun MainApp() {
    val navController = rememberNavController()

    val bottomNavItems = listOf(
        BottomNavItem(
            route = NavRoutes.HOME,
            title = "Start",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home
        ),
        BottomNavItem(
            route = NavRoutes.ACTIVITY,
            title = "Trening",
            selectedIcon = Icons.Filled.DirectionsRun,
            unselectedIcon = Icons.Outlined.DirectionsRun
        ),
        BottomNavItem(
            route = NavRoutes.HISTORY,
            title = "Historia",
            selectedIcon = Icons.Filled.History,
            unselectedIcon = Icons.Outlined.History
        ),
        BottomNavItem(
            route = NavRoutes.PHOTO_GALLERY,
            title = "Galeria",
            selectedIcon = Icons.Filled.PhotoLibrary,
            unselectedIcon = Icons.Outlined.PhotoLibrary
        ),
        BottomNavItem(
            route = NavRoutes.SETTINGS,
            title = "Ustawienia",
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings
        )
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        NavRoutes.HOME,
        NavRoutes.ACTIVITY,
        NavRoutes.HISTORY,
        NavRoutes.PHOTO_GALLERY,
        NavRoutes.SETTINGS
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentRoute == item.route

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(NavRoutes.HOME) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title
                                )
                            },
                            label = { Text(item.title) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = NavRoutes.HOME,
            modifier = Modifier.padding(padding)
        ) {
            composable(NavRoutes.HOME) {
                HomeScreen(
                    onNavigateToActivity = {
                        navController.navigate(NavRoutes.ACTIVITY)
                    },
                    onNavigateToHistory = {
                        navController.navigate(NavRoutes.HISTORY)
                    },
                    onNavigateToSettings = {
                        navController.navigate(NavRoutes.SETTINGS)
                    },
                    onNavigateToActivityDetail = { activityId ->
                        navController.navigate(NavRoutes.activityDetail(activityId))
                    }
                )
            }

            composable(NavRoutes.ACTIVITY) {
                ActivityScreen(
                    onNavigateToHistory = {
                        navController.navigate(NavRoutes.HISTORY)
                    },
                    onNavigateToSettings = {
                        navController.navigate(NavRoutes.SETTINGS)
                    },
                    onNavigateToGallery = {
                        navController.navigate(NavRoutes.PHOTO_GALLERY)
                    }
                )
            }

            composable(NavRoutes.HISTORY) {
                HistoryScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToActivityDetail = { activityId ->
                        navController.navigate(NavRoutes.activityDetail(activityId))
                    }
                )
            }

            composable(
                route = NavRoutes.ACTIVITY_DETAIL,
                arguments = listOf(navArgument("activityId") { type = NavType.LongType })
            ) { backStackEntry ->
                val activityId = backStackEntry.arguments?.getLong("activityId") ?: 0L
                ActivityDetailScreen(
                    activityId = activityId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(NavRoutes.PHOTO_GALLERY) {
                PhotoGalleryScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(NavRoutes.SETTINGS) {
                SettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
