package com.example.pgmanagerpro.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pgmanagerpro.ui.PGViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: PGViewModel,
    onNavigateToRooms: () -> Unit,
    onNavigateToBookings: () -> Unit,
    onNavigateToPayments: () -> Unit,
    onNavigateToTenants: () -> Unit
) {
    val selectedPG by viewModel.selectedPG.collectAsState()
    val pgs by viewModel.allPGs.collectAsState()
    val rooms by viewModel.rooms.collectAsState()
    val tenants by viewModel.tenants.collectAsState()
    val bookings by viewModel.bookings.collectAsState()
    val payments by viewModel.payments.collectAsState()

    var pgDropdownExpanded by remember { mutableStateOf(false) }

    val totalRooms = rooms.size.coerceAtLeast(selectedPG?.totalRooms ?: 1)
    val occupiedCount = rooms.count { it.status == "Occupied" }
    val occupancyRate = if (totalRooms > 0) (occupiedCount.toFloat() / totalRooms) else 0f
    val pendingPayments = payments.filter { it.status == "Pending" || it.status == "Overdue" }
    val totalPendingAmount = pendingPayments.sumOf { it.amount }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // PG Selector Bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Selected Property",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = selectedPG?.name ?: "Sunrise Executive Stays",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${selectedPG?.address ?: ""}, ${selectedPG?.city ?: ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { pgDropdownExpanded = true },
                            modifier = Modifier.testTag("pg_selector_dropdown")
                        ) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select PG")
                        }
                        DropdownMenu(
                            expanded = pgDropdownExpanded,
                            onDismissRequest = { pgDropdownExpanded = false }
                        ) {
                            pgs.forEach { pg ->
                                DropdownMenuItem(
                                    text = { Text(pg.name) },
                                    onClick = {
                                        viewModel.selectPG(pg.id)
                                        pgDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Stats Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Occupancy",
                    value = "$occupiedCount / $totalRooms",
                    subtext = "${(occupancyRate * 100).toInt()}% Occupied",
                    icon = Icons.Default.MeetingRoom,
                    iconBgColor = MaterialTheme.colorScheme.primaryContainer,
                    iconTint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f).clickable { onNavigateToRooms() }
                )
                StatCard(
                    title = "Due Payments",
                    value = "₹${totalPendingAmount.toInt()}",
                    subtext = "${pendingPayments.size} pending",
                    icon = Icons.Default.Payment,
                    iconBgColor = Color(0xFFFFEDD5),
                    iconTint = Color(0xFFEA580C),
                    modifier = Modifier.weight(1f).clickable { onNavigateToPayments() }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Total Tenants",
                    value = "${tenants.size}",
                    subtext = "Active residents",
                    icon = Icons.Default.People,
                    iconBgColor = Color(0xFFDCFCE7),
                    iconTint = Color(0xFF16A34A),
                    modifier = Modifier.weight(1f).clickable { onNavigateToTenants() }
                )
                StatCard(
                    title = "Bookings",
                    value = "${bookings.size}",
                    subtext = "Total records",
                    icon = Icons.Default.BookOnline,
                    iconBgColor = Color(0xFFF3E8FF),
                    iconTint = Color(0xFF7C3AED),
                    modifier = Modifier.weight(1f).clickable { onNavigateToBookings() }
                )
            }
        }

        // Occupancy Visualizer Card with Donut Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Occupancy & Capacity",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${(occupancyRate * 100).toInt()}% Capacity",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        OccupancyDonutChart(
                            occupied = occupiedCount,
                            total = totalRooms,
                            modifier = Modifier.testTag("occupancy_donut_chart")
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            LegendItem(label = "Occupied", count = occupiedCount, color = MaterialTheme.colorScheme.primary)
                            LegendItem(label = "Available", count = (totalRooms - occupiedCount).coerceAtLeast(0), color = MaterialTheme.colorScheme.surfaceVariant)
                            LegendItem(label = "Total Capacity", count = totalRooms, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
        }

        // Pending Payments Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pending & Overdue Payments",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                TextButton(onClick = onNavigateToPayments) {
                    Text("View All")
                }
            }
        }

        if (pendingPayments.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No pending payments!", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        } else {
            items(pendingPayments.take(3)) { payment ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = payment.tenant,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Room ${payment.room} • Due ${payment.dueDate}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "₹${payment.amount.toInt()}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (payment.status == "Overdue") Color(0xFFDC2626) else Color(0xFFEA580C)
                            )
                            Surface(
                                color = if (payment.status == "Overdue") Color(0xFFFEE2E2) else Color(0xFFFFEDD5),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = payment.status,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (payment.status == "Overdue") Color(0xFFDC2626) else Color(0xFFEA580C)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtext: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    color = iconBgColor,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                    }
                }
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = subtext,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun LegendItem(label: String, count: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, shape = RoundedCornerShape(2.dp))
        )
        Text(
            text = "$label: $count",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun OccupancyDonutChart(
    occupied: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    val occupiedSweep = if (total > 0) (occupied.toFloat() / total) * 360f else 0f
    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(130.dp)) {
            val strokeWidth = 20.dp.toPx()
            // Track / Available arc
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            // Occupied arc
            if (occupiedSweep > 0) {
                drawArc(
                    color = primaryColor,
                    startAngle = -90f,
                    sweepAngle = occupiedSweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val percentage = if (total > 0) ((occupied.toFloat() / total) * 100).toInt() else 0
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$occupied / $total Rooms",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
