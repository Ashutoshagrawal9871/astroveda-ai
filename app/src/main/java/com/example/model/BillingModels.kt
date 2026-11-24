package com.example.model

enum class PlanType(val displayName: String, val priceStr: String) {
    PAY_AS_YOU_USE("Pay-As-You-Use", "Usage based"),
    ELITE_MONTHLY("Elite Monthly", "₹399/month"),
    ELITE_ANNUAL("Elite Annual", "₹3,999/year")
}

data class UserBalance(
    val currentPlan: PlanType = PlanType.PAY_AS_YOU_USE,
    val planExpiryMillis: Long = 0L,
    val monthlyAllowanceSessionsRemaining: Int = 0,
    val prepaidVideoSessionsRemaining: Int = 0, // Retained solely for historical migration/audit
    val chatMessagesRemaining: Int = 0,
    val voiceMinutesRemaining: Int = 0,
    val videoMinutesRemaining: Int = 0,        // Retained solely for historical audit
    val isAdFree: Boolean = false,
    val lastAdTimestamp: Long = 0L,
    val monthlySpendingInr: Int = 0,
    val maxMonthlySpendCapInr: Int = 250000
) {
    val isSubscriptionActive: Boolean
        get() = currentPlan != PlanType.PAY_AS_YOU_USE && System.currentTimeMillis() < planExpiryMillis

    val isSpendCapReached: Boolean
        get() = monthlySpendingInr >= maxMonthlySpendCapInr

    val canSendChat: Boolean
        get() = isSubscriptionActive || chatMessagesRemaining > 0

    val canMakeVoiceCall: Boolean
        get() = isSubscriptionActive || voiceMinutesRemaining > 0

    /**
     * Ad policy: Non-subscribers see a brief ad banner once every 5 minutes.
     * Active subscribers are 100% ad-free.
     */
    fun shouldShowInterstitialAd(): Boolean {
        if (isAdFree || isSubscriptionActive) return false
        val fiveMinutesMs = 5 * 60 * 1000L
        return (System.currentTimeMillis() - lastAdTimestamp) > fiveMinutesMs
    }
}

enum class PackageCategory {
    SUBSCRIPTION,
    CHAT_CREDITS,
    VOICE_BLOCK
}

data class PurchasePackage(
    val id: String,
    val title: String,
    val category: PackageCategory,
    val priceInr: Int,
    val priceDisplay: String,
    val billingPeriod: String, // "/month", "/year", "one-time"
    val includedChatMessages: Int = 0,
    val includedVoiceMinutes: Int = 0,
    val isAdFree: Boolean = false,
    val highlighted: Boolean = false,
    val isAvailable: Boolean = true,
    val badge: String = "",
    val description: String,
    val fullTerms: String
)

object BillingPlansCatalog {
    val eliteMonthly = PurchasePackage(
        id = "sub_elite_monthly",
        title = "Elite Monthly",
        category = PackageCategory.SUBSCRIPTION,
        priceInr = 399,
        priceDisplay = "₹399",
        billingPeriod = "/month",
        includedChatMessages = 200,
        includedVoiceMinutes = 0,
        isAdFree = true,
        highlighted = true,
        isAvailable = true,
        badge = "AUTO-RENEWING • CANCEL ANYTIME",
        description = "Automatic monthly subscription. Includes up to 200 AI chat answers every month. Ad-free. Cancel anytime in Google Play Store.",
        fullTerms = "Renews automatically every month via Google Play at ₹399. Includes 200 chat answers per month. Cancel anytime in Google Play > Subscriptions."
    )

    val eliteAnnual = PurchasePackage(
        id = "sub_elite_annual",
        title = "Elite Annual",
        category = PackageCategory.SUBSCRIPTION,
        priceInr = 3999,
        priceDisplay = "₹3,999",
        billingPeriod = "/year",
        includedChatMessages = 200,
        includedVoiceMinutes = 0,
        isAdFree = true,
        highlighted = false,
        isAvailable = true,
        badge = "AUTO-RENEWING • CANCEL ANYTIME",
        description = "Automatic annual subscription. 200 AI chat answers refreshed monthly. Completely ad-free. Cancel anytime in Google Play Store.",
        fullTerms = "Billed automatically every year via Google Play at ₹3,999. Monthly allowance of 200 chat answers refreshes every 30 days. Cancel anytime in Google Play Store > Subscriptions."
    )

    val paygChatQuick = PurchasePackage(
        id = "payg_chat_quick",
        title = "8 Quick Answers",
        category = PackageCategory.CHAT_CREDITS,
        priceInr = 29,
        priceDisplay = "₹29",
        billingPeriod = "one-time",
        includedChatMessages = 8,
        isAvailable = true,
        badge = "⚡ ₹29 IMPULSE",
        description = "Ask 8 quick Vedic astrology questions. Ideal for instant doubts & daily guidance (~₹3.60/answer).",
        fullTerms = "One-time consumable Google Play purchase. Credits valid until consumed. Never expires."
    )

    val paygChat15 = PurchasePackage(
        id = "payg_chat_15",
        title = "18 Chat Answers",
        category = PackageCategory.CHAT_CREDITS,
        priceInr = 49,
        priceDisplay = "₹49",
        billingPeriod = "one-time",
        includedChatMessages = 18,
        isAvailable = true,
        badge = "MOST POPULAR",
        description = "Ask 18 in-depth Kundli-aware Vedic AI questions (~₹2.72/answer).",
        fullTerms = "One-time consumable Google Play purchase. Credits valid until consumed. Never expires."
    )

    val paygChat35 = PurchasePackage(
        id = "payg_chat_35",
        title = "45 Chat Answers",
        category = PackageCategory.CHAT_CREDITS,
        priceInr = 99,
        priceDisplay = "₹99",
        billingPeriod = "one-time",
        includedChatMessages = 45,
        isAvailable = true,
        badge = "BEST VALUE",
        description = "Ask 45 detailed Kundli & Mahadasha guidance questions (~₹2.20/answer).",
        fullTerms = "One-time consumable Google Play purchase. Credits valid until consumed. Never expires."
    )

    val paygChat100 = PurchasePackage(
        id = "payg_chat_100",
        title = "110 Chat Answers",
        category = PackageCategory.CHAT_CREDITS,
        priceInr = 199,
        priceDisplay = "₹199",
        billingPeriod = "one-time",
        includedChatMessages = 110,
        isAvailable = true,
        badge = "PRO PACK",
        description = "110 comprehensive questions for Kundli Milan, career pivots & remedies (~₹1.80/answer).",
        fullTerms = "One-time consumable Google Play purchase. Credits valid until consumed. Never expires."
    )

    val payAsYouUsePackages = listOf(
        paygChatQuick,
        paygChat15,
        paygChat35,
        paygChat100
    )

    val payAsYouUseSingleQuery get() = paygChatQuick

    val allPackages = listOf(
        eliteMonthly,
        eliteAnnual,
        paygChatQuick,
        paygChat15,
        paygChat35,
        paygChat100
    )
}
