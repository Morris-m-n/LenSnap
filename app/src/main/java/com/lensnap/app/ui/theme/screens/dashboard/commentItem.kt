package com.lensnap.app.ui.theme.screens.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.lensnap.app.data.UserViewModel
import com.lensnap.app.ui.theme.screens.userProfileScreen.FollowButton

@Composable
fun CommentItem(
    navController: NavController,
    username: String,
    userId: String,
    comment: String,
    currentUserId: String, // Pass current user's ID
    userViewModel: UserViewModel = viewModel()
) {
    val currentUser = userViewModel.currentUser.observeAsState().value
    val isFollowing = currentUser?.following?.contains(userId) == true
    val displayedUsername = if (userId == currentUserId) "You" else username

    Row(modifier = Modifier.fillMaxWidth()) {
        // Clickable Username
        Text(
            text = displayedUsername,
            color = Color(0xFF0D6EFD),
            modifier = Modifier.clickable {
                if (userId == currentUserId) {
                    navController.navigate("profile") // Navigate to profile screen for current user
                } else {
                    navController.navigate("userProfile/$userId") // Navigate to user profile screen for others
                }
            }
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Follow/Unfollow Button for other users
        if (userId != currentUserId) {
            FollowButton(userViewModel = userViewModel, targetUserId = userId)
        }
    }

    Spacer(modifier = Modifier.width(8.dp))

    // Comment Text
    Text(text = comment)
}
