package com.lensnap.app.ui.theme.screens.dashboard

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.lensnap.app.R
import com.lensnap.app.data.UserViewModel
import com.lensnap.app.models.Comment
import com.lensnap.app.ui.theme.screens.userProfileScreen.FollowButton
import com.lensnap.app.models.Post
import com.lensnap.app.data.PostRepository
import kotlinx.coroutines.launch

// Custom colors
private val AliceBlue = Color(0xFFF0F8FF)
private val BootstrapPrimary = Color(0xFF0d6efd)

@Composable
fun CustomTopAppBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(bottomEnd = 16.dp, bottomStart = 16.dp, topEnd = 16.dp, topStart = 16.dp))
            .background(Color.Transparent),
//            .border(1.dp, BootstrapPrimary, RoundedCornerShape(bottomEnd = 16.dp, bottomStart = 16.dp, topEnd = 16.dp, topStart = 16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                buildAnnotatedString {
                    withStyle(style = SpanStyle(color = BootstrapPrimary)) {
                        append("Len")
                    }
                    withStyle(style = SpanStyle(color = BootstrapPrimary)) {
                        append("Snap")
                    }
                },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )
            Row {
                IconButton(onClick = { /* Navigate to messages */ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.inbox),
                        contentDescription = "Messages",
                        tint = BootstrapPrimary, modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(onClick = { /* Navigate to notifications */ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.notifications),
                        contentDescription = "Notifications",
                        tint = BootstrapPrimary, modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CustomBottomAppBar(
    userProfileImageUrl: String?, // Profile image URL, can be null
    onStartEvent: () -> Unit,
    onJoinEvent: () -> Unit,
    onSearch: () -> Unit,
    onProfile: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(topEnd = 16.dp, topStart = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp))
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onStartEvent) {
                Icon(
                    painter = painterResource(id = R.drawable.create),
                    contentDescription = "Start Event",
                    tint = BootstrapPrimary,
                    modifier = Modifier.size(30.dp)
                )
            }
            IconButton(onClick = onSearch) {
                Icon(
                    painter = painterResource(id = R.drawable.search),
                    contentDescription = "Search",
                    tint = BootstrapPrimary,
                    modifier = Modifier.size(30.dp)
                )
            }
            IconButton(onClick = onJoinEvent) {
                Icon(
                    painter = painterResource(id = R.drawable.join),
                    contentDescription = "Join Event",
                    tint = BootstrapPrimary,
                    modifier = Modifier.size(30.dp)
                )
            }
            IconButton(onClick = onProfile) {
                if (userProfileImageUrl != null) {
                    Image(
                        painter = rememberImagePainter(userProfileImageUrl),
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color.Gray),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.placeholder),
                        contentDescription = "Placeholder Image",
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color.Gray),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    eventViewModel: EventViewModel,
    userProfileImageUrl: String?,
    currentUserId: String,
    currentUsername: String,
    onNavigateToProfile: () -> Unit,
    onStartEvent: () -> Unit,
    onJoinEvent: () -> Unit,
    onSearch: () -> Unit,
    onEventClick: (Event) -> Unit,
    navController: NavController,
    userViewModel: UserViewModel,
    postRepository: PostRepository
) {
    val events by eventViewModel.events.observeAsState(initial = emptyList())
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    val isRefreshing by eventViewModel.isRefreshing.observeAsState(false)
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing)
    var currentEventIdForComment by remember { mutableStateOf<String?>(null) }

    // Fetch posts within LaunchedEffect using the updated `fetchDashboardPosts`
    LaunchedEffect(currentUserId) {
        posts = postRepository.fetchDashboardPosts(currentUserId)
    }

    val combinedList = (events + posts).sortedByDescending {
        when (it) {
            is Event -> it.timestamp
            is Post -> it.timestamp
            else -> 0L
        }
    }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CustomTopAppBar()
        },
        bottomBar = {
            CustomBottomAppBar(
                userProfileImageUrl = userProfileImageUrl,
                onStartEvent = onStartEvent,
                onJoinEvent = onJoinEvent,
                onSearch = onSearch,
                onProfile = onNavigateToProfile
            )
        },
        content = { paddingValues ->
            SwipeRefresh(
                state = swipeRefreshState,
                onRefresh = {
                    coroutineScope.launch {
                        eventViewModel.fetchEvents()
                        posts = postRepository.fetchDashboardPosts(currentUserId)
                    }
                },
                modifier = Modifier
                    .fillMaxSize() // Ensure it takes up the whole screen
                    .padding(paddingValues)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AliceBlue),
                    contentAlignment = Alignment.Center
                ) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier.fillMaxSize() // Ensure it takes up the whole screen
                    ) {
                        items(combinedList) { item ->
                            when (item) {
                                is Event -> {
                                    val capturedImages = remember { mutableStateListOf<String>() }
                                    var likeCount by remember { mutableStateOf(0) }
                                    var commentCount by remember { mutableStateOf(0) }
                                    var isLiked by remember { mutableStateOf(false) }

                                    LaunchedEffect(item.id) {
                                        eventViewModel.fetchEventImages(item.id, onSuccess = { images ->
                                            capturedImages.clear()
                                            capturedImages.addAll(images)
                                        }, onError = { error ->
                                            Log.e("DashboardScreen", "Error fetching images: $error")
                                        })

                                        eventViewModel.getLikesCount(item.id, onSuccess = { count ->
                                            likeCount = count
                                            eventViewModel.hasUserLiked(item.id, currentUserId, onSuccess = { liked ->
                                                isLiked = liked
                                            }, onError = { error ->
                                                Log.e("DashboardScreen", "Error checking if user liked: $error")
                                            })
                                        }, onError = { error ->
                                            Log.e("DashboardScreen", "Error fetching likes count: $error")
                                        })

                                        eventViewModel.getComments(item.id, onSuccess = { comments ->
                                            commentCount = comments.size
                                        }, onError = { error ->
                                            Log.e("DashboardScreen", "Error fetching comments: $error")
                                        })
                                    }

                                    EventCardWithDetails(
                                        event = item,
                                        capturedImages = capturedImages,
                                        likeCount = likeCount,
                                        commentCount = commentCount,
                                        isLiked = isLiked,
                                        onEventClick = onEventClick,
                                        onLikeClick = {
                                            eventViewModel.addLike(item.id, currentUserId, onSuccess = {
                                                likeCount += 1
                                                isLiked = true
                                            }, onError = { error ->
                                                Log.e("DashboardScreen", "Failed to like: $error")
                                            })
                                        },
                                        onUnlikeClick = {
                                            eventViewModel.removeLike(item.id, currentUserId, onSuccess = {
                                                likeCount -= 1
                                                isLiked = false
                                            }, onError = { error ->
                                                Log.e("DashboardScreen", "Failed to unlike: $error")
                                            })
                                        },
                                        onCommentClick = {
                                            currentEventIdForComment = item.id
                                        },
                                        eventViewModel = eventViewModel,
                                        currentUserId = currentUserId,
                                        currentUsername = currentUsername,
                                        navController = navController,
                                        userViewModel = userViewModel
                                    )
                                }
                                is Post -> {
                                    PostCard(post = item)
                                }
                            }
                        }
                    }
                }
            }
        }
    )

    if (currentEventIdForComment != null) {
        CommentDialog(
            eventId = currentEventIdForComment!!,
            onDismiss = { currentEventIdForComment = null },
            eventViewModel = eventViewModel,
            currentUserId = currentUserId,
            currentUsername = currentUsername,
            navController = navController,
            userViewModel = userViewModel
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun EventCardWithDetails(
    event: Event,
    capturedImages: SnapshotStateList<String>,
    onEventClick: (Event) -> Unit,
    likeCount: Int,
    commentCount: Int,
    isLiked: Boolean,
    onLikeClick: () -> Unit,
    onUnlikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    eventViewModel: EventViewModel,
    currentUserId: String,
    currentUsername: String, // Pass current user's username
    navController: NavController, // Add NavController as a parameter
    userViewModel: UserViewModel // Add UserViewModel as a parameter
) {
    val lazyListState = rememberLazyListState()
    var showDialog by remember { mutableStateOf(false) }
    val primaryBlueColor = Color(0xFF0D6EFD) // Bootstrap primary blue color

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onEventClick(event) }
            .background(MaterialTheme.colorScheme.surface)
            .clip(MaterialTheme.shapes.medium)
    ) {
        Column {
            // Unified Image Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp) // Adjust the height for larger image display
            ) {
                val flingBehavior = rememberSnapFlingBehavior(lazyListState)

                LazyRow(
                    state = lazyListState,
                    flingBehavior = flingBehavior,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Display the main event image first
                    item {
                        ImageCard(imageUrl = event.imageUrl)
                    }
                    // Display the captured images from the gallery and camera
                    items(capturedImages) { imageUrl ->
                        ImageCard(imageUrl = imageUrl)
                    }
                }

                // Hovering Icons
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (isLiked) {
                            onUnlikeClick()
                        } else {
                            onLikeClick()
                        }
                    }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                painter = painterResource(id = if (isLiked) R.drawable.approve else R.drawable.cancel),
                                contentDescription = "Like",
                                tint = primaryBlueColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Text("$likeCount", color = Color.White, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    IconButton(onClick = { showDialog = true }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                painter = painterResource(id = R.drawable.insertcomment),
                                contentDescription = "Comment",
                                tint = primaryBlueColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Text("$commentCount", color = Color.White, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    IconButton(onClick = { /* Share action */ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.send),
                            contentDescription = "Share",
                            tint = primaryBlueColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Event Details Section
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text(text = event.name, style = MaterialTheme.typography.titleLarge, color = Color.Black)
                Text(text = event.description, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
                Text(text = "Location: ${event.location}", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                Text(text = "Date: ${event.date}", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
            }
        }
    }

    if (showDialog) {
        CommentDialog(
            eventId = event.id,
            onDismiss = { showDialog = false },
            eventViewModel = eventViewModel,
            currentUserId = currentUserId,
            currentUsername = currentUsername,
            navController = navController,
            userViewModel = userViewModel
        )
    }
}

@Composable
fun ImageCard(imageUrl: String) {
    val painter = rememberImagePainter(data = imageUrl, builder = {
        placeholder(R.drawable.placeholder)
        error(R.drawable.error)
    })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(MaterialTheme.shapes.medium)
//            .padding(horizontal = 16.dp) // Add padding to give space around images
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Crop, // Ensure images fill the space uniformly
            modifier = Modifier.fillMaxSize()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentDialog(
    eventId: String,
    onDismiss: () -> Unit,
    eventViewModel: EventViewModel,
    currentUserId: String,
    currentUsername: String,
    navController: NavController,
    userViewModel: UserViewModel
) {
    var commentText by remember { mutableStateOf("") }
    val comments = remember { mutableStateListOf<Comment>() }

    LaunchedEffect(eventId) {
        eventViewModel.getComments(eventId, onSuccess = { loadedComments ->
            comments.clear()
            comments.addAll(loadedComments)
        }, onError = { error ->
            Log.e("CommentDialog", "Failed to load comments: $error")
        })
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = (LocalContext.current.resources.displayMetrics.heightPixels * 0.75f).dp)
        ) {
            Column {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Comments",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }
                Divider()

                // Comments List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    items(comments) { comment ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Placeholder for Profile Icon
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color.Gray, shape = CircleShape)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Column {
                                // Clickable Username and Follow Button
                                val displayedUsername = if (comment.userId == currentUserId) "You" else comment.username
                                Text(
                                    text = displayedUsername,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable {
                                        if (comment.userId == currentUserId) {
                                            navController.navigate("profile") // Navigate to profile screen for current user
                                        } else {
                                            navController.navigate("profile/${comment.userId}") // Navigate to user profile screen for others
                                        }
                                    }
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                if (comment.userId != currentUserId) {
                                    FollowButton(userViewModel = userViewModel, targetUserId = comment.userId)
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = comment.comment,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                // Comment Input
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        label = { Text("Add a comment") },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            eventViewModel.addComment(eventId, currentUserId, currentUsername, commentText, onSuccess = {
                                commentText = ""
                                eventViewModel.getComments(eventId, onSuccess = { loadedComments ->
                                    comments.clear()
                                    comments.addAll(loadedComments)
                                }, onError = { error ->
                                    Log.e("CommentDialog", "Failed to load comments: $error")
                                })
                            }, onError = { error ->
                                Log.e("CommentDialog", "Failed to add comment: $error")
                            })
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send Comment"
                        )
                    }
                }
            }
        }
    }
}
