package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PolicyViewerScreen(
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "App Guidelines & Refund Policy",
                        color = MysticGold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("policy_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MysticGold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkCosmicSurface)
            )
        },
        containerColor = DeepCosmic
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkCosmicCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Gavel,
                        contentDescription = "Legal Notice",
                        tint = MysticGold,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Legal Notice: Reviewed under applicable Indian laws and Google Play guidelines.",
                        color = TextGold,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            PolicySection(
                title = "1. Plans and included usage",
                content = """
                    There is NO free AI plan. Users must subscribe or purchase pay-as-you-use credits to use AI chat consultation.
                    
                    • Elite Monthly (₹399/month): Ad-free; up to 200 AI chat answers per monthly billing period.
                    • Elite Annual (₹3,999/year): Ad-free; 200 AI chat answers refreshed monthly during the active annual subscription. Full year allowance is not granted all at once.
                    
                    Unused monthly subscription allowances reset at the end of each billing period and do not roll over. AI usage is strictly bounded to the amounts displayed; unlimited usage is never promised.
                """.trimIndent()
            )

            PolicySection(
                title = "2. Pay-as-you-use",
                content = """
                    • PAYG chat packs: 8 answers (₹29), 18 answers (₹49), 45 answers (₹99), 110 answers (₹199). Credits remain valid until used and never expire.
                    • Voice call consultation is coming soon in a future update and is not included in current plans.
                    • Video calls are not offered or supported.
                    • All prices shown in the app reflect official Google Play localized prices.
                    • Explicit confirmation is required before any purchase or session extension. No automatic charges.
                """.trimIndent()
            )

            PolicySection(
                title = "3. Ads",
                content = """
                    • Pay-as-you-use users may see a short ad at most once every 15 minutes of active app use.
                    • Ads appear strictly between interactions, NEVER interrupting an active chat answer, purchase flow, or permission prompt.
                    • Active subscribers are completely ad-free.
                    • Ads comply with Google Play policies. Private birth details, Kundli calculations, and chats are never shared with advertising partners.
                """.trimIndent()
            )

            PolicySection(
                title = "4. Subscription renewal and cancellation",
                content = """
                    • The app displays the price, billing period, included usage, and auto-renewal terms before Google Play checkout.
                    • Subscriptions renew automatically unless cancelled via Google Play Store.
                    • Users can cancel anytime in Google Play > Subscriptions. Cancellation stops future renewals; access continues through the current paid billing period.
                """.trimIndent()
            )

            PolicySection(
                title = "5. Refund policy",
                content = """
                    Refunds are handled under Google Play policies and applicable statutory laws.
                    
                    Please review plan details and included allowances before confirming checkout on Google Play. If you experience an unexpected billing error or technical issue, request assistance through Google Play Order History or customer support.
                """.trimIndent()
            )

            PolicySection(
                title = "6. Chat and consultation rules",
                content = """
                    • The app displays remaining chat question allowances in real-time.
                    • AI voice calls and AI video calls are not offered or supported.
                    • Responses are generated by AI astrologer Devi Priya for personal reflection.
                    • Consultations may pause if usage limits or safety controls are reached.
                """.trimIndent()
            )

            PolicySection(
                title = "7. AI astrologer disclosure",
                content = """
                    • The astrologer (Devi Priya) is an AI-GENERATED system, NOT a real person or human astrologer.
                    • Astrology readings are traditional cultural and philosophical interpretations, NOT guaranteed predictions or professional medical, mental-health, legal, or financial advice.
                    • AI voice calls and AI video calls are not offered or supported.
                    • No fear-based predictions, guaranteed outcomes, or high-pressure remedy sales are permitted.
                """.trimIndent()
            )

            PolicySection(
                title = "8. Privacy and permissions",
                content = """
                    • No microphone or camera permissions are requested or required.
                    • Users can toggle AI Memory off, clear chat history, and delete birth profiles at any time in Profile & Privacy settings.
                """.trimIndent()
            )

            PolicySection(
                title = "9. Kundli, profiles and support",
                content = """
                    • Approximate/unknown birth times are supported with explanatory guidance on how Lagna/House calculations are affected.
                    • Support Contact: support@jyotishai.com
                    • Corporate Entity: Jyotish AI Technologies Private Limited, New Delhi, India.
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PolicySection(title: String, content: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            color = MysticGold,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkCosmicSurface),
            shape = RoundedCornerShape(8.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle))
        ) {
            Text(
                text = content,
                color = TextSilver,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}
