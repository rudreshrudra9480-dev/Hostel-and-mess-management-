package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.*

/**
 * Screen displaying the student's room allotment details, roommates, amenities,
 * warden contacts, and an interactive form to submit room / transfer requests.
 */
@Composable
fun StudentRoomAllotmentScreen(viewModel: HostelViewModel) {
    val authState by viewModel.authState.collectAsState()
    val student = authState.loggedInUser ?: return
    val allUsers by viewModel.users.collectAsState()
    val allRequests by viewModel.roomRequests.collectAsState()
    val myRequests = allRequests.filter { it.studentId == student.studentId }

    // Find roommates living in the same room
    val roommates = allUsers.filter {
        it.role == UserRole.STUDENT &&
        it.roomNumber.isNotBlank() &&
        it.roomNumber.equals(student.roomNumber, ignoreCase = true) &&
        it.studentId != student.studentId
    }

    var showRequestDialog by remember { mutableStateOf(false) }
    var successNotice by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header & Quick Action
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Room & Allotment Details",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Your campus residence, amenities, and room change requests.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showRequestDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("request_room_btn")
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Request Room", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (successNotice != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = Color(0xFFD1FAE5),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(successNotice ?: "", color = Color(0xFF065F46), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Primary Room Allotment Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.MeetingRoom, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ALLOTTED RESIDENCE", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Surface(
                            color = Color(0xFF059669),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "ACTIVE & CONFIRMED",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("ROOM NUMBER", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(
                                student.roomNumber.ifBlank { "Not Allocated" },
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                student.blockName.ifBlank { "Campus Main Block" },
                                color = Color(0xFFCBD5E1),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Surface(
                                color = Color(0xFF1E293B),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    student.roomType,
                                    color = Color(0xFFF1F5F9),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Assigned: ${student.bedNumber.ifBlank { "Bed 1" }}",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFF334155))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Allotment Metadata Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("CHECK-IN DATE", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(student.checkInDate, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Column {
                            Text("OCCUPANCY", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("${roommates.size + 1} Residents", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Column {
                            Text("FLOOR", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            val floorNum = student.roomNumber.filter { it.isDigit() }.firstOrNull() ?: '2'
                            Text("Floor $floorNum", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Room Amenities Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Furnishing & Included Amenities", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    val amenities = listOf(
                        "High-Speed Campus Wi-Fi",
                        "Dedicated Ergonomic Study Table",
                        "Steel Lockable Wardrobe",
                        "Attached Private Washroom",
                        "Ceiling Fan & Air Circulation",
                        "24/7 Generator Power Backup",
                        "Daily Housekeeping Sweep"
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(amenities) { amenity ->
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(8.dp),
                                border = CardDefaults.outlinedCardBorder()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(amenity, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Roommates in this Room
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Group, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Roommates in ${student.roomNumber}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(6.dp)) {
                            Text("${roommates.size} Co-Residents", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (roommates.isEmpty()) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("No other student currently allotted in this room.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            roommates.forEach { roommate ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primary),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(roommate.name.take(2).uppercase(), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(roommate.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text("Roll: ${roommate.studentId} • ${roommate.bedNumber.ifBlank { "Bed 2" }}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }

                                        Surface(color = Color(0xFFD1FAE5), shape = RoundedCornerShape(6.dp)) {
                                            Text("Active", color = Color(0xFF065F46), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Incharge Warden Contact & Curfew Info
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Residence Administration & Curfew", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Chief Block Warden", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Dr. Rajesh Sharma", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("+91 98765 43210 • warden@hostel.edu", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Surface(color = Color(0xFFFEF3C7), shape = RoundedCornerShape(8.dp)) {
                            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Curfew", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                                Text("10:00 PM", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFB45309))
                            }
                        }
                    }
                }
            }
        }

        // Room Request History Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("My Room Requests & Status", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("${myRequests.size} Logged", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        if (myRequests.isEmpty()) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.AssignmentLate, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No Room Requests Submitted", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("You can apply for a room change, AC upgrade, or transfer anytime.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    }
                }
            }
        } else {
            items(myRequests) { req ->
                RoomRequestHistoryCard(req)
            }
        }
    }

    // Modal Dialog: Request for Room / Change
    if (showRequestDialog) {
        RoomRequestSubmissionDialog(
            currentRoom = student.roomNumber,
            onDismiss = { showRequestDialog = false },
            onSubmit = { type, block, roomType, floor, reason, notes ->
                viewModel.submitStudentRoomRequest(
                    requestType = type,
                    preferredBlock = block,
                    preferredRoomType = roomType,
                    preferredFloor = floor,
                    reason = reason,
                    specialNotes = notes
                )
                showRequestDialog = false
                successNotice = "Room request ($type) submitted successfully to Warden Office!"
            }
        )
    }
}

@Composable
fun RoomRequestHistoryCard(req: RoomRequest) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(req.requestType, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Ref: ${req.id} • Date: ${req.requestDate}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                val (chipBg, chipText) = when (req.status) {
                    RoomRequestStatus.PENDING -> Color(0xFFFEF3C7) to Color(0xFFD97706)
                    RoomRequestStatus.APPROVED -> Color(0xFFE0F2FE) to Color(0xFF0284C7)
                    RoomRequestStatus.ALLOCATED -> Color(0xFFD1FAE5) to Color(0xFF059669)
                    RoomRequestStatus.REJECTED -> Color(0xFFFEE2E2) to Color(0xFFDC2626)
                }

                Surface(color = chipBg, shape = RoundedCornerShape(6.dp)) {
                    Text(
                        req.status.displayName,
                        color = chipText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Preference:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("${req.preferredBlock} • ${req.preferredRoomType}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Text("Reason: ${req.reason}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (req.specialNotes.isNotBlank()) {
                        Text("Notes: ${req.specialNotes}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            if (!req.wardenRemarks.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color(0xFFECFDF5),
                    shape = RoundedCornerShape(8.dp),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.RateReview, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("Warden Remarks & Action:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                            Text(req.wardenRemarks, fontSize = 11.sp, color = Color(0xFF047857))
                            if (!req.allocatedRoom.isNullOrBlank()) {
                                Text("Allocated Room: ${req.allocatedRoom}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomRequestSubmissionDialog(
    currentRoom: String,
    onDismiss: () -> Unit,
    onSubmit: (type: String, block: String, roomType: String, floor: String, reason: String, notes: String) -> Unit
) {
    var requestType by remember { mutableStateOf("Room Change / Transfer") }
    var preferredBlock by remember { mutableStateOf("Block A (Aryabhata)") }
    var preferredRoomType by remember { mutableStateOf("Double Sharing AC") }
    var preferredFloor by remember { mutableStateOf("2nd Floor") }
    var reason by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val requestTypes = listOf(
        "Room Change / Transfer",
        "Upgrade to AC Room",
        "New Room Allocation",
        "Mutual Room Swap",
        "Single Occupancy Special"
    )

    val blocks = listOf("Block A (Aryabhata)", "Block B (Kalpana Chawla)", "Block C (Ramanujan)")
    val roomTypes = listOf("Double Sharing AC", "Single Occupancy AC", "Double Sharing Non-AC", "Triple Sharing")
    val floors = listOf("Ground Floor", "1st Floor", "2nd Floor", "3rd Floor")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Request Room / Transfer", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                Text("Current Allotment: ${currentRoom.ifBlank { "Unassigned" }}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)

                Spacer(modifier = Modifier.height(12.dp))

                Text("Request Type:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(requestTypes) { type ->
                        FilterChip(
                            selected = type == requestType,
                            onClick = { requestType = type },
                            label = { Text(type, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("Preferred Block:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(blocks) { blk ->
                        FilterChip(
                            selected = blk == preferredBlock,
                            onClick = { preferredBlock = blk },
                            label = { Text(blk.substringBefore(" "), fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("Preferred Room Category:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(roomTypes) { rType ->
                        FilterChip(
                            selected = rType == preferredRoomType,
                            onClick = { preferredRoomType = rType },
                            label = { Text(rType, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason for Request *") },
                    placeholder = { Text("e.g. Medical, need quiet environment, AC upgrade...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Special Preferences / Roommate Swap Roll No") },
                    placeholder = { Text("Optional details for warden review...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (reason.isNotBlank()) {
                            onSubmit(requestType, preferredBlock, preferredRoomType, preferredFloor, reason, notes)
                        }
                    },
                    enabled = reason.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submit_room_request_btn")
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Submit Application to Warden")
                }
            }
        }
    }
}
