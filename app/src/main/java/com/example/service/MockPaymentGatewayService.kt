package com.example.service

import com.example.model.BillType
import com.example.model.PaymentMethod
import com.example.model.PaymentStatus
import com.example.model.PaymentTransaction
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Modes for the Mock Payment Gateway to simulate realistic transaction outcomes
 * including 3D Secure OTP challenge, bank network delays, card declines, and successful reconciliation.
 */
enum class GatewaySimulationMode(val label: String, val description: String) {
    SUCCESS("Instant Success", "Direct authorization & instant cryptographic receipt"),
    OTP_CHALLENGE("3D-Secure Banking OTP", "Prompts for 6-digit SMS OTP challenge with auto-fill"),
    INSUFFICIENT_FUNDS("Insufficient Balance", "Simulates bank rejection due to low balance"),
    BANK_TIMEOUT("Gateway Timeout", "Simulates network latency and gateway failure"),
    CARD_DECLINED("Issuer Declined", "Simulates bank fraud check or card block")
}

enum class GatewayProvider(val providerName: String, val badgeText: String) {
    CAMPUS_RAZORPAY("Campus Razorpay Gateway", "Razorpay Sec"),
    BILLDESK_EDUPAY("BillDesk EduPay Gateway", "BillDesk 3DS"),
    BHARAT_UPI_SWITCH("Bharat UPI Instant Switch", "UPI-NPCI"),
    HOSTEL_WALLET_VAULT("Hostel Smart Campus Vault", "Internal Vault")
}

data class GatewayOrder(
    val orderId: String = "ORD_HST_" + (100000 + (Math.random() * 900000).toInt()),
    val studentId: String,
    val studentName: String,
    val amount: Double,
    val billType: BillType,
    val currency: String = "INR",
    val gatewayProvider: String = "Campus Razorpay Gateway",
    val createdAt: Long = System.currentTimeMillis()
)

data class GatewayPaymentResult(
    val isSuccess: Boolean,
    val orderId: String,
    val transaction: PaymentTransaction,
    val errorCode: String? = null,
    val errorMessage: String? = null
)

/**
 * Service simulating an institutional payment gateway (Razorpay / BillDesk / Bharat UPI).
 * Handles payment session initialization, 2FA/OTP simulation, cryptographic signature generation,
 * webhook reconciliation, and transaction ledger recording.
 */
object MockPaymentGatewayService {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun getTodayDate(): String = dateFormat.format(Date())

    fun createOrder(
        studentId: String,
        studentName: String,
        billType: BillType,
        amount: Double,
        gatewayProvider: String = "Campus Razorpay Gateway"
    ): GatewayOrder {
        return GatewayOrder(
            studentId = studentId,
            studentName = studentName,
            billType = billType,
            amount = amount,
            gatewayProvider = gatewayProvider
        )
    }

    fun getSuggestedProvider(method: PaymentMethod): GatewayProvider {
        return when (method) {
            PaymentMethod.UPI -> GatewayProvider.BHARAT_UPI_SWITCH
            PaymentMethod.CARD -> GatewayProvider.CAMPUS_RAZORPAY
            PaymentMethod.NET_BANKING -> GatewayProvider.BILLDESK_EDUPAY
            PaymentMethod.CAMPUS_WALLET -> GatewayProvider.HOSTEL_WALLET_VAULT
        }
    }

    /**
     * Executes mock payment gateway processing with simulated steps and error conditions.
     */
    suspend fun processGatewayTransaction(
        order: GatewayOrder,
        method: PaymentMethod,
        paymentReference: String,
        mode: GatewaySimulationMode,
        onStepUpdate: (String) -> Unit
    ): GatewayPaymentResult {
        val provider = getSuggestedProvider(method)

        onStepUpdate("Initializing secure session with ${provider.providerName}...")
        delay(450)

        onStepUpdate("Authenticating credentials with ${method.displayName.substringBefore(" ")} provider...")
        delay(550)

        val todayDate = dateFormat.format(Date())
        val generatedTxnId = "TXN-HST-${System.currentTimeMillis().toString().takeLast(6)}"
        val mockRrn = "RRN" + (100000000000L + (Math.random() * 900000000000L).toLong())
        val mockSig = "hmac_sha256_" + UUID.randomUUID().toString().take(12)

        return when (mode) {
            GatewaySimulationMode.SUCCESS -> {
                onStepUpdate("Cryptographic signature verified. Reconciling with campus ledger...")
                delay(400)

                val txn = PaymentTransaction(
                    transactionId = generatedTxnId,
                    orderId = order.orderId,
                    studentId = order.studentId,
                    studentName = order.studentName,
                    billType = order.billType,
                    amount = order.amount,
                    paymentMethod = method,
                    paymentReference = paymentReference,
                    status = PaymentStatus.SUCCESS,
                    paidAtDate = todayDate,
                    gatewayProvider = provider.providerName,
                    bankReferenceNumber = mockRrn,
                    signatureHash = mockSig,
                    remarks = "Reconciled via ${provider.providerName} • Instant settlement"
                )
                GatewayPaymentResult(isSuccess = true, orderId = order.orderId, transaction = txn)
            }

            GatewaySimulationMode.INSUFFICIENT_FUNDS -> {
                onStepUpdate("Declined by issuing bank: Insufficient account balance.")
                delay(300)
                val txn = PaymentTransaction(
                    transactionId = generatedTxnId,
                    orderId = order.orderId,
                    studentId = order.studentId,
                    studentName = order.studentName,
                    billType = order.billType,
                    amount = order.amount,
                    paymentMethod = method,
                    paymentReference = paymentReference,
                    status = PaymentStatus.FAILED,
                    paidAtDate = todayDate,
                    gatewayProvider = provider.providerName,
                    bankReferenceNumber = mockRrn,
                    failureReason = "Bank error (ERR_104): Insufficient funds in student bank account.",
                    remarks = "Payment failed at issuing bank authorization."
                )
                GatewayPaymentResult(
                    isSuccess = false,
                    orderId = order.orderId,
                    transaction = txn,
                    errorCode = "ERR_INSUFFICIENT_FUNDS",
                    errorMessage = "Your bank account has insufficient balance to complete this ₹${order.amount.toInt()} payment."
                )
            }

            GatewaySimulationMode.BANK_TIMEOUT -> {
                onStepUpdate("Connecting to banking switch...")
                delay(700)
                onStepUpdate("Gateway network response timed out (504).")
                val txn = PaymentTransaction(
                    transactionId = generatedTxnId,
                    orderId = order.orderId,
                    studentId = order.studentId,
                    studentName = order.studentName,
                    billType = order.billType,
                    amount = order.amount,
                    paymentMethod = method,
                    paymentReference = paymentReference,
                    status = PaymentStatus.FAILED,
                    paidAtDate = todayDate,
                    gatewayProvider = provider.providerName,
                    bankReferenceNumber = mockRrn,
                    failureReason = "Gateway timeout (ERR_504): Bank server did not respond within 30s.",
                    remarks = "Transaction timed out. Dues remain unpaid."
                )
                GatewayPaymentResult(
                    isSuccess = false,
                    orderId = order.orderId,
                    transaction = txn,
                    errorCode = "ERR_GATEWAY_TIMEOUT",
                    errorMessage = "Banking server connection timed out. If money was debited, it will be refunded within 2 hours."
                )
            }

            GatewaySimulationMode.CARD_DECLINED -> {
                onStepUpdate("Card security check failed. Transaction declined by card issuer.")
                delay(300)
                val txn = PaymentTransaction(
                    transactionId = generatedTxnId,
                    orderId = order.orderId,
                    studentId = order.studentId,
                    studentName = order.studentName,
                    billType = order.billType,
                    amount = order.amount,
                    paymentMethod = method,
                    paymentReference = paymentReference,
                    status = PaymentStatus.FAILED,
                    paidAtDate = todayDate,
                    gatewayProvider = provider.providerName,
                    bankReferenceNumber = mockRrn,
                    failureReason = "Issuer decline (ERR_05): Card blocked for online academic payments or incorrect CVV.",
                    remarks = "Transaction declined by card issuer."
                )
                GatewayPaymentResult(
                    isSuccess = false,
                    orderId = order.orderId,
                    transaction = txn,
                    errorCode = "ERR_CARD_DECLINED",
                    errorMessage = "Card declined by issuer. Please check card limits or use UPI / Net Banking."
                )
            }

            GatewaySimulationMode.OTP_CHALLENGE -> {
                // Handled via OTP dialog step in the UI
                val txn = PaymentTransaction(
                    transactionId = generatedTxnId,
                    orderId = order.orderId,
                    studentId = order.studentId,
                    studentName = order.studentName,
                    billType = order.billType,
                    amount = order.amount,
                    paymentMethod = method,
                    paymentReference = paymentReference,
                    status = PaymentStatus.SUCCESS,
                    paidAtDate = todayDate,
                    gatewayProvider = provider.providerName,
                    bankReferenceNumber = mockRrn,
                    signatureHash = mockSig,
                    remarks = "Verified with 3D-Secure 2FA OTP • Reconciled"
                )
                GatewayPaymentResult(isSuccess = true, orderId = order.orderId, transaction = txn)
            }
        }
    }
}
