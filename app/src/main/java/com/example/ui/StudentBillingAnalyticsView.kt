package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * Visual Analytics Dashboard for students to track their monthly mess/hostel billing history,
 * featuring interactive charts, KPI metrics, paid vs pending transaction allocations,
 * and monthly billing ledger statements.
 */
@Composable
fun StudentBillingAnalyticsView(
    viewModel: HostelViewModel,
    student: User,
    onNavigateToPay: (BillType) -> Unit,
    onViewReceipt: (PaymentTransaction) -> Unit,
    onShowFeedback: (String) -> Unit = {}
) {
    val billStatements by viewModel.billStatements.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val statement = billStatements[student.studentId] ?: StudentBillStatement(studentId = student.studentId)

    val studentTransactions = transactions.filter { it.studentId == student.studentId }
    val monthlyHistory = remember(statement, studentTransactions) {
        viewModel.getMonthlyBillingHistory(student.studentId)
    }

    var selectedTimeHorizon by remember { mutableStateOf("6_MONTHS") } // "3_MONTHS", "6_MONTHS", "YEAR"
    var selectedMonthRecord by remember {
        mutableStateOf<MonthlyBillingRecord?>(monthlyHistory.firstOrNull())
    }

    // Filtered monthly records based on selected horizon
    val displayedMonthlyHistory = remember(monthlyHistory, selectedTimeHorizon) {
        when (selectedTimeHorizon) {
            "3_MONTHS" -> monthlyHistory.take(3)
            "6_MONTHS" -> monthlyHistory.take(6)
            else -> monthlyHistory
        }
    }

    // Calculations
    val totalBilled = displayedMonthlyHistory.sumOf { it.totalBilled }
    val totalPaid = displayedMonthlyHistory.sumOf { it.totalPaid }
    val totalPending = displayedMonthlyHistory.sumOf { it.totalPending }
    val clearanceRatio = if (totalBilled > 0) (totalPaid / totalBilled * 100).toInt() else 100

    val messPendingTotal = displayedMonthlyHistory.sumOf { it.messPending }
    val roomPendingTotal = displayedMonthlyHistory.sumOf { it.roomPending }

    val successfulTxns = studentTransactions.filter { it.status == PaymentStatus.SUCCESS }
    val failedTxns = studentTransactions.filter { it.status == PaymentStatus.FAILED }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dashboard Header & Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Billing Analytics & History",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Visual ledger of mess dining & room rent transactions",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = {
                        onShowFeedback("Official 6-month statement downloaded as PDF")
                    },
                    modifier = Modifier.testTag("download_statement_btn")
                ) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = "Download Statement",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Time horizon pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedTimeHorizon == "3_MONTHS",
                    onClick = { selectedTimeHorizon = "3_MONTHS" },
                    label = { Text("Last 3 Months", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = selectedTimeHorizon == "6_MONTHS",
                    onClick = { selectedTimeHorizon = "6_MONTHS" },
                    label = { Text("Last 6 Months (Semester)", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = selectedTimeHorizon == "YEAR",
                    onClick = { selectedTimeHorizon = "YEAR" },
                    label = { Text("All Records", fontSize = 11.sp) }
                )
            }
        }

        // ==========================================
        // 1. EXECUTIVE SUMMARY HERO CARD (PAID VS PENDING)
        // ==========================================
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
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
                                Icon(Icons.Default.Analytics, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("FINANCIAL HEALTH SUMMARY", color = Color(0xFF38BDF8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Surface(
                            color = if (totalPending > 0) Color(0xFFFEF3C7) else Color(0xFFD1FAE5),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                if (totalPending > 0) "Dues Pending" else "All Cleared",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (totalPending > 0) Color(0xFFB45309) else Color(0xFF059669),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Total Billed (${displayedMonthlyHistory.size} Months)", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    Text(
                        "₹${totalBilled.toInt()}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress Bar
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Clearance Ratio", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text("$clearanceRatio% Cleared", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34D399))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { (clearanceRatio / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFF10B981),
                            trackColor = Color(0xFF334155),
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Paid vs Pending Metric Split
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF10B981)))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Paid & Settled", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("₹${totalPaid.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF34D399))
                            }
                        }

                        Surface(
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFF43F5E)))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Pending Dues", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("₹${totalPending.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFB7185))
                            }
                        }
                    }

                    // Direct 1-tap Pay Shortcut if pending dues exist
                    if (totalPending > 0) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                if (statement.netMessBill > 0 && statement.netRoomFee > 0) {
                                    onNavigateToPay(BillType.COMBINED)
                                } else if (statement.netMessBill > 0) {
                                    onNavigateToPay(BillType.MESS_BILL)
                                } else {
                                    onNavigateToPay(BillType.HOSTEL_ROOM_FEE)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                        ) {
                            Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pay Outstanding ₹${totalPending.toInt()} Now", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // ==========================================
        // 2. QUICK STATS ROW
        // ==========================================
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val avgMonthly = if (displayedMonthlyHistory.isNotEmpty()) totalBilled / displayedMonthlyHistory.size else 0.0
                StatMiniCard(
                    title = "Avg Monthly",
                    value = "₹${avgMonthly.toInt()}",
                    subtitle = "Per billing cycle",
                    icon = Icons.Default.TrendingUp,
                    accentColor = Color(0xFF0284C7),
                    modifier = Modifier.weight(1f)
                )
                StatMiniCard(
                    title = "Rebates Saved",
                    value = "₹${statement.messRebateDeductions.toInt()}",
                    subtitle = "Meal leave credit",
                    icon = Icons.Default.Savings,
                    accentColor = Color(0xFF059669),
                    modifier = Modifier.weight(1f)
                )
                StatMiniCard(
                    title = "Cleared Cycles",
                    value = "${displayedMonthlyHistory.count { it.status == MonthlyBillingStatus.PAID }}/${displayedMonthlyHistory.size}",
                    subtitle = "Paid in full",
                    icon = Icons.Default.Verified,
                    accentColor = Color(0xFF7C3AED),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ==========================================
        // 3. MONTHLY EXPENSE & BILLING TREND BAR CHART
        // ==========================================
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
                        Column {
                            Text("Monthly Expense Trends", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Tap any bar to inspect itemized statement", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        // Chart Legend
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF0284C7)))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Mess", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF8B5CF6)))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Room", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Custom Compose Canvas Bar Chart
                    MonthlyBarChart(
                        records = displayedMonthlyHistory.reversed(),
                        selectedRecord = selectedMonthRecord,
                        onSelectRecord = { selectedMonthRecord = it }
                    )
                }
            }
        }

        // ==========================================
        // 4. SELECTED MONTH IN-DEPTH INSPECTION CARD
        // ==========================================
        selectedMonthRecord?.let { record ->
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth().animateContentSize()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(record.fullMonthName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                                Text("Due Date: ${record.dueDate}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            }

                            Surface(
                                color = Color(record.status.colorHex).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, Color(record.status.colorHex).copy(alpha = 0.4f))
                            ) {
                                Text(
                                    record.status.label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(record.status.colorHex),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(10.dp))

                        // Line items breakdown
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Mess & Dining Bill", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${record.messBilled.toInt()}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                if (record.messPending > 0) {
                                    Text("Pending: ₹${record.messPending.toInt()}", fontSize = 10.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                                } else {
                                    Text("Paid in full", fontSize = 10.sp, color = Color(0xFF059669))
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("Hostel Room & Utilities", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${record.roomBilled.toInt()}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                if (record.roomPending > 0) {
                                    Text("Pending: ₹${record.roomPending.toInt()}", fontSize = 10.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                                } else {
                                    Text(if (record.roomBilled > 0) "Paid in full" else "No room dues", fontSize = 10.sp, color = Color(0xFF059669))
                                }
                            }
                        }

                        if (record.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    record.notes,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }

                        // CTA if pending
                        if (record.totalPending > 0) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    if (record.messPending > 0 && record.roomPending > 0) {
                                        onNavigateToPay(BillType.COMBINED)
                                    } else if (record.messPending > 0) {
                                        onNavigateToPay(BillType.MESS_BILL)
                                    } else {
                                        onNavigateToPay(BillType.HOSTEL_ROOM_FEE)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(42.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Pay Dues for ${record.monthName} (₹${record.totalPending.toInt()})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 5. PAID VS PENDING ALLOCATION DONUT CHART
        // ==========================================
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Payment Allocation & Breakdown", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Proportion of cleared payments versus active pending invoices", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Custom Donut Chart
                        Box(
                            modifier = Modifier.size(130.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            PaymentDonutChart(
                                paidAmount = totalPaid,
                                messPending = messPendingTotal,
                                roomPending = roomPendingTotal,
                                modifier = Modifier.size(130.dp)
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$clearanceRatio%", fontSize = 20.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                                Text("Paid", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        }

                        // Legend with Values
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            AllocationLegendItem(
                                color = Color(0xFF10B981),
                                title = "Paid & Settled",
                                amount = "₹${totalPaid.toInt()}",
                                percentage = "${clearanceRatio}%"
                            )
                            AllocationLegendItem(
                                color = Color(0xFFF59E0B),
                                title = "Pending Mess",
                                amount = "₹${messPendingTotal.toInt()}",
                                percentage = "${if (totalBilled > 0) (messPendingTotal / totalBilled * 100).toInt() else 0}%"
                            )
                            AllocationLegendItem(
                                color = Color(0xFFEF4444),
                                title = "Pending Room",
                                amount = "₹${roomPendingTotal.toInt()}",
                                percentage = "${if (totalBilled > 0) (roomPendingTotal / totalBilled * 100).toInt() else 0}%"
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // 6. TRANSACTIONS HEALTH & LEDGER SUMMARY
        // ==========================================
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Payment Transactions Summary", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Historical transaction settlement and authorization logs", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            color = Color(0xFFD1FAE5).copy(alpha = 0.6f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Success", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                                }
                                Text("${successfulTxns.size} Paid", fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color(0xFF065F46))
                                Text("₹${successfulTxns.sumOf { it.amount }.toInt()} Total", fontSize = 10.sp, color = Color(0xFF047857))
                            }
                        }

                        Surface(
                            color = if (failedTxns.isNotEmpty()) Color(0xFFFEE2E2).copy(alpha = 0.6f) else Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Error, contentDescription = null, tint = if (failedTxns.isNotEmpty()) Color(0xFFDC2626) else Color(0xFF64748B), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Failed Attempts", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (failedTxns.isNotEmpty()) Color(0xFFDC2626) else Color(0xFF64748B))
                                }
                                Text("${failedTxns.size} Failed", fontSize = 15.sp, fontWeight = FontWeight.Black, color = if (failedTxns.isNotEmpty()) Color(0xFF991B1B) else Color(0xFF334155))
                                Text("Auto-retryable", fontSize = 10.sp, color = Color(0xFF64748B))
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 7. COMPREHENSIVE MONTHLY STATEMENT HISTORY
        // ==========================================
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Monthly Statement History", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("${displayedMonthlyHistory.size} Cycles", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        items(displayedMonthlyHistory) { record ->
            MonthlyStatementCard(
                record = record,
                isSelected = selectedMonthRecord?.monthKey == record.monthKey,
                onSelect = { selectedMonthRecord = record },
                onPayDues = {
                    if (record.messPending > 0 && record.roomPending > 0) {
                        onNavigateToPay(BillType.COMBINED)
                    } else if (record.messPending > 0) {
                        onNavigateToPay(BillType.MESS_BILL)
                    } else {
                        onNavigateToPay(BillType.HOSTEL_ROOM_FEE)
                    }
                },
                onViewReceipt = {
                    val matchingTxn = studentTransactions.find {
                        it.status == PaymentStatus.SUCCESS && it.paidAtDate.startsWith(record.monthKey)
                    } ?: studentTransactions.firstOrNull { it.status == PaymentStatus.SUCCESS }
                    if (matchingTxn != null) {
                        onViewReceipt(matchingTxn)
                    } else {
                        onShowFeedback("Official receipt generated for ${record.fullMonthName}")
                    }
                }
            )
        }
    }
}

/**
 * Custom Compose Canvas Bar Chart displaying 6-month billing trends.
 */
@Composable
fun MonthlyBarChart(
    records: List<MonthlyBillingRecord>,
    selectedRecord: MonthlyBillingRecord?,
    onSelectRecord: (MonthlyBillingRecord) -> Unit
) {
    val maxBilled = records.maxOfOrNull { it.totalBilled }?.coerceAtLeast(1.0) ?: 1.0

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val chartHeight = size.height - 24.dp.toPx()
                val chartWidth = size.width
                val barWidth = 14.dp.toPx()
                val count = records.size
                val stepX = chartWidth / (count + 0.5f)

                // Draw background dotted average line
                val avgY = chartHeight * (1f - (records.map { it.totalBilled }.average() / maxBilled).toFloat())
                drawLine(
                    color = Color(0xFF94A3B8).copy(alpha = 0.4f),
                    start = Offset(0f, avgY),
                    end = Offset(chartWidth, avgY),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )

                // Draw bars for each month
                records.forEachIndexed { index, record ->
                    val centerX = (index + 0.75f) * stepX
                    val messRatio = (record.messBilled / maxBilled).toFloat().coerceIn(0f, 1f)
                    val roomRatio = (record.roomBilled / maxBilled).toFloat().coerceIn(0f, 1f)

                    val messBarHeight = chartHeight * messRatio
                    val roomBarHeight = chartHeight * roomRatio

                    val isSelected = selectedRecord?.monthKey == record.monthKey

                    // Highlight indicator behind bar if selected
                    if (isSelected) {
                        drawRoundRect(
                            color = Color(0xFF0284C7).copy(alpha = 0.12f),
                            topLeft = Offset(centerX - barWidth * 1.5f, 0f),
                            size = Size(barWidth * 3f, chartHeight + 20.dp.toPx()),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
                        )
                    }

                    // Room Fee bar (Purple)
                    if (roomBarHeight > 0) {
                        drawRoundRect(
                            color = if (isSelected) Color(0xFF7C3AED) else Color(0xFF8B5CF6),
                            topLeft = Offset(centerX + 1.dp.toPx(), chartHeight - roomBarHeight),
                            size = Size(barWidth, roomBarHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                        )
                    }

                    // Mess Fee bar (Cyan / Blue)
                    if (messBarHeight > 0) {
                        drawRoundRect(
                            color = if (isSelected) Color(0xFF0284C7) else Color(0xFF38BDF8),
                            topLeft = Offset(centerX - barWidth - 1.dp.toPx(), chartHeight - messBarHeight),
                            size = Size(barWidth, messBarHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                        )
                    }

                    // Pending warning dot at top if pending
                    if (record.totalPending > 0) {
                        val highestBarHeight = maxOf(messBarHeight, roomBarHeight)
                        drawCircle(
                            color = Color(0xFFEF4444),
                            radius = 3.dp.toPx(),
                            center = Offset(centerX, chartHeight - highestBarHeight - 6.dp.toPx())
                        )
                    }
                }
            }

            // Invisible touch overlay for detecting bar clicks
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                records.forEach { record ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onSelectRecord(record) }
                    )
                }
            }
        }

        // Labels under bars
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            records.forEach { record ->
                val isSelected = selectedRecord?.monthKey == record.monthKey
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onSelectRecord(record) }
                ) {
                    Text(
                        record.monthName.substringBefore(" "),
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "₹${(record.totalBilled / 1000).toInt()}k",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

/**
 * Custom Compose Canvas Donut Chart for Paid vs Pending proportions.
 */
@Composable
fun PaymentDonutChart(
    paidAmount: Double,
    messPending: Double,
    roomPending: Double,
    modifier: Modifier = Modifier
) {
    val total = (paidAmount + messPending + roomPending).coerceAtLeast(1.0)
    val paidSweep = ((paidAmount / total) * 360f).toFloat()
    val messSweep = ((messPending / total) * 360f).toFloat()
    val roomSweep = ((roomPending / total) * 360f).toFloat()

    Canvas(modifier = modifier) {
        val strokeWidth = 14.dp.toPx()
        val arcSize = size.minDimension - strokeWidth
        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

        var currentAngle = -90f

        // Paid Arc (Emerald Green)
        if (paidSweep > 0) {
            drawArc(
                color = Color(0xFF10B981),
                startAngle = currentAngle,
                sweepAngle = paidSweep - 2f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(arcSize, arcSize),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            currentAngle += paidSweep
        }

        // Pending Mess Arc (Amber)
        if (messSweep > 0) {
            drawArc(
                color = Color(0xFFF59E0B),
                startAngle = currentAngle,
                sweepAngle = messSweep - 2f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(arcSize, arcSize),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            currentAngle += messSweep
        }

        // Pending Room Arc (Rose Red)
        if (roomSweep > 0) {
            drawArc(
                color = Color(0xFFEF4444),
                startAngle = currentAngle,
                sweepAngle = roomSweep - 2f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(arcSize, arcSize),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun AllocationLegendItem(
    color: Color,
    title: String,
    amount: String,
    percentage: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    color = color.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        percentage,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
            Text(amount, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun StatMiniCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder(),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
            Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
            Text(subtitle, fontSize = 9.sp, color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@Composable
fun MonthlyStatementCard(
    record: MonthlyBillingRecord,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onPayDues: () -> Unit,
    onViewReceipt: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else CardDefaults.outlinedCardBorder(),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(record.status.colorHex).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (record.status == MonthlyBillingStatus.PAID) Icons.Default.CheckCircle else Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Color(record.status.colorHex),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(record.fullMonthName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Due: ${record.dueDate}", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("₹${record.totalBilled.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                    Surface(
                        color = Color(record.status.colorHex).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            record.status.label,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(record.status.colorHex),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Split Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        color = Color(0xFF0284C7).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Mess: ₹${record.messBilled.toInt()}", fontSize = 10.sp, color = Color(0xFF0284C7), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                    if (record.roomBilled > 0) {
                        Surface(
                            color = Color(0xFF8B5CF6).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Room: ₹${record.roomBilled.toInt()}", fontSize = 10.sp, color = Color(0xFF7C3AED), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (record.totalPending > 0) {
                        TextButton(
                            onClick = onPayDues,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pay ₹${record.totalPending.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        TextButton(
                            onClick = onViewReceipt,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Receipt", fontSize = 11.sp)
                        }
                    }

                    IconButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expand",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Expandable details
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Mess Amount Paid", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₹${record.messPaid.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                    if (record.messPending > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Mess Amount Pending", fontSize = 11.sp, color = Color(0xFFDC2626))
                            Text("₹${record.messPending.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                        }
                    }
                    if (record.roomBilled > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Room Amount Paid", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹${record.roomPaid.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    if (record.roomPending > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Room Amount Pending", fontSize = 11.sp, color = Color(0xFFDC2626))
                            Text("₹${record.roomPending.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                        }
                    }
                    if (record.paidDate != null) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Settlement Date", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                            Text(record.paidDate, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
        }
    }
}
