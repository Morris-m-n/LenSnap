package com.lensnap.app.ui.theme.screens.chatScreen

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.lensnap.app.models.Chat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material.*
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.lensnap.app.data.ChatInviteViewModel
import com.lensnap.app.data.UserViewModel
import com.lensnap.app.models.ChatInvite
import com.lensnap.app.models.UserRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

// Define primary blue color
val PrimaryBlue = Color(0xFF0D6EFD)

@Composable
fun ChatsScreen(
    navController: NavController,
    userViewModel: UserViewModel,
    chatInviteViewModel: ChatInviteViewModel,
    currentUserId: String
) {
    val chats by userViewModel.getUserChats(currentUserId).observeAsState(emptyList())
    val invites by chatInviteViewModel.getChatInvites(currentUserId).observeAsState(emptyList())
    var showFollowDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var rejectInviteId by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    var profilePhotoUrl by remember { mutableStateOf<String?>(null) }
    var username by remember { mutableStateOf<String?>(null) } // Add variable to hold the username

    LaunchedEffect(currentUserId) {
        userViewModel.getUserProfile(currentUserId) { url ->
            profilePhotoUrl = url
        }
        userViewModel.getUserData(currentUserId) { user ->
            username = user?.username
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        profilePhotoUrl?.let { url ->
                            UserProfileImage(profilePhotoUrl = url)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        username?.let { name -> // Display the username instead of "Chats"
                            Text(
                                text = name,
                                color = Color.Black,
                                style = MaterialTheme.typography.titleMedium.copy()
                            )
                        }
                    }
                },
                actions = {},
                backgroundColor = Color.Transparent, // Change the background color to white
                contentColor = Color.Black,
                elevation = 4.dp
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showFollowDialog = true },
                backgroundColor = PrimaryBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Chat", tint = Color.White) // Icon color to white
            }
        },
        backgroundColor = Color(0xFFF0F8FF) // Change the background to Alice Blue
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { newValue ->
                    searchQuery = newValue
                    userViewModel.searchChats(currentUserId, searchQuery)
                },
                label = { Text(text = "Search") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = Color.Gray,
                    backgroundColor = Color.White,
                    cursorColor = PrimaryBlue
                ),
                shape = RoundedCornerShape(10.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Chats",
                color = Color(0xFF0D6EFD), // Primary blue color
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .drawBehind {
                        val strokeWidth = 1f * density
                        val y = size.height - strokeWidth / 2
                        drawLine(
                            color = Color(0xFF0D6EFD),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = strokeWidth
                        )
                    }
            )

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (invites.isNotEmpty()) {
                    items(invites) { invite ->
                        ChatInviteListItem(
                            invite = invite,
                            userViewModel = userViewModel,
                            onAccept = {
                                chatInviteViewModel.acceptChatInvite(invite.id, currentUserId, invite.senderId)
                                navController.navigate("individual_chat/${invite.id}/${invite.senderId}")
                            },
                            onReject = {
                                rejectInviteId = invite.id
                                showRejectDialog = true
                            }
                        )
                        Divider(color = Color.Gray, thickness = 0.5.dp)
                    }
                }

                if (chats.isEmpty() && invites.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "You haven't engaged in any chats yet.",
                                modifier = Modifier.align(Alignment.Center),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    items(chats) { chat ->
                        Log.d("ChatsScreen", "Displaying Chat: ${chat.id}")
                        var user by remember { mutableStateOf<UserRegistration?>(null) }

                        LaunchedEffect(chat.userId) {
                            userViewModel.getUserData(chat.userId) { fetchedUser ->
                                user = fetchedUser
                            }
                        }

                        user?.let {
                            ChatListItem(
                                chat = chat,
                                currentUserId = currentUserId,
                                userViewModel = userViewModel,
                                onClick = { chatId, receiverId ->
                                    navController.navigate("individual_chat/$chatId/$receiverId")
                                    userViewModel.markMessagesAsRead(chat.id, currentUserId)
                                }
                            )
                            Divider(color = Color.Gray, thickness = 0.5.dp)
                        }
                    }
                }
            }

            if (showFollowDialog) {
                NewChatDialog(
                    userViewModel = userViewModel,
                    currentUserId = currentUserId,
                    onDismiss = { showFollowDialog = false },
                    onUserSelected = { userId ->
                        val existingChat = chats.find { it.receiverId == userId }
                        if (existingChat != null) {
                            navController.navigate("individual_chat/${existingChat.id}/$userId")
                        } else {
                            val chatId = UUID.randomUUID().toString()
                            CoroutineScope(Dispatchers.IO).launch {
                                userViewModel.createOrUpdateChat(currentUserId, userId, "Initial message or empty")
                                withContext(Dispatchers.Main) {
                                    navController.navigate("individual_chat/$chatId/$userId")
                                }
                            }
                        }
                    }
                )
            }
            if (showRejectDialog) {
                AlertDialog(
                    onDismissRequest = { showRejectDialog = false },
                    title = {
                        Text(text = "Reject Chat Invite")
                    },
                    text = {
                        Text("Are you sure you want to reject this chat invite?")
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                chatInviteViewModel.rejectChatInvite(rejectInviteId)
                                showRejectDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = PrimaryBlue,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Reject")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = { showRejectDialog = false },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = PrimaryBlue
                            )
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun UserProfileImage(profilePhotoUrl: String) {
    Image(
        painter = rememberImagePainter(data = profilePhotoUrl),
        contentDescription = "Profile Image",
        contentScale = ContentScale.Crop, // Content scale crop
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Gray)
    )
}
@Composable
fun ChatInviteListItem(invite: ChatInvite, userViewModel: UserViewModel, onAccept: () -> Unit, onReject: () -> Unit) {
    var sender by remember { mutableStateOf<UserRegistration?>(null) }

    LaunchedEffect(invite.senderId) {
        userViewModel.getUserData(invite.senderId) { fetchedUser ->
            sender = fetchedUser
        }
    }

    sender?.let { user ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(Color(0xFFF0F8FF)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberImagePainter(data = user.profilePhotoUrl),
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "sent you a chat invite",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Row {
                Button(
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = PrimaryBlue, // Primary Blue background
                        contentColor = Color.White // White text
                    )
                ) {
                    Text(text = "Follow")
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(
                    onClick = onReject,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PrimaryBlue // Primary Blue text
                    )
                ) {
                    Text(text = "Reject")
                }
            }
        }
    }
}
@Composable
fun ChatListItem(
    chat: Chat,
    currentUserId: String,
    userViewModel: UserViewModel,
    onClick: (String, String) -> Unit
) {
    var displayUser by remember { mutableStateOf<UserRegistration?>(null) }

    LaunchedEffect(chat) {
        val userIdToFetch = if (chat.userId == currentUserId) {
            chat.receiverId.takeIf { it.isNotEmpty() } ?: return@LaunchedEffect
        } else {
            chat.userId
        }
        Log.d("ChatListItem", "Fetching data for userIdToFetch: $userIdToFetch")
        userViewModel.getUserData(userIdToFetch) { fetchedUser ->
            displayUser = fetchedUser
            Log.d("ChatListItem", "Fetched data for user: $fetchedUser")
        }
    }

    displayUser?.let { user ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick(chat.id, user.id) }
                .padding(vertical = 4.dp, horizontal = 8.dp),
            elevation = 4.dp, // Add elevation
            shape = RoundedCornerShape(8.dp), // Rounded edges
            backgroundColor = Color(0xFFF0F8FF), // Light white background for the card
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = rememberImagePainter(data = user.profilePhotoUrl),
                    contentDescription = "Profile Image",
                    contentScale = ContentScale.Crop, // Content scale crop
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray) // Slim light white background
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.username,
                        style = MaterialTheme.typography.titleMedium.copy( // Correct typography style
                            fontWeight = FontWeight.Bold,
                            color = Color.Black // Changed color for better visibility
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = chat.lastMessage,
                        style = MaterialTheme.typography.bodySmall, // Correct typography style
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Text(
                        text = formatTimestamp(chat.timestamp),
                        style = MaterialTheme.typography.bodySmall.copy(color = PrimaryBlue)
                    )
                    if (chat.unreadCount > 0) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(4.dp)) // Changed to rounded square for consistency
                                .background(PrimaryBlue)
                        ) {
                            Text(
                                text = if (chat.unreadCount > 9) "9+" else chat.unreadCount.toString(),
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
// Utility function to format timestamp
fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val date = Date(timestamp)
    return sdf.format(date)
}

@Composable
fun NewChatDialog(
    userViewModel: UserViewModel,
    currentUserId: String,
    onDismiss: () -> Unit,
    onUserSelected: (String) -> Unit
) {
    val followingUsers by userViewModel.getFollowingUsers(currentUserId).observeAsState(emptyList())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Start a New Chat") },
        text = {
            LazyColumn {
                items(followingUsers) { user ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onUserSelected(user.id)
                                onDismiss()
                            }
                            .padding(16.dp)
                    ) {
                        Image(
                            painter = rememberImagePainter(data = user.profilePhotoUrl),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = user.username, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
