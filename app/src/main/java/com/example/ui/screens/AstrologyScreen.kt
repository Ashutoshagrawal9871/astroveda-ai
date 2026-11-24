package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astro.VedicEngine
import com.example.data.AstroRepository
import com.example.model.CompatibilityResult
import com.example.model.PanchangData
import com.example.ui.theme.*

@Composable
fun AstrologyScreen(
    repository: AstroRepository? = null,
    language: String = "en"
) {
    var selectedCategory by remember { mutableIntStateOf(0) }
    val panchang = remember { VedicEngine.getTodayPanchang() }

    Scaffold(
        containerColor = DeepCosmic
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(DarkCosmicSurfaceVariant, RoundedCornerShape(10.dp))
                            .border(1.dp, BorderGold, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Brightness5,
                            contentDescription = "Astrology",
                            tint = MysticGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Vedic Jyotish & Horoscope",
                            color = MysticGold,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Personalized daily readings, panchang & auspicious timings",
                            color = TextSilver,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedCategory,
                    containerColor = DarkCosmicSurface,
                    contentColor = MysticGold,
                    edgePadding = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (repository != null) {
                        Tab(
                            selected = selectedCategory == 0,
                            onClick = { selectedCategory = 0 },
                            text = { Text("Daily Horoscope") }
                        )
                    }
                    Tab(
                        selected = selectedCategory == (if (repository != null) 1 else 0),
                        onClick = { selectedCategory = if (repository != null) 1 else 0 },
                        text = { Text("Panchang") }
                    )
                    Tab(
                        selected = selectedCategory == (if (repository != null) 2 else 1),
                        onClick = { selectedCategory = if (repository != null) 2 else 1 },
                        text = { Text("Kundli Milan") }
                    )
                    Tab(
                        selected = selectedCategory == (if (repository != null) 3 else 2),
                        onClick = { selectedCategory = if (repository != null) 3 else 2 },
                        text = { Text("Muhurats") }
                    )
                    Tab(
                        selected = selectedCategory == (if (repository != null) 4 else 3),
                        onClick = { selectedCategory = if (repository != null) 4 else 3 },
                        text = { Text("Festivals") }
                    )
                }
            }

            // Tab Content
            if (repository != null && selectedCategory == 0) {
                DailyHoroscopeDisplay(
                    repository = repository,
                    language = language
                )
            } else {
                val offset = if (repository != null) 1 else 0
                val activeTab = selectedCategory - offset

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    when (activeTab) {
                        0 -> {
                            // Panchang Tab
                            item {
                                PanchangView(panchang)
                            }
                        }
                        1 -> {
                            // Full Kundli Milan Tab with 2 individuals birth details input
                            item {
                                KundliMatchingScreen(
                                    onConsultAstrologer = null,
                                    isEmbedded = true
                                )
                            }
                        }
                        2 -> {
                            // Muhurat Tab
                            item {
                                Text(
                                    text = "Shubh Muhurat (Auspicious Timings)",
                                    color = MysticGold,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            items(VedicEngine.auspiciousMuhurats) { muhurat ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = DarkCosmicCard),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = muhurat.title,
                                                color = MysticGold,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Surface(
                                                color = NebulaTeal.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = muhurat.rating,
                                                    color = NebulaTeal,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = muhurat.hindiTitle,
                                            color = SaffronOrange,
                                            fontSize = 11.sp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Time: ${muhurat.timing}",
                                                color = TextGold,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = "Nakshatra: ${muhurat.nakshatra}",
                                                color = TextSilver,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        3 -> {
                            // Festivals Tab
                            item {
                                Text(
                                    text = "Sacred Hindu Festivals & Vrats",
                                    color = MysticGold,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            items(VedicEngine.upcomingFestivals) { fest ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = DarkCosmicCard),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = fest.name,
                                                color = MysticGold,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = fest.tithi,
                                                color = SaffronOrange,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        Text(
                                            text = fest.hindiName,
                                            color = TextSilver,
                                            fontSize = 12.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = fest.description,
                                            color = TextMuted,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PanchangView(panchang: PanchangData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCosmicCard),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderGold))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = panchang.dateStr,
                color = MysticGold,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = panchang.hindiDay,
                color = SaffronOrange,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Panchang 5 Pillars (Tithi, Vaar, Nakshatra, Yoga, Karana)
            PanchangRow(label = "Tithi", value = "${panchang.tithi} (${panchang.tithiPaksha})")
            PanchangRow(label = "Nakshatra", value = panchang.nakshatra)
            PanchangRow(label = "Yoga", value = panchang.yoga)
            PanchangRow(label = "Karana", value = panchang.karana)

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = BorderSubtle)
            Spacer(modifier = Modifier.height(14.dp))

            // Sun / Moon Timings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TimingChip(icon = Icons.Default.WbSunny, label = "Sunrise", value = panchang.sunrise, tint = PlanetSunColor)
                TimingChip(icon = Icons.Default.Nightlight, label = "Sunset", value = panchang.sunset, tint = SaffronOrange)
                TimingChip(icon = Icons.Default.Bedtime, label = "Moonrise", value = panchang.moonrise, tint = PlanetMoonColor)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Auspicious & Inauspicious Muhurats Card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    color = DarkCosmicSurface,
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CrimsonAstro.copy(alpha = 0.5f)))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Rahu Kaal (Avoid)", color = CrimsonAstro, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(panchang.rahuKaal, color = TextGold, fontSize = 11.sp)
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    color = DarkCosmicSurface,
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(NebulaTeal.copy(alpha = 0.5f)))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Abhijit (Auspicious)", color = NebulaTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(panchang.abhijitMuhurat, color = TextGold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun PanchangRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextMuted, fontSize = 12.sp)
        Text(text = value, color = TextGold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun TimingChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    tint: androidx.compose.ui.graphics.Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(text = label, color = TextMuted, fontSize = 10.sp)
            Text(text = value, color = TextGold, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun KundliMatchingView() {
    var boyName by remember { mutableStateOf("Aditya") }
    var girlName by remember { mutableStateOf("Pooja") }
    var boyRashiId by remember { mutableIntStateOf(1) } // Aries
    var girlRashiId by remember { mutableIntStateOf(5) } // Leo
    var boyNakshatraIdx by remember { mutableIntStateOf(0) }
    var girlNakshatraIdx by remember { mutableIntStateOf(9) }

    var matchResult by remember {
        mutableStateOf<CompatibilityResult?>(null)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkCosmicCard),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Ashtakoota 36 Guna Milan",
                    color = MysticGold,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Boy Details
                OutlinedTextField(
                    value = boyName,
                    onValueChange = { boyName = it },
                    label = { Text("Groom / Boy's Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextGold,
                        unfocusedTextColor = TextSilver,
                        focusedBorderColor = MysticGold,
                        unfocusedBorderColor = BorderSubtle
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Girl Details
                OutlinedTextField(
                    value = girlName,
                    onValueChange = { girlName = it },
                    label = { Text("Bride / Girl's Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextGold,
                        unfocusedTextColor = TextSilver,
                        focusedBorderColor = MysticGold,
                        unfocusedBorderColor = BorderSubtle
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        matchResult = VedicEngine.calculateCompatibility(
                            boyName = boyName,
                            boyMoonSignId = boyRashiId,
                            boyNakshatraIndex = boyNakshatraIdx,
                            girlName = girlName,
                            girlMoonSignId = girlRashiId,
                            girlNakshatraIndex = girlNakshatraIdx
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("match_kundli_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = MysticGold, contentColor = DeepCosmic),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Calculate Ashtakoota Match", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Match Result Card
        matchResult?.let { res ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkCosmicCard),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderGold))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Compatibility: ${res.totalScore} / ${res.maxScore.toInt()} Gunas",
                            color = MysticGold,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = if (res.totalScore >= 18) NebulaTeal.copy(alpha = 0.2f) else CrimsonAstro.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "${res.percentage}% Match",
                                color = if (res.totalScore >= 18) NebulaTeal else CrimsonAstro,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = res.verdict,
                        color = SaffronOrange,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = res.advice,
                        color = TextSilver,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = BorderSubtle)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Manglik Analysis: ${res.manglikVerdict}",
                        color = TextGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 8 Gunas Breakdown
                    res.gunaScores.forEach { guna ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${guna.name} (${guna.hindiName})",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "${guna.pointsObtained} / ${guna.maxPoints.toInt()} pts (${guna.status})",
                                color = if (guna.pointsObtained > 0) TextGold else CrimsonAstro,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
