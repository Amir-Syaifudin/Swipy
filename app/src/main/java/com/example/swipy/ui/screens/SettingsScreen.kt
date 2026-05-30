package com.example.swipy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.*
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
    val dailyNotif by viewModel.dailyNotifEnabled.collectAsState()
    val dontShowPicker by viewModel.dontShowModePicker.collectAsState()
    val defaultMode by viewModel.defaultSwipeMode.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Pengaturan", fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(PhosphorIcons.Regular.ArrowLeft, contentDescription = "Back") }
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
                SettingsSectionTitle("Mode Geser")
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        ToggleRow(
                            title = "Sembunyikan pilihan mode",
                            subtitle = "Gunakan mode default secara otomatis",
                            checked = dontShowPicker,
                            onCheck = { viewModel.setDontShowModePicker(it) }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        
                        SelectionRow(
                            title = "Mode default",
                            subtitle = if (defaultMode == "bouncy") "Bouncy (Efek membal)" else "Seamless (Lancar)",
                            options = listOf("bouncy", "seamless"),
                            selectedOption = defaultMode,
                            onOptionSelected = { viewModel.setDefaultSwipeMode(it) }
                        )
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
                            title = "Pengingat harian",
                            subtitle = "Setiap hari pukul 22.00",
                            checked = dailyNotif,
                            onCheck = { viewModel.setDailyNotif(it) }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
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

@Composable
private fun SelectionRow(
    title: String,
    subtitle: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Pilih Mode Default") },
            text = {
                Column {
                    options.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    onOptionSelected(option)
                                    showDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = option == selectedOption,
                                onClick = { 
                                    onOptionSelected(option)
                                    showDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = DustyBlue)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (option == "bouncy") "Bouncy" else "Seamless",
                                fontSize = 16.sp,
                                color = Color(0xFF2C2C2C)
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Batal", color = DustyBlue) }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = Color(0xFF2C2C2C))
            Text(subtitle, fontSize = 12.sp, color = Color.Gray)
        }
        Icon(
            imageVector = PhosphorIcons.Regular.CaretRight,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
    }
}
