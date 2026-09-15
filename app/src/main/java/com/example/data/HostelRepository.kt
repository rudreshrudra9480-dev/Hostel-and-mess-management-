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
                role = UserRole.ADMIN,
                blockName = "All Campus Blocks",
                phone = "+91 98765 43210"
            ),
            // Seed Hostel Workers & Staff with customizable permissions
            User(
                id = "worker-1",
                name = "Ramesh Kumar",
                email = "ramesh.mess@hostel.edu",
                studentId = "WRK-MESS-01",
                role = UserRole.STAFF,
                department = "Mess & Kitchen",
                jobTitle = "Mess Supervisor & Chef",
                phone = "+91 98111 22334",
                workerAccess = WorkerAccess(
                    canScanQr = true,
                    canManageMenu = true,
                    canManageRooms = false,
                    canResolveComplaints = false,
                    canViewStudents = false,
                    canApproveRebates = false
                )
            ),
            User(
                id = "worker-2",
                name = "Suresh Yadav",
                email = "suresh.maint@hostel.edu",
                studentId = "WRK-MAINT-02",
                role = UserRole.STAFF,
                department = "Maintenance & Repairs",
                jobTitle = "Senior Electrician & Plumber",
                phone = "+91 98222 33445",
                workerAccess = WorkerAccess(
                    canScanQr = false,
                    canManageMenu = false,
                    canManageRooms = true,
                    canResolveComplaints = true,
                    canViewStudents = false,
                    canApproveRebates = false
                )
            ),
            User(
                id = "worker-3",
                name = "Anita Bai",
                email = "anita.house@hostel.edu",
                studentId = "WRK-HOUSE-03",
                role = UserRole.STAFF,
                department = "Sanitation & Housekeeping",
                jobTitle = "Housekeeping Supervisor",
                phone = "+91 98333 44556",
                workerAccess = WorkerAccess(
                    canScanQr = false,
                    canManageMenu = false,
                    canManageRooms = true,
                    canResolveComplaints = true,
                    canViewStudents = false,
                    canApproveRebates = false
                )
            ),
            User(
                id = "worker-4",
                name = "Manoj Singh",
                email = "manoj.security@hostel.edu",
                studentId = "WRK-SEC-04",
                role = UserRole.STAFF,
                department = "Campus Security",
                jobTitle = "Security Officer & Gatekeeper",
                phone = "+91 98444 55667",
                workerAccess = WorkerAccess(
                    canScanQr = true,
                    canManageMenu = false,
                    canManageRooms = false,
                    canResolveComplaints = false,
                    canViewStudents = true,
                    canApproveRebates = false
                )
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

    // Room Requests & Allotment tracking
    private val _roomRequests = MutableStateFlow<List<RoomRequest>>(
        listOf(
            RoomRequest(
                id = "REQ-ROOM-101",
                studentId = "HST-2026-101",
                studentName = "Aarav Mehta",
                requestType = "Upgrade to AC Room",
                currentRoom = "A-204",
                preferredBlock = "Block A",
                preferredRoomType = "Double Sharing AC",
                preferredFloor = "2nd Floor",
                reason = "Requirement for temperature controlled study environment for competitive exams.",
                specialNotes = "Prefer quiet corner room if available.",
                status = RoomRequestStatus.ALLOCATED,
                allocatedRoom = "A-204 (Bed 1)",
                wardenRemarks = "Allocated as requested based on academic merit score.",
                requestDate = "2026-08-10"
            ),
            RoomRequest(
                id = "REQ-ROOM-102",
                studentId = "HST-2026-102",
                studentName = "Priya Sharma",
                requestType = "Room Change / Transfer",
                currentRoom = "B-105",
                preferredBlock = "Block B",
                preferredRoomType = "Single Occupancy AC",
                preferredFloor = "1st Floor",
                reason = "Medical recommendation for ground/1st floor single occupancy due to ankle injury.",
                specialNotes = "Attached medical prescription submitted to dispensary.",
                status = RoomRequestStatus.PENDING,
                requestDate = "2026-09-10"
            )
        )
    )
    val roomRequests: StateFlow<List<RoomRequest>> = _roomRequests.asStateFlow()

    // Student Billing Statements
    private val _billStatements = MutableStateFlow<Map<String, StudentBillStatement>>(
        mapOf(
            "HST-2026-101" to StudentBillStatement(
                studentId = "HST-2026-101",
                monthlyMessFeeDue = 3200.0,
                messExtras = 250.0,
                messRebateDeductions = 400.0,
                isMessBillPaid = false,
                semesterRoomFeeDue = 18000.0,
                utilityCharges = 1500.0,
                maintenanceFund = 800.0,
                isRoomFeePaid = false,
                walletBalance = 4850.0
            ),
            "HST-2026-102" to StudentBillStatement(
                studentId = "HST-2026-102",
                monthlyMessFeeDue = 3200.0,
                messExtras = 100.0,
                messRebateDeductions = 0.0,
                isMessBillPaid = true,
                semesterRoomFeeDue = 18000.0,
                utilityCharges = 1500.0,
                maintenanceFund = 800.0,
                isRoomFeePaid = false,
                walletBalance = 6200.0
            ),
            "HST-2026-103" to StudentBillStatement(
                studentId = "HST-2026-103",
                monthlyMessFeeDue = 3200.0,
                messExtras = 0.0,
                messRebateDeductions = 200.0,
                isMessBillPaid = false,
                semesterRoomFeeDue = 18000.0,
                utilityCharges = 1500.0,
                maintenanceFund = 800.0,
                isRoomFeePaid = true,
                walletBalance = 3500.0
            )
        )
    )
    val billStatements: StateFlow<Map<String, StudentBillStatement>> = _billStatements.asStateFlow()

    // Payment Transactions History
    private val _transactions = MutableStateFlow<List<PaymentTransaction>>(
        listOf(
            PaymentTransaction(
                transactionId = "TXN-HST-84920",
                orderId = "ORD-HST-89210",
                studentId = "HST-2026-101",
                studentName = "Aarav Mehta",
                billType = BillType.MESS_BILL,
                amount = 3200.0,
                paymentMethod = PaymentMethod.UPI,
                paymentReference = "UPI: aarav@okhdfc (ID: 948192841)",
                status = PaymentStatus.SUCCESS,
                paidAtDate = "2026-08-05",
                gatewayProvider = "Bharat UPI Switch Gateway",
                bankReferenceNumber = "RRN-UPI-884920194829",
                remarks = "August 2026 Monthly Mess Advance - Reconciled"
            ),
            PaymentTransaction(
                transactionId = "TXN-HST-61029",
                orderId = "ORD-HST-61928",
                studentId = "HST-2026-101",
                studentName = "Aarav Mehta",
                billType = BillType.HOSTEL_ROOM_FEE,
                amount = 20300.0,
                paymentMethod = PaymentMethod.CARD,
                paymentReference = "SBI MasterCard ending in 1184",
                status = PaymentStatus.FAILED,
                paidAtDate = "2026-09-08",
                gatewayProvider = "Campus Razorpay Gateway",
                bankReferenceNumber = "RRN-FAIL-194829103948",
                failureReason = "Bank 3D-Secure timeout / transaction declined by issuer",
                remarks = "Declined by issuing bank during OTP verification"
            ),
            PaymentTransaction(
                transactionId = "TXN-HST-73194",
                orderId = "ORD-HST-73918",
                studentId = "HST-2026-101",
                studentName = "Aarav Mehta",
                billType = BillType.HOSTEL_ROOM_FEE,
                amount = 5000.0,
                paymentMethod = PaymentMethod.CARD,
                paymentReference = "HDFC Visa ending in 4289",
                status = PaymentStatus.SUCCESS,
                paidAtDate = "2026-07-28",
                gatewayProvider = "Campus Razorpay Gateway",
                bankReferenceNumber = "RRN-HDFC-993817294819",
                remarks = "Hostel Security Caution Deposit - Reconciled"
            ),
            PaymentTransaction(
                transactionId = "TXN-HST-92011",
                orderId = "ORD-HST-92819",
                studentId = "HST-2026-102",
                studentName = "Priya Sharma",
                billType = BillType.MESS_BILL,
                amount = 3300.0,
                paymentMethod = PaymentMethod.UPI,
                paymentReference = "Google Pay UPI: priya@okaxis",
                status = PaymentStatus.SUCCESS,
                paidAtDate = "2026-09-02",
                gatewayProvider = "Bharat UPI Switch Gateway",
                bankReferenceNumber = "RRN-UPI-774910294819",
                remarks = "September 2026 Mess Dues with Special Feast Extra"
            )
        )
    )
    val transactions: StateFlow<List<PaymentTransaction>> = _transactions.asStateFlow()

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

    // Register a new Warden (Chief Administrator)
    fun registerWarden(
        name: String,
        email: String,
        wardenId: String,
        blockName: String,
        phone: String
    ): User {
        val newWarden = User(
            name = name.trim(),
            email = email.trim(),
            studentId = wardenId.trim().ifEmpty { "WRD-${System.currentTimeMillis().toString().takeLast(4)}" },
            role = UserRole.ADMIN,
            blockName = blockName.trim(),
            phone = phone.trim()
        )
        _users.value = _users.value + newWarden
        return newWarden
    }

    // Register a new Worker/Staff member
    fun registerWorker(
        name: String,
        email: String,
        staffId: String,
        department: String,
        jobTitle: String,
        phone: String,
        access: WorkerAccess
    ): User {
        val count = _users.value.filter { it.role == UserRole.STAFF }.size + 1
        val finalStaffId = staffId.trim().ifEmpty { "WRK-${department.take(3).uppercase()}-0$count" }
        val newWorker = User(
            name = name.trim(),
            email = email.trim(),
            studentId = finalStaffId,
            role = UserRole.STAFF,
            department = department.trim(),
            jobTitle = jobTitle.trim(),
            phone = phone.trim(),
            workerAccess = access
        )
        _users.value = _users.value + newWorker
        return newWorker
    }

    // Warden editing worker permissions & active status
    fun updateWorkerAccess(
        workerId: String,
        access: WorkerAccess,
        isActive: Boolean = true,
        jobTitle: String? = null,
        department: String? = null
    ) {
        _users.value = _users.value.map { user ->
            if (user.id == workerId || user.studentId == workerId) {
                user.copy(
                    workerAccess = access,
                    isActive = isActive,
                    jobTitle = jobTitle ?: user.jobTitle,
                    department = department ?: user.department
                )
            } else {
                user
            }
        }
    }

    // Delete or remove worker from staff directory
    fun deleteWorker(workerId: String) {
        _users.value = _users.value.filter { it.id != workerId && it.studentId != workerId }
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

    // ==========================================
    // ROOM REQUEST & ALLOTMENT METHODS
    // ==========================================
    fun submitRoomRequest(
        studentId: String,
        studentName: String,
        requestType: String,
        currentRoom: String,
        preferredBlock: String,
        preferredRoomType: String,
        preferredFloor: String,
        reason: String,
        specialNotes: String
    ): RoomRequest {
        val newReq = RoomRequest(
            id = "REQ-ROOM-${System.currentTimeMillis().toString().takeLast(5)}",
            studentId = studentId,
            studentName = studentName,
            requestType = requestType,
            currentRoom = currentRoom,
            preferredBlock = preferredBlock,
            preferredRoomType = preferredRoomType,
            preferredFloor = preferredFloor,
            reason = reason,
            specialNotes = specialNotes,
            status = RoomRequestStatus.PENDING,
            requestDate = getTodayDate()
        )
        _roomRequests.value = listOf(newReq) + _roomRequests.value
        return newReq
    }

    fun approveRoomRequest(requestId: String, allocatedRoomNumber: String, remarks: String) {
        _roomRequests.value = _roomRequests.value.map { req ->
            if (req.id == requestId) {
                req.copy(
                    status = RoomRequestStatus.ALLOCATED,
                    allocatedRoom = allocatedRoomNumber,
                    wardenRemarks = remarks
                )
            } else req
        }

        // Also update student's assigned room and room occupancy if a student matches
        val targetRequest = _roomRequests.value.find { it.id == requestId }
        if (targetRequest != null && allocatedRoomNumber.isNotBlank()) {
            _users.value = _users.value.map { user ->
                if (user.studentId == targetRequest.studentId) {
                    user.copy(
                        roomNumber = allocatedRoomNumber,
                        blockName = targetRequest.preferredBlock.ifBlank { user.blockName }
                    )
                } else user
            }
        }
    }

    fun rejectRoomRequest(requestId: String, remarks: String) {
        _roomRequests.value = _roomRequests.value.map { req ->
            if (req.id == requestId) {
                req.copy(
                    status = RoomRequestStatus.REJECTED,
                    wardenRemarks = remarks
                )
            } else req
        }
    }

    // ==========================================
    // BILLING & PAYMENT PROCESSING METHODS
    // ==========================================
    fun getStudentBillStatement(studentId: String): StudentBillStatement {
        return _billStatements.value[studentId] ?: StudentBillStatement(
            studentId = studentId,
            monthlyMessFeeDue = 3200.0,
            messExtras = 0.0,
            messRebateDeductions = 0.0,
            isMessBillPaid = false,
            semesterRoomFeeDue = 18000.0,
            utilityCharges = 1500.0,
            maintenanceFund = 800.0,
            isRoomFeePaid = false,
            walletBalance = 5000.0
        )
    }

    fun recordTransaction(txn: PaymentTransaction): PaymentTransaction {
        if (txn.status == PaymentStatus.SUCCESS) {
            val currentStmt = getStudentBillStatement(txn.studentId)
            val updatedStmt = when (txn.billType) {
                BillType.MESS_BILL -> currentStmt.copy(isMessBillPaid = true)
                BillType.HOSTEL_ROOM_FEE -> currentStmt.copy(isRoomFeePaid = true)
                BillType.COMBINED -> currentStmt.copy(isMessBillPaid = true, isRoomFeePaid = true)
            }

            val finalStmt = if (txn.paymentMethod == PaymentMethod.CAMPUS_WALLET) {
                updatedStmt.copy(walletBalance = (updatedStmt.walletBalance - txn.amount).coerceAtLeast(0.0))
            } else updatedStmt

            _billStatements.value = _billStatements.value + (txn.studentId to finalStmt)
        }

        _transactions.value = listOf(txn) + _transactions.value
        return txn
    }

    fun processPayment(
        studentId: String,
        studentName: String,
        billType: BillType,
        amountPaid: Double,
        paymentMethod: PaymentMethod,
        paymentReference: String
    ): PaymentTransaction {
        val txn = PaymentTransaction(
            transactionId = "TXN-HST-${System.currentTimeMillis().toString().takeLast(6)}",
            studentId = studentId,
            studentName = studentName,
            billType = billType,
            amount = amountPaid,
            paymentMethod = paymentMethod,
            paymentReference = paymentReference,
            status = PaymentStatus.SUCCESS,
            paidAtDate = getTodayDate(),
            remarks = "Paid online via ${paymentMethod.displayName} • Verified by Hostel Accounts"
        )
        return recordTransaction(txn)
    }

    fun getMonthlyBillingHistory(studentId: String): List<MonthlyBillingRecord> {
        val currentStmt = getStudentBillStatement(studentId)
        val septMessBilled = (currentStmt.monthlyMessFeeDue + currentStmt.messExtras - currentStmt.messRebateDeductions).coerceAtLeast(0.0)
        val septRoomBilled = currentStmt.semesterRoomFeeDue + currentStmt.utilityCharges + currentStmt.maintenanceFund

        val septMessPaid = if (currentStmt.isMessBillPaid) septMessBilled else 0.0
        val septMessPending = if (currentStmt.isMessBillPaid) 0.0 else septMessBilled

        val septRoomPaid = if (currentStmt.isRoomFeePaid) septRoomBilled else 0.0
        val septRoomPending = if (currentStmt.isRoomFeePaid) 0.0 else septRoomBilled

        val septStatus = when {
            currentStmt.isMessBillPaid && currentStmt.isRoomFeePaid -> MonthlyBillingStatus.PAID
            currentStmt.isMessBillPaid || currentStmt.isRoomFeePaid -> MonthlyBillingStatus.PARTIALLY_PAID
            else -> MonthlyBillingStatus.PENDING
        }

        return listOf(
            MonthlyBillingRecord(
                monthKey = "2026-09",
                monthName = "Sep 2026",
                fullMonthName = "September 2026",
                messBilled = septMessBilled,
                messPaid = septMessPaid,
                messPending = septMessPending,
                roomBilled = septRoomBilled,
                roomPaid = septRoomPaid,
                roomPending = septRoomPending,
                status = septStatus,
                dueDate = "2026-09-25",
                paidDate = if (septStatus == MonthlyBillingStatus.PAID) "2026-09-14" else null,
                notes = "Current Active Cycle: Mess base ₹${currentStmt.monthlyMessFeeDue.toInt()} + ₹${currentStmt.messExtras.toInt()} extras - ₹${currentStmt.messRebateDeductions.toInt()} rebate"
            ),
            MonthlyBillingRecord(
                monthKey = "2026-08",
                monthName = "Aug 2026",
                fullMonthName = "August 2026",
                messBilled = 3200.0,
                messPaid = 3200.0,
                messPending = 0.0,
                roomBilled = 0.0,
                roomPaid = 0.0,
                roomPending = 0.0,
                status = MonthlyBillingStatus.PAID,
                dueDate = "2026-08-25",
                paidDate = "2026-08-05",
                notes = "Mess dining fee cleared via UPI Bharat Switch"
            ),
            MonthlyBillingRecord(
                monthKey = "2026-07",
                monthName = "Jul 2026",
                fullMonthName = "July 2026",
                messBilled = 3200.0,
                messPaid = 3200.0,
                messPending = 0.0,
                roomBilled = 5000.0,
                roomPaid = 5000.0,
                roomPending = 0.0,
                status = MonthlyBillingStatus.PAID,
                dueDate = "2026-07-28",
                paidDate = "2026-07-28",
                notes = "Hostel Security Caution Deposit & Mess advance reconciled"
            ),
            MonthlyBillingRecord(
                monthKey = "2026-06",
                monthName = "Jun 2026",
                fullMonthName = "June 2026",
                messBilled = 2800.0,
                messPaid = 2800.0,
                messPending = 0.0,
                roomBilled = 0.0,
                roomPaid = 0.0,
                roomPending = 0.0,
                status = MonthlyBillingStatus.PAID,
                dueDate = "2026-06-25",
                paidDate = "2026-06-18",
                notes = "Summer term mess fee adjusted with 4-day leave rebate"
            ),
            MonthlyBillingRecord(
                monthKey = "2026-05",
                monthName = "May 2026",
                fullMonthName = "May 2026",
                messBilled = 3450.0,
                messPaid = 3450.0,
                messPending = 0.0,
                roomBilled = 0.0,
                roomPaid = 0.0,
                roomPending = 0.0,
                status = MonthlyBillingStatus.PAID,
                dueDate = "2026-05-25",
                paidDate = "2026-05-12",
                notes = "Mess bill including ₹250 night canteen and dairy coupon charges"
            ),
            MonthlyBillingRecord(
                monthKey = "2026-04",
                monthName = "Apr 2026",
                fullMonthName = "April 2026",
                messBilled = 3200.0,
                messPaid = 3200.0,
                messPending = 0.0,
                roomBilled = 20300.0,
                roomPaid = 20300.0,
                roomPending = 0.0,
                status = MonthlyBillingStatus.PAID,
                dueDate = "2026-04-20",
                paidDate = "2026-04-15",
                notes = "Spring Semester 2026 room allotment fee and utilities paid in full"
            )
        )
    }
}
