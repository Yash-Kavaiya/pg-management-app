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
import com.example.pgmanagerpro.data.local.entities.PaymentEntity
import com.example.pgmanagerpro.ui.PGViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsScreen(viewModel: PGViewModel) {
    val payments by viewModel.payments.collectAsState()
    val tenants by viewModel.tenants.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf<String?>(null) }
    var showAddPaymentDialog by remember { mutableStateOf(false) }
    var selectedPaymentForReceipt by remember { mutableStateOf<PaymentEntity?>(null) }

    val filteredPayments = remember(payments, searchQuery, selectedStatusFilter) {
        payments.filter { payment ->
            val matchesSearch = searchQuery.isEmpty() ||
                    payment.tenant.contains(searchQuery, ignoreCase = true) ||
                    payment.room.contains(searchQuery, ignoreCase = true) ||
                    payment.receiptNumber.contains(searchQuery, ignoreCase = true)

            val matchesStatus = selectedStatusFilter == null ||
                    payment.status.equals(selectedStatusFilter, ignoreCase = true)

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
                text = "Rent & Payments Ledger",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by tenant, room or receipt #...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().testTag("payment_search_input"),
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
                listOf("Paid", "Pending", "Overdue").forEach { status ->
                    val isSelected = selectedStatusFilter == status
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedStatusFilter = if (isSelected) null else status },
                        label = { Text(status) }
                    )
                }
            }

            // List
            if (filteredPayments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No payment records found.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredPayments) { payment ->
                        PaymentCard(
                            payment = payment,
                            onClick = { selectedPaymentForReceipt = payment },
                            onDelete = { viewModel.deletePayment(payment.id) }
                        )
                    }
                }
            }
        }

        // Record Payment FAB
        FloatingActionButton(
            onClick = { showAddPaymentDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_payment_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Record Payment")
        }
    }

    if (showAddPaymentDialog) {
        AddPaymentDialog(
            tenantList = tenants.map { "${it.name} (Room ${it.room})" },
            onDismiss = { showAddPaymentDialog = false },
            onConfirm = { tenant, room, amount, dueDate, status, method, type, paidDate, txnId, notes ->
                viewModel.addPayment(
                    tenant, room, amount, dueDate, status, method, type, paidDate, txnId, notes
                )
                showAddPaymentDialog = false
            }
        )
    }

    selectedPaymentForReceipt?.let { payment ->
        PaymentReceiptDialog(
            payment = payment,
            onDismiss = { selectedPaymentForReceipt = null }
        )
    }
}

@Composable
fun PaymentCard(payment: PaymentEntity, onClick: () -> Unit, onDelete: () -> Unit) {
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
                        text = payment.tenant,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    PaymentStatusBadge(status = payment.status)
                }

                Text(
                    text = "Room ${payment.room} • ${payment.paymentType} • Due ${payment.dueDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (!payment.receiptNumber.isNullOrEmpty()) {
                    Text(
                        text = "Receipt: ${payment.receiptNumber} • Method: ${payment.paymentMethod}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${payment.amount.toInt()}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (payment.status == "Paid") Color(0xFF16A34A) else Color(0xFFDC2626)
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun PaymentStatusBadge(status: String) {
    val (bgColor, textColor) = when (status.lowercase()) {
        "paid" -> Color(0xFFDCFCE7) to Color(0xFF16A34A)
        "pending" -> Color(0xFFFFEDD5) to Color(0xFFEA580C)
        "overdue" -> Color(0xFFFEE2E2) to Color(0xFFDC2626)
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
fun AddPaymentDialog(
    tenantList: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (
        tenant: String, room: String, amount: Double, dueDate: String,
        status: String, method: String, type: String, paidDate: String?,
        txnId: String, notes: String
    ) -> Unit
) {
    var tenantName by remember { mutableStateOf("") }
    var roomNumber by remember { mutableStateOf("101") }
    var amount by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("2026-08-05") }
    var status by remember { mutableStateOf("Paid") }
    var paymentMethod by remember { mutableStateOf("UPI") }
    var paymentType by remember { mutableStateOf("Rent") }
    var paidDate by remember { mutableStateOf("2026-08-05") }
    var transactionId by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Payment") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = tenantName,
                    onValueChange = { tenantName = it },
                    label = { Text("Tenant Name") },
                    modifier = Modifier.fillMaxWidth().testTag("add_payment_tenant_input")
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = roomNumber,
                        onValueChange = { roomNumber = it },
                        label = { Text("Room #") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Amount (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = status,
                        onValueChange = { status = it },
                        label = { Text("Status (Paid, Pending, Overdue)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = paymentMethod,
                        onValueChange = { paymentMethod = it },
                        label = { Text("Method (UPI, Cash, Bank)") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = transactionId,
                    onValueChange = { transactionId = it },
                    label = { Text("Transaction ID (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (e.g. August Rent)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (tenantName.isNotBlank() && amount.isNotBlank()) {
                        onConfirm(
                            tenantName, roomNumber, amount.toDoubleOrNull() ?: 0.0,
                            dueDate, status, paymentMethod, paymentType,
                            if (status == "Paid") paidDate else null,
                            transactionId, notes
                        )
                    }
                },
                modifier = Modifier.testTag("confirm_add_payment")
            ) {
                Text("Save Payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun PaymentReceiptDialog(payment: PaymentEntity, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Receipt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Receipt Details")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Receipt #: ${payment.receiptNumber.ifEmpty { "N/A" }}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Divider(modifier = Modifier.padding(vertical = 4.dp))
                Text("Tenant: ${payment.tenant}", style = MaterialTheme.typography.bodyMedium)
                Text("Room Number: ${payment.room}", style = MaterialTheme.typography.bodyMedium)
                Text("Amount Paid: ₹${payment.amount.toInt()}", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = Color(0xFF16A34A)))
                Text("Due Date: ${payment.dueDate}", style = MaterialTheme.typography.bodyMedium)
                Text("Paid Date: ${payment.paidDate ?: "Not Paid Yet"}", style = MaterialTheme.typography.bodyMedium)
                Text("Payment Method: ${payment.paymentMethod}", style = MaterialTheme.typography.bodyMedium)
                Text("Transaction ID: ${payment.transactionId.ifEmpty { "N/A" }}", style = MaterialTheme.typography.bodyMedium)
                if (payment.notes.isNotEmpty()) {
                    Text("Notes: ${payment.notes}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Close") }
        }
    )
}
