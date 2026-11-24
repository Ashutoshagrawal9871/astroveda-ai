package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
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
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Chart styles supported by the visual Vedic Kundli component
 */
enum class KundliChartStyle {
    NORTH_INDIAN,
    SOUTH_INDIAN,
    EAST_INDIAN
}

/**
 * Premium visual representation component for Vedic Kundli birth charts.
 * Features:
 * - High-precision Canvas graphics with sacred geometric layouts
 * - North Indian (Diamond / Lagna centered), South Indian (Square / Fixed Signs), and East Indian formats
 * - Dynamic planetary glyphs, exact degrees, retrogrades, and dignity highlights (Exalted / Debilitated / Own)
 * - Interactive touch-to-inspect house calculation with animated cosmic highlighting
 * - Planetary Aspect Rays (Drishti overlay) vector paths
 * - Sanskrit house significations and planetary lords
 */
@Composable
fun VedicKundliVisualChart(
    chartData: KundliChartData,
    modifier: Modifier = Modifier,
    initialStyle: KundliChartStyle = KundliChartStyle.NORTH_INDIAN,
    onHouseSelected: ((Int) -> Unit)? = null
) {
    var chartStyle by remember { mutableStateOf(initialStyle) }
    var selectedHouse by remember { mutableIntStateOf(1) }
    var showDegrees by remember { mutableStateOf(true) }
    var showDrishti by remember { mutableStateOf(true) }
    var useDevanagariNumerals by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("vedic_kundli_visual_chart")
            .background(DarkCosmicCard, RoundedCornerShape(16.dp))
            .border(1.dp, BorderGold, RoundedCornerShape(16.dp))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Chart Header with Vedic Metadata & Style Switcher
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
                            .background(SacredSaffron, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (chartStyle) {
                            KundliChartStyle.NORTH_INDIAN -> "Lagna Kundli (North Indian)"
                            KundliChartStyle.SOUTH_INDIAN -> "Rashi Chakra (South Indian)"
                            KundliChartStyle.EAST_INDIAN -> "Surya Kundli (East Indian)"
                        },
                        color = MysticGold,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Lagna: ${chartData.lagnaSignName} • Moon: ${chartData.rashiSignName}",
                    color = TextSilver,
                    fontSize = 11.sp
                )
            }

            Surface(
                color = DarkCosmicSurfaceVariant,
                shape = RoundedCornerShape(8.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderGold))
            ) {
                Text(
                    text = "Nakshatra: ${chartData.nakshatra} (Pada ${chartData.nakshatraPada})",
                    color = MysticGoldBright,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Chart Format Selector & Display Options Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = chartStyle == KundliChartStyle.NORTH_INDIAN,
                onClick = { chartStyle = KundliChartStyle.NORTH_INDIAN },
                label = { Text("North (Diamond)", fontSize = 11.sp) },
                modifier = Modifier.weight(1f),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SacredSaffron.copy(alpha = 0.25f),
                    selectedLabelColor = MysticGold
                )
            )
            FilterChip(
                selected = chartStyle == KundliChartStyle.SOUTH_INDIAN,
                onClick = { chartStyle = KundliChartStyle.SOUTH_INDIAN },
                label = { Text("South (Square)", fontSize = 11.sp) },
                modifier = Modifier.weight(1f),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NebulaTeal.copy(alpha = 0.25f),
                    selectedLabelColor = MysticGold
                )
            )
            FilterChip(
                selected = chartStyle == KundliChartStyle.EAST_INDIAN,
                onClick = { chartStyle = KundliChartStyle.EAST_INDIAN },
                label = { Text("East (Chakra)", fontSize = 11.sp) },
                modifier = Modifier.weight(1f),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CelestialViolet.copy(alpha = 0.25f),
                    selectedLabelColor = MysticGold
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Toggle options row: Degrees, Drishti (Aspects), Numerals
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Degrees toggle
                FilterChip(
                    selected = showDegrees,
                    onClick = { showDegrees = !showDegrees },
                    label = { Text(if (showDegrees) "° Degrees ON" else "° Degrees OFF", fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DarkCosmicSurfaceVariant,
                        selectedLabelColor = MysticGold
                    )
                )

                // Drishti Aspect Rays toggle
                FilterChip(
                    selected = showDrishti,
                    onClick = { showDrishti = !showDrishti },
                    label = { Text(if (showDrishti) "👁 Aspects ON" else "👁 Aspects OFF", fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SacredSaffron.copy(alpha = 0.2f),
                        selectedLabelColor = SaffronOrange
                    )
                )
            }

            // Hindi / English numerals toggle
            TextButton(
                onClick = { useDevanagariNumerals = !useDevanagariNumerals },
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (useDevanagariNumerals) "१,२,३ (Devanagari)" else "1,2,3 (English)",
                    fontSize = 10.sp,
                    color = MysticGold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. High-Precision Vector Canvas Graphics
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
                    .testTag("vedic_kundli_canvas")
                    .pointerInput(chartData, chartStyle) {
                        detectTapGestures { offset ->
                            val tappedHouse = when (chartStyle) {
                                KundliChartStyle.NORTH_INDIAN -> calculateNorthIndianHouseFromPoint(offset, size.width.toFloat(), size.height.toFloat())
                                KundliChartStyle.SOUTH_INDIAN -> calculateSouthIndianHouseFromPoint(offset, size.width.toFloat(), size.height.toFloat(), chartData.lagnaSignId)
                                KundliChartStyle.EAST_INDIAN -> calculateEastIndianHouseFromPoint(offset, size.width.toFloat(), size.height.toFloat())
                            }
                            selectedHouse = tappedHouse
                            onHouseSelected?.invoke(tappedHouse)
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                when (chartStyle) {
                    KundliChartStyle.NORTH_INDIAN -> {
                        drawNorthIndianVisualChart(
                            scope = this,
                            chart = chartData,
                            w = w,
                            h = h,
                            selectedHouse = selectedHouse,
                            showDegrees = showDegrees,
                            showDrishti = showDrishti,
                            useDevanagari = useDevanagariNumerals
                        )
                    }
                    KundliChartStyle.SOUTH_INDIAN -> {
                        drawSouthIndianVisualChart(
                            scope = this,
                            chart = chartData,
                            w = w,
                            h = h,
                            selectedHouse = selectedHouse,
                            showDegrees = showDegrees,
                            showDrishti = showDrishti,
                            useDevanagari = useDevanagariNumerals
                        )
                    }
                    KundliChartStyle.EAST_INDIAN -> {
                        drawEastIndianVisualChart(
                            scope = this,
                            chart = chartData,
                            w = w,
                            h = h,
                            selectedHouse = selectedHouse,
                            showDegrees = showDegrees,
                            showDrishti = showDrishti,
                            useDevanagari = useDevanagariNumerals
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 4. Interactive House Details Inspector Panel
        val houseObj = chartData.houses.firstOrNull { it.number == selectedHouse }
        val planetsInSelectedHouse = chartData.planets.filter { it.house == selectedHouse }
        val signId = ((chartData.lagnaSignId + selectedHouse - 2) % 12) + 1
        val signObj = VedicEngine.zodiacSigns.firstOrNull { it.id == signId }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("visual_chart_house_detail"),
            color = DarkCosmicSurface,
            shape = RoundedCornerShape(12.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // House Title & Sanskrit Name
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = SaffronOrange.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Bhava $selectedHouse",
                                color = MysticGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = getSanskritBhavaName(selectedHouse),
                            color = TextGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Classification (Kendra / Trikona / Upachaya)
                    Surface(
                        color = DarkCosmicSurfaceVariant,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = getBhavaClassification(selectedHouse),
                            color = SacredSaffron,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Sign & Lord Details
                Text(
                    text = "Zodiac Sign: ${signObj?.englishName ?: "Aries"} (${signObj?.sanskritName ?: "Mesha"}) • Lord: ${signObj?.lord ?: "Mars"} • Element: ${signObj?.element ?: "Fire"}",
                    color = TextSilver,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Planets residing in this Bhava
                if (planetsInSelectedHouse.isNotEmpty()) {
                    Text(
                        text = "Planetary Placements (${planetsInSelectedHouse.size}):",
                        color = MysticGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        planetsInSelectedHouse.forEach { p ->
                            Surface(
                                color = DarkCosmicCard,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, getPlanetColor(p.id).copy(alpha = 0.6f)),
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = p.symbol, color = getPlanetColor(p.id), fontSize = 12.sp)
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = p.englishName.take(3),
                                            color = getPlanetColor(p.id),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (p.isRetrograde) {
                                            Text(text = " (R)", color = Color(0xFFFF5252), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Text(
                                        text = "${String.format("%.1f", p.degree)}° • ${p.dignity}",
                                        color = TextMuted,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        text = "No planets occupied. Pure influence from house lord ${signObj?.lord ?: "Jupiter"}.",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // House Significations
                Text(
                    text = "Significations: ${houseObj?.significations ?: getFallbackSignifications(selectedHouse)}",
                    color = TextSilver,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

// ==============================================================================
// 1. NORTH INDIAN (DIAMOND) CANVAS DRAWING WITH SVG PATHS & ASPECT RAYS
// ==============================================================================

private fun drawNorthIndianVisualChart(
    scope: DrawScope,
    chart: KundliChartData,
    w: Float,
    h: Float,
    selectedHouse: Int,
    showDegrees: Boolean,
    showDrishti: Boolean,
    useDevanagari: Boolean
) {
    val goldStroke = 2.5f
    val subtleStroke = 1.0f

    // 1. Outer boundary box
    scope.drawRect(
        color = MysticGold,
        topLeft = Offset(0f, 0f),
        size = Size(w, h),
        style = Stroke(goldStroke)
    )

    // 2. Corner Diagonals: (0,0) -> (w,h) and (w,0) -> (0,h)
    scope.drawLine(MysticGold, Offset(0f, 0f), Offset(w, h), goldStroke)
    scope.drawLine(MysticGold, Offset(w, 0f), Offset(0f, h), goldStroke)

    // 3. Central Diamond connecting midpoints: (w/2, 0) -> (0, h/2) -> (w/2, h) -> (w, h/2) -> close
    val centralDiamond = Path().apply {
        moveTo(w / 2f, 0f)
        lineTo(0f, h / 2f)
        lineTo(w / 2f, h)
        lineTo(w, h / 2f)
        close()
    }
    scope.drawPath(centralDiamond, color = MysticGold, style = Stroke(goldStroke))

    // 4. Inner Cross connecting corner triangles to form houses 2, 6, 8, 12
    scope.drawLine(MysticGold, Offset(w * 0.25f, h * 0.25f), Offset(w * 0.75f, h * 0.25f), subtleStroke)
    scope.drawLine(MysticGold, Offset(w * 0.75f, h * 0.25f), Offset(w * 0.75f, h * 0.75f), subtleStroke)
    scope.drawLine(MysticGold, Offset(w * 0.75f, h * 0.75f), Offset(w * 0.25f, h * 0.75f), subtleStroke)
    scope.drawLine(MysticGold, Offset(w * 0.25f, h * 0.75f), Offset(w * 0.25f, h * 0.25f), subtleStroke)

    // 5. Highlight Selected House
    highlightNorthHousePath(scope, selectedHouse, w, h)

    // Centroids for each of the 12 Bhavas
    val houseCenters = getNorthIndianCentroids(w, h)

    // 6. Draw Planetary Aspect Rays (Drishti Overlay)
    if (showDrishti) {
        drawNorthIndianDrishtiAspects(scope, chart, houseCenters, selectedHouse)
    }

    // 7. Draw House Sign Numbers and Planet Badges
    drawNorthIndianContent(scope, chart, w, h, houseCenters, showDegrees, useDevanagari)
}

private fun highlightNorthHousePath(scope: DrawScope, house: Int, w: Float, h: Float) {
    val highlightColor = SacredSaffron.copy(alpha = 0.22f)
    val path = Path()

    when (house) {
        1 -> { // Top Diamond (Lagna)
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

private fun getNorthIndianCentroids(w: Float, h: Float): List<Offset> {
    return listOf(
        Offset(w * 0.50f, h * 0.25f), // H1 (Top Diamond)
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
}

/**
 * Draws Vedic Drishti (Planetary Aspect Rays) as glowing curved SVG bezier paths
 */
private fun drawNorthIndianDrishtiAspects(
    scope: DrawScope,
    chart: KundliChartData,
    centers: List<Offset>,
    selectedHouse: Int
) {
    val activePlanets = chart.planets.filter { it.house == selectedHouse }
    if (activePlanets.isEmpty()) return

    activePlanets.forEach { planet ->
        val aspectTargets = getPlanetAspectHouses(planet.id, planet.house)
        val pColor = getPlanetColor(planet.id)
        val stroke = Stroke(
            width = 1.8f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
        )

        val src = centers[planet.house - 1]
        aspectTargets.forEach { targetHouse ->
            val dst = centers[targetHouse - 1]
            val path = Path().apply {
                moveTo(src.x, src.y)
                // Pull control point toward chart center for nice celestial curvature
                val ctrlX = (src.x + dst.x) * 0.5f * 0.6f + (centers[0].x) * 0.4f
                val ctrlY = (src.y + dst.y) * 0.5f * 0.6f + (centers[6].y * 0.5f) * 0.4f
                quadraticTo(ctrlX, ctrlY, dst.x, dst.y)
            }
            scope.drawPath(path, color = pColor.copy(alpha = 0.65f), style = stroke)

            // Small glowing aspect target dot
            scope.drawCircle(
                color = pColor,
                radius = 3.5f,
                center = dst
            )
        }
    }
}

private fun drawNorthIndianContent(
    scope: DrawScope,
    chart: KundliChartData,
    w: Float,
    h: Float,
    centers: List<Offset>,
    showDegrees: Boolean,
    useDevanagari: Boolean
) {
    val paintNumber = android.graphics.Paint().apply {
        color = android.graphics.Color.parseColor("#FFD54F")
        textSize = (w * 0.036f).coerceAtLeast(22f)
        textAlign = android.graphics.Paint.Align.CENTER
        isAntiAlias = true
        isFakeBoldText = true
    }

    val paintPlanet = android.graphics.Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = (w * 0.029f).coerceAtLeast(18f)
        textAlign = android.graphics.Paint.Align.CENTER
        isAntiAlias = true
        isFakeBoldText = true
    }

    val paintPlanetRetro = android.graphics.Paint().apply {
        color = android.graphics.Color.parseColor("#FF5252")
        textSize = (w * 0.027f).coerceAtLeast(16f)
        textAlign = android.graphics.Paint.Align.CENTER
        isAntiAlias = true
        isFakeBoldText = true
    }

    scope.drawContext.canvas.nativeCanvas.apply {
        centers.forEachIndexed { index, pos ->
            val houseNum = index + 1
            val signId = ((chart.lagnaSignId + houseNum - 2) % 12) + 1
            val planetsInHouse = chart.planets.filter { it.house == houseNum }

            // 1. Draw Sign Number
            val signStr = if (useDevanagari) toDevanagari(signId) else "$signId"
            val numY = if (houseNum in listOf(1, 4, 7, 10)) pos.y - (w * 0.052f) else pos.y - (w * 0.038f)
            drawText(signStr, pos.x, numY, paintNumber)

            // 2. Draw Planets inside this house
            if (planetsInHouse.isNotEmpty()) {
                val startY = pos.y + (w * 0.008f)
                val lineHeight = if (showDegrees) w * 0.046f else w * 0.036f

                planetsInHouse.take(3).forEachIndexed { pIdx, p ->
                    val planetLabel = buildString {
                        append(p.symbol)
                        append(" ")
                        append(p.englishName.take(2))
                        if (p.isRetrograde) append("(R)")
                        if (showDegrees) {
                            append(" ")
                            append(p.degree.toInt())
                            append("°")
                        }
                    }

                    val currentPaint = when {
                        p.isRetrograde -> paintPlanetRetro
                        p.dignity == "Exalted" -> android.graphics.Paint(paintPlanet).apply { color = android.graphics.Color.parseColor("#69F0AE") }
                        p.dignity == "Debilitated" -> android.graphics.Paint(paintPlanet).apply { color = android.graphics.Color.parseColor("#FFAB40") }
                        else -> paintPlanet
                    }

                    drawText(planetLabel, pos.x, startY + (pIdx * lineHeight), currentPaint)
                }

                if (planetsInHouse.size > 3) {
                    drawText("+${planetsInHouse.size - 3} more", pos.x, startY + (3 * lineHeight), paintPlanetRetro)
                }
            }
        }
    }
}

// ==============================================================================
// 2. SOUTH INDIAN (FIXED SIGNS SQUARE) CANVAS DRAWING
// ==============================================================================

private fun drawSouthIndianVisualChart(
    scope: DrawScope,
    chart: KundliChartData,
    w: Float,
    h: Float,
    selectedHouse: Int,
    showDegrees: Boolean,
    showDrishti: Boolean,
    useDevanagari: Boolean
) {
    val cellSize = w / 4f
    val goldStroke = 2.5f

    // 1. Draw 4x4 Grid Outer Border & Grid Lines
    scope.drawRect(
        color = MysticGold,
        topLeft = Offset(0f, 0f),
        size = Size(w, h),
        style = Stroke(goldStroke)
    )

    // Inner horizontal and vertical lines
    for (i in 1..3) {
        scope.drawLine(MysticGold, Offset(0f, i * cellSize), Offset(w, i * cellSize), 1.5f)
        scope.drawLine(MysticGold, Offset(i * cellSize, 0f), Offset(i * cellSize, h), 1.5f)
    }

    // Clear Center 2x2 Box
    scope.drawRect(
        color = DarkCosmicCard,
        topLeft = Offset(cellSize, cellSize),
        size = Size(cellSize * 2f, cellSize * 2f)
    )
    scope.drawRect(
        color = MysticGold,
        topLeft = Offset(cellSize, cellSize),
        size = Size(cellSize * 2f, cellSize * 2f),
        style = Stroke(1.5f)
    )

    // Perimeter Sign Map (Fixed Signs 1 to 12 in South Indian style)
    // Pisces (12) is (0,0), Aries (1) is (1,0), Taurus (2) is (2,0), Gemini (3) is (3,0)
    // Cancer (4) is (3,1), Leo (5) is (3,2), Virgo (6) is (3,3)
    // Libra (7) is (2,3), Scorpio (8) is (1,3), Sagittarius (9) is (0,3)
    // Capricorn (10) is (0,2), Aquarius (11) is (0,1)
    val southSignPositions = mapOf(
        12 to Pair(0, 0), 1 to Pair(1, 0), 2 to Pair(2, 0), 3 to Pair(3, 0),
        4 to Pair(3, 1), 5 to Pair(3, 2), 6 to Pair(3, 3),
        7 to Pair(2, 3), 8 to Pair(1, 3), 9 to Pair(0, 3),
        10 to Pair(0, 2), 11 to Pair(0, 1)
    )

    // Highlight Selected House in South Indian Chart
    val selectedSign = ((chart.lagnaSignId + selectedHouse - 2) % 12) + 1
    southSignPositions[selectedSign]?.let { (c, r) ->
        scope.drawRect(
            color = SacredSaffron.copy(alpha = 0.25f),
            topLeft = Offset(c * cellSize, r * cellSize),
            size = Size(cellSize, cellSize)
        )
    }

    // Mark Lagna with iconic diagonal slash
    southSignPositions[chart.lagnaSignId]?.let { (c, r) ->
        scope.drawLine(
            color = SacredSaffron,
            start = Offset(c * cellSize, (r + 1) * cellSize),
            end = Offset((c + 1) * cellSize, r * cellSize),
            strokeWidth = 2.5f
        )
    }

    // Center decorative text
    scope.drawContext.canvas.nativeCanvas.apply {
        val paintCenter = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#FFD54F")
            textSize = (w * 0.040f).coerceAtLeast(24f)
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
            isFakeBoldText = true
        }
        val paintSub = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#A59EC4")
            textSize = (w * 0.026f).coerceAtLeast(16f)
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
        drawText("RASHI KUNDLI", w * 0.5f, h * 0.48f, paintCenter)
        drawText("South Indian Format", w * 0.5f, h * 0.54f, paintSub)

        // Draw planets in each sign box
        val paintPlanet = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = (w * 0.027f).coerceAtLeast(16f)
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
            isFakeBoldText = true
        }

        val paintLagna = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#FF6F00")
            textSize = (w * 0.027f).coerceAtLeast(16f)
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
            isFakeBoldText = true
        }

        southSignPositions.forEach { (sign, coords) ->
            val cx = (coords.first + 0.5f) * cellSize
            val cy = (coords.second + 0.3f) * cellSize
            val planetsInSign = chart.planets.filter { it.signId == sign }
            val isLagna = (sign == chart.lagnaSignId)

            var yOffset = cy
            if (isLagna) {
                drawText("ASC / लग्न", cx, yOffset, paintLagna)
                yOffset += cellSize * 0.22f
            }

            planetsInSign.take(2).forEach { p ->
                val str = "${p.symbol} ${p.englishName.take(2)}${if (p.isRetrograde) "(R)" else ""}"
                drawText(str, cx, yOffset, paintPlanet)
                yOffset += cellSize * 0.22f
            }
        }
    }
}

// ==============================================================================
// 3. EAST INDIAN (BENGALI / ODIA CHAKRA) CANVAS DRAWING
// ==============================================================================

private fun drawEastIndianVisualChart(
    scope: DrawScope,
    chart: KundliChartData,
    w: Float,
    h: Float,
    selectedHouse: Int,
    showDegrees: Boolean,
    showDrishti: Boolean,
    useDevanagari: Boolean
) {
    val goldStroke = 2.5f

    // Outer boundary
    scope.drawRect(
        color = MysticGold,
        topLeft = Offset(0f, 0f),
        size = Size(w, h),
        style = Stroke(goldStroke)
    )

    // Diagonal crosses
    scope.drawLine(MysticGold, Offset(0f, 0f), Offset(w, h), 1.5f)
    scope.drawLine(MysticGold, Offset(w, 0f), Offset(0f, h), 1.5f)

    // Midpoint cross
    scope.drawLine(MysticGold, Offset(w * 0.5f, 0f), Offset(w * 0.5f, h), 1.5f)
    scope.drawLine(MysticGold, Offset(0f, h * 0.5f), Offset(w, h * 0.5f), 1.5f)

    // Central circular mandala
    scope.drawCircle(
        color = MysticGold,
        radius = w * 0.18f,
        center = Offset(w * 0.5f, h * 0.5f),
        style = Stroke(1.8f)
    )

    // Center Label
    scope.drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#FFD54F")
            textSize = (w * 0.035f).coerceAtLeast(20f)
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
            isFakeBoldText = true
        }
        val paintSub = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#A59EC4")
            textSize = (w * 0.024f).coerceAtLeast(14f)
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
        drawText("SURYA CHAKRA", w * 0.5f, h * 0.49f, paint)
        drawText("East Indian Style", w * 0.5f, h * 0.54f, paintSub)

        // Draw planets in 12 sectors
        val paintPlanet = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = (w * 0.026f).coerceAtLeast(15f)
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }

        val r = w * 0.36f
        for (i in 1..12) {
            val angle = Math.toRadians((i - 1) * 30.0 - 90.0)
            val px = (w * 0.5f + r * cos(angle)).toFloat()
            val py = (h * 0.5f + r * sin(angle)).toFloat()
            val planetsInHouse = chart.planets.filter { it.house == i }

            if (planetsInHouse.isNotEmpty()) {
                val pStr = planetsInHouse.joinToString(" ") { it.englishName.take(2) }
                drawText(pStr, px, py, paintPlanet)
            } else {
                drawText("H$i", px, py, paintPlanet)
            }
        }
    }
}

// ==============================================================================
// 4. TOUCH DETECTION & COORDINATE MAPPING
// ==============================================================================

private fun calculateNorthIndianHouseFromPoint(point: Offset, w: Float, h: Float): Int {
    val x = point.x
    val y = point.y
    val cx = w * 0.5f
    val cy = h * 0.5f

    val dx = abs(x - cx)
    val dy = abs(y - cy)

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
        x < cx && y < cy -> if (x < y) 3 else 2
        x < cx && y >= cy -> if (x < (h - y)) 5 else 6
        x >= cx && y >= cy -> if ((w - x) < (h - y)) 9 else 8
        else -> if ((w - x) < y) 11 else 12
    }
}

private fun calculateSouthIndianHouseFromPoint(point: Offset, w: Float, h: Float, lagnaSignId: Int): Int {
    val cell = w / 4f
    val col = (point.x / cell).toInt().coerceIn(0, 3)
    val row = (point.y / cell).toInt().coerceIn(0, 3)

    val signAtCell = when {
        row == 0 -> when (col) { 0 -> 12; 1 -> 1; 2 -> 2; else -> 3 }
        row == 1 -> if (col == 3) 4 else 11
        row == 2 -> if (col == 3) 5 else 10
        else -> when (col) { 3 -> 6; 2 -> 7; 1 -> 8; else -> 9 }
    }

    // Calculate house from sign:
    val house = ((signAtCell - lagnaSignId + 12) % 12) + 1
    return house
}

private fun calculateEastIndianHouseFromPoint(point: Offset, w: Float, h: Float): Int {
    val angle = Math.toDegrees(kotlin.math.atan2((point.y - h * 0.5f).toDouble(), (point.x - w * 0.5f).toDouble()))
    val normalized = (angle + 90.0 + 360.0) % 360.0
    val sector = (normalized / 30.0).toInt() + 1
    return sector.coerceIn(1, 12)
}

// ==============================================================================
// 5. VEDIC ASTROLOGICAL UTILITIES & RULES
// ==============================================================================

/**
 * Returns aspected houses for a planet based on authentic Parashari Drishti
 */
private fun getPlanetAspectHouses(planetId: String, currentHouse: Int): List<Int> {
    val aspects = mutableListOf<Int>()

    // All planets cast 7th aspect (opposite house)
    aspects.add(((currentHouse + 6 - 1) % 12) + 1)

    when (planetId.lowercase()) {
        "mars" -> {
            // Mars special aspects: 4th, 7th, 8th
            aspects.add(((currentHouse + 3 - 1) % 12) + 1)
            aspects.add(((currentHouse + 7 - 1) % 12) + 1)
        }
        "jupiter", "rahu", "ketu" -> {
            // Jupiter, Rahu, Ketu special aspects: 5th, 7th, 9th
            aspects.add(((currentHouse + 4 - 1) % 12) + 1)
            aspects.add(((currentHouse + 8 - 1) % 12) + 1)
        }
        "saturn" -> {
            // Saturn special aspects: 3rd, 7th, 10th
            aspects.add(((currentHouse + 2 - 1) % 12) + 1)
            aspects.add(((currentHouse + 9 - 1) % 12) + 1)
        }
    }
    return aspects.distinct()
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

private fun getSanskritBhavaName(house: Int): String {
    return when (house) {
        1 -> "Tanu Bhava (Self & Destiny)"
        2 -> "Dhana Bhava (Wealth & Speech)"
        3 -> "Sahaja Bhava (Courage & Siblings)"
        4 -> "Sukha Bhava (Mother & Comforts)"
        5 -> "Putra Bhava (Children & Intellect)"
        6 -> "Ari Bhava (Debts, Disease & Enemies)"
        7 -> "Yuvati Bhava (Spouse & Partnership)"
        8 -> "Randhra Bhava (Longevity & Transformation)"
        9 -> "Dharma Bhava (Fortune & Higher Wisdom)"
        10 -> "Karma Bhava (Career & Status)"
        11 -> "Labha Bhava (Gains & Social Network)"
        12 -> "Vyaya Bhava (Expenditure & Liberation)"
        else -> "Bhava $house"
    }
}

private fun getBhavaClassification(house: Int): String {
    return when (house) {
        1, 4, 7, 10 -> "Kendra (Pillar of Life)"
        5, 9 -> "Trikona (Auspicious Trine)"
        3, 6, 11 -> "Upachaya (House of Growth)"
        2, 8 -> "Maraka / Ayu (Longevity)"
        12 -> "Moksha (Liberation)"
        else -> "General House"
    }
}

private fun getFallbackSignifications(house: Int): String {
    return when (house) {
        1 -> "Physical body, vitality, disposition, personality, and life path."
        2 -> "Family wealth, speech eloquence, eyes, food habits, and accumulated savings."
        3 -> "Initiative, mental strength, communication, younger siblings, and short journeys."
        4 -> "Home tranquility, mother, properties, conveyances, and emotional peace."
        5 -> "Purva punya, speculative gains, education, mantras, and children."
        6 -> "Litigation, competitors, daily routines, healing, and overcoming obstacles."
        7 -> "Legal marriage, business alliances, public interactions, and trade."
        8 -> "Occult research, sudden unearned wealth, longevity, and deep transformation."
        9 -> "Pilgrimages, father, spiritual preceptor, good luck, and dharma."
        10 -> "Public recognition, profession, government favors, and leadership karma."
        11 -> "Fulfillment of desires, elder siblings, major income, and influential connections."
        12 -> "Foreign residence, meditation, sleep quality, charity, and spiritual detachment."
        else -> "Astrological significations"
    }
}

private fun toDevanagari(number: Int): String {
    val devanagariDigits = charArrayOf('०', '१', '२', '३', '४', '५', '६', '७', '८', '९')
    return number.toString().map { devanagariDigits[it - '0'] }.joinToString("")
}
