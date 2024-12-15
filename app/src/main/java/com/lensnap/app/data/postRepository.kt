package com.lensnap.app.data

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.lensnap.app.models.Post
import kotlinx.coroutines.tasks.await
import com.lensnap.app.models.UserRegistration


class PostRepository {
    private val storage = FirebaseStorage.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun uploadMedia(userId: String, mediaUri: Uri): String {
        val storageRef = storage.reference.child("posts/${userId}/${mediaUri.lastPathSegment}")
        val uploadTask = storageRef.putFile(mediaUri).await()
        return uploadTask.storage.downloadUrl.await().toString()
    }

    suspend fun savePost(userId: String, post: Post) {
        val userPostsRef = db.collection("users").document(userId).collection("posts")
        val postId = userPostsRef.document().id
        val postWithId = post.copy(id = postId)
        userPostsRef.document(postId).set(postWithId).await()
    }

    suspend fun getUserPosts(userId: String): List<Post> {
        val posts = mutableListOf<Post>()
        val querySnapshot = db.collection("users").document(userId).collection("posts").get().await()
        for (document in querySnapshot.documents) {
            val post = document.toObject(Post::class.java)
            post?.let { posts.add(it) }
        }
        return posts
    }

    suspend fun fetchDashboardPosts(currentUserId: String): List<Post> {
        val userRef = firestore.collection("users").document(currentUserId).get().await()
        val currentUser = userRef.toObject(UserRegistration::class.java) ?: return emptyList()

        // Fetch posts from followed users
        val followedPosts = currentUser.following.flatMap { userId ->
            firestore.collection("users").document(userId).collection("posts").get().await().documents
                .mapNotNull { it.toObject(Post::class.java) }
        }

        // Fetch other posts
        val allPostsRef = firestore.collection("posts").get().await()
        val otherPosts = allPostsRef.documents.mapNotNull { it.toObject(Post::class.java) }
            .filter { it.userId !in currentUser.following }

        // Combine and sort posts by timestamp
        val dashboardPosts = (followedPosts + otherPosts).sortedByDescending { it.timestamp }
        return dashboardPosts
    }
}
