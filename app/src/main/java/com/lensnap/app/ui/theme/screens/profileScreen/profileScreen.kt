package com.lensnap.app.ui.theme.screens.profileScreen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.lensnap.app.models.Post
import com.lensnap.app.models.UserRegistration
import com.lensnap.app.R
import com.lensnap.app.data.PostRepository
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.ui.window.Dialog

private val AliceBlue = Color(0xFFF0F8FF)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ProfileScreen(
    user: UserRegistration,
    onEdit: (UserRegistration) -> Unit,
    onDelete: () -> Unit,
    onAddOrChangeProfilePhoto: () -> Unit,
    onCreatePost: (Uri) -> Unit, // Pass the selected media URI
    onSignOut: () -> Unit
) {
    // Create a launcher for the image picker
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { onCreatePost(it) }
    }

    // State for storing user posts
    var userPosts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var selectedPost by remember { mutableStateOf<Post?>(null) }

    // Fetch user posts
    LaunchedEffect(user.id) {
        val postRepository = PostRepository()
        val posts = postRepository.getUserPosts(user.id)
        userPosts = posts
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Profile") },
                actions = {
                    IconButton(onClick = { showSignOutDialog = true }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More Options")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { launcher.launch("image/* video/*") },
                containerColor = Color(0xFF0D6EFD),
                contentColor = Color.White,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Post"
                )
            }
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AliceBlue)
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp) // Adjusted profile image height
                            ) {
                                Row {
                                    // Profile Image on the left
                                    Image(
                                        painter = if (user.profilePhotoUrl != null) {
                                            rememberImagePainter(user.profilePhotoUrl)
                                        } else {
                                            painterResource(id = R.drawable.placeholder)
                                        },
                                        contentDescription = "Profile Photo",
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Color.Gray)
                                            .clickable { onAddOrChangeProfilePhoto() },
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))

                                    // User details and buttons on the right
                                    Column(
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = user.username,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(text = "Email: ${user.email}")

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            Column {
                                                Text(text = "Followers")
                                                Text(
                                                    text = "${user.followers.size}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            Column {
                                                Text(text = "Following")
                                                Text(
                                                    text = "${user.following.size}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            Button(
                                                onClick = { onEdit(user) },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color(0xFF0D6EFD),
                                                    contentColor = Color.White
                                                )
                                            ) {
                                                Text("Edit Profile")
                                            }
                                            Button(
                                                onClick = { onDelete() },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color.White,
                                                    contentColor = Color(0xFF0D6EFD)
                                                ),
                                                border = BorderStroke(1.dp, Color(0xFF0D6EFD))
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete Account"
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Posts Section
                        Text(
                            text = "Posts",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }

                    items(userPosts.chunked(2)) { rowPosts ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            rowPosts.forEach { post ->
                                Box(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    PostCard(post, onClick = { selectedPost = post })
                                }
                            }
                        }
                    }
                }
            }
        }
    )

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSignOutDialog = false
                        onSignOut()
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    if (selectedPost != null) {
        FullScreenPostDialog(
            post = selectedPost!!,
            onDismiss = { selectedPost = null }
        )
    }
}

@Composable
fun PostCard(post: Post, onClick: () -> Unit) {
    val primaryBlueColor = Color(0xFF0D6EFD) // Bootstrap primary blue color

    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp) // Adjust height as needed
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                painter = rememberImagePainter(post.mediaUrl),
                contentDescription = "Post Media",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop // Ensure image fills the space uniformly
            )
            Text(
                text = post.caption,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(8.dp)
            )
        }
    }
}

@Composable
fun FullScreenPostDialog(post: Post, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent) // Make background transparent
                .clickable { onDismiss() }
        ) {
            Image(
                painter = rememberImagePainter(post.mediaUrl),
                contentDescription = "Full-Screen Post Media",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
        }
    }
}
