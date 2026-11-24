package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun UnknownTimeNoticeCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCosmicSurfaceVariant),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SaffronOrange.copy(alpha = 0.6f)))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Unknown Time Notice",
                tint = SaffronOrange,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Approximate / Unknown Birth Time",
                    color = MysticGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Lagna (Ascendant) rotates every 2 hours, defining the 12 houses. Without exact time, planetary house placements may vary. However, Moon sign (Rashi), Nakshatra, and major planetary dashas remain highly accurate for guidance.",
                    color = TextSilver,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
fun PlanetPlacementRow(planet: PlanetPosition) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCosmicCard),
        shape = RoundedCornerShape(10.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(DarkCosmicSurface, RoundedCornerShape(8.dp))
                        .border(1.dp, BorderGold, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = planet.symbol,
                        color = MysticGold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${planet.englishName} (${planet.hindiName})",
                            color = TextGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (planet.isRetrograde) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = CrimsonAstro.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "Vakri (R)",
                                    color = CrimsonAstro,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "Sign: ${planet.signName} • House ${planet.house} • ${String.format("%.1f", planet.degree)}°",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Surface(
                color = when (planet.dignity) {
                    "Exalted", "Exalted/Strong" -> MysticGold.copy(alpha = 0.2f)
                    "Debilitated" -> CrimsonAstro.copy(alpha = 0.2f)
                    "Own Sign" -> NebulaTeal.copy(alpha = 0.2f)
                    else -> DarkCosmicSurface
                },
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = planet.dignity,
                    color = when (planet.dignity) {
                        "Exalted", "Exalted/Strong" -> MysticGold
                        "Debilitated" -> CrimsonAstro
                        "Own Sign" -> NebulaTeal
                        else -> TextSilver
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
fun DashaTimelineView(dashas: List<DashaPeriod>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Vimshottari Dasha Periods (120-Year Cycle)",
            color = MysticGold,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        dashas.forEach { dasha ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (dasha.isCurrent) DarkCosmicSurfaceVariant else DarkCosmicCard
                ),
                shape = RoundedCornerShape(8.dp),
                border = if (dasha.isCurrent) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MysticGold)) else null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (dasha.isCurrent) {
                            Icon(
                                imageVector = Icons.Default.Stars,
                                contentDescription = "Current Dasha",
                                tint = MysticGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Column {
                            Text(
                                text = "${dasha.planet} (${dasha.planetHindi})",
                                color = if (dasha.isCurrent) MysticGold else TextGold,
                                fontSize = 13.sp,
                                fontWeight = if (dasha.isCurrent) FontWeight.Bold else FontWeight.Medium
                            )
                            if (dasha.isCurrent) {
                                Text(
                                    text = "Current Antardasha: ${dasha.currentAntardasha}",
                                    color = SaffronOrange,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${dasha.startYear} - ${dasha.endYear}",
                            color = TextSilver,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${dasha.durationYears} Years",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}
