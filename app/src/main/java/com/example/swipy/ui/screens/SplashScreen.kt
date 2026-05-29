package com.example.swipy.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.text.font.FontWeight
import com.example.swipy.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swipy.ui.theme.DustyBlue
import com.example.swipy.ui.theme.WarmWhite
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.SelectionBackground
import androidx.compose.material3.Icon
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    var permissionChecked by remember { mutableStateOf(false) }

    // Request multiple permissions at once so both images AND videos are accessible
    val multiplePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        permissionChecked = true
    }

    LaunchedEffect(Unit) {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
                // Android 13+: request granular media permissions for images AND videos
                multiplePermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO
                    )
                )
            else ->
                multiplePermissionLauncher.launch(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                )
        }
    }

    val scale = remember { Animatable(0.5f) }
    val alpha = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(permissionChecked) {
        if (permissionChecked) {
            scope.launch { scale.animateTo(1f, animationSpec = tween(700, easing = FastOutSlowInEasing)) }
            scope.launch { alpha.animateTo(1f, animationSpec = tween(700)) }
            delay(1100L)
            onSplashFinished()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(WarmWhite),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale.value)
                    .background(DustyBlue.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = PhosphorIcons.Regular.SelectionBackground,
                    contentDescription = "Swipy Logo",
                    tint = DustyBlue,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Swipy",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Light,
                color = DustyBlue,
                modifier = Modifier.scale(scale.value)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Bersihkan galeri, satu swipe",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(Modifier.height(48.dp))

            if (!permissionChecked) {
                CircularProgressIndicator(color = DustyBlue, strokeWidth = 2.dp)
            }
        }
    }
}
