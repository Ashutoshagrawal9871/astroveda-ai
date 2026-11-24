package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AstroDao {

    // Profiles
    @Query("SELECT * FROM user_profiles ORDER BY isPrimary DESC, id DESC")
    fun getAllProfiles(): Flow<List<UserProfileEntity>>

    @Query("SELECT COUNT(*) FROM user_profiles")
    suspend fun getProfileCount(): Int

    @Query("SELECT * FROM user_profiles WHERE isPrimary = 1 LIMIT 1")
    fun getPrimaryProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfileEntity): Long

    @Update
    suspend fun updateProfile(profile: UserProfileEntity)

    @Query("DELETE FROM user_profiles WHERE id = :id")
    suspend fun deleteProfile(id: Long)

    @Query("UPDATE user_profiles SET isPrimary = 0")
    suspend fun clearPrimaryFlags()

    @Query("UPDATE user_profiles SET isPrimary = 1 WHERE id = :id")
    suspend fun setPrimaryProfile(id: Long)

    // Chat
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getChatMessages(): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    suspend fun getChatMessagesOnce(): List<ChatMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity): Long

    @Query("UPDATE chat_messages SET isSavedReading = :saved WHERE id = :id")
    suspend fun toggleSaveMessage(id: Long, saved: Boolean)

    @Query("DELETE FROM chat_messages")
    suspend fun clearChatHistory()

    // Saved Readings
    @Query("SELECT * FROM saved_readings ORDER BY savedTimestamp DESC")
    fun getSavedReadings(): Flow<List<SavedReadingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedReading(reading: SavedReadingEntity): Long

    @Query("DELETE FROM saved_readings WHERE id = :id")
    suspend fun deleteSavedReading(id: Long)

    // Call History
    @Query("SELECT * FROM call_history ORDER BY timestamp DESC")
    fun getCallHistory(): Flow<List<CallHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallRecord(call: CallHistoryEntity): Long

    @Query("DELETE FROM call_history")
    suspend fun clearCallHistory()

    // Billing
    @Query("SELECT * FROM billing_state WHERE id = 1")
    fun getBillingState(): Flow<UserBillingStateEntity?>

    @Query("SELECT * FROM billing_state WHERE id = 1")
    suspend fun getBillingStateOnce(): UserBillingStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveBillingState(state: UserBillingStateEntity)

    @Query("UPDATE billing_state SET chatCredits = chatCredits + :credits WHERE id = 1")
    suspend fun addChatCredits(credits: Int)

    @Query("UPDATE billing_state SET voiceMinutes = voiceMinutes + :minutes WHERE id = 1")
    suspend fun addVoiceMinutes(minutes: Int)

    @Query("UPDATE billing_state SET videoMinutes = videoMinutes + :minutes WHERE id = 1")
    suspend fun addVideoMinutes(minutes: Int)

    @Query("UPDATE billing_state SET prepaidVideoSessions = prepaidVideoSessions + :sessions, monthlySpendingInr = monthlySpendingInr + :amountInr WHERE id = 1")
    suspend fun addPrepaidVideoSessions(sessions: Int, amountInr: Int)

    @Query("UPDATE billing_state SET chatCredits = CASE WHEN chatCredits > 0 THEN chatCredits - 1 ELSE 0 END WHERE id = 1")
    suspend fun decrementChatCredit()

    @Query("UPDATE billing_state SET voiceMinutes = CASE WHEN voiceMinutes >= :minutes THEN voiceMinutes - :minutes ELSE 0 END WHERE id = 1")
    suspend fun deductVoiceMinutes(minutes: Int)

    @Query("UPDATE billing_state SET videoMinutes = CASE WHEN videoMinutes >= :minutes THEN videoMinutes - :minutes ELSE 0 END WHERE id = 1")
    suspend fun deductVideoMinutes(minutes: Int)

    @Query("""
        UPDATE billing_state 
        SET monthlyAllowanceSessions = CASE WHEN monthlyAllowanceSessions > 0 THEN monthlyAllowanceSessions - 1 ELSE 0 END,
            prepaidVideoSessions = CASE WHEN monthlyAllowanceSessions <= 0 AND prepaidVideoSessions > 0 THEN prepaidVideoSessions - 1 ELSE prepaidVideoSessions END
        WHERE id = 1
    """)
    suspend fun deductOneVideoSession()

    @Query("UPDATE billing_state SET monthlyAllowanceSessions = 10, monthlyAllowanceRefreshedTimestamp = :now WHERE id = 1")
    suspend fun refreshMonthlyAllowance(now: Long)

    @Query("UPDATE billing_state SET aiMemoryEnabled = :enabled WHERE id = 1")
    suspend fun setAiMemory(enabled: Boolean)

    @Query("UPDATE billing_state SET languageCode = :lang WHERE id = 1")
    suspend fun setLanguage(lang: String)
}
