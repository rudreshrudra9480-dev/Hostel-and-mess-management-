package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
            title = "Admin & Warden Portal",
            subtitle = "Authorized hostel staff & manager sign-in",
            identifierLabel = "Admin Email or Staff ID",
            defaultDemoId = "admin@hostel.edu",
            defaultDemoPass = "admin123",
            accentColor = Color(0xFF0284C7),
            viewModel = viewModel,
            onBack = { viewModel.navigateToPortal(PortalType.GATEWAY) }
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
            onBack = { viewModel.navigateToPortal(PortalType.GATEWAY) }
        )
        PortalType.ADMIN_PANEL -> AdminMainContainer(
            viewModel = viewModel,
            onLogout = { viewModel.logout() }
        )
        PortalType.STUDENT_PORTAL -> StudentMainContainer(
            viewModel = viewModel,
            onLogout = { viewModel.logout() }
        )
    }
}

// Gateway Screen for Bifurcating Access
@Composable
fun BifurcatedPortalGateway(viewModel: HostelViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Identity Header
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF0F172A)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Apartment,
                contentDescription = null,
                tint = Color(0xFF38BDF8),
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Campus Hostel & Mess",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            "QR-Based Management & Anti-Proxy Tracking",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            "SELECT PORTAL TO PROCEED",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.outline,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Option 1: Admin / Warden Portal Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.navigateToPortal(PortalType.ADMIN_LOGIN) }
                .testTag("select_admin_portal_btn")
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0284C7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Admin & Staff Portal", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
                    }
                    Text(
                        "Registration, Menu Timings, Rooms, Scanner & Billing",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Option 2: Student Resident Portal Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = CardDefaults.outlinedCardBorder(),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.navigateToPortal(PortalType.STUDENT_LOGIN) }
                .testTag("select_student_portal_btn")
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF059669)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.School,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Student Resident Portal", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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

        Spacer(modifier = Modifier.height(28.dp))

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
                Text("Role-Isolated RBAC & Time-Sensitive Anti-Proxy QR", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
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
    onBack: () -> Unit
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
            .padding(24.dp),
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
            Text("Switch Access Panel", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Role Icon & Title
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (role == UserRole.ADMIN) Icons.Default.AdminPanelSettings else Icons.Default.School,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
        Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(28.dp))

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

        // Input 1: Identifier (Email/Admin ID or Student Roll No)
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
                Text("Sign In to ${if (role == UserRole.ADMIN) "Admin Dashboard" else "Student Portal"}", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Autofill Quick Helper for demo & verification
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Key, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Demo Credentials Available:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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

// Backward compatibility helper for existing test GreetingScreenshotTest
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
