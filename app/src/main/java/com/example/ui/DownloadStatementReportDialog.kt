package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.*
import com.example.service.BillingReportService
import com.example.service.FileSaveResult

/**
 * Dialog enabling students to inspect, format, customize, and download their official
 * monthly billing statements as a formatted report summarizing mess usage and hostel fees.
 */
@Composable
fun DownloadStatementReportDialog(
    student: User,
    statement: StudentBillStatement,
    monthlyRecords: List<MonthlyBillingRecord>,
    transactions: List<PaymentTransaction>,
    initialSelectedRecord: MonthlyBillingRecord? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    var selectedRecord by remember {
        mutableStateOf(initialSelectedRecord ?: monthlyRecords.firstOrNull())
    }
    var reportFormat by remember { mutableStateOf("TEXT") } // "TEXT" or "HTML"
    var saveResult by remember { mutableStateOf<FileSaveResult?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var previewMode by remember { mutableStateOf(false) }

    val formattedReportText = remember(student, statement, selectedRecord, reportFormat) {
        if (reportFormat == "HTML") {
            BillingReportService.generateFormattedHtmlReport(
                student = student,
                statement = statement,
                selectedRecord = selectedRecord,
                allRecords = monthlyRecords,
                transactions = transactions
            )
        } else {
            BillingReportService.generateFormattedTextReport(
                student = student,
                statement = statement,
                selectedRecord = selectedRecord,
                allRecords = monthlyRecords,
                transactions = transactions
            )
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .testTag("download_statement_report_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Download Billing Statement",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Formatted report for Mess & Hostel fees",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Period Selector (Horizontal scrollable chips)
                Text("Select Billing Cycle / Period:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    monthlyRecords.forEach { rec ->
                        FilterChip(
                            selected = selectedRecord?.monthKey == rec.monthKey,
                            onClick = {
                                selectedRecord = rec
                                saveResult = null
                            },
                            label = { Text(rec.monthName, fontSize = 11.sp) },
                            leadingIcon = if (selectedRecord?.monthKey == rec.monthKey) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Format selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Report Format:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = reportFormat == "TEXT",
                            onClick = { reportFormat = "TEXT"; saveResult = null },
                            label = { Text("Text Ledger (.txt)", fontSize = 11.sp) },
                            leadingIcon = { Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )
                        FilterChip(
                            selected = reportFormat == "HTML",
                            onClick = { reportFormat = "HTML"; saveResult = null },
                            label = { Text("Web Document (.html)", fontSize = 11.sp) },
                            leadingIcon = { Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Summary Card of the Selected Statement
                selectedRecord?.let { rec ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        border = CardDefaults.outlinedCardBorder(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    rec.fullMonthName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Surface(
                                    color = Color(rec.status.colorHex).copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        rec.status.label,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(rec.status.colorHex),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Mess Bill (Usage)", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                    Text("₹${rec.messBilled.toInt()}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Column {
                                    Text("Hostel Room & Util", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                    Text("₹${rec.roomBilled.toInt()}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Total Billed", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                    Text("₹${rec.totalBilled.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Preview Toggle Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (previewMode) "Document Preview (Scrollable):" else "Document Preview Available",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.outline
                    )

                    TextButton(onClick = { previewMode = !previewMode }) {
                        Icon(
                            if (previewMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (previewMode) "Hide Preview" else "Show Full Preview", fontSize = 11.sp)
                    }
                }

                // Scrollable Live Document Preview Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0F172A))
                        .padding(12.dp)
                ) {
                    val scrollState = rememberScrollState()
                    val hScrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .horizontalScroll(hScrollState)
                    ) {
                        Text(
                            text = formattedReportText,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = Color(0xFFE2E8F0),
                            lineHeight = 14.sp
                        )
                    }
                }

                // Download Success Banner if saved
                AnimatedVisibility(visible = saveResult != null) {
                    saveResult?.let { res ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = Color(0xFFD1FAE5),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Report Saved Successfully!", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF065F46))
                                    Text("${res.fileName} (${res.fileSizeFormatted})", fontSize = 10.sp, color = Color(0xFF047857))
                                    Text("Saved to: ${res.filePath}", fontSize = 9.sp, color = Color(0xFF065F46).copy(alpha = 0.8f))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Share Button
                    OutlinedButton(
                        onClick = {
                            val shareIntent = BillingReportService.createShareIntent(
                                context = context,
                                reportText = formattedReportText,
                                studentName = student.name,
                                monthName = selectedRecord?.monthName ?: "Semester"
                            )
                            context.startActivity(shareIntent)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("share_statement_report_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Download Button
                    Button(
                        onClick = {
                            isSaving = true
                            val result = BillingReportService.saveReportToFile(
                                context = context,
                                content = formattedReportText,
                                isHtml = reportFormat == "HTML",
                                studentId = student.studentId,
                                monthKey = selectedRecord?.monthKey ?: "Semester"
                            )
                            isSaving = false
                            result.onSuccess { res ->
                                saveResult = res
                                Toast.makeText(context, "Saved ${res.fileName} to Downloads", Toast.LENGTH_LONG).show()
                            }.onFailure { err ->
                                Toast.makeText(context, "Download failed: ${err.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(46.dp)
                            .testTag("confirm_download_report_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (saveResult != null) "Download Again" else "Download Statement",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
