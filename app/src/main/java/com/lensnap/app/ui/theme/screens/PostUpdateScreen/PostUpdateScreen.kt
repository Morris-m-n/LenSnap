package com.lensnap.app.ui.theme.screens.PostUpdateScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.lensnap.app.data.PostRepository
import com.lensnap.app.models.Post
import kotlinx.coroutines.launch

//@Composable
//fun PostUpdateScreen(
//    postId: String,
//    userId: String,
//    postRepository: PostRepository,
//    navController: NavController
//) {
//    // States for loading, post data, and the updated caption
//    val post = remember { mutableStateOf<Post?>(null) }
//    val isLoading = remember { mutableStateOf(true) }
//    val updatedCaption = remember { mutableStateOf("") }
//
//    // Coroutine scope for launching suspend functions
//    val coroutineScope = rememberCoroutineScope()
//
//    // Fetch post data when the screen is launched
//    LaunchedEffect(postId) {
//        val fetchedPost = postRepository.getPostById(userId, postId)
//        post.value = fetchedPost
//        updatedCaption.value = fetchedPost?.caption.orEmpty()
//        isLoading.value = false
//    }
//
//    // Show loading spinner while data is being fetched
//    if (isLoading.value) {
//        CircularProgressIndicator(modifier = Modifier.fillMaxSize())
//    } else {
//        post.value?.let { currentPost ->
//            Column(modifier = Modifier.padding(16.dp)) {
//                // Display the post image
//                Image(
//                    painter = rememberImagePainter(currentPost.mediaUrl),
//                    contentDescription = "Post Media",
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(200.dp)
//                        .clip(RoundedCornerShape(12.dp)),
//                    contentScale = ContentScale.Crop
//                )
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Caption TextField
//                OutlinedTextField(
//                    value = updatedCaption.value,
//                    onValueChange = { updatedCaption.value = it },
//                    label = { Text("Update Caption") },
//                    modifier = Modifier.fillMaxWidth(),
//                    singleLine = false,
//                    maxLines = 5
//                )
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Update Button
//                Button(
//                    onClick = {
//                        // Launch a coroutine to update the caption
//                        coroutineScope.launch {
//                            postRepository.updatePostCaption(userId, postId, updatedCaption.value)
//                            navController.popBackStack() // Navigate back after updating
//                        }
//                    },
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    Text("Update Caption")
//                }
//            }
//        } ?: run {
//            // If post is not found, show a message
//            Text("Post not found")
//        }
//    }
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostUpdateScreen(
    postId: String,
    userId: String,
    postRepository: PostRepository,
    navController: NavController
) {
    val post = remember { mutableStateOf<Post?>(null) }
    val isLoading = remember { mutableStateOf(true) }
    val updatedCaption = remember { mutableStateOf("") }
    val isUpdating = remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(postId) {
        val fetchedPost = postRepository.getPostById(userId, postId)
        post.value = fetchedPost
        updatedCaption.value = fetchedPost?.caption.orEmpty()
        isLoading.value = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Post") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading.value) {
                CircularProgressIndicator(
                    color = Color(0xFF0D6EFD),
                )
            } else {
                post.value?.let { currentPost ->
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Full-Height Image with Padding and Increased Space
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp) // Add horizontal padding
                                .weight(5f) // Increase weight for more vertical space
                                .clip(RoundedCornerShape(12.dp)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Image(
                                painter = rememberImagePainter(currentPost.mediaUrl),
                                contentDescription = "Post Media",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        // Caption Input and Button in Row
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .weight(1f), // Adjust weight for remaining space
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = updatedCaption.value,
                                onValueChange = { updatedCaption.value = it },
                                modifier = Modifier
                                    .weight(1f) // Take up available width
                                    .height(48.dp) // Explicitly set a smaller height
                                .padding(end = 8.dp), // Add spacing between text field and button
                                singleLine = true,
                                maxLines = 1,
                                shape = RoundedCornerShape(24.dp), // Rounded corners
                                textStyle = TextStyle(fontSize = 14.sp), // Adjust font size for a smaller appearance
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    unfocusedBorderColor = Color.Transparent, // No border when unfocused
                                    focusedBorderColor = Color.Transparent, // No border when focused
                                    containerColor = Color(0xFFF0F0F0), // Light gray background
                                    cursorColor = Color(0xFF0D6EFD), // Cursor color
                                    focusedLabelColor = Color(0xFF0D6EFD) // Focused label color
                                )
                            )

                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        isUpdating.value = true
                                        postRepository.updatePostCaption(userId, postId, updatedCaption.value)
                                        isUpdating.value = false

                                        snackbarHostState.showSnackbar("Caption updated successfully!")
                                    }
                                },
                                enabled = !isUpdating.value && updatedCaption.value.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF0D6EFD) // Button background color
                                ),
                                modifier = Modifier.size(56.dp), // Make button circular
                                contentPadding = PaddingValues(0.dp) // Remove default padding
                            ) {
                                if (isUpdating.value) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Update",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                } ?: run {
                    // Post not found
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Post not found")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { navController.popBackStack() }) {
                            Text("Go Back")
                        }
                    }
                }
            }
        }
    }
}
