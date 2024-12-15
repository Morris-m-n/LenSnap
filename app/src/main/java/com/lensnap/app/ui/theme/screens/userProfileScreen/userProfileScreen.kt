package com.lensnap.app.ui.theme.screens.userProfileScreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.lensnap.app.data.PostRepository
import com.lensnap.app.data.UserViewModel
import com.lensnap.app.models.Post
import com.lensnap.app.models.UserRegistration
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.NavController

@Composable
fun UserProfileScreen(userViewModel: UserViewModel, userId: String, navController: NavController) {
    val targetUser = remember { mutableStateOf<UserRegistration?>(null) }
    val followers = remember { mutableStateOf<List<UserRegistration>>(emptyList()) }
    val following = remember { mutableStateOf<List<UserRegistration>>(emptyList()) }
    var userPosts by remember { mutableStateOf<List<Post>>(emptyList()) }

    LaunchedEffect(userId) {
        userViewModel.fetchUserById(userId) { fetchedUser ->
            targetUser.value = fetchedUser
        }
        userViewModel.fetchFollowers(userId) {
            followers.value = it
        }
        userViewModel.fetchFollowing(userId) {
            following.value = it
        }
        val postRepository = PostRepository()
        val posts = postRepository.getUserPosts(userId)
        userPosts = posts
    }

    Column(modifier = Modifier.padding(16.dp)) {
        targetUser.value?.let { currentUser ->
            UserDetailsSection(
                user = currentUser,
                followersCount = followers.value.size,
                followingCount = following.value.size,
                userViewModel = userViewModel,
                targetUserId = userId,
                navController = navController
            )
            Spacer(modifier = Modifier.height(16.dp))
            PostsSection(userPosts)
        }
    }
}

@Composable
fun UserDetailsSection(
    user: UserRegistration,
    followersCount: Int,
    followingCount: Int,
    userViewModel: UserViewModel,
    targetUserId: String,
    navController: NavController
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp) // Adjusted profile image height
    ) {
        Row {
            // Profile Image on the left
            Image(
                painter = rememberImagePainter(data = user.profilePhotoUrl),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
            )
            Spacer(modifier = Modifier.width(16.dp))

            // User details and buttons on the right
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = user.username, style = MaterialTheme.typography.h5)

                Spacer(modifier = Modifier.height(8.dp))

                Row {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { navController.navigate("followers/$targetUserId") }
                    ) {
                        Text(text = "Followers")
                        Text(text = "$followersCount", style = MaterialTheme.typography.body2)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { navController.navigate("following/$targetUserId") }
                    ) {
                        Text(text = "Following")
                        Text(text = "$followingCount", style = MaterialTheme.typography.body2)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                FollowButton(userViewModel = userViewModel, targetUserId = targetUserId)
            }
        }
    }
}

@Composable
fun PostsSection(userPosts: List<Post>) {
    Column {
        Text(text = "Posts", style = MaterialTheme.typography.h6)
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(userPosts) { post ->
                PostCard(post)
            }
        }
    }
}

@Composable
fun PostCard(post: Post) {
    val primaryBlueColor = Color(0xFF0D6EFD) // Bootstrap primary blue color

    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column {
            // Top Border
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(primaryBlueColor)
            )

            // Media
            Image(
                painter = rememberImagePainter(post.mediaUrl),
                contentDescription = "Post Media",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp), // Increased height for a bigger image
                contentScale = ContentScale.Fit // Display the full image without cropping
            )

            // Caption
            Text(
                text = post.caption,
                style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(8.dp)
            )

            // Bottom Border
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(primaryBlueColor)
            )
        }
    }
}

@Composable
fun FollowButton(userViewModel: UserViewModel, targetUserId: String) {
    val currentUser by userViewModel.currentUser.observeAsState()
    val isFollowing = remember { mutableStateOf(currentUser?.following?.contains(targetUserId) == true) }

    LaunchedEffect(currentUser) {
        isFollowing.value = currentUser?.following?.contains(targetUserId) == true
    }

    Button(
        onClick = {
            if (isFollowing.value) {
                userViewModel.unfollowUser(targetUserId)
            } else {
                userViewModel.followUser(targetUserId)
            }
        },
        colors = ButtonDefaults.buttonColors(
            backgroundColor = if (isFollowing.value) Color.White else Color(0xFF0D6EFD),
            contentColor = if (isFollowing.value) Color(0xFF0D6EFD) else Color.White
        ),
        border = if (isFollowing.value) BorderStroke(1.dp, Color(0xFF0D6EFD)) else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(if (isFollowing.value) "Unfollow" else "Follow")
    }
}
