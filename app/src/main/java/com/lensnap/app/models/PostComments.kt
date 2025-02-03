package com.lensnap.app.models

data class PostComments(
    val id: String = "",
    val postId: String = "",
    val userId: String = "",
    val username: String = "",
    val profilePhotoUrl: String = "",
    val commentText: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

