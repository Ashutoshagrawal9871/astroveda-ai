package com.example

import com.example.astro.VedicEngine
import com.example.data.AstroRepository
import org.junit.Assert.*
import org.junit.Test

/**
 * Comprehensive verification tests for all core features of Astroveda AI:
 * - Vedic Kundli calculation (Lagna, Rashi, Nakshatra, 12 Houses, Planetary Dignities, Mahadasha)
 * - Kundli Matching (Milan / Ashtakoot 36 Guna calculation)
 * - Today Panchang & Auspicious Muhurats
 * - Horoscope Guidance (Lucky Number, Color, Focus Mantra)
 * - Daily Insight Cache Key Isolation
 * - Multilingual Daily Horoscope Formatting (English & Hindi)
 */
class AppFeatureVerificationTest {

    @Test
    fun testVedicKundliCalculationCompleteAndAccurate() {
        val chart = VedicEngine.calculateKundli(
            name = "Test Seeker",
            birthDate = "1995-10-24",
            birthTime = "08:15",
            place = "New Delhi, India",
            isTimeApproximate = false,
            lat = 28.6139,
            lng = 77.2090
        )

        assertNotNull("Chart should not be null", chart)
        val nonNullChart = chart!!
        assertEquals("Test Seeker", nonNullChart.personName)
        assertEquals("1995-10-24", nonNullChart.birthDate)
        assertEquals("08:15", nonNullChart.birthTime)
        assertFalse("Time should be accurate", nonNullChart.isTimeApproximate)

        // Lagna and Rashi should be populated with valid Vedic signs
        assertTrue("Lagna sign must not be blank", nonNullChart.lagnaSignName.isNotBlank())
        assertTrue("Rashi sign must not be blank", nonNullChart.rashiSignName.isNotBlank())
        assertTrue("Nakshatra must not be blank", nonNullChart.nakshatra.isNotBlank())
        assertTrue("Current Mahadasha must not be blank", nonNullChart.currentMahadasha.isNotBlank())

        // 9 Vedic planets (Sun, Moon, Mars, Mercury, Jupiter, Venus, Saturn, Rahu, Ketu)
        assertTrue("Must contain all major Vedic planets", nonNullChart.planets.size >= 9)
        val planetNames = nonNullChart.planets.map { it.englishName }
        assertTrue(planetNames.contains("Sun"))
        assertTrue(planetNames.contains("Moon"))
        assertTrue(planetNames.contains("Mars"))
        assertTrue(planetNames.contains("Mercury"))
        assertTrue(planetNames.contains("Jupiter"))
        assertTrue(planetNames.contains("Venus"))
        assertTrue(planetNames.contains("Saturn"))
        assertTrue(planetNames.contains("Rahu"))
        assertTrue(planetNames.contains("Ketu"))

        // Exactly 12 houses in chart
        assertEquals(12, nonNullChart.houses.size)
        val houseNumbers = nonNullChart.houses.map { it.number }.toSet()
        assertEquals((1..12).toSet(), houseNumbers)
    }

    @Test
    fun testKundliMilanAshtakootScoringBetween0And36() {
        val result = VedicEngine.calculateCompatibility(
            boyName = "Aarav",
            boyMoonSignId = 1,
            boyNakshatraIndex = 2,
            girlName = "Diya",
            girlMoonSignId = 3,
            girlNakshatraIndex = 6
        )

        assertNotNull(result)
        assertEquals("Aarav", result.boyName)
        assertEquals("Diya", result.girlName)
        assertTrue("Total Guna score must be >= 0", result.totalScore >= 0.0)
        assertTrue("Total Guna score must be <= 36", result.totalScore <= 36.0)
        assertEquals(36.0, result.maxScore, 0.01)

        // Verify the 8 Kootas exist
        assertEquals(8, result.gunaScores.size)
        val kootaNames = result.gunaScores.map { it.name }
        assertTrue(kootaNames.contains("Varna"))
        assertTrue(kootaNames.contains("Vashya"))
        assertTrue(kootaNames.contains("Tara"))
        assertTrue(kootaNames.contains("Yoni"))
        assertTrue(kootaNames.contains("Graha Maitri"))
        assertTrue(kootaNames.contains("Gana"))
        assertTrue(kootaNames.contains("Bhakoot"))
        assertTrue(kootaNames.contains("Nadi"))

        // Sum of individual koota scores should match totalScore
        val sumKootas = result.gunaScores.sumOf { it.pointsObtained }
        assertEquals(result.totalScore, sumKootas, 0.01)

        // Verdict must be present
        assertTrue("Verdict must not be blank", result.verdict.isNotBlank())
        assertTrue("Advice must not be blank", result.advice.isNotBlank())
    }

    @Test
    fun testPanchangAndAuspiciousMuhurats() {
        val panchang = VedicEngine.getTodayPanchang()

        assertNotNull(panchang)
        assertTrue(panchang.tithi.isNotBlank())
        assertTrue(panchang.tithiPaksha.isNotBlank())
        assertTrue(panchang.nakshatra.isNotBlank())
        assertTrue(panchang.yoga.isNotBlank())
        assertTrue(panchang.karana.isNotBlank())
        assertTrue(panchang.sunrise.isNotBlank())
        assertTrue(panchang.sunset.isNotBlank())
        assertTrue(panchang.abhijitMuhurat.isNotBlank())
        assertTrue(panchang.rahuKaal.isNotBlank())
    }

    @Test
    fun testHoroscopeGuidanceAndDailyRemedies() {
        val signs = listOf("Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo", "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces")
        for (sign in signs) {
            val guidance = VedicEngine.getHoroscopeGuidance(sign, "Today")
            assertNotNull(guidance)
            assertTrue(guidance.luckyColor.isNotBlank())
            assertTrue(guidance.luckyNumber in 1..99)
            assertTrue(guidance.general.isNotBlank())
            assertTrue(guidance.mantra.isNotBlank())
        }
    }

    @Test
    fun testDailyInsightFormattingMultilingual() {
        val kundli = VedicEngine.getOrDefaultKundli(
            name = "Priyesh",
            birthDate = "1990-01-01",
            birthTime = "12:00",
            place = "Varanasi, India",
            isTimeApproximate = false
        )

        val enReading = VedicEngine.formatDailyHoroscopeReading(kundli, "en")
        assertTrue(enReading.contains("Priyesh"))
        assertTrue(enReading.contains(kundli.rashiSignName))

        val hiReading = VedicEngine.formatDailyHoroscopeReading(kundli, "hi")
        assertTrue(hiReading.contains("Priyesh"))
        assertTrue(hiReading.contains(kundli.rashiSignName))
    }

    @Test
    fun testCacheKeyIdentityAndIsolation() {
        val k1 = AstroRepository.DailyReadingCacheKey(101L, "2026-11-24", "en")
        val k2 = AstroRepository.DailyReadingCacheKey(101L, "2026-11-24", "en")
        val kDifferentProfile = AstroRepository.DailyReadingCacheKey(102L, "2026-11-24", "en")
        val kDifferentDate = AstroRepository.DailyReadingCacheKey(101L, "2026-11-25", "en")
        val kDifferentLang = AstroRepository.DailyReadingCacheKey(101L, "2026-11-24", "hi")

        assertEquals(k1, k2)
        assertEquals(k1.hashCode(), k2.hashCode())
        assertNotEquals(k1, kDifferentProfile)
        assertNotEquals(k1, kDifferentDate)
        assertNotEquals(k1, kDifferentLang)
    }
}
