package com.example.pgmanagerpro.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pgmanagerpro.ui.screens.*

sealed class NavDestination(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : NavDestination("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Rooms : NavDestination("rooms", "Rooms", Icons.Default.MeetingRoom)
    object Bookings : NavDestination("bookings", "Bookings", Icons.Default.BookOnline)
    object Payments : NavDestination("payments", "Payments", Icons.Default.Payment)
    object Tenants : NavDestination("tenants", "Tenants", Icons.Default.People)
    object Settings : NavDestination("settings", "Settings", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(
    viewModel: PGViewModel,
    onLogout: () -> Unit
) {
    var currentDestination by remember { mutableStateOf<NavDestination>(NavDestination.Dashboard) }

    val destinations = listOf(
        NavDestination.Dashboard,
        NavDestination.Rooms,
        NavDestination.Bookings,
        NavDestination.Payments,
        NavDestination.Tenants,
        NavDestination.Settings
    )

    val themeMode by viewModel.themeMode.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                                Text(
                                    text = ">",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                        Text(
                            text = currentDestination.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val nextMode = when (themeMode) {
                                "Light" -> "Dark"
                                "Dark" -> "System"
                                else -> "Light"
                            }
                            viewModel.setThemeMode(nextMode)
                        },
                        modifier = Modifier.testTag("toggle_theme_button")
                    ) {
                        val (icon, desc) = when (themeMode) {
                            "Dark" -> Icons.Default.DarkMode to "Dark Mode Active (Tap for System)"
                            "Light" -> Icons.Default.LightMode to "Light Mode Active (Tap for Dark)"
                            else -> Icons.Default.BrightnessAuto to "System Theme (Tap for Light)"
                        }
                        Icon(icon, contentDescription = desc, tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                destinations.forEach { destination ->
                    val isSelected = currentDestination.route == destination.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentDestination = destination },
                        icon = { Icon(destination.icon, contentDescription = destination.title) },
                        label = { Text(destination.title) },
                        modifier = Modifier.testTag("nav_${destination.route}")
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentDestination) {
                NavDestination.Dashboard -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToRooms = { currentDestination = NavDestination.Rooms },
                    onNavigateToBookings = { currentDestination = NavDestination.Bookings },
                    onNavigateToPayments = { currentDestination = NavDestination.Payments },
                    onNavigateToTenants = { currentDestination = NavDestination.Tenants }
                )
                NavDestination.Rooms -> RoomsScreen(viewModel = viewModel)
                NavDestination.Bookings -> BookingsScreen(viewModel = viewModel)
                NavDestination.Payments -> PaymentsScreen(viewModel = viewModel)
                NavDestination.Tenants -> TenantsScreen(viewModel = viewModel)
                NavDestination.Settings -> SettingsScreen(viewModel = viewModel, onLogout = onLogout)
            }
        }
    }
}
