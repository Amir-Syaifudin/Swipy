package com.example.swipy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swipy.ui.theme.*

@Composable
fun HomeScreen(onNavigate: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))

            // Header
            Text(
                text = "Swipy",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Light,
                color = DustyBlue
            )
            Text(
                text = "Geser. Hapus. Lega.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(Modifier.height(40.dp))

            // Main swipe card
            HomeModeCard(
                title = "Mulai Swipe",
                subtitle = "Pilih folder & bersihkan foto",
                icon = Icons.Default.AutoAwesome,
                backgroundColor = DustyBlue.copy(alpha = 0.12f),
                iconTint = DustyBlue,
                onClick = { onNavigate("folderPicker") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HomeModeCard(
                    title = "Sampah",
                    subtitle = "Foto antrian hapus",
                    icon = Icons.Default.Delete,
                    backgroundColor = SoftPink.copy(alpha = 0.12f),
                    iconTint = SoftPink,
                    onClick = { onNavigate("trash") },
                    modifier = Modifier.weight(1f)
                )
                HomeModeCard(
                    title = "Favorit",
                    subtitle = "Foto yang disimpan",
                    icon = Icons.Default.Favorite,
                    backgroundColor = SageGreen.copy(alpha = 0.12f),
                    iconTint = SageGreen,
                    onClick = { onNavigate("favorites") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.weight(1f))

            // Settings
            TextButton(onClick = { onNavigate("settings") }) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = Color.Gray)
                Spacer(Modifier.width(8.dp))
                Text("Pengaturan", color = Color.Gray)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HomeModeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    backgroundColor: Color,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconTint.copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = iconTint, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color(0xFF2C2C2C))
            Spacer(Modifier.height(4.dp))
            Text(text = subtitle, fontSize = 12.sp, color = Color.Gray)
        }
    }
}
