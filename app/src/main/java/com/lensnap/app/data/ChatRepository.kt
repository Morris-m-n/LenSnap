package com.lensnap.app.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.lensnap.app.models.Chat
import com.lensnap.app.models.Message
import kotlinx.coroutines.tasks.await

class ChatRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val messagesCollection = firestore.collection("messages")

//    suspend fun getChatMessages(chatId: String): List<Message> {
//        val result = messagesCollection
//            .whereEqualTo("chatId", chatId)
//            .orderBy("timestamp")
//            .get()
//            .await()
//        return result.toObjects(Message::class.java)
//    }
//    suspend fun sendMessage(message: Message) {
//        try {
//            val senderId = message.senderId
//            val chatId = message.chatId
//
//            // Save the message in the sender's document
//            val senderMessageCollection = firestore.collection("users")
//                .document(senderId)
//                .collection("chats")
//                .document(chatId)
//                .collection("messages")
//
//            // Log adding message to Firestore for sender
//            Log.d("ChatRepository", "Adding message to Firestore for sender: $message")
//            senderMessageCollection.document(message.id).set(message).await()
//
//            // Save the message in the receiver's document
//            val receiverMessageCollection = firestore.collection("users")
//                .document(message.receiverId)
//                .collection("chats")
//                .document(chatId)
//                .collection("messages")
//
//            // Log adding message to Firestore for receiver
//            Log.d("ChatRepository", "Adding message to Firestore for receiver: $message")
//            receiverMessageCollection.document(message.id).set(message).await()
//
//            // After successfully adding the message, update the status
//            val deliveredMessage = message.copy(status = "delivered")
//            updateMessageStatus(deliveredMessage)
//        } catch (e: Exception) {
//            // Log error in sending message
//            val errorMessage = message.copy(status = "error")
//            Log.e("ChatRepository", "Error adding message to Firestore: $errorMessage", e)
//            updateMessageStatus(errorMessage)
//        }
//    }
//    suspend fun updateMessageStatus(message: Message) {
//        try {
//            val senderId = message.senderId
//            val chatId = message.chatId
//
//            // Update the message status in the sender's document
//            val senderMessageDocument = firestore.collection("users")
//                .document(senderId)
//                .collection("chats")
//                .document(chatId)
//                .collection("messages")
//                .document(message.id)
//
//            // Log updating message status for sender
//            Log.d("ChatRepository", "Updating message status for sender: $message")
//            senderMessageDocument.update("status", message.status).await()
//
//            // Update the message status in the receiver's document
//            val receiverMessageDocument = firestore.collection("users")
//                .document(message.receiverId)
//                .collection("chats")
//                .document(chatId)
//                .collection("messages")
//                .document(message.id)
//
//            // Log updating message status for receiver
//            Log.d("ChatRepository", "Updating message status for receiver: $message")
//            receiverMessageDocument.update("status", message.status).await()
//        } catch (e: Exception) {
//            Log.e("ChatRepository", "Error updating message status: $message", e)
//        }
//    }

    suspend fun createOrGetChat(currentUserId: String, receiverId: String): String {
        val chatId = if (currentUserId < receiverId) {
            "${currentUserId}_$receiverId"
        } else {
            "${receiverId}_$currentUserId"
        }

        Log.d("ChatRepository", "Generated chat ID: $chatId")

        val currentUserChatRef = firestore.collection("users")
            .document(currentUserId)
            .collection("chats")
            .document(chatId)

        val receiverUserChatRef = firestore.collection("users")
            .document(receiverId)
            .collection("chats")
            .document(chatId)

        if (!(currentUserChatRef.get().await().exists() && receiverUserChatRef.get().await().exists())) {
            val chat = Chat(
                id = chatId,
                userId = receiverId,
                lastMessage = "",
                timestamp = System.currentTimeMillis()
            )
            currentUserChatRef.set(chat).await()
            receiverUserChatRef.set(chat).await()
            Log.d("ChatRepository", "Created new chat with ID: $chatId")
        } else {
            Log.d("ChatRepository", "Chat already exists with ID: $chatId")
        }
        return chatId
    }
}
