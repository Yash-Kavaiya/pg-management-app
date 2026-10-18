package com.example.pgmanagerpro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.pgmanagerpro.data.local.entities.NotificationPrefEntity
import com.example.pgmanagerpro.ui.PGViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: PGViewModel,
    onLogout: () -> Unit
) {
    val pgs by viewModel.allPGs.collectAsState()
    val selectedPgId by viewModel.selectedPgId.collectAsState()
    val notificationPrefs by viewModel.notificationPrefs.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Property, 1: Notifications, 2: Account
    var showAddPgDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Settings & Preferences",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )

        PrimaryTabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Properties") },
                icon = { Icon(Icons.Default.Apartment, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Reminders") },
                icon = { Icon(Icons.Default.Notifications, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Account") },
                icon = { Icon(Icons.Default.Person, contentDescription = null) }
            )
        }

        when (selectedTab) {
            0 -> {
                // Property Settings
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Managed Properties",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Button(
                            onClick = { showAddPgDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("add_property_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Property")
                        }
                    }

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(pgs) { pg ->
                            val isSelected = pg.id == selectedPgId
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = pg.name,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "${pg.address}, ${pg.city}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${pg.totalRooms} Rooms Total",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    if (isSelected) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "Active",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White
                                            )
                                        }
                                    } else {
                                        OutlinedButton(onClick = { viewModel.selectPG(pg.id) }) {
                                            Text("Switch")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // Notification Preferences
                val currentPref = notificationPrefs ?: NotificationPrefEntity(selectedPgId)

                var emailEnabled by remember(currentPref) { mutableStateOf(currentPref.emailEnabled) }
                var smsEnabled by remember(currentPref) { mutableStateOf(currentPref.smsEnabled) }
                var daysBefore by remember(currentPref) { mutableStateOf(currentPref.daysBefore.toString()) }
                var sendOverdueReminders by remember(currentPref) { mutableStateOf(currentPref.sendOverdueReminders) }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Automated Payment Reminders",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Email Reminders", style = MaterialTheme.typography.bodyLarge)
                                Text("Send automated email notices to tenants", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = emailEnabled,
                                onCheckedChange = { emailEnabled = it },
                                modifier = Modifier.testTag("email_reminder_switch")
                            )
                        }

                        HorizontalDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("SMS Reminders", style = MaterialTheme.typography.bodyLarge)
                                Text("Send SMS alerts via integrated backend service", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = smsEnabled,
                                onCheckedChange = { smsEnabled = it },
                                modifier = Modifier.testTag("sms_reminder_switch")
                            )
                        }

                        HorizontalDivider()

                        OutlinedTextField(
                            value = daysBefore,
                            onValueChange = { daysBefore = it },
                            label = { Text("Days Before Due Date to Send Notice") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Send Overdue Alerts", style = MaterialTheme.typography.bodyLarge)
                                Text("Auto-retry alerts for past due balances", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = sendOverdueReminders,
                                onCheckedChange = { sendOverdueReminders = it }
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.updateNotificationPrefs(
                                    NotificationPrefEntity(
                                        pgId = selectedPgId,
                                        emailEnabled = emailEnabled,
                                        smsEnabled = smsEnabled,
                                        daysBefore = daysBefore.toIntOrNull() ?: 3,
                                        sendOverdueReminders = sendOverdueReminders
                                    )
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("save_notification_prefs")
                        ) {
                            Text("Save Reminder Settings")
                        }
                    }
                }
            }

            2 -> {
                // Account & Theme Settings
                val themeMode by viewModel.themeMode.collectAsState()

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Appearance & Accenture Theme",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Text(
                            text = "Choose your preferred color theme mode for PG Manager Pro:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            SegmentedButton(
                                selected = themeMode == "Light",
                                onClick = { viewModel.setThemeMode("Light") },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                                icon = { Icon(Icons.Default.LightMode, contentDescription = null) }
                            ) {
                                Text("Light")
                            }
                            SegmentedButton(
                                selected = themeMode == "Dark",
                                onClick = { viewModel.setThemeMode("Dark") },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                                icon = { Icon(Icons.Default.DarkMode, contentDescription = null) }
                            ) {
                                Text("Dark")
                            }
                            SegmentedButton(
                                selected = themeMode == "System",
                                onClick = { viewModel.setThemeMode("System") },
                                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                                icon = { Icon(Icons.Default.BrightnessAuto, contentDescription = null) }
                            ) {
                                Text("System")
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        Text(
                            text = "Account Information",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Text("Property Owner: Yash Kavaiya", style = MaterialTheme.typography.bodyMedium)
                        Text("Email: yash.kavaiya3@gmail.com", style = MaterialTheme.typography.bodyMedium)
                        Text("Role: Administrator", style = MaterialTheme.typography.bodyMedium)

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        Button(
                            onClick = onLogout,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("logout_button")
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sign Out")
                        }
                    }
                }
            }
        }
    }

    if (showAddPgDialog) {
        AddPGDialog(
            onDismiss = { showAddPgDialog = false },
            onConfirm = { name, address, city, totalRooms ->
                viewModel.addPG(name, address, city, totalRooms)
                showAddPgDialog = false
            }
        )
    }
}

@Composable
fun AddPGDialog(onDismiss: () -> Unit, onConfirm: (name: String, address: String, city: String, totalRooms: Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Bangalore") }
    var totalRooms by remember { mutableStateOf("20") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Property (PG)") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("PG Name (e.g. Royal Stays)") },
                    modifier = Modifier.fillMaxWidth().testTag("add_pg_name_input")
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Street Address") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("City") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = totalRooms,
                    onValueChange = { totalRooms = it },
                    label = { Text("Total Rooms Capacity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, address, city, totalRooms.toIntOrNull() ?: 10)
                    }
                },
                modifier = Modifier.testTag("confirm_add_pg")
            ) {
                Text("Create Property")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
