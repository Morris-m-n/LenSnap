package com.lensnap.app.models

data class UserRegistration(
    val id: String,
    val email: String,
    val username: String,
    val profilePhotoUrl: String? = null,
    val followers: List<String> = emptyList(), // List of user IDs who follow this user
    val following: List<String> = emptyList(), // List of user IDs this user is following
    val chats: Map<String, String> = emptyMap(), // Map of chat IDs to user IDs or usernames
    val isOnline: Boolean = false, // New field for online status
    val isTyping: Boolean = false, // New field for typing status
    val lastActive: Long = 0L // New field for last active time (timestamp)
) {
    // No-argument constructor
    constructor() : this("", "", "", null)
}

//package com.lensnap.app.models
//
//data class UserRegistration(
//    val id: String,
//    val email: String,
//    val username: String,
//    val profilePhotoUrl: String? = null,
//    val followers: List<String> = emptyList(), // List of user IDs who follow this user
//    val following: List<String> = emptyList(), // List of user IDs this user is following
//    val chats: Map<String, String> = emptyMap(), // Map of chat IDs to user IDs or usernames
//    val isOnline: Boolean = false, // New field for online status
//    val isTyping: Boolean = false, // New field for typing status
//    val lastActive: Long = 0L, // New field for last active time (timestamp)
//    val selectedTopics: List<String> = emptyList() // List of topics of interest selected by the user
//) {
//    // No-argument constructor
//    constructor() : this("", "", "", null)
//}
