import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.lensnap.app.models.Comment
import com.lensnap.app.ui.theme.screens.dashboard.convertToImageData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

class EventViewModel(private val context: Context) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _eventName = MutableLiveData<String>()
    val eventName: LiveData<String> get() = _eventName

    private val _eventDescription = MutableLiveData<String>()
    val eventDescription: LiveData<String> get() = _eventDescription

    private val _eventLocation = MutableLiveData<String>()
    val eventLocation: LiveData<String> get() = _eventLocation

    private val _eventImageUri = MutableLiveData<Uri?>()
    val eventImageUri: LiveData<Uri?> get() = _eventImageUri

    private val _eventImageBitmap = MutableLiveData<Bitmap?>()
    val eventImageBitmap: LiveData<Bitmap?> get() = _eventImageBitmap

    private val _eventDate = MutableLiveData<String>()
    val eventDate: LiveData<String> get() = _eventDate

    private val _eventTime = MutableLiveData<String>()
    val eventTime: LiveData<String> get() = _eventTime

    private val _upcomingEvents = MutableStateFlow<List<Event>>(emptyList())
    val upcomingEvents: StateFlow<List<Event>> get() = _upcomingEvents

    fun updateEventName(name: String) {
        _eventName.value = name
    }

    fun updateEventDescription(description: String) {
        _eventDescription.value = description
    }

    fun updateEventLocation(location: String) {
        _eventLocation.value = location
    }

    fun updateEventImageUri(uri: Uri?) {
        _eventImageUri.value = uri
    }

    fun updateEventImageBitmap(bitmap: Bitmap?) {
        _eventImageBitmap.value = bitmap
    }

    fun updateEventDate(date: String) {
        _eventDate.value = date
    }

    fun updateEventTime(time: String) {
        _eventTime.value = time
    }

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _isRefreshing = MutableLiveData<Boolean>()
    val isRefreshing: LiveData<Boolean> get() = _isRefreshing


    private var lastDocumentSnapshot: DocumentSnapshot? = null // Tracks the last document
    private var isLoadingMore = false // Prevents multiple simultaneous loads

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events

    private val _photos = MutableLiveData<List<String>>(emptyList())
    val photos: LiveData<List<String>> = _photos

    private val _qrBitmap = MutableLiveData<Bitmap?>()
    val qrBitmap: LiveData<Bitmap?> = _qrBitmap

    private val _pairingCode = MutableLiveData<String?>()
    val pairingCode: LiveData<String?> = _pairingCode

    private fun generatePairingCode(): String {
        return UUID.randomUUID().toString().substring(0, 8)
    }

    fun generateQrCode(pairingCode: String): Bitmap {
        val content = "pairingCode:$pairingCode" // Include pairing code in the content
        Log.d("GenerateQrCode", "Generating QR Code with content: $content")

        val size = 250 // Dimensions for the QR code
        val qrCodeWriter = QRCodeWriter()
        return try {
            val bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, size, size)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: WriterException) {
            e.printStackTrace()
            Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        }
    }
    private suspend fun uploadQrCodeToFirebase(bitmap: Bitmap, fileName: String): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val data = baos.toByteArray()
        val storageRef = FirebaseStorage.getInstance().reference.child("qrcodes/$fileName.png")

        // Upload the QR code to Firebase
        storageRef.putBytes(data).await()

        // Get the download URL for the uploaded QR code
        val qrCodeUrl = storageRef.downloadUrl.await().toString()

        // Logging the QR code content and upload URL for debugging
        Log.d("UploadQrCode", "QR Code Content: $fileName")
        Log.d("UploadQrCode", "Upload URL: $qrCodeUrl")

        return qrCodeUrl
    }

    fun fetchUserEvents(userId: String) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val querySnapshot = firestore.collection("events")
                    .whereEqualTo("creatorId", userId)
                    .get()
                    .await()
                val userEvents = querySnapshot.documents.mapNotNull { document ->
                    document.toObject(Event::class.java)
                }
                _events.value = userEvents
                Log.d("EventViewModel", "Fetched user events successfully")
            } catch (e: Exception) {
                Log.e("EventViewModel", "Error fetching user events: $e")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun fetchEventImages(eventId: String, onSuccess: (SnapshotStateList<ImageData>) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                Log.d("EventViewModel", "Attempting to fetch images for eventId: $eventId")

                if (eventId.isBlank()) {
                    onError("Event ID cannot be blank")
                    return@launch
                }

                val eventDocument = firestore.collection("events").document(eventId).get().await()
                val eventImages = mutableListOf<String>()

                val eventImageUrl = eventDocument.getString("imageUrl")
                eventImageUrl?.let { eventImages.add(it) }

                val imagesArray = eventDocument.get("images") as? List<String>
                imagesArray?.let { eventImages.addAll(it) }

                Log.d("EventViewModel", "Fetched event images: $eventImages")
                val imageDataList = convertToImageData(eventImages)
                onSuccess(imageDataList)
            } catch (e: Exception) {
                Log.e("EventViewModel", "Failed to fetch event images: ${e.message}", e)
                onError(e.message ?: "Failed to fetch event images")
            }
        }
    }

    fun fetchUpcomingEventsByDate() {
        // Fetch events from Firestore and filter by upcoming date and public status
        viewModelScope.launch {
            try {
                val currentDate = System.currentTimeMillis() // Get the current timestamp
                Log.d("EventViewModel", "Current timestamp: $currentDate")

                val documents = firestore.collection("events")
                    .whereEqualTo("status", "public") // Only public events
                    .get().await()

                Log.d("EventViewModel", "Fetched ${documents.size()} events from Firestore")

                val upcomingEvents = documents.mapNotNull { document ->
                    val event = document.toObject(Event::class.java)

                    try {
                        // Parse event date into a timestamp
                        val eventDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(event.date)?.time


                        // Log event details
                        Log.d(
                            "EventViewModel",
                            "Event fetched: ${event.name}, Date: ${event.date}, Image URL: ${event.imageUrl}, Parsed Date: $eventDate"
                        )

                        // Only include events that are in the future
                        eventDate?.takeIf { it > currentDate }?.let {
                            Log.d("EventViewModel", "Event is upcoming: ${event.name}, Timestamp: $eventDate")
                            event
                        }
                    } catch (e: Exception) {
                        Log.e("EventViewModel", "Error parsing date for event: ${event.name}, Exception: $e")
                        null
                    }
                }

                Log.d("EventViewModel", "Filtered ${upcomingEvents.size} upcoming events")
                _upcomingEvents.value = upcomingEvents
            } catch (e: Exception) {
                Log.e("EventViewModel", "Error fetching upcoming events: $e")
            }
        }
    }

    fun createEvent(
        event: Event,
        capturedImages: List<Bitmap>,
        galleryImages: List<Uri>,
        onSuccess: (String, String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                Log.d("EventViewModel", "Starting event creation")
                val pairingCode = generatePairingCode()
                val qrCodeContent = "pairingCode:$pairingCode"
                val qrCodeBitmap = generateQrCode(qrCodeContent)
                val qrCodeUrl = uploadQrCodeToFirebase(qrCodeBitmap, pairingCode)
                Log.d("EventViewModel", "QR Code uploaded with URL: $qrCodeUrl")

                // Assuming event.imageUrl is initially the URI to the image
                val eventImageBitmap = event.imageUrl.takeIf { it.isNotBlank() }?.let { Uri.parse(it) }?.let { getImageBitmapFromUri(it) }
                val eventImageUrl = eventImageBitmap?.let {
                    Log.d("EventViewModel", "Uploading event image to storage")
                    uploadEventImageToStorage(it, pairingCode)
                } ?: event.imageUrl
                Log.d("EventViewModel", "Event image URL: $eventImageUrl")

                val newEventRef = firestore.collection("events").document()
                val eventId = newEventRef.id
                val updatedEvent = event.copy(
                    id = eventId,
                    pairingCode = pairingCode,
                    qrCodeUrl = qrCodeUrl,
                    imageUrl = eventImageUrl,
                    creatorId = event.creatorId,  // Ensure this field is included
                    status = event.status  // Ensure this field is included
                )

                newEventRef.set(updatedEvent).await()
                Log.d("EventViewModel", "Event document created with ID: $eventId and Pairing Code: $pairingCode")

                // Upload captured images
                if (capturedImages.isNotEmpty()) {
                    uploadCapturedImages(eventId, capturedImages, onSuccess = {
                        Log.d("EventViewModel", "Captured images uploaded successfully")
                    }, onError = { error ->
                        Log.e("EventViewModel", "Error uploading captured images: $error")
                    })
                }

                // Upload gallery images
                if (galleryImages.isNotEmpty()) {
                    uploadGalleryImages(eventId, galleryImages, onSuccess = {
                        Log.d("EventViewModel", "Gallery images uploaded successfully")
                    }, onError = { error ->
                        Log.e("EventViewModel", "Error uploading gallery images: $error")
                    })
                }

                onSuccess(pairingCode, qrCodeUrl)
            } catch (e: Exception) {
                Log.e("EventViewModel", "Failed to create event", e)
                onError("Failed to create event: ${e.message}")
            }
        }
    }

    // Method to fetch paginated events
    fun fetchEvents(limit: Long = 10) {
        if (isLoadingMore) return // Prevent double-loading
        isLoadingMore = true
        _isRefreshing.value = true // Set refreshing state

        viewModelScope.launch {
            try {
                val query = firestore.collection("events")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(limit)

                val finalQuery = lastDocumentSnapshot?.let { query.startAfter(it) } ?: query
                val documents = finalQuery.get().await()

                val newEvents = documents.map { it.toObject(Event::class.java) }
                lastDocumentSnapshot = documents.documents.lastOrNull() // Update the last snapshot
                _events.value = _events.value + newEvents // Append new events to the existing list

                Log.d("EventViewModel", "Fetched ${newEvents.size} events successfully")
            } catch (e: Exception) {
                Log.e("EventViewModel", "Error fetching events: $e")
            } finally {
                isLoadingMore = false
                _isRefreshing.value = false // Reset refreshing state
            }
        }
    }

    // Fetch event by ID
    fun getEventById(eventId: String, onSuccess: (Event) -> Unit, onError: (String) -> Unit) {
        if (eventId.isEmpty()) {
            onError("Invalid event ID")
            return
        }

        viewModelScope.launch {
            try {
                val eventRef = firestore.collection("events").document(eventId)
                val documentSnapshot = eventRef.get().await()

                if (documentSnapshot.exists()) {
                    val event = documentSnapshot.toObject(Event::class.java)
                    event?.let {
                        onSuccess(it)
                        Log.d("EventViewModel", "Fetched event: $event")
                    } ?: onError("Event not found")
                } else {
                    onError("Event not found")
                }
            } catch (e: Exception) {
                Log.e("EventViewModel", "Failed to get event by ID $eventId: ${e.message}", e)
                onError("Failed to fetch event: ${e.message}")
            }
        }
    }

    fun fetchEventPairingCode(eventCode: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("events").document(eventCode).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val pairingCode = document.getString("pairingCode")
                    _pairingCode.value = pairingCode
                } else {
                    _pairingCode.value = null
                }
            }
            .addOnFailureListener {
                _pairingCode.value = null
            }
    }

    fun fetchEventPhotos(eventCode: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("events").document(eventCode).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val photoUrls = document.get("photoUrls") as? List<String> ?: emptyList()
                    _photos.value = photoUrls // Update LiveData or State
                } else {
                    _photos.value = emptyList()
                }
            }
            .addOnFailureListener {
                _photos.value = emptyList()
            }
    }

    fun fetchEventQRCode(eventCode: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("events").document(eventCode).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val qrCodeUrl = document.getString("qrCodeUrl")
                    if (!qrCodeUrl.isNullOrEmpty()) {
                        loadBitmapFromUrl(qrCodeUrl)
                    } else {
                        _qrBitmap.value = null
                    }
                } else {
                    _qrBitmap.value = null
                }
            }
            .addOnFailureListener {
                _qrBitmap.value = null
            }
    }

    private fun loadBitmapFromUrl(url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val bitmap = BitmapFactory.decodeStream(URL(url).openStream())
                withContext(Dispatchers.Main) {
                    _qrBitmap.value = bitmap
                }
            } catch (e: Exception) {
                _qrBitmap.value = null
            }
        }
    }

    // Update event details
    fun updateEvent(
        event: Event,
        bitmap: Bitmap?, // Optional bitmap for a new image
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Upload image if a new one is provided
                val imageUrl = if (bitmap != null) {
                    uploadEventImageToStorage(bitmap, event.id) // Reuse the image upload logic
                } else {
                    event.imageUrl // Use the existing image URL if no new image is uploaded
                }

                // Update the event in Firestore
                val updatedEvent = event.copy(imageUrl = imageUrl)
                val eventRef = firestore.collection("events").document(updatedEvent.id)
                eventRef.update(
                    "name", updatedEvent.name,
                    "description", updatedEvent.description,
                    "imageUrl", updatedEvent.imageUrl,
                    "date", updatedEvent.date,
                    "time", updatedEvent.time,
                    "location", updatedEvent.location,
                    "status", updatedEvent.status
                ).await()

                onSuccess()
                Log.d("EventViewModel", "Event updated successfully")
            } catch (e: Exception) {
                Log.e("EventViewModel", "Failed to update event: ${e.message}", e)
                onError("Failed to update event: ${e.message}")
            }
        }
    }

    fun deleteEvent(eventId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val eventRef = firestore.collection("events").document(eventId)
                eventRef.delete().await()

                onSuccess()
                Log.d("EventViewModel", "Event deleted successfully")
            } catch (e: Exception) {
                Log.e("EventViewModel", "Failed to delete event: ${e.message}", e)
                onError(e.message ?: "Failed to delete event")
            }
        }
    }


    private fun getImageBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            Log.d("EventViewModel", "Retrieving image bitmap from URI: $uri")
            val inputStream = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            Log.e("EventViewModel", "Failed to retrieve image bitmap", e)
            null
        }
    }

    private suspend fun uploadEventImageToStorage(bitmap: Bitmap, pairingCode: String): String {
        return try {
            Log.d("EventViewModel", "Starting image upload to storage")
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
            val data = baos.toByteArray()
            val storageRef = storage.reference.child("event_images/$pairingCode.png")
            val uploadTask = storageRef.putBytes(data).await()
            Log.d("EventViewModel", "Image upload completed. Upload task: $uploadTask")
            val downloadUrl = storageRef.downloadUrl.await().toString()
            Log.d("EventViewModel", "Download URL: $downloadUrl")
            downloadUrl
        } catch (e: Exception) {
            Log.e("EventViewModel", "Failed to upload event image", e)
            throw e
        }
    }

//     New functions for uploading images and saving URLs to Firestore

    private suspend fun uploadImageToStorage(uri: Uri, eventId: String): String {
        val storageRef = storage.reference.child("events/$eventId/${UUID.randomUUID()}.png")
        storageRef.putFile(uri).await()
        return storageRef.downloadUrl.await().toString()
    }

    private suspend fun uploadImageToStorage(bitmap: Bitmap, eventId: String): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val data = baos.toByteArray()
        val storageRef = storage.reference.child("events/$eventId/${UUID.randomUUID()}.png")
        storageRef.putBytes(data).await()
        return storageRef.downloadUrl.await().toString()
    }

    private fun addImageUrlsToFirestore(eventId: String, imageUrls: List<String>, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val eventRef = firestore.collection("events").document(eventId)
        eventRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                eventRef.update("images", FieldValue.arrayUnion(*imageUrls.toTypedArray()))
                    .addOnSuccessListener {
                        Log.d("EventViewModel", "Image URLs added to Firestore: $imageUrls")
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        Log.e("EventViewModel", "Failed to add image URLs to Firestore: ${e.message}", e)
                        onError(e.message ?: "Failed to add image URLs")
                    }
            } else {
                Log.e("EventViewModel", "Event document does not exist")
                onError("Event document does not exist")
            }
        }.addOnFailureListener { e ->
            Log.e("EventViewModel", "Failed to check if event document exists: ${e.message}", e)
            onError(e.message ?: "Failed to check if event document exists")
        }
    }

    fun uploadCapturedImages(eventId: String, capturedImages: List<Bitmap>, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val imageUrls = capturedImages.map { bitmap ->
                    uploadImageToStorage(bitmap, eventId)
                }
                addImageUrlsToFirestore(eventId, imageUrls, onSuccess, onError)
            } catch (e: Exception) {
                Log.e("EventViewModel", "Failed to upload captured images: ${e.message}", e)
                onError(e.message ?: "Failed to upload captured images")
            }
        }
    }

    fun uploadGalleryImages(eventId: String, galleryImages: List<Uri>, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val imageUrls = galleryImages.map { uri ->
                    uploadImageToStorage(uri, eventId)
                }
                addImageUrlsToFirestore(eventId, imageUrls, onSuccess, onError)
            } catch (e: Exception) {
                Log.e("EventViewModel", "Failed to upload gallery images: ${e.message}", e)
                onError(e.message ?: "Failed to upload gallery images")
            }
        }
    }

    fun hasUserLiked(eventId: String, userId: String, onSuccess: (Boolean) -> Unit, onError: (String) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val likeRef = db.collection("events").document(eventId).collection("likes").document(userId)

    likeRef.get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                onSuccess(true)
            } else {
                onSuccess(false)
            }
        }
        .addOnFailureListener { e ->
            Log.e("EventViewModel", "Failed to check if user liked: ${e.message}", e)
            onError(e.message ?: "Failed to check if user liked")
        }
}


    fun addLike(eventId: String, userId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val likeRef = db.collection("events").document(eventId).collection("likes").document(userId)

        likeRef.set(mapOf("userId" to userId))
            .addOnSuccessListener {
                Log.d("EventViewModel", "Like added successfully")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("EventViewModel", "Failed to add like: ${e.message}", e)
                onError(e.message ?: "Failed to add like")
            }
    }


    fun removeLike(eventId: String, userId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val likeRef = db.collection("events").document(eventId).collection("likes").document(userId)

        likeRef.delete()
            .addOnSuccessListener {
                Log.d("EventViewModel", "Like removed successfully")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("EventViewModel", "Failed to remove like: ${e.message}", e)
                onError(e.message ?: "Failed to remove like")
            }
    }
    fun getLikesCount(eventId: String, onSuccess: (Int) -> Unit, onError: (String) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val likesRef = db.collection("events").document(eventId).collection("likes")

        likesRef.get()
            .addOnSuccessListener { result ->
                val likeCount = result.size()
                onSuccess(likeCount)
            }
            .addOnFailureListener { e ->
                Log.e("EventViewModel", "Failed to retrieve likes count: ${e.message}", e)
                onError(e.message ?: "Failed to retrieve likes count")
            }
    }
    fun addComment(eventId: String, userId: String, username: String, comment: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val commentsRef = db.collection("events").document(eventId).collection("comments").document()

        val commentData = mapOf(
            "userId" to userId,
            "username" to username, // Include username
            "comment" to comment,
            "timestamp" to Timestamp.now()
        )

        commentsRef.set(commentData)
            .addOnSuccessListener {
                Log.d("EventViewModel", "Comment added successfully")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("EventViewModel", "Failed to add comment: ${e.message}", e)
                onError(e.message ?: "Failed to add comment")
            }
    }

    fun getComments(eventId: String, onSuccess: (List<Comment>) -> Unit, onError: (String) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val commentsRef = db.collection("events").document(eventId).collection("comments")

        commentsRef.orderBy("timestamp", Query.Direction.ASCENDING).get()
            .addOnSuccessListener { result ->
                val comments = result.map { document ->
                    document.toObject(Comment::class.java)
                }
                onSuccess(comments)
            }
            .addOnFailureListener { e ->
                Log.e("EventViewModel", "Failed to retrieve comments: ${e.message}", e)
                onError(e.message ?: "Failed to retrieve comments")
            }
    }

    class EventViewModelFactory(
        private val context: Context
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EventViewModel::class.java)) {
                return EventViewModel(context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
