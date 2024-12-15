package com.lensnap.app.models

data class UserRegistration(
    val id: String,
    val email: String,
    val username: String,
    val profilePhotoUrl: String? = null,
    val followers: List<String> = emptyList(), // List of user IDs who follow this user
    val following: List<String> = emptyList() // List of user IDs this user is following
) {
    // No-argument constructor
    constructor() : this("", "", "", null)
}
