//package com.lensnap.app.ui.theme.screens.chatScreen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.lensnap.app.data.UserViewModel
import com.lensnap.app.models.UserRegistration
import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.database.*
//import com.lensnap.app.data.WebRTCManager
import com.lensnap.app.models.Message
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.shape.CircleShape
import androidx.navigation.NavController
//import com.lensnap.app.data.FirebaseSignaling
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.indication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.lensnap.app.R
import com.lensnap.app.models.MessageType
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

val PrimaryBlue = Color(0xFF0D6EFD) // Define your primary blue color

@Composable
fun IndividualChatScreen(
    chatId: String,
    userViewModel: UserViewModel,
    receiverId: String,
    imagePickerLauncher: ActivityResultLauncher<String>,
    selectedImageUri: Uri?,
    navController: NavController,
    eventImageUrl: String? // New parameter for event image URL
) {
    val messages by userViewModel.getChatMessages(chatId).observeAsState(emptyList())
    var message by remember { mutableStateOf("") }
    var receiverUser by remember { mutableStateOf<UserRegistration?>(null) }
    val currentUser = userViewModel.getCurrentUser()
    val listState = rememberLazyListState()
    val isTyping by remember { derivedStateOf { message.isNotEmpty() } }
    var typingStatus by remember { mutableStateOf(false) }
    var mediaUri by remember { mutableStateOf(selectedImageUri) }
    var mediaUrl by remember { mutableStateOf<String?>(eventImageUrl) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isEvent = eventImageUrl != null // Flag to determine if it's an event message

    // Retrieve and decode eventImageUrl from the query parameter
    val eventImageUrlFromQuery = navController.currentBackStackEntry?.arguments?.getString("eventImageUrl")?.let {
        URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
    }

    // Update mediaUri when selectedImageUri changes
    LaunchedEffect(selectedImageUri) {
        mediaUri = selectedImageUri
        mediaUrl = selectedImageUri?.toString() ?: eventImageUrlFromQuery
        Log.d("IndividualChatScreen", "Updated mediaUri: $mediaUri, mediaUrl: $mediaUrl")
    }

    // Update mediaUrl when eventImageUrl changes
    LaunchedEffect(eventImageUrl) {
        mediaUrl = eventImageUrl
        Log.d("IndividualChatScreen", "Updated mediaUrl from eventImageUrl: $mediaUrl")
    }

    // Log the mediaUrl
    LaunchedEffect(mediaUrl) {
        Log.d("IndividualChatScreen", "Media URL set to: $mediaUrl")
    }

    // Fetch receiver user data and monitor typing status
    LaunchedEffect(receiverId) {
        userViewModel.getUserData(receiverId) { user -> receiverUser = user }
        userViewModel.getTypingStatusFromRealtimeDatabase(receiverId) { isTyping -> typingStatus = isTyping }
    }

    // Scroll to the latest message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.scrollToItem(messages.size - 1)
        }
    }

    // Mark messages as seen
    LaunchedEffect(messages) {
        val messagesToMarkSeen = messages.filter {
            it.receiverId == currentUser?.id && it.status == "delivered"
        }
        messagesToMarkSeen.forEach { message ->
            userViewModel.markMessageAsSeen(message.id, chatId, receiverId)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF0F8FF)) // Alice Blue background, or consider a neutral off-white color
        ) {
            Spacer(modifier = Modifier.height(64.dp))  // Adjust height to match the TopAppBar's height

            // Message List with weighted layout
            Box(
                modifier = Modifier
                    .weight(1f)  // Ensure this takes the remaining space
                    .fillMaxWidth()
                    .background(Color.Transparent)
            ) {
                MessageList(messages = messages, currentUser = currentUser, listState = listState)
            }

            Spacer(modifier = Modifier.height(64.dp))  // Adjust height to match the ChatInputField's height
        }

        // Floating Top App Bar
        TopAppBarWithRealtimeStatus(
            navController = navController,
            receiverId = receiverId,
            receiverUser = receiverUser,
            typingStatus = typingStatus,
            userViewModel = userViewModel
        )

        // Ensure Chat Input Field and Media Preview are at the bottom of the screen
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            // Media preview floating above the ChatInputField
            MediaPreview(mediaUrl = mediaUrl) { mediaUrl = null }

            // Chat Input Field
            ChatInputField(
                message = message,
                onMessageChange = { newValue ->
                    message = newValue
                    userViewModel.updateTypingStatusInRealtimeDatabase(currentUser?.id ?: "", true)
                },
                onSendMessage = {
                    sendMessage(
                        message = message,
                        mediaUri = mediaUri,
                        chatId = chatId,
                        receiverId = receiverId,
                        currentUser = currentUser,
                        userViewModel = userViewModel,
                        coroutineScope = coroutineScope,
                        listState = listState,
                        isEvent = isEvent, // Use the flag to determine if it's an event message
                        eventImageUrl = eventImageUrl // Pass the event image URL
                    )
                    message = ""
                    mediaUri = null
                    mediaUrl = null  // Clear the mediaUrl after sending the message
                },
                imagePickerLauncher = imagePickerLauncher,
                isTyping = isTyping,
                onTypingStopped = {
                    userViewModel.updateTypingStatusInRealtimeDatabase(currentUser?.id ?: "", false)
                },
                mediaUri = mediaUri,
                mediaUrl = mediaUrl // Add mediaUrl parameter
            )
        }
    }
}

//@Composable
//fun IndividualChatScreen(
//    chatId: String,
//    userViewModel: UserViewModel,
//    receiverId: String,
//    imagePickerLauncher: ActivityResultLauncher<String>,
//    selectedImageUri: Uri?,
//    navController: NavController,
//    eventImageUrl: String?
//) {
//    val messages by userViewModel.getChatMessages(chatId).observeAsState(emptyList())
//    var message by remember { mutableStateOf("") }
//    var receiverUser by remember { mutableStateOf<UserRegistration?>(null) }
//    val currentUser = userViewModel.getCurrentUser()
//    val listState = rememberLazyListState()
//    val isTyping by remember { derivedStateOf { message.isNotEmpty() } }
//    var typingStatus by remember { mutableStateOf(false) }
//    var mediaUri by remember { mutableStateOf(selectedImageUri) }
//    var mediaUrl by remember { mutableStateOf(eventImageUrl) }
//    val context = LocalContext.current
//    val coroutineScope = rememberCoroutineScope()
//
//    // Retrieve and decode eventImageUrl from the query parameter
//    val eventImageUrlFromQuery = navController.currentBackStackEntry?.arguments?.getString("eventImageUrl")?.let {
//        URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
//    }
//
//    // Update mediaUri when selectedImageUri changes
//    LaunchedEffect(selectedImageUri) {
//        mediaUri = selectedImageUri
//        mediaUrl = selectedImageUri?.toString() ?: eventImageUrlFromQuery
//        Log.d("IndividualChatScreen", "Updated mediaUri: $mediaUri, mediaUrl: $mediaUrl")
//    }
//
//    // Update mediaUrl when eventImageUrl changes
//    LaunchedEffect(eventImageUrl) {
//        mediaUrl = eventImageUrl
//        Log.d("IndividualChatScreen", "Updated mediaUrl from eventImageUrl: $mediaUrl")
//    }
//
//    // Log the mediaUrl
//    LaunchedEffect(mediaUrl) {
//        Log.d("IndividualChatScreen", "Media URL set to: $mediaUrl")
//    }
//
//    // Fetch receiver user data and monitor typing status
//    LaunchedEffect(receiverId) {
//        userViewModel.getUserData(receiverId) { user -> receiverUser = user }
//        userViewModel.getTypingStatusFromRealtimeDatabase(receiverId) { isTyping -> typingStatus = isTyping }
//    }
//
//    // Scroll to the latest message
//    LaunchedEffect(messages.size) {
//        if (messages.isNotEmpty()) {
//            listState.scrollToItem(messages.size - 1)
//        }
//    }
//
//    // Mark messages as seen
//    LaunchedEffect(messages) {
//        val messagesToMarkSeen = messages.filter {
//            it.receiverId == currentUser?.id && it.status == "delivered"
//        }
//        messagesToMarkSeen.forEach { message ->
//            userViewModel.markMessageAsSeen(message.id, chatId, receiverId)
//        }
//    }
//
//    Box(modifier = Modifier.fillMaxSize()) {
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(Color(0xFFF0F8FF)) // Alice Blue background, or consider a neutral off-white color
//        ) {
//            Spacer(modifier = Modifier.height(64.dp))  // Adjust height to match the TopAppBar's height
//
//            // Message List with weighted layout
//            Box(
//                modifier = Modifier
//                    .weight(1f)  // Ensure this takes the remaining space
//                    .fillMaxWidth()
//                    .background(Color.Transparent)
//            ) {
//                MessageList(messages = messages, currentUser = currentUser, listState = listState)
//            }
//
//            Spacer(modifier = Modifier.height(64.dp))  // Adjust height to match the ChatInputField's height
//        }
//
//        // Floating Top App Bar
//        TopAppBarWithRealtimeStatus(
//            navController = navController,
//            receiverId = receiverId,
//            receiverUser = receiverUser,
//            typingStatus = typingStatus,
//            userViewModel = userViewModel
//        )
//
//        // Ensure Chat Input Field and Media Preview are at the bottom of the screen
//        Column(
//            modifier = Modifier
//                .align(Alignment.BottomCenter)
//                .fillMaxWidth()
//        ) {
//            // Media preview floating above the ChatInputField
//            MediaPreview(mediaUrl = mediaUrl) { mediaUrl = null }
//
//            // Chat Input Field
//            ChatInputField(
//                message = message,
//                onMessageChange = { newValue ->
//                    message = newValue
//                    userViewModel.updateTypingStatusInRealtimeDatabase(currentUser?.id ?: "", true)
//                },
//                onSendMessage = {
//                    sendMessage(
//                        message = message,
//                        mediaUri = mediaUri,
//                        chatId = chatId,
//                        receiverId = receiverId,
//                        currentUser = currentUser,
//                        userViewModel = userViewModel,
//                        coroutineScope = coroutineScope,
//                        listState = listState
//                    )
//                    message = ""
//                    mediaUri = null
//                    mediaUrl = null  // Clear the mediaUrl after sending the message
//                },
//                imagePickerLauncher = imagePickerLauncher,
//                isTyping = isTyping,
//                onTypingStopped = {
//                    userViewModel.updateTypingStatusInRealtimeDatabase(currentUser?.id ?: "", false)
//                },
//                mediaUri = mediaUri,
//                mediaUrl = mediaUrl // Add mediaUrl parameter
//            )
//        }
//    }
//}

@Composable
fun MediaPreview(mediaUrl: String?, onCancel: () -> Unit) {
    mediaUrl?.let { url ->
        Log.d("MediaPreview", "Displaying media URL: $url")
        Box(
            modifier = Modifier
                .height(100.dp)  // Adjust height as needed
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Image(
                painter = rememberImagePainter(data = url),
                contentDescription = "Selected Media",
                modifier = Modifier
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))  // Rounded corners for media preview
            )
            IconButton(
                onClick = onCancel,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Cancel Selection", tint = Color(0xFF0D6EFD))
            }
        }
    }
}

@Composable
fun MessageList(
    messages: List<Message>,
    currentUser: UserRegistration?,
    listState: LazyListState
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxHeight(), // Ensures LazyColumn fills available vertical space
            contentPadding = PaddingValues(8.dp) // Padding between list items
        ) {
            items(messages) { message ->
                val isSender = message.senderId == currentUser?.id
                val backgroundColor = if (isSender) Color(0xFF0D6EFD) else Color(0xFFCCCCCC)
                val textColor = if (isSender) Color.White else Color.Black
                val timeColor = if (isSender) Color.Gray else Color.Gray

                // Message Column with vertical padding for better separation
                Column(
                    horizontalAlignment = if (isSender) Alignment.End else Alignment.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)  // Reduced padding between message items
                ) {
                    when (message.type) {
                        MessageType.EVENT -> {
                            message.mediaUrl?.let { mediaUrl ->
                                EventMessageItem(mediaUrl = mediaUrl, isSender = isSender)
                            }
                        }
                        MessageType.MEDIA -> {
                            message.mediaUrl?.let { mediaUrl ->
                                MediaMessage(mediaUrl = mediaUrl, isSender = isSender)
                            }
                        }
                        MessageType.TEXT -> {
                            if (message.content.isNotBlank()) {
                                MessageBubble(
                                    message = message,
                                    backgroundColor = backgroundColor,
                                    textColor = textColor,
                                    timeColor = timeColor,
                                    isSender = isSender
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

//ORIGINAL
//@Composable
//fun MessageList(
//    messages: List<Message>,
//    currentUser: UserRegistration?,
//    listState: LazyListState
//) {
//    Box(
//        modifier = Modifier.fillMaxSize()
//    ) {
//        LazyColumn(
//            state = listState,
//            modifier = Modifier
//                .fillMaxHeight(), // Ensures LazyColumn fills available vertical space
//            contentPadding = PaddingValues(8.dp) // Padding between list items
//        ) {
//            items(messages) { message ->
//                val isSender = message.senderId == currentUser?.id
//                val backgroundColor = if (isSender) Color(0xFF0D6EFD) else Color(0xFFCCCCCC)
//                val textColor = if (isSender) Color.White else Color.Black
//                val timeColor = if (isSender) Color.Gray else Color.Gray
//
//                // Message Column with vertical padding for better separation
//                Column(
//                    horizontalAlignment = if (isSender) Alignment.End else Alignment.Start,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 4.dp)  // Reduced padding between message items
//                ) {
//                    // Conditionally render Media Message if available
//                    message.mediaUrl?.let { mediaUrl ->
//                        MediaMessage(mediaUrl = mediaUrl, isSender = isSender)
//                    }
//
//                    // Conditionally render Text Message if available
//                    if (message.content.isNotBlank()) {
//                        MessageBubble(
//                            message = message,
//                            backgroundColor = backgroundColor,
//                            textColor = textColor,
//                            timeColor = timeColor,
//                            isSender = isSender
//                        )
//                    }
//                }
//            }
//        }
//    }
//}

@Composable
fun EventMessageItem(mediaUrl: String, isSender: Boolean) {
    val alignment = if (isSender) Alignment.CenterEnd else Alignment.CenterStart
    val backgroundColor = if (isSender) Color(0xFF0D6EFD) else Color(0xFFCCCCCC)

    Box(
        contentAlignment = alignment,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column {
                Image(
                    painter = rememberImagePainter(data = mediaUrl),
                    contentDescription = "Event Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Text(
                    text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()),
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: Message,
    backgroundColor: Color,
    textColor: Color,
    timeColor: Color,
    isSender: Boolean
) {
    Column(
        horizontalAlignment = if (isSender) Alignment.End else Alignment.Start,
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))  // Slightly smaller rounded corners
                .background(backgroundColor)
                .padding(8.dp)  // Padding around the text
                .widthIn(max = 250.dp)  // Set maximum width
        ) {
            Column(
                horizontalAlignment = if (isSender) Alignment.End else Alignment.Start
            ) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.body2.copy(fontSize = 16.sp),  // Reduced size of message text
                    color = textColor
                )
                if (isSender) {
                    Icon(
                        imageVector = when (message.status) {
                            "sent" -> Icons.Default.Check
                            "delivered" -> Icons.Default.Check
                            "seen" -> Icons.Default.DoneAll
                            else -> Icons.Default.Check
                        },
                        contentDescription = "Message Status",
                        tint = when (message.status) {
                            "sent" -> Color.Gray
                            "delivered" -> Color.Blue
                            "seen" -> Color.Blue
                            else -> Color.Gray
                        },
                        modifier = Modifier.size(14.dp)  // Smaller icon size
                    )
                }
            }
        }
        Text(
            text = formatTimestamp(message.timestamp),
            style = MaterialTheme.typography.caption.copy(color = timeColor),
            modifier = Modifier
                .padding(top = 2.dp)  // Padding for separation
                .offset(y = (-10).dp)  // Slightly overlapping the message bubble
                .shadow(4.dp, CircleShape)  // Adding a shadow effect
                .background(backgroundColor)  // Matching background color with the bubble
                .padding(horizontal = 4.dp, vertical = 2.dp)  // Padding around the timestamp
        )
    }
}

@Composable
fun MediaMessage(mediaUrl: String, isSender: Boolean) {
    Row(
        horizontalArrangement = if (isSender) Arrangement.End else Arrangement.Start,
        modifier = Modifier
            .fillMaxWidth(0.75f)
            .padding(top = 8.dp)  // Padding for separation
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)  // Maintain aspect ratio (can be adjusted if needed)
                .clip(RoundedCornerShape(16.dp))  // Rounded corners for media
                .background(Color.LightGray.copy(alpha = 0.2f))  // Light background shadow effect
                .padding(4.dp)  // Small padding inside the image
        ) {
            Image(
                painter = rememberImagePainter(
                    data = mediaUrl,
                    builder = {
                        placeholder(R.drawable.placeholder)  // Placeholder image from drawable
                        error(R.drawable.error)  // Error image from drawable
                    }
                ),
                contentDescription = "Media",
                contentScale = ContentScale.Crop,  // Ensures the image covers the allocated space
                modifier = Modifier
                    .fillMaxSize()  // Make sure the image fills the allocated space
                    .clip(RoundedCornerShape(16.dp))  // Rounded corners for media
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInputField(
    message: String,
    onMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    imagePickerLauncher: ActivityResultLauncher<String>,
    isTyping: Boolean,
    mediaUri: Uri?,
    mediaUrl: String?, // Add mediaUrl parameter
    onTypingStopped: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(8.dp) // Add padding to float above the bottom
            .background(Color.Transparent)
            .fillMaxHeight(0.1f)
            .fillMaxWidth()  // Make sure it takes up the full width
            .shadow(8.dp, RoundedCornerShape(16.dp), clip = true)  // Add shadow for a floating effect
            .background(Color.White, RoundedCornerShape(16.dp))  // Add a white background with rounded corners
            .padding(8.dp)  // Add padding inside the row for spacing
    ) {
        if (!isTyping) {
            IconButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                Icon(Icons.Default.AttachFile, contentDescription = "Select Photo", tint = Color.Black)  // Gallery icon to the left
            }
        }

        BasicTextField(
            value = message,
            onValueChange = onMessageChange,
            textStyle = TextStyle(fontSize = 14.sp),
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp)  // Adjust padding to center the text vertically
                .clip(RoundedCornerShape(16.dp))  // Rounding the corners
                .background(Color.LightGray.copy(alpha = 0.1f))  // Light background inside the text field
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused) onTypingStopped()
                },
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)  // Add padding around the text and icons
                        .fillMaxHeight(),  // Make the inner Box take up the full height
                    contentAlignment = Alignment.CenterStart  // Center the text vertically
                ) {
                    if (message.isEmpty()) {
                        Text("Type message...", color = Color.Gray, fontSize = 14.sp)  // Placeholder text
                    }
                    innerTextField()
                }
            },
            maxLines = 5  // Limit the number of lines for readability
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = {
                if (message.isNotBlank() || mediaUri != null || mediaUrl != null) onSendMessage()  // Enable sending if the text field is not empty, media is selected, or mediaUrl is provided
            },
            enabled = message.isNotBlank() || mediaUri != null || mediaUrl != null,  // Enable the button if any condition is met
            modifier = Modifier
                .clip(CircleShape)
                .background(if (message.isBlank() && mediaUri == null && mediaUrl == null) Color.Transparent else PrimaryBlue)  // Change background based on text field content, media selection, or mediaUrl
                .size(40.dp)
        ) {
            Icon(
                Icons.Default.Done,  // Using tick icon
                contentDescription = "Send Message",
                tint = if (message.isBlank() && mediaUri == null && mediaUrl == null) PrimaryBlue else Color.White  // Change icon color based on text field content, media selection, or mediaUrl
            )
        }
    }
}

fun sendMessage(
    message: String,
    mediaUri: Uri?,
    chatId: String,
    receiverId: String,
    currentUser: UserRegistration?,
    userViewModel: UserViewModel,
    coroutineScope: CoroutineScope,
    listState: LazyListState,
    isEvent: Boolean = false, // Flag to indicate if the message is an event
    eventImageUrl: String? = null // Event image URL for event messages
) {
    coroutineScope.launch {
        // Determine the message type based on isEvent flag and mediaUri
        val messageType = if (isEvent) {
            MessageType.EVENT
        } else if (mediaUri != null) {
            MessageType.MEDIA
        } else {
            MessageType.TEXT
        }

        if (isEvent) {
            Log.d("sendMessage", "Sending an EVENT message with event image URL: $eventImageUrl")
            userViewModel.sendMessage(
                Message(
                    id = UUID.randomUUID().toString(),
                    chatId = chatId,
                    senderId = currentUser?.id ?: "",
                    receiverId = receiverId,
                    content = message,
                    timestamp = System.currentTimeMillis(),
                    status = "sent",
                    mediaUrl = eventImageUrl,
                    type = messageType
                )
            )
        } else {
            mediaUri?.let { uri ->
                val mediaUrl = userViewModel.uploadMedia(chatId, uri)
                Log.d("sendMessage", "Sending a MEDIA message with media URL: $mediaUrl")
                userViewModel.sendMessage(
                    Message(
                        id = UUID.randomUUID().toString(),
                        chatId = chatId,
                        senderId = currentUser?.id ?: "",
                        receiverId = receiverId,
                        content = message,
                        timestamp = System.currentTimeMillis(),
                        status = "sent",
                        mediaUrl = mediaUrl,
                        type = messageType
                    )
                )
            } ?: run {
                Log.d("sendMessage", "Sending a TEXT message with content: $message")
                userViewModel.sendMessage(
                    Message(
                        id = UUID.randomUUID().toString(),
                        chatId = chatId,
                        senderId = currentUser?.id ?: "",
                        receiverId = receiverId,
                        content = message,
                        timestamp = System.currentTimeMillis(),
                        status = "sent",
                        type = messageType
                    )
                )
            }
        }

        // Ensure the list scrolls to the latest message
        listState.scrollToItem(userViewModel.getChatMessages(chatId).value?.size ?: 0)
    }
}

//ORIGINAL
//fun sendMessage(
//    message: String,
//    mediaUri: Uri?,
//    chatId: String,
//    receiverId: String,
//    currentUser: UserRegistration?,
//    userViewModel: UserViewModel,
//    coroutineScope: CoroutineScope,
//    listState: LazyListState
//) {
//    coroutineScope.launch {
//        mediaUri?.let { uri ->
//            val mediaUrl = userViewModel.uploadMedia(chatId, uri)
//            userViewModel.sendMessage(
//                Message(
//                    id = UUID.randomUUID().toString(),
//                    chatId = chatId,
//                    senderId = currentUser?.id ?: "",
//                    receiverId = receiverId,
//                    content = message,
//                    timestamp = System.currentTimeMillis(),
//                    status = "sent",
//                    mediaUrl = mediaUrl
//                )
//            )
//        } ?: run {
//            userViewModel.sendMessage(
//                Message(
//                    id = UUID.randomUUID().toString(),
//                    chatId = chatId,
//                    senderId = currentUser?.id ?: "",
//                    receiverId = receiverId,
//                    content = message,
//                    timestamp = System.currentTimeMillis(),
//                    status = "sent"
//                )
//            )
//        }
//        listState.scrollToItem(userViewModel.getChatMessages(chatId).value?.size ?: 0)
//    }
//}


// Utility function to format timestamp
fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val date = Date(timestamp)
    return sdf.format(date)
}

@Composable
fun TopAppBarWithRealtimeStatus(
    navController: NavController,
    receiverId: String,
    receiverUser: UserRegistration?,
    typingStatus: Boolean,
    userViewModel: UserViewModel,
) {
    val userStatus = remember { mutableStateOf("Loading...") }
    val database = FirebaseDatabase.getInstance()
    val context = LocalContext.current

    // Fetch status with retry logic
    fun fetchStatusWithRetry(retryCount: Int = 3) {
        val userStatusDatabaseRef = database.getReference("/status/$receiverId")

        userStatusDatabaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.value as? String
                userStatus.value = when {
                    typingStatus -> "Typing..."
                    status == "online" -> "Online"
                    status is Long -> "Last active: ${formatTimestamp(status)}"
                    else -> "Offline"
                }
                Log.d("TopAppBarStatus", "Status updated: ${userStatus.value}")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("TopAppBarStatus", "Listener was cancelled: ${error.message}")
                if (retryCount > 0) {
                    fetchStatusWithRetry(retryCount - 1)
                } else {
                    userStatus.value = "Offline (Error)"
                }
            }
        })
    }

    // Fetch Realtime Database status for the receiver user
    LaunchedEffect(receiverId) {
        fetchStatusWithRetry()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp), clip = true)  // Add shadow for a floating effect
            .background(Color.Transparent),  // Make background transparent
        contentAlignment = Alignment.Center  // Center content horizontally and vertically
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .background(Color.Transparent)  // Transparent background for Column
                .fillMaxWidth()
        ) {
            receiverUser?.let {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))  // Rounded corners for box shape
                        .background(Color.Transparent)  // Transparent background for the image container
                        .shadow(4.dp, shape = RoundedCornerShape(8.dp))  // Add shadow effect
                ) {
                    Image(
                        painter = rememberImagePainter(data = it.profilePhotoUrl),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.Transparent)  // Transparent background for Image
                    )
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(
                                when (userStatus.value) {
                                    "Online" -> Color.Blue
                                    else -> Color.Red
                                }
                            )
                            .align(Alignment.BottomEnd)  // Align dot to the bottom end of the image
                            .border(1.dp, Color.White, CircleShape)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = it.username,
                    style = MaterialTheme.typography.body1.copy(
                        fontWeight = FontWeight.Thin,
                        color = Color.Black
                    ),
                    modifier = Modifier
                        .background(Color.Transparent)  // Transparent background for Text
                        .padding(horizontal = 8.dp)  // Optional padding for aesthetics
                )
            } ?: Text(
                text = "Loading...",
                style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.background(Color.Transparent)  // Transparent background for Text
            )
        }
    }
}
