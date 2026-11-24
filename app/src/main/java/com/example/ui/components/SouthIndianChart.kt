package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astro.VedicEngine
import com.example.model.KundliChartData
import com.example.ui.theme.*

/**
 * Enhanced interactive South Indian (Fixed Zodiac Square) Birth Chart Generator using Jetpack Compose Canvas.
 * Fixed sign layout (Aries at Row 0 Col 1, Pisces at Row 0 Col 0, clockwise).
 * Clearly marks Lagna (Ascendant) with diagonal slash, highlights planets, retrogrades, and provides interactive touch-to-inspect.
 */
@Composable
fun SouthIndianChart(
    chartData: KundliChartData,
    modifier: Modifier = Modifier,
    onSignSelected: ((Int) -> Unit)? = null
) {
    var selectedSignId by remember { mutableIntStateOf(chartData.lagnaSignId) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("south_indian_chart_container")
            .background(DarkCosmicCard, RoundedCornerShape(16.dp))
            .border(1.dp, BorderGold, RoundedCornerShape(16.dp))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(NebulaTeal, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Rashi Kundli (South Indian)",
                        color = MysticGold,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Square format • Fixed signs clockwise • Tap sign to inspect",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            Surface(
                color = DarkCosmicSurfaceVariant,
                shape = RoundedCornerShape(8.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderGold))
            ) {
                Text(
                    text = "Moon: ${chartData.rashiSignName}",
                    color = PlanetMoonColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Canvas 4x4 Grid for South Indian Chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(DarkCosmicSurface, RoundedCornerShape(14.dp))
                .border(1.5.dp, BorderGold, RoundedCornerShape(14.dp))
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("south_indian_canvas")
                    .pointerInput(chartData) {
                        detectTapGestures { offset ->
                            val tappedSign = calculateSouthIndianSignFromPoint(
                                offset,
                                size.width.toFloat(),
                                size.height.toFloat()
                            )
                            if (tappedSign != null) {
                                selectedSignId = tappedSign
                                onSignSelected?.invoke(tappedSign)
                            }
                        }
                    }
            ) {
                val w = size.width
                val h = size.height
                val cellW = w / 4f
                val cellH = h / 4f
                val strokeW = 2.5f

                // Outer border
                drawRect(MysticGold, Offset(0f, 0f), size, style = Stroke(strokeW))

                // Grid lines for 4x4
                for (i in 1..3) {
                    // Vertical
                    drawLine(BorderGold, Offset(cellW * i, 0f), Offset(cellW * i, h), strokeW * 0.8f)
                    // Horizontal
                    drawLine(BorderGold, Offset(0f, cellH * i), Offset(w, cellH * i), strokeW * 0.8f)
                }

                // Central 2x2 hollow box with decorative background
                drawRect(
                    color = DarkCosmicSurfaceVariant,
                    topLeft = Offset(cellW, cellH),
                    size = Size(cellW * 2f, cellH * 2f)
                )

                // Draw decorative center emblem
                drawCenterDetails(this, chartData, cellW, cellH)

                // Highlight selected sign box
                highlightSouthIndianBox(this, selectedSignId, cellW, cellH)

                // Draw South Indian boxes with planets and sign names
                drawSouthIndianContent(this, chartData, cellW, cellH)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Selected Sign Details Inspector Panel
        val selectedSign = VedicEngine.zodiacSigns.firstOrNull { it.id == selectedSignId }
        val planetsInSelectedSign = chartData.planets.filter { it.signId == selectedSignId }
        val isLagna = selectedSignId == chartData.lagnaSignId

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("south_chart_sign_detail"),
            color = DarkCosmicSurface,
            shape = RoundedCornerShape(12.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = NebulaTeal.copy(alpha = 0.2f),
                            shape = CircleShape
                        ) {
                            Text(
                                text = selectedSign?.symbol ?: "♈",
                                color = NebulaTeal,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${selectedSign?.sanskritName ?: ""} (${selectedSign?.englishName ?: ""})" + if (isLagna) " • [ASC / Lagna]" else "",
                            color = MysticGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Lord: ${selectedSign?.lord ?: ""}",
                        color = TextSilver,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Element: ${selectedSign?.element ?: "Fire"} • Hindi: ${selectedSign?.hindiName ?: ""}",
                    color = TextMuted,
                    fontSize = 11.sp
                )

                if (planetsInSelectedSign.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = BorderSubtle)
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Planets in Sign:",
                        color = TextGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        planetsInSelectedSign.forEach { p ->
                            Surface(
                                color = DarkCosmicSurfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(getPlanetColor(p.id))
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${p.symbol} ${p.englishName}",
                                        color = getPlanetColor(p.id),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = String.format("%.1f°", p.degree) + if (p.isRetrograde) " (R)" else "",
                                        color = TextSilver,
                                        fontSize = 10.sp
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

// South Indian sign placement map (Fixed signs clockwise starting with Pisces at row 0, col 0)
private val southIndianSignCoords = mapOf(
    12 to (0 to 0), // Pisces (Meena)
    1 to (0 to 1),  // Aries (Mesha)
    2 to (0 to 2),  // Taurus (Vrishabha)
    3 to (0 to 3),  // Gemini (Mithuna)
    4 to (1 to 3),  // Cancer (Karka)
    5 to (2 to 3),  // Leo (Simha)
    6 to (3 to 3),  // Virgo (Kanya)
    7 to (3 to 2),  // Libra (Tula)
    8 to (3 to 1),  // Scorpio (Vrishchika)
    9 to (3 to 0),  // Sagittarius (Dhanu)
    10 to (2 to 0), // Capricorn (Makara)
    11 to (1 to 0)  // Aquarius (Kumbha)
)

private fun highlightSouthIndianBox(
    scope: DrawScope,
    signId: Int,
    cellW: Float,
    cellH: Float
) {
    val coord = southIndianSignCoords[signId] ?: return
    val (row, col) = coord
    scope.drawRect(
        color = SaffronOrange.copy(alpha = 0.2f),
        topLeft = Offset(col * cellW, row * cellH),
        size = Size(cellW, cellH)
    )
}

private fun drawCenterDetails(
    scope: DrawScope,
    chart: KundliChartData,
    cellW: Float,
    cellH: Float
) {
    val paintCenterName = android.graphics.Paint().apply {
        color = android.graphics.Color.parseColor("#FFF8E1")
        textSize = (cellW * 0.18f).coerceAtLeast(20f)
        textAlign = android.graphics.Paint.Align.CENTER
        isAntiAlias = true
        isFakeBoldText = true
    }

    val paintCenterDetail = android.graphics.Paint().apply {
        color = android.graphics.Color.parseColor("#FFD54F")
        textSize = (cellW * 0.14f).coerceAtLeast(16f)
        textAlign = android.graphics.Paint.Align.CENTER
        isAntiAlias = true
    }

    val paintCenterMuted = android.graphics.Paint().apply {
        color = android.graphics.Color.parseColor("#A59EC4")
        textSize = (cellW * 0.12f).coerceAtLeast(14f)
        textAlign = android.graphics.Paint.Align.CENTER
        isAntiAlias = true
    }

    scope.drawContext.canvas.nativeCanvas.apply {
        drawText(chart.personName, cellW * 2f, cellH * 1.80f, paintCenterName)
        drawText("Lagna: ${chart.lagnaSignName}", cellW * 2f, cellH * 2.05f, paintCenterDetail)
        drawText("Rashi: ${chart.rashiSignName} (${chart.nakshatra})", cellW * 2f, cellH * 2.25f, paintCenterMuted)
    }
}

private fun drawSouthIndianContent(
    scope: DrawScope,
    chart: KundliChartData,
    cellW: Float,
    cellH: Float
) {
    val paintSign = android.graphics.Paint().apply {
        color = android.graphics.Color.parseColor("#FFD54F")
        textSize = (cellW * 0.14f).coerceAtLeast(16f)
        textAlign = android.graphics.Paint.Align.CENTER
        isAntiAlias = true
        isFakeBoldText = true
    }

    val paintLagna = android.graphics.Paint().apply {
        color = android.graphics.Color.parseColor("#FF9800")
        textSize = (cellW * 0.13f).coerceAtLeast(15f)
        textAlign = android.graphics.Paint.Align.CENTER
        isAntiAlias = true
        isFakeBoldText = true
    }

    val paintPlanet = android.graphics.Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = (cellW * 0.13f).coerceAtLeast(15f)
        textAlign = android.graphics.Paint.Align.CENTER
        isAntiAlias = true
        isFakeBoldText = true
    }

    val paintPlanetRetro = android.graphics.Paint().apply {
        color = android.graphics.Color.parseColor("#FF8A80")
        textSize = (cellW * 0.12f).coerceAtLeast(14f)
        textAlign = android.graphics.Paint.Align.CENTER
        isAntiAlias = true
    }

    scope.drawContext.canvas.nativeCanvas.apply {
        for ((signId, coord) in southIndianSignCoords) {
            val (row, col) = coord
            val centerX = col * cellW + cellW / 2f
            val topY = row * cellH
            val isLagna = signId == chart.lagnaSignId
            val planetsInSign = chart.planets.filter { it.signId == signId }

            // 1. Draw Sign Name in corner
            val sign = VedicEngine.zodiacSigns.first { it.id == signId }
            val signLabel = sign.sanskritName.take(3)
            drawText(signLabel, centerX, topY + (cellH * 0.22f), paintSign)

            // If Lagna, draw diagonal top-left slash in that box
            if (isLagna) {
                scope.drawLine(
                    color = SaffronOrange,
                    start = Offset(col * cellW, row * cellH),
                    end = Offset(col * cellW + (cellW * 0.45f), row * cellH + (cellH * 0.45f)),
                    strokeWidth = 2.0f
                )
                drawText("[ASC]", centerX, topY + (cellH * 0.38f), paintLagna)
            }

            // 2. Draw Planets in this sign
            if (planetsInSign.isNotEmpty()) {
                val startY = if (isLagna) topY + (cellH * 0.54f) else topY + (cellH * 0.44f)
                val lineHeight = cellH * 0.16f

                planetsInSign.take(3).forEachIndexed { pIdx, planet ->
                    val planetStr = buildString {
                        append(planet.englishName.take(2))
                        if (planet.isRetrograde) append("(R)")
                    }
                    val currentPaint = if (planet.isRetrograde) paintPlanetRetro else paintPlanet
                    drawText(planetStr, centerX, startY + (pIdx * lineHeight), currentPaint)
                }

                if (planetsInSign.size > 3) {
                    drawText("+${planetsInSign.size - 3}", centerX, startY + (3 * lineHeight), paintPlanetRetro)
                }
            }
        }
    }
}

/**
 * Maps touch screen tap coordinates (X, Y) to the corresponding South Indian Sign ID (1 to 12)
 */
private fun calculateSouthIndianSignFromPoint(point: Offset, w: Float, h: Float): Int? {
    val cellW = w / 4f
    val cellH = h / 4f
    val col = (point.x / cellW).toInt().coerceIn(0, 3)
    val row = (point.y / cellH).toInt().coerceIn(0, 3)

    // Center 2x2 is empty
    if ((row == 1 || row == 2) && (col == 1 || col == 2)) {
        return null
    }

    return southIndianSignCoords.entries.firstOrNull { it.value == (row to col) }?.key
}

private fun getPlanetColor(id: String): Color {
    return when (id.lowercase()) {
        "sun" -> PlanetSunColor
        "moon" -> PlanetMoonColor
        "mars" -> PlanetMarsColor
        "mercury" -> PlanetMercuryColor
        "jupiter" -> PlanetJupiterColor
        "venus" -> PlanetVenusColor
        "saturn" -> PlanetSaturnColor
        "rahu" -> PlanetRahuColor
        "ketu" -> PlanetKetuColor
        else -> MysticGold
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0D0B1A)
@Composable
fun SouthIndianChartPreview() {
    MyApplicationTheme {
        val sampleKundli = VedicEngine.getOrDefaultKundli(
            name = "Aditya Sharma",
            birthDate = "1996-08-24",
            birthTime = "14:30",
            place = "New Delhi, India",
            isTimeApproximate = false
        )
        SouthIndianChart(chartData = sampleKundli)
    }
}
