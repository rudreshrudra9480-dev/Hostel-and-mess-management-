package com.example.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentMainContainer(
    viewModel: HostelViewModel,
    onLogout: () -> Unit
) {
    val currentTab by viewModel.studentTab.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val studentUser = authState.loggedInUser

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Badge(
                                containerColor = Color(0xFF059669),
                                contentColor = Color.White,
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text("STUDENT RESIDENT", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                            }
                            Text(
                                studentUser?.name ?: "Student Portal",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            "Roll: ${studentUser?.studentId ?: ""} • Room ${studentUser?.roomNumber ?: ""}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.testTag("student_logout_btn")
                    ) {
                        Icon(
                            Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                StudentTab.values().forEach { tab ->
                    val selected = tab == currentTab
                    val icon = when (tab) {
                        StudentTab.DYNAMIC_QR -> Icons.Default.QrCode2
                        StudentTab.MY_ID -> Icons.Default.Badge
                        StudentTab.TODAY_MENU -> Icons.Default.RestaurantMenu
                        StudentTab.SERVICES -> Icons.Default.SupportAgent
                    }
                    NavigationBarItem(
                        selected = selected,
                        onClick = { viewModel.setStudentTab(tab) },
                        icon = { Icon(icon, contentDescription = tab.title) },
                        label = { Text(tab.title.substringBefore(" "), fontSize = 11.sp) },
                        modifier = Modifier.testTag("student_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (currentTab) {
                StudentTab.DYNAMIC_QR -> StudentDynamicQrScreen(viewModel)
                StudentTab.MY_ID -> StudentDigitalIdScreen(viewModel)
                StudentTab.TODAY_MENU -> StudentMessMenuScreen(viewModel)
                StudentTab.SERVICES -> StudentServicesScreen(viewModel)
            }
        }
    }
}

// 1. Dynamic 60-second QR Generator screen
@Composable
fun StudentDynamicQrScreen(viewModel: HostelViewModel) {
    val studentUser = viewModel.authState.collectAsState().value.loggedInUser
    val payload by viewModel.dynamicPayload.collectAsState()
    val secondsRemaining by viewModel.qrSecondsRemaining.collectAsState()
    val attendanceList by viewModel.attendance.collectAsState()
    val todayDate = viewModel.repository.getTodayDate()
    val activeMeal = viewModel.repository.getActiveMealType() ?: MealType.LUNCH

    val alreadyClaimed = attendanceList.any {
        it.studentId == (studentUser?.studentId ?: "") && it.mealType == activeMeal && it.date == todayDate
    }

    val progress by animateFloatAsState(targetValue = secondsRemaining / 60f, label = "qr_progress")

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Anti-proxy security banner
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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
                                Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Anti-Proxy Protected", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Surface(
                            color = if (alreadyClaimed) Color(0xFF065F46) else Color(0xFF0284C7),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                if (alreadyClaimed) "CLAIMED TODAY" else "${activeMeal.displayName.uppercase()} OPEN",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // QR Display Box with Circular countdown ring
                    Box(contentAlignment = Alignment.Center) {
                        if (alreadyClaimed) {
                            Box(
                                modifier = Modifier
                                    .size(230.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFF1E293B))
                                    .border(2.dp, Color(0xFF059669), RoundedCornerShape(20.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("Meal Already Claimed", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text(
                                        "You have already checked in for ${activeMeal.displayName} today ($todayDate).",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            DynamicQRCodeView(
                                data = payload?.token ?: "INIT_${studentUser?.studentId}",
                                size = 230.dp,
                                modifier = Modifier.testTag("dynamic_qr_code")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!alreadyClaimed) {
                        // Countdown timer ticker
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.size(36.dp),
                                    color = if (secondsRemaining < 10) Color(0xFFEF4444) else Color(0xFF38BDF8),
                                    trackColor = Color(0xFF334155),
                                    strokeWidth = 3.dp
                                )
                                Text(
                                    "$secondsRemaining",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Auto-refreshes in $secondsRemaining seconds",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "Screenshots expire automatically to prevent proxy sharing.",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        FilledTonalButton(
                            onClick = { viewModel.refreshQrImmediately() },
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF1E293B), contentColor = Color(0xFF38BDF8)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("refresh_qr_btn")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Force Refresh Token", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Current Operational Window", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${activeMeal.displayName}: ${activeMeal.defaultStart} - ${activeMeal.defaultEnd}", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        Text("Date: $todayDate", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }
    }
}

// 2. Student Digital ID & Room screen
@Composable
fun StudentDigitalIdScreen(viewModel: HostelViewModel) {
    val studentUser = viewModel.authState.collectAsState().value.loggedInUser ?: return
    val rebates by viewModel.rebates.collectAsState()
    val studentRebates = rebates.filter { it.studentId == studentUser.studentId && it.isApproved }
    val rebateDeductions = studentRebates.sumOf { it.daysCount * 100 }
    val netMessBill = (studentUser.baseMonthlyMessFee - rebateDeductions).coerceAtLeast(0.0)

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Digital Pass Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F172A))
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("CAMPUS RESIDENT DIGITAL ID", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF059669)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            studentUser.name.take(2).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(studentUser.name, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF0F172A))
                    Text("ROLL NO: ${studentUser.studentId}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF059669))

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("ROOM", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                            Text(studentUser.roomNumber, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("BLOCK", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                            Text(studentUser.blockName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("BED", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                            Text(studentUser.bedNumber, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("STATIC PROFILE VERIFICATION QR", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(6.dp))
                    DynamicQRCodeView(
                        data = "PROFILE_${studentUser.studentId}_${studentUser.roomNumber}",
                        size = 130.dp
                    )
                }
            }
        }

        // Monthly Mess Billing Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Current Month Mess Billing", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Surface(color = Color(0xFF059669).copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp)) {
                            Text("ACTIVE", color = Color(0xFF059669), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Base Monthly Fee:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₹${studentUser.baseMonthlyMessFee.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Approved Rebate Deductions (${studentRebates.sumOf { it.daysCount }} days):", fontSize = 13.sp, color = Color(0xFF059669))
                        Text("- ₹$rebateDeductions", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF059669))
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Net Payable Amount:", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("₹${netMessBill.toInt()}", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

// 3. Student Mess Menu Screen
@Composable
fun StudentMessMenuScreen(viewModel: HostelViewModel) {
    val menuItems by viewModel.menu.collectAsState()
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    var selectedDay by remember { mutableStateOf("Monday") }

    val dayMenu = menuItems.filter { it.dayOfWeek == selectedDay }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Hostel Mess Schedule & Menu", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Daily menu planner prepared by hostel dining board.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(days) { day ->
                    FilterChip(
                        selected = day == selectedDay,
                        onClick = { selectedDay = day },
                        label = { Text(day) }
                    )
                }
            }
        }

        items(dayMenu) { item ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(item.mealType.displayName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(6.dp)) {
                            Text(
                                "${item.startTime} - ${item.endTime}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    item.menuItems.forEach { food ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(food, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

// 4. Student Support & Rebate Screen
@Composable
fun StudentServicesScreen(viewModel: HostelViewModel) {
    val authState by viewModel.authState.collectAsState()
    val student = authState.loggedInUser ?: return

    var complaintCategory by remember { mutableStateOf(ComplaintCategory.HOSTEL_ROOM) }
    var complaintTitle by remember { mutableStateOf("") }
    var complaintDesc by remember { mutableStateOf("") }
    var complaintSuccess by remember { mutableStateOf(false) }

    var leaveStart by remember { mutableStateOf("2026-09-18") }
    var leaveEnd by remember { mutableStateOf("2026-09-21") }
    var leaveReason by remember { mutableStateOf("") }
    var leaveDays by remember { mutableStateOf("3") }
    var rebateSuccess by remember { mutableStateOf(false) }

    val complaints by viewModel.complaints.collectAsState()
    val studentComplaints = complaints.filter { it.studentId == student.studentId }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Complaint Ticket Submission
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Log Complaint / Maintenance Ticket", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Directly routes issue to warden and maintenance supervisors.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Category:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(ComplaintCategory.values()) { cat ->
                            FilterChip(
                                selected = cat == complaintCategory,
                                onClick = { complaintCategory = cat },
                                label = { Text(cat.displayName, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = complaintTitle,
                        onValueChange = { complaintTitle = it },
                        label = { Text("Issue Title") },
                        placeholder = { Text("e.g. Geyser not warming water") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = complaintDesc,
                        onValueChange = { complaintDesc = it },
                        label = { Text("Detailed Description") },
                        placeholder = { Text("Describe location and specifics...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (complaintTitle.isNotBlank()) {
                                viewModel.submitStudentComplaint(complaintCategory, complaintTitle, complaintDesc)
                                complaintTitle = ""
                                complaintDesc = ""
                                complaintSuccess = true
                            }
                        },
                        enabled = complaintTitle.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Submit Ticket")
                    }

                    if (complaintSuccess) {
                        Text("Ticket logged successfully with status PENDING.", fontSize = 12.sp, color = Color(0xFF059669), modifier = Modifier.padding(top = 6.dp))
                    }
                }
            }
        }

        // Rebate Request Submission
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Mess Rebate Leave Request", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Submit dates in advance to claim ₹100/day mess fee deduction.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = leaveStart,
                            onValueChange = { leaveStart = it },
                            label = { Text("Start Date") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = leaveEnd,
                            onValueChange = { leaveEnd = it },
                            label = { Text("End Date") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = leaveReason,
                        onValueChange = { leaveReason = it },
                        label = { Text("Leave Reason") },
                        placeholder = { Text("e.g. Attending family function") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (leaveReason.isNotBlank()) {
                                val days = leaveDays.toIntOrNull() ?: 2
                                viewModel.submitStudentRebate(leaveStart, leaveEnd, leaveReason, days)
                                leaveReason = ""
                                rebateSuccess = true
                            }
                        },
                        enabled = leaveReason.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                    ) {
                        Text("Request Mess Fee Rebate")
                    }

                    if (rebateSuccess) {
                        Text("Rebate request submitted for Warden approval.", fontSize = 12.sp, color = Color(0xFF059669), modifier = Modifier.padding(top = 6.dp))
                    }
                }
            }
        }

        item {
            Text("My Logged Complaints (${studentComplaints.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        items(studentComplaints) { item ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(item.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        val (statusColor, statusBg) = when (item.status) {
                            ComplaintStatus.PENDING -> Color(0xFFD97706) to Color(0xFFFEF3C7)
                            ComplaintStatus.IN_PROGRESS -> Color(0xFF0284C7) to Color(0xFFE0F2FE)
                            ComplaintStatus.RESOLVED -> Color(0xFF059669) to Color(0xFFD1FAE5)
                        }
                        Surface(color = statusBg, shape = RoundedCornerShape(6.dp)) {
                            Text(item.status.displayName, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    Text(item.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
