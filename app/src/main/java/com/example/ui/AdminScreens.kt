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
                        AdminTab.MESS_MENU -> Icons.Default.Restaurant
                        AdminTab.ROOM_MATRIX -> Icons.Default.MeetingRoom
                        AdminTab.SCANNER -> Icons.Default.QrCodeScanner
                        AdminTab.COMPLAINTS -> Icons.Default.ReportProblem
                    }
                    NavigationBarItem(
                        selected = selected,
                        onClick = { viewModel.setAdminTab(tab) },
                        icon = { Icon(icon, contentDescription = tab.title) },
                        label = { Text(tab.title.substringBefore(" "), fontSize = 11.sp) },
                        modifier = Modifier.testTag("admin_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (currentTab) {
                AdminTab.STUDENT_REGISTRATION -> AdminStudentRegistrationScreen(viewModel)
                AdminTab.MESS_MENU -> AdminMenuTimingsScreen(viewModel)
                AdminTab.ROOM_MATRIX -> AdminRoomMatrixScreen(viewModel)
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
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, AVAILABLE, FULL, MAINTENANCE
    var feedbackNotification by remember { mutableStateOf<String?>(null) }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "ALL" to "All (${rooms.size})",
                    "AVAILABLE" to "Available (${rooms.count { it.status == RoomStatus.AVAILABLE }})",
                    "FULL" to "Full (${rooms.count { it.status == RoomStatus.FULL }})",
                    "MAINTENANCE" to "Maintenance ($maintenanceRooms)"
                ).forEach { (key, label) ->
                    FilterChip(
                        selected = selectedFilter == key,
                        onClick = { selectedFilter = key },
                        label = { Text(label, fontSize = 11.sp) }
                    )
                }
            }
        }

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
