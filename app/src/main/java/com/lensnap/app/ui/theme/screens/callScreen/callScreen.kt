package com.lensnap.app.ui.theme.screens.callScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.lensnap.app.data.UserViewModel
import com.lensnap.app.models.UserRegistration
//import com.lensnap.app.data.FirebaseSignaling
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
//import initiateVideoCall
//import initiateVoiceCall
import androidx.compose.animation.core.*
import androidx.compose.foundation.shape.CircleShape

@Composable
fun CallScreen(
    navController: NavController,
    receiverId: String,
    userViewModel: UserViewModel,
    isVideoCall: Boolean,
    onHangUpClick: () -> Unit,
//    signaling: FirebaseSignaling
) {
    val context = LocalContext.current
    var receiverUser by remember { mutableStateOf<UserRegistration?>(null) }

    // Fetch user data
    LaunchedEffect(receiverId) {
        userViewModel.getUserData(receiverId) { user ->
            receiverUser = user
        }
    }

//    // Initiate the WebRTC call
//    LaunchedEffect(Unit) {
//        withContext(Dispatchers.IO) {
//            if (isVideoCall) {
//                initiateVideoCall(context, navController, receiverId, userViewModel, signaling)
//            } else {
//                initiateVoiceCall(context, navController, receiverId, userViewModel, signaling)
//            }
//        }
//    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x80000000)), // Semi-dark transparent background
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Display the receiver's profile image in a rounded box and username with loading indicator
            receiverUser?.let {
                Image(
                    painter = rememberImagePainter(data = it.profilePhotoUrl),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White) // Background color to distinguish image
                )
                Spacer(modifier = Modifier.height(16.dp))
                LoadingDots() // Display loading indicator (bouncing dots)
                Spacer(modifier = Modifier.height(16.dp))
                // Display the receiver's username
                Text(
                    text = it.username,
                    style = MaterialTheme.typography.h5.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            } ?: Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                LoadingDots() // Display loading indicator (bouncing dots)
            }
        }

        // Red hang-up button at the center bottom of the screen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 24.dp), // Add some margin at the bottom
            contentAlignment = Alignment.BottomCenter
        ) {
            IconButton(
                onClick = onHangUpClick,
                modifier = Modifier
                    .size(60.dp)
                    .background(Color.Red, RoundedCornerShape(30.dp))
                    .padding(12.dp)
            ) {
                Icon(Icons.Default.CallEnd, contentDescription = "Hang Up", tint = Color.White)
            }
        }
    }
}

@Composable
fun LoadingDots() {
    val maxSize = 8.dp
    val minSize = 4.dp
    val infiniteTransition = rememberInfiniteTransition()
    val scale = infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        )
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 16.dp)
    ) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .size(maxSize.times(scale.value))
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
    }
}