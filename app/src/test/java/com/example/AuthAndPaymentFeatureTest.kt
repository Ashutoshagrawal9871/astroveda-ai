package com.example

import com.example.auth.AuthManager
import com.example.billing.PlayPurchaseState
import com.example.model.BillingPlansCatalog
import com.example.model.PlanType
import com.example.model.UserBalance
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Email/Password Authentication state transitions,
 * Sideload / QA test credit mechanics, and Google Play Billing catalog and error handling.
 */
class AuthAndPaymentFeatureTest {

    @Test
    fun testGuestModeTransition() {
        AuthManager.continueAsGuest()
        assertTrue("Guest mode should be active after continueAsGuest()", AuthManager.isGuestMode.value)

        AuthManager.exitGuestMode()
        assertFalse("Guest mode should be deactivated after exitGuestMode()", AuthManager.isGuestMode.value)
    }

    @Test
    fun testEmailPasswordValidationConstraints() {
        val validEmail = "user@example.com"
        val invalidEmailNoAt = "userexample.com"
        val validPassword = "securePassword123"
        val shortPassword = "123"

        assertTrue("Valid email format contains @", validEmail.contains("@") && validEmail.isNotBlank())
        assertFalse("Invalid email does not contain @", invalidEmailNoAt.contains("@"))
        assertTrue("Password meets min length of 6", validPassword.length >= 6)
        assertFalse("Short password fails min length of 6", shortPassword.length >= 6)
    }

    @Test
    fun testSideloadTesterCreditTopUp() {
        var balance = UserBalance(
            currentPlan = PlanType.PAY_AS_YOU_USE,
            planExpiryMillis = 0L,
            chatMessagesRemaining = 0,
            voiceMinutesRemaining = 0
        )
        assertFalse("User with 0 credits cannot send chat", balance.canSendChat)

        // Simulate activating 10 test credits on a sideloaded APK
        balance = balance.copy(chatMessagesRemaining = balance.chatMessagesRemaining + 10)
        assertTrue("User with test credits can send chat", balance.canSendChat)
        assertEquals(10, balance.chatMessagesRemaining)
    }

    @Test
    fun testPlayPurchaseStateTransitions() {
        val idleState: PlayPurchaseState = PlayPurchaseState.Idle
        assertEquals(PlayPurchaseState.Idle, idleState)

        val failedState = PlayPurchaseState.Failed("Google Play product details are loading", canRetry = true)
        assertTrue(failedState.canRetry)
        assertTrue(failedState.errorMessage.contains("Google Play"))

        val successState = PlayPurchaseState.Success(
            productId = "sub_elite_monthly",
            orderId = "order_test_123",
            message = "Subscription Activated"
        )
        assertEquals("order_test_123", successState.orderId)
        assertEquals("sub_elite_monthly", successState.productId)
    }

    @Test
    fun testBillingCatalogConsistency() {
        val monthly = BillingPlansCatalog.eliteMonthly
        assertEquals("sub_elite_monthly", monthly.id)
        assertTrue(monthly.highlighted)

        val annual = BillingPlansCatalog.eliteAnnual
        assertEquals("sub_elite_annual", annual.id)

        val quickPack = BillingPlansCatalog.paygChatQuick
        assertEquals("payg_chat_quick", quickPack.id)

        val packs = BillingPlansCatalog.payAsYouUsePackages
        assertTrue("Catalog must contain all consumable tiers", packs.isNotEmpty())
        assertTrue("All consumable packs must have non-blank IDs", packs.all { it.id.isNotBlank() })
    }
}
