package com.example.swipy.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.swipy.ui.screens.*

@Composable
fun App() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = "splash") {

        composable("splash") {
            SplashScreen(onSplashFinished = {
                nav.navigate("home") { popUpTo("splash") { inclusive = true } }
            })
        }

        composable("home") {
            HomeScreen(onNavigate = { route -> nav.navigate(route) })
        }

        composable("folderPicker") {
            FolderPickerScreen(
                onFolderSelected = { _ -> nav.navigate("swipe") },
                onBack = { nav.popBackStack() }
            )
        }

        composable("swipe") {
            SwipeScreen(
                onBack = { nav.popBackStack() },
                onDone = {
                    nav.navigate("summary") { popUpTo("swipe") { inclusive = true } }
                }
            )
        }

        composable("summary") {
            SummaryScreen(onBack = {
                nav.navigate("home") { popUpTo("home") { inclusive = false } }
            })
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
