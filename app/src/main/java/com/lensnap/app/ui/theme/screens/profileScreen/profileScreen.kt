package com.lensnap.app.ui.theme.screens.profileScreen

import android.net.Uri
import android.util.Log
import android.widget.Toast
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
import com.lensnap.app.R
import com.lensnap.app.data.PostRepository
import androidx.compose.material.icons.filled.MoreVert
import com.lensnap.app.models.UserRegistration
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.lensnap.app.data.UserViewModel
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavArgument
import androidx.navigation.NavType
import com.lensnap.app.data.DailyUpdatesViewModel
import com.lensnap.app.ui.DailyUpdateScreen
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import com.google.accompanist.pager.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

private val AliceBlue = Color(0xFFF0F8FF)


@Composable
fun ProfileScreen(
    user: UserRegistration,
    navController: NavController, // Accept NavController here
    onEdit: (UserRegistration) -> Unit,
    onDelete: () -> Unit,
    onAddOrChangeProfilePhoto: () -> Unit,
    onCreatePost: (Uri) -> Unit,
    onSignOut: () -> Unit,
    userViewModel: UserViewModel,
    eventViewModel: EventViewModel,
    dailyUpdatesViewModel: DailyUpdatesViewModel = viewModel(),
    postRepository: PostRepository
) {
    // Create a launcher for the image picker
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { onCreatePost(it) }
    }

    // State variables
    var selectedMediaUri by remember { mutableStateOf<Uri?>(null) }
    var showDailyUpdateScreen by remember { mutableStateOf(false) }

    // Create a launcher for daily updates
    val dailyUpdateLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedMediaUri = it
            showDailyUpdateScreen = true
        }
    }

    // State variables
    var userPosts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var userEvents by remember { mutableStateOf<List<Event>>(emptyList()) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var selectedPostIndex by remember { mutableStateOf<Int?>(null) }
//    val navController = rememberNavController()

    // Fetch user posts and events
    LaunchedEffect(user.id) {
        val postRepository = PostRepository()
        userPosts = postRepository.getUserPosts(user.id)
        eventViewModel.fetchUserEvents(user.id)
        dailyUpdatesViewModel.fetchDailyUpdates(user.id)
        dailyUpdatesViewModel.removeExpiredUpdates(user.id)
    }

    // Observe events LiveData from EventViewModel
    val eventsState by eventViewModel.events.collectAsState(initial = emptyList()) // Using StateFlow for events
    userEvents = eventsState

    // State for tab selection
    var selectedTab by remember { mutableStateOf(0) }

    // Sample captured images for events
    val capturedImages = remember { mutableStateListOf<String>() }
    val currentUserId = user.id
    val currentUsername = user.username

    Scaffold(
        topBar = { ProfileTopAppBar { showSignOutDialog = true } },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { launcher.launch("image/* video/*") },
                containerColor = Color(0xFF0D6EFD),
                contentColor = Color.White,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Create Post")
            }
        },
        content = { paddingValues ->
            if (showDailyUpdateScreen && selectedMediaUri != null) {
                // Updated ProfileScreen call to DailyUpdateScreen
                DailyUpdateScreen(
                    userId = currentUserId,
                    uri = selectedMediaUri!!,
                    username = currentUsername,
                    profilePhotoUrl = user.profilePhotoUrl ?: "", // Provide a default value if null
                    onUpload = { userId, dailyUpdate, uri ->
                        dailyUpdatesViewModel.addDailyUpdate(
                            userId = userId,
                            update = dailyUpdate,
                            uri = uri,
                            username = currentUsername,
                            profilePhotoUrl = user.profilePhotoUrl ?: "" // Provide a default value if null
                        )
                        showDailyUpdateScreen = false
                        selectedMediaUri = null
                    },
                    onCancel = {
                        showDailyUpdateScreen = false
                        selectedMediaUri = null
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    item {
                        ProfileHeader(
                            user = user,
                            onAddOrChangeProfilePhoto = onAddOrChangeProfilePhoto,
                            onEdit = onEdit,
                            onDelete = onDelete,
                            onCreateDailyUpdate = { dailyUpdateLauncher.launch("image/* video/*") }
                        )
                    }
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = Modifier
                                    .background(Color.Transparent),
                                horizontalArrangement = Arrangement.spacedBy(45.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TabIconWithTitle(
                                    count = userPosts.size, // Number of posts
                                    title = "Posts",
                                    selected = selectedTab == 0,
                                    onClick = { selectedTab = 0 }
                                )
                                TabIconWithTitle(
                                    count = userEvents.size, // Number of events
                                    title = "Events",
                                    selected = selectedTab == 1,
                                    onClick = { selectedTab = 1 }
                                )
                            }
                        }
                    }
                    item {
                        if (selectedTab == 0) {
                            PostsSection(
                                posts = userPosts,
                                onPostClick = { post ->
                                    selectedPostIndex = userPosts.indexOf(post)
                                    println("Post clicked, index: $selectedPostIndex")
                                },
                                navController = navController, // Pass the NavController here
                                postRepository = postRepository
                            )

                        } else {
                            EventsSection(
                                events = userEvents,
                                capturedImages = capturedImages,
                                onEventClick = { event ->
                                    navController.navigate("event_update_screen/${event.id}")
                                },
                                onUpdateClick = { event ->
                                    navController.navigate("event_update_screen/${event.id}")
                                },
                                onDeleteClick = { event ->
                                    eventViewModel.deleteEvent(
                                        eventId = event.id,
                                        onSuccess = {
                                            Log.d("EventsSection", "Event deleted successfully: ${event.id}")
                                            // Optionally refresh the event list or provide user feedback
                                        },
                                        onError = { errorMessage ->
                                            Log.e("EventsSection", "Failed to delete event: $errorMessage")
                                        }
                                    )
                                },
                                eventViewModel = eventViewModel,
                                currentUserId = currentUserId,
                                currentUsername = currentUsername,
                                navController = navController,
                                userViewModel = userViewModel
                            )

                        }
                    }
                }
            }
        }
    )

    if (showSignOutDialog) {
        SignOutDialog(
            onDismiss = { showSignOutDialog = false },
            onSignOut = {
                showSignOutDialog = false
                onSignOut()
            }
        )
    }

    if (selectedPostIndex != null) {
        println("Showing FullScreenPostDialog for index: $selectedPostIndex")
        key(selectedPostIndex) {
            FullScreenPostDialog(
                posts = userPosts,
                initialPostIndex = selectedPostIndex!!,
                onDismiss = {
                    println("Dialog dismissed")
                    selectedPostIndex = null // Reset state
                }
            )
        }
    }
}

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
                .size(48.dp)
                .clip(CircleShape)
                .background(backgroundColor)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count.toString(),
                color = textColor,
                style = MaterialTheme.typography.bodyLarge, // Bigger font size
                fontWeight = FontWeight.Thin // Thin font weight
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            color = textColor,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTopAppBar(onMoreOptionsClick: () -> Unit) {
    TopAppBar(
        title = { Text(text = "Profile") },
        actions = {
            IconButton(onClick = onMoreOptionsClick) {
                Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More Options")
            }
        }
    )
}
@Composable
fun ProfileHeader(
    user: UserRegistration,
    onAddOrChangeProfilePhoto: () -> Unit,
    onEdit: (UserRegistration) -> Unit,
    onDelete: () -> Unit,
    onCreateDailyUpdate: () -> Unit
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
//                .height(150.dp)
        ) {
            Row {
                // Profile Image in a card with rounded edges
                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .size(125.dp)
                        .clickable { onAddOrChangeProfilePhoto() },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = if (user.profilePhotoUrl != null) {
                                rememberImagePainter(user.profilePhotoUrl)
                            } else {
                                painterResource(id = R.drawable.placeholder)
                            },
                            contentDescription = "Profile Photo",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        FloatingActionButton(
                            onClick = onCreateDailyUpdate,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(24.dp),
                            containerColor = Color(0xFF0D6EFD)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Create Daily Update", tint = Color.White)
                        }
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
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
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
                                    text = "${user.followers.size}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Card(
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
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
                                    text = "${user.following.size}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Edit Profile and Delete buttons in their own row
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onEdit(user) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                modifier = Modifier.weight(0.5f),
                shape = RoundedCornerShape(8.dp), // Less border radius

            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = Color.Black)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Edit Profile", color = Color.Black)
            }
            Button(
                onClick = { onDelete() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                modifier = Modifier.weight(0.5f),
                shape = RoundedCornerShape(8.dp), // Less border radius
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Black)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Delete", color = Color.Black)
            }
        }
    }
}

@Composable
fun PostsSection(
    posts: List<Post>,
    onPostClick: (Post) -> Unit,
    navController: NavController,
    postRepository: PostRepository
) {
    var showDeleteDialog by remember { mutableStateOf<Post?>(null) } // State for showing the delete dialog
    val mediaUrls = posts.filter { it.mediaType == "video" }.map { it.mediaUrl } // Extract video URLs
    var selectedMediaUrl by remember { mutableStateOf<String?>(null) } // Track selected video for full screen

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        posts.chunked(2).forEach { rowPosts -> // Group posts in pairs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowPosts.forEach { post ->
                    Box(modifier = Modifier.weight(1f)) {
                        PostCard(
                            post = post,
                            onClick = { onPostClick(post) },
                            onUpdateClick = {
                                navController.navigate("post_update_screen/${post.userId}/${post.id}")
                            },
                            onDeleteClick = { showDeleteDialog = post },
                            navController = navController,
                            mediaUrls = mediaUrls, // Pass media URLs for navigation
                            onFullScreenClick = { selectedMediaUrl = it } // Handle full-screen video
                        )
                    }
                }
            }
        }
    }

    // Show delete confirmation dialog
    showDeleteDialog?.let { post ->
        DeletePostConfirmationDialog(
            post = post,
            postRepository = postRepository,
            onDismiss = { showDeleteDialog = null }
        )
    }

    // Show full-screen video player when a video is clicked
    selectedMediaUrl?.let { url ->
        FullScreenVideoPager(
            mediaUrls = mediaUrls,
            initialPage = mediaUrls.indexOf(url).coerceAtLeast(0), // Prevents crash if not found
            onDismiss = { selectedMediaUrl = null }
        )
    }
}

@Composable
fun PostCard(
    post: Post,
    onClick: () -> Unit,
    onUpdateClick: (Post) -> Unit,
    onDeleteClick: (Post) -> Unit,
    navController: NavController,
    mediaUrls: List<String>,
    onFullScreenClick: (String) -> Unit // Pass selected media URL
) {
    val aspectRatioModifier = when {
        post.aspectRatio == 1f -> Modifier.height(200.dp)
        post.aspectRatio > 1f -> Modifier.height((200 * post.aspectRatio).dp)
        else -> Modifier.height((200 / post.aspectRatio).dp)
    }

    var showMenu by remember { mutableStateOf(false) }
    var showFullScreenDialog by remember { mutableStateOf(false) }
    var selectedMediaUrl by remember { mutableStateOf("") }

    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .then(aspectRatioModifier)
            .clickable {
                if (post.mediaType == "video") {
                    selectedMediaUrl = post.mediaUrl
                    showFullScreenDialog = true // Show full-screen dialog
                } else {
                    onClick()
                }
            },
        colors = CardDefaults.cardColors(containerColor = Color.White)
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
                    style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

            // Ellipsis Button with Dropdown Menu
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            ) {
                IconButton(
                    onClick = { showMenu = !showMenu },
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color.White.copy(alpha = 0.4f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More Options",
                        tint = Color(0xFF0D6EFD),
                        modifier = Modifier.size(16.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        onClick = {
                            showMenu = false
                            onUpdateClick(post)
                        },
                        text = { Text("Update") }
                    )
                    DropdownMenuItem(
                        onClick = {
                            showMenu = false
                            onDeleteClick(post) // Triggers delete confirmation dialog
                        },
                        text = { Text("Delete") }
                    )
                }
            }
        }

        // Show full-screen dialog for video posts
        if (showFullScreenDialog) {
            FullScreenVideoDialog(
                mediaUrls = mediaUrls,
                initialPage = mediaUrls.indexOf(selectedMediaUrl),
                onDismiss = { showFullScreenDialog = false }
            )
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun FullScreenVideoDialog(
    mediaUrls: List<String>,
    initialPage: Int,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = { onDismiss() }, // Close when tapped outside
        properties = DialogProperties(
            usePlatformDefaultWidth = false // Ensures dialog spans the entire screen
        )
    ) {
        FullScreenVideoPager(
            mediaUrls = mediaUrls,
            initialPage = initialPage,
            onDismiss = onDismiss
        )
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun FullScreenVideoPager(
    mediaUrls: List<String>,
    initialPage: Int,
    onDismiss: () -> Unit
) {
    val pagerState = rememberPagerState(initialPage = initialPage)
    var currentIndex by remember { mutableStateOf(initialPage) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        VerticalPager(
            count = mediaUrls.size,
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectVerticalDragGestures { change, dragAmount ->
                        if (dragAmount > 0) {
                            // Swipe up
                            if (currentIndex > 0) {
                                currentIndex--
                            }
                        } else {
                            // Swipe down
                            if (currentIndex < mediaUrls.size - 1) {
                                currentIndex++
                            }
                        }
                        change.consume()
                    }
                },
        ) { page ->
            FullScreenVideoPlayer(
                mediaUrl = mediaUrls[page],
                onDismiss = onDismiss
            )
        }

        // Close button
        IconButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

//@OptIn(ExperimentalPagerApi::class)
//@Composable
//fun FullScreenVideoPager(
//    mediaUrls: List<String>,
//    initialPage: Int,
//    onDismiss: () -> Unit
//) {
//    val pagerState = rememberPagerState(initialPage = initialPage)
//    var currentIndex by remember { mutableStateOf(initialPage) }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.Black)
//    ) {
//        VerticalPager(
//            count = mediaUrls.size,
//            state = pagerState,
//            modifier = Modifier
//                .fillMaxSize()
//                .pointerInput(Unit) {
//                    detectVerticalDragGestures { change, dragAmount ->
//                        if (dragAmount > 0) {
//                            // Swipe up
//                            if (currentIndex > 0) {
//                                currentIndex--
//                            }
//                        } else {
//                            // Swipe down
//                            if (currentIndex < mediaUrls.size - 1) {
//                                currentIndex++
//                            }
//                        }
//                        change.consume()
//                    }
//                },
//        ) { page ->
//            FullScreenVideoPlayer(
//                mediaUrl = mediaUrls[page],
//                onDismiss = onDismiss
//            )
//        }
//
//        // Close button
//        IconButton(
//            onClick = onDismiss,
//            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
//        ) {
//            Icon(
//                imageVector = Icons.Default.Close,
//                contentDescription = "Close",
//                tint = Color.White,
//                modifier = Modifier.size(24.dp)
//            )
//        }
//    }
//}

@Composable
fun FullScreenVideoPlayer(
    mediaUrl: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val player = remember {
        SimpleExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(mediaUrl))
            prepare()
            playWhenReady = true
            volume = 0f // Ensure the video is muted when launched
        }
    }

    var isMuted by remember { mutableStateOf(true) } // Initially muted

    DisposableEffect(key1 = Unit) {
        onDispose {
            player.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { PlayerView(context).apply {
                this.player = player
                this.useController = false // Disable default controls
            } },
            modifier = Modifier.fillMaxSize()
        )

        // Mute/Unmute Button
        IconButton(
            onClick = {
                isMuted = !isMuted
                player.volume = if (isMuted) 0f else 1f
            },
            modifier = Modifier.align(Alignment.BottomStart).padding(8.dp)
        ) {
            Icon(
                imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                contentDescription = if (isMuted) "Unmute" else "Mute",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        // Display video duration in minutes and seconds with reduced text size
        val duration = player.duration.takeIf { it >= 0 } ?: 0
        val minutes = (duration / 1000) / 60
        val seconds = (duration / 1000) % 60

        Text(
            text = String.format("%02d:%02d", minutes, seconds),
            color = Color.White,
            fontSize = 12.sp, // Reduced text size
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
        )

        // Close button
        IconButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

//video player for the profile screen
@Composable
fun VideoCard(
    mediaUrl: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val player = remember {
        SimpleExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(mediaUrl))
            prepare()
            playWhenReady = true
            volume = 0f // Ensure the video is muted when launched
        }
    }

    var isMuted by remember { mutableStateOf(true) } // Initially muted

    DisposableEffect(key1 = Unit) {
        onDispose {
            player.release()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clip(RoundedCornerShape(12.dp))
    ) {
        AndroidView(
            factory = { PlayerView(context).apply {
                this.player = player
                this.useController = false // Disable default controls
            } },
            modifier = Modifier.fillMaxSize()
        )

        // Mute/Unmute Button
        IconButton(
            onClick = {
                isMuted = !isMuted
                player.volume = if (isMuted) 0f else 1f
            },
            modifier = Modifier.align(Alignment.BottomStart).padding(1.dp)
        ) {
            Icon(
                imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                contentDescription = if (isMuted) "Unmute" else "Mute",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        // Display video duration in minutes and seconds with reduced text size
        val duration = player.duration.takeIf { it >= 0 } ?: 0
        val minutes = (duration / 1000) / 60
        val seconds = (duration / 1000) % 60

        Text(
            text = String.format("%02d:%02d", minutes, seconds),
            color = Color.White,
            fontSize = 12.sp, // Reduced text size
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
        )
    }
}

@Composable
fun DeletePostConfirmationDialog(
    post: Post,
    postRepository: PostRepository,
    onDismiss: () -> Unit // Add the onDismiss callback parameter
) {
    var showDeleteDialog by remember { mutableStateOf(true) } // Manage dialog visibility
    var isDeleteConfirmed by remember { mutableStateOf(false) } // Manage delete confirmation

    // Perform the delete operation when isDeleteConfirmed becomes true
    LaunchedEffect(isDeleteConfirmed) {
        if (isDeleteConfirmed) {
            try {
                postRepository.deletePost(post.userId, post.id)
                Log.d("Delete Success", "Post deleted successfully.")
            } catch (e: Exception) {
                Log.e("Delete Error", "Failed to delete post: ${e.message}")
            }
            showDeleteDialog = false // Close the dialog after deletion
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { onDismiss() }, // Call onDismiss when dismissed
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete this post? This action cannot be undone.") },
            shape = RoundedCornerShape(8.dp),
            confirmButton = {
                TextButton(onClick = {
                    // Set the state to trigger deletion
                    isDeleteConfirmed = true
                }) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { onDismiss() }) { // Close dialog when Cancel is clicked
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun EventsSection(
    events: List<Event>,
    capturedImages: SnapshotStateList<String>,
    onEventClick: (Event) -> Unit,
    onUpdateClick: (Event) -> Unit,
    onDeleteClick: (Event) -> Unit,
    eventViewModel: EventViewModel,
    currentUserId: String,
    currentUsername: String,
    navController: NavController,
    userViewModel: UserViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        events.forEach { event ->
            EventCard(
                event = event,
                capturedImages = capturedImages,
                onEventClick = onEventClick,
                onUpdateClick = { onUpdateClick(event) },  // Pass the event to update
                onDeleteClick = { onDeleteClick(event) },
                eventViewModel = eventViewModel,
                currentUserId = currentUserId,
                currentUsername = currentUsername,
                navController = navController,
                userViewModel = userViewModel
            )
        }
    }
}

@Composable
fun EventCard(
    event: Event,
    capturedImages: SnapshotStateList<String>,
    onEventClick: (Event) -> Unit,
    onUpdateClick: (Event) -> Unit,
    onDeleteClick: (Event) -> Unit,
    eventViewModel: EventViewModel,
    currentUserId: String,
    currentUsername: String,
    navController: NavController,
    userViewModel: UserViewModel
) {
    val primaryBlueColor = Color(0xFF0D6EFD)
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) } // State for delete dialog
    val context = LocalContext.current
    val isDeleted = remember { mutableStateOf(false) } // State to track if the card should disappear

    if (!isDeleted.value) { // Show the card only if it hasn't been deleted
        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clickable { onEventClick(event) },
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
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
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    IconButton(
                        onClick = {
                            showMenu = !showMenu
                        },
                        modifier = Modifier.background(Color.White.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            tint = primaryBlueColor
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        DropdownMenuItem(
                            onClick = {
                                showMenu = false
                                onUpdateClick(event)
                            },
                            text = { Text("Update") }
                        )
                        DropdownMenuItem(
                            onClick = {
                                showMenu = false
                                showDeleteDialog = true // Show confirmation dialog
                            },
                            text = { Text("Delete") }
                        )
                    }
                }
            }
        }
    }

    // Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Event") },
            text = { Text("Are you sure you want to delete this event? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteClick(event) // Perform deletion

                        // Update state to make card disappear
                        isDeleted.value = true

                        // Show toast
                        Toast.makeText(
                            context,
                            "Event deleted successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                ) {
                    Text("Confirm", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(8.dp) // Reduce border radius of the dialog
        )
    }
}

@Composable
fun SignOutDialog(onDismiss: () -> Unit, onSignOut: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sign Out") },
        text = { Text("Are you sure you want to sign out?") },
        confirmButton = {
            TextButton(onClick = onSignOut) {
                Text("Yes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("No")
            }
        }
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenPostDialog(
    posts: List<Post>,
    initialPostIndex: Int,
    onDismiss: () -> Unit
) {
    var currentIndex by remember { mutableStateOf(initialPostIndex) }
    val scaffoldState = rememberBottomSheetScaffoldState()
    val lazyListState = rememberLazyListState(initialPostIndex)

    LaunchedEffect(Unit) {
        scaffoldState.bottomSheetState.expand()
    }

    println("FullScreenPostDialog - initialPostIndex: $initialPostIndex")

    DisposableEffect(Unit) {
        onDispose {
            println("Dialog dismissed") // Logging
            onDismiss() // Proper cleanup
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(posts) { post ->
                    Card(
                        elevation = CardDefaults.cardElevation(8.dp), // Correcting elevation type
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Card(
                                elevation = CardDefaults.cardElevation(4.dp), // Correcting elevation type
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                Image(
                                    painter = rememberImagePainter(post.mediaUrl),
                                    contentDescription = "Full-Screen Post Media",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                )
                            }

                            if (post.caption.isNotEmpty()) {
                                Text(
                                    text = post.caption,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF333333),
                                    modifier = Modifier
                                        .background(Color(0xFFF5F5F5))
                                        .padding(16.dp)
                                        .fillMaxWidth()
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val iconColor = Color(0xFF0D6EFD) // Unified icon color
                                IconButton(onClick = { /* Handle like action */ }) {
                                    Icon(
                                        imageVector = Icons.Default.FavoriteBorder,
                                        contentDescription = "Like",
                                        tint = iconColor
                                    )
                                }
                                IconButton(onClick = { /* Handle comment action */ }) {
                                    Icon(
                                        imageVector = Icons.Default.Comment,
                                        contentDescription = "Comment",
                                        tint = iconColor
                                    )
                                }
                                IconButton(onClick = { /* Handle share action */ }) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share",
                                        tint = iconColor
                                    )
                                }
                                IconButton(onClick = { /* Handle save action */ }) {
                                    Icon(
                                        imageVector = Icons.Default.BookmarkBorder,
                                        contentDescription = "Save",
                                        tint = iconColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        sheetPeekHeight = 0.dp,
        content = {
            println("FullScreenPostDialog content rendered")
        }
    )
}