package com.example.swipy.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.swipy.ui.screens.*

@Composable
fun App() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") { SplashScreen(onSplashFinished = { navController.navigate("home") }) }
        composable("home") { HomeScreen(onNavigate = { route -> navController.navigate(route) }) }
        composable("folderPicker") { FolderPickerScreen(onFolderSelected = { /*TODO*/ }) }
        composable("swipe") { SwipeScreen() }
        composable("summary") { SummaryScreen() }
        composable("trash") { TrashScreen() }
        composable("favorites") { FavoritesScreen() }
        composable("settings") { SettingsScreen() }
    }
}
