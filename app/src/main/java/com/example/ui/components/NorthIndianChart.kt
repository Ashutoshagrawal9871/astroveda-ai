package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
import com.example.model.PlanetPosition
import com.example.ui.theme.*

/**
 * Enhanced interactive North Indian (Diamond / Kundli) Style Birth Chart Generator using Jetpack Compose Canvas.
 * Generates all 12 houses (Bhavas), signs, planet symbols, retrogrades, degrees, and enables tapping any house
 * to inspect full planetary details and house significations.
 */
@Composable
fun NorthIndianChart(
    chartData: KundliChartData,
    modifier: Modifier = Modifier,
    onHouseSelected: ((Int) -> Unit)? = null
) {
    var selectedHouse by remember { mutableIntStateOf(1) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("north_indian_chart_container")
            .background(DarkCosmicCard, RoundedCornerShape(16.dp))
            .border(1.dp, BorderGold, RoundedCornerShape(16.dp))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with Lagna and Rashi indicators
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
                            .background(MysticGold, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Lagna Kundli (North Indian)",
                        color = MysticGold,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Diamond format • Fixed 1st house at top • Tap house to inspect",
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
                    text = "Lagna: ${chartData.lagnaSignName}",
                    color = MysticGoldBright,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // High-Precision Canvas Drawing of North Indian Diamond Geometry
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
                    .testTag("north_indian_canvas")
                    .pointerInput(chartData) {
                        detectTapGestures { offset ->
                            val tappedHouse = calculateNorthIndianHouseFromPoint(offset, size.width.toFloat(), size.height.toFloat())
                            selectedHouse = tappedHouse
                            onHouseSelected?.invoke(tappedHouse)
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                drawNorthIndianGeometry(
                    chart = chartData,
                    w = w,
                    h = h,
                    selectedHouse = selectedHouse
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Selected House Inspector Details Panel
        val houseInfo = chartData.houses.firstOrNull { it.number == selectedHouse }
        val planetsInSelectedHouse = chartData.planets.filter { it.house == selectedHouse }
        val signNumber = ((chartData.lagnaSignId + selectedHouse - 2) % 12) + 1
        val signObj = VedicEngine.zodiacSigns.firstOrNull { it.id == signNumber }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("north_chart_house_detail"),
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
                            color = SaffronOrange.copy(alpha = 0.2f),
                            shape = CircleShape
                        ) {
                            Text(
                                text = "H$selectedHouse",
                                color = SaffronOrange,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "House $selectedHouse (${signObj?.sanskritName ?: ""} / ${signObj?.englishName ?: ""})",
                            color = MysticGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Lord: ${signObj?.lord ?: ""}",
                        color = TextSilver,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = houseInfo?.significations ?: "General house significations",
                    color = TextMuted,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                if (planetsInSelectedHouse.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = BorderSubtle)
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Planets in House:",
                        color = TextGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        planetsInSelectedHouse.forEach { p ->
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

/**
 * Canvas drawing logic for North Indian Kundli chart lines, backgrounds, sign numbers, and planets
 */
private fun DrawScope.drawNorthIndianGeometry(
    chart: KundliChartData,
    w: Float,
    h: Float,
    selectedHouse: Int
) {
    val goldStroke = 2.5f
    val subtleStroke = 1.0f

    // 1. Draw outer boundary box
    drawRect(
        color = MysticGold,
        topLeft = Offset(0f, 0f),
        size = size,
        style = Stroke(goldStroke)
    )

    // 2. Corner Diagonals: (0,0) -> (w,h) and (w,0) -> (0,h)
    drawLine(MysticGold, Offset(0f, 0f), Offset(w, h), goldStroke)
    drawLine(MysticGold, Offset(w, 0f), Offset(0f, h), goldStroke)

    // 3. Central Diamond connecting midpoints: (w/2, 0) -> (0, h/2) -> (w/2, h) -> (w, h/2) -> close
    val centralDiamond = Path().apply {
        moveTo(w / 2f, 0f)
        lineTo(0f, h / 2f)
        lineTo(w / 2f, h)
        lineTo(w, h / 2f)
        close()
    }
    drawPath(centralDiamond, color = MysticGold, style = Stroke(goldStroke))

    // 4. Inner Cross connecting corner triangles to form houses 2, 6, 8, 12:
    // Line from (0, h/2) to (w/2, 0) is diamond edge.
    // Inner square connecting mid-points of four quarters:
    drawLine(MysticGold, Offset(w * 0.25f, h * 0.25f), Offset(w * 0.75f, h * 0.25f), subtleStroke)
    drawLine(MysticGold, Offset(w * 0.75f, h * 0.25f), Offset(w * 0.75f, h * 0.75f), subtleStroke)
    drawLine(MysticGold, Offset(w * 0.75f, h * 0.75f), Offset(w * 0.25f, h * 0.75f), subtleStroke)
    drawLine(MysticGold, Offset(w * 0.25f, h * 0.75f), Offset(w * 0.25f, h * 0.25f), subtleStroke)

    // 5. Draw highlighted background for selected house
    highlightSelectedNorthHouse(this, selectedHouse, w, h)

    // 6. Draw Sign Numbers and Planets in each of the 12 Bhavas
    drawNorthIndianHouseContent(this, chart, w, h, selectedHouse)
}

private fun highlightSelectedNorthHouse(
    scope: DrawScope,
    house: Int,
    w: Float,
    h: Float
) {
    val highlightColor = SaffronOrange.copy(alpha = 0.18f)
    val path = Path()

    when (house) {
        1 -> { // Top Diamond
            path.moveTo(w * 0.5f, 0f)
            path.lineTo(w * 0.25f, h * 0.25f)
            path.lineTo(w * 0.5f, h * 0.5f)
            path.lineTo(w * 0.75f, h * 0.25f)
            path.close()
        }
        2 -> { // Top Left Outer Triangle
            path.moveTo(0f, 0f)
            path.lineTo(w * 0.5f, 0f)
            path.lineTo(w * 0.25f, h * 0.25f)
            path.close()
        }
        3 -> { // Left Upper Outer Triangle
            path.moveTo(0f, 0f)
            path.lineTo(0f, h * 0.5f)
            path.lineTo(w * 0.25f, h * 0.25f)
            path.close()
        }
        4 -> { // Left Diamond
            path.moveTo(0f, h * 0.5f)
            path.lineTo(w * 0.25f, h * 0.25f)
            path.lineTo(w * 0.5f, h * 0.5f)
            path.lineTo(w * 0.25f, h * 0.75f)
            path.close()
        }
        5 -> { // Left Lower Outer Triangle
            path.moveTo(0f, h * 0.5f)
            path.lineTo(0f, h)
            path.lineTo(w * 0.25f, h * 0.75f)
            path.close()
        }
        6 -> { // Bottom Left Outer Triangle
            path.moveTo(0f, h)
            path.lineTo(w * 0.5f, h)
            path.lineTo(w * 0.25f, h * 0.75f)
            path.close()
        }
        7 -> { // Bottom Diamond
            path.moveTo(w * 0.5f, h)
            path.lineTo(w * 0.25f, h * 0.75f)
            path.lineTo(w * 0.5f, h * 0.5f)
            path.lineTo(w * 0.75f, h * 0.75f)
            path.close()
        }
        8 -> { // Bottom Right Outer Triangle
            path.moveTo(w * 0.5f, h)
            path.lineTo(w, h)
            path.lineTo(w * 0.75f, h * 0.75f)
            path.close()
        }
        9 -> { // Right Lower Outer Triangle
            path.moveTo(w, h * 0.5f)
            path.lineTo(w, h)
            path.lineTo(w * 0.75f, h * 0.75f)
            path.close()
        }
        10 -> { // Right Diamond
            path.moveTo(w, h * 0.5f)
            path.lineTo(w * 0.75f, h * 0.25f)
            path.lineTo(w * 0.5f, h * 0.5f)
            path.lineTo(w * 0.75f, h * 0.75f)
            path.close()
        }
        11 -> { // Right Upper Outer Triangle
            path.moveTo(w, 0f)
            path.lineTo(w, h * 0.5f)
            path.lineTo(w * 0.75f, h * 0.25f)
            path.close()
        }
        12 -> { // Top Right Outer Triangle
            path.moveTo(w * 0.5f, 0f)
            path.lineTo(w, 0f)
            path.lineTo(w * 0.75f, h * 0.25f)
            path.close()
        }
    }
    scope.drawPath(path, color = highlightColor)
}

private fun drawNorthIndianHouseContent(
    scope: DrawScope,
    chart: KundliChartData,
    w: Float,
    h: Float,
    selectedHouse: Int
) {
    val paintNumber = android.graphics.Paint().apply {
        color = android.graphics.Color.parseColor("#FFD54F")
        textSize = (w * 0.038f).coerceAtLeast(24f)
        textAlign = android.graphics.Paint.Align.CENTER
        isAntiAlias = true
        isFakeBoldText = true
    }

    val paintPlanet = android.graphics.Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = (w * 0.030f).coerceAtLeast(18f)
        textAlign = android.graphics.Paint.Align.CENTER
        isAntiAlias = true
        isFakeBoldText = true
    }

    val paintPlanetRetro = android.graphics.Paint().apply {
        color = android.graphics.Color.parseColor("#FF8A80")
        textSize = (w * 0.026f).coerceAtLeast(16f)
        textAlign = android.graphics.Paint.Align.CENTER
        isAntiAlias = true
    }

    // Precise centroid anchors for each house in North Indian layout
    val houseCenters = listOf(
        Offset(w * 0.50f, h * 0.25f), // H1 (Top Diamond - Lagna Bhava)
        Offset(w * 0.25f, h * 0.12f), // H2 (Top-Left Triangle)
        Offset(w * 0.12f, h * 0.25f), // H3 (Left-Top Triangle)
        Offset(w * 0.25f, h * 0.50f), // H4 (Left Diamond)
        Offset(w * 0.12f, h * 0.75f), // H5 (Left-Bottom Triangle)
        Offset(w * 0.25f, h * 0.88f), // H6 (Bottom-Left Triangle)
        Offset(w * 0.50f, h * 0.75f), // H7 (Bottom Diamond)
        Offset(w * 0.75f, h * 0.88f), // H8 (Bottom-Right Triangle)
        Offset(w * 0.88f, h * 0.75f), // H9 (Right-Bottom Triangle)
        Offset(w * 0.75f, h * 0.50f), // H10 (Right Diamond)
        Offset(w * 0.88f, h * 0.25f), // H11 (Right-Top Triangle)
        Offset(w * 0.75f, h * 0.12f)  // H12 (Top-Right Triangle)
    )

    scope.drawContext.canvas.nativeCanvas.apply {
        houseCenters.forEachIndexed { index, pos ->
            val houseNum = index + 1
            // In North Indian chart, the rashi sign number inside house H:
            val signId = ((chart.lagnaSignId + houseNum - 2) % 12) + 1
            val planetsInHouse = chart.planets.filter { it.house == houseNum }

            // 1. Draw Rashi Sign Number at top/apex of the house
            val numY = if (houseNum in listOf(1, 4, 7, 10)) pos.y - (w * 0.05f) else pos.y - (w * 0.035f)
            drawText("$signId", pos.x, numY, paintNumber)

            // 2. Draw Planets with Symbol, Name abbreviation, and Retrograde indicator
            if (planetsInHouse.isNotEmpty()) {
                val startY = pos.y + (w * 0.01f)
                val lineHeight = w * 0.038f

                planetsInHouse.take(3).forEachIndexed { pIdx, planet ->
                    val planetStr = buildString {
                        append(planet.englishName.take(2))
                        if (planet.isRetrograde) append("(R)")
                    }
                    val currentPaint = if (planet.isRetrograde) paintPlanetRetro else paintPlanet
                    drawText(planetStr, pos.x, startY + (pIdx * lineHeight), currentPaint)
                }

                if (planetsInHouse.size > 3) {
                    drawText("+${planetsInHouse.size - 3}", pos.x, startY + (3 * lineHeight), paintPlanetRetro)
                }
            }
        }
    }
}

/**
 * Maps touch screen tap coordinates (X, Y) to the corresponding North Indian House (1 to 12)
 */
private fun calculateNorthIndianHouseFromPoint(point: Offset, w: Float, h: Float): Int {
    val x = point.x
    val y = point.y
    val cx = w * 0.5f
    val cy = h * 0.5f

    // Check inner diamond quadrants first
    val dx = kotlin.math.abs(x - cx)
    val dy = kotlin.math.abs(y - cy)

    // Diamond condition: dx / (w/2) + dy / (h/2) <= 1
    if ((dx / (w * 0.5f)) + (dy / (h * 0.5f)) <= 1f) {
        return when {
            y < cy && dx <= dy -> 1   // House 1 (Top diamond)
            x < cx && dx > dy -> 4   // House 4 (Left diamond)
            y >= cy && dx <= dy -> 7  // House 7 (Bottom diamond)
            else -> 10               // House 10 (Right diamond)
        }
    }

    // Outer triangles
    return when {
        x < cx && y < cy -> {
            if (x < y) 3 else 2 // H3 (Left-Top) or H2 (Top-Left)
        }
        x < cx && y >= cy -> {
            if (x < (h - y)) 5 else 6 // H5 (Left-Bottom) or H6 (Bottom-Left)
        }
        x >= cx && y >= cy -> {
            if ((w - x) < (h - y)) 9 else 8 // H9 (Right-Bottom) or H8 (Bottom-Right)
        }
        else -> {
            if ((w - x) < y) 11 else 12 // H11 (Right-Top) or H12 (Top-Right)
        }
    }
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
fun NorthIndianChartPreview() {
    MyApplicationTheme {
        val sampleKundli = VedicEngine.getOrDefaultKundli(
            name = "Aditya Sharma",
            birthDate = "1996-08-24",
            birthTime = "14:30",
            place = "New Delhi, India",
            isTimeApproximate = false
        )
        NorthIndianChart(chartData = sampleKundli)
    }
}
