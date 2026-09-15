package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.HostelRepository
import com.example.model.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMainContainer(
    viewModel: HostelViewModel,
    onLogout: () -> Unit
) {
    val currentTab by viewModel.adminTab.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val adminUser = authState.loggedInUser

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Badge(
                                containerColor = Color(0xFF0284C7),
                                contentColor = Color.White,
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text("ADMIN / WARDEN", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                            }
                            Text(
                                "Hostel Management",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            adminUser?.name ?: "System Administrator",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.testTag("admin_logout_btn")
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
                AdminTab.values().forEach { tab ->
                    val selected = tab == currentTab
                    val icon = when (tab) {
                        AdminTab.STUDENT_REGISTRATION -> Icons.Default.PersonAdd
                        AdminTab.WORKER_ACCESS -> Icons.Default.Badge
                        AdminTab.ROOM_MATRIX -> Icons.Default.MeetingRoom
                        AdminTab.MESS_MENU -> Icons.Default.Restaurant
                        AdminTab.SCANNER -> Icons.Default.QrCodeScanner
                        AdminTab.COMPLAINTS -> Icons.Default.ReportProblem
                    }
                    val labelText = when (tab) {
                        AdminTab.STUDENT_REGISTRATION -> "Students"
                        AdminTab.WORKER_ACCESS -> "Workers"
                        AdminTab.ROOM_MATRIX -> "Rooms"
                        AdminTab.MESS_MENU -> "Menu"
                        AdminTab.SCANNER -> "Scanner"
                        AdminTab.COMPLAINTS -> "Grievance"
                    }
                    NavigationBarItem(
                        selected = selected,
                        onClick = { viewModel.setAdminTab(tab) },
                        icon = { Icon(icon, contentDescription = tab.title) },
                        label = { Text(labelText, fontSize = 10.sp, maxLines = 1) },
                        modifier = Modifier.testTag("admin_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (currentTab) {
                AdminTab.STUDENT_REGISTRATION -> AdminStudentRegistrationScreen(viewModel)
                AdminTab.WORKER_ACCESS -> AdminWorkerAccessScreen(viewModel)
                AdminTab.ROOM_MATRIX -> AdminRoomMatrixScreen(viewModel)
                AdminTab.MESS_MENU -> AdminMenuTimingsScreen(viewModel)
                AdminTab.SCANNER -> AdminScannerVerificationScreen(viewModel)
                AdminTab.COMPLAINTS -> AdminComplaintsBillingScreen(viewModel)
            }
        }
    }
}

// 1. Admin Student Registration & Digital ID Generator
@Composable
fun AdminStudentRegistrationScreen(viewModel: HostelViewModel) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var roomNumber by remember { mutableStateOf("A-204") }
    var blockName by remember { mutableStateOf("Block A") }
    var bedNumber by remember { mutableStateOf("Bed-3") }

    val recentStudent by viewModel.recentlyRegistered.collectAsState()
    val users by viewModel.users.collectAsState()
    val students = users.filter { it.role == UserRole.STUDENT }

    var showIdCardModal by remember { mutableStateOf(false) }
    var selectedStudentForCard by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(recentStudent) {
        if (recentStudent != null) {
            selectedStudentForCard = recentStudent
            showIdCardModal = true
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Onboard New Student", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    }
                    Text(
                        "Auto-generates unique Student Roll ID and initial login credentials.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Student Full Name") },
                        placeholder = { Text("e.g. Rahul Sharma") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().testTag("reg_name_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        placeholder = { Text("e.g. rahul.sharma@campus.edu") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().testTag("reg_email_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = blockName,
                            onValueChange = { blockName = it },
                            label = { Text("Block") },
                            modifier = Modifier.weight(1f).testTag("reg_block_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = roomNumber,
                            onValueChange = { roomNumber = it },
                            label = { Text("Room No") },
                            modifier = Modifier.weight(1f).testTag("reg_room_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = bedNumber,
                            onValueChange = { bedNumber = it },
                            label = { Text("Bed") },
                            modifier = Modifier.weight(1f).testTag("reg_bed_input"),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (fullName.isNotBlank() && email.isNotBlank()) {
                                viewModel.registerStudent(fullName, email, roomNumber, blockName, bedNumber)
                                fullName = ""
                                email = ""
                            }
                        },
                        enabled = fullName.isNotBlank() && email.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("register_student_submit"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Badge, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Register & Generate Digital ID Card", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Registered Students (${students.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    "Tap to view ID Card",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        items(students) { student ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selectedStudentForCard = student
                        showIdCardModal = true
                    }
                    .testTag("student_card_${student.studentId}")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0284C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            student.name.take(2).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(student.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text(
                            "Roll: ${student.studentId} • Room: ${student.roomNumber}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            student.email,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    IconButton(onClick = {
                        selectedStudentForCard = student
                        showIdCardModal = true
                    }) {
                        Icon(Icons.Default.QrCode, contentDescription = "View ID Card", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }

    if (showIdCardModal && selectedStudentForCard != null) {
        DigitalIdCardDialog(
            student = selectedStudentForCard!!,
            onDismiss = {
                showIdCardModal = false
                viewModel.clearRecentlyRegistered()
            }
        )
    }
}

// Digital Student ID Card Dialog
@Composable
fun DigitalIdCardDialog(
    student: User,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Badge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A))
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("CAMPUS HOSTEL AUTHORITY", color = Color(0xFF94A3B8), fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Text("DIGITAL RESIDENT ID PASS", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Avatar and roll number
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0284C7))
                        .border(3.dp, Color(0xFF38BDF8), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        student.name.take(2).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(student.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF0F172A))
                Text("HOSTEL ROLL NO: ${student.studentId}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF0284C7))

                Spacer(modifier = Modifier.height(12.dp))

                // Details grid
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
                        Text(student.roomNumber.ifBlank { "N/A" }, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("BLOCK", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                        Text(student.blockName.ifBlank { "A" }, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("BED", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                        Text(student.bedNumber.ifBlank { "1" }, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Static profile QR code
                Text("STATIC RESIDENT PROFILE QR", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                Spacer(modifier = Modifier.height(6.dp))
                DynamicQRCodeView(
                    data = "HOSTEL_PROFILE_${student.studentId}_${student.roomNumber}",
                    size = 140.dp
                )

                Spacer(modifier = Modifier.height(14.dp))
                Text("Initial Password: ${student.studentId}", fontSize = 11.sp, color = Color(0xFF059669), fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().testTag("close_id_card_dialog"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
                ) {
                    Text("Done / Close Pass")
                }
            }
        }
    }
}

// 2. Admin Mess Menu & Timings Configuration
@Composable
fun AdminMenuTimingsScreen(viewModel: HostelViewModel) {
    val menuItems by viewModel.menu.collectAsState()
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    var selectedDay by remember { mutableStateOf("Monday") }

    var editingItem by remember { mutableStateOf<MessMenuItem?>(null) }
    var showSuccessBanner by remember { mutableStateOf<String?>(null) }

    val dayMenu = menuItems.filter { it.dayOfWeek == selectedDay }
    val existingMealTypes = dayMenu.map { it.mealType }.toSet()
    val missingMealTypes = MealType.values().filter { it !in existingMealTypes }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Food Menu & Timetable Manager", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(
                        "Configure daily breakfast, lunch, snacks, and dinner dishes & service hours.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (showSuccessBanner != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = Color(0xFFD1FAE5),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF10B981))),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(showSuccessBanner!!, color = Color(0xFF065F46), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        IconButton(onClick = { showSuccessBanner = null }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF065F46), modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Day selector tabs
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(days) { day ->
                    FilterChip(
                        selected = day == selectedDay,
                        onClick = { selectedDay = day },
                        label = { Text(day) },
                        modifier = Modifier.testTag("day_chip_$day")
                    )
                }
            }
        }

        items(dayMenu) { item ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header with meal icon, title, timetable badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val icon = when (item.mealType) {
                                MealType.BREAKFAST -> Icons.Default.FreeBreakfast
                                MealType.LUNCH -> Icons.Default.LunchDining
                                MealType.SNACKS -> Icons.Default.BakeryDining
                                MealType.DINNER -> Icons.Default.DinnerDining
                            }
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(item.mealType.displayName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(
                                    "Timetable: ${item.startTime} - ${item.endTime}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Edit Button for this meal
                        Button(
                            onClick = { editingItem = item },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("edit_menu_btn_${item.mealType.name.lowercase()}")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit Menu", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Menu dishes list
                    Text(
                        "Scheduled Food Dishes (${item.menuItems.size}):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        item.menuItems.forEach { food ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF059669))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    food,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Option to add missing meal slot for that day
        if (missingMealTypes.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Add Unscheduled Meal Slot for $selectedDay:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            missingMealTypes.forEach { missingType ->
                                OutlinedButton(
                                    onClick = {
                                        editingItem = MessMenuItem(
                                            dayOfWeek = selectedDay,
                                            mealType = missingType,
                                            menuItems = listOf("Standard Hostel Special"),
                                            startTime = missingType.defaultStart,
                                            endTime = missingType.defaultEnd
                                        )
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(missingType.displayName, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Interactive Food Menu & Timetable Editor Dialog
    if (editingItem != null) {
        EditFoodMenuTimetableDialog(
            dayOfWeek = selectedDay,
            item = editingItem!!,
            onDismiss = { editingItem = null },
            onSave = { updatedItems, newStart, newEnd ->
                val mealType = editingItem!!.mealType
                viewModel.updateMealMenuAndTimings(
                    day = selectedDay,
                    mealType = mealType,
                    menuItems = updatedItems,
                    start = newStart,
                    end = newEnd
                )
                editingItem = null
                showSuccessBanner = "Saved $selectedDay ${mealType.displayName} menu & timetable successfully!"
            }
        )
    }
}

// Dedicated Full-featured Dialog to edit both Dishes and Timetable timings
@Composable
fun EditFoodMenuTimetableDialog(
    dayOfWeek: String,
    item: MessMenuItem,
    onDismiss: () -> Unit,
    onSave: (items: List<String>, start: String, end: String) -> Unit
) {
    var editStart by remember { mutableStateOf(item.startTime) }
    var editEnd by remember { mutableStateOf(item.endTime) }
    val currentDishes = remember { mutableStateListOf<String>().apply { addAll(item.menuItems) } }
    var newDishInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Curated popular Indian & hostel suggestions according to meal category
    val suggestions = when (item.mealType) {
        MealType.BREAKFAST -> listOf("Poha", "Idli Sambar", "Aloo Paratha", "Bread Omelette", "Tea / Coffee", "Boiled Eggs", "Upma", "Fresh Fruits", "Masala Dosa")
        MealType.LUNCH -> listOf("Paneer Butter Masala", "Dal Tadka", "Jeera Rice", "Roti / Chapati", "Curd / Raita", "Mix Veg Sabzi", "Gulab Jamun", "Salad", "Rajma Chawal")
        MealType.SNACKS -> listOf("Samosa", "Chai / Coffee", "Pakora", "Veg Sandwich", "Biscuits", "Fruit Juice", "Bhel Puri", "Vada Pav")
        MealType.DINNER -> listOf("Dal Makhani", "Shahi Paneer", "Veg Biryani", "Butter Naan / Roti", "Kheer", "Egg Curry", "Chole Masala", "Ice Cream", "Moong Dal Halwa")
    }

    // Standard time presets for fast timetable configuration
    val timePresets = when (item.mealType) {
        MealType.BREAKFAST -> listOf("07:00" to "09:30", "07:30" to "10:00", "08:00" to "10:30")
        MealType.LUNCH -> listOf("12:00" to "14:00", "12:30" to "14:30", "13:00" to "15:00")
        MealType.SNACKS -> listOf("16:00" to "17:30", "16:30" to "18:00", "17:00" to "18:30")
        MealType.DINNER -> listOf("19:30" to "21:30", "20:00" to "22:00", "20:30" to "22:30")
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.RestaurantMenu,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Edit ${item.mealType.displayName}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                        Text(
                            "$dayOfWeek Food Menu & Timetable",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.outline)
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            errorMessage!!,
                            color = Color(0xFF991B1B),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                // SECTION 1: TIMETABLE SCHEDULE
                Text(
                    "1. Operational Service Hours (Timetable)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Defines when dynamic QR codes can be generated & claimed.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = editStart,
                        onValueChange = { editStart = it },
                        label = { Text("Start Time (HH:mm)") },
                        placeholder = { Text("07:30") },
                        modifier = Modifier.weight(1f).testTag("edit_start_time_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editEnd,
                        onValueChange = { editEnd = it },
                        label = { Text("End Time (HH:mm)") },
                        placeholder = { Text("09:30") },
                        modifier = Modifier.weight(1f).testTag("edit_end_time_input"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text("Quick Timing Presets:", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    items(timePresets) { (presetStart, presetEnd) ->
                        SuggestionChip(
                            onClick = {
                                editStart = presetStart
                                editEnd = presetEnd
                            },
                            label = { Text("$presetStart - $presetEnd", fontSize = 11.sp) }
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                // SECTION 2: FOOD MENU DISHES
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "2. Food Menu Dishes (${currentDishes.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (currentDishes.isNotEmpty()) {
                        TextButton(
                            onClick = { currentDishes.clear() },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Clear All", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                // Existing dishes list with deletion
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (currentDishes.isEmpty()) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "No dishes in menu. Add below or pick suggestions.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    } else {
                        currentDishes.forEachIndexed { index, dish ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Text("${index + 1}.", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(dish, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                }
                                IconButton(
                                    onClick = { currentDishes.removeAt(index) },
                                    modifier = Modifier.size(28.dp).testTag("delete_dish_${index}")
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Remove $dish",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Input to add a new dish
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newDishInput,
                        onValueChange = { newDishInput = it },
                        label = { Text("Add Food Dish / Item") },
                        placeholder = { Text("e.g. Paneer Butter Masala") },
                        modifier = Modifier.weight(1f).testTag("add_dish_input"),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            val trimmed = newDishInput.trim()
                            if (trimmed.isNotBlank()) {
                                if (!currentDishes.any { it.equals(trimmed, ignoreCase = true) }) {
                                    currentDishes.add(trimmed)
                                }
                                newDishInput = ""
                                errorMessage = null
                            }
                        },
                        enabled = newDishInput.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("add_dish_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add")
                    }
                }

                // Popular suggestions
                Spacer(modifier = Modifier.height(10.dp))
                Text("Tap to add popular dishes:", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    items(suggestions) { itemSuggestion ->
                        val alreadyAdded = currentDishes.any { it.equals(itemSuggestion, ignoreCase = true) }
                        FilterChip(
                            selected = alreadyAdded,
                            onClick = {
                                if (alreadyAdded) {
                                    currentDishes.removeAll { it.equals(itemSuggestion, ignoreCase = true) }
                                } else {
                                    currentDishes.add(itemSuggestion)
                                    errorMessage = null
                                }
                            },
                            label = { Text(itemSuggestion, fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(
                                    if (alreadyAdded) Icons.Default.Check else Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Dialog Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (editStart.isBlank() || editEnd.isBlank()) {
                                errorMessage = "Please provide valid Start and End operational times."
                                return@Button
                            }
                            if (currentDishes.isEmpty()) {
                                errorMessage = "Please add at least one dish to the menu."
                                return@Button
                            }
                            onSave(currentDishes.toList(), editStart.trim(), editEnd.trim())
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("save_menu_timetable_btn")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Changes", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// 3. Admin Room & Occupancy Details Panel
@Composable
fun AdminRoomMatrixScreen(viewModel: HostelViewModel) {
    val rooms by viewModel.rooms.collectAsState()
    val users by viewModel.users.collectAsState()

    val totalRooms = rooms.size
    val totalCapacity = rooms.sumOf { it.capacity }
    val occupiedBeds = rooms.sumOf { it.occupiedBeds }
    val availableBeds = (totalCapacity - occupiedBeds).coerceAtLeast(0)
    val maintenanceRooms = rooms.count { it.status == RoomStatus.MAINTENANCE }

    var showAddRoomDialog by remember { mutableStateOf(false) }
    var selectedRoomForEdit by remember { mutableStateOf<Room?>(null) }
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, REQUESTS, AVAILABLE, FULL, MAINTENANCE
    var feedbackNotification by remember { mutableStateOf<String?>(null) }

    val roomRequests by viewModel.roomRequests.collectAsState()
    val pendingRequests = roomRequests.filter { it.status == RoomRequestStatus.PENDING }
    var selectedRequestForAction by remember { mutableStateOf<RoomRequest?>(null) }
    var actionType by remember { mutableStateOf<String?>(null) } // "APPROVE" or "REJECT"
    var allocateRoomInput by remember { mutableStateOf("") }
    var actionRemarksInput by remember { mutableStateOf("") }

    val filteredRooms = when (selectedFilter) {
        "AVAILABLE" -> rooms.filter { it.status == RoomStatus.AVAILABLE }
        "FULL" -> rooms.filter { it.status == RoomStatus.FULL }
        "MAINTENANCE" -> rooms.filter { it.status == RoomStatus.MAINTENANCE }
        else -> rooms
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Hostel Room Matrix & Availability", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Add new rooms, adjust bed availability, and manage facility status.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(
                    onClick = { showAddRoomDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("add_room_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Room", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (feedbackNotification != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = Color(0xFFD1FAE5),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF10B981))),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(feedbackNotification!!, color = Color(0xFF065F46), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        IconButton(onClick = { feedbackNotification = null }, modifier = Modifier.size(22.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF065F46), modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Analytics Grid
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OccupancyStatCard(
                    title = "Occupied",
                    count = "$occupiedBeds / $totalCapacity",
                    subtitle = "Beds Occupied",
                    color = Color(0xFF0284C7),
                    modifier = Modifier.weight(1f)
                )
                OccupancyStatCard(
                    title = "Available",
                    count = "$availableBeds",
                    subtitle = "Beds Vacant",
                    color = Color(0xFF059669),
                    modifier = Modifier.weight(1f)
                )
                OccupancyStatCard(
                    title = "Maintenance",
                    count = "$maintenanceRooms",
                    subtitle = "Rooms Closed",
                    color = Color(0xFFD97706),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Filters
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val filterItems = listOf(
                    "ALL" to "All Rooms (${rooms.size})",
                    "REQUESTS" to "Student Requests (${roomRequests.size})",
                    "AVAILABLE" to "Available (${rooms.count { it.status == RoomStatus.AVAILABLE }})",
                    "FULL" to "Full (${rooms.count { it.status == RoomStatus.FULL }})",
                    "MAINTENANCE" to "Maintenance ($maintenanceRooms)"
                )
                items(filterItems) { (key, label) ->
                    FilterChip(
                        selected = selectedFilter == key,
                        onClick = { selectedFilter = key },
                        label = { Text(label, fontSize = 11.sp) }
                    )
                }
            }
        }

        if (selectedFilter == "REQUESTS") {
            item {
                Text("Student Room Allotment & Transfer Requests (${roomRequests.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            if (roomRequests.isEmpty()) {
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
                            Icon(Icons.Default.Inbox, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No Room Requests Found", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("When students apply for room change or allotment, they appear here for approval.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(roomRequests) { req ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
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
                                    Text(req.studentName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Roll No: ${req.studentId} • Current: ${req.currentRoom.ifBlank { "Unassigned" }}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                val (chipBg, chipText) = when (req.status) {
                                    RoomRequestStatus.PENDING -> Color(0xFFFEF3C7) to Color(0xFFD97706)
                                    RoomRequestStatus.APPROVED, RoomRequestStatus.ALLOCATED -> Color(0xFFD1FAE5) to Color(0xFF059669)
                                    RoomRequestStatus.REJECTED -> Color(0xFFFEE2E2) to Color(0xFFDC2626)
                                }

                                Surface(color = chipBg, shape = RoundedCornerShape(6.dp)) {
                                    Text(
                                        req.status.displayName,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = chipText,
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
                                    Text("Request: ${req.requestType} • ${req.preferredBlock} (${req.preferredRoomType})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("Reason: ${req.reason}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    if (req.specialNotes.isNotBlank()) {
                                        Text("Notes: ${req.specialNotes}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }

                            if (!req.wardenRemarks.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Warden Remarks: ${req.wardenRemarks}", fontSize = 11.sp, color = Color(0xFF059669), fontWeight = FontWeight.SemiBold)
                                if (!req.allocatedRoom.isNullOrBlank()) {
                                    Text("Allocated Room: ${req.allocatedRoom}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                                }
                            }

                            if (req.status == RoomRequestStatus.PENDING) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            selectedRequestForAction = req
                                            actionType = "REJECT"
                                            actionRemarksInput = "Unable to accommodate requested room at this time."
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("Reject", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Button(
                                        onClick = {
                                            selectedRequestForAction = req
                                            actionType = "APPROVE"
                                            allocateRoomInput = "B-201"
                                            actionRemarksInput = "Approved and allotted as per availability."
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Approve & Allocate", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            item {
                Text("Room Allocation Breakdown (${filteredRooms.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            items(filteredRooms) { room ->
            val vacantBeds = (room.capacity - room.occupiedBeds).coerceAtLeast(0)
            val residentStudents = users.filter { it.roomNumber.equals(room.roomNumber, ignoreCase = true) }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedRoomForEdit = room }
                    .testTag("room_item_${room.roomNumber}")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Room ${room.roomNumber}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                val (badgeColor, badgeText) = when (room.status) {
                                    RoomStatus.AVAILABLE -> Color(0xFF059669) to "AVAILABLE"
                                    RoomStatus.FULL -> Color(0xFFE11D48) to "FULL"
                                    RoomStatus.MAINTENANCE -> Color(0xFFD97706) to "MAINTENANCE"
                                }
                                Surface(
                                    color = badgeColor.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        badgeText,
                                        color = badgeColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                "${room.blockName} • Floor ${room.floor} • Total Capacity: ${room.capacity} bed(s)",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        OutlinedButton(
                            onClick = { selectedRoomForEdit = room },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("edit_room_${room.roomNumber}")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Bed Availability bar & metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (vacantBeds > 0) "$vacantBeds Bed(s) Available" else "No Vacant Beds",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (vacantBeds > 0) Color(0xFF059669) else Color(0xFFE11D48)
                        )
                        Text(
                            "${room.occupiedBeds}/${room.capacity} occupied",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    val progress = if (room.capacity > 0) (room.occupiedBeds.toFloat() / room.capacity.toFloat()).coerceIn(0f, 1f) else 0f
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (progress >= 1f) Color(0xFFE11D48) else Color(0xFF0284C7),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    if (residentStudents.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Residents: " + residentStudents.joinToString(", ") { "${it.name} (${it.studentId.substringAfterLast("-")})" },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        }
    }

    // Student Room Request Review & Allocation Dialog
    if (selectedRequestForAction != null) {
        val req = selectedRequestForAction!!
        AlertDialog(
            onDismissRequest = { selectedRequestForAction = null },
            title = {
                Text(
                    if (actionType == "APPROVE") "Approve & Allocate Room" else "Reject Room Request",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Student: ${req.studentName} (${req.studentId})", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("Request Type: ${req.requestType} • Preferred: ${req.preferredBlock} (${req.preferredRoomType})", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    if (actionType == "APPROVE") {
                        OutlinedTextField(
                            value = allocateRoomInput,
                            onValueChange = { allocateRoomInput = it },
                            label = { Text("Assign Room Number *") },
                            placeholder = { Text("e.g. A-204 or B-102") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    OutlinedTextField(
                        value = actionRemarksInput,
                        onValueChange = { actionRemarksInput = it },
                        label = { Text("Warden Remarks *") },
                        placeholder = { Text("Reason or allotment details...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (actionType == "APPROVE") {
                            viewModel.approveRoomRequest(
                                requestId = req.id,
                                allocatedRoomNumber = allocateRoomInput.ifBlank { "B-201" },
                                remarks = actionRemarksInput.ifBlank { "Approved & Allotted" }
                            )
                            feedbackNotification = "Room ${allocateRoomInput.ifBlank { "B-201" }} successfully allotted to ${req.studentName}!"
                        } else {
                            viewModel.rejectRoomRequest(
                                requestId = req.id,
                                remarks = actionRemarksInput.ifBlank { "Rejected by Warden Office" }
                            )
                            feedbackNotification = "Request from ${req.studentName} has been rejected."
                        }
                        selectedRequestForAction = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (actionType == "APPROVE") Color(0xFF059669) else MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(if (actionType == "APPROVE") "Confirm Allotment" else "Confirm Rejection")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedRequestForAction = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add New Room Dialog
    if (showAddRoomDialog) {
        AddRoomDialog(
            existingRooms = rooms,
            onDismiss = { showAddRoomDialog = false },
            onAdd = { roomNumber, blockName, floor, capacity, occupied, status ->
                viewModel.addRoom(roomNumber, blockName, floor, capacity, occupied, status)
                showAddRoomDialog = false
                feedbackNotification = "Room $roomNumber added with $capacity bed capacity!"
            }
        )
    }

    // Edit Room & Availability Dialog
    if (selectedRoomForEdit != null) {
        EditRoomDialog(
            room = selectedRoomForEdit!!,
            residentCount = users.count { it.roomNumber.equals(selectedRoomForEdit!!.roomNumber, ignoreCase = true) },
            onDismiss = { selectedRoomForEdit = null },
            onSave = { updatedNumber, updatedBlock, updatedFloor, updatedCap, updatedOcc, updatedStatus ->
                viewModel.updateRoom(
                    roomId = selectedRoomForEdit!!.id,
                    roomNumber = updatedNumber,
                    blockName = updatedBlock,
                    floor = updatedFloor,
                    capacity = updatedCap,
                    occupiedBeds = updatedOcc,
                    status = updatedStatus
                )
                selectedRoomForEdit = null
                feedbackNotification = "Room $updatedNumber updated successfully!"
            },
            onDelete = {
                val num = selectedRoomForEdit!!.roomNumber
                viewModel.deleteRoom(selectedRoomForEdit!!.id)
                selectedRoomForEdit = null
                feedbackNotification = "Room $num deleted."
            }
        )
    }
}

// Add Room Dialog
@Composable
fun AddRoomDialog(
    existingRooms: List<Room>,
    onDismiss: () -> Unit,
    onAdd: (roomNumber: String, blockName: String, floor: Int, capacity: Int, occupied: Int, status: RoomStatus) -> Unit
) {
    var roomNumber by remember { mutableStateOf("") }
    var blockName by remember { mutableStateOf("Block A") }
    var floor by remember { mutableIntStateOf(1) }
    var capacity by remember { mutableIntStateOf(2) }
    var occupiedBeds by remember { mutableIntStateOf(0) }
    var selectedStatus by remember { mutableStateOf<RoomStatus?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val availableBeds = (capacity - occupiedBeds).coerceAtLeast(0)
    val effectiveStatus = selectedStatus ?: if (occupiedBeds >= capacity) RoomStatus.FULL else RoomStatus.AVAILABLE

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Hotel, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add New Hostel Room", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Room Number
                OutlinedTextField(
                    value = roomNumber,
                    onValueChange = {
                        roomNumber = it
                        errorMessage = null
                    },
                    label = { Text("Room Number * (e.g. A-103)") },
                    singleLine = true,
                    isError = errorMessage != null,
                    supportingText = errorMessage?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth().testTag("add_room_number_input")
                )

                // Block Selection
                Text("Hostel Block:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Block A", "Block B", "Block C").forEach { block ->
                        FilterChip(
                            selected = blockName == block,
                            onClick = { blockName = block },
                            label = { Text(block, fontSize = 11.sp) }
                        )
                    }
                }

                // Floor
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Floor Level: Floor $floor", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (floor > 0) floor-- },
                            enabled = floor > 0,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("$floor", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        IconButton(
                            onClick = { if (floor < 10) floor++ },
                            enabled = floor < 10,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                HorizontalDivider()

                // Bed Capacity Stepper
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total Bed Capacity", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Maximum residents in room", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                if (capacity > 1) {
                                    capacity--
                                    if (occupiedBeds > capacity) occupiedBeds = capacity
                                }
                            },
                            enabled = capacity > 1,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("$capacity Beds", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        IconButton(
                            onClick = { if (capacity < 8) capacity++ },
                            enabled = capacity < 8,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Occupied Beds Stepper
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Initially Occupied Beds", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Current allocated residents", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (occupiedBeds > 0) occupiedBeds-- },
                            enabled = occupiedBeds > 0,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("$occupiedBeds", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        IconButton(
                            onClick = { if (occupiedBeds < capacity) occupiedBeds++ },
                            enabled = occupiedBeds < capacity,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Availability Summary Box
                Surface(
                    color = if (availableBeds > 0) Color(0xFFECFDF5) else Color(0xFFFEF2F2),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Vacant Bed Availability:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (availableBeds > 0) Color(0xFF065F46) else Color(0xFF991B1B)
                        )
                        Text(
                            "$availableBeds / $capacity Vacant",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (availableBeds > 0) Color(0xFF059669) else Color(0xFFDC2626)
                        )
                    }
                }

                // Status Override
                Text("Operational Status:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    RoomStatus.values().forEach { st ->
                        FilterChip(
                            selected = effectiveStatus == st,
                            onClick = { selectedStatus = st },
                            label = { Text(st.name, fontSize = 10.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmed = roomNumber.trim()
                    if (trimmed.isBlank()) {
                        errorMessage = "Please provide a valid room number."
                        return@Button
                    }
                    if (existingRooms.any { it.roomNumber.equals(trimmed, ignoreCase = true) }) {
                        errorMessage = "Room $trimmed already exists."
                        return@Button
                    }
                    onAdd(trimmed, blockName, floor, capacity, occupiedBeds, effectiveStatus)
                },
                modifier = Modifier.testTag("submit_add_room_btn")
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

// Edit Room & Availability Dialog
@Composable
fun EditRoomDialog(
    room: Room,
    residentCount: Int,
    onDismiss: () -> Unit,
    onSave: (roomNumber: String, blockName: String, floor: Int, capacity: Int, occupiedBeds: Int, status: RoomStatus) -> Unit,
    onDelete: () -> Unit
) {
    var roomNumber by remember { mutableStateOf(room.roomNumber) }
    var blockName by remember { mutableStateOf(room.blockName) }
    var floor by remember { mutableIntStateOf(room.floor) }
    var capacity by remember { mutableIntStateOf(room.capacity) }
    var occupiedBeds by remember { mutableIntStateOf(room.occupiedBeds) }
    var status by remember { mutableStateOf(room.status) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val availableBeds = (capacity - occupiedBeds).coerceAtLeast(0)

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Room ${room.roomNumber}?") },
            text = { Text("Are you sure you want to delete this room from the hostel matrix? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Room")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Room & Availability", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Room", tint = MaterialTheme.colorScheme.error)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Room Number
                OutlinedTextField(
                    value = roomNumber,
                    onValueChange = { roomNumber = it },
                    label = { Text("Room Number *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_room_number_input")
                )

                // Block
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = blockName,
                        onValueChange = { blockName = it },
                        label = { Text("Block") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = floor.toString(),
                        onValueChange = { it.toIntOrNull()?.let { f -> floor = f } },
                        label = { Text("Floor") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                HorizontalDivider()

                // Bed Capacity Stepper
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Bed Capacity", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Total beds in room", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                if (capacity > 1) {
                                    capacity--
                                    if (occupiedBeds > capacity) occupiedBeds = capacity
                                }
                            },
                            enabled = capacity > 1,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("$capacity Beds", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        IconButton(
                            onClick = { if (capacity < 12) capacity++ },
                            enabled = capacity < 12,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Occupied Beds Stepper (Quick availability adjustment!)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Occupied Beds", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Directly edit availability", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                if (occupiedBeds > 0) {
                                    occupiedBeds--
                                    if (status == RoomStatus.FULL) status = RoomStatus.AVAILABLE
                                }
                            },
                            enabled = occupiedBeds > 0,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("$occupiedBeds", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        IconButton(
                            onClick = {
                                if (occupiedBeds < capacity) {
                                    occupiedBeds++
                                    if (occupiedBeds >= capacity && status == RoomStatus.AVAILABLE) {
                                        status = RoomStatus.FULL
                                    }
                                }
                            },
                            enabled = occupiedBeds < capacity,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Live Availability Indicator Box
                Surface(
                    color = if (availableBeds > 0) Color(0xFFECFDF5) else Color(0xFFFEF2F2),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Live Availability:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (availableBeds > 0) Color(0xFF065F46) else Color(0xFF991B1B)
                            )
                            Text(
                                if (availableBeds > 0) "$availableBeds Bed(s) Vacant" else "Fully Occupied",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (availableBeds > 0) Color(0xFF059669) else Color(0xFFDC2626)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Capacity: $capacity • Occupied: $occupiedBeds • Registered: $residentCount student(s)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Status Radios
                Text("Operational Status:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                RoomStatus.values().forEach { st ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { status = st }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = status == st,
                            onClick = { status = st }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        val label = when (st) {
                            RoomStatus.AVAILABLE -> "Available for Student Allocation"
                            RoomStatus.FULL -> "Full (No Beds Vacant)"
                            RoomStatus.MAINTENANCE -> "Maintenance / Cleaning / Repairs"
                        }
                        Text(label, fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (roomNumber.isNotBlank()) {
                        val finalStatus = if (occupiedBeds >= capacity && status == RoomStatus.AVAILABLE) {
                            RoomStatus.FULL
                        } else status
                        onSave(roomNumber, blockName, floor, capacity, occupiedBeds, finalStatus)
                    }
                },
                modifier = Modifier.testTag("save_room_changes_btn")
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

@Composable
fun OccupancyStatCard(
    title: String,
    count: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
            Text(count, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// 4. Admin Scanner & Attendance Verification Screen
@Composable
fun AdminScannerVerificationScreen(viewModel: HostelViewModel) {
    var scannerMode by remember { mutableStateOf("CAMERA") } // "CAMERA" or "MANUAL"
    var manualRollNo by remember { mutableStateOf("") }
    val scanResult by viewModel.lastScanResult.collectAsState()
    val attendanceList by viewModel.attendance.collectAsState()
    val users by viewModel.users.collectAsState()
    val activeMeal = viewModel.repository.getActiveMealType() ?: MealType.LUNCH

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Meal Attendance & QR Scanner", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(
                        "Active Window: ${activeMeal.displayName} (${activeMeal.defaultStart} - ${activeMeal.defaultEnd})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Mode Selector: Camera Scanner vs Manual/Demo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = scannerMode == "CAMERA",
                    onClick = { scannerMode = "CAMERA" },
                    label = { Text("Live Camera Scanner", fontWeight = FontWeight.SemiBold) },
                    leadingIcon = { Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.weight(1f).testTag("tab_camera_scanner")
                )
                FilterChip(
                    selected = scannerMode == "MANUAL",
                    onClick = { scannerMode = "MANUAL" },
                    label = { Text("Quick / Roll No", fontWeight = FontWeight.SemiBold) },
                    leadingIcon = { Icon(Icons.Default.Keyboard, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.weight(1f).testTag("tab_manual_scanner")
                )
            }
        }

        // Live Camera Scanner Card
        if (scannerMode == "CAMERA") {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Embedded CameraX + ZXing real-time viewfinder
                        CameraLiveQrScannerView(
                            onQrScanned = { qrText ->
                                viewModel.scanQrCode(qrText)
                            },
                            onFallbackRequested = {
                                scannerMode = "MANUAL"
                            },
                            modifier = Modifier.testTag("live_camera_viewfinder")
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Live camera scanning: Point device camera directly at student's dynamic QR code.",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Quick demo & manual entry card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (scannerMode == "CAMERA") MaterialTheme.colorScheme.surfaceVariant else Color(0xFF0F172A)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        "Test Scanner with Student Tokens:",
                        color = if (scannerMode == "CAMERA") MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFFCBD5E1),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        users.filter { it.role == UserRole.STUDENT }.take(3).forEach { st ->
                            FilledTonalButton(
                                onClick = {
                                    val token = viewModel.repository.generateDynamicToken(st).token
                                    viewModel.scanQrCode(token)
                                },
                                modifier = Modifier.weight(1f).testTag("quick_scan_${st.studentId}"),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Text(st.studentId.substringAfterLast("-"), fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Manual Roll Number Entry
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = manualRollNo,
                            onValueChange = { manualRollNo = it },
                            placeholder = { Text("Manual Roll No (e.g. HST-2026-101)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("manual_roll_input"),
                            colors = if (scannerMode == "CAMERA") {
                                OutlinedTextFieldDefaults.colors()
                            } else {
                                OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF38BDF8),
                                    unfocusedBorderColor = Color(0xFF475569)
                                )
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (manualRollNo.isNotBlank()) {
                                    viewModel.scanQrCode(manualRollNo)
                                    manualRollNo = ""
                                }
                            },
                            modifier = Modifier.testTag("verify_manual_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Verify")
                        }
                    }
                }
            }
        }

        // Scan verification feedback banner
        if (scanResult != null) {
            item {
                when (val res = scanResult!!) {
                    is HostelRepository.ScanResult.Success -> {
                        val studentUser = users.find { it.studentId.equals(res.attendance.studentId, ignoreCase = true) }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF10B981))),
                            modifier = Modifier.fillMaxWidth().testTag("scan_success_card")
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("ENTRY VERIFIED: APPROVED", fontWeight = FontWeight.Bold, color = Color(0xFF065F46), fontSize = 14.sp)
                                    Text("${res.attendance.studentName} (${res.attendance.studentId})", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF047857))
                                    Text(
                                        "Room: ${studentUser?.roomNumber ?: "Unassigned"} • ${studentUser?.blockName ?: ""} • Meal: ${res.attendance.mealType.displayName}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF059669)
                                    )
                                    Text(
                                        "Logged at ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(res.attendance.scannedAt))}",
                                        fontSize = 10.sp,
                                        color = Color(0xFF047857)
                                    )
                                }
                            }
                        }
                    }
                    is HostelRepository.ScanResult.AlreadyClaimed -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFF59E0B))),
                            modifier = Modifier.fillMaxWidth().testTag("scan_duplicate_card")
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(34.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("DUPLICATE SCAN BLOCKED", fontWeight = FontWeight.Bold, color = Color(0xFF92400E), fontSize = 14.sp)
                                    Text(res.message, fontSize = 12.sp, color = Color(0xFFB45309))
                                }
                            }
                        }
                    }
                    is HostelRepository.ScanResult.Expired -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFEF4444))),
                            modifier = Modifier.fillMaxWidth().testTag("scan_expired_card")
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TimerOff, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(34.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("TOKEN EXPIRED (>60 SECONDS)", fontWeight = FontWeight.Bold, color = Color(0xFF991B1B), fontSize = 14.sp)
                                    Text(res.message, fontSize = 12.sp, color = Color(0xFFB91C1C))
                                }
                            }
                        }
                    }
                    is HostelRepository.ScanResult.Invalid -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                            modifier = Modifier.fillMaxWidth().testTag("scan_invalid_card")
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Error, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(34.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("INVALID QR TOKEN", fontWeight = FontWeight.Bold, color = Color(0xFF991B1B), fontSize = 14.sp)
                                    Text(res.message, fontSize = 12.sp, color = Color(0xFFB91C1C))
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Text("Recent Verified Attendances (${attendanceList.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        items(attendanceList.reversed()) { att ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(10.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(att.studentName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(
                            "${att.studentId} • ${att.mealType.displayName} on ${att.date}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        color = Color(0xFF10B981).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            "CLAIMED",
                            color = Color(0xFF047857),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

// 5. Admin Complaints & Billing Analytics Screen
@Composable
fun AdminComplaintsBillingScreen(viewModel: HostelViewModel) {
    val complaints by viewModel.complaints.collectAsState()
    val rebates by viewModel.rebates.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Complaints Pipeline & Rebate Approvals", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Manage resident service requests and mess fee rebate adjustments.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        item {
            Text("Hostel & Mess Complaints (${complaints.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        items(complaints) { complaint ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                complaint.category.displayName,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        val (statusColor, statusBg) = when (complaint.status) {
                            ComplaintStatus.PENDING -> Color(0xFFD97706) to Color(0xFFFEF3C7)
                            ComplaintStatus.IN_PROGRESS -> Color(0xFF0284C7) to Color(0xFFE0F2FE)
                            ComplaintStatus.RESOLVED -> Color(0xFF059669) to Color(0xFFD1FAE5)
                        }

                        Surface(color = statusBg, shape = RoundedCornerShape(6.dp)) {
                            Text(
                                complaint.status.displayName,
                                color = statusColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(complaint.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(complaint.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("By: ${complaint.studentName} (${complaint.studentId})", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)

                    if (complaint.status != ComplaintStatus.RESOLVED) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            if (complaint.status == ComplaintStatus.PENDING) {
                                TextButton(
                                    onClick = { viewModel.updateComplaintStatus(complaint.id, ComplaintStatus.IN_PROGRESS) }
                                ) {
                                    Text("Mark In Progress", fontSize = 12.sp)
                                }
                            }
                            Button(
                                onClick = { viewModel.updateComplaintStatus(complaint.id, ComplaintStatus.RESOLVED) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                            ) {
                                Text("Resolve Issue", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text("Mess Rebate Leave Requests", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        items(rebates) { rebate ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Roll: ${rebate.studentId} • ${rebate.daysCount} Days Leave", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("${rebate.startDate} to ${rebate.endDate}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        Text(rebate.reason, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Deduction: ₹${rebate.daysCount * 100} / $${rebate.daysCount * 10}", fontSize = 11.sp, color = Color(0xFF059669), fontWeight = FontWeight.Bold)
                    }

                    if (rebate.isApproved) {
                        Surface(color = Color(0xFFD1FAE5), shape = RoundedCornerShape(6.dp)) {
                            Text("APPROVED", color = Color(0xFF059669), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                        }
                    } else {
                        Button(
                            onClick = { viewModel.approveRebate(rebate.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                        ) {
                            Text("Approve", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// WARDEN WORKER ACCESS MANAGEMENT SCREEN
// ==========================================
@Composable
fun AdminWorkerAccessScreen(viewModel: HostelViewModel) {
    val users by viewModel.users.collectAsState()
    val workers = users.filter { it.role == UserRole.STAFF }

    var searchQuery by remember { mutableStateOf("") }
    var selectedDepartment by remember { mutableStateOf("All") }
    var editingWorker by remember { mutableStateOf<User?>(null) }
    var showAddWorkerDialog by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    val departments = listOf("All", "Mess & Kitchen", "Maintenance & Repairs", "Sanitation & Housekeeping", "Campus Security")

    val filteredWorkers = workers.filter { worker ->
        val matchesSearch = worker.name.contains(searchQuery, ignoreCase = true) ||
                worker.studentId.contains(searchQuery, ignoreCase = true) ||
                worker.jobTitle.contains(searchQuery, ignoreCase = true) ||
                worker.department.contains(searchQuery, ignoreCase = true)
        val matchesDept = selectedDepartment == "All" || worker.department.equals(selectedDepartment, ignoreCase = true)
        matchesSearch && matchesDept
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section Header & Subtitle
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0284C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ManageAccounts, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Worker & Staff Access Control", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Text("Warden Privilege: Configure Granular Staff Permissions", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Overview Metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${workers.size}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color.White)
                                Text("Total Staff", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            }
                        }
                        Surface(
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${workers.count { it.isActive }}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF10B981))
                                Text("Active", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            }
                        }
                        Surface(
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${workers.count { it.department.contains("Mess", ignoreCase = true) }}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFFF59E0B))
                                Text("Mess Crew", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            }
                        }
                        Surface(
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${workers.count { it.department.contains("Maint", ignoreCase = true) }}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF38BDF8))
                                Text("Repairs", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            }
                        }
                    }
                }
            }
        }

        // Notification banner if updated
        if (statusMessage != null) {
            item {
                Surface(
                    color = Color(0xFFECFDF5),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF10B981))),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(statusMessage!!, color = Color(0xFF065F46), fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        IconButton(onClick = { statusMessage = null }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color(0xFF059669), modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }

        // Search & Add Worker Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search worker name, ID or role...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.weight(1f).testTag("search_worker_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Button(
                    onClick = { showAddWorkerDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    modifier = Modifier.height(52.dp).testTag("add_new_worker_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Worker", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Department Filter Chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(departments) { dept ->
                    val isSelected = selectedDepartment == dept
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedDepartment = dept },
                        label = { Text(dept, fontSize = 12.sp) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        } else null
                    )
                }
            }
        }

        // Worker Cards List
        items(filteredWorkers) { worker ->
            WorkerItemCard(
                worker = worker,
                onEditAccess = { editingWorker = worker },
                onToggleActive = { newActive ->
                    viewModel.updateWorkerAccess(
                        workerId = worker.id,
                        access = worker.workerAccess,
                        isActive = newActive
                    )
                    statusMessage = "Updated ${worker.name}'s status to ${if (newActive) "Active" else "Suspended"}."
                },
                onDelete = {
                    viewModel.deleteWorker(worker.id)
                    statusMessage = "Worker ${worker.name} removed."
                }
            )
        }

        if (filteredWorkers.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.PersonOff, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No Workers Found", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("No staff matches the selected filter or search.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }

    // Modal Dialog: Edit Worker Access
    if (editingWorker != null) {
        EditWorkerAccessDialog(
            worker = editingWorker!!,
            onDismiss = { editingWorker = null },
            onSave = { updatedAccess, isActive, jobTitle, department ->
                viewModel.updateWorkerAccess(
                    workerId = editingWorker!!.id,
                    access = updatedAccess,
                    isActive = isActive,
                    jobTitle = jobTitle,
                    department = department
                )
                statusMessage = "Access permissions for ${editingWorker!!.name} have been updated successfully!"
                editingWorker = null
            }
        )
    }

    // Modal Dialog: Add New Worker
    if (showAddWorkerDialog) {
        AddNewWorkerDialog(
            onDismiss = { showAddWorkerDialog = false },
            onSave = { name, email, staffId, department, jobTitle, phone, access ->
                viewModel.registerWorker(name, email, staffId, department, jobTitle, phone, access)
                statusMessage = "New worker $name onboarded with assigned access permissions."
                showAddWorkerDialog = false
            }
        )
    }
}

@Composable
fun WorkerItemCard(
    worker: User,
    onEditAccess: () -> Unit,
    onToggleActive: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val access = worker.workerAccess
    val deptColor = when {
        worker.department.contains("Mess", ignoreCase = true) -> Color(0xFFF59E0B)
        worker.department.contains("Maint", ignoreCase = true) -> Color(0xFF0284C7)
        worker.department.contains("Sanit", ignoreCase = true) || worker.department.contains("House", ignoreCase = true) -> Color(0xFF10B981)
        else -> Color(0xFF8B5CF6)
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier.fillMaxWidth().testTag("worker_card_${worker.studentId}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar badge with initials
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(deptColor.copy(alpha = 0.15f))
                        .border(1.5.dp, deptColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        worker.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
                        fontWeight = FontWeight.ExtraBold,
                        color = deptColor,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(worker.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = if (worker.isActive) Color(0xFFD1FAE5) else Color(0xFFFEE2E2),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                if (worker.isActive) "ACTIVE" else "SUSPENDED",
                                color = if (worker.isActive) Color(0xFF059669) else Color(0xFFDC2626),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        "${worker.jobTitle.ifEmpty { "Hostel Staff" }} • ${worker.studentId}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Surface(
                            color = deptColor.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                worker.department.ifEmpty { "General Staff" },
                                color = deptColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                        if (worker.phone.isNotBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("• ${worker.phone}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Worker", tint = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Spacer(modifier = Modifier.height(10.dp))

            // Current Assigned Permissions Chips
            Text("Assigned Access Permissions:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val hasAnyAccess = access.canScanQr || access.canManageMenu || access.canManageRooms || access.canResolveComplaints || access.canViewStudents || access.canApproveRebates

                if (!hasAnyAccess) {
                    Surface(color = Color(0xFFFEE2E2), shape = RoundedCornerShape(6.dp)) {
                        Text("No active permissions (Access Revoked)", color = Color(0xFFDC2626), fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (access.canScanQr) {
                                PermissionChip(title = "QR Scanner", icon = Icons.Default.QrCodeScanner, color = Color(0xFF0284C7))
                            }
                            if (access.canManageMenu) {
                                PermissionChip(title = "Menu Timings", icon = Icons.Default.Restaurant, color = Color(0xFFF59E0B))
                            }
                            if (access.canManageRooms) {
                                PermissionChip(title = "Room Matrix", icon = Icons.Default.MeetingRoom, color = Color(0xFF10B981))
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (access.canResolveComplaints) {
                                PermissionChip(title = "Complaints", icon = Icons.Default.ReportProblem, color = Color(0xFFEC4899))
                            }
                            if (access.canViewStudents) {
                                PermissionChip(title = "Students List", icon = Icons.Default.People, color = Color(0xFF8B5CF6))
                            }
                            if (access.canApproveRebates) {
                                PermissionChip(title = "Rebates", icon = Icons.Default.Payments, color = Color(0xFF14B8A6))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action: Edit Access Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Account Active", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = worker.isActive,
                        onCheckedChange = onToggleActive,
                        modifier = Modifier.testTag("toggle_worker_active_${worker.studentId}")
                    )
                }

                Button(
                    onClick = onEditAccess,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("edit_access_btn_${worker.studentId}")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Edit Worker Access", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun PermissionChip(title: String, icon: ImageVector, color: Color) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(6.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(color.copy(alpha = 0.4f)))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(title, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// Dialog for editing worker's permissions & access
@Composable
fun EditWorkerAccessDialog(
    worker: User,
    onDismiss: () -> Unit,
    onSave: (access: WorkerAccess, isActive: Boolean, jobTitle: String, department: String) -> Unit
) {
    var canScanQr by remember { mutableStateOf(worker.workerAccess.canScanQr) }
    var canManageMenu by remember { mutableStateOf(worker.workerAccess.canManageMenu) }
    var canManageRooms by remember { mutableStateOf(worker.workerAccess.canManageRooms) }
    var canResolveComplaints by remember { mutableStateOf(worker.workerAccess.canResolveComplaints) }
    var canViewStudents by remember { mutableStateOf(worker.workerAccess.canViewStudents) }
    var canApproveRebates by remember { mutableStateOf(worker.workerAccess.canApproveRebates) }
    var isActive by remember { mutableStateOf(worker.isActive) }

    var jobTitle by remember { mutableStateOf(worker.jobTitle) }
    var department by remember { mutableStateOf(worker.department) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Edit Worker Access", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        Text("${worker.name} • ${worker.studentId}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Role Presets
                Text("QUICK ROLE PRESETS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        OutlinedButton(
                            onClick = {
                                canScanQr = true
                                canManageMenu = true
                                canManageRooms = false
                                canResolveComplaints = false
                                canViewStudents = false
                                canApproveRebates = false
                                jobTitle = "Mess Supervisor"
                                department = "Mess & Kitchen"
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Mess Supervisor", fontSize = 11.sp)
                        }
                    }
                    item {
                        OutlinedButton(
                            onClick = {
                                canScanQr = false
                                canManageMenu = false
                                canManageRooms = true
                                canResolveComplaints = true
                                canViewStudents = false
                                canApproveRebates = false
                                jobTitle = "Maintenance Technician"
                                department = "Maintenance & Repairs"
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Maintenance Tech", fontSize = 11.sp)
                        }
                    }
                    item {
                        OutlinedButton(
                            onClick = {
                                canScanQr = true
                                canManageMenu = false
                                canManageRooms = false
                                canResolveComplaints = false
                                canViewStudents = true
                                canApproveRebates = false
                                jobTitle = "Security Guard"
                                department = "Campus Security"
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Gate Security", fontSize = 11.sp)
                        }
                    }
                    item {
                        OutlinedButton(
                            onClick = {
                                canScanQr = true
                                canManageMenu = true
                                canManageRooms = true
                                canResolveComplaints = true
                                canViewStudents = true
                                canApproveRebates = true
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Full Access", fontSize = 11.sp)
                        }
                    }
                    item {
                        OutlinedButton(
                            onClick = {
                                canScanQr = false
                                canManageMenu = false
                                canManageRooms = false
                                canResolveComplaints = false
                                canViewStudents = false
                                canApproveRebates = false
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Revoke All", fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Editable Job Title & Department
                OutlinedTextField(
                    value = jobTitle,
                    onValueChange = { jobTitle = it },
                    label = { Text("Job Title / Designation") },
                    modifier = Modifier.fillMaxWidth().testTag("edit_worker_job_title"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = department,
                    onValueChange = { department = it },
                    label = { Text("Department") },
                    modifier = Modifier.fillMaxWidth().testTag("edit_worker_department"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Permissions Checklist
                Text("INDIVIDUAL PERMISSION TOGGLES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)

                Spacer(modifier = Modifier.height(8.dp))

                PermissionToggleRow(
                    title = "Meal QR Verification Scanner",
                    subtitle = "Verify dynamic student QR passes at mess or gate entry",
                    icon = Icons.Default.QrCodeScanner,
                    checked = canScanQr,
                    onCheckedChange = { canScanQr = it },
                    testTag = "perm_toggle_qr_scanner"
                )

                PermissionToggleRow(
                    title = "Mess Menu & Food Timings",
                    subtitle = "Update daily menu items and meal operating hours",
                    icon = Icons.Default.Restaurant,
                    checked = canManageMenu,
                    onCheckedChange = { canManageMenu = it },
                    testTag = "perm_toggle_mess_menu"
                )

                PermissionToggleRow(
                    title = "Room Matrix & Maintenance",
                    subtitle = "Update bed occupancy, room repairs & cleaning status",
                    icon = Icons.Default.MeetingRoom,
                    checked = canManageRooms,
                    onCheckedChange = { canManageRooms = it },
                    testTag = "perm_toggle_room_matrix"
                )

                PermissionToggleRow(
                    title = "Student Grievance & Complaints",
                    subtitle = "Review and mark hostel complaints as in-progress or resolved",
                    icon = Icons.Default.ReportProblem,
                    checked = canResolveComplaints,
                    onCheckedChange = { canResolveComplaints = it },
                    testTag = "perm_toggle_complaints"
                )

                PermissionToggleRow(
                    title = "Student Resident Directory",
                    subtitle = "View list of hostel students and room allocations",
                    icon = Icons.Default.People,
                    checked = canViewStudents,
                    onCheckedChange = { canViewStudents = it },
                    testTag = "perm_toggle_students"
                )

                PermissionToggleRow(
                    title = "Mess Rebate Verification",
                    subtitle = "Process student leave mess rebate deductions",
                    icon = Icons.Default.Payments,
                    checked = canApproveRebates,
                    onCheckedChange = { canApproveRebates = it },
                    testTag = "perm_toggle_rebates"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Account Active / Suspended
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Worker Account Status", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(if (isActive) "Worker is active and can login" else "Account suspended by Warden", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = isActive,
                            onCheckedChange = { isActive = it },
                            modifier = Modifier.testTag("perm_toggle_account_active")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Save Changes Button
                Button(
                    onClick = {
                        val newAccess = WorkerAccess(
                            canScanQr = canScanQr,
                            canManageMenu = canManageMenu,
                            canManageRooms = canManageRooms,
                            canResolveComplaints = canResolveComplaints,
                            canViewStudents = canViewStudents,
                            canApproveRebates = canApproveRebates
                        )
                        onSave(newAccess, isActive, jobTitle, department)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_worker_access_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save & Apply Worker Permissions", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PermissionToggleRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Surface(
        color = if (checked) Color(0xFF0284C7).copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(10.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (checked) Color(0xFF0284C7).copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant
            )
        ),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (checked) Color(0xFF0284C7) else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.testTag(testTag)
            )
        }
    }
}

// Dialog for onboarding a brand new worker
@Composable
fun AddNewWorkerDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, email: String, staffId: String, department: String, jobTitle: String, phone: String, access: WorkerAccess) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var staffId by remember { mutableStateOf("WRK-0" + (10..99).random()) }
    var department by remember { mutableStateOf("Mess & Kitchen") }
    var jobTitle by remember { mutableStateOf("Mess Assistant") }
    var phone by remember { mutableStateOf("+91 ") }

    var canScanQr by remember { mutableStateOf(true) }
    var canManageMenu by remember { mutableStateOf(false) }
    var canManageRooms by remember { mutableStateOf(false) }
    var canResolveComplaints by remember { mutableStateOf(false) }
    var canViewStudents by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Onboard New Worker", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        Text("Assign initial permissions and staff credentials", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Worker Full Name *") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("add_worker_name"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = staffId,
                    onValueChange = { staffId = it },
                    label = { Text("Staff ID (e.g. WRK-MESS-05) *") },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("add_worker_id"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = department,
                    onValueChange = { department = it },
                    label = { Text("Department (e.g. Mess, Maintenance, Security)") },
                    leadingIcon = { Icon(Icons.Default.Work, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("add_worker_dept"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = jobTitle,
                    onValueChange = { jobTitle = it },
                    label = { Text("Job Title / Role") },
                    modifier = Modifier.fillMaxWidth().testTag("add_worker_title"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Staff Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("add_worker_email"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Contact Phone") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("add_worker_phone"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("INITIAL ACCESS PERMISSIONS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)

                Spacer(modifier = Modifier.height(6.dp))

                PermissionToggleRow(
                    title = "Meal QR Scanner",
                    subtitle = "Scan and verify student QR tokens",
                    icon = Icons.Default.QrCodeScanner,
                    checked = canScanQr,
                    onCheckedChange = { canScanQr = it },
                    testTag = "add_perm_qr"
                )

                PermissionToggleRow(
                    title = "Mess Menu & Timings",
                    subtitle = "Manage weekly menu & serving timings",
                    icon = Icons.Default.Restaurant,
                    checked = canManageMenu,
                    onCheckedChange = { canManageMenu = it },
                    testTag = "add_perm_menu"
                )

                PermissionToggleRow(
                    title = "Room Maintenance",
                    subtitle = "Update bed occupancy and room repair status",
                    icon = Icons.Default.MeetingRoom,
                    checked = canManageRooms,
                    onCheckedChange = { canManageRooms = it },
                    testTag = "add_perm_rooms"
                )

                PermissionToggleRow(
                    title = "Complaints Resolution",
                    subtitle = "Resolve student complaints and maintenance tickets",
                    icon = Icons.Default.ReportProblem,
                    checked = canResolveComplaints,
                    onCheckedChange = { canResolveComplaints = it },
                    testTag = "add_perm_complaints"
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val access = WorkerAccess(
                            canScanQr = canScanQr,
                            canManageMenu = canManageMenu,
                            canManageRooms = canManageRooms,
                            canResolveComplaints = canResolveComplaints,
                            canViewStudents = canViewStudents
                        )
                        val finalEmail = email.ifEmpty { "${staffId.lowercase()}@hostel.edu" }
                        onSave(name, finalEmail, staffId, department, jobTitle, phone, access)
                    },
                    enabled = name.isNotBlank() && staffId.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("submit_add_worker"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save & Add Worker", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// WORKER MAIN CONTAINER (ACCESS ENFORCEMENT)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerMainContainer(
    viewModel: HostelViewModel,
    onLogout: () -> Unit
) {
    val authState by viewModel.authState.collectAsState()
    val worker = authState.loggedInUser
    val access = worker?.workerAccess ?: WorkerAccess()

    // Determine permitted tools
    val permittedTabs = remember(access) {
        val list = mutableListOf<String>()
        if (access.canScanQr) list.add("SCANNER")
        if (access.canManageMenu) list.add("MENU")
        if (access.canManageRooms) list.add("ROOMS")
        if (access.canResolveComplaints) list.add("COMPLAINTS")
        if (access.canViewStudents) list.add("STUDENTS")
        list
    }

    var currentWorkerTab by remember(permittedTabs) {
        mutableStateOf(permittedTabs.firstOrNull() ?: "RESTRICTED")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Badge(
                                containerColor = Color(0xFFF59E0B),
                                contentColor = Color.White,
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text("HOSTEL STAFF", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                            }
                            Text(
                                worker?.jobTitle?.ifEmpty { "Hostel Worker" } ?: "Staff Portal",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            "${worker?.name ?: "Staff"} • ${worker?.department ?: "Operations"}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.testTag("worker_logout_btn")
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            )
        },
        bottomBar = {
            if (permittedTabs.size > 1) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                    permittedTabs.forEach { tabKey ->
                        val (icon, label) = when (tabKey) {
                            "SCANNER" -> Icons.Default.QrCodeScanner to "Scanner"
                            "MENU" -> Icons.Default.Restaurant to "Menu"
                            "ROOMS" -> Icons.Default.MeetingRoom to "Rooms"
                            "COMPLAINTS" -> Icons.Default.ReportProblem to "Grievances"
                            "STUDENTS" -> Icons.Default.People to "Students"
                            else -> Icons.Default.Help to tabKey
                        }
                        NavigationBarItem(
                            selected = currentWorkerTab == tabKey,
                            onClick = { currentWorkerTab = tabKey },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label, fontSize = 11.sp) },
                            modifier = Modifier.testTag("worker_tab_${tabKey.lowercase()}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentWorkerTab) {
                "SCANNER" -> AdminScannerVerificationScreen(viewModel)
                "MENU" -> AdminMenuTimingsScreen(viewModel)
                "ROOMS" -> AdminRoomMatrixScreen(viewModel)
                "COMPLAINTS" -> AdminComplaintsBillingScreen(viewModel)
                "STUDENTS" -> AdminStudentRegistrationScreen(viewModel)
                else -> {
                    // Restricted State
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEF2F2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(36.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Access Restricted by Warden", fontWeight = FontWeight.Bold, fontSize = 18.sp, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Your account is registered as ${worker?.name ?: "Worker"} (${worker?.studentId}), but the Warden has not enabled any active tool permissions for your profile.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(8.dp)) {
                            Text(
                                "Contact: Chief Warden Office • Administration Block",
                                fontSize = 12.sp,
                                modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
