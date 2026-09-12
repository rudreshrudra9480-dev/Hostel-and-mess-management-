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

data class User(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val email: String,
    val studentId: String, // Roll number (e.g., HST-2026-101) or ADMIN-01
    val role: UserRole,
    val roomNumber: String = "",
    val blockName: String = "",
    val bedNumber: String = "",
    val photoUrl: String = "",
    val baseMonthlyMessFee: Double = 3200.0,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

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
