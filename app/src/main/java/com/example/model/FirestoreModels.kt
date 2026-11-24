package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue

/**
 * Value object encapsulating Vedic birth chart inputs.
 */
data class BirthDetails(
    val name: String = "",
    val birthDate: String = "",       // Format: YYYY-MM-DD
    val birthTime: String = "",       // Format: HH:MM
    val birthPlace: String = "",      // Location string (e.g., "New Delhi, India")
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timezoneOffset: Double = 5.5, // Default IST (+5.5 UTC)
    val gender: String = "male",
    val isTimeApproximate: Boolean = false
) {
    fun isValid(): Boolean = birthDate.isNotBlank() && birthTime.isNotBlank() && birthPlace.isNotBlank()
}

/**
 * Root User Profile document stored at /users/{userId} in Firestore.
 * Holds user identity, astrological configuration, and primary birth details.
 */
data class UserProfileFirestore(
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String? = null,
    val preferredLanguage: String = "en",
    val aiMemoryEnabled: Boolean = true,
    // Primary birth details for instant Kundli calculations
    val birthDate: String? = null,
    val birthTime: String? = null,
    val birthPlace: String? = null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timezoneOffset: Double = 5.5,
    val gender: String = "male",
    val isTimeApproximate: Boolean = false,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun hasValidBirthDetails(): Boolean {
        return !birthDate.isNullOrBlank() && !birthTime.isNullOrBlank() && !birthPlace.isNullOrBlank()
    }

    fun toBirthDetails(): BirthDetails? {
        if (!hasValidBirthDetails()) return null
        return BirthDetails(
            name = displayName.ifBlank { "Seeker" },
            birthDate = birthDate ?: "",
            birthTime = birthTime ?: "",
            birthPlace = birthPlace ?: "",
            latitude = latitude,
            longitude = longitude,
            timezoneOffset = timezoneOffset,
            gender = gender,
            isTimeApproximate = isTimeApproximate
        )
    }

    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "id" to id,
            "email" to email,
            "displayName" to displayName,
            "preferredLanguage" to preferredLanguage,
            "aiMemoryEnabled" to aiMemoryEnabled,
            "createdAt" to (createdAt ?: FieldValue.serverTimestamp()),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        photoUrl?.let { map["photoUrl"] = it }
        birthDate?.let { map["birthDate"] = it }
        birthTime?.let { map["birthTime"] = it }
        birthPlace?.let { map["birthPlace"] = it }
        if (latitude != 0.0) map["latitude"] = latitude
        if (longitude != 0.0) map["longitude"] = longitude
        map["timezoneOffset"] = timezoneOffset
        map["gender"] = gender
        map["isTimeApproximate"] = isTimeApproximate
        return map
    }
}

/**
 * Individual Kundli Profile document stored at /users/{userId}/kundli_profiles/{profileId}.
 * Allows saving multiple Vedic birth charts (Self, Family, Partner, Friends).
 */
data class KundliProfileFirestore(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val relationship: String = "Self", // e.g., "Self", "Spouse", "Child", "Parent", "Partner"
    val birthDate: String = "",
    val birthTime: String = "",
    val birthPlace: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timezoneOffset: Double = 5.5,
    val gender: String = "male",
    val isTimeApproximate: Boolean = false,
    val isPrimary: Boolean = false,
    val notes: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun hasValidBirthDetails(): Boolean {
        return birthDate.isNotBlank() && birthTime.isNotBlank() && birthPlace.isNotBlank()
    }

    fun toBirthDetails(): BirthDetails {
        return BirthDetails(
            name = name,
            birthDate = birthDate,
            birthTime = birthTime,
            birthPlace = birthPlace,
            latitude = latitude,
            longitude = longitude,
            timezoneOffset = timezoneOffset,
            gender = gender,
            isTimeApproximate = isTimeApproximate
        )
    }

    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "name" to name,
            "relationship" to relationship,
            "birthDate" to birthDate,
            "birthTime" to birthTime,
            "birthPlace" to birthPlace,
            "latitude" to latitude,
            "longitude" to longitude,
            "timezoneOffset" to timezoneOffset,
            "gender" to gender,
            "isTimeApproximate" to isTimeApproximate,
            "isPrimary" to isPrimary,
            "notes" to notes,
            "createdAt" to (createdAt ?: FieldValue.serverTimestamp()),
            "updatedAt" to FieldValue.serverTimestamp()
        )
    }
}

data class UserEntitlementFirestore(
    val userId: String = "",
    val currentPlan: String = "PAY_AS_YOU_USE",
    val planExpiryMillis: Long = 0L,
    val chatMessagesRemaining: Long = 0L,
    val voiceMinutesRemaining: Long = 0L,
    val videoMinutesRemaining: Long = 0L,
    val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "userId" to userId,
            "currentPlan" to currentPlan,
            "planExpiryMillis" to planExpiryMillis,
            "chatMessagesRemaining" to chatMessagesRemaining,
            "voiceMinutesRemaining" to voiceMinutesRemaining,
            "videoMinutesRemaining" to videoMinutesRemaining,
            "updatedAt" to FieldValue.serverTimestamp()
        )
    }
}

data class ChatMessageFirestore(
    val id: String = "",
    val userId: String = "",
    val sender: String = "user",
    val text: String = "",
    val timestamp: Timestamp? = null,
    val profileId: String = ""
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "sender" to sender,
            "text" to text,
            "timestamp" to (timestamp ?: FieldValue.serverTimestamp()),
            "profileId" to profileId
        )
    }
}
