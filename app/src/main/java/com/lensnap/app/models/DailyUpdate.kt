package com.lensnap.app.models

data class DailyUpdate(
    val id: String = "",
    val uri: String = "",
    val timestamp: Long = 0L,
    val aspectRatio: Float = 1.0f, // Default aspect ratio is 1:1
    val caption: String = "",
    val userId: String = "",
    val username: String = "", // Add the username field
    val profilePhotoUrl: String = "" // Add the profilePhotoUrl field
)
