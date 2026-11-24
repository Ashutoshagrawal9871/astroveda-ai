package com.example.ui.components

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.billing.PlayProductUiInfo
import com.example.billing.PlayPurchaseState
import com.example.model.BillingPlansCatalog
import com.example.model.PackageCategory
import com.example.model.PurchasePackage
import com.example.ui.theme.*

/**
 * Production-ready Google Play Pay-As-You-Use Bottom Sheet.
 * Direct integration with Google Play Billing Library.
 * Displays official Play-returned prices, product inclusions, and handles
 * real-time purchase sheet launch and server-verified entitlement states.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayAsYouUseBottomSheet(
    initialPackage: PurchasePackage? = null,
    playPurchaseState: PlayPurchaseState = PlayPurchaseState.Idle,
    availablePlayProducts: Map<String, PlayProductUiInfo> = emptyMap(),
    onDismiss: () -> Unit,
    onPurchasePackage: (productId: String) -> Unit,
    onRestorePurchases: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? Activity

    var selectedPackage by remember {
        mutableStateOf(initialPackage ?: BillingPlansCatalog.paygChatQuick)
    }

    val paygPackages = remember { BillingPlansCatalog.payAsYouUsePackages }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkCosmicSurface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = MysticGold.copy(alpha = 0.6f)) },
        modifier = Modifier.testTag("payg_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(SacredSaffron.copy(alpha = 0.2f), CircleShape)
                            .border(1.dp, SacredSaffron, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = SacredSaffron,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Pay-As-You-Use Credits",
                            color = MysticGold,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "One-time digital packs • No recurring commitment",
                            color = TextSilver,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSilver)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Purchase State Feedback Banner
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
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = MysticGold,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = state.message,
                                color = MysticGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
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
                            Icon(
                                imageVector = Icons.Default.HourglassTop,
                                contentDescription = null,
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = state.message,
                                color = Color(0xFFFFD54F),
                                fontSize = 12.sp
                            )
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
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Purchase Successful!",
                                    color = Color(0xFF10B981),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = state.message,
                                    color = Color(0xFFA7F3D0),
                                    fontSize = 11.sp
                                )
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
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = state.errorMessage,
                                color = Color(0xFFFCA5A5),
                                fontSize = 12.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                else -> {}
            }

            // Package Selector List
            Text(
                text = "Select Digital Package",
                color = TextSilver,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            paygPackages.forEach { pkg ->
                val isSelected = selectedPackage.id == pkg.id
                val playInfo = availablePlayProducts[pkg.id]
                val displayPrice = playInfo?.formattedPrice ?: pkg.priceDisplay

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable(enabled = pkg.isAvailable) {
                            selectedPackage = pkg
                        }
                        .testTag("payg_pkg_${pkg.id}"),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            !pkg.isAvailable -> DarkCosmicSurfaceVariant.copy(alpha = 0.5f)
                            isSelected -> SacredSaffron.copy(alpha = 0.16f)
                            else -> DarkCosmicCard
                        }
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) SacredSaffron else BorderSubtle
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { if (pkg.isAvailable) selectedPackage = pkg },
                                enabled = pkg.isAvailable,
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = SacredSaffron,
                                    unselectedColor = TextMuted
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = pkg.title,
                                        color = if (pkg.isAvailable) MysticGold else TextMuted,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (pkg.badge.isNotBlank()) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = when {
                                                pkg.badge.contains("IMPULSE") -> NebulaTeal.copy(alpha = 0.25f)
                                                pkg.badge.contains("POPULAR") -> SacredSaffron.copy(alpha = 0.25f)
                                                pkg.badge.contains("VALUE") -> MysticGold.copy(alpha = 0.25f)
                                                pkg.badge.contains("PRO") -> CelestialPurple.copy(alpha = 0.3f)
                                                else -> DarkCosmicSurfaceVariant
                                            },
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = pkg.badge,
                                                color = when {
                                                    pkg.badge.contains("IMPULSE") -> NebulaTeal
                                                    pkg.badge.contains("POPULAR") -> SacredSaffron
                                                    pkg.badge.contains("VALUE") -> MysticGold
                                                    pkg.badge.contains("PRO") -> MysticGold
                                                    else -> TextSilver
                                                },
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = pkg.description,
                                    color = TextSilver,
                                    fontSize = 11.sp,
                                    maxLines = 2
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = displayPrice,
                                color = if (pkg.isAvailable) SacredSaffron else TextMuted,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "one-time",
                                color = TextMuted,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val priceText = availablePlayProducts[selectedPackage.id]?.formattedPrice ?: selectedPackage.priceDisplay

            // Single, compliant purchase path: Google Play Billing.
            Button(
                onClick = {
                    onPurchasePackage(selectedPackage.id)
                },
                enabled = selectedPackage.isAvailable && playPurchaseState !is PlayPurchaseState.Processing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("payg_buy_play_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SacredSaffron,
                    contentColor = DeepCosmic,
                    disabledContainerColor = DarkCosmicSurfaceVariant,
                    disabledContentColor = TextMuted
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (selectedPackage.isAvailable) "Buy with Google Play • $priceText" else "Pending Availability",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Policy & Restore Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onRestorePurchases,
                    modifier = Modifier.testTag("payg_restore_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = null,
                        tint = TextSilver,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Restore Purchases",
                        color = TextSilver,
                        fontSize = 11.sp
                    )
                }

                Text(
                    text = "Billed via Google Play • Terms apply",
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
        }
    }
}
