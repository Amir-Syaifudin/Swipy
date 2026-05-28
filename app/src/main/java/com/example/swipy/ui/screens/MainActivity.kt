package com.example.swipy.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.swipy.data.UserPreferences
import com.example.swipy.ui.App
import com.example.swipy.ui.theme.SwipyTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val accentIndex by userPreferences.selectedAccent.collectAsState(initial = 0)
            
            SwipyTheme(accentIndex = accentIndex) {
                App()
            }
        }
    }
}
