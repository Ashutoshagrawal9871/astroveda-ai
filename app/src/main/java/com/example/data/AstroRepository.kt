package com.example.data

import com.example.model.PlanType
import com.example.model.UserBalance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AstroRepository(private val dao: AstroDao) {

    val allProfiles: Flow<List<UserProfileEntity>> = dao.getAllProfiles()
    val primaryProfile: Flow<UserProfileEntity?> = dao.getPrimaryProfile()
    val chatMessages: Flow<List<ChatMessageEntity>> = dao.getChatMessages()
    val savedReadings: Flow<List<SavedReadingEntity>> = dao.getSavedReadings()
    val callHistory: Flow<List<CallHistoryEntity>> = dao.getCallHistory()

    data class DailyReadingCacheKey(
        val profileId: Long,
        val dateString: String,
        val language: String
    )

    private val dailyReadingCache = java.util.concurrent.ConcurrentHashMap<DailyReadingCacheKey, String>()

    fun getDailyHoroscopeCached(profileId: Long, dateString: String, language: String): String? {
        return dailyReadingCache[DailyReadingCacheKey(profileId, dateString, language)]
    }

    fun putDailyHoroscopeCache(profileId: Long, dateString: String, language: String, reading: String) {
        dailyReadingCache[DailyReadingCacheKey(profileId, dateString, language)] = reading
    }

    val billingState: Flow<UserBalance> = dao.getBillingState().map { entity ->
        if (entity == null) {
            UserBalance()
        } else {
            val plan = try {
                PlanType.valueOf(entity.planType)
            } catch (_: Exception) {
                PlanType.PAY_AS_YOU_USE
            }
            UserBalance(
                currentPlan = plan,
                planExpiryMillis = entity.expiryMillis,
                monthlyAllowanceSessionsRemaining = entity.monthlyAllowanceSessions,
                prepaidVideoSessionsRemaining = entity.prepaidVideoSessions,
                chatMessagesRemaining = entity.chatCredits,
                voiceMinutesRemaining = entity.voiceMinutes,
                videoMinutesRemaining = entity.videoMinutes,
                isAdFree = entity.isAdFree,
                monthlySpendingInr = entity.monthlySpendingInr,
                maxMonthlySpendCapInr = entity.maxMonthlySpendCapInr
            )
        }
    }

    suspend fun initializeDefaultsIfNeeded() {
        val existing = dao.getBillingStateOnce()
        if (existing == null) {
            // Strict rule: There is no free AI plan. Initial credits = 0.
            dao.saveBillingState(
                UserBillingStateEntity(
                    id = 1,
                    planType = "PAY_AS_YOU_USE",
                    expiryMillis = 0L,
                    monthlyAllowanceSessions = 0,
                    prepaidVideoSessions = 0,
                    chatCredits = 0,
                    voiceMinutes = 0,
                    videoMinutes = 0,
                    isAdFree = false,
                    aiMemoryEnabled = true,
                    languageCode = "en",
                    monthlyAllowanceRefreshedTimestamp = System.currentTimeMillis(),
                    monthlySpendingInr = 0,
                    maxMonthlySpendCapInr = 250000
                )
            )
        }
    }

    suspend fun saveProfile(profile: UserProfileEntity): Long {
        if (profile.isPrimary) {
            dao.clearPrimaryFlags()
        }
        return dao.insertProfile(profile)
    }

    suspend fun deleteProfile(id: Long) = dao.deleteProfile(id)

    suspend fun setPrimaryProfile(id: Long) {
        dao.clearPrimaryFlags()
        dao.setPrimaryProfile(id)
    }

    suspend fun sendChatMessage(text: String, isUser: Boolean, category: String = "general"): Long {
        val entity = ChatMessageEntity(
            senderRole = if (isUser) "user" else "model",
            text = text,
            timestamp = System.currentTimeMillis(),
            category = category
        )
        val id = dao.insertChatMessage(entity)
        if (isUser) {
            dao.decrementChatCredit()
        }
        return id
    }

    /**
     * Sends a message to Gemini via GeminiService and saves both user query and Gemini's response
     * in Room local storage.
     */
    suspend fun sendChatMessageToGemini(
        userMessage: String,
        kundli: com.example.model.KundliChartData? = null,
        language: String = "en",
        category: String = "general"
    ): String {
        // 1. Save user message to database
        sendChatMessage(userMessage, isUser = true, category = category)

        // 2. Fetch recent conversation context for Gemini
        val recentHistory = dao.getChatMessagesOnce().takeLast(6).map { it.senderRole to it.text }

        // 3. Call Gemini
        val reply = com.example.ai.GeminiService.consultAstrologer(
            userMessage = userMessage,
            kundli = kundli,
            chatHistory = recentHistory,
            language = language,
            isVoiceOrVideo = false
        )

        // 4. Save Gemini response to database
        sendChatMessage(reply, isUser = false, category = category)
        return reply
    }

    suspend fun toggleSaveMessage(id: Long, saved: Boolean) = dao.toggleSaveMessage(id, saved)

    /**
     * Fetches a personalized daily horoscope reading based on user's saved birth details,
     * consulting Gemini with Vedic astrological parameters and saving the reading.
     */
    suspend fun fetchPersonalizedDailyHoroscope(
        profile: UserProfileEntity?,
        language: String = "en",
        forceRefresh: Boolean = false
    ): String {
        if (profile == null || profile.birthDate.isBlank()) {
            return if (language == "hi") {
                "कृपया प्रामाणिक दैनिक वैदिक राशिफल के लिए पहले अपनी जन्म तिथि, समय और स्थान दर्ज करके अपनी जन्म कुंडली प्रोफ़ाइल बनाएं।"
            } else {
                "Please create or select your Kundli profile with your authentic birth date, time, and location to generate a personalized daily Vedic horoscope reading."
            }
        }

        val todayDateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        val cacheKey = DailyReadingCacheKey(profile.id, todayDateStr, language)

        if (!forceRefresh) {
            dailyReadingCache[cacheKey]?.let { cached ->
                return cached
            }
        }

        val personName = profile.name.ifBlank { "Seeker" }
        val birthDate = profile.birthDate
        val birthTime = profile.birthTime
        val birthPlace = profile.birthPlace
        val lat = profile.lat
        val lng = profile.lng
        val isTimeUnknown = profile.isTimeUnknown

        // Compute authentic Vedic chart parameters
        val kundli = com.example.astro.VedicEngine.calculateKundli(
            name = personName,
            birthDate = birthDate,
            birthTime = birthTime,
            place = birthPlace,
            isTimeApproximate = isTimeUnknown,
            lat = lat,
            lng = lng
        ) ?: return "Unable to calculate Kundli. Please ensure birth date and time format are correct."

        val prompt = if (language == "hi") {
            "कृपया मेरे जन्म विवरण (जन्म तिथि: $birthDate, जन्म समय: $birthTime, स्थान: $birthPlace, लग्न: ${kundli.lagnaSignName}, चंद्र राशि: ${kundli.rashiSignName}, नक्षत्र: ${kundli.nakshatra}, वर्तमान महादशा: ${kundli.currentMahadasha}) के आधार पर आज का सटीक और व्यक्तिगत दैनिक राशिफल प्रदान करें। इसमें सामान्य दिनचर्या, करियर/धन, प्रेम/संबंध, स्वास्थ्य और आज का सरल वैदिक उपाय (मंत्र/रंग) शामिल करें।"
        } else {
            "Please provide a personalized daily Vedic horoscope reading based on my saved birth details (Birth Date: $birthDate, Birth Time: $birthTime, Birth Place: $birthPlace, Ascendant/Lagna: ${kundli.lagnaSignName}, Moon Sign/Rashi: ${kundli.rashiSignName}, Nakshatra: ${kundli.nakshatra}, Current Mahadasha: ${kundli.currentMahadasha}). Include sections for: 1. Cosmic Weather of the Day, 2. Career & Finance, 3. Love & Family, 4. Health & Energy, and 5. Today's Vedic Remedy, Lucky Color, and Mantra."
        }

        val rawReading = try {
            com.example.ai.GeminiService.consultAstrologer(
                userMessage = prompt,
                kundli = kundli,
                chatHistory = emptyList(),
                language = language,
                isVoiceOrVideo = false
            )
        } catch (_: Exception) {
            ""
        }

        val reading = if (rawReading.isNotBlank()) {
            rawReading
        } else {
            com.example.astro.VedicEngine.formatDailyHoroscopeReading(kundli, language)
        }

        // Cache the reading for (profileId, today, language)
        dailyReadingCache[cacheKey] = reading

        // Save to reading history
        saveReading(
            title = "Daily Horoscope - ${java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date())}",
            category = "Daily Horoscope",
            content = reading
        )

        return reading
    }

    suspend fun saveReading(title: String, category: String, content: String): Long {
        return dao.insertSavedReading(
            SavedReadingEntity(
                title = title,
                category = category,
                content = content
            )
        )
    }

    suspend fun deleteSavedReading(id: Long) = dao.deleteSavedReading(id)

    suspend fun clearChatHistory() = dao.clearChatHistory()

    suspend fun clearCallHistory() = dao.clearCallHistory()

    suspend fun recordCall(type: String, durationSecs: Int) {
        dao.insertCallRecord(
            CallHistoryEntity(
                callType = type,
                durationSeconds = durationSecs,
                timestamp = System.currentTimeMillis()
            )
        )
        val minutesUsed = ((durationSecs + 59) / 60).coerceAtLeast(1)
        if (type == "voice") {
            dao.deductVoiceMinutes(minutesUsed)
        } else {
            dao.deductVideoMinutes(minutesUsed)
        }
    }

    /**
     * Overwrites the local billing state with server-authoritative entitlements.
     * The backend is the single source of truth after Google Play verification.
     */
    suspend fun applyServerEntitlements(
        planType: String,
        expiryMillis: Long,
        chatCredits: Int,
        voiceMinutes: Int,
        isAdFree: Boolean
    ) {
        val current = dao.getBillingStateOnce() ?: UserBillingStateEntity()
        val plan = when (planType) {
            PlanType.ELITE_MONTHLY.name -> PlanType.ELITE_MONTHLY
            PlanType.ELITE_ANNUAL.name -> PlanType.ELITE_ANNUAL
            else -> PlanType.PAY_AS_YOU_USE
        }
        dao.saveBillingState(
            current.copy(
                planType = plan.name,
                expiryMillis = expiryMillis,
                chatCredits = chatCredits.coerceAtLeast(0),
                voiceMinutes = voiceMinutes.coerceAtLeast(0),
                isAdFree = isAdFree
            )
        )
    }

    suspend fun addChatCredits(credits: Int) = dao.addChatCredits(credits)

    suspend fun addVoiceMinutes(minutes: Int) = dao.addVoiceMinutes(minutes)

    suspend fun addVideoMinutes(minutes: Int) = dao.addVideoMinutes(minutes)

    suspend fun setAiMemory(enabled: Boolean) = dao.setAiMemory(enabled)

    suspend fun setLanguage(lang: String) = dao.setLanguage(lang)

    suspend fun resetBillingState() = dao.saveBillingState(UserBillingStateEntity())
}
