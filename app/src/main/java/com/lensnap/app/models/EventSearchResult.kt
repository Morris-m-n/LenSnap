package com.lensnap.app.models

data class EventSearchResult(
    val id: String,
    val name: String,
    val location: String,
    val date: String,
    val description: String,
    val imageUrl: String,
    val images: List<String>
)
