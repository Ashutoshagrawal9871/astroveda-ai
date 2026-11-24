package com.example.model

data class ZodiacSign(
    val id: Int, // 1 to 12
    val englishName: String,
    val sanskritName: String,
    val hindiName: String,
    val lord: String,
    val element: String,
    val symbol: String
)

data class NakshatraInfo(
    val id: Int, // 1 to 27
    val name: String,
    val hindiName: String,
    val lord: String,
    val deity: String,
    val gana: String, // Deva, Manushya, Rakshasa
    val yoni: String,
    val nadi: String, // Aadi, Madhya, Antya
    val vashya: String,
    val varna: String
)

data class PlanetPosition(
    val id: String,
    val englishName: String,
    val hindiName: String,
    val symbol: String,
    val signId: Int, // 1 to 12
    val signName: String,
    val house: Int, // 1 to 12
    val degree: Double, // 0.0 to 30.0
    val isRetrograde: Boolean = false,
    val dignity: String = "Neutral", // Exalted, Debilitated, Own Sign, Friendly, Neutral, Enemy
    val nakshatra: String = "",
    val pada: Int = 1
)

data class HouseInfo(
    val number: Int, // 1 to 12
    val signId: Int,
    val signName: String,
    val lord: String,
    val planets: List<String>,
    val significations: String
)

data class DashaPeriod(
    val planet: String,
    val planetHindi: String,
    val startYear: Int,
    val endYear: Int,
    val durationYears: Int,
    val isCurrent: Boolean = false,
    val currentAntardasha: String = ""
)

data class KundliChartData(
    val personName: String,
    val birthDate: String,
    val birthTime: String,
    val birthPlace: String,
    val isTimeApproximate: Boolean,
    val lagnaSignId: Int,
    val lagnaSignName: String,
    val rashiSignId: Int,
    val rashiSignName: String,
    val nakshatra: String,
    val nakshatraPada: Int,
    val nakshatraLord: String,
    val planets: List<PlanetPosition>,
    val houses: List<HouseInfo>,
    val dashas: List<DashaPeriod>,
    val currentMahadasha: String,
    val currentAntardasha: String,
    val manglikStatus: String,
    val accuracyNote: String = ""
)

data class PanchangData(
    val dateStr: String,
    val dayOfWeek: String,
    val hindiDay: String,
    val tithi: String,
    val tithiPaksha: String,
    val nakshatra: String,
    val yoga: String,
    val karana: String,
    val sunrise: String,
    val sunset: String,
    val moonrise: String,
    val rahuKaal: String,
    val abhijitMuhurat: String,
    val gulikaKaal: String,
    val yamaganda: String
)

data class MuhuratItem(
    val title: String,
    val hindiTitle: String,
    val category: String, // Marriage, Griha Pravesh, Vehicle, Business, General
    val date: String,
    val timing: String,
    val nakshatra: String,
    val tithi: String,
    val rating: String // Highly Auspicious, Good, Moderate
)

data class FestivalItem(
    val name: String,
    val hindiName: String,
    val date: String,
    val tithi: String,
    val description: String
)

data class GunaScore(
    val name: String,
    val hindiName: String,
    val pointsObtained: Double,
    val maxPoints: Double,
    val description: String,
    val status: String // Excellent, Good, Average, Defective
)

data class CompatibilityResult(
    val boyName: String,
    val girlName: String,
    val boyRashi: String,
    val girlRashi: String,
    val boyNakshatra: String,
    val girlNakshatra: String,
    val totalScore: Double,
    val maxScore: Double = 36.0,
    val percentage: Int,
    val gunaScores: List<GunaScore>,
    val boyManglik: Boolean,
    val girlManglik: Boolean,
    val manglikVerdict: String,
    val verdict: String,
    val advice: String
)

data class HoroscopeGuidance(
    val period: String, // Today, Week, Month, Year
    val sign: String,
    val general: String,
    val career: String,
    val love: String,
    val health: String,
    val luckyNumber: Int,
    val luckyColor: String,
    val gemstoneRemedy: String,
    val mantra: String
)
