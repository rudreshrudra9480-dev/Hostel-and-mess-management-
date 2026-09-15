package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.UserRole
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HostelApp()
                }
            }
        }
    }
}

@Composable
fun HostelApp(viewModel: HostelViewModel = viewModel()) {
    val authState by viewModel.authState.collectAsState()

    when (authState.currentPortal) {
        PortalType.GATEWAY -> BifurcatedPortalGateway(viewModel)
        PortalType.ADMIN_LOGIN -> DedicatedLoginScreen(
            role = UserRole.ADMIN,
            title = "Warden Administration",
            subtitle = "Hostel Chief Warden & Administrative managers",
            identifierLabel = "Warden Email or Official ID",
            defaultDemoId = "admin@hostel.edu",
            defaultDemoPass = "admin123",
            accentColor = Color(0xFF0284C7),
            viewModel = viewModel,
            onBack = { viewModel.navigateToPortal(PortalType.GATEWAY) },
            onRegisterWarden = { viewModel.navigateToPortal(PortalType.WARDEN_REGISTER) }
        )
        PortalType.WARDEN_REGISTER -> WardenRegistrationScreen(
            viewModel = viewModel,
            onBack = { viewModel.navigateToPortal(PortalType.ADMIN_LOGIN) }
        )
        PortalType.WORKER_LOGIN -> DedicatedLoginScreen(
            role = UserRole.STAFF,
            title = "Worker & Staff Portal",
            subtitle = "Mess, maintenance, sanitation & security staff",
            identifierLabel = "Staff ID or Email",
            defaultDemoId = "WRK-MESS-01",
            defaultDemoPass = "worker123",
            accentColor = Color(0xFFF59E0B),
            viewModel = viewModel,
            onBack = { viewModel.navigateToPortal(PortalType.GATEWAY) },
            onRegisterWarden = null
        )
        PortalType.STUDENT_LOGIN -> DedicatedLoginScreen(
            role = UserRole.STUDENT,
            title = "Student Resident Portal",
            subtitle = "Access dynamic QR & room management",
            identifierLabel = "Hostel Roll No / Student ID",
            defaultDemoId = "HST-2026-101",
            defaultDemoPass = "student123",
            accentColor = Color(0xFF059669),
            viewModel = viewModel,
            onBack = { viewModel.navigateToPortal(PortalType.GATEWAY) },
            onRegisterWarden = null
        )
        PortalType.ADMIN_PANEL -> AdminMainContainer(
            viewModel = viewModel,
            onLogout = { viewModel.logout() }
        )
        PortalType.WORKER_PANEL -> WorkerMainContainer(
            viewModel = viewModel,
            onLogout = { viewModel.logout() }
        )
        PortalType.STUDENT_PORTAL -> StudentMainContainer(
            viewModel = viewModel,
            onLogout = { viewModel.logout() }
        )
    }
}

// Gateway Screen for Bifurcating Access with Separate Warden & Worker Portals
@Composable
fun BifurcatedPortalGateway(viewModel: HostelViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // App Identity Header
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF0F172A)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Apartment,
                contentDescription = null,
                tint = Color(0xFF38BDF8),
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            "Campus Hostel & Mess",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            "Role-Based Access Control & Anti-Proxy Tracking",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "SELECT AUTHORIZED ACCESS GATEWAY",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.outline,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Option 1: Warden Portal Card (Includes separate Warden Registration)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("select_warden_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0284C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Warden Administration", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Badge(containerColor = Color(0xFF0284C7)) {
                                Text("CHIEF", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                        Text(
                            "Student Onboarding, Worker Access Control, Room Matrix & Menus",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.navigateToPortal(PortalType.ADMIN_LOGIN) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("select_admin_portal_btn")
                    ) {
                        Text("Warden Login", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { viewModel.navigateToPortal(PortalType.WARDEN_REGISTER) },
                        shape = RoundedCornerShape(10.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF38BDF8))),
                        modifier = Modifier.weight(1f).testTag("register_warden_gateway_btn")
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Register Warden", fontSize = 11.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Option 2: Worker & Staff Operations Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            border = CardDefaults.outlinedCardBorder(),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.navigateToPortal(PortalType.WORKER_LOGIN) }
                .testTag("select_worker_portal_btn")
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF59E0B)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Engineering,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Worker & Staff Portal", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("STAFF", color = Color(0xFFB45309), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                        }
                    }
                    Text(
                        "Mess Chefs, Room Maintenance & Security (Warden Granted)",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Option 3: Student Resident Portal Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = CardDefaults.outlinedCardBorder(),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.navigateToPortal(PortalType.STUDENT_LOGIN) }
                .testTag("select_student_portal_btn")
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF059669)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.School,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Student Resident Portal", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(
                        "60s Dynamic QR, Meal Passes, Digital ID & Complaints",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFF059669)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Security assurance badge
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Role-Isolated Security: Wardens Edit Worker Access Individually", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// Dedicated High-Security Login Screen for each role
@Composable
fun DedicatedLoginScreen(
    role: UserRole,
    title: String,
    subtitle: String,
    identifierLabel: String,
    defaultDemoId: String,
    defaultDemoPass: String,
    accentColor: Color,
    viewModel: HostelViewModel,
    onBack: () -> Unit,
    onRegisterWarden: (() -> Unit)? = null
) {
    var identifier by remember { mutableStateOf(defaultDemoId) }
    var password by remember { mutableStateOf(defaultDemoPass) }
    var passwordVisible by remember { mutableStateOf(false) }

    val authState by viewModel.authState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Switch Portal", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Role Icon & Title
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            val icon = when (role) {
                UserRole.ADMIN -> Icons.Default.AdminPanelSettings
                UserRole.STAFF -> Icons.Default.Engineering
                UserRole.STUDENT -> Icons.Default.School
            }
            Icon(
                icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
        Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)

        Spacer(modifier = Modifier.height(24.dp))

        // Error message banner
        if (authState.errorMessage != null) {
            Surface(
                color = Color(0xFFFEF2F2),
                shape = RoundedCornerShape(10.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFEF4444))),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Error, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(authState.errorMessage!!, color = Color(0xFF991B1B), fontSize = 12.sp)
                }
            }
        }

        // Input 1: Identifier
        OutlinedTextField(
            value = identifier,
            onValueChange = { identifier = it },
            label = { Text(identifierLabel) },
            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
            modifier = Modifier.fillMaxWidth().testTag("login_identifier_input"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Input 2: Password with visibility toggle
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle password visibility"
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().testTag("login_password_input"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Submit Login Button
        Button(
            onClick = {
                viewModel.login(role, identifier, password)
            },
            enabled = identifier.isNotBlank() && password.isNotBlank() && !authState.isAuthenticating,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("submit_login_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (authState.isAuthenticating) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Authenticating...")
            } else {
                val buttonText = when (role) {
                    UserRole.ADMIN -> "Sign In to Warden Dashboard"
                    UserRole.STAFF -> "Sign In as Worker"
                    UserRole.STUDENT -> "Sign In to Student Portal"
                }
                Text(buttonText, fontWeight = FontWeight.Bold)
            }
        }

        // Separate Warden Registration Link
        if (role == UserRole.ADMIN && onRegisterWarden != null) {
            Spacer(modifier = Modifier.height(14.dp))
            OutlinedButton(
                onClick = onRegisterWarden,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("go_to_warden_registration_btn"),
                shape = RoundedCornerShape(12.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF0284C7)))
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("New Warden? Register Warden Account", color = Color(0xFF0284C7), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        // Worker note
        if (role == UserRole.STAFF) {
            Spacer(modifier = Modifier.height(14.dp))
            Surface(
                color = Color(0xFFFFFBEB),
                shape = RoundedCornerShape(10.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFCD34D))),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Worker accounts & access permissions are configured exclusively by the Warden from the Warden Portal.",
                        fontSize = 11.sp,
                        color = Color(0xFF92400E)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Demo Credentials Helper
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Key, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Preloaded Demo Credentials:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "ID: $defaultDemoId | Password: $defaultDemoPass",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ==========================================
// DEDICATED SEPARATE WARDEN REGISTRATION SCREEN
// ==========================================
@Composable
fun WardenRegistrationScreen(
    viewModel: HostelViewModel,
    onBack: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var officialEmail by remember { mutableStateOf("") }
    var wardenId by remember { mutableStateOf("WDN-2026-0" + (2..9).random()) }
    var assignedBlock by remember { mutableStateOf("Block A & B (Senior Hostel)") }
    var phone by remember { mutableStateOf("+91 ") }
    var password by remember { mutableStateOf("admin123") }
    var confirmPassword by remember { mutableStateOf("admin123") }
    var passwordVisible by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }
    var isRegistering by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back Navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text("Back to Warden Login", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Warden Shield Icon
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color(0xFF0284C7).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Security,
                contentDescription = null,
                tint = Color(0xFF0284C7),
                modifier = Modifier.size(34.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text("Warden Registration", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
        Text(
            "Register as an Administrative Warden with master authority to manage students, meals & worker access.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (validationError != null) {
            Surface(
                color = Color(0xFFFEF2F2),
                shape = RoundedCornerShape(10.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFEF4444))),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Error, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(validationError!!, color = Color(0xFF991B1B), fontSize = 12.sp)
                }
            }
        }

        // Full Name
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it; validationError = null },
            label = { Text("Warden Full Name *") },
            placeholder = { Text("e.g. Dr. Sunita Sharma") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth().testTag("warden_reg_name_input"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Official Email
        OutlinedTextField(
            value = officialEmail,
            onValueChange = { officialEmail = it; validationError = null },
            label = { Text("Official University / Hostel Email *") },
            placeholder = { Text("e.g. warden.sharma@hostel.edu") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            modifier = Modifier.fillMaxWidth().testTag("warden_reg_email_input"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Warden ID
        OutlinedTextField(
            value = wardenId,
            onValueChange = { wardenId = it; validationError = null },
            label = { Text("Warden ID / Staff Code *") },
            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
            modifier = Modifier.fillMaxWidth().testTag("warden_reg_id_input"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Assigned Hostel Block
        OutlinedTextField(
            value = assignedBlock,
            onValueChange = { assignedBlock = it; validationError = null },
            label = { Text("Assigned Hostel Complex / Block") },
            leadingIcon = { Icon(Icons.Default.Apartment, contentDescription = null) },
            modifier = Modifier.fillMaxWidth().testTag("warden_reg_block_input"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Phone Number
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it; validationError = null },
            label = { Text("Official Contact Number") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
            modifier = Modifier.fillMaxWidth().testTag("warden_reg_phone_input"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Password
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; validationError = null },
            label = { Text("Password *") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().testTag("warden_reg_password_input"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Confirm Password
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; validationError = null },
            label = { Text("Confirm Password *") },
            leadingIcon = { Icon(Icons.Default.LockClock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().testTag("warden_reg_confirm_password_input"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Authority Notice Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF86EFAC))),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Warden Permissions Granted", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF15803D))
                    Text("Includes exclusive access to configure worker permissions & suspend/activate staff accounts.", fontSize = 11.sp, color = Color(0xFF166534))
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Submit Registration Button
        Button(
            onClick = {
                if (fullName.isBlank()) {
                    validationError = "Please enter Warden Full Name."
                    return@Button
                }
                if (officialEmail.isBlank() || !officialEmail.contains("@")) {
                    validationError = "Please enter a valid official email address."
                    return@Button
                }
                if (wardenId.isBlank()) {
                    validationError = "Please enter a valid Warden ID."
                    return@Button
                }
                if (password.length < 4) {
                    validationError = "Password must be at least 4 characters."
                    return@Button
                }
                if (password != confirmPassword) {
                    validationError = "Passwords do not match."
                    return@Button
                }

                isRegistering = true
                viewModel.registerWarden(
                    name = fullName.trim(),
                    email = officialEmail.trim(),
                    wardenId = wardenId.trim(),
                    blockName = assignedBlock.trim(),
                    phone = phone.trim()
                )
            },
            enabled = !isRegistering,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("submit_warden_registration_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isRegistering) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Registering Warden Profile...")
            } else {
                Icon(Icons.Default.HowToReg, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Register Warden & Open Administration", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

// Backward compatibility helper for existing test GreetingScreenshotTest
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
