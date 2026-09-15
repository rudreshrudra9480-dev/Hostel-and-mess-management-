package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.*
import com.example.service.*
import com.example.service.MockPaymentGatewayService.getTodayDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Payment and Billing Screen for Students to pay their:
 * 1. Mess & Dining Bill (Monthly Base + Extras - Rebates)
 * 2. Hostel Room Fee (Semester Rent + Utilities + Maintenance)
 * Supports multiple payment methods: UPI, Credit/Debit Card, Net Banking, and Campus Wallet.
 * Generates official digital receipts with instant reconciliation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentBillingPaymentScreen(viewModel: HostelViewModel) {
    val authState by viewModel.authState.collectAsState()
    val student = authState.loggedInUser ?: return
    val billStatements by viewModel.billStatements.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val statement = billStatements[student.studentId] ?: StudentBillStatement(studentId = student.studentId)

    val studentTransactions = transactions.filter { it.studentId == student.studentId }

    // Selected bill to pay: MESS_BILL, HOSTEL_ROOM_FEE, COMBINED
    var selectedBillType by remember { mutableStateOf(BillType.MESS_BILL) }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.UPI) }

    // Payment Form Inputs
    var upiId by remember { mutableStateOf("student@okhdfcbank") }
    var selectedUpiApp by remember { mutableStateOf("Google Pay") }
    var cardNumber by remember { mutableStateOf("4532 8912 3456 7890") }
    var cardHolder by remember { mutableStateOf(student.name) }
    var cardExpiry by remember { mutableStateOf("08/29") }
    var cardCvv by remember { mutableStateOf("382") }
    var selectedBank by remember { mutableStateOf("State Bank of India") }

    // Mock Payment Gateway Options & Simulation Mode
    var gatewaySimMode by remember { mutableStateOf(GatewaySimulationMode.SUCCESS) }
    var activeGatewayOrder by remember { mutableStateOf<GatewayOrder?>(null) }
    var showGatewayCheckout by remember { mutableStateOf(false) }

    // Transaction History Filter States
    var historyStatusFilter by remember { mutableStateOf("ALL") } // "ALL", "SUCCESS", "FAILED"
    var historyBillFilter by remember { mutableStateOf("ALL") } // "ALL", "MESS_BILL", "HOSTEL_ROOM_FEE"
    var actionFeedbackMessage by remember { mutableStateOf<String?>(null) }

    // Dialogs and processing states
    var activeReceipt by remember { mutableStateOf<PaymentTransaction?>(null) }
    var showQrDialog by remember { mutableStateOf(false) }
    var showDownloadReportDialog by remember { mutableStateOf(false) }
    var reportTargetRecord by remember { mutableStateOf<MonthlyBillingRecord?>(null) }

    val monthlyHistory = remember(statement, studentTransactions) {
        viewModel.getMonthlyBillingHistory(student.studentId)
    }

    val coroutineScope = rememberCoroutineScope()

    // Compute active payment amount
    val paymentAmount = when (selectedBillType) {
        BillType.MESS_BILL -> statement.netMessBill
        BillType.HOSTEL_ROOM_FEE -> statement.netRoomFee
        BillType.COMBINED -> statement.totalOutstandingDue
    }

    val isCurrentSelectionPaid = when (selectedBillType) {
        BillType.MESS_BILL -> statement.isMessBillPaid
        BillType.HOSTEL_ROOM_FEE -> statement.isRoomFeePaid
        BillType.COMBINED -> statement.isMessBillPaid && statement.isRoomFeePaid
    }

    // Tab Navigation: 0 = Visual Analytics & History Dashboard, 1 = Pay Dues & Gateway
    var activeBillingTab by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Navigation Tab Row
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            TabRow(
                selectedTabIndex = activeBillingTab,
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = activeBillingTab == 0,
                    onClick = { activeBillingTab = 0 },
                    text = { Text("Billing Analytics", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_billing_analytics")
                )
                Tab(
                    selected = activeBillingTab == 1,
                    onClick = { activeBillingTab = 1 },
                    text = { Text("Pay Dues & Gateway", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_billing_pay")
                )
            }
        }

        // Global Action Feedback Banner
        actionFeedbackMessage?.let { msg ->
            Surface(
                color = Color(0xFF0F172A),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(msg, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize().weight(1f)) {
            if (activeBillingTab == 0) {
                StudentBillingAnalyticsView(
                    viewModel = viewModel,
                    student = student,
                    onNavigateToPay = { billType ->
                        selectedBillType = billType
                        activeBillingTab = 1
                    },
                    onViewReceipt = { txn ->
                        activeReceipt = txn
                    },
                    onOpenDownloadReport = { targetRecord ->
                        reportTargetRecord = targetRecord
                        showDownloadReportDialog = true
                    },
                    onShowFeedback = { msg ->
                        actionFeedbackMessage = msg
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
        // Header
        item {
            Column {
                Text(
                    "Fee & Billing Payment Portal",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Pay your monthly dining mess bill and hostel semester room fees securely.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Outstanding Summary Hero Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
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
                                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("HOSTEL ACCOUNTS DUES", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        val isAllClear = statement.totalOutstandingDue <= 0.0
                        Surface(
                            color = if (isAllClear) Color(0xFF059669) else Color(0xFFD97706),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                if (isAllClear) "ALL DUES CLEARED" else "PAYMENT DUE",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("TOTAL OUTSTANDING DUES", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "₹${statement.totalOutstandingDue.toInt()}",
                        color = Color.White,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFF334155))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("MESS BILL", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(
                                if (statement.isMessBillPaid) "PAID (₹0)" else "₹${statement.netMessBill.toInt()}",
                                color = if (statement.isMessBillPaid) Color(0xFF10B981) else Color(0xFFFBBF24),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column {
                            Text("HOSTEL ROOM FEE", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(
                                if (statement.isRoomFeePaid) "PAID (₹0)" else "₹${statement.netRoomFee.toInt()}",
                                color = if (statement.isRoomFeePaid) Color(0xFF10B981) else Color(0xFFFBBF24),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("CAMPUS WALLET", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("₹${statement.walletBalance.toInt()}", color = Color(0xFF38BDF8), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedButton(
                        onClick = {
                            reportTargetRecord = monthlyHistory.firstOrNull()
                            showDownloadReportDialog = true
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                        border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("download_statement_report_hero_btn")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download Statement Report (HTML / Text)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Two Breakdown Cards: Mess Bill vs Room Fee
        item {
            Text("Breakdown of Charges", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        // 1. Mess Bill Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFEF3C7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Restaurant, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Monthly Mess & Dining Bill", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("September 2026 Cycle", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Surface(
                            color = if (statement.isMessBillPaid) Color(0xFFD1FAE5) else Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                if (statement.isMessBillPaid) "PAID" else "DUE",
                                color = if (statement.isMessBillPaid) Color(0xFF059669) else Color(0xFFD97706),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Base Monthly Mess Rate (3 meals + snacks):", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₹${statement.monthlyMessFeeDue.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Special Feasts / Extra Dining Items:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("+ ₹${statement.messExtras.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Approved Mess Rebate Deductions (4 days leave):", fontSize = 12.sp, color = Color(0xFF059669))
                        Text("- ₹${statement.messRebateDeductions.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF059669))
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Net Mess Payable", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹${statement.netMessBill.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        }

                        if (!statement.isMessBillPaid) {
                            Button(
                                onClick = {
                                    selectedBillType = BillType.MESS_BILL
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = if (selectedBillType == BillType.MESS_BILL) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(if (selectedBillType == BillType.MESS_BILL) "Selected" else "Select to Pay", fontSize = 12.sp)
                            }
                        } else {
                            Surface(color = Color(0xFFECFDF5), shape = RoundedCornerShape(8.dp)) {
                                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Reconciled", fontSize = 11.sp, color = Color(0xFF059669), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Hostel Room Fee Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE0F2FE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.MeetingRoom, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Hostel Room Fee", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Semester Term 1 • Room ${student.roomNumber}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Surface(
                            color = if (statement.isRoomFeePaid) Color(0xFFD1FAE5) else Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                if (statement.isRoomFeePaid) "PAID" else "DUE",
                                color = if (statement.isRoomFeePaid) Color(0xFF059669) else Color(0xFFD97706),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Semester Room Rent (${student.roomType}):", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₹${statement.semesterRoomFeeDue.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Electricity, Wi-Fi & Water Utilities:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("+ ₹${statement.utilityCharges.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Hostel Maintenance & Security Fund:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("+ ₹${statement.maintenanceFund.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Net Room Fee Payable", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹${statement.netRoomFee.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        }

                        if (!statement.isRoomFeePaid) {
                            Button(
                                onClick = {
                                    selectedBillType = BillType.HOSTEL_ROOM_FEE
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = if (selectedBillType == BillType.HOSTEL_ROOM_FEE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(if (selectedBillType == BillType.HOSTEL_ROOM_FEE) "Selected" else "Select to Pay", fontSize = 12.sp)
                            }
                        } else {
                            Surface(color = Color(0xFFECFDF5), shape = RoundedCornerShape(8.dp)) {
                                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Reconciled", fontSize = 11.sp, color = Color(0xFF059669), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // PAYMENT CHECKOUT & GATEWAY SECTION
        // ==========================================
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Payment, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Payment Checkout & Methods", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Text("Select which dues to pay and your preferred payment option.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(14.dp))

                    // 1. Dues Selector
                    Text("1. Choose Bill to Pay:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedBillType == BillType.MESS_BILL,
                            onClick = { selectedBillType = BillType.MESS_BILL },
                            label = { Text("Mess (₹${statement.netMessBill.toInt()})", fontSize = 11.sp) },
                            enabled = !statement.isMessBillPaid,
                            modifier = Modifier.weight(1f)
                        )

                        FilterChip(
                            selected = selectedBillType == BillType.HOSTEL_ROOM_FEE,
                            onClick = { selectedBillType = BillType.HOSTEL_ROOM_FEE },
                            label = { Text("Room (₹${statement.netRoomFee.toInt()})", fontSize = 11.sp) },
                            enabled = !statement.isRoomFeePaid,
                            modifier = Modifier.weight(1f)
                        )

                        FilterChip(
                            selected = selectedBillType == BillType.COMBINED,
                            onClick = { selectedBillType = BillType.COMBINED },
                            label = { Text("Both (₹${statement.totalOutstandingDue.toInt()})", fontSize = 11.sp) },
                            enabled = statement.totalOutstandingDue > 0,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. Payment Method Selector
                    Text("2. Select Payment Method:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(PaymentMethod.values()) { method ->
                            FilterChip(
                                selected = method == selectedMethod,
                                onClick = { selectedMethod = method },
                                leadingIcon = {
                                    val icon = when (method) {
                                        PaymentMethod.UPI -> Icons.Default.QrCode
                                        PaymentMethod.CARD -> Icons.Default.CreditCard
                                        PaymentMethod.NET_BANKING -> Icons.Default.AccountBalance
                                        PaymentMethod.CAMPUS_WALLET -> Icons.Default.AccountBalanceWallet
                                    }
                                    Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
                                },
                                label = { Text(method.displayName.substringBefore(" "), fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Dynamic Form inputs based on selected payment method
                    when (selectedMethod) {
                        PaymentMethod.UPI -> {
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Fast UPI Checkout", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    val upiApps = listOf("Google Pay", "PhonePe", "Paytm", "BHIM UPI")
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(upiApps) { app ->
                                            Surface(
                                                color = if (app == selectedUpiApp) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.clickable { selectedUpiApp = app }
                                            ) {
                                                Text(
                                                    app,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (app == selectedUpiApp) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = upiId,
                                        onValueChange = { upiId = it },
                                        label = { Text("Enter UPI ID / VPA") },
                                        placeholder = { Text("e.g. mobile@upi or name@okhdfc") },
                                        trailingIcon = {
                                            Text("VERIFIED", color = Color(0xFF059669), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp))
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))
                                    TextButton(
                                        onClick = { showQrDialog = true },
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Scan QR Code Instead", fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        PaymentMethod.CARD -> {
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Credit / Debit Card Details", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Surface(color = Color(0xFF1E3A8A), shape = RoundedCornerShape(4.dp)) {
                                                Text("VISA", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                            }
                                            Surface(color = Color(0xFFDC2626), shape = RoundedCornerShape(4.dp)) {
                                                Text("MasterCard", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                            }
                                            Surface(color = Color(0xFF059669), shape = RoundedCornerShape(4.dp)) {
                                                Text("RuPay", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = cardNumber,
                                        onValueChange = { cardNumber = it },
                                        label = { Text("16-Digit Card Number") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = cardExpiry,
                                            onValueChange = { cardExpiry = it },
                                            label = { Text("Expiry (MM/YY)") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )

                                        OutlinedTextField(
                                            value = cardCvv,
                                            onValueChange = { cardCvv = it },
                                            label = { Text("CVV") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )
                                    }
                                }
                            }
                        }

                        PaymentMethod.NET_BANKING -> {
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Select Bank for Net Banking:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    val popularBanks = listOf("State Bank of India", "HDFC Bank", "ICICI Bank", "Axis Bank", "Punjab National Bank")
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        popularBanks.forEach { bank ->
                                            Surface(
                                                color = if (bank == selectedBank) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { selectedBank = bank }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(10.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    RadioButton(
                                                        selected = bank == selectedBank,
                                                        onClick = { selectedBank = bank },
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(bank, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        PaymentMethod.CAMPUS_WALLET -> {
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Hostel Smart Dining Wallet", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("Prepaid student balance for mess and amenities", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Text("₹${statement.walletBalance.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF0284C7))
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    if (statement.walletBalance < paymentAmount) {
                                        Text(
                                            "Insufficient wallet balance. Please choose UPI or Card, or recharge wallet.",
                                            color = MaterialTheme.colorScheme.error,
                                            fontSize = 11.sp
                                        )
                                    } else {
                                        Text(
                                            "Sufficient balance available. Funds will be auto-debited instantly.",
                                            color = Color(0xFF059669),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ==========================================
                    // MOCK PAYMENT GATEWAY SIMULATION SELECTOR
                    // ==========================================
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.SettingsInputComponent,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Gateway Simulation Mode",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                                val provider = MockPaymentGatewayService.getSuggestedProvider(selectedMethod)
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        provider.badgeText,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                "Simulate different payment gateway scenarios (3DS OTP, bank timeout, or insufficient balance):",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                            )

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(GatewaySimulationMode.values()) { mode ->
                                    FilterChip(
                                        selected = gatewaySimMode == mode,
                                        onClick = { gatewaySimMode = mode },
                                        label = { Text(mode.label, fontSize = 11.sp) }
                                    )
                                }
                            }

                            Text(
                                "• ${gatewaySimMode.description}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Final CTA Pay Button
                    val isActionDisabled = isCurrentSelectionPaid || paymentAmount <= 0 ||
                            (selectedMethod == PaymentMethod.CAMPUS_WALLET && statement.walletBalance < paymentAmount)

                    Button(
                        onClick = {
                            val order = MockPaymentGatewayService.createOrder(
                                studentId = student.studentId,
                                studentName = student.name,
                                billType = selectedBillType,
                                amount = paymentAmount
                            )
                            activeGatewayOrder = order
                            showGatewayCheckout = true
                        },
                        enabled = !isActionDisabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("proceed_payment_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (isCurrentSelectionPaid) "Selected Dues Already Paid" else "Proceed to Payment Gateway • ₹${paymentAmount.toInt()}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("256-Bit SSL Encrypted Campus Payment Gateway", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }

        // ==========================================
        // PAYMENT TRANSACTION HISTORY & STATUS TRACKING
        // ==========================================
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Payment History & Ledger", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Track transaction status, gateway reconciliation & receipts", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "${studentTransactions.size} Records",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Status Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val successCount = studentTransactions.count { it.status == PaymentStatus.SUCCESS }
                    val failedCount = studentTransactions.count { it.status == PaymentStatus.FAILED }

                    FilterChip(
                        selected = historyStatusFilter == "ALL",
                        onClick = { historyStatusFilter = "ALL" },
                        label = { Text("All (${studentTransactions.size})", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = historyStatusFilter == "SUCCESS",
                        onClick = { historyStatusFilter = "SUCCESS" },
                        leadingIcon = {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(14.dp))
                        },
                        label = { Text("Successful ($successCount)", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = historyStatusFilter == "FAILED",
                        onClick = { historyStatusFilter = "FAILED" },
                        leadingIcon = {
                            Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                        },
                        label = { Text("Failed ($failedCount)", fontSize = 11.sp) }
                    )
                }

                // Bill Type Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = historyBillFilter == "ALL",
                        onClick = { historyBillFilter = "ALL" },
                        label = { Text("All Bills", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = historyBillFilter == "MESS_BILL",
                        onClick = { historyBillFilter = "MESS_BILL" },
                        label = { Text("Mess Only", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = historyBillFilter == "HOSTEL_ROOM_FEE",
                        onClick = { historyBillFilter = "HOSTEL_ROOM_FEE" },
                        label = { Text("Room Fee Only", fontSize = 11.sp) }
                    )
                }
            }
        }

        val filteredTransactions = studentTransactions.filter { txn ->
            val statusMatches = when (historyStatusFilter) {
                "SUCCESS" -> txn.status == PaymentStatus.SUCCESS
                "FAILED" -> txn.status == PaymentStatus.FAILED
                else -> true
            }
            val billMatches = when (historyBillFilter) {
                "MESS_BILL" -> txn.billType == BillType.MESS_BILL
                "HOSTEL_ROOM_FEE" -> txn.billType == BillType.HOSTEL_ROOM_FEE
                else -> true
            }
            statusMatches && billMatches
        }

        if (filteredTransactions.isEmpty()) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No transactions found matching this filter.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredTransactions) { txn ->
                PaymentHistoryItemCard(
                    txn = txn,
                    onViewReceipt = { activeReceipt = txn },
                    onRetryPayment = {
                        // Preselect the bill type and trigger gateway checkout
                        selectedBillType = txn.billType
                        val retryOrder = MockPaymentGatewayService.createOrder(
                            studentId = student.studentId,
                            studentName = student.name,
                            billType = txn.billType,
                            amount = txn.amount
                        )
                        activeGatewayOrder = retryOrder
                        gatewaySimMode = GatewaySimulationMode.SUCCESS
                        showGatewayCheckout = true
                    }
                )
            }
        }
    }
            }
        }
    }

    // Action Feedback Snackbar Banner
    actionFeedbackMessage?.let { msg ->
        LaunchedEffect(msg) {
            delay(3000)
            actionFeedbackMessage = null
        }
    }

    // Mock Gateway Checkout Modal
    if (showGatewayCheckout && activeGatewayOrder != null) {
        val reference = when (selectedMethod) {
            PaymentMethod.UPI -> "UPI: $upiId ($selectedUpiApp)"
            PaymentMethod.CARD -> "Card ending in ${cardNumber.takeLast(4)}"
            PaymentMethod.NET_BANKING -> "NetBanking: $selectedBank"
            PaymentMethod.CAMPUS_WALLET -> "Campus Prepaid Wallet"
        }

        MockGatewayCheckoutDialog(
            order = activeGatewayOrder!!,
            method = selectedMethod,
            paymentReference = reference,
            initialMode = gatewaySimMode,
            onDismiss = {
                showGatewayCheckout = false
                activeGatewayOrder = null
            },
            onSuccess = { txn ->
                viewModel.recordGatewayTransaction(txn)
                activeReceipt = txn
            },
            onFailureRecorded = { txn ->
                viewModel.recordGatewayTransaction(txn)
            }
        )
    }

    // Digital Payment Receipt Modal
    activeReceipt?.let { receipt ->
        DigitalPaymentReceiptDialog(
            receipt = receipt,
            onDismiss = { activeReceipt = null },
            onShareOrDownload = {
                actionFeedbackMessage = "Official e-Receipt (${receipt.transactionId}) saved to downloads"
            }
        )
    }

    // QR Scan & Pay Dialog
    if (showQrDialog) {
        Dialog(onDismissRequest = { showQrDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("UPI Scan & Pay", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Scan with any UPI app to pay ₹${paymentAmount.toInt()}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(14.dp))

                    DynamicQRCodeView(
                        data = "upi://pay?pa=hostelaccounts@campus&pn=HostelAccounts&am=${paymentAmount.toInt()}&cu=INR",
                        size = 180.dp
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("UPI ID: hostelaccounts@campus", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { showQrDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close QR")
                    }
                }
            }
        }
    }

    // Monthly Billing Statement Report Download Dialog
    if (showDownloadReportDialog) {
        DownloadStatementReportDialog(
            student = student,
            statement = statement,
            monthlyRecords = monthlyHistory,
            transactions = studentTransactions,
            initialSelectedRecord = reportTargetRecord,
            onDismiss = { showDownloadReportDialog = false }
        )
    }
}

/**
 * Interactive Mock Payment Gateway Checkout Dialog simulating Razorpay/BillDesk/Bharat UPI.
 */
@Composable
fun MockGatewayCheckoutDialog(
    order: GatewayOrder,
    method: PaymentMethod,
    paymentReference: String,
    initialMode: GatewaySimulationMode,
    onDismiss: () -> Unit,
    onSuccess: (PaymentTransaction) -> Unit,
    onFailureRecorded: (PaymentTransaction) -> Unit
) {
    var currentMode by remember { mutableStateOf(initialMode) }
    var currentStep by remember { mutableStateOf("INITIATING") } // INITIATING, OTP_CHALLENGE, SUCCESS, FAILED
    var statusMessage by remember { mutableStateOf("Initializing Secure Gateway Session...") }
    var resultTxn by remember { mutableStateOf<PaymentTransaction?>(null) }
    var failureReason by remember { mutableStateOf<String?>(null) }
    var otpInput by remember { mutableStateOf("") }
    val mockGeneratedOtp = remember { "849201" }
    var otpError by remember { mutableStateOf<String?>(null) }
    var secondsRemaining by remember { mutableStateOf(45) }

    val coroutineScope = rememberCoroutineScope()

    // Automatic step progression
    LaunchedEffect(currentMode) {
        if (currentMode == GatewaySimulationMode.OTP_CHALLENGE) {
            statusMessage = "Connecting to ${method.displayName.substringBefore(" ")} Gateway Switch..."
            delay(600)
            statusMessage = "Requesting Two-Factor 3DS OTP Authentication from Issuing Bank..."
            delay(700)
            currentStep = "OTP_CHALLENGE"
        } else {
            statusMessage = "Connecting to Gateway Provider (${order.gatewayProvider})..."
            delay(600)
            statusMessage = "Authorizing transaction with central bank switch..."
            delay(700)

            when (currentMode) {
                GatewaySimulationMode.SUCCESS -> {
                    statusMessage = "Cryptographic signature verified. Reconciling transaction..."
                    delay(500)
                    val txn = PaymentTransaction(
                        transactionId = "TXN-HST-${System.currentTimeMillis().toString().takeLast(6)}",
                        orderId = order.orderId,
                        studentId = order.studentId,
                        studentName = order.studentName,
                        billType = order.billType,
                        amount = order.amount,
                        paymentMethod = method,
                        paymentReference = paymentReference,
                        status = PaymentStatus.SUCCESS,
                        paidAtDate = getTodayDate(),
                        gatewayProvider = order.gatewayProvider,
                        bankReferenceNumber = "RRN-GW-${System.currentTimeMillis().toString().takeLast(8)}",
                        signatureHash = "hmac_sha256_${System.currentTimeMillis().toString().takeLast(10)}",
                        remarks = "Approved by ${order.gatewayProvider} • Reconciled"
                    )
                    resultTxn = txn
                    currentStep = "SUCCESS"
                    onSuccess(txn)
                }
                GatewaySimulationMode.INSUFFICIENT_FUNDS -> {
                    val failTxn = PaymentTransaction(
                        transactionId = "TXN-HST-${System.currentTimeMillis().toString().takeLast(6)}",
                        orderId = order.orderId,
                        studentId = order.studentId,
                        studentName = order.studentName,
                        billType = order.billType,
                        amount = order.amount,
                        paymentMethod = method,
                        paymentReference = paymentReference,
                        status = PaymentStatus.FAILED,
                        paidAtDate = getTodayDate(),
                        gatewayProvider = order.gatewayProvider,
                        bankReferenceNumber = "RRN-FAIL-${System.currentTimeMillis().toString().takeLast(6)}",
                        failureReason = "Bank Code 51: Insufficient funds in account",
                        remarks = "Declined by issuing bank due to low balance"
                    )
                    resultTxn = failTxn
                    failureReason = failTxn.failureReason
                    currentStep = "FAILED"
                    onFailureRecorded(failTxn)
                }
                GatewaySimulationMode.BANK_TIMEOUT -> {
                    val failTxn = PaymentTransaction(
                        transactionId = "TXN-HST-${System.currentTimeMillis().toString().takeLast(6)}",
                        orderId = order.orderId,
                        studentId = order.studentId,
                        studentName = order.studentName,
                        billType = order.billType,
                        amount = order.amount,
                        paymentMethod = method,
                        paymentReference = paymentReference,
                        status = PaymentStatus.FAILED,
                        paidAtDate = getTodayDate(),
                        gatewayProvider = order.gatewayProvider,
                        bankReferenceNumber = "RRN-FAIL-${System.currentTimeMillis().toString().takeLast(6)}",
                        failureReason = "Gateway Code 504: Bank authorization network timeout",
                        remarks = "Gateway timed out waiting for issuer response"
                    )
                    resultTxn = failTxn
                    failureReason = failTxn.failureReason
                    currentStep = "FAILED"
                    onFailureRecorded(failTxn)
                }
                GatewaySimulationMode.CARD_DECLINED -> {
                    val failTxn = PaymentTransaction(
                        transactionId = "TXN-HST-${System.currentTimeMillis().toString().takeLast(6)}",
                        orderId = order.orderId,
                        studentId = order.studentId,
                        studentName = order.studentName,
                        billType = order.billType,
                        amount = order.amount,
                        paymentMethod = method,
                        paymentReference = paymentReference,
                        status = PaymentStatus.FAILED,
                        paidAtDate = getTodayDate(),
                        gatewayProvider = order.gatewayProvider,
                        bankReferenceNumber = "RRN-FAIL-${System.currentTimeMillis().toString().takeLast(6)}",
                        failureReason = "Bank Code 05: Do not honor / card payment declined by issuer",
                        remarks = "Card rejected by issuing bank fraud protection"
                    )
                    resultTxn = failTxn
                    failureReason = failTxn.failureReason
                    currentStep = "FAILED"
                    onFailureRecorded(failTxn)
                }
                else -> {}
            }
        }
    }

    Dialog(onDismissRequest = {
        if (currentStep != "INITIATING") onDismiss()
    }) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("CAMPUS PAYMENT GATEWAY", fontWeight = FontWeight.Black, fontSize = 11.sp)
                            Text(order.gatewayProvider, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }

                    Surface(
                        color = Color(0xFFD1FAE5),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            "SSL 256-Bit",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF059669),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Order and Amount Summary
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(order.billType.displayName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Order ID: ${order.orderId}", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                        }
                        Text(
                            "₹${order.amount.toInt()}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Content by Step
                when (currentStep) {
                    "INITIATING" -> {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(44.dp), strokeWidth = 3.dp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(statusMessage, fontSize = 12.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Please do not close this window or hit back", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }

                    "OTP_CHALLENGE" -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Simulated SMS Banner
                            Surface(
                                color = Color(0xFFEFF6FF),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFF93C5FD))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Sms, contentDescription = null, tint = Color(0xFF1D4ED8), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Bank SMS Notification", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D4ED8))
                                        Text(
                                            "OTP for ₹${order.amount.toInt()} payment on Campus Gateway is $mockGeneratedOtp. Valid for 10 mins.",
                                            fontSize = 10.sp,
                                            color = Color(0xFF1E3A8A)
                                        )
                                    }
                                }
                            }

                            Text("Enter 6-Digit One-Time Password (OTP)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Sent to student mobile registered with banking provider", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                            OutlinedTextField(
                                value = otpInput,
                                onValueChange = {
                                    if (it.length <= 6) {
                                        otpInput = it
                                        otpError = null
                                    }
                                },
                                label = { Text("6-Digit OTP") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth().testTag("otp_input_field"),
                                singleLine = true,
                                isError = otpError != null,
                                supportingText = {
                                    if (otpError != null) Text(otpError!!, color = MaterialTheme.colorScheme.error, fontSize = 10.sp)
                                }
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = {
                                        otpInput = mockGeneratedOtp
                                        otpError = null
                                    }
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Auto-fill ($mockGeneratedOtp)", fontSize = 11.sp)
                                }

                                Text("Expires in ${secondsRemaining}s", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            }

                            Button(
                                onClick = {
                                    if (otpInput.trim() == mockGeneratedOtp || otpInput.trim().length == 6) {
                                        coroutineScope.launch {
                                            statusMessage = "Verifying 3DS OTP Signature..."
                                            currentStep = "INITIATING"
                                            delay(700)
                                            val txn = PaymentTransaction(
                                                transactionId = "TXN-HST-${System.currentTimeMillis().toString().takeLast(6)}",
                                                orderId = order.orderId,
                                                studentId = order.studentId,
                                                studentName = order.studentName,
                                                billType = order.billType,
                                                amount = order.amount,
                                                paymentMethod = method,
                                                paymentReference = paymentReference,
                                                status = PaymentStatus.SUCCESS,
                                                paidAtDate = getTodayDate(),
                                                gatewayProvider = order.gatewayProvider,
                                                bankReferenceNumber = "RRN-OTP-${System.currentTimeMillis().toString().takeLast(8)}",
                                                signatureHash = "hmac_sha256_3ds_${System.currentTimeMillis().toString().takeLast(8)}",
                                                remarks = "3DS OTP Verified • Reconciled"
                                            )
                                            resultTxn = txn
                                            currentStep = "SUCCESS"
                                            onSuccess(txn)
                                        }
                                    } else {
                                        otpError = "Invalid OTP. Use $mockGeneratedOtp to verify."
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Verify & Authorize Payment", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier.fillMaxWidth().height(44.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Cancel Transaction")
                            }
                        }
                    }

                    "SUCCESS" -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFD1FAE5)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(32.dp))
                            }

                            Text("Payment Approved & Reconciled", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF059669))
                            Text("Transaction ID: ${resultTxn?.transactionId ?: ""}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Bank RRN: ${resultTxn?.bankReferenceNumber ?: ""}", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = onDismiss,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("View Official Reconciled Receipt", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    "FAILED" -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.errorContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(32.dp))
                            }

                            Text("Payment Declined by Gateway", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.error)
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    failureReason ?: "Transaction failed",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(10.dp),
                                    textAlign = TextAlign.Center
                                )
                            }

                            Text("Recorded in history ledger as FAILED", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    // Retry with instant success
                                    currentMode = GatewaySimulationMode.SUCCESS
                                    currentStep = "INITIATING"
                                },
                                modifier = Modifier.fillMaxWidth().height(46.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Retry Payment (Auto-Success)")
                            }

                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier.fillMaxWidth().height(44.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Close")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentHistoryItemCard(
    txn: PaymentTransaction,
    onViewReceipt: () -> Unit,
    onRetryPayment: () -> Unit
) {
    val isSuccess = txn.status == PaymentStatus.SUCCESS
    var showAuditDetails by remember { mutableStateOf(false) }

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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isSuccess) Color(0xFFD1FAE5) else Color(0xFFFEE2E2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = if (isSuccess) Color(0xFF059669) else Color(0xFFDC2626),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(txn.billType.displayName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = if (isSuccess) Color(0xFFECFDF5) else Color(0xFFFEF2F2),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    if (isSuccess) "VERIFIED" else "FAILED",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSuccess) Color(0xFF059669) else Color(0xFFDC2626),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text("${txn.transactionId} • ${txn.paidAtDate}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "Via: ${txn.paymentMethod.displayName.substringBefore(" ")} (${txn.gatewayProvider ?: "Hostel Switch"})",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "₹${txn.amount.toInt()}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = if (isSuccess) Color(0xFF059669) else Color(0xFFDC2626)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    if (isSuccess) {
                        TextButton(
                            onClick = onViewReceipt,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Receipt", fontSize = 11.sp)
                        }
                    } else {
                        Button(
                            onClick = onRetryPayment,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Retry", fontSize = 11.sp)
                        }
                    }
                }
            }

            // Failure Banner if failed
            if (!isSuccess && txn.failureReason != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color(0xFFFEF2F2),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Reason: ${txn.failureReason}",
                            fontSize = 10.sp,
                            color = Color(0xFFB91C1C),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Expandable Audit Trail
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAuditDetails = !showAuditDetails }
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (showAuditDetails) "Hide Gateway Audit Trail" else "View Gateway Audit Trail",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    if (showAuditDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
            }

            if (showAuditDetails) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Order ID: ${txn.orderId ?: "N/A"}", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                        Text("Bank RRN / UTR: ${txn.bankReferenceNumber ?: "N/A"}", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                        if (txn.signatureHash != null) {
                            Text("HMAC Signature: ${txn.signatureHash}", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                        }
                        Text("Remarks: ${txn.remarks}", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }
    }
}

@Composable
fun DigitalPaymentReceiptDialog(
    receipt: PaymentTransaction,
    onDismiss: () -> Unit,
    onShareOrDownload: () -> Unit = {}
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Official Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0F172A))
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("CAMPUS HOSTEL ACCOUNTS RECEIPT", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD1FAE5)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(32.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("PAYMENT SUCCESSFUL", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color(0xFF0F172A))
                Text("Transaction ID: ${receipt.transactionId}", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ReceiptRow("Student Name", receipt.studentName)
                        ReceiptRow("Roll Number", receipt.studentId)
                        ReceiptRow("Bill Category", receipt.billType.displayName)
                        ReceiptRow("Gateway Provider", receipt.gatewayProvider ?: "Campus Razorpay Gateway")
                        ReceiptRow("Bank RRN / UTR", receipt.bankReferenceNumber ?: "RRN-993817294819")
                        ReceiptRow("Payment Method", receipt.paymentMethod.displayName)
                        ReceiptRow("Reference", receipt.paymentReference)
                        ReceiptRow("Date of Payment", receipt.paidAtDate)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFE2E8F0))
                        ReceiptRow("Total Paid", "₹${receipt.amount.toInt()}", isBold = true, isHighlight = true)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Verification Stamp
                Surface(
                    color = Color(0xFFECFDF5),
                    shape = RoundedCornerShape(8.dp),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("OFFICIALLY VERIFIED & RECONCILED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onShareOrDownload,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
                    ) {
                        Text("Done", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ReceiptRow(label: String, value: String, isBold: Boolean = false, isHighlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 11.sp, color = Color(0xFF64748B))
        Text(
            value,
            fontSize = if (isHighlight) 14.sp else 11.sp,
            fontWeight = if (isBold || isHighlight) FontWeight.Bold else FontWeight.Medium,
            color = if (isHighlight) Color(0xFF059669) else Color(0xFF0F172A)
        )
    }
}
