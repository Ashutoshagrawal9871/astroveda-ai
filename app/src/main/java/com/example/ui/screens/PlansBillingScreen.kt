package com.example.ui.screens

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.billing.PlayProductUiInfo
import com.example.billing.PlayPurchaseState
import com.example.model.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Production-ready Google Play Plans & Billing Screen.
 * Fully compliant with Google Play Billing Policy for digital subscriptions and AI credits.
 * Displays official Play-returned prices, active entitlements from server,
 * official Play subscription management, and native purchase sheets.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlansBillingScreen(
    userBalance: UserBalance,
    playPurchaseState: PlayPurchaseState = PlayPurchaseState.Idle,
    availablePlayProducts: Map<String, PlayProductUiInfo> = emptyMap(),
    purchaseHistory: List<PaymentLogRecord> = emptyList(),
    onPurchaseProduct: (productId: String) -> Unit,
    onRestorePurchases: () -> Unit = {},
    onRefreshHistory: () -> Unit = {},
    onManageSubscriptions: (sku: String?) -> Unit = {},
    onGrantTestCredits: () -> Unit = {},
    onViewPolicy: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var showTransactionHistory by remember { mutableStateOf(false) }

    if (showTransactionHistory) {
        TransactionHistoryScreen(
            records = purchaseHistory,
            onBack = { showTransactionHistory = false },
            onNavigateToPlans = { showTransactionHistory = false },
            onRestorePurchases = onRestorePurchases,
            onRefresh = onRefreshHistory
        )
        return
    }

    Scaffold(
        topBar = {
            if (onBack != null) {
                TopAppBar(
                    title = { Text("Plans & Usage", color = MysticGold, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MysticGold)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkCosmicSurface)
                )
            }
        },
        containerColor = DeepCosmic
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Screen Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Plans & AI Subscriptions",
                            color = MysticGold,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Official Google Play Billing • Instant Activation & Cloud Sync",
                            color = TextSilver,
                            fontSize = 12.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { showTransactionHistory = true },
                            modifier = Modifier.testTag("open_history_from_plans_button")
                        ) {
                            Icon(imageVector = Icons.Default.Receipt, contentDescription = "History", tint = MysticGold)
                        }

                        IconButton(onClick = onViewPolicy) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = "Policy", tint = MysticGold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Play Purchase State Indicator
                when (val state = playPurchaseState) {
                    is PlayPurchaseState.Processing -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = DarkCosmicSurfaceVariant),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MysticGold)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MysticGold, strokeWidth = 2.5.dp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = state.message, color = MysticGold, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    is PlayPurchaseState.Pending -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2000)),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFFFB300))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.HourglassTop, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = state.message, color = Color(0xFFFFD54F), fontSize = 12.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    is PlayPurchaseState.Success -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF06331E)),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF10B981))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Purchase Activated!", color = Color(0xFF10B981), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(state.message, color = Color(0xFFA7F3D0), fontSize = 11.sp)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    is PlayPurchaseState.Failed -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF3B0B0B)),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFEF4444))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("Google Play Notice", color = Color(0xFFFCA5A5), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = state.errorMessage,
                                    color = Color(0xFFFCA5A5),
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = onGrantTestCredits,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SacredSaffron,
                                        contentColor = DeepCosmic
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Activate 10 Free Test Credits (Sideload / QA Mode)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    else -> {}
                }

                // Active Entitlement Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkCosmicCard),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, BorderGold)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Current Active Plan", color = TextSilver, fontSize = 11.sp)
                                Text(userBalance.currentPlan.displayName, color = MysticGold, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                            }

                            Surface(
                                color = if (userBalance.isSubscriptionActive) SacredSaffron.copy(alpha = 0.2f) else DarkCosmicSurfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, if (userBalance.isSubscriptionActive) SacredSaffron else BorderSubtle)
                            ) {
                                Text(
                                    text = if (userBalance.isSubscriptionActive) "ACTIVE SUBSCRIBER" else "PAY-AS-YOU-USE",
                                    color = if (userBalance.isSubscriptionActive) SacredSaffron else TextSilver,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        if (userBalance.isSubscriptionActive) {
                            Spacer(modifier = Modifier.height(6.dp))
                            val expiryDateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(userBalance.planExpiryMillis))
                            Text("Active through $expiryDateStr via Google Play Store", color = TextMuted, fontSize = 11.sp)
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = BorderSubtle)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            UsageMetric("AI Chat Answers", "${userBalance.chatMessagesRemaining}")
                            UsageMetric("Experience", if (userBalance.isAdFree) "100% Ad-Free" else "Standard")
                        }

                        if (userBalance.isSubscriptionActive) {
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedButton(
                                onClick = { onManageSubscriptions(null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("manage_play_subscription_button"),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MysticGold),
                                border = BorderStroke(1.dp, BorderGold),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Manage Subscription on Google Play", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Section Header: Subscriptions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Monthly & Annual Subscriptions", color = MysticGold, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                        Text("Full astrology allowance • 100% Ad-Free", color = TextSilver, fontSize = 11.sp)
                    }

                    TextButton(onClick = onRestorePurchases) {
                        Icon(imageVector = Icons.Default.Restore, contentDescription = null, tint = MysticGold, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Restore", color = MysticGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Subscription Cards (Monthly & Yearly)
            items(listOf(BillingPlansCatalog.eliteMonthly, BillingPlansCatalog.eliteAnnual)) { pkg ->
                val playInfo = availablePlayProducts[pkg.id]
                val playPrice = playInfo?.formattedPrice
                val displayPrice = if (!playPrice.isNullOrBlank() && !playPrice.contains("Checking", ignoreCase = true)) {
                    playPrice
                } else {
                    "${pkg.priceDisplay} ${pkg.billingPeriod}"
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .testTag("sub_card_${pkg.id}"),
                    colors = CardDefaults.cardColors(containerColor = DarkCosmicCard),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, if (pkg.highlighted) Brush.horizontalGradient(listOf(SacredSaffron, BorderGold)) else BorderStroke(1.dp, BorderGold).brush)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = pkg.title,
                                        color = MysticGold,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    if (pkg.badge.isNotBlank()) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            color = SacredSaffron.copy(alpha = 0.25f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = pkg.badge,
                                                color = SacredSaffron,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = pkg.description,
                                    color = TextSilver,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Features List
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            FeaturePoint("200 AI Chat Answers every month")
                            FeaturePoint("100% Ad-Free Experience")
                            FeaturePoint("Full Kundli, Dasha & Remedies Analysis")
                            FeaturePoint("Cancel anytime in Google Play Store")
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = BorderSubtle)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = displayPrice,
                                    color = SacredSaffron,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = if (pkg.id == "sub_elite_annual") "Billed annually via Google Play" else "Billed monthly via Google Play",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }

                            Button(
                                onClick = { onPurchaseProduct(pkg.id) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SacredSaffron,
                                    contentColor = DeepCosmic
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("subscribe_button_${pkg.id}")
                            ) {
                                Text(
                                    text = "Subscribe $displayPrice",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = pkg.fullTerms,
                            color = TextMuted,
                            fontSize = 9.sp,
                            lineHeight = 13.sp
                        )
                    }
                }
            }

            // Section: Pay-As-You-Use Micro-Packs
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Pay-As-You-Use Credit Packs",
                    color = MysticGold,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Consumable one-time chat packs • Never expires",
                    color = TextSilver,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Consumable Credit Cards
            items(BillingPlansCatalog.payAsYouUsePackages) { pkg ->
                val playInfo = availablePlayProducts[pkg.id]
                val playPrice = playInfo?.formattedPrice
                val displayPrice = if (!playPrice.isNullOrBlank() && !playPrice.contains("Checking", ignoreCase = true)) {
                    playPrice
                } else {
                    pkg.priceDisplay
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("payg_card_${pkg.id}"),
                    colors = CardDefaults.cardColors(containerColor = DarkCosmicCard),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (pkg.highlighted) SacredSaffron else BorderSubtle)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(pkg.title, color = MysticGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                if (pkg.badge.isNotBlank()) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(color = SacredSaffron.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                                        Text(pkg.badge, color = SacredSaffron, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                    }
                                }
                            }
                            Text(pkg.description, color = TextSilver, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Button(
                            onClick = { onPurchaseProduct(pkg.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = MysticGold, contentColor = DeepCosmic),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("buy_payg_${pkg.id}")
                        ) {
                            Text("Buy $displayPrice", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Sideloaded / Testing Card
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkCosmicSurfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BorderGold.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🛠️ Sideload & Testing Controls",
                            color = MysticGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "If testing outside Google Play Store where Play Billing is unavailable, tap below to add 10 complimentary AI questions.",
                            color = TextSilver,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onGrantTestCredits,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MysticGold),
                            border = BorderStroke(1.dp, MysticGold),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("+10 Free Test Credits", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeaturePoint(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = NebulaTeal, modifier = Modifier.size(13.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, color = TextSilver, fontSize = 11.sp)
    }
}

@Composable
private fun UsageMetric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = TextSilver, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, color = MysticGold, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0D0B1A)
@Composable
fun PlansBillingScreenPreview() {
    MyApplicationTheme {
        PlansBillingScreen(
            userBalance = UserBalance(
                currentPlan = PlanType.PAY_AS_YOU_USE,
                chatMessagesRemaining = 8,
                isAdFree = false
            ),
            onPurchaseProduct = {},
            onViewPolicy = {}
        )
    }
}
