package com.example.swipy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.swipy.ui.theme.*
import com.example.swipy.ui.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val selectedAccent by viewModel.selectedAccent.collectAsState()
    val storageReminder by viewModel.storageReminderEnabled.collectAsState()
    val weeklyNotif by viewModel.weeklyNotifEnabled.collectAsState()

    val accents = listOf(
        Triple("Dusty Blue", DustyBlue, "Biru pastel tenang"),
        Triple("Soft Pink", SoftPink, "Pink lembut hangat"),
        Triple("Sage Green", SageGreen, "Hijau alami segar")
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Pengaturan", fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = WarmWhite)
            )
        },
        containerColor = WarmWhite
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            item {
                SettingsSectionTitle("Warna Aksen")
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        accents.forEachIndexed { idx, (name, color, desc) ->
                            AccentOption(
                                name = name,
                                color = color,
                                description = desc,
                                isSelected = selectedAccent == idx
                            ) { viewModel.setAccent(idx) }
                        }
                    }
                }
            }

            item {
                SettingsSectionTitle("Notifikasi")
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        ToggleRow(
                            title = "Pengingat mingguan",
                            subtitle = "Setiap Senin pukul 10.00",
                            checked = weeklyNotif,
                            onCheck = { viewModel.setWeeklyNotif(it) }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        ToggleRow(
                            title = "Pengingat penyimpanan",
                            subtitle = "Ketika penyimpanan > 80%",
                            checked = storageReminder,
                            onCheck = { viewModel.setStorageReminder(it) }
                        )
                    }
                }
            }

            item {
                SettingsSectionTitle("Tentang Aplikasi")
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Swipy v1.0", fontWeight = FontWeight.Medium, fontSize = 15.sp, color = Color(0xFF2C2C2C))
                        Text("Semua foto diproses lokal di device kamu. Tidak ada data yang dikirim ke server.", fontSize = 13.sp, color = Color.Gray, lineHeight = 18.sp)
                    }
                }
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        color = Color.Gray,
        modifier = Modifier.padding(start = 4.dp)
    )
}

@Composable
private fun AccentOption(name: String, color: Color, description: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(36.dp).background(color, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal, fontSize = 15.sp, color = Color(0xFF2C2C2C))
            Text(description, fontSize = 12.sp, color = Color.Gray)
        }
        if (isSelected) {
            Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        }
    }
}

@Composable
private fun ToggleRow(title: String, subtitle: String, checked: Boolean, onCheck: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = Color(0xFF2C2C2C))
            Text(subtitle, fontSize = 12.sp, color = Color.Gray)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheck,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = DustyBlue)
        )
    }
}
