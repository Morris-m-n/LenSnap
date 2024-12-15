package com.lensnap.app.models

import com.google.firebase.Timestamp

data class Comment(
    val userId: String = "",
    val username: String = "", // Add username field
    val comment: String = "",
    val timestamp: Timestamp = Timestamp.now()
)
