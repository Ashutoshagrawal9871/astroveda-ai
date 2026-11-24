package com.example

import com.example.data.AstroRepository
import org.junit.Assert.*
import org.junit.Test

class TodayDailyReadingCacheTest {

    @Test
    fun testCacheKeyIdentityDistinctAcrossProfileDateAndLanguage() {
        val key1 = AstroRepository.DailyReadingCacheKey(1L, "2026-11-15", "en")
        val key2 = AstroRepository.DailyReadingCacheKey(1L, "2026-11-15", "en")
        val keyDifferentProfile = AstroRepository.DailyReadingCacheKey(2L, "2026-11-15", "en")
        val keyDifferentDate = AstroRepository.DailyReadingCacheKey(1L, "2026-11-16", "en")
        val keyDifferentLang = AstroRepository.DailyReadingCacheKey(1L, "2026-11-15", "hi")

        // Equivalence
        assertEquals(key1, key2)
        assertEquals(key1.hashCode(), key2.hashCode())

        // Isolation
        assertNotEquals(key1, keyDifferentProfile)
        assertNotEquals(key1, keyDifferentDate)
        assertNotEquals(key1, keyDifferentLang)
    }

    @Test
    fun testDailyInsightFormattingFallback() {
        val kundli = com.example.astro.VedicEngine.getOrDefaultKundli(
            name = "Aarav",
            birthDate = "1995-10-24",
            birthTime = "08:15",
            place = "New Delhi, India",
            isTimeApproximate = false
        )

        val readingEn = com.example.astro.VedicEngine.formatDailyHoroscopeReading(kundli, "en")
        val readingHi = com.example.astro.VedicEngine.formatDailyHoroscopeReading(kundli, "hi")

        assertNotNull(readingEn)
        assertTrue(readingEn.contains("Aarav"))
        assertTrue(readingEn.contains(kundli.rashiSignName))
        assertTrue(readingEn.contains("Career"))

        assertNotNull(readingHi)
        assertTrue(readingHi.contains("Aarav"))
        assertTrue(readingHi.contains(kundli.rashiSignName))
        assertTrue(readingHi.contains("करियर"))
    }

    @Test
    fun testTodayPanchangReturnsValidAuspiciousAndInauspiciousTimings() {
        val panchang = com.example.astro.VedicEngine.getTodayPanchang()

        assertNotNull(panchang.tithi)
        assertNotNull(panchang.nakshatra)
        assertNotNull(panchang.sunrise)
        assertNotNull(panchang.sunset)
        assertNotNull(panchang.abhijitMuhurat)
        assertNotNull(panchang.rahuKaal)

        assertTrue("Sunrise must not be blank", panchang.sunrise.isNotBlank())
        assertTrue("Abhijit Muhurat must not be blank", panchang.abhijitMuhurat.isNotBlank())
        assertTrue("Rahu Kaal must not be blank", panchang.rahuKaal.isNotBlank())
    }
}
