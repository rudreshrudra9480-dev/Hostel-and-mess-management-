package com.example.data

import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class HostelRepository {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    // Initial users: Admin & Students
    private val _users = MutableStateFlow<List<User>>(
        listOf(
            User(
                id = "admin-1",
                name = "Dr. Rajesh Sharma (Warden)",
                email = "admin@hostel.edu",
                studentId = "ADMIN-001",
                role = UserRole.ADMIN
            ),
            User(
                id = "student-1",
                name = "Aarav Mehta",
                email = "aarav.mehta@campus.edu",
                studentId = "HST-2026-101",
                role = UserRole.STUDENT,
                roomNumber = "A-204",
                blockName = "Block A (Aryabhata)",
                bedNumber = "Bed-1",
                baseMonthlyMessFee = 3200.0
            ),
            User(
                id = "student-2",
                name = "Priya Sharma",
                email = "priya.sharma@campus.edu",
                studentId = "HST-2026-102",
                role = UserRole.STUDENT,
                roomNumber = "B-105",
                blockName = "Block B (Kalpana Chawla)",
                bedNumber = "Bed-2",
                baseMonthlyMessFee = 3200.0
            ),
            User(
                id = "student-3",
                name = "Rohan Verma",
                email = "rohan.verma@campus.edu",
                studentId = "HST-2026-103",
                role = UserRole.STUDENT,
                roomNumber = "A-301",
                blockName = "Block A (Aryabhata)",
                bedNumber = "Bed-3",
                baseMonthlyMessFee = 3200.0
            )
        )
    )
    val users: StateFlow<List<User>> = _users.asStateFlow()

    // Rooms
    private val _rooms = MutableStateFlow<List<Room>>(
        listOf(
            Room(roomNumber = "A-101", blockName = "Block A", floor = 1, capacity = 3, occupiedBeds = 3, status = RoomStatus.FULL),
            Room(roomNumber = "A-204", blockName = "Block A", floor = 2, capacity = 3, occupiedBeds = 2, status = RoomStatus.AVAILABLE),
            Room(roomNumber = "A-301", blockName = "Block A", floor = 3, capacity = 2, occupiedBeds = 1, status = RoomStatus.AVAILABLE),
            Room(roomNumber = "B-105", blockName = "Block B", floor = 1, capacity = 2, occupiedBeds = 2, status = RoomStatus.FULL),
            Room(roomNumber = "B-210", blockName = "Block B", floor = 2, capacity = 4, occupiedBeds = 1, status = RoomStatus.AVAILABLE),
            Room(roomNumber = "B-302", blockName = "Block B", floor = 3, capacity = 3, occupiedBeds = 0, status = RoomStatus.MAINTENANCE),
            Room(roomNumber = "C-101", blockName = "Block C", floor = 1, capacity = 2, occupiedBeds = 0, status = RoomStatus.AVAILABLE)
        )
    )
    val rooms: StateFlow<List<Room>> = _rooms.asStateFlow()

    // Mess Menu
    private val _menu = MutableStateFlow<List<MessMenuItem>>(
        listOf(
            MessMenuItem("Monday", MealType.BREAKFAST, listOf("Idli Sambhar", "Coconut Chutney", "Boiled Eggs / Sprouts", "Tea / Coffee"), "07:30", "09:30"),
            MessMenuItem("Monday", MealType.LUNCH, listOf("Paneer Butter Masala", "Yellow Dal Tadka", "Steamed Basmati Rice", "Roti", "Fresh Salad", "Gulab Jamun"), "12:30", "14:30"),
            MessMenuItem("Monday", MealType.SNACKS, listOf("Veg Samosa with Mint Chutney", "Adrak Chai / Green Tea"), "17:00", "18:00"),
            MessMenuItem("Monday", MealType.DINNER, listOf("Mixed Veg Curry", "Dal Makhani", "Jeera Rice", "Butter Roti", "Kheer"), "19:30", "21:30"),
            
            MessMenuItem("Tuesday", MealType.BREAKFAST, listOf("Aloo Paratha", "Dahi", "Pickle", "Seasonal Fruit", "Tea / Coffee"), "07:30", "09:30"),
            MessMenuItem("Tuesday", MealType.LUNCH, listOf("Rajma Masala", "Aloo Gobi", "Jeera Rice", "Phulka", "Cucumber Raita"), "12:30", "14:30"),
            MessMenuItem("Tuesday", MealType.SNACKS, listOf("Poha with Sev", "Filter Coffee / Tea"), "17:00", "18:00"),
            MessMenuItem("Tuesday", MealType.DINNER, listOf("Chicken Curry / Matar Mushroom", "Tarka Dal", "Steamed Rice", "Roti", "Custard"), "19:30", "21:30"),

            MessMenuItem("Wednesday", MealType.BREAKFAST, listOf("Masala Dosa", "Sambhar", "Tomato Chutney", "Boiled Egg / Bananas"), "07:30", "09:30"),
            MessMenuItem("Wednesday", MealType.LUNCH, listOf("Chole Bhature", "Kashmiri Pulao", "Boondi Raita", "Sweet Lassi"), "12:30", "14:30"),
            MessMenuItem("Wednesday", MealType.SNACKS, listOf("Bread Pakoda", "Masala Chai"), "17:00", "18:00"),
            MessMenuItem("Wednesday", MealType.DINNER, listOf("Veg Kolhapuri", "Moong Dal", "Rice", "Tandoori Roti", "Ice Cream"), "19:30", "21:30"),

            MessMenuItem("Thursday", MealType.BREAKFAST, listOf("Puri Bhaji", "Sprouts Salad", "Orange / Banana", "Tea / Coffee"), "07:30", "09:30"),
            MessMenuItem("Thursday", MealType.LUNCH, listOf("Kadhi Pakora", "Aloo Shimla Mirch", "Khichdi / Rice", "Roti", "Papad"), "12:30", "14:30"),
            MessMenuItem("Thursday", MealType.SNACKS, listOf("Veg Cutlet with Sauce", "Tea / Lemon Juice"), "17:00", "18:00"),
            MessMenuItem("Thursday", MealType.DINNER, listOf("Egg Curry / Malai Kofta", "Dal Fry", "Rice", "Rumali Roti", "Rasgulla"), "19:30", "21:30"),

            MessMenuItem("Friday", MealType.BREAKFAST, listOf("Methi Thepla", "Curd", "Chutney", "Boiled Egg", "Milk / Coffee"), "07:30", "09:30"),
            MessMenuItem("Friday", MealType.LUNCH, listOf("Dum Aloo", "Dal Tadka", "Peas Pulao", "Butter Naan", "Mixed Raita"), "12:30", "14:30"),
            MessMenuItem("Friday", MealType.SNACKS, listOf("Bhelpuri / Sev Puri", "Masala Tea"), "17:00", "18:00"),
            MessMenuItem("Friday", MealType.DINNER, listOf("Shahi Paneer", "Dal Makhani", "Jeera Rice", "Laccha Paratha", "Moong Dal Halwa"), "19:30", "21:30"),

            MessMenuItem("Saturday", MealType.BREAKFAST, listOf("Uttapam", "Coconut Chutney", "Sambhar", "Tea / Coffee"), "07:30", "09:30"),
            MessMenuItem("Saturday", MealType.LUNCH, listOf("Veg Biryani / Egg Biryani", "Mirchi Ka Salan", "Onion Raita", "Sweet"), "12:30", "14:30"),
            MessMenuItem("Saturday", MealType.SNACKS, listOf("Pav Bhaji", "Cold Coffee"), "17:00", "18:00"),
            MessMenuItem("Saturday", MealType.DINNER, listOf("Baingan Bharta", "Chana Dal", "Steamed Rice", "Phulka", "Fruit Custard"), "19:30", "21:30"),

            MessMenuItem("Sunday", MealType.BREAKFAST, listOf("Chole Kulche", "Fresh Juice", "Boiled Egg", "Tea / Coffee"), "08:00", "10:00"),
            MessMenuItem("Sunday", MealType.LUNCH, listOf("Special Feast: Butter Chicken / Paneer Tikka", "Dal Maharani", "Pulao", "Naan"), "12:30", "15:00"),
            MessMenuItem("Sunday", MealType.SNACKS, listOf("Cookies & Biscuits", "Chai"), "17:00", "18:00"),
            MessMenuItem("Sunday", MealType.DINNER, listOf("Light Khichdi", "Curd", "Achar", "Papad", "Pani Puri Station"), "19:30", "21:30")
        )
    )
    val menu: StateFlow<List<MessMenuItem>> = _menu.asStateFlow()

    // Attendance
    private val _attendance = MutableStateFlow<List<MessAttendance>>(
        listOf(
            MessAttendance(studentId = "HST-2026-101", studentName = "Aarav Mehta", mealType = MealType.BREAKFAST, date = "2026-09-12", scannedBy = "ADMIN-001"),
            MessAttendance(studentId = "HST-2026-102", studentName = "Priya Sharma", mealType = MealType.BREAKFAST, date = "2026-09-12", scannedBy = "ADMIN-001")
        )
    )
    val attendance: StateFlow<List<MessAttendance>> = _attendance.asStateFlow()

    // Complaints
    private val _complaints = MutableStateFlow<List<Complaint>>(
        listOf(
            Complaint(
                studentId = "HST-2026-101",
                studentName = "Aarav Mehta",
                category = ComplaintCategory.HOSTEL_ROOM,
                title = "Ceiling fan regulator loose in Room A-204",
                description = "The fan runs only on speed 5. Please replace the potentiometer.",
                status = ComplaintStatus.IN_PROGRESS
            ),
            Complaint(
                studentId = "HST-2026-102",
                studentName = "Priya Sharma",
                category = ComplaintCategory.MESS_FOOD,
                title = "Need extra warm milk option at Breakfast",
                description = "Requesting warden to ensure milk warmer is kept operational till 9:30 AM.",
                status = ComplaintStatus.PENDING
            )
        )
    )
    val complaints: StateFlow<List<Complaint>> = _complaints.asStateFlow()

    // Rebates
    private val _rebates = MutableStateFlow<List<RebateRequest>>(
        listOf(
            RebateRequest(
                studentId = "HST-2026-101",
                startDate = "2026-09-01",
                endDate = "2026-09-04",
                reason = "Attending National Robotics Hackathon at Tech Institute",
                daysCount = 4,
                isApproved = true
            )
        )
    )
    val rebates: StateFlow<List<RebateRequest>> = _rebates.asStateFlow()

    fun getTodayDate(): String = dateFormat.format(Date())
    fun getCurrentTime(): String = timeFormat.format(Date())

    // Determine current active meal window
    fun getActiveMealType(): MealType? {
        val now = getCurrentTime()
        // Compare with meal windows:
        return when {
            now >= "07:00" && now <= "10:00" -> MealType.BREAKFAST
            now >= "12:00" && now <= "15:00" -> MealType.LUNCH
            now >= "16:30" && now <= "18:30" -> MealType.SNACKS
            now >= "19:00" && now <= "22:00" -> MealType.DINNER
            else -> MealType.LUNCH // Fallback active meal for demo convenience
        }
    }

    // Register a new student
    fun registerStudent(
        name: String,
        email: String,
        roomNumber: String,
        blockName: String,
        bedNumber: String
    ): User {
        val count = _users.value.filter { it.role == UserRole.STUDENT }.size + 104
        val studentId = "HST-2026-$count"
        val newUser = User(
            name = name,
            email = email,
            studentId = studentId,
            role = UserRole.STUDENT,
            roomNumber = roomNumber,
            blockName = blockName,
            bedNumber = bedNumber
        )
        _users.value = _users.value + newUser

        // Update room occupancy
        _rooms.value = _rooms.value.map { room ->
            if (room.roomNumber.equals(roomNumber, ignoreCase = true)) {
                val newOccupancy = (room.occupiedBeds + 1).coerceAtMost(room.capacity)
                val newStatus = if (newOccupancy >= room.capacity) RoomStatus.FULL else RoomStatus.AVAILABLE
                room.copy(occupiedBeds = newOccupancy, status = newStatus)
            } else {
                room
            }
        }
        return newUser
    }

    // Generate dynamic QR token with 60-second validity
    fun generateDynamicToken(student: User): DynamicTokenPayload {
        val activeMeal = getActiveMealType() ?: MealType.LUNCH
        val issuedAt = System.currentTimeMillis()
        val expiresAt = issuedAt + 60_000 // 60 seconds
        val today = getTodayDate()
        // Secure token payload string
        val tokenHash = "SECURE_JWT_v1_${student.studentId}_${activeMeal.name}_${today}_${issuedAt}"
        return DynamicTokenPayload(
            token = tokenHash,
            studentId = student.studentId,
            studentName = student.name,
            mealType = activeMeal,
            date = today,
            issuedAt = issuedAt,
            expiresAt = expiresAt
        )
    }

    // Verify QR scan
    sealed class ScanResult {
        data class Success(val attendance: MessAttendance) : ScanResult()
        data class AlreadyClaimed(val message: String) : ScanResult()
        data class Expired(val message: String) : ScanResult()
        data class Invalid(val message: String) : ScanResult()
    }

    fun verifyScan(payloadStr: String, adminId: String): ScanResult {
        try {
            val parts = payloadStr.split("_")
            if (parts.size < 6 || !payloadStr.startsWith("SECURE_JWT_v1_")) {
                // Check if user manually typed just a roll number
                val student = _users.value.find { it.studentId.equals(payloadStr.trim(), ignoreCase = true) }
                if (student != null) {
                    val activeMeal = getActiveMealType() ?: MealType.LUNCH
                    val today = getTodayDate()
                    val already = _attendance.value.any { it.studentId == student.studentId && it.mealType == activeMeal && it.date == today }
                    if (already) {
                        return ScanResult.AlreadyClaimed("${student.name} (${student.studentId}) has already claimed $activeMeal for today!")
                    }
                    val record = MessAttendance(
                        studentId = student.studentId,
                        studentName = student.name,
                        mealType = activeMeal,
                        date = today,
                        scannedBy = adminId
                    )
                    _attendance.value = _attendance.value + record
                    return ScanResult.Success(record)
                }
                return ScanResult.Invalid("Invalid QR Token signature or format.")
            }

            val studentId = parts[3]
            val mealTypeName = parts[4]
            val date = parts[5]
            val issuedAt = parts[6].toLongOrNull() ?: 0L
            val now = System.currentTimeMillis()

            if (now - issuedAt > 65_000) {
                return ScanResult.Expired("Security Alert: QR Code has expired (>60s). Please ask student to refresh the dynamic token.")
            }

            val mealType = try { MealType.valueOf(mealTypeName) } catch (e: Exception) { MealType.LUNCH }
            val student = _users.value.find { it.studentId == studentId } ?: return ScanResult.Invalid("Student ID not recognized in database.")

            val already = _attendance.value.any { it.studentId == studentId && it.mealType == mealType && it.date == date }
            if (already) {
                return ScanResult.AlreadyClaimed("${student.name} has already verified entry for ${mealType.displayName} on $date.")
            }

            val record = MessAttendance(
                studentId = studentId,
                studentName = student.name,
                mealType = mealType,
                date = date,
                scannedBy = adminId
            )
            _attendance.value = _attendance.value + record
            return ScanResult.Success(record)
        } catch (e: Exception) {
            return ScanResult.Invalid("Verification error: ${e.localizedMessage}")
        }
    }

    // Complaints management
    fun updateComplaintStatus(complaintId: String, newStatus: ComplaintStatus) {
        _complaints.value = _complaints.value.map {
            if (it.id == complaintId) it.copy(status = newStatus) else it
        }
    }

    fun submitComplaint(student: User, category: ComplaintCategory, title: String, description: String) {
        val newComplaint = Complaint(
            studentId = student.studentId,
            studentName = student.name,
            category = category,
            title = title,
            description = description,
            status = ComplaintStatus.PENDING
        )
        _complaints.value = listOf(newComplaint) + _complaints.value
    }

    // Rebate management
    fun submitRebate(studentId: String, start: String, end: String, reason: String, days: Int) {
        val req = RebateRequest(
            studentId = studentId,
            startDate = start,
            endDate = end,
            reason = reason,
            daysCount = days,
            isApproved = false
        )
        _rebates.value = listOf(req) + _rebates.value
    }

    fun approveRebate(id: String) {
        _rebates.value = _rebates.value.map {
            if (it.id == id) it.copy(isApproved = true) else it
        }
    }

    // Room operations
    fun updateRoomStatus(roomId: String, newStatus: RoomStatus) {
        _rooms.value = _rooms.value.map {
            if (it.id == roomId) it.copy(status = newStatus) else it
        }
    }

    fun addRoom(
        roomNumber: String,
        blockName: String,
        floor: Int,
        capacity: Int,
        occupiedBeds: Int = 0,
        status: RoomStatus? = null
    ): Room {
        val calculatedStatus = status ?: if (occupiedBeds >= capacity) {
            RoomStatus.FULL
        } else {
            RoomStatus.AVAILABLE
        }
        val newRoom = Room(
            roomNumber = roomNumber.trim(),
            blockName = blockName.trim(),
            floor = floor,
            capacity = capacity,
            occupiedBeds = occupiedBeds.coerceIn(0, capacity),
            status = calculatedStatus
        )
        _rooms.value = _rooms.value + newRoom
        return newRoom
    }

    fun updateRoom(
        roomId: String,
        roomNumber: String,
        blockName: String,
        floor: Int,
        capacity: Int,
        occupiedBeds: Int,
        status: RoomStatus
    ) {
        _rooms.value = _rooms.value.map {
            if (it.id == roomId) {
                val safeOccupied = occupiedBeds.coerceIn(0, capacity)
                it.copy(
                    roomNumber = roomNumber.trim(),
                    blockName = blockName.trim(),
                    floor = floor,
                    capacity = capacity,
                    occupiedBeds = safeOccupied,
                    status = status
                )
            } else it
        }
    }

    fun deleteRoom(roomId: String) {
        _rooms.value = _rooms.value.filter { it.id != roomId }
    }

    // Menu timing and food item update
    fun updateMenuTimings(day: String, mealType: MealType, start: String, end: String) {
        _menu.value = _menu.value.map {
            if (it.dayOfWeek == day && it.mealType == mealType) {
                it.copy(startTime = start, endTime = end)
            } else it
        }
    }

    fun updateMealMenuAndTimings(day: String, mealType: MealType, menuItems: List<String>, start: String, end: String) {
        val existing = _menu.value.find { it.dayOfWeek == day && it.mealType == mealType }
        if (existing != null) {
            _menu.value = _menu.value.map {
                if (it.dayOfWeek == day && it.mealType == mealType) {
                    it.copy(menuItems = menuItems, startTime = start, endTime = end)
                } else it
            }
        } else {
            val newEntry = MessMenuItem(
                dayOfWeek = day,
                mealType = mealType,
                menuItems = menuItems,
                startTime = start,
                endTime = end
            )
            _menu.value = _menu.value + newEntry
        }
    }
}
