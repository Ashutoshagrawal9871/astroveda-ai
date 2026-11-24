package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Short Ad Banner Component.
 * Automatically displays a brief cosmic sponsor ad every 5 minutes for free/PAYG users.
 * Active subscribers are 100% ad-free.
 */
@Composable
fun AdNoticeBanner(
    isAdFree: Boolean,
    onUpgradeClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isAdFree) return

    var adIndex by remember { mutableIntStateOf(0) }
    var isVisible by remember { mutableStateOf(true) }

    val sponsorAds = remember {
        listOf(
            "Vedic Certified Gemstones & Rudraksha" to "Pure Panchadhatu rings. Go Elite to remove all ads.",
            "Certified Energized Shree Yantra" to "Sacred geometry for home prosperity. Upgrade to go ad-free.",
            "Authentic Natural Blue Sapphire (Neelam)" to "Saturn protection & mental clarity. Go Elite for ad-free app.",
            "Pure 5-Mukhi Rudraksha Mala" to "Chanted with Mahamrityunjaya Mantra. Go Elite for 100% ad-free."
        )
    }

    // 5-Minute Timer Loop
    LaunchedEffect(isVisible) {
        if (!isVisible) {
            // Wait 5 minutes (300,000 ms) before showing the next ad
            delay(5 * 60 * 1000L)
            adIndex = (adIndex + 1) % sponsorAds.size
            isVisible = true
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        val currentAd = sponsorAds[adIndex]

        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp)
                .testTag("ad_notice_card"),
            colors = CardDefaults.cardColors(containerColor = DarkCosmicCard),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(BorderGold.copy(alpha = 0.5f), SacredSaffron.copy(alpha = 0.5f))))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = SacredSaffron.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "AD • 5 MIN",
                            color = SacredSaffron,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = currentAd.first,
                            color = TextGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentAd.second,
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        onClick = onUpgradeClicked,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = "Upgrade",
                            tint = MysticGold,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Go Ad-Free",
                            color = MysticGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = { isVisible = false },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss ad for 5 minutes",
                            tint = TextMuted,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}
