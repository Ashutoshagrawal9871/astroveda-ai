package com.example.ui.screens

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astro.VedicEngine
import com.example.data.UserProfileEntity
import com.example.model.KundliChartData
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun KundliScreen(
    currentProfile: UserProfileEntity?,
    onSaveProfile: (UserProfileEntity) -> Unit,
    onNavigateToChat: () -> Unit
) {
    var showEditProfileDialog by remember { mutableStateOf(currentProfile == null) }
    var chartStyleNorth by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableIntStateOf(0) }

    val isSampleChart = currentProfile == null
    val chartData = remember(currentProfile) {
        if (currentProfile != null) {
            VedicEngine.getOrDefaultKundli(
                name = currentProfile.name,
                birthDate = currentProfile.birthDate,
                birthTime = currentProfile.birthTime,
                place = currentProfile.birthPlace,
                isTimeApproximate = currentProfile.isTimeUnknown,
                lat = currentProfile.lat,
                lng = currentProfile.lng
            )
        } else {
            VedicEngine.getOrDefaultKundli(
                name = "Sample Chart",
                birthDate = "1998-05-15",
                birthTime = "14:30",
                place = "New Delhi, India",
                isTimeApproximate = false
            )
        }
    }

    Scaffold(
        containerColor = DeepCosmic
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Notice for Sample Chart if unconfigured
            if (isSampleChart) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .testTag("sample_chart_banner"),
                        colors = CardDefaults.cardColors(containerColor = DarkCosmicSurfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, SaffronOrange)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = SaffronOrange, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Viewing Sample Demonstration Chart",
                                        color = MysticGold,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Create your personal birth profile for your authentic Kundli.",
                                        color = TextSilver,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Button(
                                onClick = { showEditProfileDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MysticGold, contentColor = DeepCosmic),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Create", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            // Profile Header Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("kundli_profile_card"),
                    colors = CardDefaults.cardColors(containerColor = DarkCosmicCard),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.5.dp, androidx.compose.ui.graphics.SolidColor(BorderGold))
                ) {
                    Column {
                        // Header band
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(listOf(SacredSaffron.copy(alpha = 0.15f), DarkCosmicCard)),
                                    androidx.compose.foundation.shape.RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(
                                                Brush.radialGradient(listOf(SaffronOrange.copy(alpha = 0.4f), Color(0x00000000))),
                                                CircleShape
                                            )
                                            .border(1.5.dp, MysticGold, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = chartData.personName.firstOrNull()?.uppercaseChar()?.toString() ?: "✦",
                                            color = MysticGold,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = chartData.personName,
                                            color = TextGold,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Text(
                                            text = "${chartData.birthDate}  ·  ${chartData.birthTime}  ${if (chartData.isTimeApproximate) "(≈)" else ""}",
                                            color = TextSilver,
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = chartData.birthPlace,
                                            color = TextMuted,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { showEditProfileDialog = true },
                                    modifier = Modifier.testTag("edit_kundli_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit birth details",
                                        tint = MysticGold
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = BorderSubtle)

                        Column(modifier = Modifier.padding(16.dp)) {
                            // Key Highlights Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AstroBadge("Lagna", chartData.lagnaSignName.take(8), SaffronOrange, Modifier.weight(1f))
                                AstroBadge("Moon Rashi", chartData.rashiSignName.take(8), PlanetMoonColor, Modifier.weight(1f))
                                AstroBadge("Nakshatra", chartData.nakshatra.take(8), MysticGold, Modifier.weight(1f))
                                AstroBadge("Dasha", chartData.currentMahadasha.take(7), CelestialViolet, Modifier.weight(1f))
                            }

                            if (chartData.isTimeApproximate) {
                                Spacer(modifier = Modifier.height(10.dp))
                                UnknownTimeNoticeCard()
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Tab Row (Chart, Planets, Houses, Dashas, Transits)
            item {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = DarkCosmicSurface,
                    contentColor = MysticGold,
                    edgePadding = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Birth Chart") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Planets") }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("12 Houses") }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = { Text("Dashas") }
                    )
                    Tab(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        text = { Text("Transits") }
                    )
                    Tab(
                        selected = selectedTab == 5,
                        onClick = { selectedTab = 5 },
                        text = { Text("Guna Milan") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Tab Content
            when (selectedTab) {
                0 -> {
                    item {
                        // Advanced Visual Representation of Vedic Kundli Chart with Canvas Graphics & SVG Paths
                        VedicKundliVisualChart(
                            chartData = chartData,
                            initialStyle = if (chartStyleNorth) KundliChartStyle.NORTH_INDIAN else KundliChartStyle.SOUTH_INDIAN
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Ask AI about this chart button
                        Button(
                            onClick = onNavigateToChat,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("ask_ai_astrologer_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = SacredSaffron),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Consult AI",
                                tint = DeepCosmic
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ask Devi Priya About Your Kundli",
                                color = DeepCosmic,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                1 -> {
                    item {
                        Text(
                            text = "Planetary Dignities & Placements",
                            color = MysticGold,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    items(chartData.planets) { planet ->
                        PlanetPlacementRow(planet = planet)
                    }
                }

                2 -> {
                    item {
                        Text(
                            text = "12 Bhavas (Houses) Analysis",
                            color = MysticGold,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    items(chartData.houses) { house ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkCosmicCard),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "House ${house.number}: ${house.signName}",
                                        color = MysticGold,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Lord: ${house.lord}",
                                        color = TextSilver,
                                        fontSize = 12.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = house.significations,
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                                if (house.planets.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Occupants: ${house.planets.joinToString(", ")}",
                                        color = SaffronOrange,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                3 -> {
                    item {
                        DashaTimelineView(dashas = chartData.dashas)
                    }
                }

                4 -> {
                    item {
                        Text(
                            text = "Current Planetary Transits (Gochar)",
                            color = MysticGold,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Transit impacts on your ${chartData.rashiSignName} Moon sign:",
                            color = TextSilver,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        val transitList = listOf(
                            Triple("Guru (Jupiter)", "Gemini / Taurus", "Brings expansion of intellect and favorable professional associations."),
                            Triple("Shani (Saturn)", "Aquarius (Kumbha)", "Demands disciplined work habits and structural karmic balance."),
                            Triple("Rahu & Ketu", "Pisces & Virgo Axis", "Deepens spiritual inquiry and intuitive sensitivity."),
                            Triple("Surya (Sun)", "Virgo / Libra", "Highlights leadership, vitality, and social recognition.")
                        )

                        transitList.forEach { (planet, sign, impact) ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkCosmicCard),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "$planet currently in $sign",
                                        color = SaffronOrange,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = impact,
                                        color = TextSilver,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }

                5 -> {
                    item {
                        KundliMatchingScreen(
                            onConsultAstrologer = { onNavigateToChat() },
                            isEmbedded = true
                        )
                    }
                }
            }
        }

        if (showEditProfileDialog) {
            BirthDetailsDialog(
                initial = currentProfile,
                onDismiss = { showEditProfileDialog = false },
                onSave = { updatedProfile ->
                    onSaveProfile(updatedProfile)
                    showEditProfileDialog = false
                }
            )
        }
    }
}

@Composable
private fun AstroBadge(
    title: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = DarkCosmicSurface,
        shape = RoundedCornerShape(8.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(color.copy(alpha = 0.4f)))
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = TextMuted,
                fontSize = 9.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun BirthDetailsDialog(
    initial: UserProfileEntity?,
    onDismiss: () -> Unit,
    onSave: (UserProfileEntity) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var birthDate by remember { mutableStateOf(initial?.birthDate ?: "") }
    var birthTime by remember { mutableStateOf(initial?.birthTime ?: "") }
    var isTimeUnknown by remember { mutableStateOf(initial?.isTimeUnknown ?: false) }
    var birthPlace by remember { mutableStateOf(initial?.birthPlace ?: "") }
    var gender by remember { mutableStateOf(initial?.gender ?: "Male") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = if (initial == null) "Create Birth Chart (Kundli)" else "Edit Birth Details",
                    color = MysticGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Accurate birth details ensure precise Lagna & planetary houses.",
                    color = TextSilver,
                    fontSize = 11.sp
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    placeholder = { Text("e.g. Aarav Sharma", color = TextMuted) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("birth_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextGold,
                        unfocusedTextColor = TextSilver,
                        focusedBorderColor = MysticGold,
                        unfocusedBorderColor = BorderSubtle
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Gender", color = TextSilver, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Male", "Female", "Other").forEach { option ->
                        FilterChip(
                            selected = gender == option,
                            onClick = { gender = option },
                            label = { Text(option, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MysticGold,
                                selectedLabelColor = DeepCosmic,
                                labelColor = TextSilver
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = birthDate,
                    onValueChange = { birthDate = it },
                    label = { Text("Birth Date (YYYY-MM-DD)") },
                    placeholder = { Text("e.g. 1998-08-15", color = TextMuted) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextGold,
                        unfocusedTextColor = TextSilver,
                        focusedBorderColor = MysticGold,
                        unfocusedBorderColor = BorderSubtle
                    )
                )
                Text(
                    text = "• Determines Sun sign, planetary degrees & Mahadasha periods",
                    color = TextMuted,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (!isTimeUnknown) {
                    OutlinedTextField(
                        value = birthTime,
                        onValueChange = { birthTime = it },
                        label = { Text("Birth Time (24h HH:mm)") },
                        placeholder = { Text("e.g. 14:30", color = TextMuted) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextGold,
                            unfocusedTextColor = TextSilver,
                            focusedBorderColor = MysticGold,
                            unfocusedBorderColor = BorderSubtle
                        )
                    )
                    Text(
                        text = "• Determines your Ascendant (Lagna) which shifts every ~2 hours",
                        color = TextMuted,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isTimeUnknown = !isTimeUnknown }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isTimeUnknown,
                        onCheckedChange = { isTimeUnknown = it },
                        colors = CheckboxDefaults.colors(checkedColor = MysticGold)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Birth time unknown or approximate",
                        color = TextGold,
                        fontSize = 12.sp
                    )
                }

                if (isTimeUnknown) {
                    Text(
                        text = "Vedic Note: Without exact birth time, Moon Sign (Rashi) and Nakshatra are calculated accurately (Moon stays in sign ~2.5 days), while house boundaries are estimated at solar noon.",
                        color = SaffronOrange,
                        fontSize = 10.sp,
                        lineHeight = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = birthPlace,
                    onValueChange = { birthPlace = it },
                    label = { Text("Birth City & Country") },
                    placeholder = { Text("e.g. New Delhi, India", color = TextMuted) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextGold,
                        unfocusedTextColor = TextSilver,
                        focusedBorderColor = MysticGold,
                        unfocusedBorderColor = BorderSubtle
                    )
                )
                Text(
                    text = "• Determines latitude/longitude for local sunrise & astronomical horizon",
                    color = TextMuted,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalDate = birthDate.ifBlank { "1998-01-01" }
                    val finalPlace = birthPlace.ifBlank { "New Delhi, India" }
                    val p = UserProfileEntity(
                        id = initial?.id ?: 0L,
                        name = name.ifBlank { "Seeker" },
                        gender = gender,
                        birthDate = finalDate,
                        birthTime = if (isTimeUnknown || birthTime.isBlank()) "12:00" else birthTime,
                        isTimeUnknown = isTimeUnknown,
                        birthPlace = finalPlace,
                        isPrimary = true
                    )
                    onSave(p)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MysticGold),
                modifier = Modifier.testTag("save_birth_details_button")
            ) {
                Text("Generate Chart", color = DeepCosmic, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            if (initial != null) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = TextSilver)
                }
            }
        },
        containerColor = DarkCosmicSurface
    )
}
