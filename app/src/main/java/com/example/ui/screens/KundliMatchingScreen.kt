package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astro.VedicEngine
import com.example.model.CompatibilityResult
import com.example.model.GunaScore
import com.example.ui.theme.*

/**
 * Full Kundli Matching (Horoscope Milan / Guna Milan) Screen.
 * Allows users to enter detailed birth data for two individuals (Boy/Groom and Girl/Bride):
 * Name, Birth Date (YYYY-MM-DD), Birth Time (HH:mm), Birth Place, or direct Rashi & Nakshatra selector.
 * Calculates the authentic Ashtakoota 36 Guna Milan compatibility score with Manglik analysis,
 * individual 8 Guna breakdowns, dosha warnings, and Vedic marital remedies.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KundliMatchingScreen(
    onBack: (() -> Unit)? = null,
    onConsultAstrologer: ((CompatibilityResult) -> Unit)? = null,
    isEmbedded: Boolean = false
) {
    // Mode toggle: 0 = By Full Birth Details (Calculates exact Moon sign & Nakshatra automatically), 1 = Direct Moon Sign & Nakshatra Selection
    var inputMode by remember { mutableIntStateOf(0) }

    // Person 1 (Boy / Groom) State
    var boyName by remember { mutableStateOf("Aditya Sharma") }
    var boyBirthDate by remember { mutableStateOf("1996-08-24") }
    var boyBirthTime by remember { mutableStateOf("07:45") }
    var boyBirthPlace by remember { mutableStateOf("New Delhi, India") }
    var boyMoonSignId by remember { mutableIntStateOf(5) } // Leo (Simha)
    var boyNakshatraIndex by remember { mutableIntStateOf(10) } // Purva Phalguni
    var boyIsManglik by remember { mutableStateOf(false) }

    // Person 2 (Girl / Bride) State
    var girlName by remember { mutableStateOf("Pooja Verma") }
    var girlBirthDate by remember { mutableStateOf("1998-11-12") }
    var girlBirthTime by remember { mutableStateOf("14:20") }
    var girlBirthPlace by remember { mutableStateOf("Jaipur, Rajasthan") }
    var girlMoonSignId by remember { mutableIntStateOf(4) } // Cancer (Karka)
    var girlNakshatraIndex by remember { mutableIntStateOf(8) } // Ashlesha
    var girlIsManglik by remember { mutableStateOf(false) }

    var isCalculating by remember { mutableStateOf(false) }
    var matchResult by remember { mutableStateOf<CompatibilityResult?>(null) }
    var expandedGunaIndex by remember { mutableIntStateOf(-1) }

    fun runKundliMatch() {
        isCalculating = true

        val (actualBoyRashiId, actualBoyNakIdx, actualBoyManglik) = if (inputMode == 0) {
            val chart = VedicEngine.getOrDefaultKundli(
                name = boyName.ifBlank { "Groom" },
                birthDate = boyBirthDate,
                birthTime = boyBirthTime,
                place = boyBirthPlace,
                isTimeApproximate = false
            )
            val nakIdx = VedicEngine.nakshatras.indexOfFirst { it.name.equals(chart.nakshatra, ignoreCase = true) }.let {
                if (it >= 0) it else 0
            }
            Triple(chart.rashiSignId, nakIdx, chart.manglikStatus.startsWith("Manglik", ignoreCase = true))
        } else {
            Triple(boyMoonSignId, boyNakshatraIndex, boyIsManglik)
        }

        val (actualGirlRashiId, actualGirlNakIdx, actualGirlManglik) = if (inputMode == 0) {
            val chart = VedicEngine.getOrDefaultKundli(
                name = girlName.ifBlank { "Bride" },
                birthDate = girlBirthDate,
                birthTime = girlBirthTime,
                place = girlBirthPlace,
                isTimeApproximate = false
            )
            val nakIdx = VedicEngine.nakshatras.indexOfFirst { it.name.equals(chart.nakshatra, ignoreCase = true) }.let {
                if (it >= 0) it else 0
            }
            Triple(chart.rashiSignId, nakIdx, chart.manglikStatus.startsWith("Manglik", ignoreCase = true))
        } else {
            Triple(girlMoonSignId, girlNakshatraIndex, girlIsManglik)
        }

        val res = VedicEngine.calculateCompatibility(
            boyName = boyName.ifBlank { "Groom" },
            boyMoonSignId = actualBoyRashiId,
            boyNakshatraIndex = actualBoyNakIdx,
            girlName = girlName.ifBlank { "Bride" },
            girlMoonSignId = actualGirlRashiId,
            girlNakshatraIndex = actualGirlNakIdx
        )

        // Enhance with Manglik details
        val manglikVerdict = when {
            actualBoyManglik && actualGirlManglik -> "Both partners have matching Manglik status; Dosha cancels out naturally (Parihara)."
            actualBoyManglik && !actualGirlManglik -> "Groom is Manglik; Kumbha Vivah or Mangal Shanti ritual is recommended."
            !actualBoyManglik && actualGirlManglik -> "Bride is Manglik; Vishnu Vivah remedy is recommended before marriage."
            else -> "Neither partner is Manglik. Purely auspicious planetary alignment."
        }

        matchResult = res.copy(
            boyManglik = actualBoyManglik,
            girlManglik = actualGirlManglik,
            manglikVerdict = manglikVerdict
        )
        isCalculating = false
    }

    // Auto-calculate default match on initial launch
    LaunchedEffect(Unit) {
        runKundliMatch()
    }

    val content: @Composable (Modifier) -> Unit = { listModifier ->
        if (isEmbedded) {
            Column(
                modifier = listModifier
                    .fillMaxWidth()
                    .testTag("kundli_matching_screen"),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Mode Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    FilterChip(
                        selected = inputMode == 0,
                        onClick = { inputMode = 0 },
                        label = { Text("Exact Birth Details") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DarkCosmicSurfaceVariant,
                            selectedLabelColor = MysticGold
                        ),
                        modifier = Modifier.testTag("mode_birth_details_chip")
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    FilterChip(
                        selected = inputMode == 1,
                        onClick = { inputMode = 1 },
                        label = { Text("Direct Rashi & Nakshatra") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DarkCosmicSurfaceVariant,
                            selectedLabelColor = MysticGold
                        ),
                        modifier = Modifier.testTag("mode_rashi_nakshatra_chip")
                    )
                }

                // Person 1: Boy / Groom Details Card
                PersonInputCard(
                    title = "Groom / Boy's Details (वर)",
                    name = boyName,
                    onNameChange = { boyName = it },
                    inputMode = inputMode,
                    birthDate = boyBirthDate,
                    onBirthDateChange = { boyBirthDate = it },
                    birthTime = boyBirthTime,
                    onBirthTimeChange = { boyBirthTime = it },
                    birthPlace = boyBirthPlace,
                    onBirthPlaceChange = { boyBirthPlace = it },
                    selectedRashiId = boyMoonSignId,
                    onRashiSelected = { boyMoonSignId = it },
                    selectedNakshatraIdx = boyNakshatraIndex,
                    onNakshatraSelected = { boyNakshatraIndex = it },
                    isManglik = boyIsManglik,
                    onManglikChanged = { boyIsManglik = it },
                    accentColor = NebulaTeal,
                    testTagPrefix = "boy"
                )

                // Person 2: Girl / Bride Details Card
                PersonInputCard(
                    title = "Bride / Girl's Details (कन्या)",
                    name = girlName,
                    onNameChange = { girlName = it },
                    inputMode = inputMode,
                    birthDate = girlBirthDate,
                    onBirthDateChange = { girlBirthDate = it },
                    birthTime = girlBirthTime,
                    onBirthTimeChange = { girlBirthTime = it },
                    birthPlace = girlBirthPlace,
                    onBirthPlaceChange = { girlBirthPlace = it },
                    selectedRashiId = girlMoonSignId,
                    onRashiSelected = { girlMoonSignId = it },
                    selectedNakshatraIdx = girlNakshatraIndex,
                    onNakshatraSelected = { girlNakshatraIndex = it },
                    isManglik = girlIsManglik,
                    onManglikChanged = { girlIsManglik = it },
                    accentColor = SacredSaffron,
                    testTagPrefix = "girl"
                )

                // Match Button
                Button(
                    onClick = { runKundliMatch() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("calculate_kundli_match_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = MysticGold, contentColor = DeepCosmic),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isCalculating
                ) {
                    if (isCalculating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = DeepCosmic,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Aligning Stars...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(imageVector = Icons.Default.Favorite, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Calculate 36 Guna Milan & Dosha",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Results Section
                matchResult?.let { res ->
                    CompatibilityOverviewCard(result = res)
                    ManglikAndDoshaCard(result = res)

                    Text(
                        text = "Detailed 8 Guna Breakdown (Ashtakoota)",
                        color = MysticGold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Vedic matchmaking evaluates 8 essential psychological, spiritual, and physical kootas (Max 36 Points).",
                        color = TextMuted,
                        fontSize = 11.sp
                    )

                    res.gunaScores.forEachIndexed { index, guna ->
                        val isExpanded = expandedGunaIndex == index
                        GunaDetailCard(
                            guna = guna,
                            isExpanded = isExpanded,
                            onToggle = { expandedGunaIndex = if (isExpanded) -1 else index }
                        )
                    }

                    // Remedial Advice & Astrologer Consultation Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("compatibility_remedy_card"),
                        colors = CardDefaults.cardColors(containerColor = DarkCosmicCard),
                        shape = RoundedCornerShape(16.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderGold.copy(alpha = 0.6f)))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Spa,
                                    contentDescription = null,
                                    tint = SaffronOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Vedic Guidance & Remedies",
                                    color = MysticGold,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = res.advice,
                                color = TextGold,
                                fontSize = 13.sp,
                                lineHeight = 19.sp
                            )

                            if (onConsultAstrologer != null) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = { onConsultAstrologer(res) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SacredSaffron, contentColor = DeepCosmic),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("ask_astrologer_match_button"),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Consult Devi Priya on Marriage Prospects", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = listModifier
                    .fillMaxSize()
                    .testTag("kundli_matching_screen")
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Mode Selector
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        FilterChip(
                            selected = inputMode == 0,
                            onClick = { inputMode = 0 },
                            label = { Text("Exact Birth Details") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DarkCosmicSurfaceVariant,
                                selectedLabelColor = MysticGold
                            ),
                            modifier = Modifier.testTag("mode_birth_details_chip")
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        FilterChip(
                            selected = inputMode == 1,
                            onClick = { inputMode = 1 },
                            label = { Text("Direct Rashi & Nakshatra") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DarkCosmicSurfaceVariant,
                                selectedLabelColor = MysticGold
                            ),
                            modifier = Modifier.testTag("mode_rashi_nakshatra_chip")
                        )
                    }
                }

                // Person 1: Boy / Groom Details Card
                item {
                    PersonInputCard(
                        title = "Groom / Boy's Details (वर)",
                        name = boyName,
                        onNameChange = { boyName = it },
                        inputMode = inputMode,
                        birthDate = boyBirthDate,
                        onBirthDateChange = { boyBirthDate = it },
                        birthTime = boyBirthTime,
                        onBirthTimeChange = { boyBirthTime = it },
                        birthPlace = boyBirthPlace,
                        onBirthPlaceChange = { boyBirthPlace = it },
                        selectedRashiId = boyMoonSignId,
                        onRashiSelected = { boyMoonSignId = it },
                        selectedNakshatraIdx = boyNakshatraIndex,
                        onNakshatraSelected = { boyNakshatraIndex = it },
                        isManglik = boyIsManglik,
                        onManglikChanged = { boyIsManglik = it },
                        accentColor = NebulaTeal,
                        testTagPrefix = "boy"
                    )
                }

                // Person 2: Girl / Bride Details Card
                item {
                    PersonInputCard(
                        title = "Bride / Girl's Details (कन्या)",
                        name = girlName,
                        onNameChange = { girlName = it },
                        inputMode = inputMode,
                        birthDate = girlBirthDate,
                        onBirthDateChange = { girlBirthDate = it },
                        birthTime = girlBirthTime,
                        onBirthTimeChange = { girlBirthTime = it },
                        birthPlace = girlBirthPlace,
                        onBirthPlaceChange = { girlBirthPlace = it },
                        selectedRashiId = girlMoonSignId,
                        onRashiSelected = { girlMoonSignId = it },
                        selectedNakshatraIdx = girlNakshatraIndex,
                        onNakshatraSelected = { girlNakshatraIndex = it },
                        isManglik = girlIsManglik,
                        onManglikChanged = { girlIsManglik = it },
                        accentColor = SacredSaffron,
                        testTagPrefix = "girl"
                    )
                }

                // Match Button
                item {
                    Button(
                        onClick = { runKundliMatch() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("calculate_kundli_match_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MysticGold, contentColor = DeepCosmic),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isCalculating
                    ) {
                        if (isCalculating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = DeepCosmic,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Aligning Stars...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(imageVector = Icons.Default.Favorite, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Calculate 36 Guna Milan & Dosha",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Results Section
                matchResult?.let { res ->
                    item {
                        CompatibilityOverviewCard(result = res)
                    }

                    item {
                        ManglikAndDoshaCard(result = res)
                    }

                    item {
                        Text(
                            text = "Detailed 8 Guna Breakdown (Ashtakoota)",
                            color = MysticGold,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Vedic matchmaking evaluates 8 essential psychological, spiritual, and physical kootas (Max 36 Points).",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    items(res.gunaScores.indices.toList()) { index ->
                        val guna = res.gunaScores[index]
                        val isExpanded = expandedGunaIndex == index
                        GunaDetailCard(
                            guna = guna,
                            isExpanded = isExpanded,
                            onToggle = { expandedGunaIndex = if (isExpanded) -1 else index }
                        )
                    }

                    // Remedial Advice & Astrologer Consultation Card
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("compatibility_remedy_card"),
                            colors = CardDefaults.cardColors(containerColor = DarkCosmicCard),
                            shape = RoundedCornerShape(16.dp),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderGold.copy(alpha = 0.6f)))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Spa,
                                        contentDescription = null,
                                        tint = SaffronOrange,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Vedic Guidance & Remedies",
                                        color = MysticGold,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = res.advice,
                                    color = TextGold,
                                    fontSize = 13.sp,
                                    lineHeight = 19.sp
                                )

                                if (onConsultAstrologer != null) {
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Button(
                                        onClick = { onConsultAstrologer(res) },
                                        colors = ButtonDefaults.buttonColors(containerColor = SacredSaffron, contentColor = DeepCosmic),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("ask_astrologer_match_button"),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Consult Devi Priya on Marriage Prospects", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (isEmbedded) {
        content(Modifier)
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Kundli Milan & Compatibility",
                                color = MysticGold,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Ashtakoota 36 Guna Milan & Vedic Matchmaking",
                                color = TextSilver,
                                fontSize = 11.sp
                            )
                        }
                    },
                    navigationIcon = {
                        if (onBack != null) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MysticGold
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .padding(start = 12.dp, end = 4.dp)
                                    .size(34.dp)
                                    .background(DarkCosmicSurfaceVariant, CircleShape)
                                    .border(1.dp, BorderGold, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = SaffronOrange,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkCosmicSurface
                    )
                )
            },
            containerColor = DeepCosmic
        ) { innerPadding ->
            content(Modifier.padding(innerPadding))
        }
    }
}

@Composable
private fun PersonInputCard(
    title: String,
    name: String,
    onNameChange: (String) -> Unit,
    inputMode: Int,
    birthDate: String,
    onBirthDateChange: (String) -> Unit,
    birthTime: String,
    onBirthTimeChange: (String) -> Unit,
    birthPlace: String,
    onBirthPlaceChange: (String) -> Unit,
    selectedRashiId: Int,
    onRashiSelected: (Int) -> Unit,
    selectedNakshatraIdx: Int,
    onNakshatraSelected: (Int) -> Unit,
    isManglik: Boolean,
    onManglikChanged: (Boolean) -> Unit,
    accentColor: Color,
    testTagPrefix: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("${testTagPrefix}_input_card"),
        colors = CardDefaults.cardColors(containerColor = DarkCosmicCard),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(accentColor.copy(alpha = 0.5f)))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(accentColor, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = MysticGold,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Name field
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Full Name") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("${testTagPrefix}_name_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextGold,
                    unfocusedTextColor = TextSilver,
                    focusedBorderColor = MysticGold,
                    unfocusedBorderColor = BorderSubtle
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (inputMode == 0) {
                // Exact Birth Details Mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = birthDate,
                        onValueChange = onBirthDateChange,
                        label = { Text("Date (YYYY-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("${testTagPrefix}_birthdate_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextGold,
                            unfocusedTextColor = TextSilver,
                            focusedBorderColor = MysticGold,
                            unfocusedBorderColor = BorderSubtle
                        )
                    )

                    OutlinedTextField(
                        value = birthTime,
                        onValueChange = onBirthTimeChange,
                        label = { Text("Time (HH:mm)") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(0.8f)
                            .testTag("${testTagPrefix}_birthtime_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextGold,
                            unfocusedTextColor = TextSilver,
                            focusedBorderColor = MysticGold,
                            unfocusedBorderColor = BorderSubtle
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = birthPlace,
                    onValueChange = onBirthPlaceChange,
                    label = { Text("Birth City & Country") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("${testTagPrefix}_birthplace_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextGold,
                        unfocusedTextColor = TextSilver,
                        focusedBorderColor = MysticGold,
                        unfocusedBorderColor = BorderSubtle
                    )
                )
            } else {
                // Direct Moon Sign & Nakshatra Selector Mode
                var rashiExpanded by remember { mutableStateOf(false) }
                var nakshatraExpanded by remember { mutableStateOf(false) }

                val selectedRashi = VedicEngine.zodiacSigns.firstOrNull { it.id == selectedRashiId }
                val selectedNak = VedicEngine.nakshatras.getOrNull(selectedNakshatraIdx)

                // Moon Sign Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { rashiExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextGold)
                    ) {
                        Text(
                            text = "Rashi: ${selectedRashi?.englishName ?: "Aries"} (${selectedRashi?.hindiName ?: "मेष"})",
                            modifier = Modifier.weight(1f)
                        )
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                    }

                    DropdownMenu(
                        expanded = rashiExpanded,
                        onDismissRequest = { rashiExpanded = false },
                        modifier = Modifier.background(DarkCosmicSurface)
                    ) {
                        VedicEngine.zodiacSigns.forEach { sign ->
                            DropdownMenuItem(
                                text = { Text("${sign.englishName} (${sign.hindiName}) - ${sign.symbol}", color = TextGold) },
                                onClick = {
                                    onRashiSelected(sign.id)
                                    rashiExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Nakshatra Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { nakshatraExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextGold)
                    ) {
                        Text(
                            text = "Nakshatra: ${selectedNak?.name ?: "Ashwini"} (${selectedNak?.hindiName ?: "अश्विनी"})",
                            modifier = Modifier.weight(1f)
                        )
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                    }

                    DropdownMenu(
                        expanded = nakshatraExpanded,
                        onDismissRequest = { nakshatraExpanded = false },
                        modifier = Modifier.background(DarkCosmicSurface)
                    ) {
                        VedicEngine.nakshatras.forEachIndexed { idx, nak ->
                            DropdownMenuItem(
                                text = { Text("${nak.id}. ${nak.name} (${nak.hindiName}) - ${nak.gana} Gana", color = TextGold) },
                                onClick = {
                                    onNakshatraSelected(idx)
                                    nakshatraExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onManglikChanged(!isManglik) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isManglik,
                        onCheckedChange = onManglikChanged,
                        colors = CheckboxDefaults.colors(checkedColor = CrimsonAstro)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Marked as Manglik (Kuja Dosha)",
                        color = TextGold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CompatibilityOverviewCard(result: CompatibilityResult) {
    val isGood = result.totalScore >= 18.0
    val scoreColor = if (result.totalScore >= 24) NebulaTeal else if (result.totalScore >= 18) MysticGold else CrimsonAstro

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("compatibility_overview_card"),
        colors = CardDefaults.cardColors(containerColor = DarkCosmicCard),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(scoreColor))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${result.boyName} & ${result.girlName}",
                        color = MysticGold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${result.boyRashi} (${result.boyNakshatra})  ♥  ${result.girlRashi} (${result.girlNakshatra})",
                        color = TextSilver,
                        fontSize = 11.sp
                    )
                }

                Surface(
                    color = scoreColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(scoreColor))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${result.totalScore.toInt()} / ${result.maxScore.toInt()}",
                            color = scoreColor,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${result.percentage}% Match",
                            color = scoreColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = BorderSubtle)
            Spacer(modifier = Modifier.height(12.dp))

            // Score Progress Bar
            LinearProgressIndicator(
                progress = { (result.totalScore / result.maxScore).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = scoreColor,
                trackColor = DarkCosmicSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = result.verdict,
                color = if (isGood) SaffronOrange else CrimsonAstro,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isGood)
                    "18 or more gunas indicates acceptable Vedic compatibility for a blissful and prosperous married life."
                else
                    "Below 18 points signifies substantial karmic hurdles; marriage requires careful remedial neutralization.",
                color = TextSilver,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun ManglikAndDoshaCard(result: CompatibilityResult) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("manglik_analysis_card"),
        colors = CardDefaults.cardColors(containerColor = DarkCosmicCard),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = if (result.boyManglik || result.girlManglik) SaffronOrange else NebulaTeal,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Manglik (Mars) & Dosha Compatibility",
                    color = MysticGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Groom: ${if (result.boyManglik) "Manglik (Mars in 1/4/7/8/12)" else "Non-Manglik"}",
                    color = if (result.boyManglik) CrimsonAstro else NebulaTeal,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Bride: ${if (result.girlManglik) "Manglik (Mars in 1/4/7/8/12)" else "Non-Manglik"}",
                    color = if (result.girlManglik) CrimsonAstro else NebulaTeal,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = result.manglikVerdict,
                color = TextGold,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun GunaDetailCard(
    guna: GunaScore,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val isPerfect = guna.pointsObtained == guna.maxPoints
    val isZero = guna.pointsObtained == 0.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCosmicCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${guna.name} (${guna.hindiName})",
                            color = MysticGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• ${guna.status}",
                            color = if (isPerfect) NebulaTeal else if (isZero) CrimsonAstro else SaffronOrange,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = guna.description,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${guna.pointsObtained.toInt()} / ${guna.maxPoints.toInt()} pts",
                        color = if (isPerfect) NebulaTeal else if (isZero) CrimsonAstro else TextGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle Guna Detail",
                        tint = TextSilver,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    HorizontalDivider(color = BorderSubtle)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = getGunaSignificationText(guna.name),
                        color = TextSilver,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

private fun getGunaSignificationText(gunaName: String): String {
    return when (gunaName) {
        "Varna" -> "Measures spiritual and ego alignment. Boy's varna should ideally be equal or higher than girl's for harmonious role dynamics."
        "Vashya" -> "Assesses mutual magnetic attraction, mutual influence, and who yields control during marital disputes."
        "Tara" -> "Evaluates health, longevity, destiny, and reciprocal fortune of the bride and groom based on Moon nakshatra distance."
        "Yoni" -> "Measures sexual compatibility, instinctual nature, biological affinity, and physical mutual satisfaction."
        "Graha Maitri" -> "Analyzes friendship of Moon sign planetary lords; critical for psychological wavelength and intellectual harmony."
        "Gana" -> "Analyzes temperament (Deva = divine/calm, Manushya = human/practical, Rakshasa = fierce/stubborn). Deva & Rakshasa clash generates friction."
        "Bhakoot" -> "Crucial 7 points evaluating financial abundance, family prosperity, and emotional longevity. Avoids 6-8 (Shadashtak) or 2-12 (Dwidwadash) clash."
        "Nadi" -> "Most critical 8 points measuring physiological health, blood genetics, and healthy progeny. Same Nadi causes severe Nadi Dosha requiring remedial puja."
        else -> "Evaluates overall Vedic psychological and cosmic alignment."
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0D0B1A)
@Composable
fun KundliMatchingScreenPreview() {
    MyApplicationTheme {
        KundliMatchingScreen(isEmbedded = true)
    }
}
