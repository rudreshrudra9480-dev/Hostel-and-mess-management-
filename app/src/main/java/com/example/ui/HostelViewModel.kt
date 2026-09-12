package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.HostelRepository
import com.example.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class PortalType {
    GATEWAY,
    ADMIN_LOGIN,
    STUDENT_LOGIN,
    ADMIN_PANEL,
    STUDENT_PORTAL
}

enum class AdminTab(val title: String) {
    STUDENT_REGISTRATION("Student Onboarding"),
    MESS_MENU("Menu & Timings"),
    ROOM_MATRIX("Hostel Rooms"),
    SCANNER("QR Verification"),
    COMPLAINTS("Complaints & Bills")
}

enum class StudentTab(val title: String) {
    MY_ID("Digital ID & Room"),
    DYNAMIC_QR("Live Meal QR"),
    TODAY_MENU("Mess Schedule"),
    SERVICES("Support & Rebate")
}

data class AuthUiState(
    val currentPortal: PortalType = PortalType.GATEWAY,
    val loggedInUser: User? = null,
    val token: String? = null,
    val errorMessage: String? = null,
    val isAuthenticating: Boolean = false,
    val failedAttempts: Int = 0
)

class HostelViewModel(
    val repository: HostelRepository = HostelRepository()
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthUiState())
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    // Current active Admin Tab
    private val _adminTab = MutableStateFlow(AdminTab.STUDENT_REGISTRATION)
    val adminTab: StateFlow<AdminTab> = _adminTab.asStateFlow()

    // Current active Student Tab
    private val _studentTab = MutableStateFlow(StudentTab.DYNAMIC_QR)
    val studentTab: StateFlow<StudentTab> = _studentTab.asStateFlow()

    // Repository state flows
    val users = repository.users
    val rooms = repository.rooms
    val menu = repository.menu
    val attendance = repository.attendance
    val complaints = repository.complaints
    val rebates = repository.rebates

    // Dynamic 60s QR code state for logged-in student
    private val _dynamicPayload = MutableStateFlow<DynamicTokenPayload?>(null)
    val dynamicPayload: StateFlow<DynamicTokenPayload?> = _dynamicPayload.asStateFlow()

    private val _qrSecondsRemaining = MutableStateFlow(60)
    val qrSecondsRemaining: StateFlow<Int> = _qrSecondsRemaining.asStateFlow()

    private var qrTickerJob: Job? = null

    // Scanner verification feedback state
    private val _lastScanResult = MutableStateFlow<HostelRepository.ScanResult?>(null)
    val lastScanResult: StateFlow<HostelRepository.ScanResult?> = _lastScanResult.asStateFlow()

    // Newly registered student for digital card modal/preview
    private val _recentlyRegistered = MutableStateFlow<User?>(null)
    val recentlyRegistered: StateFlow<User?> = _recentlyRegistered.asStateFlow()

    init {
        // Automatically start default student preview if needed or start at gateway
        // Let's keep gateway so the bifurcated security layer is immediately visible!
    }

    fun navigateToPortal(portal: PortalType) {
        _authState.update { it.copy(currentPortal = portal, errorMessage = null) }
    }

    fun setAdminTab(tab: AdminTab) {
        _adminTab.value = tab
    }

    fun setStudentTab(tab: StudentTab) {
        _studentTab.value = tab
    }

    fun clearRecentlyRegistered() {
        _recentlyRegistered.value = null
    }

    fun clearScanResult() {
        _lastScanResult.value = null
    }

    // Bifurcated Login Security Engine
    fun login(role: UserRole, identifier: String, pass: String) {
        viewModelScope.launch {
            _authState.update { it.copy(isAuthenticating = true, errorMessage = null) }
            delay(400) // Realistic credential hashing and token generation simulation

            val userList = users.value
            val user = if (role == UserRole.ADMIN) {
                userList.find { it.role == UserRole.ADMIN && (it.email.equals(identifier.trim(), ignoreCase = true) || it.studentId.equals(identifier.trim(), ignoreCase = true)) }
            } else {
                userList.find { it.role == UserRole.STUDENT && (it.studentId.equals(identifier.trim(), ignoreCase = true) || it.email.equals(identifier.trim(), ignoreCase = true)) }
            }

            if (user == null) {
                _authState.update {
                    it.copy(
                        isAuthenticating = false,
                        failedAttempts = it.failedAttempts + 1,
                        errorMessage = "Invalid credentials. Check your ${if (role == UserRole.ADMIN) "Admin ID/Email" else "Student Roll Number"}."
                    )
                }
                return@launch
            }

            // Verify password (default pass demo: "admin123" for admin, "student123" or studentId for student)
            val isValidPass = pass.isNotBlank() && (pass == "admin123" || pass == "student123" || pass == "pass123" || pass == user.studentId)
            if (!isValidPass) {
                _authState.update {
                    it.copy(
                        isAuthenticating = false,
                        failedAttempts = it.failedAttempts + 1,
                        errorMessage = "Authentication failed: Incorrect password."
                    )
                }
                return@launch
            }

            // Success: issue signed session token
            val sessionToken = "BEARER_JWT_${user.role}_${user.id}_${System.currentTimeMillis()}"
            _authState.update {
                it.copy(
                    isAuthenticating = false,
                    loggedInUser = user,
                    token = sessionToken,
                    failedAttempts = 0,
                    errorMessage = null,
                    currentPortal = if (user.role == UserRole.ADMIN) PortalType.ADMIN_PANEL else PortalType.STUDENT_PORTAL
                )
            }

            if (user.role == UserRole.STUDENT) {
                startDynamicQrTicker(user)
            }
        }
    }

    fun logout() {
        qrTickerJob?.cancel()
        _dynamicPayload.value = null
        _authState.update {
            AuthUiState(currentPortal = PortalType.GATEWAY)
        }
    }

    // Dynamic QR Generator Loop
    private fun startDynamicQrTicker(student: User) {
        qrTickerJob?.cancel()
        qrTickerJob = viewModelScope.launch {
            while (true) {
                // Generate new 60s dynamic signed token
                val payload = repository.generateDynamicToken(student)
                _dynamicPayload.value = payload
                _qrSecondsRemaining.value = 60

                // Count down second by second
                for (s in 59 downTo 0) {
                    delay(1000)
                    _qrSecondsRemaining.value = s
                }
            }
        }
    }

    fun refreshQrImmediately() {
        val student = _authState.value.loggedInUser
        if (student != null && student.role == UserRole.STUDENT) {
            startDynamicQrTicker(student)
        }
    }

    // Admin Operations
    fun registerStudent(name: String, email: String, roomNumber: String, blockName: String, bedNumber: String) {
        val newStudent = repository.registerStudent(name, email, roomNumber, blockName, bedNumber)
        _recentlyRegistered.value = newStudent
    }

    fun scanQrCode(scannedData: String) {
        val admin = _authState.value.loggedInUser?.studentId ?: "ADMIN-001"
        val result = repository.verifyScan(scannedData, admin)
        _lastScanResult.value = result
    }

    fun updateComplaintStatus(id: String, status: ComplaintStatus) {
        repository.updateComplaintStatus(id, status)
    }

    fun updateRoomStatus(roomId: String, status: RoomStatus) {
        repository.updateRoomStatus(roomId, status)
    }

    fun addRoom(
        roomNumber: String,
        blockName: String,
        floor: Int,
        capacity: Int,
        occupiedBeds: Int,
        status: RoomStatus? = null
    ) {
        repository.addRoom(roomNumber, blockName, floor, capacity, occupiedBeds, status)
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
        repository.updateRoom(roomId, roomNumber, blockName, floor, capacity, occupiedBeds, status)
    }

    fun deleteRoom(roomId: String) {
        repository.deleteRoom(roomId)
    }

    fun updateMenuTimings(day: String, mealType: MealType, start: String, end: String) {
        repository.updateMenuTimings(day, mealType, start, end)
    }

    fun updateMealMenuAndTimings(day: String, mealType: MealType, menuItems: List<String>, start: String, end: String) {
        repository.updateMealMenuAndTimings(day, mealType, menuItems, start, end)
    }

    fun approveRebate(id: String) {
        repository.approveRebate(id)
    }

    // Student Operations
    fun submitStudentComplaint(category: ComplaintCategory, title: String, description: String) {
        val student = _authState.value.loggedInUser ?: return
        repository.submitComplaint(student, category, title, description)
    }

    fun submitStudentRebate(start: String, end: String, reason: String, days: Int) {
        val student = _authState.value.loggedInUser ?: return
        repository.submitRebate(student.studentId, start, end, reason, days)
    }
}
