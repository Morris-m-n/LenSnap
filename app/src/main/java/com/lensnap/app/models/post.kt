package com.lensnap.app.models

data class Post(
    val id: String = "",
    val userId: String = "",
    val mediaUrl: String = "",
    val mediaType: String = "", // "image" or "video"
    val caption: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val username: String = "",          // Add username field
    val profilePhotoUrl: String = "",    // Add profile photo URL field
    val aspectRatio: Float = 1f,         // Add aspect ratio field
    val likes: MutableList<String> = mutableListOf() // List of user IDs who liked the post
)

//package com.lensnap.app.models
//
//data class Post(
//    val id: String = "",
//    val userId: String = "",
//    val mediaUrl: String = "",
//    val mediaType: String = "", // "image" or "video"
//    val caption: String = "",
//    val timestamp: Long = System.currentTimeMillis(),
//    val username: String = "",          // Add username field
//    val profilePhotoUrl: String = ""    // Add profile photo URL field
//)
