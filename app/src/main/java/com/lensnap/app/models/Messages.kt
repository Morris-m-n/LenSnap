package com.lensnap.app.models

//data class Message(
//    var id: String = "",
//    val chatId: String = "",
//    val senderId: String = "",
//    val receiverId: String = "",
//    val content: String = "",
//    val timestamp: Long = 0L,
//    val status: String = "sent", // Default status
//    val mediaUrl: String? = null // Field for media URL
//)

enum class MessageType {
    TEXT,
    MEDIA,
    EVENT
}

data class Message(
    var id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val content: String = "",
    val timestamp: Long = 0L,
    val status: String = "sent", // Default status
    val mediaUrl: String? = null, // Field for media URL
    val type: MessageType = MessageType.TEXT // Type of message
)
