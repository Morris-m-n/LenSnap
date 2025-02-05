package com.lensnap.app.ui.theme.screens.userProfileScreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.lensnap.app.models.UserRegistration
import com.lensnap.app.ui.theme.screens.profileScreen.VideoCard

//@Composable
//fun UserProfileScreen(userViewModel: UserViewModel, userId: String, navController: NavController) {
//    val targetUser = remember { mutableStateOf<UserRegistration?>(null) }
//    val followers = remember { mutableStateOf<List<UserRegistration>>(emptyList()) }
//    val following = remember { mutableStateOf<List<UserRegistration>>(emptyList()) }
//    var userPosts by remember { mutableStateOf<List<Post>>(emptyList()) }
//
//    LaunchedEffect(userId) {
//        userViewModel.fetchUserById(userId) { fetchedUser ->
//            targetUser.value = fetchedUser
//        }
//        userViewModel.fetchFollowers(userId) {
//            followers.value = it
//        }
//        userViewModel.fetchFollowing(userId) {
//            following.value = it
//        }
//        val postRepository = PostRepository()
//        val posts = postRepository.getUserPosts(userId)
//        userPosts = posts
//    }
//
//    LazyColumn(
//        verticalArrangement = Arrangement.spacedBy(16.dp),
//        modifier = Modifier.padding(16.dp)
//    ) {
//        item {
//            targetUser.value?.let { currentUser ->
//                UserDetailsSection(
//                    user = currentUser,
//                    followersCount = followers.value.size,
//                    followingCount = following.value.size,
//                    userViewModel = userViewModel,
//                    targetUserId = userId,
//                    navController = navController
//                )
//            }
//        }
//        item {
//            Spacer(modifier = Modifier.height(16.dp))
//        }
//        item {
//            PostsSection(userPosts)
//        }
//    }
//}
//
//@Composable
//fun FollowButton(userViewModel: UserViewModel, targetUserId: String) {
//    val currentUser by userViewModel.currentUser.observeAsState()
//    val isFollowing = remember { mutableStateOf(currentUser?.following?.contains(targetUserId) == true) }
//
//    LaunchedEffect(currentUser) {
//        isFollowing.value = currentUser?.following?.contains(targetUserId) == true
//    }
//
//    Button(
//        onClick = {
//            if (isFollowing.value) {
//                userViewModel.unfollowUser(targetUserId)
//            } else {
//                userViewModel.followUser(targetUserId)
//            }
//        },
//        colors = ButtonDefaults.buttonColors(
//            backgroundColor = if (isFollowing.value) Color.White else Color(0xFF0D6EFD),
//            contentColor = if (isFollowing.value) Color(0xFF0D6EFD) else Color.White
//        ),
//        border = if (isFollowing.value) BorderStroke(1.dp, Color(0xFF0D6EFD)) else null,
//        shape = RoundedCornerShape(8.dp),
//        modifier = Modifier
//            .width(IntrinsicSize.Min)
//            .height(40.dp)
//            .padding(vertical = 4.dp)
//    ) {
//        Text(
//            text = if (isFollowing.value) "Unfollow" else "Follow",
//            color = if (isFollowing.value) Color(0xFF0D6EFD) else Color.White,
//            style = MaterialTheme.typography.body2,
//            fontWeight = FontWeight.Bold
//        )
//    }
//}
//
//@Composable
//fun UserDetailsSection(
//    user: UserRegistration,
//    followersCount: Int,
//    followingCount: Int,
//    userViewModel: UserViewModel,
//    targetUserId: String,
//    navController: NavController
//) {
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(16.dp),
//        verticalArrangement = Arrangement.spacedBy(8.dp)
//    ) {
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//        ) {
//            Row {
//                // Profile Image in a card with rounded edges
//                Card(
//                    elevation = 4.dp, // Use Dp for elevation
//                    modifier = Modifier
//                        .size(125.dp),
//                    shape = RoundedCornerShape(12.dp)
//                ) {
//                    Box(modifier = Modifier.fillMaxSize()) {
//                        Image(
//                            painter = rememberImagePainter(data = user.profilePhotoUrl),
//                            contentDescription = "Profile Photo",
//                            modifier = Modifier
//                                .fillMaxSize()
//                                .clip(RoundedCornerShape(12.dp)),
//                            contentScale = ContentScale.Crop
//                        )
//                    }
//                }
//
//                // User details (username, followers, and following) in columns
//                Column(
//                    verticalArrangement = Arrangement.Center,
//                    horizontalAlignment = Alignment.Start,
//                    modifier = Modifier
//                        .weight(1f)
//                        .padding(start = 16.dp)
//                ) {
//                    Text(
//                        text = user.username,
//                        fontWeight = FontWeight.Thin,
//                        fontSize = 20.sp
//                    )
//
//                    Spacer(modifier = Modifier.height(8.dp))
//
//                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
//                        Card(
//                            elevation = 4.dp, // Use Dp for elevation
//                            modifier = Modifier
//                                .padding(4.dp)
//                                .shadow(4.dp, RoundedCornerShape(8.dp))
//                                .background(Color(0xFFF0F8FF)),
//                            shape = RoundedCornerShape(8.dp)
//                        ) {
//                            Column(
//                                horizontalAlignment = Alignment.CenterHorizontally,
//                                modifier = Modifier.padding(8.dp)
//                            ) {
//                                Text(text = "Followers")
//                                Text(
//                                    text = "$followersCount",
//                                    style = MaterialTheme.typography.body2, // Corrected text style reference
//                                    fontWeight = FontWeight.Bold
//                                )
//                            }
//                        }
//                        Card(
//                            elevation = 4.dp, // Use Dp for elevation
//                            modifier = Modifier
//                                .padding(4.dp)
//                                .shadow(4.dp, RoundedCornerShape(8.dp))
//                                .background(Color(0xFFF0F8FF)),
//                            shape = RoundedCornerShape(8.dp)
//                        ) {
//                            Column(
//                                horizontalAlignment = Alignment.CenterHorizontally,
//                                modifier = Modifier.padding(8.dp)
//                            ) {
//                                Text(text = "Following")
//                                Text(
//                                    text = "$followingCount",
//                                    style = MaterialTheme.typography.body2, // Corrected text style reference
//                                    fontWeight = FontWeight.Bold
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        // Follow and Unfollow button
//        Row(
//            modifier = Modifier
//                .fillMaxWidth(),
//            horizontalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//                FollowButton(userViewModel = userViewModel, targetUserId = targetUserId)
//        }
//    }
//}
//
//@Composable
//fun PostsSection(userPosts: List<Post>) {
//    Column {
//        Text(text = "Posts", style = MaterialTheme.typography.h6)
//
//        // Display posts in chunks of two
//        userPosts.chunked(2).forEach { rowPosts ->
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.spacedBy(16.dp)
//            ) {
//                rowPosts.forEach { post ->
//                    Box(
//                        modifier = Modifier
//                            .weight(1f)
//                    ) {
//                        PostCard(post)
//                    }
//                }
//                // If there's an odd number of posts, add a spacer to fill the remaining space
//                if (rowPosts.size < 2) {
//                    Spacer(modifier = Modifier.weight(1f))
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun PostCard(post: Post) {
//
//    val aspectRatioModifier = when {
//        post.aspectRatio == 1f -> Modifier.height(200.dp)
//        post.aspectRatio > 1f -> Modifier.height((200 * post.aspectRatio).dp)
//        else -> Modifier.height((200 / post.aspectRatio).dp)
//    }
//
//    Card(
//        shape = RoundedCornerShape(12.dp),
//        elevation = 10.dp,
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(8.dp)
//            .then(aspectRatioModifier),
//    ) {
//        Box(
//            modifier = Modifier.fillMaxSize()
//        ) {
//            if (post.mediaType == "image") {
//                // Post Media - Image
//                Image(
//                    painter = rememberImagePainter(post.mediaUrl),
//                    contentDescription = "Post Media",
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .clip(RoundedCornerShape(12.dp)),
//                    contentScale = ContentScale.Crop
//                )
//            } else {
//                // Post Media - Video
//                VideoCard(
//                    mediaUrl = post.mediaUrl,
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .clip(RoundedCornerShape(12.dp))
//                )
//            }
//
//            // Gradient overlay and caption
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .background(
//                        brush = Brush.verticalGradient(
//                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)),
//                            startY = 300f
//                        )
//                    )
//                    .padding(8.dp)
//            ) {
//                Text(
//                    text = post.caption,
//                    color = Color.White,
//                    style = MaterialTheme.typography.body1.copy(textAlign = TextAlign.Center),
//                    modifier = Modifier.align(Alignment.BottomCenter)
//                )
//            }
//        }
//    }
//}

@Composable
fun UserProfileScreen(
    userViewModel: UserViewModel,
    eventViewModel: EventViewModel,
    userId: String,
    navController: NavController
) {
    val targetUser = remember { mutableStateOf<UserRegistration?>(null) }
    val followers = remember { mutableStateOf<List<UserRegistration>>(emptyList()) }
    val following = remember { mutableStateOf<List<UserRegistration>>(emptyList()) }
    var userPosts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var selectedTabIndex by remember { mutableStateOf(0) }

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

    LazyColumn {
        item {
            targetUser.value?.let { currentUser ->
                UserDetailsSection(
                    user = currentUser,
                    followersCount = followers.value.size,
                    followingCount = following.value.size,
                    userViewModel = userViewModel,
                    targetUserId = userId,
                    navController = navController
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Custom Tab Row
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(45.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TabIconWithTitle(
                        count = userPosts.size,
                        title = "Posts",
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 }
                    )
                    TabIconWithTitle(
                        count = following.value.size,  // You can replace this with the appropriate count
                        title = "Events",
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Content based on selected tab
            when (selectedTabIndex) {
                0 -> PostsSection(userPosts)
                1 -> EventsSection(eventViewModel, userId)
            }
        }
    }
}

//@Composable
//fun UserProfileScreen(
//    userViewModel: UserViewModel,
//    eventViewModel: EventViewModel,
//    userId: String,
//    navController: NavController
//) {
//    val targetUser = remember { mutableStateOf<UserRegistration?>(null) }
//    val followers = remember { mutableStateOf<List<UserRegistration>>(emptyList()) }
//    val following = remember { mutableStateOf<List<UserRegistration>>(emptyList()) }
//    var userPosts by remember { mutableStateOf<List<Post>>(emptyList()) }
//    var selectedTabIndex by remember { mutableStateOf(0) }
//
//    LaunchedEffect(userId) {
//        userViewModel.fetchUserById(userId) { fetchedUser ->
//            targetUser.value = fetchedUser
//        }
//        userViewModel.fetchFollowers(userId) {
//            followers.value = it
//        }
//        userViewModel.fetchFollowing(userId) {
//            following.value = it
//        }
//        val postRepository = PostRepository()
//        val posts = postRepository.getUserPosts(userId)
//        userPosts = posts
//    }
//
//    Column {
//        targetUser.value?.let { currentUser ->
//            UserDetailsSection(
//                user = currentUser,
//                followersCount = followers.value.size,
//                followingCount = following.value.size,
//                userViewModel = userViewModel,
//                targetUserId = userId,
//                navController = navController
//            )
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // Custom Tab Row
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 16.dp),
//            contentAlignment = Alignment.Center
//        ) {
//            Row(
//                horizontalArrangement = Arrangement.spacedBy(45.dp),
//                verticalAlignment = Alignment.CenterVertically,
//            ) {
//                TabIconWithTitle(
//                    count = userPosts.size,
//                    title = "Posts",
//                    selected = selectedTabIndex == 0,
//                    onClick = { selectedTabIndex = 0 }
//                )
//                TabIconWithTitle(
//                    count = following.value.size,  // You can replace this with the appropriate count
//                    title = "Events",
//                    selected = selectedTabIndex == 1,
//                    onClick = { selectedTabIndex = 1 }
//                )
//            }
//        }
//
//        // Content based on selected tab
//        when (selectedTabIndex) {
//            0 -> PostsSection(userPosts)
//            1 -> EventsSection(eventViewModel, userId)
//        }
//    }
//}

@Composable
fun TabIconWithTitle(count: Int, title: String, selected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (selected) Color(0xFFF0F8FF) else Color.Transparent // Alice Blue for selected, transparent for unselected
    val textColor = if (selected) Color(0xFF0D6EFD) else Color.Gray

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(backgroundColor)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count.toString(),
                color = textColor,
                style = MaterialTheme.typography.body2, // Smaller font size
                fontWeight = FontWeight.Thin // Thin font weight
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            color = textColor,
            style = MaterialTheme.typography.body2 // Smaller font size
        )
    }
}
//@Composable
//fun FollowButton(userViewModel: UserViewModel, targetUserId: String) {
//    val currentUser by userViewModel.currentUser.observeAsState()
//    val isFollowing = remember { mutableStateOf(currentUser?.following?.contains(targetUserId) == true) }
//
//    LaunchedEffect(currentUser) {
//        isFollowing.value = currentUser?.following?.contains(targetUserId) == true
//    }
//
//    Button(
//        onClick = {
//            if (isFollowing.value) {
//                userViewModel.unfollowUser(targetUserId)
//            } else {
//                userViewModel.followUser(targetUserId)
//            }
//        },
//        colors = ButtonDefaults.buttonColors(
//            backgroundColor = if (isFollowing.value) Color.White else Color(0xFF0D6EFD),
//            contentColor = if (isFollowing.value) Color(0xFF0D6EFD) else Color.White
//        ),
//        border = if (isFollowing.value) BorderStroke(1.dp, Color(0xFF0D6EFD)) else null,
//        shape = RoundedCornerShape(8.dp),
//        modifier = Modifier
//            .width(IntrinsicSize.Min)
//            .height(40.dp)
//            .padding(vertical = 4.dp)
//    ) {
//        Text(
//            text = if (isFollowing.value) "Unfollow" else "Follow",
//            color = if (isFollowing.value) Color(0xFF0D6EFD) else Color.White,
//            style = MaterialTheme.typography.body2,
//            fontWeight = FontWeight.Bold
//        )
//    }
//}

//@Composable
//fun UserDetailsSection(
//    user: UserRegistration,
//    followersCount: Int,
//    followingCount: Int,
//    userViewModel: UserViewModel,
//    targetUserId: String,
//    navController: NavController
//) {
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(16.dp),
//        verticalArrangement = Arrangement.spacedBy(8.dp)
//    ) {
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//        ) {
//            Row {
//                // Profile Image in a card with rounded edges
//                Card(
//                    elevation = 4.dp, // Use Dp for elevation
//                    modifier = Modifier
//                        .size(125.dp),
//                    shape = RoundedCornerShape(12.dp)
//                ) {
//                    Box(modifier = Modifier.fillMaxSize()) {
//                        Image(
//                            painter = rememberImagePainter(data = user.profilePhotoUrl),
//                            contentDescription = "Profile Photo",
//                            modifier = Modifier
//                                .fillMaxSize()
//                                .clip(RoundedCornerShape(12.dp)),
//                            contentScale = ContentScale.Crop
//                        )
//                    }
//                }
//
//                // User details (username, followers, and following) in columns
//                Column(
//                    verticalArrangement = Arrangement.Center,
//                    horizontalAlignment = Alignment.Start,
//                    modifier = Modifier
//                        .weight(1f)
//                        .padding(start = 16.dp)
//                ) {
//                    Text(
//                        text = user.username,
//                        fontWeight = FontWeight.Thin,
//                        fontSize = 20.sp
//                    )
//
//                    Spacer(modifier = Modifier.height(8.dp))
//
//                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
//                        Card(
//                            elevation = 4.dp, // Use Dp for elevation
//                            modifier = Modifier
//                                .padding(4.dp)
//                                .shadow(4.dp, RoundedCornerShape(8.dp))
//                                .background(Color(0xFFF0F8FF)),
//                            shape = RoundedCornerShape(8.dp)
//                        ) {
//                            Column(
//                                horizontalAlignment = Alignment.CenterHorizontally,
//                                modifier = Modifier.padding(8.dp)
//                            ) {
//                                Text(text = "Followers")
//                                Text(
//                                    text = "$followersCount",
//                                    style = MaterialTheme.typography.body2, // Corrected text style reference
//                                    fontWeight = FontWeight.Bold
//                                )
//                            }
//                        }
//                        Card(
//                            elevation = 4.dp, // Use Dp for elevation
//                            modifier = Modifier
//                                .padding(4.dp)
//                                .shadow(4.dp, RoundedCornerShape(8.dp))
//                                .background(Color(0xFFF0F8FF)),
//                            shape = RoundedCornerShape(8.dp)
//                        ) {
//                            Column(
//                                horizontalAlignment = Alignment.CenterHorizontally,
//                                modifier = Modifier.padding(8.dp)
//                            ) {
//                                Text(text = "Following")
//                                Text(
//                                    text = "$followingCount",
//                                    style = MaterialTheme.typography.body2, // Corrected text style reference
//                                    fontWeight = FontWeight.Bold
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        // Follow and Unfollow button
//        Row(
//            modifier = Modifier
//                .fillMaxWidth(),
//            horizontalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//            FollowButton(userViewModel = userViewModel, targetUserId = targetUserId)
//        }
//    }
//}

@Composable
fun UserDetailsSection(
    user: UserRegistration,
    followersCount: Int,
    followingCount: Int,
    userViewModel: UserViewModel,
    targetUserId: String,
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row {
                // Profile Image in a card with rounded edges
                Card(
                    elevation = 4.dp, // Use Dp for elevation
                    modifier = Modifier
                        .size(125.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = rememberImagePainter(data = user.profilePhotoUrl),
                            contentDescription = "Profile Photo",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // User details (username, followers, and following) in columns
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                ) {
                    Text(
                        text = user.username,
                        fontWeight = FontWeight.Thin,
                        fontSize = 20.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Card(
                            elevation = 4.dp, // Use Dp for elevation
                            modifier = Modifier
                                .padding(4.dp)
                                .shadow(4.dp, RoundedCornerShape(8.dp))
                                .background(Color(0xFFF0F8FF)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Text(text = "Followers")
                                Text(
                                    text = "$followersCount",
                                    style = MaterialTheme.typography.body2, // Corrected text style reference
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Card(
                            elevation = 4.dp, // Use Dp for elevation
                            modifier = Modifier
                                .padding(4.dp)
                                .shadow(4.dp, RoundedCornerShape(8.dp))
                                .background(Color(0xFFF0F8FF)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Text(text = "Following")
                                Text(
                                    text = "$followingCount",
                                    style = MaterialTheme.typography.body2, // Corrected text style reference
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Center the Follow button vertically and horizontally
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp), // Ensure the height matches the button's height
            contentAlignment = Alignment.Center
        ) {
            FollowButton(userViewModel = userViewModel, targetUserId = targetUserId)
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
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .width(IntrinsicSize.Min)
            .height(40.dp)
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = if (isFollowing.value) "Unfollow" else "Follow",
            color = if (isFollowing.value) Color(0xFF0D6EFD) else Color.White,
            style = MaterialTheme.typography.body2,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PostsSection(userPosts: List<Post>) {
    Column{
        // Display posts in chunks of two
        userPosts.chunked(2).forEach { rowPosts ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp), // Padding between rows
                horizontalArrangement = Arrangement.SpaceAround // Space around each post
            ) {
                rowPosts.forEach { post ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp) // Padding around each post
                    ) {
                        PostCard(post)
                    }
                }
                // If there's an odd number of posts, add a spacer to fill the remaining space
                if (rowPosts.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun PostCard(post: Post) {

    val aspectRatioModifier = when {
        post.aspectRatio == 1f -> Modifier.height(200.dp)
        post.aspectRatio > 1f -> Modifier.height((200 * post.aspectRatio).dp)
        else -> Modifier.height((200 / post.aspectRatio).dp)
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = 10.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .then(aspectRatioModifier),
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (post.mediaType == "image") {
                // Post Media - Image
                Image(
                    painter = rememberImagePainter(post.mediaUrl),
                    contentDescription = "Post Media",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Post Media - Video
                VideoCard(
                    mediaUrl = post.mediaUrl,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            // Gradient overlay and caption
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)),
                            startY = 300f
                        )
                    )
                    .padding(8.dp)
            ) {
                Text(
                    text = post.caption,
                    color = Color.White,
                    style = MaterialTheme.typography.body1.copy(textAlign = TextAlign.Center),
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@Composable
fun EventsSection(eventViewModel: EventViewModel, userId: String) {
    val userEvents by eventViewModel.events.collectAsState()

    LaunchedEffect(userId) {
        eventViewModel.fetchUserEvents(userId)
    }

    Column {
        userEvents.forEach { event ->
            EventCard(event)
        }
    }
}
@Composable
fun EventCard(event: Event) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = 10.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        ) {
            Image(
                painter = rememberImagePainter(data = event.imageUrl),
                contentDescription = "Event Image",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)),
                            startY = 200f
                        )
                    )
                    .padding(8.dp)
            ) {
                Text(
                    text = event.name,
                    color = Color.White,
                    style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}
