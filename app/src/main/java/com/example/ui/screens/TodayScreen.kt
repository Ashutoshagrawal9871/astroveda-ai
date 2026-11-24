package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astro.VedicEngine
import com.example.data.AstroRepository
import com.example.data.UserProfileEntity
import com.example.model.KundliChartData
import com.example.model.PanchangData
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    currentProfile: UserProfileEntity?,
    allProfiles: List<UserProfileEntity>,
    repository: AstroRepository,
    language: String = "en",
    chatCredits: Int = 0,
    onSelectProfile: (Long) -> Unit,
    onAddNewProfile: () -> Unit,
    onNavigateToExplore: () -> Unit,
    onNavigateToAskAi: (String?) -> Unit,
    onNavigateToPlans: () -> Unit,
    onSaveReading: (title: String, category: String, content: String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showProfileDropdown by remember { mutableStateOf(false) }
    var dailyInsightText by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isSavedToBookmarks by remember { mutableStateOf(false) }

    val panchang: PanchangData = remember { VedicEngine.getTodayPanchang() }

    val todayFormatted = remember {
        SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date())
    }
    val todayIsoDate = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    val kundli: KundliChartData? = remember(currentProfile?.id, currentProfile?.birthDate, currentProfile?.birthTime) {
        currentProfile?.let { p ->
            VedicEngine.calculateKundli(
                name = p.name,
                birthDate = p.birthDate,
                birthTime = p.birthTime,
                place = p.birthPlace,
                isTimeApproximate = p.isTimeUnknown,
                lat = p.lat,
                lng = p.lng
            )
        }
    }

    val guidance = remember(kundli?.rashiSignName) {
        kundli?.let { VedicEngine.getHoroscopeGuidance(it.rashiSignName, "Today") }
    }

    LaunchedEffect(currentProfile?.id, language) {
        if (currentProfile != null && currentProfile.birthDate.isNotBlank()) {
            val cached = repository.getDailyHoroscopeCached(currentProfile.id, todayIsoDate, language)
            if (cached != null) {
                dailyInsightText = cached
            } else {
                isLoading = true
                try {
                    val result = repository.fetchPersonalizedDailyHoroscope(
                        profile = currentProfile,
                        language = language,
                        forceRefresh = false
                    )
                    dailyInsightText = result
                } catch (_: Exception) {
                    if (kundli != null) {
                        dailyInsightText = VedicEngine.formatDailyHoroscopeReading(kundli, language)
                    }
                } finally {
                    isLoading = false
                }
            }
        } else {
            dailyInsightText = null
            isLoading = false
        }
    }

    Scaffold(containerColor = DeepCosmic) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("today_screen"),
            contentPadding = PaddingValues(bottom = 36.dp)
        ) {

            // ── Hero Header ───────────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(HeroGradient))
                        .padding(horizontal = 20.dp)
                        .padding(top = 20.dp, bottom = 24.dp)
                ) {
                    Column {
                        // Greeting row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (currentProfile != null)
                                        "Namaste, ${currentProfile.name.ifBlank { "Seeker" }} 🙏"
                                    else "Namaste, Seeker 🙏",
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = todayFormatted,
                                    color = TextSilver.copy(alpha = 0.8f),
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            // Credits chip
                            Surface(
                                onClick = onNavigateToPlans,
                                color = Color.White.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, MysticGold.copy(alpha = 0.6f)),
                                modifier = Modifier.testTag("today_credits_chip")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MysticGold, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (chatCredits > 0) "$chatCredits" else "Plans",
                                        color = MysticGold,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Profile selector pill
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Surface(
                                onClick = { showProfileDropdown = true },
                                color = Color.White.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
                                modifier = Modifier.fillMaxWidth().testTag("profile_selector_pill")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Letter avatar
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(
                                                    Brush.radialGradient(listOf(SaffronOrange.copy(alpha = 0.5f), Color.Transparent)),
                                                    CircleShape
                                                )
                                                .border(1.5.dp, MysticGold, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = currentProfile?.name?.firstOrNull()?.uppercaseChar()?.toString() ?: "✦",
                                                color = MysticGold,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = currentProfile?.name?.ifBlank { "Personal Chart" } ?: "No Profile — Tap to Add",
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = if (kundli != null) "☽ ${kundli.rashiSignName}  ·  Asc ${kundli.lagnaSignName}"
                                                else "Add birth details for personalised readings",
                                                color = TextSilver.copy(alpha = 0.7f),
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Switch Profile", tint = MysticGold)
                                }
                            }

                            DropdownMenu(
                                expanded = showProfileDropdown,
                                onDismissRequest = { showProfileDropdown = false },
                                modifier = Modifier.background(DarkCosmicCard)
                            ) {
                                allProfiles.forEach { p ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(p.name, color = if (p.id == currentProfile?.id) MysticGold else TextSilver, fontWeight = FontWeight.SemiBold)
                                                Text("${p.birthDate} · ${p.birthPlace}", color = TextMuted, fontSize = 10.sp)
                                            }
                                        },
                                        leadingIcon = {
                                            if (p.id == currentProfile?.id)
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MysticGold, modifier = Modifier.size(16.dp))
                                        },
                                        onClick = { onSelectProfile(p.id); showProfileDropdown = false }
                                    )
                                }
                                HorizontalDivider(color = BorderSubtle)
                                DropdownMenuItem(
                                    text = { Text("+ Add New Birth Chart", color = SaffronOrange, fontWeight = FontWeight.Bold) },
                                    leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, tint = SaffronOrange) },
                                    onClick = { showProfileDropdown = false; onAddNewProfile() }
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // ── Onboarding or Daily Insight Card ─────────────────────────
            item {
                if (currentProfile == null) {
                    // Onboarding card
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).testTag("today_onboarding_card"),
                        colors = CardDefaults.cardColors(containerColor = DarkCosmicCard),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.5.dp, Brush.horizontalGradient(GoldGradient))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(Brush.radialGradient(listOf(SaffronOrange.copy(alpha = 0.3f), Color.Transparent)), CircleShape)
                                        .border(1.5.dp, MysticGold, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("✦", color = MysticGold, fontSize = 20.sp)
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text("Unlock Your Daily Cosmic Insight", color = MysticGold, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                    Text("Precise Vedic calculations from your birth chart", color = TextSilver, fontSize = 11.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = BorderSubtle)
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Add your birth date, time, and location to calculate your authentic Vedic Lagna (Ascendant), Moon sign, Nakshatra, and personalised daily guidance.",
                                color = TextSilver,
                                fontSize = 13.sp,
                                lineHeight = 20.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onAddNewProfile,
                                colors = ButtonDefaults.buttonColors(containerColor = MysticGold, contentColor = DeepCosmic),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("today_create_profile_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Create Your Birth Chart", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                            }
                        }
                    }
                } else {
                    // Personalized Daily Insight Card
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).testTag("today_insight_card"),
                        colors = CardDefaults.cardColors(containerColor = DarkCosmicCard),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.5.dp, Brush.horizontalGradient(GoldGradient))
                    ) {
                        Column {
                            // Card header with gradient band
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.horizontalGradient(listOf(SacredSaffron.copy(alpha = 0.18f), DeepCosmic.copy(alpha = 0f))),
                                        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
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
                                                .size(40.dp)
                                                .background(Brush.radialGradient(listOf(SaffronOrange.copy(alpha = 0.4f), Color.Transparent)), CircleShape)
                                                .border(1.dp, MysticGold, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.WbSunny, contentDescription = null, tint = MysticGold, modifier = Modifier.size(20.dp))
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text("Today's Cosmic Blueprint", color = MysticGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                            Text(
                                                text = if (kundli != null) "☽ ${kundli.rashiSignName}  ·  Asc ${kundli.lagnaSignName}" else "Vedic Daily Reading",
                                                color = TextSilver,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                    Surface(
                                        color = AuspiciousGreen.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, AuspiciousGreen.copy(alpha = 0.4f))
                                    ) {
                                        Text("Vedic ✓", color = AuspiciousGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp))
                                    }
                                }
                            }

                            HorizontalDivider(color = BorderSubtle)

                            Column(modifier = Modifier.padding(16.dp)) {
                                if (isLoading) {
                                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            CircularProgressIndicator(color = MysticGold, modifier = Modifier.size(32.dp), strokeWidth = 2.dp)
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text("Calculating your cosmic blueprint…", color = TextMuted, fontSize = 11.sp)
                                        }
                                    }
                                } else {
                                    val readingContent = dailyInsightText
                                        ?: (guidance?.general ?: "Cosmic energies are supportive for intellectual endeavors and thoughtful reflection.")
                                    Text(
                                        text = readingContent.take(380) + if (readingContent.length > 380) "…" else "",
                                        color = TextSilver,
                                        fontSize = 13.5.sp,
                                        lineHeight = 21.sp
                                    )

                                    if (guidance != null) {
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            InsightHighlightChip("Lucky Color", guidance.luckyColor, PlanetJupiterColor, Modifier.weight(1f))
                                            InsightHighlightChip("Number", guidance.luckyNumber.toString(), PlanetSunColor, Modifier.weight(1f))
                                            InsightHighlightChip("Nakshatra", kundli?.nakshatra ?: "Vedic", CelestialViolet, Modifier.weight(1f))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))
                                    HorizontalDivider(color = BorderSubtle)
                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Action buttons
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = {
                                                val prompt = "Tell me more about my daily reading for today (${kundli?.rashiSignName ?: "my sign"}): $readingContent"
                                                onNavigateToAskAi(prompt)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = MysticGold, contentColor = DeepCosmic),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1.4f).testTag("today_ask_ai_button")
                                        ) {
                                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Ask AI", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                onSaveReading("Daily Insight - $todayFormatted", "Daily Horoscope", readingContent)
                                                isSavedToBookmarks = true
                                                Toast.makeText(context, "Saved to reading history", Toast.LENGTH_SHORT).show()
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, if (isSavedToBookmarks) MysticGold else BorderSubtle),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = if (isSavedToBookmarks) MysticGold else TextSilver),
                                            modifier = Modifier.weight(1f).testTag("today_save_reading_button")
                                        ) {
                                            Icon(
                                                imageVector = if (isSavedToBookmarks) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                                contentDescription = "Save",
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (isSavedToBookmarks) "Saved" else "Save", fontSize = 11.sp)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "text/plain"
                                                    putExtra(Intent.EXTRA_SUBJECT, "My Daily Vedic Blueprint • Astroveda AI")
                                                    putExtra(Intent.EXTRA_TEXT, "✨ Daily Vedic Insight for $todayFormatted (${kundli?.rashiSignName ?: "Vedic"}):\n\n$readingContent\n\n🪐 Astroveda AI")
                                                }
                                                context.startActivity(Intent.createChooser(shareIntent, "Share Daily Insight"))
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, BorderSubtle),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSilver),
                                            modifier = Modifier.weight(0.8f).testTag("today_share_button")
                                        ) {
                                            Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            // ── Compact Panchang Summary ──────────────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).testTag("today_panchang_card"),
                    colors = CardDefaults.cardColors(containerColor = DarkCosmicCard),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, BorderSubtle)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(36.dp)
                                        .background(DarkCosmicSurfaceVariant, RoundedCornerShape(10.dp))
                                        .border(1.dp, BorderGold, RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MysticGold, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Today's Panchang", color = MysticGold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    Text("${panchang.tithi} (${panchang.tithiPaksha}) · ${panchang.nakshatra}", color = TextSilver, fontSize = 11.sp)
                                }
                            }
                            TextButton(onClick = onNavigateToExplore) {
                                Text("Full →", color = MysticGold, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = BorderSubtle)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PanchangCompactItem("Sunrise", "🌅 ${panchang.sunrise}", modifier = Modifier.weight(1f))
                            PanchangCompactItem("Sunset", "🌇 ${panchang.sunset}", modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PanchangCompactItem("Abhijit ✦", panchang.abhijitMuhurat, highlight = true, highlightColor = PlanetMercuryColor, modifier = Modifier.weight(1f))
                            PanchangCompactItem("Rahu Kaal ✗", panchang.rahuKaal, highlight = true, highlightColor = SaffronOrange, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            // ── Daily Mantra Card ─────────────────────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).testTag("today_remedy_card"),
                    colors = CardDefaults.cardColors(containerColor = DarkCosmicSurfaceVariant),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, CelestialViolet.copy(alpha = 0.25f))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier.size(42.dp)
                                .background(CelestialViolet.copy(alpha = 0.15f), CircleShape)
                                .border(1.dp, CelestialViolet.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SelfImprovement, contentDescription = null, tint = CelestialViolet, modifier = Modifier.size(21.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Daily Vedic Focus & Mantra", color = CelestialViolet, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = guidance?.mantra ?: "Om Namah Shivaya · ॐ नमः शिवाय",
                                color = TextGold,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontStyle = FontStyle.Italic
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = guidance?.gemstoneRemedy ?: "Chant 108 times during sunrise for inner clarity.",
                                color = TextSilver,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // ── Trust Disclaimer ──────────────────────────────────────────
            item {
                Text(
                    text = "✦  Interpretive Vedic guidance powered by AI. Astrology provides spiritual insight and perspective, not certainty or a substitute for medical, financial, or relationship counsel.",
                    color = TextMuted,
                    fontSize = 10.sp,
                    lineHeight = 15.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 8.dp, bottom = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun InsightHighlightChip(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        color = color.copy(alpha = 0.08f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, color = TextMuted, fontSize = 9.sp)
            Spacer(modifier = Modifier.height(3.dp))
            Text(text = value, color = color, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
        }
    }
}

@Composable
private fun PanchangCompactItem(
    title: String,
    value: String,
    highlight: Boolean = false,
    highlightColor: Color = MysticGold,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (highlight) highlightColor.copy(alpha = 0.06f) else DarkCosmicSurface,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, if (highlight) highlightColor.copy(alpha = 0.4f) else BorderSubtle),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp)) {
            Text(text = title, color = if (highlight) highlightColor else TextMuted, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(3.dp))
            Text(text = value, color = if (highlight) highlightColor else TextGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}
