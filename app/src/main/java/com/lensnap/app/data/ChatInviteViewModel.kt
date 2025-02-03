package com.lensnap.app.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.lensnap.app.models.ChatInvite

class ChatInviteViewModel {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun sendChatInvite(senderId: String, receiverId: String) {
        val inviteId = firestore.collection("chatInvites").document().id
        val chatInvite = ChatInvite(id = inviteId, senderId = senderId, receiverId = receiverId, timestamp = System.currentTimeMillis())

        firestore.collection("chatInvites")
            .document(inviteId)
            .set(chatInvite)
            .addOnSuccessListener {
                Log.d("ChatInviteViewModel", "Chat invite sent successfully")
            }
            .addOnFailureListener { e ->
                Log.e("ChatInviteViewModel", "Error sending chat invite", e)
            }
    }

    fun getChatInvites(userId: String): LiveData<List<ChatInvite>> {
        val invitesLiveData = MutableLiveData<List<ChatInvite>>()
        firestore.collection("chatInvites")
            .whereEqualTo("receiverId", userId)
            .whereEqualTo("status", "pending")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Log.e("ChatInviteViewModel", "Error getting chat invites", firebaseFirestoreException)
                    return@addSnapshotListener
                }

                if (querySnapshot != null) {
                    val invites = querySnapshot.documents.mapNotNull { document ->
                        val id = document.getString("id")
                        val senderId = document.getString("senderId")
                        val receiverId = document.getString("receiverId")
                        val timestamp = document.getLong("timestamp")
                        val status = document.getString("status")

                        if (id != null && senderId != null && receiverId != null && timestamp != null && status != null) {
                            ChatInvite(id, senderId, receiverId, timestamp, status)
                        } else {
                            Log.e("ChatInviteViewModel", "Missing field in document ${document.id}")
                            null
                        }
                    }
                    invitesLiveData.postValue(invites)
                }
            }
        return invitesLiveData
    }

    fun acceptChatInvite(inviteId: String, receiverId: String, senderId: String) {
        firestore.collection("chatInvites")
            .document(inviteId)
            .update("status", "accepted")
            .addOnSuccessListener {
                Log.d("ChatInviteViewModel", "Chat invite accepted")
                // Add logic to follow each other or enable messaging between users
            }
            .addOnFailureListener { e ->
                Log.e("ChatInviteViewModel", "Error accepting chat invite", e)
            }
    }

    fun rejectChatInvite(inviteId: String) {
        firestore.collection("chatInvites")
            .document(inviteId)
            .update("status", "rejected")
            .addOnSuccessListener {
                Log.d("ChatInviteViewModel", "Chat invite rejected")
            }
            .addOnFailureListener { e ->
                Log.e("ChatInviteViewModel", "Error rejecting chat invite", e)
            }
    }
}
