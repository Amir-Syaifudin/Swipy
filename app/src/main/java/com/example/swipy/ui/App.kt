package com.example.swipy.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.swipy.ui.screens.*

@Composable
fun App() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = "splash") {

        composable("splash") {
            SplashScreen {
                nav.navigate("home") {
                    popUpTo("splash") {
                        inclusive = true
                    }
                }
            }
        }

        composable("home") {
            HomeScreen { route -> nav.navigate(route) }
        }

        composable("folderPicker") {
            FolderPickerScreen(
                onFolderSelected = { bucketName -> 
                    nav.navigate("swipe/$bucketName") 
                },
                onBack = { nav.popBackStack() },
            )
        }

        composable(
            route = "swipe/{bucketName}",
            arguments = listOf(navArgument("bucketName") { type = NavType.StringType })
        ) { backStackEntry ->
            val bucketName = backStackEntry.arguments?.getString("bucketName")
            SwipeScreen(
                bucketName = bucketName,
                onBack = { nav.popBackStack() },
                onDone = {
                    nav.navigate("summary") { popUpTo("swipe/{bucketName}") { inclusive = true } }
                }
            )
        }

        composable("summary") {
            SummaryScreen {
                nav.navigate("home") { popUpTo("home") { inclusive = false } }
            }
        }

        composable("trash") {
            TrashScreen(onBack = { nav.popBackStack() })
        }

        composable("favorites") {
            FavoritesScreen(onBack = { nav.popBackStack() })
        }

        composable("settings") {
            SettingsScreen(onBack = { nav.popBackStack() })
        }
    }
}
