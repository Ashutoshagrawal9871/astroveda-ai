package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val gender: String,
    val birthDate: String, // YYYY-MM-DD
    val birthTime: String, // HH:mm
    val isTimeUnknown: Boolean = false,
    val birthPlace: String,
    val lat: Double = 28.6139,
    val lng: Double = 77.2090,
    val isPrimary: Boolean = true,
    val createdTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val senderRole: String, // "user", "model"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSavedReading: Boolean = false,
    val category: String = "general"
)

@Entity(tableName = "saved_readings")
data class SavedReadingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String, // Career, Love, Health, Remedies, Dasha
    val content: String,
    val savedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "call_history")
data class CallHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val callType: String, // "voice", "video"
    val durationSeconds: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "COMPLETED"
)

@Entity(tableName = "billing_state")
data class UserBillingStateEntity(
    @PrimaryKey val id: Int = 1,
    val planType: String = "PAY_AS_YOU_USE",
    val expiryMillis: Long = 0L,
    val monthlyAllowanceSessions: Int = 0, // 10 included sessions per month for Elite Monthly & Annual
    val prepaidVideoSessions: Int = 0,    // Prepaid video sessions or extensions
    val chatCredits: Int = 0,              // Strict rule: No free AI plan
    val voiceMinutes: Int = 0,
    val videoMinutes: Int = 0,
    val isAdFree: Boolean = false,
    val aiMemoryEnabled: Boolean = true,
    val languageCode: String = "en",
    val monthlyAllowanceRefreshedTimestamp: Long = System.currentTimeMillis(),
    val monthlySpendingInr: Int = 0,       // Track monthly spending for operator spending cap
    val maxMonthlySpendCapInr: Int = 250000 // Operator-configured monthly spend cap
)
