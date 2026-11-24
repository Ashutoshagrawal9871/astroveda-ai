package com.example.data

import android.content.Context
import com.example.R
import com.example.model.*
import com.example.util.FirestoreErrorLogger
import com.example.util.OperationType
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreAstroRepository(
    private val db: FirebaseFirestore
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

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
    }

    suspend fun saveUserProfile(profile: UserProfileFirestore): Result<Unit> {
        val docRef = db.collection("users").document(profile.id)
        return try {
            docRef.set(profile.toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            FirestoreErrorLogger.handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    fun observeKundliProfiles(userId: String): Flow<List<KundliProfileFirestore>> {
        val colRef = db.collection("users").document(userId).collection("kundli_profiles")
        return colRef.snapshots()
            .map { querySnapshot ->
                querySnapshot.toObjects(KundliProfileFirestore::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .catch { e ->
                FirestoreErrorLogger.handleFirestoreError(e as Exception, OperationType.LIST, colRef.path)
                emit(emptyList())
            }
    }

    suspend fun saveKundliProfile(profile: KundliProfileFirestore): Result<Unit> {
        val docRef = db.collection("users").document(profile.userId).collection("kundli_profiles").document(profile.id)
        return try {
            docRef.set(profile.toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            FirestoreErrorLogger.handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteKundliProfile(userId: String, profileId: String): Result<Unit> {
        val docRef = db.collection("users").document(userId).collection("kundli_profiles").document(profileId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            FirestoreErrorLogger.handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    fun observeEntitlements(userId: String): Flow<UserEntitlementFirestore?> {
        val docRef = db.collection("users").document(userId).collection("billing").document("entitlements")
        return docRef.snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(UserEntitlementFirestore::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                } else {
                    null
                }
            }
            .catch { e ->
                FirestoreErrorLogger.handleFirestoreError(e as Exception, OperationType.GET, docRef.path)
                emit(null)
            }
    }

    suspend fun saveEntitlements(entitlement: UserEntitlementFirestore): Result<Unit> {
        val docRef = db.collection("users").document(entitlement.userId).collection("billing").document("entitlements")
        return try {
            docRef.set(entitlement.toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            FirestoreErrorLogger.handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    fun observeChatMessages(userId: String): Flow<List<ChatMessageFirestore>> {
        val colRef = db.collection("users").document(userId).collection("chat_messages")
        return colRef.snapshots()
            .map { snapshot ->
                val list = snapshot.toObjects(ChatMessageFirestore::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                list.sortedBy { it.timestamp?.seconds ?: 0L }
            }
            .catch { e ->
                FirestoreErrorLogger.handleFirestoreError(e as Exception, OperationType.LIST, colRef.path)
                emit(emptyList())
            }
    }

    suspend fun saveChatMessage(message: ChatMessageFirestore): Result<Unit> {
        val docRef = db.collection("users").document(message.userId).collection("chat_messages").document(message.id)
        return try {
            docRef.set(message.toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            FirestoreErrorLogger.handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun clearChatMessages(userId: String): Result<Unit> {
        val colRef = db.collection("users").document(userId).collection("chat_messages")
        return try {
            val snapshot = colRef.get().await()
            for (doc in snapshot.documents) {
                doc.reference.delete().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            FirestoreErrorLogger.handleFirestoreError(e, OperationType.DELETE, colRef.path)
            Result.failure(e)
        }
    }
}
