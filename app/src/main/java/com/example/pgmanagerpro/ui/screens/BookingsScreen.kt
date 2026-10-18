package com.example.pgmanagerpro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.pgmanagerpro.data.local.entities.BookingEntity
import com.example.pgmanagerpro.ui.PGViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingsScreen(viewModel: PGViewModel) {
    val bookings by viewModel.bookings.collectAsState()
    val rooms by viewModel.rooms.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf<String?>(null) }
    var showAddBookingDialog by remember { mutableStateOf(false) }
    var selectedBookingForDetails by remember { mutableStateOf<BookingEntity?>(null) }

    val filteredBookings = remember(bookings, searchQuery, selectedStatusFilter) {
        bookings.filter { booking ->
            val matchesSearch = searchQuery.isEmpty() ||
                    booking.tenant.contains(searchQuery, ignoreCase = true) ||
                    booking.room.contains(searchQuery, ignoreCase = true) ||
                    booking.email.contains(searchQuery, ignoreCase = true)

            val matchesStatus = selectedStatusFilter == null ||
                    booking.status.equals(selectedStatusFilter, ignoreCase = true)

            matchesSearch && matchesStatus
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Bookings & Reservations",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            // Search input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by tenant, room or email...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().testTag("booking_search_input"),
                singleLine = true
            )

            // Status Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedStatusFilter == null,
                    onClick = { selectedStatusFilter = null },
                    label = { Text("All") }
                )
                listOf("Active", "Upcoming", "Pending", "Completed").forEach { status ->
                    val isSelected = selectedStatusFilter == status
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedStatusFilter = if (isSelected) null else status },
                        label = { Text(status) }
                    )
                }
            }

            // Bookings List
            if (filteredBookings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No bookings found matching criteria.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredBookings) { booking ->
                        BookingItemCard(
                            booking = booking,
                            onClick = { selectedBookingForDetails = booking },
                            onDelete = { viewModel.deleteBooking(booking.id) }
                        )
                    }
                }
            }
        }

        // Add Booking FAB
        FloatingActionButton(
            onClick = { showAddBookingDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_booking_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "New Booking")
        }
    }

    if (showAddBookingDialog) {
        AddBookingDialog(
            availableRooms = rooms.map { it.number },
            onDismiss = { showAddBookingDialog = false },
            onConfirm = { room, tenant, email, phone, startDate, endDate, status, rent, deposit, advance, emName, emPhone, emRel, notes ->
                viewModel.addBooking(
                    room, tenant, email, phone, startDate, endDate, status, rent, deposit, advance, emName, emPhone, emRel, notes
                )
                showAddBookingDialog = false
            }
        )
    }

    selectedBookingForDetails?.let { booking ->
        BookingDetailsDialog(
            booking = booking,
            onDismiss = { selectedBookingForDetails = null },
            onConvertToTenant = {
                viewModel.addTenant(
                    name = booking.tenant,
                    room = booking.room,
                    phone = booking.phone,
                    email = booking.email,
                    status = "Active",
                    joinDate = booking.startDate
                )
                viewModel.updateBooking(booking.copy(status = "Active"))
                selectedBookingForDetails = null
            }
        )
    }
}

@Composable
fun BookingItemCard(booking: BookingEntity, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = booking.tenant,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    BookingStatusBadge(status = booking.status)
                }

                Text(
                    text = "Room ${booking.room} • ${booking.startDate} to ${booking.endDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Rent: ₹${booking.monthlyRent.toInt()} • Deposit: ₹${booking.securityDeposit.toInt()}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun BookingStatusBadge(status: String) {
    val (bgColor, textColor) = when (status.lowercase()) {
        "active" -> Color(0xFFDCFCE7) to Color(0xFF16A34A)
        "upcoming" -> Color(0xFFEEF2FF) to Color(0xFF4F46E5)
        "pending" -> Color(0xFFFFEDD5) to Color(0xFFEA580C)
        "completed" -> Color(0xFFF1F5F9) to Color(0xFF475569)
        else -> Color.LightGray to Color.DarkGray
    }

    Surface(color = bgColor, shape = RoundedCornerShape(4.dp)) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}

@Composable
fun AddBookingDialog(
    availableRooms: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (
        room: String, tenant: String, email: String, phone: String,
        startDate: String, endDate: String, status: String, rent: Double,
        deposit: Double, advance: Double, emName: String, emPhone: String,
        emRel: String, notes: String
    ) -> Unit
) {
    var room by remember { mutableStateOf(availableRooms.firstOrNull() ?: "101") }
    var tenant by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("2026-08-10") }
    var endDate by remember { mutableStateOf("2027-08-09") }
    var rent by remember { mutableStateOf("12000") }
    var deposit by remember { mutableStateOf("24000") }
    var advance by remember { mutableStateOf("5000") }
    var emergencyName by remember { mutableStateOf("") }
    var emergencyPhone by remember { mutableStateOf("") }
    var emergencyRelation by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Booking") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.heightIn(max = 400.dp)) {
                item {
                    OutlinedTextField(
                        value = tenant,
                        onValueChange = { tenant = it },
                        label = { Text("Tenant Full Name") },
                        modifier = Modifier.fillMaxWidth().testTag("add_booking_tenant_input")
                    )
                }
                item {
                    OutlinedTextField(
                        value = room,
                        onValueChange = { room = it },
                        label = { Text("Room Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = startDate,
                            onValueChange = { startDate = it },
                            label = { Text("Start Date") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = endDate,
                            onValueChange = { endDate = it },
                            label = { Text("End Date") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = rent,
                            onValueChange = { rent = it },
                            label = { Text("Monthly Rent (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = deposit,
                            onValueChange = { deposit = it },
                            label = { Text("Deposit (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = emergencyName,
                        onValueChange = { emergencyName = it },
                        label = { Text("Emergency Contact Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = emergencyPhone,
                        onValueChange = { emergencyPhone = it },
                        label = { Text("Emergency Contact Phone") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (tenant.isNotBlank() && room.isNotBlank()) {
                        onConfirm(
                            room, tenant, email, phone, startDate, endDate, "Active",
                            rent.toDoubleOrNull() ?: 0.0,
                            deposit.toDoubleOrNull() ?: 0.0,
                            advance.toDoubleOrNull() ?: 0.0,
                            emergencyName, emergencyPhone, emergencyRelation, notes
                        )
                    }
                },
                modifier = Modifier.testTag("confirm_add_booking")
            ) {
                Text("Confirm Booking")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun BookingDetailsDialog(
    booking: BookingEntity,
    onDismiss: () -> Unit,
    onConvertToTenant: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(booking.tenant)
                BookingStatusBadge(status = booking.status)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Room Number: ${booking.room}", style = MaterialTheme.typography.bodyMedium)
                Text("Phone: ${booking.phone.ifEmpty { "N/A" }}", style = MaterialTheme.typography.bodyMedium)
                Text("Email: ${booking.email.ifEmpty { "N/A" }}", style = MaterialTheme.typography.bodyMedium)
                Text("Tenancy Period: ${booking.startDate} to ${booking.endDate}", style = MaterialTheme.typography.bodyMedium)
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text("Monthly Rent: ₹${booking.monthlyRent.toInt()}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                Text("Security Deposit: ₹${booking.securityDeposit.toInt()}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                Text("Token Advance Paid: ₹${booking.advance.toInt()}", style = MaterialTheme.typography.bodyMedium)
                if (booking.emergencyContactName.isNotEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text("Emergency Contact: ${booking.emergencyContactName} (${booking.emergencyContactPhone})", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!booking.status.equals("Active", ignoreCase = true)) {
                    Button(
                        onClick = onConvertToTenant,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Convert to Active Tenant")
                    }
                }
                TextButton(onClick = onDismiss) { Text("Close") }
            }
        }
    )
}
