package com.example.pgmanagerpro.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.pgmanagerpro.data.local.entities.PaymentEntity
import com.example.pgmanagerpro.data.local.entities.TenantEntity
import com.example.pgmanagerpro.ui.PGViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantsScreen(viewModel: PGViewModel) {
    val tenants by viewModel.tenants.collectAsState()
    val rooms by viewModel.rooms.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf<String?>(null) }
    var showAddTenantDialog by remember { mutableStateOf(false) }
    var editingTenant by remember { mutableStateOf<TenantEntity?>(null) }
    var selectedTenantDetails by remember { mutableStateOf<TenantEntity?>(null) }
    var selectedTenantForNewPayment by remember { mutableStateOf<TenantEntity?>(null) }

    val filteredTenants = remember(tenants, searchQuery, selectedStatusFilter) {
        tenants.filter { tenant ->
            val matchesSearch = searchQuery.isEmpty() ||
                    tenant.name.contains(searchQuery, ignoreCase = true) ||
                    tenant.room.contains(searchQuery, ignoreCase = true) ||
                    tenant.phone.contains(searchQuery, ignoreCase = true) ||
                    tenant.email.contains(searchQuery, ignoreCase = true)

            val matchesStatus = selectedStatusFilter == null ||
                    tenant.status.equals(selectedStatusFilter, ignoreCase = true)

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
                text = "Tenants Directory",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search tenants by name, room, or contact...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().testTag("tenant_search_input"),
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
                listOf("Active", "Notice Period", "Inactive").forEach { status ->
                    val isSelected = selectedStatusFilter == status
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedStatusFilter = if (isSelected) null else status },
                        label = { Text(status) }
                    )
                }
            }

            // Directory List
            if (filteredTenants.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tenants found.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredTenants) { tenant ->
                        TenantCard(
                            tenant = tenant,
                            onClick = { selectedTenantDetails = tenant },
                            onEdit = { editingTenant = tenant },
                            onDelete = { viewModel.deleteTenant(tenant.id) }
                        )
                    }
                }
            }
        }

        // Add Tenant FAB
        FloatingActionButton(
            onClick = { showAddTenantDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_tenant_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Tenant")
        }
    }

    if (showAddTenantDialog) {
        AddTenantDialog(
            availableRooms = rooms.map { it.number },
            onDismiss = { showAddTenantDialog = false },
            onConfirm = { name, room, phone, email, status, joinDate ->
                viewModel.addTenant(name, room, phone, email, status, joinDate)
                showAddTenantDialog = false
            }
        )
    }

    editingTenant?.let { tenant ->
        EditTenantDialog(
            tenant = tenant,
            availableRooms = rooms.map { it.number },
            onDismiss = { editingTenant = null },
            onConfirm = { updatedTenant ->
                viewModel.updateTenant(updatedTenant)
                editingTenant = null
            }
        )
    }

    selectedTenantDetails?.let { tenant ->
        TenantDetailDialog(
            tenant = tenant,
            viewModel = viewModel,
            onDismiss = { selectedTenantDetails = null },
            onRecordPayment = {
                selectedTenantForNewPayment = tenant
            }
        )
    }

    selectedTenantForNewPayment?.let { tenant ->
        AddTenantPaymentDialog(
            tenant = tenant,
            onDismiss = { selectedTenantForNewPayment = null },
            onConfirm = { amount, dueDate, status, method, type, paidDate, txnId, notes ->
                viewModel.addPayment(
                    tenant = tenant.name,
                    room = tenant.room,
                    amount = amount,
                    dueDate = dueDate,
                    status = status,
                    paymentMethod = method,
                    paymentType = type,
                    paidDate = paidDate,
                    transactionId = txnId,
                    notes = notes
                )
                selectedTenantForNewPayment = null
            }
        )
    }
}

@Composable
fun TenantCard(
    tenant: TenantEntity,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("tenant_card_${tenant.id}"),
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
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Initial Avatar
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = tenant.name.take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = tenant.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        TenantStatusBadge(status = tenant.status)
                    }

                    Text(
                        text = "Room ${tenant.room} • Joined ${tenant.joinDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "${tenant.phone} • ${tenant.email}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.testTag("edit_tenant_button_${tenant.id}")
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Tenant",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "Delete Tenant",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun TenantStatusBadge(status: String) {
    val (bgColor, textColor) = when (status.lowercase()) {
        "active" -> Color(0xFFDCFCE7) to Color(0xFF16A34A)
        "notice period" -> Color(0xFFFFEDD5) to Color(0xFFEA580C)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantDetailDialog(
    tenant: TenantEntity,
    viewModel: PGViewModel,
    onDismiss: () -> Unit,
    onRecordPayment: () -> Unit
) {
    val payments by viewModel.payments.collectAsState()

    val tenantPayments = remember(payments, tenant) {
        payments.filter { payment ->
            (payment.tenantId != 0 && payment.tenantId == tenant.id) ||
            payment.tenant.equals(tenant.name, ignoreCase = true) ||
            (payment.tenant.contains(tenant.name, ignoreCase = true) && payment.room == tenant.room)
        }
    }

    var paymentFilter by remember { mutableStateOf<String?>(null) }

    val filteredHistory = remember(tenantPayments, paymentFilter) {
        if (paymentFilter == null) tenantPayments
        else tenantPayments.filter { it.status.equals(paymentFilter, ignoreCase = true) }
    }

    val totalPaid = remember(tenantPayments) {
        tenantPayments.filter { it.status.equals("Paid", ignoreCase = true) }.sumOf { it.amount }
    }
    val totalPending = remember(tenantPayments) {
        tenantPayments.filter { !it.status.equals("Paid", ignoreCase = true) }.sumOf { it.amount }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("tenant_detail_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tenant Payment Profile",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Profile Header Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = CircleShape,
                                        modifier = Modifier.size(52.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = tenant.name.take(1).uppercase(),
                                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = tenant.name,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            TenantStatusBadge(status = tenant.status)
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Surface(
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "Room ${tenant.room}",
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            Text(
                                                text = "Joined ${tenant.joinDate}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(text = tenant.phone, style = MaterialTheme.typography.bodySmall)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(text = tenant.email, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }

                    // Payment Financial Summary Cards
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Total Paid",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF16A34A)
                                    )
                                    Text(
                                        text = "₹${totalPaid.toInt()}",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF15803D)
                                    )
                                }
                            }

                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (totalPending > 0) Color(0xFFFFEDD5) else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Pending / Due",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (totalPending > 0) Color(0xFFEA580C) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "₹${totalPending.toInt()}",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (totalPending > 0) Color(0xFFC2410C) else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // Payment History Header & Filter Chips
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Payment History (${tenantPayments.size})",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = paymentFilter == null,
                                    onClick = { paymentFilter = null },
                                    label = { Text("All") }
                                )
                                FilterChip(
                                    selected = paymentFilter == "Paid",
                                    onClick = { paymentFilter = if (paymentFilter == "Paid") null else "Paid" },
                                    label = { Text("Paid") }
                                )
                                FilterChip(
                                    selected = paymentFilter == "Pending",
                                    onClick = { paymentFilter = if (paymentFilter == "Pending") null else "Pending" },
                                    label = { Text("Pending") }
                                )
                            }
                        }
                    }

                    // Payment History Items
                    if (filteredHistory.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ReceiptLong,
                                        contentDescription = null,
                                        modifier = Modifier.size(36.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                    Text(
                                        text = "No payment records for ${tenant.name}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(filteredHistory) { payment ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = payment.paymentType,
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                            )
                                            PaymentStatusBadge(status = payment.status)
                                        }

                                        Text(
                                            text = "Due: ${payment.dueDate}" + (payment.paidDate?.let { " • Paid: $it" } ?: ""),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        Text(
                                            text = "Method: ${payment.paymentMethod}" + if (payment.receiptNumber.isNotEmpty()) " • Receipt: ${payment.receiptNumber}" else "",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.End,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "₹${payment.amount.toInt()}",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (payment.status.equals("Paid", ignoreCase = true)) Color(0xFF16A34A) else Color(0xFFDC2626)
                                        )

                                        if (!payment.status.equals("Paid", ignoreCase = true)) {
                                            Button(
                                                onClick = {
                                                    viewModel.updatePayment(payment.copy(status = "Paid", paidDate = "2026-08-09"))
                                                },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.height(28.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                                            ) {
                                                Text("Mark Paid", style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Action Button
                Button(
                    onClick = onRecordPayment,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("record_tenant_payment_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Record Payment for ${tenant.name}")
                }
            }
        }
    }
}

@Composable
fun AddTenantDialog(
    availableRooms: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, room: String, phone: String, email: String, status: String, joinDate: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var room by remember { mutableStateOf(availableRooms.firstOrNull() ?: "101") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Active") }
    var joinDate by remember { mutableStateOf("2026-08-09") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Tenant") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth().testTag("add_tenant_name_input")
                )
                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    label = { Text("Assigned Room Number") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = joinDate,
                    onValueChange = { joinDate = it },
                    label = { Text("Join Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, room, phone, email, status, joinDate)
                    }
                },
                modifier = Modifier.testTag("confirm_add_tenant")
            ) {
                Text("Save Tenant")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddTenantPaymentDialog(
    tenant: TenantEntity,
    onDismiss: () -> Unit,
    onConfirm: (
        amount: Double, dueDate: String, status: String,
        method: String, type: String, paidDate: String?,
        txnId: String, notes: String
    ) -> Unit
) {
    var amount by remember { mutableStateOf("12000") }
    var dueDate by remember { mutableStateOf("2026-08-15") }
    var status by remember { mutableStateOf("Paid") }
    var method by remember { mutableStateOf("UPI") }
    var type by remember { mutableStateOf("Rent") }
    var paidDate by remember { mutableStateOf("2026-08-09") }
    var txnId by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Payment for ${tenant.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = tenant.name,
                    onValueChange = {},
                    enabled = false,
                    label = { Text("Tenant Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = tenant.room,
                    onValueChange = {},
                    enabled = false,
                    label = { Text("Room") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("tenant_payment_amount_input")
                )
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Due Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = status == "Paid",
                        onClick = { status = "Paid" },
                        label = { Text("Paid") }
                    )
                    FilterChip(
                        selected = status == "Pending",
                        onClick = { status = "Pending" },
                        label = { Text("Pending") }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Rent", "Security Deposit", "Maintenance").forEach { paymentType ->
                        FilterChip(
                            selected = type == paymentType,
                            onClick = { type = paymentType },
                            label = { Text(paymentType) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountVal = amount.toDoubleOrNull() ?: 0.0
                    if (amountVal > 0) {
                        onConfirm(
                            amountVal, dueDate, status, method, type,
                            if (status == "Paid") paidDate else null,
                            txnId, notes
                        )
                    }
                },
                modifier = Modifier.testTag("confirm_tenant_payment")
            ) {
                Text("Save Payment Record")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun EditTenantDialog(
    tenant: TenantEntity,
    availableRooms: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (TenantEntity) -> Unit
) {
    var name by remember { mutableStateOf(tenant.name) }
    var room by remember { mutableStateOf(tenant.room) }
    var phone by remember { mutableStateOf(tenant.phone) }
    var email by remember { mutableStateOf(tenant.email) }
    var status by remember { mutableStateOf(tenant.status) }
    var joinDate by remember { mutableStateOf(tenant.joinDate) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Tenant Information") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth().testTag("edit_tenant_name_input")
                )
                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    label = { Text("Assigned Room Number") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = joinDate,
                    onValueChange = { joinDate = it },
                    label = { Text("Join Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Tenancy Status:", style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Active", "Notice Period", "Vacated").forEach { st ->
                        FilterChip(
                            selected = status == st,
                            onClick = { status = st },
                            label = { Text(st) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(
                            tenant.copy(
                                name = name,
                                room = room,
                                phone = phone,
                                email = email,
                                status = status,
                                joinDate = joinDate
                            )
                        )
                    }
                },
                modifier = Modifier.testTag("confirm_edit_tenant")
            ) {
                Text("Update Tenant")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

