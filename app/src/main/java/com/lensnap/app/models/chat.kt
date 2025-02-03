package com.lensnap.app.models

data class Chat(
    val id: String = "",
    val userId: String = "", // Sender
    val receiverId: String = "", // Receiver
    val lastMessage: String = "",
    val timestamp: Long = 0L,
    val unread: Boolean = false,
    val unreadCount: Int = 0
)
