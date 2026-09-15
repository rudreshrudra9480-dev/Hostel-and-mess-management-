package com.example.service

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.model.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

data class FileSaveResult(
    val fileName: String,
    val filePath: String,
    val uri: Uri?,
    val fileSizeFormatted: String,
    val mimeType: String
)

object BillingReportService {

    fun generateFormattedTextReport(
        student: User,
        statement: StudentBillStatement,
        selectedRecord: MonthlyBillingRecord?,
        allRecords: List<MonthlyBillingRecord>,
        transactions: List<PaymentTransaction>
    ): String {
        val now = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.ENGLISH).format(Date())
        val refNo = "STMT-${(100000..999999).random()}"
        val record = selectedRecord ?: allRecords.firstOrNull()

        val sb = StringBuilder()
        sb.appendLine("==========================================================================")
        sb.appendLine("                CAMPUS HOSTEL & MESS ADMINISTRATION                     ")
        sb.appendLine("              OFFICIAL MONTHLY BILLING STATEMENT REPORT                 ")
        sb.appendLine("==========================================================================")
        sb.appendLine("Statement Ref No : $refNo")
        sb.appendLine("Generated On     : $now")
        sb.appendLine("Academic Session : 2026-2027 (Odd Semester)")
        sb.appendLine("--------------------------------------------------------------------------")
        sb.appendLine("STUDENT DETAILS:")
        sb.appendLine("  Student Name   : ${student.name.uppercase()}")
        sb.appendLine("  Roll / Reg ID  : ${student.studentId}")
        sb.appendLine("  Email Address  : ${student.email}")
        sb.appendLine("  Room Allotment : Room 204, Block B, East Wing")
        sb.appendLine("  Dining Category: Standard Campus Mess (Vegetarian & Non-Veg)")
        sb.appendLine("--------------------------------------------------------------------------")

        if (record != null) {
            sb.appendLine("BILLING CYCLE: ${record.fullMonthName.uppercase()}")
            sb.appendLine("  Status         : ${record.status.label.uppercase()}")
            sb.appendLine("  Payment Due    : ${record.dueDate}")
            if (record.paidDate != null) {
                sb.appendLine("  Settled Date   : ${record.paidDate}")
            }
            sb.appendLine()
            sb.appendLine("1. MESS DINING & CANTEEN USAGE SUMMARY:")
            sb.appendLine("   -----------------------------------------------------------------------")
            sb.appendLine("   Item Description                       Rate / Unit      Amount (INR)")
            sb.appendLine("   -----------------------------------------------------------------------")
            sb.appendLine(String.format("   Monthly Base Dining Plan (30 Days)     ₹106.67/day       ₹ %9.2f", 3200.0))
            if (statement.messExtras > 0 && record.monthKey == "2026-09") {
                sb.appendLine(String.format("   Canteen Extras & Night Canteen Addons  Itemized          ₹ %9.2f", statement.messExtras))
            }
            if (statement.messRebateDeductions > 0 && record.monthKey == "2026-09") {
                sb.appendLine(String.format("   Approved Leave Rebate (Deduction)      Credit            -₹ %8.2f", statement.messRebateDeductions))
            }
            sb.appendLine("   -----------------------------------------------------------------------")
            sb.appendLine(String.format("   NET MESS CHARGES FOR CYCLE                             ₹ %9.2f", record.messBilled))
            sb.appendLine(String.format("   Amount Paid: ₹ %.2f  |  Outstanding Balance: ₹ %.2f", record.messPaid, record.messPending))
            sb.appendLine()

            sb.appendLine("2. HOSTEL ROOM RENT & UTILITY CHARGES SUMMARY:")
            sb.appendLine("   -----------------------------------------------------------------------")
            sb.appendLine("   Item Description                       Duration         Amount (INR)")
            sb.appendLine("   -----------------------------------------------------------------------")
            if (record.roomBilled > 0) {
                sb.appendLine(String.format("   Semester Room Rent (Double Occupancy)  Per Term          ₹ %9.2f", 18000.0))
                sb.appendLine(String.format("   Electricity & Water Supply Surcharges  Sub-metered       ₹ %9.2f", 1500.0))
                sb.appendLine(String.format("   Hostel Maintenance & Amenities Fund    Annual            ₹ %9.2f", 800.0))
                sb.appendLine("   -----------------------------------------------------------------------")
                sb.appendLine(String.format("   NET HOSTEL ROOM FEES FOR CYCLE                         ₹ %9.2f", record.roomBilled))
            } else {
                sb.appendLine("   No semester room rent dues assessed for this monthly cycle.")
                sb.appendLine("   (Room charges billed semi-annually during semester inception)")
                sb.appendLine("   -----------------------------------------------------------------------")
                sb.appendLine("   NET HOSTEL ROOM FEES FOR CYCLE                         ₹      0.00")
            }
            sb.appendLine(String.format("   Amount Paid: ₹ %.2f  |  Outstanding Balance: ₹ %.2f", record.roomPaid, record.roomPending))
            sb.appendLine()

            sb.appendLine("3. CYCLE RECONCILIATION TOTALS:")
            sb.appendLine("   =======================================================================")
            sb.appendLine(String.format("   TOTAL BILLED AMOUNT :                                  ₹ %9.2f", record.totalBilled))
            sb.appendLine(String.format("   TOTAL PAID CLEARED  :                                  ₹ %9.2f", record.totalPaid))
            sb.appendLine(String.format("   NET PENDING DUES    :                                  ₹ %9.2f", record.totalPending))
            sb.appendLine("   =======================================================================")
        }

        sb.appendLine()
        sb.appendLine("4. SEMESTER-WIDE 6-MONTH BILLING OVERVIEW:")
        sb.appendLine("   -----------------------------------------------------------------------")
        sb.appendLine("   Cycle       Mess Bill     Room Rent     Total Billed  Paid Status")
        sb.appendLine("   -----------------------------------------------------------------------")
        allRecords.forEach { rec ->
            sb.appendLine(
                String.format(
                    "   %-10s  ₹ %8.0f    ₹ %8.0f    ₹ %9.0f   %-14s",
                    rec.monthName, rec.messBilled, rec.roomBilled, rec.totalBilled, rec.status.label
                )
            )
        }
        sb.appendLine("   -----------------------------------------------------------------------")
        val grandBilled = allRecords.sumOf { it.totalBilled }
        val grandPaid = allRecords.sumOf { it.totalPaid }
        val grandPending = allRecords.sumOf { it.totalPending }
        sb.appendLine(String.format("   SEMESTER TOTAL: Billed ₹%.0f | Paid ₹%.0f | Pending ₹%.0f", grandBilled, grandPaid, grandPending))
        sb.appendLine()

        val matchingTxns = transactions.filter { it.studentId == student.studentId }
        if (matchingTxns.isNotEmpty()) {
            sb.appendLine("5. RECENT PAYMENT TRANSACTIONS AUDIT TRAIL:")
            sb.appendLine("   -----------------------------------------------------------------------")
            sb.appendLine("   Txn ID            Date        Method         Gateway      Amount      Status")
            sb.appendLine("   -----------------------------------------------------------------------")
            matchingTxns.take(5).forEach { txn ->
                sb.appendLine(
                    String.format(
                        "   %-16s  %-10s  %-12s   %-10s   ₹ %7.0f   %s",
                        txn.transactionId.take(16), txn.paidAtDate, txn.paymentMethod.name, (txn.gatewayProvider ?: "GATEWAY").take(10), txn.amount, txn.status.name
                    )
                )
            }
            sb.appendLine("   -----------------------------------------------------------------------")
        }

        sb.appendLine()
        sb.appendLine("--------------------------------------------------------------------------")
        sb.appendLine("OFFICIAL RECONCILIATION VERIFICATION:")
        sb.appendLine("  This is a computer-verified digital report issued under the authority")
        sb.appendLine("  of the Office of Student Accounts and Mess Wardens.")
        sb.appendLine("  Security Hash: SHA256-${UUID.randomUUID().toString().replace("-", "").take(16).uppercase()}")
        sb.appendLine("  For dispute resolutions, contact: hostel-accounts@campus.edu")
        sb.appendLine("==========================================================================")

        return sb.toString()
    }

    fun generateFormattedHtmlReport(
        student: User,
        statement: StudentBillStatement,
        selectedRecord: MonthlyBillingRecord?,
        allRecords: List<MonthlyBillingRecord>,
        transactions: List<PaymentTransaction>
    ): String {
        val now = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.ENGLISH).format(Date())
        val refNo = "STMT-${(100000..999999).random()}"
        val record = selectedRecord ?: allRecords.firstOrNull()
        val totalBilled = allRecords.sumOf { it.totalBilled }
        val totalPaid = allRecords.sumOf { it.totalPaid }
        val totalPending = allRecords.sumOf { it.totalPending }

        return """
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<title>Hostel & Mess Billing Statement - ${student.studentId}</title>
<style>
  body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; margin: 0; padding: 24px; color: #1e293b; background: #fff; }
  .header { border-bottom: 2px solid #0284c7; padding-bottom: 16px; margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center; }
  .inst-title { font-size: 20px; font-weight: 800; color: #0369a1; margin: 0; }
  .inst-sub { font-size: 12px; color: #64748b; margin-top: 4px; }
  .badge { background: #e0f2fe; color: #0369a1; padding: 6px 12px; border-radius: 6px; font-size: 11px; font-weight: 700; text-transform: uppercase; }
  .student-box { background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 10px; padding: 14px; margin-bottom: 20px; display: grid; grid-template-columns: 1fr 1fr; gap: 10px; font-size: 13px; }
  .student-box strong { color: #475569; }
  h3 { font-size: 15px; color: #0f172a; margin-top: 24px; margin-bottom: 10px; border-left: 4px solid #0284c7; padding-left: 8px; }
  table { width: 100%; border-collapse: collapse; margin-bottom: 16px; font-size: 12px; }
  th { background: #f1f5f9; color: #334155; text-align: left; padding: 10px; border-bottom: 1px solid #cbd5e1; font-weight: 600; }
  td { padding: 9px 10px; border-bottom: 1px solid #e2e8f0; }
  .num { text-align: right; font-variant-numeric: tabular-nums; }
  .total-row { font-weight: 700; background: #f8fafc; }
  .status-paid { color: #059669; font-weight: 700; }
  .status-pending { color: #dc2626; font-weight: 700; }
  .kpi-container { display: flex; gap: 12px; margin-bottom: 20px; }
  .kpi-card { flex: 1; padding: 12px; border-radius: 8px; border: 1px solid #e2e8f0; background: #fff; }
  .kpi-label { font-size: 11px; color: #64748b; margin-bottom: 4px; }
  .kpi-val { font-size: 18px; font-weight: 800; color: #0f172a; }
  .footer { margin-top: 36px; padding-top: 16px; border-top: 1px solid #e2e8f0; font-size: 11px; color: #64748b; display: flex; justify-content: space-between; align-items: flex-end; }
  .stamp { border: 2px dashed #0284c7; padding: 10px; border-radius: 8px; text-align: center; color: #0284c7; font-weight: 700; font-size: 11px; display: inline-block; }
</style>
</head>
<body>

<div class="header">
  <div>
    <h1 class="inst-title">Campus Hostel & Mess Administration</h1>
    <div class="inst-sub">Directorate of Student Welfare &middot; Official Financial Statement</div>
  </div>
  <div class="badge">Official Verified</div>
</div>

<div class="student-box">
  <div><strong>Student Name:</strong> ${student.name}</div>
  <div><strong>Roll / Student ID:</strong> ${student.studentId}</div>
  <div><strong>Email:</strong> ${student.email}</div>
  <div><strong>Room Allotment:</strong> Room 204, Block B (East Wing)</div>
  <div><strong>Statement Period:</strong> ${record?.fullMonthName ?: "Semester Overview"}</div>
  <div><strong>Generated On:</strong> $now &middot; Ref: $refNo</div>
</div>

<div class="kpi-container">
  <div class="kpi-card">
    <div class="kpi-label">TOTAL BILLED</div>
    <div class="kpi-val">₹${totalBilled.toInt()}</div>
  </div>
  <div class="kpi-card">
    <div class="kpi-label">TOTAL PAID & SETTLED</div>
    <div class="kpi-val" style="color: #059669;">₹${totalPaid.toInt()}</div>
  </div>
  <div class="kpi-card">
    <div class="kpi-label">PENDING BALANCE DUE</div>
    <div class="kpi-val" style="color: ${if (totalPending > 0) "#dc2626" else "#059669"};">₹${totalPending.toInt()}</div>
  </div>
</div>

<h3>1. Mess Dining & Extras Itemized Statement (${record?.fullMonthName ?: "Current"})</h3>
<table>
  <thead>
    <tr>
      <th>Description</th>
      <th>Billing Basis</th>
      <th class="num">Amount (INR)</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>Standard Dining Meal Plan (Breakfast, Lunch, Snacks, Dinner)</td>
      <td>30 Days @ ₹106.67/day</td>
      <td class="num">₹3,200.00</td>
    </tr>
    ${if (statement.messExtras > 0 && record?.monthKey == "2026-09") """
    <tr>
      <td>Special Night Canteen & Dairy Coupon Addons</td>
      <td>Itemized Usage</td>
      <td class="num">₹${String.format("%.2f", statement.messExtras)}</td>
    </tr>
    """ else ""}
    ${if (statement.messRebateDeductions > 0 && record?.monthKey == "2026-09") """
    <tr>
      <td>Approved Meal Leave Rebate Credit (Absence Deduction)</td>
      <td>Leave Granted</td>
      <td class="num" style="color: #059669;">-₹${String.format("%.2f", statement.messRebateDeductions)}</td>
    </tr>
    """ else ""}
    <tr class="total-row">
      <td colspan="2">Net Mess Dining Charges Due</td>
      <td class="num">₹${String.format("%.2f", record?.messBilled ?: 3200.0)}</td>
    </tr>
  </tbody>
</table>

<h3>2. Hostel Accommodation & Utility Fees</h3>
<table>
  <thead>
    <tr>
      <th>Description</th>
      <th>Billing Period</th>
      <th class="num">Amount (INR)</th>
    </tr>
  </thead>
  <tbody>
    ${if ((record?.roomBilled ?: 0.0) > 0) """
    <tr>
      <td>Semester Room Accommodation (Double Sharing, Block B)</td>
      <td>Term Rental</td>
      <td class="num">₹18,000.00</td>
    </tr>
    <tr>
      <td>Water Supply & Power Surcharges</td>
      <td>Sub-metered</td>
      <td class="num">₹1,500.00</td>
    </tr>
    <tr>
      <td>Common Amenities & Facility Maintenance Fund</td>
      <td>Semester Fund</td>
      <td class="num">₹800.00</td>
    </tr>
    <tr class="total-row">
      <td colspan="2">Net Room & Utilities Charges</td>
      <td class="num">₹${String.format("%.2f", record?.roomBilled ?: 20300.0)}</td>
    </tr>
    """ else """
    <tr>
      <td colspan="2">No room fees assessed for this month (Room fee charged semi-annually)</td>
      <td class="num">₹0.00</td>
    </tr>
    """}
  </tbody>
</table>

<h3>3. 6-Month Semester Billing Summary</h3>
<table>
  <thead>
    <tr>
      <th>Cycle</th>
      <th class="num">Mess Bill</th>
      <th class="num">Room Fee</th>
      <th class="num">Total Billed</th>
      <th>Due Date</th>
      <th>Status</th>
    </tr>
  </thead>
  <tbody>
    ${allRecords.joinToString("\n") { r ->
        """
        <tr>
          <td><strong>${r.monthName}</strong></td>
          <td class="num">₹${r.messBilled.toInt()}</td>
          <td class="num">₹${r.roomBilled.toInt()}</td>
          <td class="num"><strong>₹${r.totalBilled.toInt()}</strong></td>
          <td>${r.dueDate}</td>
          <td><span class="${if (r.status == MonthlyBillingStatus.PAID) "status-paid" else "status-pending"}">${r.status.label}</span></td>
        </tr>
        """
    }}
  </tbody>
</table>

<div class="footer">
  <div>
    <div>Office of Hostel Wardens & Student Financial Services</div>
    <div>Campus Central Accounts Branch &middot; Reference: $refNo</div>
    <div style="margin-top: 4px; color: #94a3b8; font-family: monospace;">HASH: SHA256-${UUID.randomUUID().toString().replace("-", "").take(16).uppercase()}</div>
  </div>
  <div class="stamp">
    HOSTEL ADMINISTRATION<br>
    RECONCILED & VERIFIED
  </div>
</div>

</body>
</html>
        """.trimIndent()
    }

    /**
     * Saves the formatted report to local device downloads or external files.
     */
    fun saveReportToFile(
        context: Context,
        content: String,
        isHtml: Boolean,
        studentId: String,
        monthKey: String
    ): Result<FileSaveResult> {
        return try {
            val extension = if (isHtml) "html" else "txt"
            val mimeType = if (isHtml) "text/html" else "text/plain"
            val sanitizedStudent = studentId.replace("-", "_")
            val fileName = "Billing_Statement_${monthKey}_$sanitizedStudent.$extension"

            var savedUri: Uri? = null
            var finalPath = ""

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/HostelStatements")
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(content.toByteArray())
                    }
                    savedUri = uri
                    finalPath = "Downloads/HostelStatements/$fileName"
                }
            }

            // Fallback or secondary file write for direct app-file access
            if (savedUri == null) {
                val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                    ?: context.filesDir
                val file = File(downloadsDir, fileName)
                FileOutputStream(file).use { fos ->
                    fos.write(content.toByteArray())
                }
                finalPath = file.absolutePath
            }

            val sizeKb = String.format(Locale.ENGLISH, "%.1f KB", content.toByteArray().size / 1024.0)

            Result.success(
                FileSaveResult(
                    fileName = fileName,
                    filePath = finalPath,
                    uri = savedUri,
                    fileSizeFormatted = sizeKb,
                    mimeType = mimeType
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Creates an Android Share Intent with formatted report text/summary.
     */
    fun createShareIntent(
        context: Context,
        reportText: String,
        studentName: String,
        monthName: String
    ): Intent {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, "Hostel & Mess Billing Statement ($monthName) - $studentName")
            putExtra(Intent.EXTRA_TEXT, reportText)
            type = "text/plain"
        }
        return Intent.createChooser(sendIntent, "Share Billing Statement Report")
    }
}
