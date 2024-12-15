data class Event(
    val id: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val name: String = "",
    val date: String = "",
    val time: String = "",
    val location: String = "",
    val description: String = "",
    val imageUrl: String = "", // Cover image URL
    val pairingCode: String = "",
    val qrCodeUrl: String = "",
    val images: List<String> = listOf() // List of image URLs
)
