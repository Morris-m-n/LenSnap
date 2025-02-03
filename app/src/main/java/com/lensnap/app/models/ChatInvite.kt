package com.lensnap.app.models

data class ChatInvite(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val timestamp: Long = 0L,
    val status: String = "pending" // default to pending
)
