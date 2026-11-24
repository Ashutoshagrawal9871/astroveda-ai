package com.example.data

import android.content.Context
import com.example.R
import com.example.astro.VedicEngine
import com.example.model.BirthDetails
import com.example.model.KundliChartData
import com.example.model.KundliProfileFirestore
import com.example.model.UserProfileFirestore
import com.example.util.FirestoreErrorLogger
import com.example.util.OperationType
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Dedicated Repository layer for storing, synchronizing, and computing
 * Vedic birth details and Kundli calculations using Firebase Firestore.
 */
class KundliBirthDetailsRepository(
    private val db: FirebaseFirestore
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    /**
     * Real-time stream of the authenticated user's root profile and birth details.
     */
    fun observeUserProfile(userId: String): Flow<UserProfileFirestore?> {
        val docRef = db.collection("users").document(userId)
        return docRef.snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(UserProfileFirestore::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                } else {
                    null
                }
            }
            .catch { e ->
                FirestoreErrorLogger.handleFirestoreError(e as Exception, OperationType.GET, docRef.path)
                emit(null)
            }
            .flowOn(Dispatchers.IO)
    }

    /**
     * One-shot fetch of the user's profile and birth details.
     */
    suspend fun getUserProfile(userId: String): Result<UserProfileFirestore?> = withContext(Dispatchers.IO) {
        val docRef = db.collection("users").document(userId)
        try {
            val snapshot = docRef.get().await()
            val profile = if (snapshot.exists()) {
                snapshot.toObject(UserProfileFirestore::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            } else {
                null
            }
            Result.success(profile)
        } catch (e: Exception) {
            FirestoreErrorLogger.handleFirestoreError(e, OperationType.GET, docRef.path)
            Result.failure(e)
        }
    }

    /**
     * Saves or updates the root UserProfile document.
     */
    suspend fun saveUserProfile(profile: UserProfileFirestore): Result<Unit> = withContext(Dispatchers.IO) {
        val docRef = db.collection("users").document(profile.id)
        try {
            docRef.set(profile.toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            FirestoreErrorLogger.handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    /**
     * Updates only birth details in the user's profile for Kundli calculation.
     */
    suspend fun updateUserBirthDetails(
        userId: String,
        birthDetails: BirthDetails
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val docRef = db.collection("users").document(userId)
        try {
            val existing = docRef.get().await()
            val baseProfile = if (existing.exists()) {
                existing.toObject(UserProfileFirestore::class.java) ?: UserProfileFirestore(id = userId)
            } else {
                UserProfileFirestore(id = userId)
            }

            val updated = baseProfile.copy(
                birthDate = birthDetails.birthDate,
                birthTime = birthDetails.birthTime,
                birthPlace = birthDetails.birthPlace,
                latitude = birthDetails.latitude,
                longitude = birthDetails.longitude,
                timezoneOffset = birthDetails.timezoneOffset,
                gender = birthDetails.gender,
                isTimeApproximate = birthDetails.isTimeApproximate
            )

            docRef.set(updated.toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            FirestoreErrorLogger.handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    /**
     * Observes all saved Kundli profiles under /users/{userId}/kundli_profiles
     */
    fun observeKundliProfiles(userId: String): Flow<List<KundliProfileFirestore>> {
        val colRef = db.collection("users").document(userId).collection("kundli_profiles")
        return colRef.snapshots()
            .map { querySnapshot ->
                querySnapshot.toObjects(KundliProfileFirestore::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedWith(
                        compareByDescending<KundliProfileFirestore> { it.isPrimary }
                            .thenByDescending { it.createdAt?.seconds ?: 0L }
                    )
            }
            .catch { e ->
                FirestoreErrorLogger.handleFirestoreError(e as Exception, OperationType.LIST, colRef.path)
                emit(emptyList())
            }
            .flowOn(Dispatchers.IO)
    }

    /**
     * Fetches a specific Kundli profile by ID.
     */
    suspend fun getKundliProfile(userId: String, profileId: String): Result<KundliProfileFirestore?> = withContext(Dispatchers.IO) {
        val docRef = db.collection("users").document(userId).collection("kundli_profiles").document(profileId)
        try {
            val snapshot = docRef.get().await()
            val profile = if (snapshot.exists()) {
                snapshot.toObject(KundliProfileFirestore::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            } else null
            Result.success(profile)
        } catch (e: Exception) {
            FirestoreErrorLogger.handleFirestoreError(e, OperationType.GET, docRef.path)
            Result.failure(e)
        }
    }

    /**
     * Saves or creates a Kundli birth chart profile.
     */
    suspend fun saveKundliProfile(profile: KundliProfileFirestore): Result<String> = withContext(Dispatchers.IO) {
        val targetId = profile.id.ifBlank { "chart_${System.currentTimeMillis()}" }
        val toSave = profile.copy(id = targetId)
        val docRef = db.collection("users").document(profile.userId).collection("kundli_profiles").document(targetId)
        try {
            docRef.set(toSave.toMap()).await()
            Result.success(targetId)
        } catch (e: Exception) {
            FirestoreErrorLogger.handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    /**
     * Deletes a Kundli profile document from Firestore.
     */
    suspend fun deleteKundliProfile(userId: String, profileId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val docRef = db.collection("users").document(userId).collection("kundli_profiles").document(profileId)
        try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            FirestoreErrorLogger.handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    /**
     * Sets a specific chart as primary and unsets all other charts for this user.
     */
    suspend fun setPrimaryKundliProfile(userId: String, primaryProfileId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val colRef = db.collection("users").document(userId).collection("kundli_profiles")
        try {
            val snapshot = colRef.get().await()
            for (doc in snapshot.documents) {
                val isTarget = doc.id == primaryProfileId
                doc.reference.update("isPrimary", isTarget).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            FirestoreErrorLogger.handleFirestoreError(e, OperationType.UPDATE, colRef.path)
            Result.failure(e)
        }
    }

    /**
     * High-performance Kundli computation for given birth details using Vedic astrology rules.
     */
    fun calculateKundli(details: BirthDetails): KundliChartData? {
        val lat = if (details.latitude != 0.0) details.latitude else 28.6139
        val lng = if (details.longitude != 0.0) details.longitude else 77.2090
        return VedicEngine.calculateKundli(
            name = details.name.ifBlank { "Seeker" },
            birthDate = details.birthDate,
            birthTime = details.birthTime,
            place = details.birthPlace,
            isTimeApproximate = details.isTimeApproximate,
            lat = lat,
            lng = lng
        )
    }

    /**
     * Computes Kundli directly from the UserProfileFirestore model.
     */
    fun calculateKundliForUserProfile(userProfile: UserProfileFirestore): KundliChartData? {
        val birthDetails = userProfile.toBirthDetails() ?: return null
        return calculateKundli(birthDetails)
    }

    /**
     * Computes Kundli directly from a saved KundliProfileFirestore document.
     */
    fun calculateKundliForProfile(profile: KundliProfileFirestore): KundliChartData? {
        return calculateKundli(profile.toBirthDetails())
    }
}
