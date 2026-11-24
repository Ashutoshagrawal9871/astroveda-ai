package com.example.astro

import com.example.model.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

object VedicEngine {

    val zodiacSigns = listOf(
        ZodiacSign(1, "Aries", "Mesha", "मेष", "Mars (Mangal)", "Fire", "♈"),
        ZodiacSign(2, "Taurus", "Vrishabha", "वृषभ", "Venus (Shukra)", "Earth", "♉"),
        ZodiacSign(3, "Gemini", "Mithuna", "मिथुन", "Mercury (Budha)", "Air", "♊"),
        ZodiacSign(4, "Cancer", "Karka", "कर्क", "Moon (Chandra)", "Water", "♋"),
        ZodiacSign(5, "Leo", "Simha", "सिंह", "Sun (Surya)", "Fire", "♌"),
        ZodiacSign(6, "Virgo", "Kanya", "कन्या", "Mercury (Budha)", "Earth", "♍"),
        ZodiacSign(7, "Libra", "Tula", "तुला", "Venus (Shukra)", "Air", "♎"),
        ZodiacSign(8, "Scorpio", "Vrishchika", "वृश्चिक", "Mars (Mangal)", "Water", "♏"),
        ZodiacSign(9, "Sagittarius", "Dhanu", "धनु", "Jupiter (Guru)", "Fire", "♐"),
        ZodiacSign(10, "Capricorn", "Makara", "मकर", "Saturn (Shani)", "Earth", "♑"),
        ZodiacSign(11, "Aquarius", "Kumbha", "कुंभ", "Saturn (Shani)", "Air", "♒"),
        ZodiacSign(12, "Pisces", "Meena", "मीन", "Jupiter (Guru)", "Water", "♓")
    )

    val nakshatras = listOf(
        NakshatraInfo(1, "Ashwini", "अश्विनी", "Ketu", "Ashwini Kumaras", "Deva", "Horse", "Aadi", "Chatushpada", "Kshatriya"),
        NakshatraInfo(2, "Bharani", "भरणी", "Venus", "Yama", "Manushya", "Elephant", "Madhya", "Chatushpada", "Kshatriya"),
        NakshatraInfo(3, "Krittika", "कृत्तिका", "Sun", "Agni", "Rakshasa", "Sheep", "Antya", "Chatushpada", "Brahmin"),
        NakshatraInfo(4, "Rohini", "रोहिणी", "Moon", "Brahma", "Manushya", "Serpent", "Antya", "Chatushpada", "Shudra"),
        NakshatraInfo(5, "Mrigashira", "मृगशिरा", "Mars", "Soma", "Deva", "Serpent", "Madhya", "Dwipada", "Servant"),
        NakshatraInfo(6, "Ardra", "आर्द्रा", "Rahu", "Rudra", "Manushya", "Dog", "Aadi", "Dwipada", "Brahmin"),
        NakshatraInfo(7, "Punarvasu", "पुनर्वसु", "Jupiter", "Aditi", "Deva", "Cat", "Aadi", "Dwipada", "Vaishya"),
        NakshatraInfo(8, "Pushya", "पुष्य", "Saturn", "Brihaspati", "Deva", "Goat", "Madhya", "Jalchar", "Kshatriya"),
        NakshatraInfo(9, "Ashlesha", "अश्लेषा", "Mercury", "Nagas", "Rakshasa", "Cat", "Antya", "Jalchar", "Brahmin"),
        NakshatraInfo(10, "Magha", "मघा", "Ketu", "Pitris", "Rakshasa", "Rat", "Antya", "Chatushpada", "Shudra"),
        NakshatraInfo(11, "Purva Phalguni", "पूर्वाफाल्गुनी", "Venus", "Bhaga", "Manushya", "Rat", "Madhya", "Dwipada", "Brahmin"),
        NakshatraInfo(12, "Uttara Phalguni", "उत्तराफाल्गुनी", "Sun", "Aryaman", "Manushya", "Cow", "Aadi", "Dwipada", "Kshatriya"),
        NakshatraInfo(13, "Hasta", "हस्त", "Moon", "Savitr", "Deva", "Buffalo", "Aadi", "Dwipada", "Vaishya"),
        NakshatraInfo(14, "Chitra", "चित्रा", "Mars", "Tvastar", "Rakshasa", "Tiger", "Madhya", "Dwipada", "Shudra"),
        NakshatraInfo(15, "Swati", "स्वाति", "Rahu", "Vayu", "Deva", "Buffalo", "Antya", "Dwipada", "Brahmin"),
        NakshatraInfo(16, "Vishakha", "विशाखा", "Jupiter", "Indra-Agni", "Rakshasa", "Tiger", "Antya", "Dwipada", "Brahmin"),
        NakshatraInfo(17, "Anuradha", "अनुराधा", "Saturn", "Mitra", "Deva", "Deer", "Madhya", "Keeta", "Shudra"),
        NakshatraInfo(18, "Jyeshtha", "ज्येष्ठा", "Mercury", "Indra", "Rakshasa", "Deer", "Aadi", "Keeta", "Brahmin"),
        NakshatraInfo(19, "Mula", "मूल", "Ketu", "Nirriti", "Rakshasa", "Dog", "Aadi", "Chatushpada", "Shudra"),
        NakshatraInfo(20, "Purva Ashadha", "पूर्वाषाढ़ा", "Venus", "Apas", "Manushya", "Monkey", "Madhya", "Dwipada", "Brahmin"),
        NakshatraInfo(21, "Uttara Ashadha", "उत्तराषाढ़ा", "Sun", "Vishvadevas", "Manushya", "Mongoose", "Antya", "Dwipada", "Kshatriya"),
        NakshatraInfo(22, "Shravana", "श्रवण", "Moon", "Vishnu", "Deva", "Monkey", "Antya", "Dwipada", "Vaishya"),
        NakshatraInfo(23, "Dhanishta", "धनिष्ठा", "Mars", "Vasus", "Rakshasa", "Lion", "Madhya", "Chatushpada", "Shudra"),
        NakshatraInfo(24, "Shatabhisha", "शतभिषा", "Rahu", "Varuna", "Rakshasa", "Horse", "Aadi", "Dwipada", "Brahmin"),
        NakshatraInfo(25, "Purva Bhadrapada", "पूर्वभाद्रपदा", "Jupiter", "Aja Ekapada", "Manushya", "Lion", "Aadi", "Dwipada", "Brahmin"),
        NakshatraInfo(26, "Uttara Bhadrapada", "उत्तरभाद्रपदा", "Saturn", "Ahirbudhnya", "Manushya", "Cow", "Madhya", "Jalchar", "Kshatriya"),
        NakshatraInfo(27, "Revati", "रेवती", "Mercury", "Pushan", "Deva", "Elephant", "Antya", "Jalchar", "Shudra")
    )

    private val houseSignifications = listOf(
        "Self, Physical Appearance, Personality, Vitality, Head & Brain (Tanu Bhava)",
        "Wealth, Family, Speech, Assets, Food habits, Face & Right Eye (Dhana Bhava)",
        "Siblings, Courage, Communication, Short journeys, Hands & Arms (Sahaja Bhava)",
        "Mother, Home, Land, Vehicles, Happiness, Chest & Heart (Sukha Bhava)",
        "Children, Intelligence, Creativity, Past life karma, Romance, Stomach (Putra Bhava)",
        "Enemies, Debts, Health/Diseases, Daily work, Service, Lower abdomen (Ari Bhava)",
        "Spouse, Marriage, Partnerships, Business relationships, Public image (Yuvati Bhava)",
        "Longevity, Secrets, Sudden gains/losses, Transformation, Occult (Randhra Bhava)",
        "Dharma, Fortune, Higher Wisdom, Father, Long pilgrimages (Dharma Bhava)",
        "Career, Profession, Status, Fame, Karma, Knees & Bones (Karma Bhava)",
        "Gains, Desires fulfillment, Social circle, Elder siblings (Labha Bhava)",
        "Expenditure, Foreign travel, Liberation (Moksha), Sleep, Feet (Vyaya Bhava)"
    )

    // Vimshottari order & years
    val dashaPlanetsOrder = listOf(
        "Ketu" to 7,
        "Venus" to 20,
        "Sun" to 6,
        "Moon" to 10,
        "Mars" to 7,
        "Rahu" to 18,
        "Jupiter" to 16,
        "Saturn" to 19,
        "Mercury" to 17
    )

    fun calculateKundli(
        name: String,
        birthDate: String, // YYYY-MM-DD
        birthTime: String, // HH:mm
        place: String,
        isTimeApproximate: Boolean,
        lat: Double = 28.6139,
        lng: Double = 77.2090
    ): KundliChartData? {
        if (birthDate.isBlank()) return null
        val cal = Calendar.getInstance()
        try {
            val dateParts = birthDate.split("-")
            if (dateParts.size != 3) return null
            val year = dateParts[0].toInt()
            val month = dateParts[1].toInt() - 1
            val day = dateParts[2].toInt()

            val timeParts = if (birthTime.contains(":")) birthTime.split(":") else listOf("12", "00")
            val hour = timeParts[0].toIntOrNull() ?: 12
            val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0

            cal.set(year, month, day, hour, minute)
        } catch (_: Exception) {
            return null
        }

        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
        val birthYear = cal.get(Calendar.YEAR)
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)

        // Lahiri Ayanamsa approximation for epoch: ~23.85° + (year - 2000) * 0.01397°
        val ayanamsa = 23.85 + (birthYear - 2000) * 0.01397

        fun normDeg(deg: Double): Double {
            val m = deg % 360.0
            return if (m < 0) m + 360.0 else m
        }

        // Approximate Ascendant (Lagna)
        val timeDecimal = hour + minute / 60.0
        val baseSolarLongitude = normDeg((dayOfYear - 80) * (360.0 / 365.25) - ayanamsa)
        val lagnaOffset = normDeg(timeDecimal * 15.0 + lng / 4.0)
        val lagnaLongitude = normDeg(baseSolarLongitude + lagnaOffset)

        val lagnaSignId = if (isTimeApproximate) {
            (normDeg(baseSolarLongitude / 30.0).toInt() % 12) + 1
        } else {
            (normDeg(lagnaLongitude / 30.0).toInt() % 12) + 1
        }
        val lagnaSign = zodiacSigns.first { it.id == lagnaSignId }

        // Approximate planetary longitudes (Sidereal Nirayana)
        val daysSinceJ2000 = (birthYear - 2000) * 365.25 + dayOfYear

        // Sun sidereal longitude
        val sunLong = normDeg(280.46 + 0.9856474 * daysSinceJ2000 - ayanamsa)
        // Moon sidereal longitude (approx 13.176° per day + birth hour fraction)
        val moonLong = normDeg(218.32 + 13.176396 * daysSinceJ2000 + (timeDecimal * 0.55) - ayanamsa)
        // Mars sidereal longitude (approx 0.524° per day)
        val marsLong = normDeg(355.43 + 0.524033 * daysSinceJ2000 - ayanamsa)
        // Mercury
        val mercuryLong = normDeg(sunLong + sin(daysSinceJ2000 * 0.07) * 18.0)
        // Jupiter (approx 0.083° per day)
        val jupiterLong = normDeg(34.35 + 0.083085 * daysSinceJ2000 - ayanamsa)
        // Venus
        val venusLong = normDeg(sunLong + cos(daysSinceJ2000 * 0.05) * 35.0)
        // Saturn (approx 0.033° per day)
        val saturnLong = normDeg(50.08 + 0.033444 * daysSinceJ2000 - ayanamsa)
        // Rahu (Mean retrograde 0.0529° per day backwards)
        val rahuLong = normDeg(125.0 - 0.05295 * daysSinceJ2000 - ayanamsa)
        val ketuLong = normDeg(rahuLong + 180.0)

        val rashiSignId = (normDeg(moonLong / 30.0).toInt() % 12) + 1
        val rashiSign = zodiacSigns.first { it.id == rashiSignId }

        // Moon Nakshatra (360° / 27 = 13°20' = 13.3333°)
        val nakshatraIndex = (normDeg(moonLong / (360.0 / 27.0)).toInt() % 27)
        val nakshatraObj = nakshatras[nakshatraIndex.coerceIn(0, 26)]
        val nakshatraDegree = normDeg(moonLong) % (360.0 / 27.0)
        val pada = (nakshatraDegree / (13.3333 / 4.0)).toInt().coerceIn(0, 3) + 1

        fun houseForSign(planetSignId: Int): Int {
            val h = (planetSignId - lagnaSignId + 12) % 12
            return if (h == 0) 12 else h
        }

        fun planetDignity(name: String, signId: Int): String {
            return when (name) {
                "Sun" -> when (signId) { 1 -> "Exalted"; 7 -> "Debilitated"; 5 -> "Own Sign"; 9, 12, 8 -> "Friendly"; else -> "Neutral" }
                "Moon" -> when (signId) { 2 -> "Exalted"; 8 -> "Debilitated"; 4 -> "Own Sign"; 1, 5, 9 -> "Friendly"; else -> "Neutral" }
                "Mars" -> when (signId) { 10 -> "Exalted"; 4 -> "Debilitated"; 1, 8 -> "Own Sign"; 5, 9, 12 -> "Friendly"; else -> "Neutral" }
                "Mercury" -> when (signId) { 6 -> "Exalted"; 12 -> "Debilitated"; 3 -> "Own Sign"; 2, 7, 11 -> "Friendly"; else -> "Neutral" }
                "Jupiter" -> when (signId) { 4 -> "Exalted"; 10 -> "Debilitated"; 9, 12 -> "Own Sign"; 1, 5, 8 -> "Friendly"; else -> "Neutral" }
                "Venus" -> when (signId) { 12 -> "Exalted"; 6 -> "Debilitated"; 2, 7 -> "Own Sign"; 3, 10, 11 -> "Friendly"; else -> "Neutral" }
                "Saturn" -> when (signId) { 7 -> "Exalted"; 1 -> "Debilitated"; 10, 11 -> "Own Sign"; 2, 3, 6 -> "Friendly"; else -> "Enemy" }
                "Rahu" -> when (signId) { 2, 3 -> "Exalted/Strong"; 8, 9 -> "Debilitated"; else -> "Neutral" }
                "Ketu" -> when (signId) { 8, 9 -> "Exalted/Strong"; 2, 3 -> "Debilitated"; else -> "Neutral" }
                else -> "Neutral"
            }
        }

        fun createPlanetPos(
            id: String,
            name: String,
            hindi: String,
            symbol: String,
            longitude: Double,
            isRetro: Boolean = false
        ): PlanetPosition {
            val signId = ((longitude / 30.0).toInt() % 12) + 1
            val sign = zodiacSigns.first { it.id == signId }
            val degInSign = longitude % 30.0
            val house = houseForSign(signId)
            val nakIndex = ((longitude / (360.0 / 27.0)).toInt() % 27)
            val pNak = nakshatras[nakIndex]
            val pPada = ((longitude % (360.0 / 27.0)) / (13.3333 / 4.0)).toInt() + 1
            val dignity = planetDignity(name, signId)

            return PlanetPosition(
                id = id,
                englishName = name,
                hindiName = hindi,
                symbol = symbol,
                signId = signId,
                signName = sign.sanskritName,
                house = house,
                degree = degInSign,
                isRetrograde = isRetro,
                dignity = dignity,
                nakshatra = pNak.name,
                pada = pPada
            )
        }

        val planets = listOf(
            createPlanetPos("sun", "Sun", "सूर्य", "☉", sunLong),
            createPlanetPos("moon", "Moon", "चन्द्र", "☽", moonLong),
            createPlanetPos("mars", "Mars", "मंगल", "♂", marsLong, (birthYear + dayOfYear) % 7 == 0),
            createPlanetPos("mercury", "Mercury", "बुध", "☿", mercuryLong, (birthYear + dayOfYear) % 5 == 0),
            createPlanetPos("jupiter", "Jupiter", "बृहस्पति (गुरु)", "♃", jupiterLong, (birthYear + dayOfYear) % 4 == 0),
            createPlanetPos("venus", "Venus", "शुक्र", "♀", venusLong),
            createPlanetPos("saturn", "Saturn", "शनि", "♄", saturnLong, (birthYear + dayOfYear) % 3 == 0),
            createPlanetPos("rahu", "Rahu", "राहु", "☊", rahuLong, true),
            createPlanetPos("ketu", "Ketu", "केतु", "☋", ketuLong, true)
        )

        // 12 Houses
        val houses = (1..12).map { houseNum ->
            val signId = ((lagnaSignId + houseNum - 2) % 12) + 1
            val sign = zodiacSigns.first { it.id == signId }
            val occupants = planets.filter { it.house == houseNum }.map { "${it.hindiName} (${it.symbol})" }
            HouseInfo(
                number = houseNum,
                signId = signId,
                signName = sign.sanskritName,
                lord = sign.lord,
                planets = occupants,
                significations = houseSignifications[houseNum - 1]
            )
        }

        // Vimshottari Dasha calculation
        // Find starting dasha planet from Moon Nakshatra Lord
        val nakLord = nakshatraObj.lord
        val startingIndex = dashaPlanetsOrder.indexOfFirst { it.first.equals(nakLord, ignoreCase = true) }.let {
            if (it >= 0) it else 0
        }
        val nakshatraFractionPassed = nakshatraDegree / 13.3333
        val firstDashaTotalYears = dashaPlanetsOrder[startingIndex].second
        val firstDashaBalanceYears = ((1.0 - nakshatraFractionPassed) * firstDashaTotalYears).roundToInt().coerceAtLeast(1)

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val dashaList = mutableListOf<DashaPeriod>()
        var runningYear = birthYear

        // First dasha with remaining balance
        val firstDashaLord = dashaPlanetsOrder[startingIndex].first
        val firstDashaEnd = runningYear + firstDashaBalanceYears
        dashaList.add(
            DashaPeriod(
                planet = firstDashaLord,
                planetHindi = getHindiPlanet(firstDashaLord),
                startYear = runningYear,
                endYear = firstDashaEnd,
                durationYears = firstDashaBalanceYears,
                isCurrent = currentYear in runningYear until firstDashaEnd,
                currentAntardasha = firstDashaLord
            )
        )
        runningYear = firstDashaEnd

        // Subsequent dashas in sequence
        for (i in 1..8) {
            val idx = (startingIndex + i) % dashaPlanetsOrder.size
            val (planet, duration) = dashaPlanetsOrder[idx]
            val endY = runningYear + duration
            val isCur = currentYear in runningYear until endY
            dashaList.add(
                DashaPeriod(
                    planet = planet,
                    planetHindi = getHindiPlanet(planet),
                    startYear = runningYear,
                    endYear = endY,
                    durationYears = duration,
                    isCurrent = isCur,
                    currentAntardasha = if (isCur) getAntardasha(planet, currentYear - runningYear, duration) else planet
                )
            )
            runningYear = endY
        }

        val currentDashaObj = dashaList.firstOrNull { it.isCurrent } ?: dashaList.first()
        val currentMahadasha = currentDashaObj.planet
        val currentAntardasha = currentDashaObj.currentAntardasha

        // Manglik check: Mars in 1st, 2nd, 4th, 7th, 8th, or 12th house
        val marsHouse = planets.first { it.id == "mars" }.house
        val isManglik = marsHouse in listOf(1, 2, 4, 7, 8, 12)
        val manglikStatus = if (isManglik) {
            "Manglik (Mars in ${marsHouse}th House)"
        } else {
            "Non-Manglik (Anshik / Safe)"
        }

        return KundliChartData(
            personName = name.ifBlank { "User" },
            birthDate = birthDate,
            birthTime = birthTime,
            birthPlace = place.ifBlank { "New Delhi, India" },
            isTimeApproximate = isTimeApproximate,
            lagnaSignId = lagnaSignId,
            lagnaSignName = "${lagnaSign.sanskritName} (${lagnaSign.englishName})",
            rashiSignId = rashiSignId,
            rashiSignName = "${rashiSign.sanskritName} (${rashiSign.englishName})",
            nakshatra = nakshatraObj.name,
            nakshatraPada = pada,
            nakshatraLord = nakshatraObj.lord,
            planets = planets,
            houses = houses,
            dashas = dashaList,
            currentMahadasha = currentMahadasha,
            currentAntardasha = currentAntardasha,
            manglikStatus = manglikStatus,
            accuracyNote = if (isTimeApproximate) {
                "Approximate Birth Time: Surya Lagna applied. Exact birth time is required for accurate Lagna house cusps and fine dasha timing."
            } else {
                "Accurate Birth Time: Vedic sidereal Lahiri Nirayana calculation."
            }
        )
    }

    fun getOrDefaultKundli(
        name: String,
        birthDate: String,
        birthTime: String,
        place: String,
        isTimeApproximate: Boolean,
        lat: Double = 28.6139,
        lng: Double = 77.2090
    ): KundliChartData {
        return calculateKundli(name, birthDate, birthTime, place, isTimeApproximate, lat, lng)
            ?: calculateKundli(
                name = name.ifBlank { "Astro Seek" },
                birthDate = "1998-05-15",
                birthTime = "14:30",
                place = "New Delhi, India",
                isTimeApproximate = false
            )!!
    }

    private fun getHindiPlanet(english: String): String = when (english.lowercase()) {
        "sun" -> "सूर्य"
        "moon" -> "चन्द्र"
        "mars" -> "मंगल"
        "mercury" -> "बुध"
        "jupiter" -> "बृहस्पति (गुरु)"
        "venus" -> "शुक्र"
        "saturn" -> "शनि"
        "rahu" -> "राहु"
        "ketu" -> "केतु"
        else -> english
    }

    private fun getAntardasha(mahaPlanet: String, yearsPassed: Int, totalMahaYears: Int): String {
        val fraction = yearsPassed.toDouble() / totalMahaYears.toDouble()
        val antardashaIdx = (fraction * 9.0).toInt().coerceIn(0, 8)
        val startIdx = dashaPlanetsOrder.indexOfFirst { it.first.equals(mahaPlanet, true) }.let { if (it < 0) 0 else it }
        val targetIdx = (startIdx + antardashaIdx) % dashaPlanetsOrder.size
        return dashaPlanetsOrder[targetIdx].first
    }

    // Ashtakoota 36 Guna Matching Algorithm
    fun calculateCompatibility(
        boyName: String,
        boyMoonSignId: Int,
        boyNakshatraIndex: Int,
        girlName: String,
        girlMoonSignId: Int,
        girlNakshatraIndex: Int
    ): CompatibilityResult {
        val boyNak = nakshatras[boyNakshatraIndex.coerceIn(0, 26)]
        val girlNak = nakshatras[girlNakshatraIndex.coerceIn(0, 26)]

        // 1. Varna (1 point): Spiritual compatibility & ego
        val varnaRanks = mapOf("Brahmin" to 4, "Kshatriya" to 3, "Vaishya" to 2, "Shudra" to 1, "Servant" to 1)
        val boyVarnaScore = varnaRanks[boyNak.varna] ?: 2
        val girlVarnaScore = varnaRanks[girlNak.varna] ?: 2
        val varnaPts = if (boyVarnaScore >= girlVarnaScore) 1.0 else 0.0

        // 2. Vashya (2 points): Mutual attraction & control
        val vashyaPts = if (boyNak.vashya == girlNak.vashya) 2.0 else 1.0

        // 3. Tara (3 points): Destiny, health & well-being
        val diff1 = ((boyNak.id - girlNak.id + 27) % 9)
        val diff2 = ((girlNak.id - boyNak.id + 27) % 9)
        val taraPts = when {
            diff1 % 2 != 0 && diff2 % 2 != 0 -> 3.0
            diff1 % 2 != 0 || diff2 % 2 != 0 -> 1.5
            else -> 0.0
        }

        // 4. Yoni (4 points): Biological affinity & physical harmony
        val yoniPts = if (boyNak.yoni == girlNak.yoni) 4.0 else 2.0

        // 5. Graha Maitri (5 points): Psychological harmony & friendship
        val rashiDiff = (boyMoonSignId - girlMoonSignId + 12) % 12
        val maitriPts = when (rashiDiff) {
            0, 4, 8 -> 5.0 // Trikona or same
            6, 10 -> 4.0
            1, 11 -> 3.0
            2, 9 -> 1.0
            else -> 0.5 // 6-8 Shadastak or 2-12
        }

        // 6. Gana (6 points): Temperament & behavioral harmony
        val ganaPts = when {
            boyNak.gana == girlNak.gana -> 6.0
            boyNak.gana == "Deva" && girlNak.gana == "Manushya" -> 5.0
            boyNak.gana == "Manushya" && girlNak.gana == "Deva" -> 5.0
            else -> 0.0 // Deva & Rakshasa clash or Manushya & Rakshasa clash
        }

        // 7. Bhakoot (7 points): Emotional bonding & prosperity
        val bhakootPts = when (rashiDiff) {
            0, 2, 4, 6, 8, 10 -> 7.0
            else -> 0.0
        }

        // 8. Nadi (8 points): Genetic health & future progeny
        val nadiPts = if (boyNak.nadi != girlNak.nadi) 8.0 else 0.0

        val totalPoints = varnaPts + vashyaPts + taraPts + yoniPts + maitriPts + ganaPts + bhakootPts + nadiPts
        val pct = ((totalPoints / 36.0) * 100).roundToInt()

        val gunas = listOf(
            GunaScore("Varna", "वर्ण", varnaPts, 1.0, "Spiritual compatibility & work ego", if (varnaPts == 1.0) "Excellent" else "Average"),
            GunaScore("Vashya", "वश्य", vashyaPts, 2.0, "Mutual attraction & affection control", if (vashyaPts >= 1.5) "Good" else "Average"),
            GunaScore("Tara", "तारा", taraPts, 3.0, "Destiny, luck, health & longevity", if (taraPts >= 2.0) "Good" else "Average"),
            GunaScore("Yoni", "योनि", yoniPts, 4.0, "Physical attraction & emotional compatibility", if (yoniPts >= 3.0) "Harmonious" else "Neutral"),
            GunaScore("Graha Maitri", "ग्रह मैत्री", maitriPts, 5.0, "Mental wavelength & planetary friendship", if (maitriPts >= 4.0) "Excellent" else "Moderate"),
            GunaScore("Gana", "गण", ganaPts, 6.0, "Temperament (${boyNak.gana} & ${girlNak.gana})", if (ganaPts >= 5.0) "Auspicious" else "Clash"),
            GunaScore("Bhakoot", "भकूट", bhakootPts, 7.0, "Family happiness & financial prosperity", if (bhakootPts > 0) "Auspicious" else "Bhakoot Dosha"),
            GunaScore("Nadi", "नाड़ी", nadiPts, 8.0, "Genetic health & progeny (${boyNak.nadi} & ${girlNak.nadi})", if (nadiPts > 0) "No Nadi Dosha" else "Nadi Dosha Present")
        )

        val boyManglik = (boyMoonSignId in listOf(1, 4, 7, 8, 12))
        val girlManglik = (girlMoonSignId in listOf(1, 4, 7, 8, 12))
        val manglikVerdict = when {
            boyManglik && girlManglik -> "Both partners have matching Manglik status; Dosha cancels out naturally (Parihara)."
            boyManglik && !girlManglik -> "Boy is Manglik; Kumbha Vivah or Mangal Shanti puja is recommended."
            !boyManglik && girlManglik -> "Girl is Manglik; Vishnu Vivah remedy is recommended before marriage."
            else -> "Neither partner is Manglik. Purely auspicious alignment."
        }

        val verdict = when {
            totalPoints >= 28.0 -> "Utkrisht (Excellent Match) - Highly Recommended!"
            totalPoints >= 21.0 -> "Madhyam (Good Match) - Auspicious with minor remedies."
            totalPoints >= 18.0 -> "Samanya (Acceptable Match) - Astrologer consultation advised."
            else -> "Varjya (Incompatible) - Severe doshas observed; marriage discouraged without remedies."
        }

        val advice = if (totalPoints >= 21.0) {
            "The Ashtakoota score reflects a harmonious mental, spiritual, and physical bond. Perform Gauri Shankar puja and Brihaspati chanting for long-lasting marital bliss."
        } else {
            "Low score primarily due to ${if (nadiPts == 0.0) "Nadi Dosha" else "Bhakoot/Gana differences"}. Consider consulting our AI Astrologer Devi Priya for specific customized remedies."
        }

        return CompatibilityResult(
            boyName = boyName,
            girlName = girlName,
            boyRashi = zodiacSigns.first { it.id == boyMoonSignId }.hindiName,
            girlRashi = zodiacSigns.first { it.id == girlMoonSignId }.hindiName,
            boyNakshatra = boyNak.name,
            girlNakshatra = girlNak.name,
            totalScore = totalPoints,
            percentage = pct,
            gunaScores = gunas,
            boyManglik = boyManglik,
            girlManglik = girlManglik,
            manglikVerdict = manglikVerdict,
            verdict = verdict,
            advice = advice
        )
    }

    // Panchang for today
    fun getTodayPanchang(): PanchangData {
        val now = Calendar.getInstance()
        val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
        val dateStr = sdf.format(now.time)
        val dayOfWeek = SimpleDateFormat("EEEE", Locale.ENGLISH).format(now.time)

        val dayNamesHindi = mapOf(
            "Sunday" to "रविवार (Ravivaar)",
            "Monday" to "सोमवार (Somvaar)",
            "Tuesday" to "मंगलवार (Mangalvaar)",
            "Wednesday" to "बुधवार (Budhvaar)",
            "Thursday" to "गुरुवार (Guruvaar)",
            "Friday" to "शुक्रवार (Shukravaar)",
            "Saturday" to "शनिवार (Shanivaar)"
        )

        val tithis = listOf(
            "Pratipada", "Dwitiya", "Tritiya", "Chaturthi", "Panchami",
            "Shasthi", "Saptami", "Ashtami", "Navami", "Dashami",
            "Ekadashi", "Dwadashi", "Trayodashi", "Chaturdashi", "Purnima",
            "Pratipada", "Dwitiya", "Tritiya", "Chaturthi", "Panchami",
            "Shasthi", "Saptami", "Ashtami", "Navami", "Dashami",
            "Ekadashi", "Dwadashi", "Trayodashi", "Chaturdashi", "Amavasya"
        )

        val dayOfYear = now.get(Calendar.DAY_OF_YEAR)
        val tithiIdx = (dayOfYear + 4) % tithis.size
        val paksha = if (tithiIdx < 15) "Shukla Paksha (Bright Fortnight)" else "Krishna Paksha (Dark Fortnight)"
        val nakIndex = (dayOfYear * 2) % 27
        val nak = nakshatras[nakIndex]

        val yogas = listOf(
            "Vishkambha", "Priti", "Ayushman", "Saubhagya", "Shobhana", "Atiganda", "Sukarma",
            "Dhriti", "Shula", "Ganda", "Vriddhi", "Dhruva", "Vyaghata", "Harshana", "Vajra",
            "Siddhi", "Vyatipata", "Variyan", "Parigha", "Shiva", "Siddha", "Sadhya", "Shubha",
            "Shukla", "Brahma", "Indra", "Vaidhriti"
        )
        val yoga = yogas[(dayOfYear + 7) % yogas.size]

        val karanas = listOf("Bava", "Balava", "Kaulava", "Taitila", "Gara", "Vanija", "Vishti (Bhadra)")
        val karana = karanas[(dayOfYear + 2) % karanas.size]

        // Dynamic timings based on day of week for Rahu Kaal
        val rahuKaalMap = mapOf(
            "Sunday" to "04:30 PM - 06:00 PM",
            "Monday" to "07:30 AM - 09:00 AM",
            "Tuesday" to "03:00 PM - 04:30 PM",
            "Wednesday" to "12:00 PM - 01:30 PM",
            "Thursday" to "01:30 PM - 03:00 PM",
            "Friday" to "10:30 AM - 12:00 PM",
            "Saturday" to "09:00 AM - 10:30 AM"
        )

        return PanchangData(
            dateStr = dateStr,
            dayOfWeek = dayOfWeek,
            hindiDay = dayNamesHindi[dayOfWeek] ?: dayOfWeek,
            tithi = tithis[tithiIdx],
            tithiPaksha = paksha,
            nakshatra = "${nak.name} (${nak.hindiName})",
            yoga = "$yoga Yoga",
            karana = "$karana Karana",
            sunrise = "06:12 AM",
            sunset = "06:28 PM",
            moonrise = "07:45 PM",
            rahuKaal = rahuKaalMap[dayOfWeek] ?: "01:30 PM - 03:00 PM",
            abhijitMuhurat = "11:58 AM - 12:46 PM",
            gulikaKaal = "09:15 AM - 10:45 AM",
            yamaganda = "06:12 AM - 07:42 AM"
        )
    }

    // Hindu Festivals
    val upcomingFestivals = listOf(
        FestivalItem("Diwali (Deepawali)", "दीपावली", "Kartik Amavasya", "Amavasya", "Festival of lights, celebration of Lord Rama's return to Ayodhya and Goddess Lakshmi worship."),
        FestivalItem("Maha Shivratri", "महाशिवरात्रि", "Phalguna Krishna Chaturdashi", "Chaturdashi", "The great night of Shiva celebrating the cosmic dance of creation and preservation."),
        FestivalItem("Holi", "होली", "Phalguna Purnima", "Purnima", "Festival of colors signifying victory of good over evil (Prahlada) and arrival of spring."),
        FestivalItem("Navratri & Dussehra", "नवरात्रि एवं दशहरा", "Ashwin Shukla Pratipada", "Navami-Dashami", "Nine sacred nights worshipping Goddess Durga followed by Vijayadashami."),
        FestivalItem("Krishna Janmashtami", "श्री कृष्ण जन्माष्टमी", "Bhadrapada Krishna Ashtami", "Ashtami", "Celebration of the birth of Lord Krishna in Rohini Nakshatra."),
        FestivalItem("Karwa Chauth", "करवा चौथ", "Kartik Krishna Chaturthi", "Chaturthi", "Traditional fasting for marital harmony, health, and longevity of the spouse."),
        FestivalItem("Ganesh Chaturthi", "गणेश चतुर्थी", "Bhadrapada Shukla Chaturthi", "Chaturthi", "Arrival of Lord Ganesha from Kailash Parvat with great devotion and Modaks.")
    )

    // Auspicious Muhurats
    val auspiciousMuhurats = listOf(
        MuhuratItem("Vivah Muhurat (Wedding)", "विवाह शुभ मुहूर्त", "Marriage", "Auspicious Date & Time", "07:15 PM - 02:30 AM", "Rohini / Uttara Phalguni", "Shukla Saptami", "Highly Auspicious"),
        MuhuratItem("Griha Pravesh (Housewarming)", "गृह प्रवेश मुहूर्त", "Griha Pravesh", "Auspicious Date & Time", "06:30 AM - 10:15 AM", "Pushya / Anuradha", "Shukla Dashami", "Highly Auspicious"),
        MuhuratItem("Vehicle Purchase (Vahan)", "वाहन क्रय मुहूर्त", "Vehicle", "Auspicious Date & Time", "02:00 PM - 05:45 PM", "Swati / Shravana", "Ekadashi", "Good"),
        MuhuratItem("Property & Land Registry", "भूमि एवं संपत्ति क्रय", "Property", "Auspicious Date & Time", "11:20 AM - 01:10 PM", "Hasta / Chitra", "Trayodashi", "Good"),
        MuhuratItem("New Business Opening", "व्यापार एवं प्रतिष्ठान शुभारंभ", "Business", "Auspicious Date & Time", "09:45 AM - 12:15 PM", "Ashwini / Pushya", "Panchami", "Highly Auspicious")
    )

    fun getHoroscopeGuidance(rashiSign: String, period: String): HoroscopeGuidance {
        return HoroscopeGuidance(
            period = period,
            sign = rashiSign,
            general = "Favorable planetary transits bring intellectual clarity and cosmic support. The alignment of Jupiter encourages spiritual discipline and auspicious undertakings.",
            career = "Strong momentum in professional ventures. Colleagues will seek your expertise. A favorable time for leadership negotiations.",
            love = "Warm and affectionate communication strengthens relationship bonds. Sincere dialogue resolves past misunderstandings.",
            health = "Maintain sattvic nutrition and mindful hydration. Gentle morning pranayama will balance bodily doshas.",
            luckyNumber = (rashiSign.hashCode().absoluteValue % 9) + 1,
            luckyColor = listOf("Golden Yellow", "Royal Blue", "Saffron Crimson", "Emerald Green")[(rashiSign.hashCode().absoluteValue % 4)],
            gemstoneRemedy = "Yellow Sapphire (Pukhraj) or Pearl (Moti) worn after purification.",
            mantra = "Om Namah Shivaya • ॐ नमः शिवाय (108 repetitions)"
        )
    }

    fun formatDailyHoroscopeReading(kundli: KundliChartData, language: String = "en"): String {
        val guidance = getHoroscopeGuidance(kundli.rashiSignName, "Today")
        val person = kundli.personName.ifBlank { "Seeker" }

        return if (language == "hi") {
            """
            🌟 $person के लिए आज का व्यक्तिगत वैदिक राशिफल (${kundli.rashiSignName})
            लग्न: ${kundli.lagnaSignName} • नक्षत्र: ${kundli.nakshatra} (चरण ${kundli.nakshatraPada}) • वर्तमान महादशा: ${kundli.currentMahadasha}

            🌌 आज का गोचर एवं ग्रह प्रभाव:
            ${guidance.general}

            💼 करियर एवं आर्थिक स्थिति:
            ${guidance.career}

            ❤️ प्रेम, संबंध एवं पारिवारिक सुख:
            ${guidance.love}

            🌿 स्वास्थ्य एवं जीवन ऊर्जा:
            ${guidance.health}

            ✨ आज के शुभ संकेत एवं अचूक वैदिक उपाय:
            • शुभ अंक: ${guidance.luckyNumber}
            • शुभ रंग: ${guidance.luckyColor}
            • अनुकूल रत्न परामर्श: ${guidance.gemstoneRemedy}
            • दैनिक कल्याणकारी मंत्र: ${guidance.mantra}
            """.trimIndent()
        } else {
            """
            🌟 Personalized Daily Vedic Horoscope for $person (${kundli.rashiSignName})
            Ascendant: ${kundli.lagnaSignName} • Nakshatra: ${kundli.nakshatra} (Pada ${kundli.nakshatraPada}) • Mahadasha: ${kundli.currentMahadasha}

            🌌 Cosmic Weather & Planetary Transits:
            ${guidance.general}

            💼 Career & Financial Trajectory:
            ${guidance.career}

            ❤️ Love, Relationships & Harmony:
            ${guidance.love}

            🌿 Health, Vitality & Wellness:
            ${guidance.health}

            ✨ Daily Auspicious Alignments & Vedic Remedies:
            • Lucky Number: ${guidance.luckyNumber}
            • Lucky Color: ${guidance.luckyColor}
            • Recommended Gemstone: ${guidance.gemstoneRemedy}
            • Auspicious Mantra: ${guidance.mantra}
            """.trimIndent()
        }
    }

    fun generateVedicGuidance(
        userMessage: String,
        kundli: KundliChartData?,
        language: String = "en"
    ): String {
        val msg = userMessage.lowercase()
        val isHi = language == "hi"
        val person = kundli?.personName?.ifBlank { if (isHi) "जातक" else "Seeker" } ?: (if (isHi) "जातक" else "Seeker")
        val lagna = kundli?.lagnaSignName ?: (if (isHi) "मेष लग्न" else "Mesha Lagna")
        val rashi = kundli?.rashiSignName ?: (if (isHi) "वृषभ राशि" else "Vrishabha Rashi")
        val nakshatra = kundli?.nakshatra ?: "Rohini"
        val dasha = kundli?.currentMahadasha ?: "Jupiter"
        val antardasha = kundli?.currentAntardasha ?: "Venus"
        val manglik = kundli?.manglikStatus ?: "Non-Manglik"

        val house7 = kundli?.houses?.firstOrNull { it.number == 7 }
        val house10 = kundli?.houses?.firstOrNull { it.number == 10 }
        val house2 = kundli?.houses?.firstOrNull { it.number == 2 }
        val house11 = kundli?.houses?.firstOrNull { it.number == 11 }
        val house6 = kundli?.houses?.firstOrNull { it.number == 6 }

        return when {
            msg.contains("marriage") || msg.contains("spouse") || msg.contains("soulmate") || msg.contains("vivah") || msg.contains("विवाह") || msg.contains("शादी") -> {
                if (isHi) {
                    """
                    नमस्ते $person! आपके जन्म चक्र के अनुसार 7वें भाव (कलत्र एवं विवाह भाव) का विश्लेषण:

                    💍 वैवाहिक एवं संबंध योग:
                    • 7वां भाव राशि: ${house7?.signName ?: "तुला"} (भाव स्वामी: ${house7?.lord ?: "शुक्र"})
                    • भाव में स्थित ग्रह: ${house7?.planets?.joinToString(", ")?.ifEmpty { "सौम्य शुभ दृष्टि" } ?: "शुभ दृष्टि"}
                    • मांगलिक स्थिति: $manglik
                    • वर्तमान महादशा: $dasha एवं अंतर्दशा: $antardasha

                    🔮 फलित विश्लेषण:
                    सप्तमेश की अनुकूल स्थिति एवं बृहस्पति की शुभ दृष्टि एक सुसंस्कृत, बौद्धिक और समर्पित जीवनसाथी का संकेत देती है। वर्तमान $dasha महादशा संबंध को परिपक्वता और स्थायित्व प्रदान कर रही है।

                    🕉️ सरल वैदिक उपाय:
                    1. शुक्रवार को मां महालक्ष्मी अथवा गौरी-शंकर का पूजन करें।
                    2. ॐ शुं शुक्राय नमः का 108 बार जप करें।
                    3. किसी कन्या अथवा मंदिर में सफेद मिष्ठान का दान करें।
                    """.trimIndent()
                } else {
                    """
                    Namaste $person! Here is your Vedic marital & relationship analysis for the 7th House (Yuvati Bhava):

                    💍 Marriage & Soulmate Alignment:
                    • 7th House Sign: ${house7?.signName ?: "Tula (Libra)"} (Lord: ${house7?.lord ?: "Venus (Shukra)"})
                    • Occupying / Aspecting Planets: ${house7?.planets?.joinToString(", ")?.ifEmpty { "Auspicious benefic aspect" } ?: "Benefic aspect"}
                    • Manglik Status: $manglik
                    • Current Planetary Period: $dasha Mahadasha • $antardasha Antardasha

                    🔮 Interpretive Insight:
                    The lord of your 7th house and Venus indicate an empathetic, intellectually compatible partner who values harmony. Your current $dasha period activates positive relationship growth.

                    🕉️ Auspicious Vedic Remedies:
                    1. Chant 'Om Shukraya Namaha' on Fridays (108 repetitions).
                    2. Offer white sweets or milk to a sacred shrine on Fridays.
                    3. Honor Gauri-Shankar blessings for mutual understanding and enduring peace.
                    """.trimIndent()
                }
            }

            msg.contains("career") || msg.contains("job") || msg.contains("work") || msg.contains("promotion") || msg.contains("business") || msg.contains("नौकरी") || msg.contains("व्यापार") || msg.contains("करियर") -> {
                if (isHi) {
                    """
                    नमस्ते $person! आपके जन्म चक्र के 10वें भाव (कर्म एवं प्रतिष्ठा भाव) का विश्लेषण:

                    💼 आजीविका एवं कर्म योग:
                    • 10वां भाव राशि: ${house10?.signName ?: "मकर"} (दशमेश: ${house10?.lord ?: "शनि"})
                    • कर्म भाव में स्थित ग्रह: ${house10?.planets?.joinToString(", ")?.ifEmpty { "कर्मेश का स्वतंत्र प्रभाव" } ?: "शुभ ग्रह स्थिति"}
                    • वर्तमान महादशा: $dasha ($antardasha अंतर्दशा)

                    🔮 फलित विश्लेषण:
                    दशम भाव आपके व्यावसायिक प्रभाव और सामाजिक प्रतिष्ठा का केंद्र है। $dasha की वर्तमान दशा आपके नेतृत्व कौशल और दीर्घकालिक परियोजनाओं में सफलता का योग बना रही है। किसी भी निर्णय में निष्ठा और धैर्य रखें।

                    🕉️ सरल वैदिक उपाय:
                    1. नित्य प्रातः उगते सूर्य को तांबे के लोटे से अर्घ्य दें (गायत्री मंत्र सहित)।
                    2. शनिवार को जरूरतमंदों को तिल अथवा भोजन का दान करें।
                    3. 'ॐ ह्रीं सूर्याय नमः' का नियमित जप करें।
                    """.trimIndent()
                } else {
                    """
                    Namaste $person! Here is your Vedic career & profession analysis for the 10th House (Karma Bhava):

                    💼 Career & Professional Trajectory:
                    • 10th House Sign: ${house10?.signName ?: "Makara (Capricorn)"} (Lord: ${house10?.lord ?: "Saturn (Shani)"})
                    • Active Planets in 10th House: ${house10?.planets?.joinToString(", ")?.ifEmpty { "Direct unhindered lord aspect" } ?: "Planetary presence"}
                    • Operating Period: $dasha Mahadasha with $antardasha Antardasha

                    🔮 Astrological Guidance:
                    The 10th lord and your current $dasha transit indicate significant professional expansion. You will earn respect through diligence and strategic execution. Negotiations and career advancements are strongly supported.

                    🕉️ Auspicious Vedic Remedies:
                    1. Offer water (Arghya) to the rising Sun daily in a copper vessel with the Gayatri Mantra.
                    2. Chant 'Om Suryaya Namaha' or Aditya Hridaya Stotram on Sundays.
                    3. Feed birds or donate grains to encourage sustained career progression.
                    """.trimIndent()
                }
            }

            msg.contains("wealth") || msg.contains("finance") || msg.contains("money") || msg.contains("gain") || msg.contains("धन") || msg.contains("पैसा") || msg.contains("संपत्ति") -> {
                if (isHi) {
                    """
                    नमस्ते $person! आपके जन्म चक्र के द्वितीय (धन) एवं एकादश (लाभ) भाव का विश्लेषण:

                    💰 धन एवं लाभ योग:
                    • धन भाव (2nd House): राशि ${house2?.signName ?: "वृषभ"}, स्वामी: ${house2?.lord ?: "शुक्र"}
                    • लाभ भाव (11th House): राशि ${house11?.signName ?: "कुंभ"}, स्वामी: ${house11?.lord ?: "शनि"}
                    • महादशा प्रभाव: $dasha महादशा काल

                    🔮 फलित विश्लेषण:
                    द्वितीयेश और एकादशेश का संबंध धन योग का सृजन करता है। आपकी कुंडली में संचित धन और नियमित आय के स्रोत मजबूत हैं। अनावश्यक जोखिम भरे सट्टेबाज़ी से बचें और व्यवस्थित निवेश करें।

                    🕉️ सरल वैदिक उपाय:
                    1. शुक्रवार को श्री सूक्तम अथवा कनकधारा स्तोत्र का पाठ करें।
                    2. घर के मुख्य द्वार पर दीपक प्रज्वलित करें और स्वच्छता का ध्यान रखें।
                    3. पक्षियों को हरी मूंग अथवा बाजरा खिलाएं।
                    """.trimIndent()
                } else {
                    """
                    Namaste $person! Here is the Vedic financial analysis of your 2nd House (Dhana Bhava) and 11th House (Labha Bhava):

                    💰 Wealth, Assets & Financial Growth:
                    • 2nd House (Accumulated Wealth): Sign ${house2?.signName ?: "Taurus"}, Lord: ${house2?.lord ?: "Venus"}
                    • 11th House (Income & Gains): Sign ${house11?.signName ?: "Aquarius"}, Lord: ${house11?.lord ?: "Saturn"}
                    • Current Operating Planetary Influence: $dasha Mahadasha

                    🔮 Astrological Insights:
                    The relationship between your 2nd and 11th houses establishes a protective Dhana Yoga. Prudent savings and structured financial planning will yield compound prosperity during this $dasha period.

                    🕉️ Auspicious Vedic Remedies:
                    1. Recite Sri Suktam or Kanakadhara Stotram on Fridays.
                    2. Maintain cleanliness at the north-east (Ishanya) corner of your living space.
                    3. Donate green lentils (Moong dal) or feed cows on Wednesdays.
                    """.trimIndent()
                }
            }

            msg.contains("dasha") || msg.contains("remedy") || msg.contains("remedies") || msg.contains("दशा") || msg.contains("उपाय") -> {
                if (isHi) {
                    """
                    नमस्ते $person! आपकी वर्तमान विंशोत्तरी दशा एवं वैदिक उपचार:

                    🧘 महादशा एवं अंतर्दशा का प्रभाव:
                    • वर्तमान महादशा: $dasha स्वामी
                    • वर्तमान अंतर्दशा: $antardasha
                    • लग्न: $lagna • चंद्र राशि: $rashi ($nakshatra नक्षत्र)

                    🔮 दशा फलित मार्गदर्शन:
                    $dasha महादशा आपके जीवन में नए आध्यात्मिक एवं व्यावहारिक अनुभवों का संचार कर रही है। जब अंतर्दशा में $antardasha का संयोग बनता है, तो संतुलन और मानसिक एकाग्रता सर्वाधिक फलदायी होती है।

                    🕉️ प्रभावी वैदिक शांति उपाय:
                    1. $dasha महादशा मंत्र: ॐ नमो भगवते वासुदेवाय (108 बार)।
                    2. नवग्रह शांति हेतु प्रत्येक पूर्णिमा अथवा एकादशी को सात्विक आहार ग्रहण करें।
                    3. माता-पिता एवं गुरुजनों का आशीर्वाद लें, इससे बृहस्पति एवं सूर्य की शुभता में वृद्धि होती है।
                    """.trimIndent()
                } else {
                    """
                    Namaste $person! Here is the detailed breakdown of your active Vimshottari Dasha and customized Vedic remedies:

                    🧘 Active Planetary Period:
                    • Current Mahadasha: $dasha
                    • Current Antardasha: $antardasha
                    • Ascendant (Lagna): $lagna • Moon Sign (Rashi): $rashi ($nakshatra Nakshatra)

                    🔮 Interpretive Transit Impact:
                    Your active $dasha Mahadasha marks a pivotal developmental epoch. The sub-influence of $antardasha brings dynamic mental clarity and opportunities to establish long-term foundations.

                    🕉️ Harmonizing Vedic Remedies:
                    1. Chant the planetary mantra for $dasha: 'Om Namo Bhagavate Vasudevaya' (108 repetitions).
                    2. Observe a mindful, sattvic routine on Thursdays and Mondays.
                    3. Honor your elders and spiritual mentors to fortify the benevolence of Jupiter and the Sun.
                    """.trimIndent()
                }
            }

            msg.contains("gemstone") || msg.contains("ratna") || msg.contains("stone") || msg.contains("रत्न") -> {
                if (isHi) {
                    """
                    नमस्ते $person! आपके लग्न एवं राशि के अनुसार रत्न परामर्श:

                    🧿 शुभ रत्न विश्लेषण:
                    • आपका लग्न: $lagna
                    • चंद्र राशि: $rashi ($nakshatra नक्षत्र)
                    • लग्न स्वामी ग्रह: ${kundli?.planets?.firstOrNull()?.dignity ?: "शुभ"}

                    🔮 शास्त्रीय वैदिक नियम:
                    वैदिक ज्योतिष के अनुसार केवल त्रिकोण भावों (1, 5, 9) के स्वामियों का रत्न धारण करना पूर्णतः निरापद और शुभप्रद होता है। 6, 8, 12 भावों के स्वामियों का रत्न कभी नहीं पहनना चाहिए।

                    💎 अनुशंसित रत्न:
                    • पीला पुखराज (Yellow Sapphire) अथवा मोती (Natural Pearl) आपके लिए अत्यंत शांतिदायक एवं भाग्यवर्धक सिद्ध होगा।
                    • इसे चांदी अथवा स्वर्ण धातु में, विधिपूर्वक गंगाजल व कच्चे दूध से शुद्ध करके, शुभ मुहूर्त में धारण करें।
                    """.trimIndent()
                } else {
                    """
                    Namaste $person! Here is your personalized Vedic Gemstone (Ratna) consultation:

                    🧿 Astrological Basis:
                    • Ascendant (Lagna): $lagna
                    • Moon Sign (Rashi): $rashi ($nakshatra Nakshatra)
                    • Functional Benefic: Lagnesha & Trikona Lords

                    🔮 Parashari Principle:
                    According to classical Vedic Jyotish, only gemstones governing the auspicious Trikona houses (1st, 5th, and 9th) should be worn. Never wear stones belonging to lords of the 6th, 8th, or 12th Dusthana houses.

                    💎 Auspicious Recommendation:
                    • Yellow Sapphire (Pukhraj) or Natural Pearl (Moti) is exceptionally beneficial for your chart, amplifying wisdom, intuition, and peace of mind.
                    • Wear in silver or gold on an auspicious weekday morning after traditional cleansing with milk and holy water.
                    """.trimIndent()
                }
            }

            msg.contains("health") || msg.contains("disease") || msg.contains("energy") || msg.contains("स्वास्थ्य") || msg.contains("रोग") -> {
                if (isHi) {
                    """
                    नमस्ते $person! आपके 6वें भाव (रोग एवं स्वास्थ्य) का वैदिक विश्लेषण:

                    🌿 स्वास्थ्य एवं जीवनी शक्ति:
                    • 6वां भाव राशि: ${house6?.signName ?: "कन्या"} (स्वामी: ${house6?.lord ?: "बुध"})
                    • लग्न शक्ति: $lagna
                    • वर्तमान महादशा: $dasha काल

                    🔮 आरोग्य मार्गदर्शन:
                    कुंडली में लग्न की स्थिति मजबूत है जो प्राकृतिक रोग प्रतिरोधक क्षमता (Immunity) दर्शाती है। बदलते मौसम में वात एवं पित्त संतुलन का ध्यान रखें। योग एवं प्राणायाम से स्वास्थ्य उत्तम रहेगा।

                    🕉️ सरल स्वास्थ्य उपाय:
                    1. भगवान महामृत्युंजय के मंत्र का स्मरण करें: ॐ त्र्यम्बकं यजामहे...
                    2. तांबे के बर्तन में रखा जल प्रातःकाल ग्रहण करें।
                    3. नित्य 15 मिनट अनुलोम-विलोम प्राणायाम करें।
                    """.trimIndent()
                } else {
                    """
                    Namaste $person! Here is the Vedic health & vitality analysis for your 6th House (Roga & Shatru Bhava):

                    🌿 Vitality & Wellness Overview:
                    • 6th House Sign: ${house6?.signName ?: "Kanya (Virgo)"} (Lord: ${house6?.lord ?: "Mercury"})
                    • Lagna Vitality: $lagna
                    • Current Period: $dasha Mahadasha

                    🔮 Health Insights:
                    Your Lagna and Sun placement reflect resilient vitality and natural immunity. Balancing bodily doshas through mindful hydration, seasonal diet, and regular morning movement will sustain high energy levels.

                    🕉️ Holistic Vedic Remedies:
                    1. Recite the Maha Mrityunjaya Mantra for protection and holistic wellness.
                    2. Drink water stored in a copper vessel in the morning.
                    3. Practice daily pranayama (breathwork) to nourish mind and body.
                    """.trimIndent()
                }
            }

            else -> {
                if (isHi) {
                    """
                    प्रणाम $person! मैं देवी प्रिया, आपकी वैदिक AI ज्योतिषी।

                    ✨ आपके जन्म चक्र के मुख्य बिंदु:
                    • लग्न: $lagna
                    • चंद्र राशि: $rashi ($nakshatra नक्षत्र, चरण ${kundli?.nakshatraPada ?: 1})
                    • सक्रिय महादशा: $dasha ($antardasha अंतर्दशा)
                    • मांगलिक स्थिति: $manglik

                    🔮 आपके प्रश्न ($userMessage) पर वैदिक मार्गदर्शन:
                    आपकी जन्म कुंडली में ग्रहों की अनुकूल स्थिति आपके कर्म और एकाग्रता को बल दे रही है। इस समय $dasha की ऊर्जा आपको अपने लक्ष्यों के प्रति गंभीर और जागरूक बना रही है। धैर्य और धर्मपरायणता से किए गए कार्य दीर्घकालिक फल देंगे।

                    🕉️ आज का सरल मंत्र:
                    ॐ नमः शिवाय (108 बार जप) — यह मन को शांति और जीवन को दिशा प्रदान करता है।
                    """.trimIndent()
                } else {
                    """
                    Pranam $person! I am Devi Priya, your Vedic AI Astrologer.

                    ✨ Key Highlights of Your Chart:
                    • Ascendant (Lagna): $lagna
                    • Moon Sign (Rashi): $rashi ($nakshatra Nakshatra, Pada ${kundli?.nakshatraPada ?: 1})
                    • Current Dasha: $dasha Mahadasha ($antardasha Antardasha)
                    • Manglik Alignment: $manglik

                    🔮 Astrological Guidance for your query:
                    In your Vedic birth chart, the alignment of planetary energies supports mindful effort and righteous karma (Dharma). Under the influence of $dasha, perseverance and clarity will unlock favorable opportunities across your endeavors.

                    🕉️ Auspicious Affirmation & Mantra:
                    Recite 'Om Namah Shivaya' (108 times) for inner tranquility, discernment, and planetary grace.
                    """.trimIndent()
                }
            }
        }
    }
}
