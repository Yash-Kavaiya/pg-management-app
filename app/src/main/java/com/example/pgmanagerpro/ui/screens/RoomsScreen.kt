package com.example.pgmanagerpro.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pgmanagerpro.data.local.entities.RoomEntity
import com.example.pgmanagerpro.data.local.entities.TenantEntity
import com.example.pgmanagerpro.ui.PGViewModel
import com.example.pgmanagerpro.ui.theme.AccenturePurple
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomsScreen(viewModel: PGViewModel) {
    val rooms by viewModel.rooms.collectAsState()
    val tenants by viewModel.tenants.collectAsState()

    var selectedViewMode by remember { mutableStateOf(1) } // Default 1: 3D Interactive Floor Map
    var selectedFloor by remember { mutableStateOf<Int?>(null) }
    var selectedStatus by remember { mutableStateOf<String?>(null) }
    var showAddRoomDialog by remember { mutableStateOf(false) }
    var editingRoom by remember { mutableStateOf<RoomEntity?>(null) }
    var deletingRoomId by remember { mutableStateOf<Int?>(null) }
    var inspectedRoom by remember { mutableStateOf<RoomEntity?>(null) }

    val floors = remember(rooms) { rooms.map { it.floor }.distinct().sorted() }

    val filteredRooms = remember(rooms, selectedFloor, selectedStatus) {
        rooms.filter { room ->
            (selectedFloor == null || room.floor == selectedFloor) &&
            (selectedStatus == null || room.status.equals(selectedStatus, ignoreCase = true))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header & View Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "3D Room & Building Layout",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${rooms.size} Total Rooms • ${rooms.count { it.status.equals("Available", true) }} Available",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                SingleChoiceSegmentedButtonRow {
                    SegmentedButton(
                        selected = selectedViewMode == 1,
                        onClick = { selectedViewMode = 1 },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.ViewInAr, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("3D Floor Map")
                        }
                    }
                    SegmentedButton(
                        selected = selectedViewMode == 0,
                        onClick = { selectedViewMode = 0 },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("List")
                        }
                    }
                }
            }

            // Filters Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = selectedFloor == null,
                    onClick = { selectedFloor = null },
                    label = { Text("All Floors") }
                )
                floors.forEach { floor ->
                    FilterChip(
                        selected = selectedFloor == floor,
                        onClick = { selectedFloor = if (selectedFloor == floor) null else floor },
                        label = { Text("Floor $floor") }
                    )
                }
            }

            // Status Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Occupied", "Available", "Maintenance").forEach { status ->
                    val isSelected = selectedStatus == status
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedStatus = if (isSelected) null else status },
                        label = { Text(status) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(getRoomStatusColor(status), CircleShape)
                            )
                        }
                    )
                }
            }

            // Content
            if (selectedViewMode == 0) {
                // List View
                if (filteredRooms.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Text(
                            text = "No rooms match the selected filters.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredRooms) { room ->
                            val activeOccupants = tenants.count { it.room == room.number && it.status.equals("Active", true) }
                            RoomCard(
                                room = room,
                                occupantCount = activeOccupants,
                                onEdit = { editingRoom = room },
                                onDelete = { deletingRoomId = room.id }
                            )
                        }
                    }
                }
            } else {
                // Interactive 3D Architectural Floor Map Visualizer
                Interactive3DFloorPlanVisualizer(
                    rooms = filteredRooms,
                    allRooms = rooms,
                    tenants = tenants,
                    selectedFloor = selectedFloor,
                    onSelectFloor = { selectedFloor = it },
                    onRoomClick = { room -> inspectedRoom = room }
                )
            }
        }

        // Add Room FAB
        FloatingActionButton(
            onClick = { showAddRoomDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_room_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Room")
        }
    }

    if (showAddRoomDialog) {
        AddRoomDialog(
            onDismiss = { showAddRoomDialog = false },
            onConfirm = { number, type, rent, deposit, floor, status ->
                viewModel.addRoom(number, type, rent, deposit, floor, status)
                showAddRoomDialog = false
            }
        )
    }

    editingRoom?.let { room ->
        EditRoomDialog(
            room = room,
            onDismiss = { editingRoom = null },
            onConfirm = { updatedRoom ->
                viewModel.updateRoom(updatedRoom)
                editingRoom = null
            }
        )
    }

    inspectedRoom?.let { room ->
        val roomTenants = tenants.filter { it.room == room.number && it.status.equals("Active", true) }
        RoomInspectorBottomSheet(
            room = room,
            tenants = roomTenants,
            onDismiss = { inspectedRoom = null },
            onEdit = {
                editingRoom = room
                inspectedRoom = null
            },
            onDelete = {
                deletingRoomId = room.id
                inspectedRoom = null
            }
        )
    }

    deletingRoomId?.let { roomId ->
        AlertDialog(
            onDismissRequest = { deletingRoomId = null },
            title = { Text("Delete Room") },
            text = { Text("Are you sure you want to delete this room? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteRoom(roomId)
                        deletingRoomId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingRoomId = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun RoomCard(
    room: RoomEntity,
    occupantCount: Int = 0,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
            .testTag("room_card_${room.id}"),
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
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Room ${room.number}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Surface(
                        color = getRoomStatusColor(room.status).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = room.status,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = getRoomStatusColor(room.status)
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${room.type} Sharing • Floor ${room.floor}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (occupantCount > 0) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        ) {
                            Text(
                                text = "$occupantCount Active Occupant${if (occupantCount > 1) "s" else ""}",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Text(
                    text = "Rent: ₹${room.rent.toInt()}/mo • Deposit: ₹${room.deposit.toInt()}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.testTag("edit_room_button_${room.id}")
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Room Details",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_room_button_${room.id}")
                ) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "Delete Room",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun RoomTileBlueprint(
    room: RoomEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = getRoomStatusColor(room.status)
    Card(
        modifier = modifier
            .aspectRatio(0.9f)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(2.dp, statusColor.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = statusColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = room.status,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = statusColor
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = room.number,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = room.type,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "₹${room.rent.toInt()}",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun AddRoomDialog(
    onDismiss: () -> Unit,
    onConfirm: (number: String, type: String, rent: Double, deposit: Double, floor: Int, status: String) -> Unit
) {
    var number by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Single") }
    var rent by remember { mutableStateOf("10000") }
    var deposit by remember { mutableStateOf("20000") }
    var floor by remember { mutableStateOf("1") }
    var status by remember { mutableStateOf("Available") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Room") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = number,
                    onValueChange = { number = it },
                    label = { Text("Room Number (e.g. 101)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_room_number_input")
                )

                Text(
                    text = "Room Capacity / Sharing Type",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Single", "Double", "Triple", "Quad").forEach { sharingType ->
                        FilterChip(
                            selected = type.equals(sharingType, ignoreCase = true),
                            onClick = { type = sharingType },
                            label = { Text(sharingType) }
                        )
                    }
                }

                OutlinedTextField(
                    value = rent,
                    onValueChange = { rent = it },
                    label = { Text("Monthly Rent Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_room_rent_input")
                )
                OutlinedTextField(
                    value = deposit,
                    onValueChange = { deposit = it },
                    label = { Text("Security Deposit (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = floor,
                    onValueChange = { floor = it },
                    label = { Text("Floor Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Current Availability Status",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Available", "Occupied", "Maintenance").forEach { roomStatus ->
                        FilterChip(
                            selected = status.equals(roomStatus, ignoreCase = true),
                            onClick = { status = roomStatus },
                            label = { Text(roomStatus) },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(getRoomStatusColor(roomStatus), CircleShape)
                                )
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rentVal = rent.toDoubleOrNull() ?: 0.0
                    val depVal = deposit.toDoubleOrNull() ?: 0.0
                    val floorVal = floor.toIntOrNull() ?: 1
                    if (number.isNotBlank()) {
                        onConfirm(number, type, rentVal, depVal, floorVal, status)
                    }
                },
                modifier = Modifier.testTag("confirm_add_room")
            ) {
                Text("Add Room")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditRoomDialog(
    room: RoomEntity,
    onDismiss: () -> Unit,
    onConfirm: (updatedRoom: RoomEntity) -> Unit
) {
    var number by remember(room) { mutableStateOf(room.number) }
    var type by remember(room) { mutableStateOf(room.type) }
    var rent by remember(room) { mutableStateOf(room.rent.toInt().toString()) }
    var deposit by remember(room) { mutableStateOf(room.deposit.toInt().toString()) }
    var floor by remember(room) { mutableStateOf(room.floor.toString()) }
    var status by remember(room) { mutableStateOf(room.status) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Room Details (Room ${room.number})") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = number,
                    onValueChange = { number = it },
                    label = { Text("Room Number") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_room_number_input")
                )

                Text(
                    text = "Capacity / Sharing Type",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Single", "Double", "Triple", "Quad").forEach { sharingType ->
                        FilterChip(
                            selected = type.equals(sharingType, ignoreCase = true),
                            onClick = { type = sharingType },
                            label = { Text(sharingType) }
                        )
                    }
                }

                OutlinedTextField(
                    value = rent,
                    onValueChange = { rent = it },
                    label = { Text("Monthly Rent Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_room_rent_input")
                )

                OutlinedTextField(
                    value = deposit,
                    onValueChange = { deposit = it },
                    label = { Text("Security Deposit (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = floor,
                    onValueChange = { floor = it },
                    label = { Text("Floor Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Current Availability Status",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Available", "Occupied", "Maintenance").forEach { roomStatus ->
                        FilterChip(
                            selected = status.equals(roomStatus, ignoreCase = true),
                            onClick = { status = roomStatus },
                            label = { Text(roomStatus) },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(getRoomStatusColor(roomStatus), CircleShape)
                                )
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rentVal = rent.toDoubleOrNull() ?: room.rent
                    val depVal = deposit.toDoubleOrNull() ?: room.deposit
                    val floorVal = floor.toIntOrNull() ?: room.floor
                    val updated = room.copy(
                        number = number,
                        type = type,
                        rent = rentVal,
                        deposit = depVal,
                        floor = floorVal,
                        status = status
                    )
                    onConfirm(updated)
                },
                modifier = Modifier.testTag("confirm_edit_room")
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun getRoomStatusColor(status: String): Color {
    return when (status.lowercase()) {
        "occupied" -> Color(0xFF4F46E5)
        "available" -> Color(0xFF16A34A)
        "maintenance" -> Color(0xFFEA580C)
        else -> Color.Gray
    }
}

@Composable
fun Interactive3DFloorPlanVisualizer(
    rooms: List<RoomEntity>,
    allRooms: List<RoomEntity>,
    tenants: List<TenantEntity>,
    selectedFloor: Int?,
    onSelectFloor: (Int?) -> Unit,
    onRoomClick: (RoomEntity) -> Unit
) {
    var yaw by remember { mutableFloatStateOf(45f) }
    var pitch by remember { mutableFloatStateOf(35f) }
    var zoom by remember { mutableFloatStateOf(1.0f) }
    var selectedCameraPreset by remember { mutableStateOf("3D Isometric") }

    val textMeasurer = rememberTextMeasurer()
    val availableFloors = remember(allRooms) { allRooms.map { it.floor }.distinct().sorted() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Top Toolbar: Camera Presets & View Controls
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.ViewInAr, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("3D Architectural Viewport", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    }

                    Surface(
                        color = AccenturePurple.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = "Drag canvas to orbit 3D camera",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = AccenturePurple
                        )
                    }
                }

                // Camera Presets
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val presets = listOf("3D Isometric", "Top-Down Plan", "Side Elevation", "3D Building Stack")
                    items(presets) { preset ->
                        FilterChip(
                            selected = selectedCameraPreset == preset,
                            onClick = {
                                selectedCameraPreset = preset
                                when (preset) {
                                    "3D Isometric" -> { yaw = 45f; pitch = 35f; zoom = 1.0f }
                                    "Top-Down Plan" -> { yaw = 0f; pitch = 85f; zoom = 1.0f }
                                    "Side Elevation" -> { yaw = 0f; pitch = 10f; zoom = 1.1f }
                                    "3D Building Stack" -> { yaw = 30f; pitch = 40f; zoom = 0.85f; onSelectFloor(null) }
                                }
                            },
                            label = { Text(preset, fontSize = 11.sp) },
                            leadingIcon = {
                                when (preset) {
                                    "3D Isometric" -> Icon(Icons.Default.ViewInAr, contentDescription = null, modifier = Modifier.size(14.dp))
                                    "Top-Down Plan" -> Icon(Icons.Default.CropLandscape, contentDescription = null, modifier = Modifier.size(14.dp))
                                    "Side Elevation" -> Icon(Icons.Default.BorderOuter, contentDescription = null, modifier = Modifier.size(14.dp))
                                    else -> Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(14.dp))
                                }
                            }
                        )
                    }
                }
            }
        }

        // Main 3D Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0F111A)) // Dark Architectural Viewport Canvas
                .border(1.dp, Color(0xFF2A2D3D), RoundedCornerShape(12.dp))
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoomFactor, _ ->
                        yaw = (yaw + pan.x * 0.4f).coerceIn(-180f, 180f)
                        pitch = (pitch - pan.y * 0.3f).coerceIn(10f, 85f)
                        zoom = (zoom * zoomFactor).coerceIn(0.6f, 2.2f)
                    }
                }
                .pointerInput(rooms, selectedFloor, selectedCameraPreset, yaw, pitch, zoom) {
                    detectTapGestures { tapOffset ->
                        val canvasW = size.width.toFloat()
                        val canvasH = size.height.toFloat()
                        val centerX = canvasW / 2f
                        val centerY = canvasH / 2f + 20.dp.toPx()
                        val radYaw = Math.toRadians(yaw.toDouble())
                        val radPitch = Math.toRadians(pitch.toDouble())

                        val activeDisplayFloors = if (selectedFloor != null && selectedCameraPreset != "3D Building Stack") {
                            listOf(selectedFloor)
                        } else {
                            availableFloors
                        }

                        var closestRoom: RoomEntity? = null
                        var minDistance = Float.MAX_VALUE

                        activeDisplayFloors.forEachIndexed { floorIndex, fl ->
                            val floorRooms = rooms.filter { it.floor == fl }
                            val floorVerticalOffset = if (selectedCameraPreset == "3D Building Stack" || selectedFloor == null) {
                                (floorIndex - (activeDisplayFloors.size - 1) / 2f) * 110.dp.toPx()
                            } else {
                                0f
                            }
                            val slabW = 280.dp.toPx() * zoom
                            val slabH = 180.dp.toPx() * zoom
                            val roomCols = 3
                            val roomBoxW = 75.dp.toPx()
                            val roomBoxH = 65.dp.toPx()
                            val roomWallH = 28.dp.toPx()

                            floorRooms.forEachIndexed { rIndex, room ->
                                val col = rIndex % roomCols
                                val row = rIndex / roomCols
                                val rx = -slabW / 2 + 25.dp.toPx() + col * (roomBoxW + 15.dp.toPx())
                                val ry = -slabH / 2 + 20.dp.toPx() + row * (roomBoxH + 15.dp.toPx())

                                val roomCenterX = rx + roomBoxW / 2
                                val roomCenterY = ry + roomBoxH / 2
                                val roomCenterZ = roomWallH / 2

                                val px = centerX + ((roomCenterX * cos(radYaw) - roomCenterY * sin(radYaw)) * zoom).toFloat()
                                val py = centerY - floorVerticalOffset + (((roomCenterX * sin(radYaw) + roomCenterY * cos(radYaw)) * sin(radPitch) - roomCenterZ * cos(radPitch)) * zoom).toFloat()

                                val dist = (Offset(px, py) - tapOffset).getDistance()
                                if (dist < minDistance && dist < 80.dp.toPx()) {
                                    minDistance = dist
                                    closestRoom = room
                                }
                            }
                        }

                        closestRoom?.let { room ->
                            onRoomClick(room)
                        }
                    }
                }
        ) {
            // Interactive 3D Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasW = size.width
                val canvasH = size.height
                val centerX = canvasW / 2f
                val centerY = canvasH / 2f + 20.dp.toPx()

                // Render Background Grid Lines
                val gridStep = 40.dp.toPx() * zoom
                val gridColor = Color(0xFF1E2235)
                for (i in -8..8) {
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, centerY + i * gridStep * 0.5f),
                        end = Offset(canvasW, centerY + i * gridStep * 0.5f),
                        strokeWidth = 1f
                    )
                }

                // Group rooms by floor
                val activeDisplayFloors = if (selectedFloor != null && selectedCameraPreset != "3D Building Stack") {
                    listOf(selectedFloor)
                } else {
                    availableFloors
                }

                activeDisplayFloors.forEachIndexed { floorIndex, fl ->
                    val floorRooms = rooms.filter { it.floor == fl }
                    val floorVerticalOffset = if (selectedCameraPreset == "3D Building Stack" || selectedFloor == null) {
                        (floorIndex - (activeDisplayFloors.size - 1) / 2f) * 110.dp.toPx()
                    } else {
                        0f
                    }

                    // Render 3D Floor Plate Slab
                    val slabW = 280.dp.toPx() * zoom
                    val slabH = 180.dp.toPx() * zoom
                    val radYaw = Math.toRadians(yaw.toDouble())
                    val radPitch = Math.toRadians(pitch.toDouble())

                    fun project3D(x: Float, y: Float, z: Float): Offset {
                        val rx = x * cos(radYaw) - y * sin(radYaw)
                        val ry = x * sin(radYaw) + y * cos(radYaw)
                        val px = centerX + (rx * zoom).toFloat()
                        val py = centerY - floorVerticalOffset + ((ry * sin(radPitch) - z * cos(radPitch)) * zoom).toFloat()
                        return Offset(px.toFloat(), py.toFloat())
                    }

                    // Floor Slab Base
                    val p0 = project3D(-slabW / 2, -slabH / 2, 0f)
                    val p1 = project3D(slabW / 2, -slabH / 2, 0f)
                    val p2 = project3D(slabW / 2, slabH / 2, 0f)
                    val p3 = project3D(-slabW / 2, slabH / 2, 0f)

                    val slabPath = Path().apply {
                        moveTo(p0.x, p0.y)
                        lineTo(p1.x, p1.y)
                        lineTo(p2.x, p2.y)
                        lineTo(p3.x, p3.y)
                        close()
                    }

                    drawPath(
                        path = slabPath,
                        color = Color(0xFF1B1E2E).copy(alpha = 0.85f)
                    )
                    drawPath(
                        path = slabPath,
                        color = Color(0xFFA100FF).copy(alpha = 0.5f),
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Draw Floor Label Tag in 3D
                    val floorTagPos = project3D(-slabW / 2 - 10.dp.toPx(), 0f, 10.dp.toPx())
                    drawText(
                        textMeasurer = textMeasurer,
                        text = "FLOOR $fl",
                        style = TextStyle(color = Color(0xFFA100FF), fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        topLeft = floorTagPos
                    )

                    // Render Room Modules on Slab
                    val roomCols = 3
                    val roomBoxW = 75.dp.toPx()
                    val roomBoxH = 65.dp.toPx()
                    val roomWallH = 28.dp.toPx()

                    floorRooms.forEachIndexed { rIndex, room ->
                        val col = rIndex % roomCols
                        val row = rIndex / roomCols

                        val rx = -slabW / 2 + 25.dp.toPx() + col * (roomBoxW + 15.dp.toPx())
                        val ry = -slabH / 2 + 20.dp.toPx() + row * (roomBoxH + 15.dp.toPx())

                        val stColor = getRoomStatusColor(room.status)

                        // 3D Room Box Points
                        val b0 = project3D(rx, ry, 0f)
                        val b1 = project3D(rx + roomBoxW, ry, 0f)
                        val b2 = project3D(rx + roomBoxW, ry + roomBoxH, 0f)
                        val b3 = project3D(rx, ry + roomBoxH, 0f)

                        val t0 = project3D(rx, ry, roomWallH)
                        val t1 = project3D(rx + roomBoxW, ry, roomWallH)
                        val t2 = project3D(rx + roomBoxW, ry + roomBoxH, roomWallH)
                        val t3 = project3D(rx, ry + roomBoxH, roomWallH)

                        // Floor Base
                        val rFloor = Path().apply {
                            moveTo(b0.x, b0.y)
                            lineTo(b1.x, b1.y)
                            lineTo(b2.x, b2.y)
                            lineTo(b3.x, b3.y)
                            close()
                        }
                        drawPath(rFloor, color = stColor.copy(alpha = 0.25f))

                        // Walls (Left & Back Wall for isometric depth)
                        val backWall = Path().apply {
                            moveTo(b0.x, b0.y)
                            lineTo(b1.x, b1.y)
                            lineTo(t1.x, t1.y)
                            lineTo(t0.x, t0.y)
                            close()
                        }
                        val leftWall = Path().apply {
                            moveTo(b0.x, b0.y)
                            lineTo(b3.x, b3.y)
                            lineTo(t3.x, t3.y)
                            lineTo(t0.x, t0.y)
                            close()
                        }

                        drawPath(backWall, color = Color(0xFF2A2E45))
                        drawPath(backWall, color = stColor.copy(alpha = 0.4f), style = Stroke(width = 1f))

                        drawPath(leftWall, color = Color(0xFF212538))
                        drawPath(leftWall, color = stColor.copy(alpha = 0.4f), style = Stroke(width = 1f))

                        // Render 3D Furniture (Beds inside room)
                        val bedCount = when (room.type.lowercase()) {
                            "double" -> 2
                            "triple" -> 3
                            "quad" -> 4
                            else -> 1
                        }

                        for (b in 0 until bedCount) {
                            val bedOffsetX = rx + 8.dp.toPx() + (b % 2) * 30.dp.toPx()
                            val bedOffsetY = ry + 8.dp.toPx() + (b / 2) * 26.dp.toPx()

                            val bedP0 = project3D(bedOffsetX, bedOffsetY, 4.dp.toPx())
                            val bedP1 = project3D(bedOffsetX + 20.dp.toPx(), bedOffsetY, 4.dp.toPx())
                            val bedP2 = project3D(bedOffsetX + 20.dp.toPx(), bedOffsetY + 22.dp.toPx(), 4.dp.toPx())
                            val bedP3 = project3D(bedOffsetX, bedOffsetY + 22.dp.toPx(), 4.dp.toPx())

                            val bedPath = Path().apply {
                                moveTo(bedP0.x, bedP0.y)
                                lineTo(bedP1.x, bedP1.y)
                                lineTo(bedP2.x, bedP2.y)
                                lineTo(bedP3.x, bedP3.y)
                                close()
                            }
                            drawPath(bedPath, color = Color.White.copy(alpha = 0.9f))
                            drawPath(bedPath, color = AccenturePurple, style = Stroke(width = 1.dp.toPx()))

                            // Pillow
                            val pil0 = project3D(bedOffsetX + 2.dp.toPx(), bedOffsetY + 2.dp.toPx(), 6.dp.toPx())
                            val pil1 = project3D(bedOffsetX + 18.dp.toPx(), bedOffsetY + 2.dp.toPx(), 6.dp.toPx())
                            val pil2 = project3D(bedOffsetX + 18.dp.toPx(), bedOffsetY + 6.dp.toPx(), 6.dp.toPx())
                            val pil3 = project3D(bedOffsetX + 2.dp.toPx(), bedOffsetY + 6.dp.toPx(), 6.dp.toPx())
                            val pilPath = Path().apply {
                                moveTo(pil0.x, pil0.y)
                                lineTo(pil1.x, pil1.y)
                                lineTo(pil2.x, pil2.y)
                                lineTo(pil3.x, pil3.y)
                                close()
                            }
                            drawPath(pilPath, color = stColor)
                        }

                        // Outer Glowing Frame
                        drawPath(rFloor, color = stColor, style = Stroke(width = 2.dp.toPx()))

                        // 3D Room Header Badge floating above room
                        val headerPos = project3D(rx + roomBoxW / 2, ry + roomBoxH / 2, roomWallH + 12.dp.toPx())
                        drawText(
                            textMeasurer = textMeasurer,
                            text = "R-${room.number}",
                            style = TextStyle(color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold),
                            topLeft = Offset(headerPos.x - 14.dp.toPx(), headerPos.y - 8.dp.toPx())
                        )
                    }
                }
            }

            if (rooms.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0F111A).copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(Icons.Default.MeetingRoom, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                        Text("No Rooms Found", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                        Text("Tap '+' button below to add a room to this property.", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
                    }
                }
            } else if (rooms.filter { room -> (selectedFloor == null || room.floor == selectedFloor) }.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0F111A).copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(Icons.Default.Layers, contentDescription = null, tint = AccenturePurple, modifier = Modifier.size(40.dp))
                        Text("No Rooms on Floor ${selectedFloor ?: ""}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                        Text("Select 'All Floors' or tap '+' to add a room on Floor ${selectedFloor ?: 1}.", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
                    }
                }
            }

            // Legend Overlay
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp),
                color = Color(0xFF141622).copy(alpha = 0.9f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFF2D3142))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(8.dp).background(Color(0xFF4F46E5), CircleShape))
                        Text("Occupied", style = MaterialTheme.typography.labelSmall, color = Color.White)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(8.dp).background(Color(0xFF16A34A), CircleShape))
                        Text("Available", style = MaterialTheme.typography.labelSmall, color = Color.White)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(8.dp).background(Color(0xFFEA580C), CircleShape))
                        Text("Maint.", style = MaterialTheme.typography.labelSmall, color = Color.White)
                    }
                }
            }
        }

        // Room Selection Quick Row below Canvas
        Text("Tap room module to inspect specs & occupants:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(rooms) { room ->
                val statusColor = getRoomStatusColor(room.status)
                Card(
                    modifier = Modifier
                        .clickable { onRoomClick(room) }
                        .width(110.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, statusColor)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Room ${room.number}", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            Box(modifier = Modifier.size(6.dp).background(statusColor, CircleShape))
                        }
                        Text("${room.type} • Floor ${room.floor}", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₹${room.rent.toInt()}/mo", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomInspectorBottomSheet(
    room: RoomEntity,
    tenants: List<TenantEntity>,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.MeetingRoom, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Column {
                        Text("Room ${room.number}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                        Text("${room.type} Sharing • Floor ${room.floor}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Surface(
                    color = getRoomStatusColor(room.status).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = room.status,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = getRoomStatusColor(room.status)
                    )
                }
            }

            HorizontalDivider()

            // Key Specs Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Monthly Rent", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₹${room.rent.toInt()}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Security Deposit", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₹${room.deposit.toInt()}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }

            // Occupants Section
            Text("Active Occupants (${tenants.size})", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

            if (tenants.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = "No active occupants assigned to this room.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    tenants.forEach { tenant ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Icon(Icons.Default.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Column {
                                        Text(tenant.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                        Text(tenant.phone.ifEmpty { tenant.email }, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("Joined ${tenant.joinDate}", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Edit Room")
                }

                Button(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delete Room")
                }
            }
        }
    }
}

