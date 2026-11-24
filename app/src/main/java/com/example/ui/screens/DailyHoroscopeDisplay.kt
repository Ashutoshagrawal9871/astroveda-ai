package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.astro.VedicEngine
import com.example.data.AstroRepository
import com.example.data.UserProfileEntity
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * DailyHoroscopeDisplay composable:
 * Uses the AstroRepository to fetch and display a personalized daily horoscope reading
 * based on the user's saved birth details and Vedic astrological chart.
 */
@Composable
fun DailyHoroscopeDisplay(
    repository: AstroRepository,
    modifier: Modifier = Modifier,
    language: String = "en",
    onNavigateToEditBirthDetails: (() -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()

    // Observe saved primary user profile from repository
    val primaryProfile by repository.primaryProfile.collectAsStateWithLifecycle(initialValue = null)

    var horoscopeText by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var lastUpdatedDate by remember { mutableStateOf("") }
    var isSavedToBookmarks by remember { mutableStateOf(false) }

    val todayFormatted = remember {
        SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date())
    }

    // Function to trigger repository fetch
    fun loadPersonalizedHoroscope(forceRefresh: Boolean = false) {
        coroutineScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val result = repository.fetchPersonalizedDailyHoroscope(
                    profile = primaryProfile,
                    language = language,
                    forceRefresh = forceRefresh
                )
                horoscopeText = result
                lastUpdatedDate = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Unable to fetch today's horoscope. Please check connection."
            } finally {
                isLoading = false
            }
        }
    }

    // Auto-fetch when profile or language becomes available
    LaunchedEffect(primaryProfile?.id, language) {
        if (horoscopeText == null && !isLoading) {
            loadPersonalizedHoroscope(forceRefresh = false)
        }
    }

    // Compute preview chart details from saved profile
    val profile = primaryProfile
    val quickKundli = remember(profile?.birthDate, profile?.birthTime, profile?.birthPlace) {
        if (profile != null) {
            VedicEngine.calculateKundli(
                name = profile.name,
                birthDate = profile.birthDate,
                birthTime = profile.birthTime,
                place = profile.birthPlace,
                isTimeApproximate = profile.isTimeUnknown,
                lat = profile.lat,
                lng = profile.lng
            )
        } else null
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("daily_horoscope_screen")
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("horoscope_header_card"),
                colors = CardDefaults.cardColors(containerColor = DarkCosmicCard),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderGold))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(DarkCosmicSurfaceVariant, CircleShape)
                                    .border(1.dp, BorderGold, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Cosmic Horoscope",
                                    tint = MysticGold,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (language == "hi") "दैनिक व्यक्तिगत राशिफल" else "Personalized Daily Horoscope",
                                    color = MysticGold,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = todayFormatted,
                                    color = TextSilver,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Refresh Action Button
                        IconButton(
                            onClick = { loadPersonalizedHoroscope(forceRefresh = true) },
                            enabled = !isLoading,
                            modifier = Modifier.testTag("horoscope_refresh_button")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MysticGold,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh Horoscope",
                                    tint = MysticGold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = BorderSubtle)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Saved Profile Birth Details Summary
                    if (profile != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Birth Chart: ${profile.name}",
                                    color = TextGold,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${profile.birthDate} • ${profile.birthTime} • ${profile.birthPlace}",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                                if (quickKundli != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Lagna: ${quickKundli.lagnaSignName} | Moon Sign: ${quickKundli.rashiSignName} (${quickKundli.nakshatra})",
                                        color = SaffronOrange,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            if (onNavigateToEditBirthDetails != null) {
                                TextButton(
                                    onClick = onNavigateToEditBirthDetails,
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("edit_birth_details_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Profile",
                                        tint = MysticGold,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Edit",
                                        color = MysticGold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = SaffronOrange,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Using default astrological birth chart. Add your custom details in Kundli tab.",
                                color = TextSilver,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Astrological Vedic Pill Badges
        if (quickKundli != null) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HoroscopePill(
                        label = "Moon Sign",
                        value = quickKundli.rashiSignName,
                        tint = PlanetMoonColor,
                        modifier = Modifier.weight(1f)
                    )
                    HoroscopePill(
                        label = "Ascendant",
                        value = quickKundli.lagnaSignName,
                        tint = SaffronOrange,
                        modifier = Modifier.weight(1f)
                    )
                    HoroscopePill(
                        label = "Mahadasha",
                        value = quickKundli.currentMahadasha.substringBefore(" "),
                        tint = MysticGold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Main Horoscope Reading Card or Loading State
        item {
            when {
                isLoading && horoscopeText == null -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .testTag("horoscope_loading_card"),
                        colors = CardDefaults.cardColors(containerColor = DarkCosmicCard),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    color = MysticGold,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = if (language == "hi") "ग्रह नक्षत्रों और गोचर का विश्लेषण हो रहा है..." else "Aligning planetary transits & Vedic aspects...",
                                    color = MysticGold,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Consulting Gemini AI with your personalized birth details",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                errorMessage != null && horoscopeText == null -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCosmicCard),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = "Error",
                                tint = CrimsonAstro,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Unable to load horoscope",
                                color = MysticGold,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = errorMessage ?: "Unknown error",
                                color = TextSilver,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { loadPersonalizedHoroscope(forceRefresh = true) },
                                colors = ButtonDefaults.buttonColors(containerColor = MysticGold, contentColor = DeepCosmic),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Try Again")
                            }
                        }
                    }
                }

                horoscopeText != null -> {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("horoscope_reading_content_card"),
                            colors = CardDefaults.cardColors(containerColor = DarkCosmicCard),
                            shape = RoundedCornerShape(16.dp),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderGold.copy(alpha = 0.6f)))
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Stars,
                                            contentDescription = null,
                                            tint = MysticGold,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Cosmic Reading for You",
                                            color = MysticGold,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Bookmark button to save reading to AstroRepository
                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                repository.saveReading(
                                                    title = "Daily Horoscope - $todayFormatted",
                                                    category = "Daily Horoscope",
                                                    content = horoscopeText ?: ""
                                                )
                                                isSavedToBookmarks = true
                                            }
                                        },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .testTag("save_horoscope_reading_button")
                                    ) {
                                        Icon(
                                            imageVector = if (isSavedToBookmarks) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                            contentDescription = "Save Reading",
                                            tint = if (isSavedToBookmarks) SaffronOrange else TextSilver
                                        )
                                    }
                                }

                                if (lastUpdatedDate.isNotBlank()) {
                                    Text(
                                        text = "Calculated today at $lastUpdatedDate",
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                HorizontalDivider(color = BorderSubtle)
                                Spacer(modifier = Modifier.height(14.dp))

                                // Render parsed or structured horoscope reading text
                                Text(
                                    text = horoscopeText ?: "",
                                    color = TextGold,
                                    fontSize = 13.sp,
                                    lineHeight = 22.sp
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Quick astrological guidance summary items
                                val guidance = remember(quickKundli?.rashiSignName) {
                                    VedicEngine.getHoroscopeGuidance(
                                        rashiSign = quickKundli?.rashiSignName ?: "Mesha",
                                        period = "Today"
                                    )
                                }

                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = DarkCosmicSurface,
                                    shape = RoundedCornerShape(12.dp),
                                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "Vedic Energy Parameters",
                                            color = SaffronOrange,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Lucky Number: ${guidance.luckyNumber}",
                                                color = MysticGold,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = "Lucky Color: ${guidance.luckyColor}",
                                                color = NebulaTeal,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Chant: ${guidance.mantra}",
                                            color = TextSilver,
                                            fontSize = 11.sp
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
private fun HoroscopePill(
    label: String,
    value: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = DarkCosmicSurface,
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(tint.copy(alpha = 0.4f)))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                color = TextMuted,
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                color = tint,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0D0B1A)
@Composable
fun HoroscopePillPreview() {
    MyApplicationTheme {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HoroscopePill(label = "Moon Sign", value = "Vrishabha (Taurus)", tint = PlanetMoonColor)
            HoroscopePill(label = "Ascendant", value = "Mesha (Aries)", tint = SaffronOrange)
            HoroscopePill(label = "Mahadasha", value = "Jupiter", tint = MysticGold)
        }
    }
}
