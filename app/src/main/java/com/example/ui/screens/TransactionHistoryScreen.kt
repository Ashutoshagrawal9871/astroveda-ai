package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PaymentLogRecord
import com.example.model.PaymentUiUtils
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Production-ready Google Play Purchase & Transaction History Screen.
 * Lists verified Google Play subscription and consumable credit orders.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    records: List<PaymentLogRecord> = emptyList(),
    onBack: () -> Unit,
    onNavigateToPlans: () -> Unit,
    onRestorePurchases: () -> Unit = {},
    onRefresh: () -> Unit = {}
) {
    val context = LocalContext.current
    BackHandler(onBack = onBack)

    var filterCategory by remember { mutableStateOf("ALL") } // ALL, SUBSCRIPTION, CONSUMABLE

    val filteredRecords = remember(records, filterCategory) {
        when (filterCategory) {
            "SUBSCRIPTION" -> records.filter { isSubscriptionRecord(it) }
            "CONSUMABLE" -> records.filterNot { isSubscriptionRecord(it) }
            else -> records
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Purchase History",
                            color = MysticGold,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Google Play verified orders & entitlements",
                            color = TextSilver,
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MysticGold
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            onRefresh()
                            Toast.makeText(context, "Refreshing purchase history...", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh History",
                            tint = MysticGold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkCosmicSurface,
                    titleContentColor = MysticGold
                )
            )
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
            // Notice card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkCosmicSurfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BorderGold.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(SacredSaffron.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = SacredSaffron,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Verified Google Play Ledger",
                                color = MysticGold,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "All digital orders are authenticated with Google Play server-side and recorded securely.",
                                color = TextSilver,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Category filter chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("ALL" to "All", "SUBSCRIPTION" to "Subscriptions", "CONSUMABLE" to "Credit Packs").forEach { (key, label) ->
                        FilterChip(
                            selected = filterCategory == key,
                            onClick = { filterCategory = key },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SacredSaffron.copy(alpha = 0.25f),
                                selectedLabelColor = MysticGold,
                                labelColor = TextSilver,
                                containerColor = DarkCosmicSurface
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = filterCategory == key,
                                borderColor = BorderSubtle,
                                selectedBorderColor = SacredSaffron
                            ),
                            modifier = Modifier.testTag("txn_filter_$key")
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Verified records list
            if (filteredRecords.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkCosmicCard),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No verified purchases yet",
                                color = MysticGold,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Orders appear here automatically after Google Play verifies your payment.",
                                color = TextMuted,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onNavigateToPlans,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SacredSaffron,
                                    contentColor = DeepCosmic
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("View Plans", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            } else {
                items(filteredRecords, key = { it.transactionId }) { record ->
                    PurchaseRecordCard(record)
                    Spacer(modifier = Modifier.height(10.dp))
                }
                item { Spacer(modifier = Modifier.height(6.dp)) }
            }

            // Sync / Restore Action
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkCosmicCard),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BorderSubtle)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Looking for a recent purchase or reinstalling?",
                            color = MysticGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Google Play automatically restores subscriptions and unconsumed credit blocks across your devices.",
                            color = TextSilver,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                onRestorePurchases()
                                Toast.makeText(context, "Querying Google Play purchases...", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SacredSaffron,
                                contentColor = DeepCosmic
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Restore & Verify Purchases", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Information details
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkCosmicSurfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Receipts & Invoice Information",
                            color = MysticGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Official purchase receipts, tax invoices (GST/VAT), and payment method updates are emailed directly by Google Play to your Google Account.\n" +
                                    "• Subscriptions can be paused, modified, or cancelled anytime in Google Play Store > Payments & Subscriptions.\n" +
                                    "• If you have any billing questions, contact support@jyotishai.com with your Google Play Order ID.",
                            color = TextMuted,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

private fun isSubscriptionRecord(record: PaymentLogRecord): Boolean =
    record.packageId.startsWith("sub_") || record.status.contains("SUBSCRIPTION", ignoreCase = true)

@Composable
private fun PurchaseRecordCard(record: PaymentLogRecord) {
    val context = LocalContext.current
    val isSubscription = isSubscriptionRecord(record)
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    val statusColor = when {
        record.status.contains("ACTIVE", ignoreCase = true) -> NebulaTeal
        record.status.contains("PENDING", ignoreCase = true) -> SacredSaffron
        record.status.contains("CANCEL", ignoreCase = true) -> CrimsonAstro
        else -> TextSilver
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("txn_record_${record.transactionId}"),
        colors = CardDefaults.cardColors(containerColor = DarkCosmicCard),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(
                            if (isSubscription) MysticGold.copy(alpha = 0.15f) else SacredSaffron.copy(alpha = 0.15f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSubscription) Icons.Default.WorkspacePremium else Icons.Default.Bolt,
                        contentDescription = null,
                        tint = if (isSubscription) MysticGold else SacredSaffron,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = record.packageName,
                        color = TextGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isSubscription) "Subscription" else "Consumable credit pack",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
                Text(
                    text = "₹${record.amountInr}",
                    color = MysticGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = BorderSubtle)
            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Order ID", color = TextMuted, fontSize = 10.sp)
                    Text(
                        text = record.transactionId.take(22) + if (record.transactionId.length > 22) "…" else "",
                        color = TextSilver,
                        fontSize = 11.sp
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Purchased on", color = TextMuted, fontSize = 10.sp)
                    Text(
                        text = dateFormat.format(Date(record.timestamp)),
                        color = TextSilver,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = record.status.replace("_", " "),
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${record.paymentMethod} • ${record.packageId}",
                    color = TextMuted,
                    fontSize = 10.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Copy Order ID",
                    color = SacredSaffron,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable {
                            PaymentUiUtils.copyToClipboard(context, "Order ID", record.transactionId)
                        }
                        .padding(4.dp)
                )
            }
        }
    }
}
