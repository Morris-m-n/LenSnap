package com.lensnap.app.data

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.storage.FirebaseStorage
import com.lensnap.app.models.Post
import com.lensnap.app.models.UserRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class UserViewModel(private val context: Context) : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)

    private val _currentUser = MutableLiveData<UserRegistration?>()
    val currentUser: LiveData<UserRegistration?> get() = _currentUser
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

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

    private fun fetchCurrentUser(userId: String) {
        Log.d("UserViewModel", "Fetching current user with ID: $userId")
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                _currentUser.value = document.toObject(UserRegistration::class.java)
                Log.d("UserViewModel", "User data fetched successfully")
            }
            .addOnFailureListener { exception ->
                Log.e("UserViewModel", "Error fetching user data", exception)
            }
    }

    fun signUp(
        email: String,
        password: String,
        username: String,
        profileImageUri: Uri?, // Adding profile image URI
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

                            val newUser = UserRegistration(
                                id = userId,
                                email = email,
                                username = username,
                                profilePhotoUrl = profilePhotoUrl
                            )

                            db.collection("users").document(userId).set(newUser)
                                .addOnSuccessListener {
                                    onResult(true, null)
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
                        sharedPreferences.edit().putString("USER_ID", userId).apply()
                        userId?.let { fetchCurrentUser(it) }
                        Log.d("UserViewModel", "User signed in successfully. User ID: $userId")
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

    fun getStoredUserId(): String? {
        return sharedPreferences.getString("USER_ID", null)
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
        db.collection("users").whereArrayContains("followers", userId).get()
            .addOnSuccessListener { documents ->
                val followers = documents.mapNotNull { it.toObject(UserRegistration::class.java) }
                onResult(followers)
            }.addOnFailureListener { e ->
                Log.e("UserViewModel", "Error fetching followers: ", e)
                onResult(emptyList())
            }
    }

    fun fetchFollowing(userId: String, onResult: (List<UserRegistration>) -> Unit) {
        db.collection("users").whereArrayContains("following", userId).get()
            .addOnSuccessListener { documents ->
                val following = documents.mapNotNull { it.toObject(UserRegistration::class.java) }
                onResult(following)
            }.addOnFailureListener { e ->
                Log.e("UserViewModel", "Error fetching following: ", e)
                onResult(emptyList())
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
    fun submitPost(mediaUri: Uri, caption: String) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            val user = currentUser.value ?: return@launch
            val username = user.username
            val profilePhotoUrl = user.profilePhotoUrl ?: "" // Provide a default value if null

            // Upload media
            val mediaUrl = postRepository.uploadMedia(userId, mediaUri)
            val mediaType = if (mediaUri.toString().contains("image")) "image" else "video"

            // Create post
            val post = Post(
                userId = userId,
                username = username,
                profilePhotoUrl = profilePhotoUrl,
                mediaUrl = mediaUrl,
                mediaType = mediaType,
                caption = caption
            )

            // Save post
            postRepository.savePost(userId, post)
        }
    }
}
