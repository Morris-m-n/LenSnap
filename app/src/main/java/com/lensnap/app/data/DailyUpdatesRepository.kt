package com.lensnap.app.data

import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.ktx.storage
import com.lensnap.app.models.DailyUpdate
import com.lensnap.app.models.UserRegistration
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

class DailyUpdateRepository(private val userViewModel: UserViewModel) {

    private val db: FirebaseFirestore = Firebase.firestore
    private val storage: FirebaseStorage = Firebase.storage

    private fun getUserUpdatesRef(userId: String) =
        db.collection("users").document(userId).collection("dailyUpdates")

    private suspend fun uploadMedia(userId: String, uri: Uri): String {
        val fileName = uri.lastPathSegment ?: "media_${System.currentTimeMillis()}"
        val mediaRef = storage.reference.child("dailyUpdates/$userId/$fileName")
        mediaRef.putFile(uri).await()
        val downloadUrl = mediaRef.downloadUrl.await().toString()
        Log.d("DailyUpdateRepository", "Uploaded media to: dailyUpdates/$userId/$fileName")
        Log.d("DailyUpdateRepository", "Uploaded media URL: $downloadUrl")
        return downloadUrl
    }

    suspend fun addDailyUpdate(userId: String, update: DailyUpdate, uri: Uri, username: String, profilePhotoUrl: String) {
        val mediaUrl = uploadMedia(userId, uri)
        val updateWithMedia = update.copy(
            uri = mediaUrl,
            userId = userId, // Include userId
            username = username, // Include username
            profilePhotoUrl = profilePhotoUrl // Include profile photo URL
        )
        val updatesRef = getUserUpdatesRef(userId)
        val newUpdateRef = updatesRef.document()
        val updateWithId = updateWithMedia.copy(id = newUpdateRef.id)
        newUpdateRef.set(updateWithId).await()
    }

    suspend fun getFollowingDailyUpdates(followingUserIds: List<String>): List<DailyUpdate> = coroutineScope {
        val updatesRef = db.collectionGroup("dailyUpdates")
        Log.d("DailyUpdateRepository", "Fetching updates for user IDs: $followingUserIds")

        try {
            val querySnapshot = updatesRef.whereIn("userId", followingUserIds).get().await()
            Log.d("DailyUpdateRepository", "Query snapshot size: ${querySnapshot.size()}")

            val updates = querySnapshot.documents.mapNotNull { document ->
                Log.d("DailyUpdateRepository", "Document ID: ${document.id}, Data: ${document.data}")
                document.toObject(DailyUpdate::class.java)
            }
            Log.d("DailyUpdateRepository", "Fetched updates: ${updates.map { it.id }}")
            updates
        } catch (e: Exception) {
            Log.e("DailyUpdateRepository", "Error fetching daily updates: ", e)
            emptyList()
        }
    }

    suspend fun removeExpiredUpdates(userId: String) {
        val updatesRef = getUserUpdatesRef(userId)
        val currentTime = System.currentTimeMillis()
        val snapshot = updatesRef.get().await()
        for (document in snapshot.documents) {
            val update = document.toObject(DailyUpdate::class.java)
            if (update != null && currentTime - update.timestamp > 24 * 60 * 60 * 1000) {
                updatesRef.document(update.id).delete().await()
            }
        }
    }
}