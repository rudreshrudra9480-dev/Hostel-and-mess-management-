package com.example

import com.example.data.HostelRepository
import com.example.model.*
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testMonthlyBillingHistoryCalculation() {
        val repository = HostelRepository()
        val history = repository.getMonthlyBillingHistory("HST-2026-101")
        
        assertNotNull(history)
        assertEquals(6, history.size)
        
        // Check latest month (September 2026)
        val septRecord = history.first()
        assertEquals("2026-09", septRecord.monthKey)
        assertTrue(septRecord.totalBilled > 0)
        
        // Initially student 101 has dues pending for September
        assertEquals(MonthlyBillingStatus.PENDING, septRecord.status)
        assertTrue(septRecord.totalPending > 0)
        
        // Record a payment for Mess Bill
        repository.processPayment(
            studentId = "HST-2026-101",
            studentName = "Aarav Mehta",
            billType = BillType.MESS_BILL,
            amountPaid = septRecord.messBilled,
            paymentMethod = PaymentMethod.UPI,
            paymentReference = "UPI: aarav@okhdfc"
        )
        
        val updatedHistory = repository.getMonthlyBillingHistory("HST-2026-101")
        val updatedSept = updatedHistory.first()
        assertEquals(0.0, updatedSept.messPending, 0.01)
        assertEquals(septRecord.messBilled, updatedSept.messPaid, 0.01)
        // Now partially paid since room fee is still pending
        assertEquals(MonthlyBillingStatus.PARTIALLY_PAID, updatedSept.status)
    }
}
