package com.example

import com.example.model.PlanType
import com.example.model.UserBalance
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests covering backend entitlement checks, session limits,
 * spending caps, ad frequencies, and subscription rules.
 */
class AiPlanEntitlementUnitTest {

    @Test
    fun testNoFreeAiPlanRule() {
        // Strict requirement: There is no free AI plan.
        val defaultFreeBalance = UserBalance(
            currentPlan = PlanType.PAY_AS_YOU_USE,
            planExpiryMillis = 0L,
            chatMessagesRemaining = 0,
            voiceMinutesRemaining = 0
        )
        assertFalse("Unpaid user must NOT be allowed to send chat without credits", defaultFreeBalance.canSendChat)
    }

    @Test
    fun testEliteMonthlySubscriptionEntitlement() {
        // Elite Monthly: ₹399/month, 200 chat answers, ad-free
        val activeMonthly = UserBalance(
            currentPlan = PlanType.ELITE_MONTHLY,
            planExpiryMillis = System.currentTimeMillis() + (30L * 24L * 60L * 60L * 1000L),
            chatMessagesRemaining = 200,
            voiceMinutesRemaining = 0,
            isAdFree = true
        )
        assertTrue("Subscribed user should be active", activeMonthly.isSubscriptionActive)
        assertTrue("Subscribed user can send chat", activeMonthly.canSendChat)
        assertFalse("Subscriber is always ad-free", activeMonthly.shouldShowInterstitialAd())
    }

    @Test
    fun testPayAsYouUseCredits() {
        // Pay-as-you-use user with 15 chat answers purchased
        val paygBalance = UserBalance(
            currentPlan = PlanType.PAY_AS_YOU_USE,
            planExpiryMillis = 0L,
            chatMessagesRemaining = 15,
            voiceMinutesRemaining = 0,
            isAdFree = false
        )
        assertFalse("Plan is not subscription", paygBalance.isSubscriptionActive)
        assertTrue("Can send chat with purchased credits", paygBalance.canSendChat)
    }

    @Test
    fun testPaygAdFrequencyPolicy() {
        // PAYG user: ads allowed at most once per 15 minutes of active app use
        val recentAdBalance = UserBalance(
            currentPlan = PlanType.PAY_AS_YOU_USE,
            isAdFree = false,
            lastAdTimestamp = System.currentTimeMillis() - (5 * 60 * 1000L) // 5 minutes ago
        )
        assertFalse("Must not show ad within 15 minutes", recentAdBalance.shouldShowInterstitialAd())

        val eligibleAdBalance = UserBalance(
            currentPlan = PlanType.PAY_AS_YOU_USE,
            isAdFree = false,
            lastAdTimestamp = System.currentTimeMillis() - (16 * 60 * 1000L) // 16 minutes ago
        )
        assertTrue("Eligible to show ad after 15 minutes", eligibleAdBalance.shouldShowInterstitialAd())
    }

    @Test
    fun testOperatorSpendingCapEnforcement() {
        // When monthly spending cap is reached, paid services are flagged
        val spendCapExceededBalance = UserBalance(
            currentPlan = PlanType.PAY_AS_YOU_USE,
            monthlySpendingInr = 250000,
            maxMonthlySpendCapInr = 250000
        )
        assertTrue("Monthly spend cap reached", spendCapExceededBalance.isSpendCapReached)
    }
}
