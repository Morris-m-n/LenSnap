package com.lensnap.app.data

import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.lensnap.app.models.Post
import com.lensnap.app.models.PostComments
import com.lensnap.app.models.UserRegistration
import kotlinx.coroutines.tasks.await

class PostRepository {
    private val storage = FirebaseStorage.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun uploadMedia(userId: String, mediaUri: Uri): String {
        val storageRef = storage.reference.child("posts/$userId/${mediaUri.lastPathSegment}")
        val uploadTask = storageRef.putFile(mediaUri).await()
        return uploadTask.storage.downloadUrl.await().toString()
    }


    // Function to get all posts for a user
    suspend fun getUserPosts(userId: String): List<Post> {
        val posts = mutableListOf<Post>()

        // Log the Firestore path being accessed
        Log.d("FirestoreDebug", "Fetching posts from path: users/$userId/posts")

        return try {
            // Fetch posts from Firestore
            val querySnapshot = db.collection("users")
                .document(userId)
                .collection("posts")
                .get()
                .await()

            for (document in querySnapshot.documents) {
                val post = document.toObject(Post::class.java)
                post?.let { posts.add(it) }
            }

            // Log the number of posts fetched
            Log.d("FirestoreDebug", "Successfully fetched ${posts.size} posts.")
            posts
        } catch (e: Exception) {
            // Log the error if fetching fails
            Log.e("FirestoreDebug", "Error fetching posts: ${e.message}", e)
            emptyList()
        }
    }

//    // Function to get a specific post by ID
//    suspend fun getPostById(userId: String, postId: String): Post? {
//        // Log the Firestore path being accessed
//        Log.d("FirestoreDebug", "Fetching post from path: users/$userId/posts/$postId")
//
//        return try {
//            // Fetch the post document
//            val doc = db.collection("users")
//                .document(userId)
//                .collection("posts")
//                .document(postId)
//                .get()
//                .await()
//
//            val post = doc.toObject(Post::class.java)
//
//            // Log success or not found
//            if (post != null) {
//                Log.d("FirestoreDebug", "Successfully fetched post with ID: $postId")
//            } else {
//                Log.d("FirestoreDebug", "Post with ID: $postId not found.")
//            }
//
//            post
//        } catch (e: Exception) {
//            // Log the error if fetching fails
//            Log.e("FirestoreDebug", "Error fetching post with ID $postId: ${e.message}", e)
//            null
//        }
//    }

    suspend fun getPostById(userId: String, postId: String): Post? {
        // Log the Firestore path being accessed
        Log.d("FirestoreDebug", "Fetching post from path: users/$userId/posts/$postId")

        return try {
            // Fetch the post document
            val doc = db.collection("users")
                .document(userId)
                .collection("posts")
                .document(postId)
                .get()
                .await()

            val post = doc.toObject(Post::class.java)

            // Log success or not found
            if (post != null) {
                Log.d("FirestoreDebug", "Successfully fetched post with ID: $postId")
            } else {
                Log.d("FirestoreDebug", "Post with ID: $postId not found.")
            }

            post
        } catch (e: Exception) {
            // Log the error if fetching fails
            Log.e("FirestoreDebug", "Error fetching post with ID $postId: ${e.message}", e)
            null
        }
    }

    // Function to update post caption
    suspend fun updatePostCaption(userId: String, postId: String, newCaption: String) {
        try {
            val postRef = db.collection("users").document(userId)
                .collection("posts").document(postId)

            postRef.update("caption", newCaption).await()
        } catch (e: Exception) {
            throw Exception("Error updating post caption")
        }
    }
    suspend fun deletePost(userId: String, postId: String) {
        val userPostsRef = db.collection("users").document(userId).collection("posts")
        userPostsRef.document(postId).delete().await()
    }

    suspend fun fetchDashboardPosts(currentUserId: String): List<Post> {
        Log.d("fetchDashboardPosts", "Fetching posts for user ID: $currentUserId")

        // Check that currentUserId is not empty
        if (currentUserId.isEmpty()) {
            Log.e("fetchDashboardPosts", "currentUserId is empty")
            throw IllegalArgumentException("currentUserId cannot be empty")
        }

        // Fetch current user
        val userRef = firestore.collection("users").document(currentUserId).get().await()
        val currentUser = userRef.toObject(UserRegistration::class.java) ?: return emptyList()
        Log.d("fetchDashboardPosts", "Current user fetched: $currentUser")

        // Fetch posts from followed users
        val followedPosts = currentUser.following.flatMap { followedUserId ->
            Log.d("fetchDashboardPosts", "Fetching posts for followed user ID: $followedUserId")
            firestore.collection("users").document(followedUserId).collection("posts").get().await().documents
                .mapNotNull { it.toObject(Post::class.java) }
        }

        // Fetch other posts
        val allPostsRef = firestore.collection("posts").get().await()
        val otherPosts = allPostsRef.documents.mapNotNull { it.toObject(Post::class.java) }
            .filter { it.userId !in currentUser.following }

        // Combine and sort posts by timestamp
        val dashboardPosts = (followedPosts + otherPosts).sortedByDescending { it.timestamp }
        Log.d("fetchDashboardPosts", "Fetched ${dashboardPosts.size} posts")

        return dashboardPosts
    }
}