package com.example.model

import java.util.UUID

enum class UserRole {
    ADMIN,
    STAFF,
    STUDENT
}

enum class MealType(val displayName: String, val defaultStart: String, val defaultEnd: String) {
    BREAKFAST("Breakfast", "07:30", "09:30"),
    LUNCH("Lunch", "12:30", "14:30"),
    SNACKS("Snacks", "17:00", "18:00"),
    DINNER("Dinner", "19:30", "21:30")
}

enum class RoomStatus {
    AVAILABLE,
    FULL,
    MAINTENANCE
}

enum class ComplaintCategory(val displayName: String) {
    HOSTEL_ROOM("Hostel Room"),
    MESS_FOOD("Mess Food"),
    CLEANLINESS("Cleanliness & Hygiene")
}

enum class ComplaintStatus(val displayName: String) {
    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    RESOLVED("Resolved")
}

data class WorkerAccess(
    val canScanQr: Boolean = true,
    val canManageMenu: Boolean = false,
    val canManageRooms: Boolean = false,
    val canResolveComplaints: Boolean = false,
    val canViewStudents: Boolean = false,
    val canApproveRebates: Boolean = false
)

data class User(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val email: String,
    val studentId: String, // Roll number (e.g., HST-2026-101), ADMIN-01, or WRK-01
    val role: UserRole,
    val roomNumber: String = "",
    val blockName: String = "",
    val bedNumber: String = "",
    val photoUrl: String = "",
    val baseMonthlyMessFee: Double = 3200.0,
    val isActive: Boolean = true,
    val jobTitle: String = "",
    val department: String = "",
    val phone: String = "",
    val workerAccess: WorkerAccess = WorkerAccess(),
    val roomType: String = "Double Sharing AC",
    val checkInDate: String = "2026-08-01",
    val createdAt: Long = System.currentTimeMillis()
)

enum class RoomRequestStatus(val displayName: String) {
    PENDING("Pending Warden Review"),
    APPROVED("Approved"),
    ALLOCATED("Room Allocated"),
    REJECTED("Rejected")
}

data class RoomRequest(
    val id: String = UUID.randomUUID().toString(),
    val studentId: String,
    val studentName: String,
    val requestType: String, // "New Room Allotment", "Room Change / Transfer", "Upgrade to AC Room", "Mutual Swap"
    val currentRoom: String,
    val preferredBlock: String,
    val preferredRoomType: String,
    val preferredFloor: String,
    val reason: String,
    val specialNotes: String = "",
    val status: RoomRequestStatus = RoomRequestStatus.PENDING,
    val allocatedRoom: String? = null,
    val wardenRemarks: String? = null,
    val requestDate: String,
    val createdAt: Long = System.currentTimeMillis()
)

enum class BillType(val displayName: String) {
    MESS_BILL("Mess & Dining Bill"),
    HOSTEL_ROOM_FEE("Hostel Room Fee"),
    COMBINED("Combined Mess & Room Dues")
}

enum class PaymentMethod(val displayName: String) {
    UPI("UPI (GPay / PhonePe / Paytm)"),
    CARD("Debit / Credit Card"),
    NET_BANKING("Net Banking"),
    CAMPUS_WALLET("Hostel Campus Wallet")
}

enum class PaymentStatus {
    SUCCESS,
    PENDING,
    FAILED
}

data class PaymentTransaction(
    val transactionId: String = "TXN-" + UUID.randomUUID().toString().take(8).uppercase(),
    val studentId: String,
    val studentName: String,
    val billType: BillType,
    val amount: Double,
    val paymentMethod: PaymentMethod,
    val paymentReference: String, // e.g. "UPI: aarav@okhdfc" or "Card ending 4242"
    val status: PaymentStatus = PaymentStatus.SUCCESS,
    val paidAtDate: String,
    val timestamp: Long = System.currentTimeMillis(),
    val remarks: String = "Official Campus Hostel e-Receipt",
    val orderId: String = "ORD-" + UUID.randomUUID().toString().take(6).uppercase(),
    val gatewayProvider: String = "Campus Razorpay Gateway",
    val bankReferenceNumber: String = "RRN-" + (100000000000L + (Math.random() * 900000000000L).toLong()),
    val signatureHash: String = "sig_" + UUID.randomUUID().toString().take(12),
    val failureReason: String? = null
)

data class StudentBillStatement(
    val studentId: String,
    val monthlyMessFeeDue: Double = 3200.0,
    val messExtras: Double = 250.0,
    val messRebateDeductions: Double = 400.0,
    val isMessBillPaid: Boolean = false,
    
    val semesterRoomFeeDue: Double = 18000.0,
    val utilityCharges: Double = 1500.0,
    val maintenanceFund: Double = 800.0,
    val isRoomFeePaid: Boolean = false,
    
    val walletBalance: Double = 5000.0
) {
    val netMessBill: Double
        get() = if (isMessBillPaid) 0.0 else (monthlyMessFeeDue + messExtras - messRebateDeductions).coerceAtLeast(0.0)
        
    val netRoomFee: Double
        get() = if (isRoomFeePaid) 0.0 else (semesterRoomFeeDue + utilityCharges + maintenanceFund)
        
    val totalOutstandingDue: Double
        get() = netMessBill + netRoomFee
}

enum class MonthlyBillingStatus(val label: String, val colorHex: Long) {
    PAID("Paid in Full", 0xFF059669),
    PARTIALLY_PAID("Partially Paid", 0xFFD97706),
    PENDING("Payment Due", 0xFFDC2626),
    UPCOMING("Upcoming", 0xFF2563EB)
}

data class MonthlyBillingRecord(
    val monthKey: String, // e.g. "2026-09"
    val monthName: String, // e.g. "Sep 2026"
    val fullMonthName: String, // e.g. "September 2026"
    val messBilled: Double,
    val messPaid: Double,
    val messPending: Double,
    val roomBilled: Double,
    val roomPaid: Double,
    val roomPending: Double,
    val status: MonthlyBillingStatus,
    val dueDate: String,
    val paidDate: String? = null,
    val notes: String = ""
) {
    val totalBilled: Double get() = messBilled + roomBilled
    val totalPaid: Double get() = messPaid + roomPaid
    val totalPending: Double get() = messPending + roomPending
}

data class MessMenuItem(
    val dayOfWeek: String, // Monday - Sunday
    val mealType: MealType,
    val menuItems: List<String>,
    val startTime: String, // "07:30"
    val endTime: String    // "09:30"
)

data class MessAttendance(
    val id: String = UUID.randomUUID().toString(),
    val studentId: String,
    val studentName: String,
    val mealType: MealType,
    val date: String, // YYYY-MM-DD
    val scannedBy: String,
    val scannedAt: Long = System.currentTimeMillis()
)

data class Room(
    val id: String = UUID.randomUUID().toString(),
    val roomNumber: String,
    val blockName: String,
    val floor: Int,
    val capacity: Int,
    val occupiedBeds: Int,
    val status: RoomStatus
)

data class Complaint(
    val id: String = UUID.randomUUID().toString(),
    val studentId: String,
    val studentName: String,
    val category: ComplaintCategory,
    val title: String,
    val description: String,
    val status: ComplaintStatus = ComplaintStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
)

data class RebateRequest(
    val id: String = UUID.randomUUID().toString(),
    val studentId: String,
    val startDate: String,
    val endDate: String,
    val reason: String,
    val daysCount: Int,
    val isApproved: Boolean = false
)

data class DynamicTokenPayload(
    val token: String,
    val studentId: String,
    val studentName: String,
    val mealType: MealType,
    val date: String,
    val issuedAt: Long,
    val expiresAt: Long
)
