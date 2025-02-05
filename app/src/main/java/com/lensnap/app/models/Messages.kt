package com.lensnap.app.models

//enum class MessageType {
//    TEXT,
//    MEDIA,
//    EVENT
//}
//
//data class Message(
//    var id: String = "",
//    val chatId: String = "",
//    val senderId: String = "",
//    val receiverId: String = "",
//    val content: String = "",
//    val timestamp: Long = 0L,
//    val status: String = "sent", // Default status
//    val mediaUrl: String? = null, // Field for media URL
//    val type: MessageType = MessageType.TEXT // Type of message
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
    val type: MessageType = MessageType.TEXT, // Type of message
    val eventId: String? = null, // Event ID for event messages
    val eventName: String? = null // Event name for event messages
)
