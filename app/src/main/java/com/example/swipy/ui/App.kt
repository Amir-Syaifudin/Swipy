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
            HomeScreen { route -> 
                if (route == "swipe/all?bucketName=Hari_Ini") {
                    nav.navigate("swipe/all/bouncy/Hari_Ini")
                } else {
                    nav.navigate(route)
                }
            }
        }

        composable(
            route = "folderPicker/{mediaType}",
            arguments = listOf(navArgument("mediaType") { type = NavType.StringType })
        ) { backStackEntry ->
            val mediaType = backStackEntry.arguments?.getString("mediaType") ?: "photo"
            FolderPickerScreen(
                mediaType = mediaType,
                onFolderSelected = { bucketName, swipeMode -> 
                    nav.navigate("swipe/$mediaType/$swipeMode/$bucketName") 
                },
                onBack = { nav.popBackStack() },
            )
        }

        composable(
            route = "swipe/{mediaType}/{swipeMode}/{bucketName}",
            arguments = listOf(
                navArgument("mediaType") { type = NavType.StringType },
                navArgument("swipeMode") { type = NavType.StringType },
                navArgument("bucketName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val mediaType = backStackEntry.arguments?.getString("mediaType") ?: "photo"
            val swipeMode = backStackEntry.arguments?.getString("swipeMode") ?: "bouncy"
            val bucketName = backStackEntry.arguments?.getString("bucketName")
            SwipeScreen(
                mediaType = mediaType,
                swipeMode = swipeMode,
                bucketName = bucketName,
                onBack = { nav.popBackStack() },
                onDone = { deleted, deletedSz, kept, keptSz, fav, favSz ->
                    nav.navigate(
                        "summary/$deleted/$deletedSz/$kept/$keptSz/$fav/$favSz"
                    ) { popUpTo("swipe/{mediaType}/{swipeMode}/{bucketName}") { inclusive = true } }
                }
            )
        }

        composable(
            route = "summary/{deleted}/{deletedSz}/{kept}/{keptSz}/{fav}/{favSz}",
            arguments = listOf(
                navArgument("deleted")   { type = NavType.IntType },
                navArgument("deletedSz") { type = NavType.LongType },
                navArgument("kept")      { type = NavType.IntType },
                navArgument("keptSz")    { type = NavType.LongType },
                navArgument("fav")       { type = NavType.IntType },
                navArgument("favSz")     { type = NavType.LongType },
            )
        ) { back ->
            val args = back.arguments!!
            SummaryScreen(
                deletedCount   = args.getInt("deleted"),
                deletedSize    = args.getLong("deletedSz"),
                keptCount      = args.getInt("kept"),
                keptSize       = args.getLong("keptSz"),
                favoriteCount  = args.getInt("fav"),
                favoriteSize   = args.getLong("favSz"),
                onBack = {
                    nav.navigate("home") { popUpTo("home") { inclusive = false } }
                }
            )
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
