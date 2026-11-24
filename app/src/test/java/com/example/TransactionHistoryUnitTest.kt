package com.example

import com.example.model.BillingPlansCatalog
import com.example.model.PackageCategory
import com.example.model.PaymentLogRecord
import org.junit.Assert.*
import org.junit.Test

class TransactionHistoryUnitTest {

    @Test
    fun testTransactionRecordFiltering() {
        val records = listOf(
            PaymentLogRecord(
                transactionId = "GPA.3312-9841-1029-48191",
                packageId = "sub_elite_monthly",
                packageName = "Elite Monthly",
                amountInr = 399,
                paymentMethod = "Google Play",
                status = "VERIFIED_ACTIVE",
                timestamp = 1000L
            ),
            PaymentLogRecord(
                transactionId = "GPA.3312-9841-1029-48192",
                packageId = "payg_chat_15",
                packageName = "15 Chat Answers",
                amountInr = 49,
                paymentMethod = "Google Play",
                status = "VERIFIED_ACTIVE",
                timestamp = 2000L
            )
        )

        val active = records.filter { it.status == "VERIFIED_ACTIVE" }
        assertEquals(2, active.size)
        assertEquals("GPA.3312-9841-1029-48191", active[0].transactionId)
    }

    @Test
    fun testGooglePlaySubscriptionsCatalog() {
        val monthly = BillingPlansCatalog.eliteMonthly
        assertEquals(399, monthly.priceInr)
        assertEquals(200, monthly.includedChatMessages)
        assertEquals(0, monthly.includedVoiceMinutes)
        assertTrue(monthly.isAdFree)
        assertEquals(PackageCategory.SUBSCRIPTION, monthly.category)

        val annual = BillingPlansCatalog.eliteAnnual
        assertEquals(3999, annual.priceInr)
        assertEquals(200, annual.includedChatMessages)
        assertEquals(0, annual.includedVoiceMinutes)
        assertTrue(annual.isAdFree)
        assertEquals(PackageCategory.SUBSCRIPTION, annual.category)
    }

    @Test
    fun testGooglePlayPayAsYouUsePackages() {
        val quick = BillingPlansCatalog.paygChatQuick
        assertEquals(29, quick.priceInr)
        assertEquals(8, quick.includedChatMessages)
        assertEquals(PackageCategory.CHAT_CREDITS, quick.category)

        val chat15 = BillingPlansCatalog.paygChat15
        assertEquals(49, chat15.priceInr)
        assertEquals(18, chat15.includedChatMessages)
        assertEquals(PackageCategory.CHAT_CREDITS, chat15.category)

        val chat35 = BillingPlansCatalog.paygChat35
        assertEquals(99, chat35.priceInr)
        assertEquals(45, chat35.includedChatMessages)

        val chat100 = BillingPlansCatalog.paygChat100
        assertEquals(199, chat100.priceInr)
        assertEquals(110, chat100.includedChatMessages)
        assertEquals(4, BillingPlansCatalog.payAsYouUsePackages.size)
    }
}
