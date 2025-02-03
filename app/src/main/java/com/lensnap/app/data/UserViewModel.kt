package com.lensnap.app.data

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.lensnap.app.models.Chat
import com.lensnap.app.models.Message
import com.lensnap.app.models.Post
import com.lensnap.app.models.UserRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import androidx.navigation.NavController
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.QuerySnapshot
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.lensnap.app.models.PostComments
import kotlinx.coroutines.CompletableDeferred
import androidx.compose.runtime.State
import android.os.Environment
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import java.io.File
import java.nio.ByteBuffer

class UserViewModel(private val context: Context) : ViewModel() {
//    private val _mediaUri = MutableLiveData<Uri?>()
    private val chatRepository = ChatRepository()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    private val sharedPreferences: SharedPreferences =
    context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)

    private val _currentUser = MutableLiveData<UserRegistration?>()
    val currentUser: LiveData<UserRegistration?> get() = _currentUser

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _userChats = MutableLiveData<List<Chat>>()
    val userChats: LiveData<List<Chat>> = _userChats

    private val _followedUsers = MutableLiveData<List<UserRegistration>>()
    val followedUsers: LiveData<List<UserRegistration>> = _followedUsers

    private val _usersToFollowBack = MutableLiveData<List<UserRegistration>>()
    val usersToFollowBack: LiveData<List<UserRegistration>> = _usersToFollowBack

    private val _peopleYouMayKnow = MutableLiveData<List<UserRegistration>>()
    val peopleYouMayKnow: LiveData<List<UserRegistration>> = _peopleYouMayKnow

    private val postRepository = PostRepository()

    init {
        checkIfUserIsLoggedIn()
    }

    fun checkIfUserIsLoggedIn() {
        val userId = getStoredUserId()
        Log.d("UserViewModel", "Checking if user is logged in. Stored user ID: $userId")
        userId?.let {
            fetchCurrentUser(it)
        }
    }


    fun getStoredUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid ?: sharedPreferences.getString("USER_ID", null)
    }

    fun storeUserId(userId: String) {
        sharedPreferences.edit().putString("USER_ID", userId).apply()
        Log.d("UserViewModel", "User ID stored: $userId")
    }
//    fun fetchCurrentUser(userId: String) {
//        Log.d("UserViewModel", "Fetching current user with ID: $userId")
//        db.collection("users").document(userId).get()
//            .addOnSuccessListener { document ->
//                _currentUser.value = document.toObject(UserRegistration::class.java)
//                Log.d("UserViewModel", "User data fetched successfully")
//            }
//            .addOnFailureListener { exception ->
//                Log.e("UserViewModel", "Error fetching user data", exception)
//            }
//    }

    fun fetchCurrentUser(userId: String) {
        Log.d("UserViewModel", "Fetching current user with ID: $userId")
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                _currentUser.value = document.toObject(UserRegistration::class.java)
                Log.d("UserViewModel", "User data fetched successfully")
            }
            .addOnFailureListener { exception ->
                Log.e("UserViewModel", "Error fetching user data", exception)
                _currentUser.value = null
            }
    }

    fun signUp(
        email: String,
        password: String,
        username: String,
        profileImageUri: Uri?,
        onResult: (Boolean, String?) -> Unit
    ) {
        if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
            onResult(false, "Please fill in all fields")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        val userId = user?.uid ?: return@addOnCompleteListener

                        viewModelScope.launch {
                            val profilePhotoUrl = profileImageUri?.let { uploadProfileImage(userId, it) }

                            val newUser = mapOf(
                                "id" to userId,
                                "email" to email,
                                "username" to username,
                                "profilePhotoUrl" to profilePhotoUrl
                            )

                            db.collection("users").document(userId).set(newUser)
                                .addOnSuccessListener {
                                    onResult(true, null)
                                    storeUserId(userId)  // Store user ID after successful sign-up
                                    fetchCurrentUser(userId)
                                    _isLoading.value = false
                                    Log.d("UserViewModel", "User signed up and data saved successfully")
                                }
                                .addOnFailureListener { exception ->
                                    onResult(false, exception.message)
                                    _isLoading.value = false
                                    Log.e("UserViewModel", "Error saving user data", exception)
                                }
                        }
                    } else {
                        onResult(false, task.exception?.message)
                        _isLoading.value = false
                        Log.e("UserViewModel", "Error creating user", task.exception)
                    }
                }
        }
    }

    private suspend fun uploadProfileImage(userId: String, imageUri: Uri): String {
        val storageRef = FirebaseStorage.getInstance().reference.child("profile_images/$userId.jpg")
        return withContext(Dispatchers.IO) {
            storageRef.putFile(imageUri).await() // Wait for the file to be uploaded
            storageRef.downloadUrl.await().toString() // Get the download URL
        }
    }
    fun signIn(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        if (email.isEmpty() || password.isEmpty()) {
            onResult(false, "Please fill in both email and password")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid
                        Log.d("UserViewModel", "User ID retrieved after sign-in: $userId")

                        // Store userId in SharedPreferences
                        if (userId != null) {
                            sharedPreferences.edit().putString("USER_ID", userId).apply()
                            Log.d("UserViewModel", "User ID stored in SharedPreferences: $userId")

                            // Fetch current user details
                            fetchCurrentUser(userId)
                        } else {
                            Log.e("UserViewModel", "User ID is null after successful sign-in")
                        }
                    } else {
                        Log.e("UserViewModel", "Sign-in failed: ${task.exception?.message}")
                    }

                    onResult(task.isSuccessful, task.exception?.message)
                    _isLoading.value = false
                    Log.d("UserViewModel", "Sign-in result: ${task.isSuccessful}, ${task.exception?.message}")
                }
        }
    }

    fun signOut() {
        auth.signOut()
        sharedPreferences.edit().remove("USER_ID").apply()
        _currentUser.value = null
        Log.d("UserViewModel", "User signed out")
    }
    class UserViewModelFactory(
        private val context: Context
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
                return UserViewModel(context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    fun fetchCurrentUser() {
        auth.currentUser?.uid?.let { userId ->
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        _currentUser.value = document.toObject(UserRegistration::class.java)
                    } else {
                        _currentUser.value = null
                    }
                }
                .addOnFailureListener { _currentUser.value = null }
        }
    }

    fun getCurrentUser(): UserRegistration? {
        return _currentUser.value
    }

    fun updateUser(user: UserRegistration) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).set(user)
        _currentUser.value = user
    }

    fun deleteUser() {
        auth.currentUser?.uid?.let { userId ->
            db.collection("users").document(userId).delete()
            auth.currentUser?.delete()
            _currentUser.value = null
        }
    }

    fun uploadProfilePhoto(
        bitmap: Bitmap,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
                val storageRef = storage.reference.child("profile_photos/$userId.jpg")

                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()

                storageRef.putBytes(data).await()

                val downloadUrl = storageRef.downloadUrl.await().toString()
                updateProfilePhotoUrl(userId, downloadUrl, onSuccess, onError)
                _isLoading.value = false
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error")
                _isLoading.value = false
            }
        }
    }

    private fun updateProfilePhotoUrl(
        userId: String,
        photoUrl: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val userRef = db.collection("users").document(userId)
        userRef.update("profilePhotoUrl", photoUrl)
            .addOnSuccessListener { onSuccess(photoUrl) }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to update profile photo URL")
            }
    }
    fun followUser(targetUserId: String) {
        val currentUserId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            val currentUserRef = db.collection("users").document(currentUserId)
            val targetUserRef = db.collection("users").document(targetUserId)

            db.runBatch { batch ->
                batch.update(currentUserRef, "following", FieldValue.arrayUnion(targetUserId))
                batch.update(targetUserRef, "followers", FieldValue.arrayUnion(currentUserId))
            }.addOnSuccessListener {
                fetchCurrentUser()
                fetchUserById(targetUserId) { updatedUser ->
                    // handle the updated user if needed
                }
            }.addOnFailureListener { e ->
                Log.e("UserViewModel", "Error following user: ", e)
            }
        }
    }

    fun unfollowUser(targetUserId: String) {
        val currentUserId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            val currentUserRef = db.collection("users").document(currentUserId)
            val targetUserRef = db.collection("users").document(targetUserId)

            db.runBatch { batch ->
                batch.update(currentUserRef, "following", FieldValue.arrayRemove(targetUserId))
                batch.update(targetUserRef, "followers", FieldValue.arrayRemove(currentUserId))
            }.addOnSuccessListener {
                fetchCurrentUser()
                fetchUserById(targetUserId) { updatedUser ->
                    // handle the updated user if needed
                }
            }.addOnFailureListener { e ->
                Log.e("UserViewModel", "Error unfollowing user: ", e)
            }
        }
    }
    fun fetchFollowers(userId: String, onResult: (List<UserRegistration>) -> Unit) {
        // Fetch users who have the given user in their 'following' array
        db.collection("users").whereArrayContains("following", userId).get()
            .addOnSuccessListener { documents ->
                val followers = documents.mapNotNull { it.toObject(UserRegistration::class.java) }
                onResult(followers)
            }
            .addOnFailureListener { e ->
                Log.e("UserViewModel", "Error fetching followers: ", e)
                onResult(emptyList())
            }
    }

    fun fetchFollowing(userId: String, onResult: (List<UserRegistration>) -> Unit) {
        // Fetch the 'following' array of the given user directly
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val followingIds = document["following"] as? List<String> ?: emptyList()
                    if (followingIds.isNotEmpty()) {
                        // Fetch details of users in the 'following' list
                        val tasks = followingIds.map { id ->
                            db.collection("users").document(id).get()
                        }

                        Tasks.whenAllSuccess<DocumentSnapshot>(tasks)
                            .addOnSuccessListener { snapshots ->
                                val followingUsers = snapshots.mapNotNull { it.toObject(UserRegistration::class.java) }
                                onResult(followingUsers)
                            }
                            .addOnFailureListener { e ->
                                Log.e("UserViewModel", "Error fetching following users: ", e)
                                onResult(emptyList())
                            }
                    } else {
                        onResult(emptyList())
                    }
                } else {
                    Log.w("UserViewModel", "User document does not exist for ID: $userId")
                    onResult(emptyList())
                }
            }
            .addOnFailureListener { e ->
                Log.e("UserViewModel", "Error fetching user document: ", e)
                onResult(emptyList())
            }
    }

    // Fetch users to follow back
    fun fetchUsersToFollowBack(userId: String) {
        Log.d("fetchUsersToFollowBack", "Fetching followers and following for user: $userId")

        // Fetch followers first
        fetchFollowers(userId) { followers ->
            Log.d("fetchUsersToFollowBack", "Followers fetched: ${followers.map { it.id }}")

            // Fetch following list only after fetching followers
            fetchFollowing(userId) { following ->
                Log.d("fetchUsersToFollowBack", "Following fetched: ${following.map { it.id }}")

                // Get the list of users you're following
                val followingIds = following.map { it.id }

                // Filter the followers who are not in the following list (i.e., you haven't followed them back)
                val usersToFollowBack = followers.filter { it.id !in followingIds }

                Log.d("fetchUsersToFollowBack", "Users to follow back: ${usersToFollowBack.map { it.id }}")

                // Post the result
                _usersToFollowBack.postValue(usersToFollowBack) // Update LiveData
            }
        }
    }

    fun fetchPeopleYouMayKnow(userId: String) {
        Log.d("fetchPeopleYouMayKnow", "Fetching following list for user: $userId")

        // Step 1: Get the users that the current user is following
        fetchFollowing(userId) { following ->
            val followingIds = following.map { it.id.trim() } // Ensure IDs are clean
            Log.d("fetchPeopleYouMayKnow", "Following fetched: $followingIds")

            if (followingIds.isEmpty()) {
                Log.d("fetchPeopleYouMayKnow", "User is not following anyone.")
                _peopleYouMayKnow.postValue(emptyList())
                return@fetchFollowing
            }

            val peopleYouMayKnow = mutableMapOf<String, Int>()
            val tasks = mutableListOf<Task<QuerySnapshot>>()

            // Step 2: For each user the current user is following, fetch their following list
            followingIds.forEach { followingId ->
                Log.d("fetchPeopleYouMayKnow", "Fetching users followed by: $followingId")
                val task = db.collection("users")
                    .whereArrayContains("following", followingId)
                    .get()
                tasks.add(task)
            }

            // Step 3: Process all the results from the fetched following lists
            Tasks.whenAllSuccess<QuerySnapshot>(tasks).addOnSuccessListener { results ->
                Log.d("fetchPeopleYouMayKnow", "Fetched ${results.size} query results")

                results.forEach { snapshot ->
                    snapshot.documents.forEach { document ->
                        val user = document.toObject(UserRegistration::class.java)
                        if (user != null && user.id != userId && user.id !in followingIds) {
                            // Ensure we track mutual connections correctly
                            val userIdTrimmed = user.id.trim()
                            Log.d("fetchPeopleYouMayKnow", "Adding user to map: $userIdTrimmed")
                            peopleYouMayKnow[userIdTrimmed] = peopleYouMayKnow.getOrDefault(userIdTrimmed, 0) + 1
                        }
                    }
                }

                // Step 4: Filter mutual connections
                val suggestedUsers = peopleYouMayKnow
                    .filter { it.value >= 2 && it.key !in followingIds }
                    .keys
                Log.d("fetchPeopleYouMayKnow", "Suggested users (before filtering): $suggestedUsers")

                if (suggestedUsers.isEmpty()) {
                    Log.d("fetchPeopleYouMayKnow", "No people to suggest after filtering.")
                    _peopleYouMayKnow.postValue(emptyList())
                    return@addOnSuccessListener
                }

                // Step 5: Get detailed user information for the suggested users
                val userDetailsTasks = suggestedUsers.map { id ->
                    CompletableDeferred<UserRegistration?>().apply {
                        getUserData(id) { user -> this.complete(user) }
                    }
                }

                viewModelScope.launch {
                    try {
                        val userDetails = userDetailsTasks.mapNotNull { it.await() }
                        Log.d("fetchPeopleYouMayKnow", "Final list of suggested users: ${userDetails.map { it.id }}")
                        _peopleYouMayKnow.postValue(userDetails)
                    } catch (e: Exception) {
                        Log.e("fetchPeopleYouMayKnow", "Error fetching user details: ", e)
                        _peopleYouMayKnow.postValue(emptyList())
                    }
                }

            }.addOnFailureListener { e ->
                Log.e("fetchPeopleYouMayKnow", "Error fetching people you may know", e)
                _peopleYouMayKnow.postValue(emptyList())
            }
        }
    }

    fun fetchUserById(userId: String, onComplete: (UserRegistration?) -> Unit) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toObject(UserRegistration::class.java)
                    onComplete(user)
                } else {
                    onComplete(null)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("UserViewModel", "Error fetching user: ", exception)
                onComplete(null)
            }
    }

    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> get() = _loading

    fun submitPost(mediaUri: Uri, caption: String, onCompletion: () -> Unit) {
        Log.d("SubmitPostDebug", "Starting post submission")
        viewModelScope.launch {
            _loading.value = true
            val userId = auth.currentUser?.uid ?: return@launch
            val user = currentUser.value ?: return@launch
            val username = user.username
            val profilePhotoUrl = user.profilePhotoUrl ?: "" // Provide a default value if null

            Log.d("SubmitPostDebug", "Uploading media for user: $userId")
            // Upload media
            val mediaUrl = uploadMediaToStorage(userId, mediaUri)
            val mediaType = if (mediaUri.toString().contains("image")) "image" else "video"

            if (mediaUrl.isEmpty()) {
                Log.e("SubmitPostDebug", "Media upload failed")
                _loading.value = false
                return@launch
            }

            Log.d("SubmitPostDebug", "Media uploaded with URL: $mediaUrl, Type: $mediaType")
            // Create post
            val post = Post(
                userId = userId,
                username = username,
                profilePhotoUrl = profilePhotoUrl,
                mediaUrl = mediaUrl,
                mediaType = mediaType,
                caption = caption
            )

            Log.d("SubmitPostDebug", "Saving post for user: $userId with post: $post")
            // Save post
            savePost(userId, post)
            Log.d("SubmitPostDebug", "Post submission completed")
            _loading.value = false
            onCompletion() // Call the completion callback
        }
    }

    suspend fun uploadMediaToStorage(userId: String, mediaUri: Uri): String {
        val storageRef = FirebaseStorage.getInstance().reference
        val userMediaRef = storageRef.child("users/$userId/${mediaUri.lastPathSegment}")

        return try {
            Log.d("FirebaseStorageDebug", "Uploading media to path: users/$userId/${mediaUri.lastPathSegment}")

            val uploadTask = userMediaRef.putFile(mediaUri)
            uploadTask.addOnProgressListener { snapshot ->
                val progress = (100.0 * snapshot.bytesTransferred / snapshot.totalByteCount)
                Log.d("FirebaseStorageDebug", "Upload is $progress% done")
            }.addOnPausedListener {
                Log.d("FirebaseStorageDebug", "Upload is paused")
            }.addOnFailureListener { exception ->
                Log.e("FirebaseStorageDebug", "Upload failed", exception)
            }.addOnSuccessListener {
                Log.d("FirebaseStorageDebug", "Upload successful")
            }

            // Wait for the upload task to complete
            uploadTask.await()

            val downloadUrl = userMediaRef.downloadUrl.await()
            Log.d("FirebaseStorageDebug", "Media uploaded successfully: $downloadUrl")
            downloadUrl.toString()
        } catch (e: Exception) {
            Log.e("FirebaseStorageDebug", "Error uploading media: ${e.message}", e)
            ""
        }
    }

    // Function to save a post
    suspend fun savePost(userId: String, post: Post) {
        val userPostsRef = db.collection("users").document(userId).collection("posts")
        val postId = userPostsRef.document().id
        val postWithId = post.copy(id = postId)

        // Log the Firestore path and post data being written
        Log.d("FirestoreDebug", "Saving post to path: users/$userId/posts/$postId")
        Log.d("FirestoreDebug", "Post data: $postWithId")

        try {
            // Perform the write operation
            userPostsRef.document(postId).set(postWithId).await()
            Log.d("FirestoreDebug", "Post successfully saved with ID: $postId")
        } catch (e: Exception) {
            // Log the error if writing fails
            Log.e("FirestoreDebug", "Error saving post: ${e.message}", e)
        }
    }

//    private val _loading = mutableStateOf(false)
//    val loading: State<Boolean> get() = _loading
//
//    fun submitPost(context: Context, mediaUri: Uri, caption: String, onCompletion: () -> Unit) {
//        Log.d("SubmitPostDebug", "Starting post submission")
//        viewModelScope.launch {
//            _loading.value = true
//            val userId = auth.currentUser?.uid ?: return@launch
//            val user = currentUser.value ?: return@launch
//            val username = user.username
//            val profilePhotoUrl = user.profilePhotoUrl ?: ""
//
//            Log.d("SubmitPostDebug", "Uploading media for user: $userId")
//            // Upload media
//            val compressedMediaUri = compressMedia(context, mediaUri)
//            if (compressedMediaUri == null) {
//                Log.e("SubmitPostDebug", "Media compression failed")
//                _loading.value = false
//                return@launch
//            }
//
//            val mediaUrl = uploadMediaToStorage(userId, compressedMediaUri)
//            val mediaType = if (mediaUri.toString().contains("image")) "image" else "video"
//
//            if (mediaUrl.isEmpty()) {
//                Log.e("SubmitPostDebug", "Media upload failed")
//                _loading.value = false
//                return@launch
//            }
//
//            Log.d("SubmitPostDebug", "Media uploaded with URL: $mediaUrl, Type: $mediaType")
//            // Create post
//            val post = Post(
//                userId = userId,
//                username = username,
//                profilePhotoUrl = profilePhotoUrl,
//                mediaUrl = mediaUrl,
//                mediaType = mediaType,
//                caption = caption,
//                timestamp = System.currentTimeMillis() // Add timestamp for sorting
//            )
//
//            Log.d("SubmitPostDebug", "Saving post for user: $userId with post: $post")
//            // Save post
//            savePost(userId, post)
//            Log.d("SubmitPostDebug", "Post submission completed")
//            _loading.value = false
//            onCompletion() // Call the completion callback
//        }
//    }
//
//    fun compressMedia(context: Context, mediaUri: Uri): Uri? {
//        val inputPath = getPathFromUri(context, mediaUri)
//        val outputPath = getOutputPath(context, "compressed_video.mp4")
//
//        if (inputPath == null) {
//            Log.e("MediaCodec", "Failed to get the input path from URI")
//            return null
//        }
//
//        val extractor = MediaExtractor()
//        extractor.setDataSource(inputPath)
//        val format = extractor.getTrackFormat(0)
//        val mime = format.getString(MediaFormat.KEY_MIME) ?: return null
//
//        val codec = MediaCodec.createEncoderByType(mime)
//        val outputFormat = MediaFormat.createVideoFormat(mime, 1280, 720) // Output resolution
//        outputFormat.setInteger(MediaFormat.KEY_BIT_RATE, 1000000) // Bitrate
//        outputFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30) // Frame rate
//        outputFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 10) // Key frame interval
//
//        codec.configure(outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
//        codec.start()
//
//        val muxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
//        val trackIndex = muxer.addTrack(outputFormat)
//        muxer.start()
//
//        val bufferInfo = MediaCodec.BufferInfo()
//        var outputDone = false
//        var inputDone = false
//        val buffer = ByteBuffer.allocate(65536)
//
//        while (!outputDone) {
//            if (!inputDone) {
//                val inputBufferId = codec.dequeueInputBuffer(10000)
//                if (inputBufferId >= 0) {
//                    val inputBuffer = codec.getInputBuffer(inputBufferId) ?: continue
//                    val sampleSize = extractor.readSampleData(inputBuffer, 0)
//                    if (sampleSize < 0) {
//                        codec.queueInputBuffer(inputBufferId, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
//                        inputDone = true
//                    } else {
//                        codec.queueInputBuffer(inputBufferId, 0, sampleSize, extractor.sampleTime, 0)
//                        extractor.advance()
//                    }
//                }
//            }
//
//            val outputBufferId = codec.dequeueOutputBuffer(bufferInfo, 10000)
//            if (outputBufferId >= 0) {
//                val outputBuffer = codec.getOutputBuffer(outputBufferId) ?: continue
//                if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
//                    bufferInfo.size = 0
//                }
//                if (bufferInfo.size != 0) {
//                    outputBuffer.position(bufferInfo.offset)
//                    outputBuffer.limit(bufferInfo.offset + bufferInfo.size)
//                    muxer.writeSampleData(trackIndex, outputBuffer, bufferInfo)
//                }
//                codec.releaseOutputBuffer(outputBufferId, false)
//                if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
//                    outputDone = true
//                }
//            }
//        }
//
//        codec.stop()
//        codec.release()
//        muxer.stop()
//        muxer.release()
//        extractor.release()
//
//        Log.i("MediaCodec", "Video compression completed successfully.")
//        return Uri.fromFile(File(outputPath))
//    }
//
//    fun getPathFromUri(context: Context, uri: Uri): String? {
//        // Implement this function to get the actual path of the file from the URI.
//        // You can use a library like "RealPathUtil" or the following code snippet:
//        val projection = arrayOf(android.provider.MediaStore.Video.Media.DATA)
//        val cursor = context.contentResolver.query(uri, projection, null, null, null)
//        cursor?.use {
//            if (it.moveToFirst()) {
//                val columnIndex = it.getColumnIndexOrThrow(android.provider.MediaStore.Video.Media.DATA)
//                return it.getString(columnIndex)
//            }
//        }
//        return null
//    }
//
//    fun getOutputPath(context: Context, fileName: String): String {
//        val externalFilesDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
//        val outputDir = File(externalFilesDir, "CompressedVideos")
//        if (!outputDir.exists()) {
//            outputDir.mkdirs()
//        }
//        return File(outputDir, fileName).absolutePath
//    }
//
//    suspend fun uploadMediaToStorage(userId: String, mediaUri: Uri): String {
//        val storageRef = FirebaseStorage.getInstance().reference
//        val userMediaRef = storageRef.child("users/$userId/${mediaUri.lastPathSegment}")
//
//        return try {
//            Log.d("FirebaseStorageDebug", "Uploading media to path: users/$userId/${mediaUri.lastPathSegment}")
//
//            val uploadTask = userMediaRef.putFile(mediaUri)
//            uploadTask.addOnProgressListener { snapshot ->
//                val progress = (100.0 * snapshot.bytesTransferred / snapshot.totalByteCount)
//                Log.d("FirebaseStorageDebug", "Upload is $progress% done")
//            }.addOnPausedListener {
//                Log.d("FirebaseStorageDebug", "Upload is paused")
//            }.addOnFailureListener { exception ->
//                Log.e("FirebaseStorageDebug", "Upload failed", exception)
//            }.addOnSuccessListener {
//                Log.d("FirebaseStorageDebug", "Upload successful")
//            }
//
//            // Wait for the upload task to complete
//            uploadTask.await()
//
//            val downloadUrl = userMediaRef.downloadUrl.await()
//            Log.d("FirebaseStorageDebug", "Media uploaded successfully: $downloadUrl")
//            downloadUrl.toString()
//        } catch (e: Exception) {
//            Log.e("FirebaseStorageDebug", "Error uploading media: ${e.message}", e)
//            ""
//        }
//    }
//
//    suspend fun savePost(userId: String, post: Post) {
//        val userPostsRef = db.collection("users").document(userId).collection("posts")
//        val postId = userPostsRef.document().id
//        val postWithId = post.copy(id = postId)
//
//        // Log the Firestore path and post data being written
//        Log.d("FirestoreDebug", "Saving post to path: users/$userId/posts/$postId")
//        Log.d("FirestoreDebug", "Post data: $postWithId")
//
//        try {
//            // Perform the write operation
//            userPostsRef.document(postId).set(postWithId).await()
//            Log.d("FirestoreDebug", "Post successfully saved with ID: $postId")
//        } catch (e: Exception) {
//            // Log the error if writing fails
//            Log.e("FirestoreDebug", "Error saving post: ${e.message}", e)
//        }
//    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun unlikePost(userId: String, postId: String) {
        val postRef = db.collection("users").document(userId).collection("posts").document(postId)
        postRef.update("likes", FieldValue.arrayRemove(auth.currentUser?.uid))
            .addOnSuccessListener { Log.d("FirestoreDebug", "Post unliked successfully") }
            .addOnFailureListener { e -> Log.e("FirestoreDebug", "Error unliking post: ${e.message}", e) }
    }

    fun likePost(userId: String, postId: String) {
        val postRef = db.collection("users").document(userId).collection("posts").document(postId)
        postRef.update("likes", FieldValue.arrayUnion(auth.currentUser?.uid))
            .addOnSuccessListener { Log.d("FirestoreDebug", "Post liked successfully") }
            .addOnFailureListener { e -> Log.e("FirestoreDebug", "Error liking post: ${e.message}", e) }
    }

    fun fetchTotalLikes(userId: String, postId: String, onLikesFetched: (Int) -> Unit) {
        val postRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("posts")
            .document(postId)

        postRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e("FirestoreDebug", "Error fetching likes: ${e.message}", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val likes = snapshot.get("likes") as? List<String> ?: emptyList()
                Log.d("FirestoreDebug", "Likes count fetched: ${likes.size}")
                onLikesFetched(likes.size)
            } else {
                Log.d("FirestoreDebug", "No likes found for post: $postId")
                onLikesFetched(0)
            }
        }
    }

    fun addPostComment(postOwnerId: String, postId: String, commentText: String, onPostSuccess: () -> Unit) {
        viewModelScope.launch {
            val currentUserId = auth.currentUser?.uid ?: return@launch
            val user = currentUser.value ?: return@launch
            val username = user.username
            val profilePhotoUrl = user.profilePhotoUrl ?: ""

            if (commentText.isBlank()) {
                Log.e("FirestoreDebug", "Comment text is blank")
                return@launch
            }

            val comment = PostComments(
                postId = postId,
                userId = currentUserId,
                username = username,
                profilePhotoUrl = profilePhotoUrl,
                commentText = commentText
            )

            val commentsRef = db.collection("users").document(postOwnerId).collection("posts").document(postId).collection("comments")
            val commentId = commentsRef.document().id
            val commentWithId = comment.copy(id = commentId)

            try {
                commentsRef.document(commentId).set(commentWithId).await()
                Log.d("FirestoreDebug", "Comment successfully added with ID: $commentId, Text: $commentText")
                onPostSuccess()
            } catch (e: Exception) {
                Log.e("FirestoreDebug", "Error adding comment: ${e.message}", e)
            }
        }
    }


    suspend fun getPostComments(postOwnerId: String, postId: String): List<PostComments> {
        val commentsRef = db.collection("users").document(postOwnerId).collection("posts").document(postId).collection("comments")
        return try {
            val snapshot = commentsRef.get().await()
            snapshot.toObjects(PostComments::class.java)
        } catch (e: Exception) {
            Log.e("FirestoreDebug", "Error getting comments: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getCommentsCount(postOwnerId: String, postId: String): Int {
        val commentsRef = db.collection("users").document(postOwnerId).collection("posts").document(postId).collection("comments")
        return try {
            val snapshot = commentsRef.get().await()
            snapshot.size()
        } catch (e: Exception) {
            Log.e("FirestoreDebug", "Error getting comments count: ${e.message}", e)
            0
        }
    }

    fun getUserProfile(userId: String, onResult: (String?) -> Unit) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val profilePhotoUrl = document.getString("profilePhotoUrl")
                onResult(profilePhotoUrl)
            }
            .addOnFailureListener { exception ->
                Log.e("UserViewModel", "Error fetching user profile", exception)
                onResult(null)
            }
    }
    fun getChatMessages(chatId: String): LiveData<List<Message>> {
        val messagesLiveData = MutableLiveData<List<Message>>()
        val currentUser = getCurrentUser()

        if (currentUser != null) {
            firestore.collection("users")
                .document(currentUser.id)
                .collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp")
                .addSnapshotListener { snapshot, exception ->
                    if (exception != null) {
                        Log.e("UserViewModel", "Error fetching messages", exception)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val messages = snapshot.documents.mapNotNull { it.toObject(Message::class.java) }
                        messagesLiveData.postValue(messages)
                    }
                }
        }

        return messagesLiveData
    }
    fun getUserChats(userId: String): LiveData<List<Chat>> {
        val chatsLiveData = MutableLiveData<List<Chat>>()
        firestore.collection("users")
            .document(userId)
            .collection("chats")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Log.e("UserViewModel", "Error getting user chats", firebaseFirestoreException)
                    return@addSnapshotListener
                }

                if (querySnapshot != null) {
                    val chats = querySnapshot.documents.mapNotNull { document ->
                        val id = document.getString("id")
                        val userId = document.getString("userId")
                        val receiverId = document.getString("receiverId")
                        val lastMessage = document.getString("lastMessage")
                        val timestamp = document.getLong("timestamp")
                        val unread = document.getBoolean("unread")
                        val unreadCount = document.getLong("unreadCount")?.toInt()

                        if (id != null && userId != null && receiverId != null && receiverId.isNotEmpty() && lastMessage != null && timestamp != null && unread != null) {
                            Log.d("UserViewModel", "Fetched Chat: id=$id, userId=$userId, receiverId=$receiverId, lastMessage=$lastMessage, timestamp=$timestamp, unread=$unread, unreadCount=$unreadCount")
                            Chat(id, userId, receiverId, lastMessage, timestamp, unread, unreadCount ?: 0)
                        } else {
                            Log.e("UserViewModel", "Invalid or missing field in document ${document.id}")
                            null
                        }
                    }
                    Log.d("UserViewModel", "Chats found: ${chats.size}")
                    chatsLiveData.postValue(chats)
                }
            }
        return chatsLiveData
    }
    suspend fun sendMessage(message: Message) {
        withContext(Dispatchers.IO) {
            try {
                val senderId = message.senderId
                val chatId = message.chatId

                // Save the message in the sender's document
                val senderMessageCollection = firestore.collection("users")
                    .document(senderId)
                    .collection("chats")
                    .document(chatId)
                    .collection("messages")

                Log.d("UserViewModel", "Adding message to Firestore for sender: $message")
                Tasks.await(senderMessageCollection.document(message.id).set(message.copy(status = "sent"), SetOptions.merge()))

                // Save the message in the receiver's document
                val receiverMessageCollection = firestore.collection("users")
                    .document(message.receiverId)
                    .collection("chats")
                    .document(chatId)
                    .collection("messages")

                Log.d("UserViewModel", "Adding message to Firestore for receiver: $message")
                Tasks.await(receiverMessageCollection.document(message.id).set(message.copy(status = "sent"), SetOptions.merge()))

                // Ensure chat document exists before updating the last message
                createOrUpdateChat(senderId, message.receiverId, message.content)
                createOrUpdateChat(message.receiverId, senderId, message.content)

                // Update the last message in both sender's and receiver's documents
                updateLastMessage(chatId, message.content, message.timestamp, message.receiverId, senderId)

                // Update the message status to "delivered"
                Log.d("UserViewModel", "Message delivered: $message")
                val deliveredMessage = message.copy(status = "delivered")
                updateMessageStatus(deliveredMessage)
            } catch (e: Exception) {
                // Log error in sending message
                val errorMessage = message.copy(status = "error")
                Log.e("UserViewModel", "Error adding message to Firestore: $errorMessage", e)
                updateMessageStatus(errorMessage)
            }
        }
    }
    suspend fun createOrUpdateChat(userId: String, receiverId: String, lastMessage: String) {
        withContext(Dispatchers.IO) {
            val chatId = generateChatId(userId, receiverId)

            // Chat data for the sender (User A)
            val userChatData = mapOf(
                "id" to chatId,
                "userId" to userId,
                "receiverId" to receiverId,
                "lastMessage" to lastMessage,
                "timestamp" to System.currentTimeMillis(),
                "unread" to true,
                "unreadCount" to 1
            )

            // Chat data for the receiver (User B)
            val receiverChatData = mapOf(
                "id" to chatId,
                "userId" to receiverId,
                "receiverId" to userId, // Swap user and receiver
                "lastMessage" to lastMessage,
                "timestamp" to System.currentTimeMillis(),
                "unread" to true,
                "unreadCount" to 1
            )

            val userChatDocument = firestore.collection("users")
                .document(userId)
                .collection("chats")
                .document(chatId)

            val receiverChatDocument = firestore.collection("users")
                .document(receiverId)
                .collection("chats")
                .document(chatId)

            Tasks.await(userChatDocument.set(userChatData, SetOptions.merge()))
            Log.d("UserViewModel", "Chat document for user $userId created/updated successfully: $chatId")

            Tasks.await(receiverChatDocument.set(receiverChatData, SetOptions.merge()))
            Log.d("UserViewModel", "Chat document for receiver $receiverId created/updated successfully: $chatId")
        }
    }
    private suspend fun updateLastMessage(chatId: String, lastMessage: String, timestamp: Long, receiverId: String, senderId: String) {
        withContext(Dispatchers.IO) {
            try {
                // Update the last message in the sender's document
                val senderChatDocument = firestore.collection("users")
                    .document(senderId)
                    .collection("chats")
                    .document(chatId)

                Log.d("UserViewModel", "Updating last message in sender's document: $chatId")
                Tasks.await(senderChatDocument.update(mapOf(
                    "lastMessage" to lastMessage,
                    "lastMessageTimestamp" to timestamp,
                    "unreadCount" to 0 // Reset unread count for sender
                )))

                // Update the last message in the receiver's document
                val receiverChatDocument = firestore.collection("users")
                    .document(receiverId)
                    .collection("chats")
                    .document(chatId)

                Log.d("UserViewModel", "Updating last message in receiver's document: $chatId")
                Tasks.await(receiverChatDocument.update(mapOf(
                    "lastMessage" to lastMessage,
                    "lastMessageTimestamp" to timestamp,
                    "unreadCount" to FieldValue.increment(1) // Increment unread count for receiver
                )))
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error updating last message: $chatId", e)
            }
        }
    }
    private suspend fun updateMessageStatus(message: Message) {
        try {
            val senderId = message.senderId
            val chatId = message.chatId

            // Update the message status in the sender's document
            val senderMessageDocument = firestore.collection("users")
                .document(senderId)
                .collection("chats")
                .document(chatId)
                .collection("messages")
                .document(message.id)

            Log.d("UserViewModel", "Updating message status for sender: $message")
            senderMessageDocument.update("status", message.status).await()

            // Update the message status in the receiver's document
            val receiverMessageDocument = firestore.collection("users")
                .document(message.receiverId)
                .collection("chats")
                .document(chatId)
                .collection("messages")
                .document(message.id)

            Log.d("UserViewModel", "Updating message status for receiver: $message")
            receiverMessageDocument.update("status", message.status).await()
        } catch (e: Exception) {
            Log.e("UserViewModel", "Error updating message status: $message", e)
        }
    }
//for the individual chat screen checking messages states
    fun markMessageAsSeen(messageId: String, chatId: String, receiverId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentUser = getCurrentUser()

                // Update the message status to "seen" in the sender's document
                val senderMessageDocument = firestore.collection("users")
                    .document(currentUser?.id ?: "")
                    .collection("chats")
                    .document(chatId)
                    .collection("messages")
                    .document(messageId)

                Log.d("UserViewModel", "Attempting to update message status in sender's document: ${senderMessageDocument.path}")

                senderMessageDocument.update("status", "seen")
                    .addOnSuccessListener {
                        Log.d("UserViewModel", "Message marked as seen in sender's document: $messageId")
                    }
                    .addOnFailureListener { e ->
                        Log.e("UserViewModel", "Error marking message as seen in sender's document: $messageId", e)
                    }

                // Update the message status to "seen" in the receiver's document
                val receiverMessageDocument = firestore.collection("users")
                    .document(receiverId)
                    .collection("chats")
                    .document(chatId)
                    .collection("messages")
                    .document(messageId)

                Log.d("UserViewModel", "Attempting to update message status in receiver's document: ${receiverMessageDocument.path}")

                receiverMessageDocument.update("status", "seen")
                    .addOnSuccessListener {
                        Log.d("UserViewModel", "Message marked as seen in receiver's document: $messageId")
                    }
                    .addOnFailureListener { e ->
                        Log.e("UserViewModel", "Error marking message as seen in receiver's document: $messageId", e)
                    }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Exception marking message as seen: $messageId", e)
            }
        }
    }

// Update function to mark messages as read
    fun markMessagesAsRead(chatId: String, userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val updates = mapOf(
                    "unread" to false,
                    "unreadCount" to 0
                )
                firestore.collection("users")
                    .document(userId)
                    .collection("chats")
                    .document(chatId)
                    .update(updates)
                    .addOnSuccessListener {
                        Log.d("UserViewModel", "Marked messages as read for chat: $chatId")
                    }
                    .addOnFailureListener { e ->
                        Log.e("UserViewModel", "Error marking messages as read for chat: $chatId", e)
                    }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Exception marking messages as read", e)
            }
        }
    }
    // Function to update typing status in Realtime Database
    fun updateTypingStatusInRealtimeDatabase(userId: String, isTyping: Boolean) {
        val typingStatusDatabaseRef = database.getReference("/typingStatus/$userId")
        typingStatusDatabaseRef.setValue(isTyping)
    }

    // Function to get typing status from Realtime Database
    fun getTypingStatusFromRealtimeDatabase(userId: String, callback: (Boolean) -> Unit) {
        val typingStatusDatabaseRef = database.getReference("/typingStatus/$userId")
        typingStatusDatabaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isTyping = snapshot.getValue(Boolean::class.java) ?: false
                callback(isTyping)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("UserViewModel", "Typing status listener was cancelled")
            }
        })
    }
    fun generateChatId(userId1: String, userId2: String): String {
        val sortedIds = listOf(userId1, userId2).sorted()
        return "${sortedIds[0]}_${sortedIds[1]}"
    }

    // Function to upload media to Firebase Storage
    suspend fun uploadMedia(chatId: String, mediaUri: Uri): String {
        val storageRef = storage.reference.child("chats/$chatId/${mediaUri.lastPathSegment}")
        val uploadTask = storageRef.putFile(mediaUri).await()
        return uploadTask.storage.downloadUrl.await().toString()
    }
    // Function to send a message with media
    suspend fun sendMessageWithMedia(chatId: String, message: String, receiverId: String, mediaUrl: String) {
        val firestore = FirebaseFirestore.getInstance()
        val messageId = firestore.collection("chats").document(chatId).collection("messages").document().id

        val newMessage = Message(
            id = messageId,
            senderId = getCurrentUser()?.id ?: "",
            receiverId = receiverId,
            content = message,
            timestamp = System.currentTimeMillis(),
            status = "sent",
            mediaUrl = mediaUrl
        )

        firestore.collection("chats").document(chatId).collection("messages").document(messageId).set(newMessage)
            .addOnSuccessListener {
                Log.d("UserViewModel", "Message with media sent successfully")
            }
            .addOnFailureListener { e ->
                Log.e("UserViewModel", "Error sending message with media", e)
            }
    }

    fun getUserData(userId: String, onResult: (UserRegistration?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val document = db.collection("users").document(userId).get().await()
                val user = document.toObject(UserRegistration::class.java)
                withContext(Dispatchers.Main) {
                    onResult(user)
                }
            } catch (exception: Exception) {
                Log.e("UserViewModel", "Error fetching user data", exception)
                withContext(Dispatchers.Main) {
                    onResult(null)
                }
            }
        }
    }

    fun getFollowingUsers(currentUserId: String): LiveData<List<UserRegistration>> {
        val liveData = MutableLiveData<List<UserRegistration>>()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val document = db.collection("users").document(currentUserId).get().await()
                val user = document.toObject(UserRegistration::class.java)
                if (user != null) {
                    val followingIds = user.following
                    val documents = db.collection("users").whereIn("id", followingIds).get().await()
                    val users = documents.mapNotNull { it.toObject(UserRegistration::class.java) }
                    withContext(Dispatchers.Main) {
                        liveData.value = users
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        liveData.value = emptyList()
                    }
                }
            } catch (exception: Exception) {
                Log.e("UserViewModel", "Error fetching following users", exception)
                withContext(Dispatchers.Main) {
                    liveData.value = emptyList()
                }
            }
        }
        return liveData
    }
    fun navigateToIncomingCallScreen(navController: NavController, receiverId: String) {
        navController.navigate("incomingCallScreen/$receiverId")
    }
    fun searchChats(userId: String, query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val chatsSnapshot = db.collection("users")
                    .document(userId)
                    .collection("chats")
                    .whereGreaterThanOrEqualTo("lastMessage", query)
                    .get()
                    .await()

                val chats = chatsSnapshot.documents.mapNotNull { it.toObject(Chat::class.java) }
                withContext(Dispatchers.Main) {
                    _userChats.value = chats
                }
            } catch (exception: Exception) {
                Log.e("UserViewModel", "Error searching chats", exception)
                withContext(Dispatchers.Main) {
                    _userChats.value = emptyList()
                }
            }
        }
    }
}
