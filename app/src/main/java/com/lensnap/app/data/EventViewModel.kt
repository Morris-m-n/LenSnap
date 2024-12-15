import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.util.Log
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.*

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

    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> get() = _events

    private val _isRefreshing = MutableLiveData<Boolean>()
    val isRefreshing: LiveData<Boolean> get() = _isRefreshing

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
    fun fetchEventImages(eventId: String, onSuccess: (List<String>) -> Unit, onError: (String) -> Unit) {
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
                onSuccess(eventImages)
            } catch (e: Exception) {
                Log.e("EventViewModel", "Failed to fetch event images: ${e.message}", e)
                onError(e.message ?: "Failed to fetch event images")
            }
        }
    }
    fun fetchEvents() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val documents = firestore.collection("events").get().await()
                val events = documents.map { document ->
                    document.toObject(Event::class.java)
                }
                _events.value = events
                Log.d("EventViewModel", "Fetched events successfully")
            } catch (e: Exception) {
                Log.e("EventViewModel", "Error fetching events: $e")
            } finally {
                _isRefreshing.value = false
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
                    imageUrl = eventImageUrl
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
