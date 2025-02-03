package com.lensnap.app.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.lensnap.app.models.EventSearchResult
import com.lensnap.app.models.UserSearchResult
import kotlinx.coroutines.tasks.await

class SearchRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun searchUsers(query: String): List<UserSearchResult> {
        val results = mutableListOf<UserSearchResult>()

        return try {
            val snapshot = db.collection("users")
                .whereGreaterThanOrEqualTo("username", query)
                .whereLessThanOrEqualTo("username", query + '\uf8ff')
                .get()
                .await()

            for (document in snapshot.documents) {
                val username = document.getString("username") ?: ""
                val email = document.getString("email") ?: ""
                val profilePhotoUrl = document.getString("profilePhotoUrl") ?: ""
                results.add(UserSearchResult(username, email, profilePhotoUrl))
            }
            Log.d("SearchRepository", "User search results: ${results.size} found")
            results
        } catch (e: Exception) {
            Log.e("SearchRepository", "Error fetching user search results", e)
            emptyList()
        }
    }

    suspend fun searchEvents(query: String): List<EventSearchResult> {
        val results = mutableListOf<EventSearchResult>()

        return try {
            val snapshot = db.collection("events")
                .whereGreaterThanOrEqualTo("name", query)
                .whereLessThanOrEqualTo("name", query + '\uf8ff')
                .get()
                .await()

            for (document in snapshot.documents) {
                val id = document.getString("id") ?: ""
                val name = document.getString("name") ?: ""
                val location = document.getString("location") ?: ""
                val date = document.getString("date") ?: ""
                val description = document.getString("description") ?: ""
                val imageUrl = document.getString("imageUrl") ?: ""
                val images = document.get("images") as List<String>? ?: emptyList()
                results.add(EventSearchResult(id, name, location, date, description, imageUrl, images))
            }
            Log.d("SearchRepository", "Event search results: ${results.size} found")
            results
        } catch (e: Exception) {
            Log.e("SearchRepository", "Error fetching event search results", e)
            emptyList()
        }
    }
}
