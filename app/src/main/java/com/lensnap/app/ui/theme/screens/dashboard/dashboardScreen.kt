package com.lensnap.app.ui.theme.screens.dashboard

import ImageData
import PrimaryBlue
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.lensnap.app.models.UserRegistration
import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.draw.shadow
import com.lensnap.app.data.DailyUpdateRepository
import com.lensnap.app.models.DailyUpdate
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.size.Size
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

// Custom colors
private val AliceBlue = Color(0xFFF0F8FF)
private val BootstrapPrimary = Color(0xFF0d6efd)

@Composable
fun CustomBottomAppBar(
    userProfileImageUrl: String?, // Profile image URL, can be null
    onStartEvent: () -> Unit,
    onJoinEvent: () -> Unit,
    onSearch: () -> Unit,
    onProfile: () -> Unit,
    onHome: () -> Unit // Home button callback
) {
    var selectedItem by remember { mutableStateOf("home") } // Track the selected item

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
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
            IconButton(
                onClick = {
                    onHome()
                    selectedItem = "home"
                },
                modifier = Modifier
                    .background(if (selectedItem == "home") AliceBlue else Color.Transparent, shape = CircleShape)
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Home,
                    contentDescription = "Home",
                    tint = if (selectedItem == "home") PrimaryBlue else Color.DarkGray,
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(
                onClick = {
                    onSearch()
                    selectedItem = "search"
                },
                modifier = Modifier
                    .background(if (selectedItem == "search") AliceBlue else Color.Transparent, shape = CircleShape)
                    .padding(4.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.search),
                    contentDescription = "Search",
                    tint = if (selectedItem == "search") PrimaryBlue else Color.DarkGray,
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(
                onClick = {
                    onStartEvent()
                    selectedItem = "start"
                },
                modifier = Modifier
                    .background(if (selectedItem == "start") AliceBlue else Color.Transparent, shape = CircleShape)
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp)) // Box shape with rounded edges
                        .background(PrimaryBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add, // Changed to plus icon
                        contentDescription = "Start Event",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            IconButton(
                onClick = {
                    onJoinEvent()
                    selectedItem = "join"
                },
                modifier = Modifier
                    .background(if (selectedItem == "join") AliceBlue else Color.Transparent, shape = CircleShape)
                    .padding(4.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.join),
                    contentDescription = "Join Event",
                    tint = if (selectedItem == "join") PrimaryBlue else Color.DarkGray,
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(
                onClick = {
                    onProfile()
                    selectedItem = "profile"
                },
                modifier = Modifier
                    .background(if (selectedItem == "profile") AliceBlue else Color.Transparent, shape = CircleShape)
                    .padding(4.dp)
            ) {
                if (userProfileImageUrl != null) {
                    Image(
                        painter = rememberImagePainter(userProfileImageUrl),
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(8.dp))
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
    userId: String,
    currentUsername: String,
    onNavigateToProfile: () -> Unit,
    onStartEvent: () -> Unit,
    onJoinEvent: () -> Unit,
    onSearch: () -> Unit,
    onHome: () -> Unit,
    onEventClick: (Event) -> Unit,
    navController: NavController,
    userViewModel: UserViewModel,
    postRepository: PostRepository,
    dailyUpdateRepository: DailyUpdateRepository
) {
    Log.d("DashboardScreen", "DashboardScreen loaded with userId: $userId")
    val events by eventViewModel.events.collectAsState(initial = emptyList()) // Using StateFlow for events
    val upcomingEvents by eventViewModel.upcomingEvents.collectAsState(emptyList()) // Collect the upcoming events state
    val peopleYouMayKnow by userViewModel.peopleYouMayKnow.observeAsState(emptyList())
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var dailyUpdates by remember { mutableStateOf<List<DailyUpdate>>(emptyList()) }
    val isRefreshing by eventViewModel.isRefreshing.observeAsState(false)
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing)
    var currentEventIdForComment by remember { mutableStateOf<String?>(null) }
    var fullScreenImageUrl by remember { mutableStateOf<String?>(null) } // State for the full-screen image URL
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val auth: FirebaseAuth by lazy {FirebaseAuth.getInstance() }

    if (userId.isEmpty()) {
        Log.e("DashboardScreen", "userId is empty")
        return
    }

    // Fetch posts, events, people you may know, and daily updates
    LaunchedEffect(userId) {
        try {
            userViewModel.fetchPeopleYouMayKnow(userId)
            userViewModel.fetchUsersToFollowBack(userId)
            eventViewModel.fetchUpcomingEventsByDate()
            eventViewModel.fetchEvents()
            posts = postRepository.fetchDashboardPosts(userId)
            userViewModel.fetchFollowing(userId) { followingUsers ->
                coroutineScope.launch {
                    dailyUpdates = dailyUpdateRepository.getFollowingDailyUpdates(followingUsers.map { it.id })
                }
            }
        } catch (e: Exception) {
            Log.e("DashboardScreen", "Error fetching data for user ID: $userId", e)
        }
    }

    val combinedList = (events + posts).sortedByDescending {
        when (it) {
            is Event -> it.timestamp
            is Post -> it.timestamp
            else -> 0L
        }
    }

    Scaffold(
        bottomBar = {
            CustomBottomAppBar(
                userProfileImageUrl = userProfileImageUrl,
                onStartEvent = onStartEvent,
                onJoinEvent = onJoinEvent,
                onSearch = onSearch,
                onProfile = onNavigateToProfile,
                onHome = onHome
            )
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                SwipeRefresh(
                    state = swipeRefreshState,
                    onRefresh = {
                        coroutineScope.launch {
                            try {
                                eventViewModel.fetchEvents()
                                posts = postRepository.fetchDashboardPosts(userId)
                                userViewModel.fetchFollowing(userId) { followingUsers ->
                                    coroutineScope.launch {
                                        dailyUpdates = dailyUpdateRepository.getFollowingDailyUpdates(followingUsers.map { it.id })
                                    }
                                }
                                userViewModel.fetchPeopleYouMayKnow(userId)
                            } catch (e: Exception) {
                                Log.e("DashboardScreen", "Error refreshing data", e)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(AliceBlue)
                    ) {

                        LazyColumn(
                            state = lazyListState,
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // 1. Daily Updates Section (always at the top)
                            item {
                                Text(
                                    buildAnnotatedString {
                                        withStyle(style = SpanStyle(color = BootstrapPrimary)) { append("Len") }
                                        withStyle(style = SpanStyle(color = BootstrapPrimary)) { append("Snap") }
                                    },
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Thin,
                                    modifier = Modifier.padding(16.dp)
                                )
                                DailyUpdatesSection(updates = dailyUpdates)
                            }

                            // 2. Build a combined list with interleaved sections
                            val combinedListWithSections = buildList {
                                combinedList.forEachIndexed { index, item ->
                                    add(item)

                                    // Add Upcoming Events Section after every 6 items
                                    if ((index + 1) % 6 == 0) {
                                        add("UpcomingEventsSection")
                                    }

                                    // Add People You May Know Section after every 12 items
                                    if ((index + 1) % 12 == 0) {
                                        add("PeopleYouMayKnowSection")
                                    }
                                }
                            }

                            // 3. Use items to render the combined list with sections
                            items(combinedListWithSections) { listItem ->
                                when (listItem) {
                                    is Event -> {
                                        val capturedImages = remember { mutableStateListOf<ImageData>() }
                                        var likeCount by remember { mutableStateOf(0) }
                                        var commentCount by remember { mutableStateOf(0) }
                                        var isLiked by remember { mutableStateOf(false) }

                                        LaunchedEffect(listItem.id) {
                                            try {
                                                eventViewModel.fetchEventImages(
                                                    listItem.id,
                                                    onSuccess = { images -> capturedImages.clear(); capturedImages.addAll(images) },
                                                    onError = { error -> Log.e("DashboardScreen", "Error fetching images: $error") }
                                                )
                                                eventViewModel.getLikesCount(
                                                    listItem.id,
                                                    onSuccess = { count -> likeCount = count },
                                                    onError = { error -> Log.e("DashboardScreen", "Error fetching likes count: $error") }
                                                )
                                                eventViewModel.getComments(
                                                    listItem.id,
                                                    onSuccess = { comments -> commentCount = comments.size },
                                                    onError = { error -> Log.e("DashboardScreen", "Error fetching comments: $error") }
                                                )
                                            } catch (e: Exception) {
                                                Log.e("DashboardScreen", "Error in LaunchedEffect for event ID: ${listItem.id}", e)
                                            }
                                        }

                                        EventCardWithDetails(
                                            event = listItem,
                                            capturedImages = capturedImages,
                                            likeCount = likeCount,
                                            commentCount = commentCount,
                                            isLiked = isLiked,
                                            onEventClick = onEventClick,
                                            onLikeClick = {
                                                eventViewModel.addLike(
                                                    listItem.id,
                                                    userId,
                                                    onSuccess = { likeCount += 1; isLiked = true },
                                                    onError = { error -> Log.e("DashboardScreen", "Failed to like: $error") }
                                                )
                                            },
                                            onUnlikeClick = {
                                                eventViewModel.removeLike(
                                                    listItem.id,
                                                    userId,
                                                    onSuccess = { likeCount -= 1; isLiked = false },
                                                    onError = { error -> Log.e("DashboardScreen", "Failed to unlike: $error") }
                                                )
                                            },
                                            onCommentClick = { currentEventIdForComment = listItem.id },
                                            eventViewModel = eventViewModel,
                                            currentUserId = userId,
                                            currentUsername = currentUsername,
                                            navController = navController,
                                            userViewModel = userViewModel
                                        )
                                    }

                                    is Post -> {
                                        val currentUserId = userViewModel.getCurrentUserId()
                                        val isLikedState = remember { mutableStateOf(listItem.likes.contains(currentUserId)) }
                                        val totalLikes = remember { mutableStateOf(0) }

                                        // Fetch total likes and listen for real-time updates
                                        LaunchedEffect(listItem.id) {
                                            userViewModel.fetchTotalLikes(listItem.userId, listItem.id) { likes ->
                                                Log.d("FirestoreDebug", "Total likes updated in composable: $likes")
                                                totalLikes.value = likes
                                            }
                                        }
                                        PostCard(
                                            post = listItem,
                                            isLiked = isLikedState.value,
                                            onLikeClick = {
                                                userViewModel.likePost(listItem.userId, listItem.id)
                                            },
                                            onUnlikeClick = {
                                                userViewModel.unlikePost(listItem.userId, listItem.id)
                                            },
                                            onShareClick = { /* Handle share click */ },
                                            isLikedState = isLikedState,
                                            totalLikes = totalLikes.value,
                                            userViewModel = userViewModel,
                                            currentUserId = userId // Adjusted parameter name to currentUserId
                                        )
                                    }

                                    "UpcomingEventsSection" -> {
                                        UpcomingEventsSection(
                                            events = upcomingEvents,
                                            onEventClick = onEventClick,
                                            onUpdateClick = { /* Handle update click */ },
                                            onDeleteClick = { /* Handle delete click */ },
                                            eventViewModel = eventViewModel,
                                            currentUserId = userId,
                                            currentUsername = currentUsername,
                                            navController = navController
                                        )
                                    }

                                    "PeopleYouMayKnowSection" -> {
                                        PeopleYouMayKnowSection(
                                            viewModel = userViewModel,
                                            onFollowClick = { userId -> userViewModel.followUser(userId) },
                                            navController = navController,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp)

                                        )
                                    }
                                }
                            }
                        }

                        // Observe lazy list state for pagination
                        ObserveLazyListStateForPagination(
                            lazyListState = lazyListState,
                            onLoadMore = {
                                eventViewModel.fetchEvents() // Trigger fetching more events
                            }
                        )
                    }
                }

                // Floating Action Button (FAB)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd) // Align to bottom end
                        .padding(top = 10.dp, end = 10.dp) // Adjust padding
                ) {
                    FloatingActionButton(
                        onClick = {
                            navController.navigate("chats")
                        },
                        containerColor = Color(0xFF0d6efd), // Background color
                        contentColor = Color.White, // Text color
                        shape = RoundedCornerShape(16.dp), // Box shape with rounded edges
                        elevation = FloatingActionButtonDefaults.elevation(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = "Notifications",
                            tint = Color.White, // Icon color
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    )

    // Comment Dialog
    if (currentEventIdForComment != null) {
        CommentDialog(
            eventId = currentEventIdForComment!!,
            onDismiss = { currentEventIdForComment = null },
            eventViewModel = eventViewModel,
            currentUserId = userId,
            currentUsername = currentUsername,
            navController = navController,
            userViewModel = userViewModel
        )
    }
}
@Composable
fun ObserveLazyListStateForPagination(
    lazyListState: LazyListState,
    onLoadMore: () -> Unit
) {
    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .distinctUntilChanged()
            .collect { lastVisibleIndex ->
                // Trigger pagination if the last visible item is the last item in the list
                if (lastVisibleIndex == lazyListState.layoutInfo.totalItemsCount - 1) {
                    onLoadMore()
                }
            }
    }
}

@Composable
fun DailyUpdatesSection(
    updates: List<DailyUpdate>,
    modifier: Modifier = Modifier
) {
    var selectedUpdateIndex by remember { mutableStateOf(-1) }

    // Full-Screen View
    if (selectedUpdateIndex != -1) {
        FullScreenImageModal(
            updates = updates,
            selectedIndex = selectedUpdateIndex,
            onDismiss = { selectedUpdateIndex = -1 } // Close full-screen view
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "Daily Updates",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(updates) { index, update ->
                DailyUpdatesCard(
                    update = update,
                    modifier = Modifier.width(150.dp),
                    onClick = { selectedUpdateIndex = index } // Open full-screen view
                )
            }
        }
    }
}

@Composable
fun DailyUpdatesCard(
    update: DailyUpdate,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    if (update.uri.isNotEmpty()) {
        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            modifier = modifier
                .width(150.dp)
                .padding(8.dp)
                .height(200.dp)
                .clickable { onClick() }, // Trigger full-screen view on click
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Image(
                    painter = rememberImagePainter(
                        data = update.uri,
                        builder = {
                            diskCachePolicy(CachePolicy.ENABLED)
                            networkCachePolicy(CachePolicy.ENABLED)
                        }
                    ),
                    contentDescription = "Daily Update Media",
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
                                startY = 300f
                            )
                        )
                        .padding(8.dp)
                ) {
                    Text(
                        text = update.username,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.align(Alignment.BottomStart)
                    )
                }
            }
        }
    }
}

@Composable
fun FullScreenImageModal(
    updates: List<DailyUpdate>,
    selectedIndex: Int,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = { onDismiss() }, // Close when tapped outside
        properties = DialogProperties(
            usePlatformDefaultWidth = false // Ensures dialog spans the entire screen
        )
    ) {
        FullScreenImageView(
            updates = updates,
            selectedIndex = selectedIndex,
            onDismiss = onDismiss
        )
    }
}

@Composable
fun FullScreenImageView(
    updates: List<DailyUpdate>,
    selectedIndex: Int,
    onDismiss: () -> Unit
) {
    var currentIndex by remember { mutableStateOf(selectedIndex) }

    val update = updates[currentIndex]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        val screenWidth = size.width
                        if (offset.x < screenWidth / 2) {
                            // Left tap
                            if (currentIndex > 0) {
                                currentIndex--
                            }
                        } else {
                            // Right tap
                            if (currentIndex < updates.size - 1) {
                                currentIndex++
                            }
                        }
                    }
                )
            }
    ) {
        // Full-Screen Image with Padding and Rounded Edges
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp) // Add padding around the image
                .clip(RoundedCornerShape(16.dp)) // Rounded edges
                .background(Color.Black)
        ) {
            Image(
                painter = rememberImagePainter(
                    data = update.uri,
                    builder = {
                        placeholder(R.drawable.placeholder) // Replace with your placeholder drawable
                        error(R.drawable.error) // Replace with your error drawable
                        crossfade(true) // Smooth transition effect
                    }
                ),
                contentDescription = "Full-Screen Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Profile Image and Username (Top Left)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            // Profile Image with Rounded Shape
            Image(
                painter = rememberImagePainter(
                    data = update.profilePhotoUrl,
                    builder = {
                        placeholder(R.drawable.placeholder) // Replace with your placeholder drawable
                        error(R.drawable.error) // Replace with your error drawable
                        crossfade(true)
                    }
                ),
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(16.dp)) // Rounded edges
                    .background(Color.Gray)
            )
            Spacer(modifier = Modifier.width(8.dp))
            // Username
            Text(
                text = update.username,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.shadow(8.dp) // Subtle shadow for better contrast
            )
        }

        // Caption at the Bottom with Shadow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Text(
                text = update.caption,
                color = Color.White,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .shadow(8.dp) // Add shadow for text contrast
            )
        }

        // Close Button (Top Right)
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White
            )
        }
    }
}


// Converts image urls to data so they can be used by ImageCard in assigning the proper aspect ratio
fun convertToImageData(urls: List<String>): SnapshotStateList<ImageData> {
    val imageDataList = mutableStateListOf<ImageData>()
    urls.forEach { url ->
        imageDataList.add(ImageData(url = url, aspectRatio = 1.0f)) // Add appropriate aspect ratio
    }
    return imageDataList
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun EventCardWithDetails(
    event: Event,
    capturedImages: SnapshotStateList<ImageData>,
    onEventClick: (Event) -> Unit,
    likeCount: Int,
    commentCount: Int,
    isLiked: Boolean,
    onLikeClick: () -> Unit,
    onUnlikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    eventViewModel: EventViewModel,
    currentUserId: String,
    currentUsername: String,
    navController: NavController,
    userViewModel: UserViewModel
) {
    val lazyListState = rememberLazyListState()
    var showCommentDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onEventClick(event) },
        elevation = CardDefaults.cardElevation(0.dp), // No elevation for frameless look
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent) // Transparent card
    ) {
        Column(
            modifier = Modifier
                .background(Color.Transparent) // Transparent background
        ) {
            // Image Section with Elevation and Shadow
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(12.dp)) // Shadow effect
            ) {
                val flingBehavior = rememberSnapFlingBehavior(lazyListState)

                LazyRow(
                    state = lazyListState,
                    flingBehavior = flingBehavior,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxSize()
                        .background(Color.Transparent) // Transparent images section
                ) {
                    item {
                        ImageCard(imageData = ImageData(url = event.imageUrl, aspectRatio = 1.0f))
                    }
                    items(capturedImages) { imageData ->
                        ImageCard(imageData = imageData)
                    }
                }

                // Indicator Dots
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    val totalPages = 1 + capturedImages.size
                    for (i in 0 until totalPages) {
                        Box(
                            modifier = Modifier
                                .size(if (lazyListState.firstVisibleItemIndex == i) 8.dp else 6.dp)
                                .clip(CircleShape)
                                .background(if (lazyListState.firstVisibleItemIndex == i) Color.Gray else Color.LightGray)
                                .padding(2.dp)
                        )
                    }
                }
            }

            // Icons Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = {
                        if (isLiked) {
                            onUnlikeClick()
                        } else {
                            onLikeClick()
                        }
                    },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("$likeCount", color = Color.Gray, style = MaterialTheme.typography.bodySmall)

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(
                    onClick = { showCommentDialog = true },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Comment,
                        contentDescription = "Comment",
                        tint = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("$commentCount", color = Color.Gray, style = MaterialTheme.typography.bodySmall)

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(
                    onClick = {
                        Log.d("EventCardWithDetails", "Share button clicked. Event Image URL: ${event.imageUrl}")
                        showShareDialog = true
                    },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.Gray
                    )
                }
            }

            // Event Details Section with Smaller Text
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(Color.Transparent) // Transparent details section
            ) {
                Text(
                    text = event.name,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodySmall, // Smaller text for description
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = "Location", tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = event.location,
                        style = MaterialTheme.typography.bodySmall, // Smaller text for location
                        color = Color.DarkGray
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Event, contentDescription = "Date", tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = event.date,
                        style = MaterialTheme.typography.bodySmall, // Smaller text for date
                        color = Color.DarkGray
                    )
                }
            }
        }
    }

    if (showCommentDialog) {
        CommentDialog(
            eventId = event.id,
            onDismiss = { showCommentDialog = false },
            eventViewModel = eventViewModel,
            currentUserId = currentUserId,
            currentUsername = currentUsername,
            navController = navController,
            userViewModel = userViewModel
        )
    }

    if (showShareDialog) {
        ShareDialog(
            onDismiss = { showShareDialog = false },
            currentUserId = currentUserId,
            userViewModel = userViewModel,
            navController = navController,
            event = event
        )
    }
}

//@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
//@Composable
//fun EventCardWithDetails(
//    event: Event,
//    capturedImages: SnapshotStateList<ImageData>,
//    onEventClick: (Event) -> Unit,
//    likeCount: Int,
//    commentCount: Int,
//    isLiked: Boolean,
//    onLikeClick: () -> Unit,
//    onUnlikeClick: () -> Unit,
//    onCommentClick: () -> Unit,
//    eventViewModel: EventViewModel,
//    currentUserId: String,
//    currentUsername: String,
//    navController: NavController,
//    userViewModel: UserViewModel
//) {
//    val lazyListState = rememberLazyListState()
//    var showCommentDialog by remember { mutableStateOf(false) }
//    var showShareDialog by remember { mutableStateOf(false) }
//
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 16.dp)
//            .clickable { onEventClick(event) },
//        elevation = CardDefaults.cardElevation(0.dp), // No elevation for frameless look
//        shape = RoundedCornerShape(12.dp),
//        colors = CardDefaults.cardColors(containerColor = Color.Transparent) // Transparent card
//    ) {
//        Column(
//            modifier = Modifier
//                .background(Color.Transparent) // Transparent background
//        ) {
//            // Image Section with Elevation and Shadow
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(300.dp)
//                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(12.dp)) // Shadow effect
//            ) {
//                val flingBehavior = rememberSnapFlingBehavior(lazyListState)
//
//                LazyRow(
//                    state = lazyListState,
//                    flingBehavior = flingBehavior,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .fillMaxSize()
//                        .background(Color.Transparent) // Transparent images section
//                ) {
//                    item {
//                        ImageCard(imageData = ImageData(url = event.imageUrl, aspectRatio = 1.0f))
//                    }
//                    items(capturedImages) { imageData ->
//                        ImageCard(imageData = imageData)
//                    }
//                }
//
//                // Indicator Dots
//                Row(
//                    modifier = Modifier
//                        .align(Alignment.BottomCenter)
//                        .padding(8.dp),
//                    horizontalArrangement = Arrangement.Center
//                ) {
//                    val totalPages = 1 + capturedImages.size
//                    for (i in 0 until totalPages) {
//                        Box(
//                            modifier = Modifier
//                                .size(if (lazyListState.firstVisibleItemIndex == i) 8.dp else 6.dp)
//                                .clip(CircleShape)
//                                .background(if (lazyListState.firstVisibleItemIndex == i) Color.Gray else Color.LightGray)
//                                .padding(2.dp)
//                        )
//                    }
//                }
//            }
//
//            // Icons Row
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 16.dp, vertical = 8.dp),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.Start
//            ) {
//                IconButton(
//                    onClick = {
//                        if (isLiked) {
//                            onUnlikeClick()
//                        } else {
//                            onLikeClick()
//                        }
//                    },
//                    modifier = Modifier.size(20.dp)
//                ) {
//                    Icon(
//                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
//                        contentDescription = "Like",
//                        tint = Color.Gray
//                    )
//                }
//                Spacer(modifier = Modifier.width(8.dp))
//                Text("$likeCount", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
//
//                Spacer(modifier = Modifier.width(16.dp))
//
//                IconButton(
//                    onClick = { showCommentDialog = true },
//                    modifier = Modifier.size(20.dp)
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Comment,
//                        contentDescription = "Comment",
//                        tint = Color.Gray
//                    )
//                }
//                Spacer(modifier = Modifier.width(8.dp))
//                Text("$commentCount", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
//
//                Spacer(modifier = Modifier.width(16.dp))
//
//                IconButton(
//                    onClick = { showShareDialog = true },
//                    modifier = Modifier.size(20.dp)
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Share,
//                        contentDescription = "Share",
//                        tint = Color.Gray
//                    )
//                }
//            }
//
//            // Event Details Section with Smaller Text
//            Column(
//                modifier = Modifier
//                    .padding(horizontal = 16.dp, vertical = 8.dp)
//                    .background(Color.Transparent) // Transparent details section
//            ) {
//                Text(
//                    text = event.name,
//                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
//                    color = Color.Black
//                )
//                Spacer(modifier = Modifier.height(4.dp))
//                Text(
//                    text = event.description,
//                    style = MaterialTheme.typography.bodySmall, // Smaller text for description
//                    color = Color.DarkGray
//                )
//                Spacer(modifier = Modifier.height(8.dp))
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = "Location", tint = Color.Gray)
//                    Spacer(modifier = Modifier.width(4.dp))
//                    Text(
//                        text = event.location,
//                        style = MaterialTheme.typography.bodySmall, // Smaller text for location
//                        color = Color.DarkGray
//                    )
//                }
//                Spacer(modifier = Modifier.height(4.dp))
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Icon(imageVector = Icons.Default.Event, contentDescription = "Date", tint = Color.Gray)
//                    Spacer(modifier = Modifier.width(4.dp))
//                    Text(
//                        text = event.date,
//                        style = MaterialTheme.typography.bodySmall, // Smaller text for date
//                        color = Color.DarkGray
//                    )
//                }
//            }
//        }
//    }
//
//    if (showCommentDialog) {
//        CommentDialog(
//            eventId = event.id,
//            onDismiss = { showCommentDialog = false },
//            eventViewModel = eventViewModel,
//            currentUserId = currentUserId,
//            currentUsername = currentUsername,
//            navController = navController,
//            userViewModel = userViewModel
//        )
//    }
//
//    if (showShareDialog) {
//        ShareDialog(
//            onDismiss = { showShareDialog = false },
//            currentUserId = currentUserId,
//            userViewModel = userViewModel,
//            event = event,
//            navController = navController
//        )
//    }
//}

@Composable
fun ImageCard(imageData: ImageData) {
    val painter = rememberImagePainter(data = imageData.url
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .aspectRatio(imageData.aspectRatio)
            .clip(RoundedCornerShape(8.dp)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun UpcomingEventsSection(
    events: List<Event> = emptyList(),
    loading: Boolean = false,
    onEventClick: (Event) -> Unit,
    onUpdateClick: (Event) -> Unit,
    onDeleteClick: (Event) -> Unit,
    eventViewModel: EventViewModel,
    currentUserId: String,
    currentUsername: String,
    navController: NavController
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Upcoming Events",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        when {
            loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            events.isEmpty() -> {
                Text(
                    text = "No upcoming events at the moment.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            else -> {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.animateContentSize() // Smooth animations for content changes
                ) {
                    items(events) { event ->
                        UpcomingEventCard(
                            event = event,
                            onEventClick = onEventClick,
                            onUpdateClick = onUpdateClick,
                            onDeleteClick = onDeleteClick,
                            eventViewModel = eventViewModel,
                            currentUserId = currentUserId,
                            currentUsername = currentUsername,
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun UpcomingEventCard(
    event: Event,
    onEventClick: (Event) -> Unit,
    onUpdateClick: (Event) -> Unit,
    onDeleteClick: (Event) -> Unit,
    eventViewModel: EventViewModel,
    currentUserId: String,
    currentUsername: String,
    navController: NavController
) {
    val primaryBlueColor = Color(0xFF0D6EFD)
    var showMenu by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        modifier = Modifier
            .width(300.dp) // Set fixed width for the card
            .padding(8.dp)
            .clickable { onEventClick(event) },
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            // Display event image with placeholders and error handling
            Image(
                painter = rememberImagePainter(
                    data = event.imageUrl,
                    builder = {
                        crossfade(true) // Smooth transition
                        placeholder(R.drawable.placeholder) // Placeholder while loading
                        error(R.drawable.error) // Error image
                    }
                ),
                contentDescription = "Event Image",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            // Gradient overlay for text visibility
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
                // Event name
                Text(
                    text = event.name,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center),
                    maxLines = Int.MAX_VALUE // Remove ellipsis
                )
            }

            // Display event date at the bottom-left corner
            Text(
                text = event.date, // Assumes `event.date` is a formatted string
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            )

            // More options button
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                IconButton(
                    onClick = { showMenu = !showMenu },
                    modifier = Modifier.background(Color.White.copy(alpha = 0.6f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More Options",
                        tint = primaryBlueColor
                    )
                }

                // Options dropdown menu
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
                            onDeleteClick(event)
                        },
                        text = { Text("Delete") }
                    )
                }
            }
        }
    }
}
@Composable
fun PeopleYouMayKnowSection(
    viewModel: UserViewModel,
    onFollowClick: (String) -> Unit,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // Observe LiveData
    val people by viewModel.peopleYouMayKnow.observeAsState(emptyList<UserRegistration>())
    val followedUsers by viewModel.followedUsers.observeAsState(emptyList<String>())  // Observe followed users from ViewModel (List<String>)
    val followers by viewModel.usersToFollowBack.observeAsState(emptyList<UserRegistration>())  // Assuming users are of type UserRegistration

    Log.d("PeopleYouMayKnowSection", "Recomposing with ${people.size} suggestions")

    // Filter out already followed users based on followedUsers LiveData
    val remainingPeople = people.filterNot { user -> followedUsers.contains(user.id) }

    Column(modifier = modifier) {
        Text(
            text = "People You May Know",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (remainingPeople.isEmpty()) {
            Text(
                text = "No suggestions at the moment.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(remainingPeople) { user ->
                    val isFollower = followers.any { it.id == user.id }

                    PeopleYouMayKnowCard(
                        user = user,
                        isFollower = isFollower,
                        onFollowClick = { userId ->
                            // Perform Firestore follow action
                            onFollowClick(userId)
                        },
                        navController = navController,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeopleYouMayKnowCard(
    user: UserRegistration,
    isFollower: Boolean,
    onFollowClick: (String) -> Unit,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(200.dp)
            .height(300.dp)
            .padding(8.dp)
            .shadow(8.dp, shape = RoundedCornerShape(12.dp)) // Shadow for card
            .clickable {
                if (user.id.isNotEmpty()) {
                    navController.navigate("profile/${user.id}")
                }
            },
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)) // Solid background color
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5)) // Solid background color
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                // Profile Image with Elevation and Shadow
                Box(
                    modifier = Modifier
                        .size(130.dp) // Further increased size
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Transparent) // Fallback color
                        .shadow(4.dp, shape = RoundedCornerShape(16.dp)) // Reduced shadow intensity
                        .border(0.dp, Color.Transparent) // No border
                        .align(Alignment.CenterHorizontally)
                ) {
                    AsyncImage(
                        model = user.profilePhotoUrl,
                        contentDescription = "${user.username}'s profile image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(16.dp))
                    )
                }

                // Username
                Text(
                    text = user.username ?: "Unknown",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .align(Alignment.CenterHorizontally)
                )

                // Subtitle (Optional Tagline)
                Text(
                    text = "Photographer", // Placeholder; adjust dynamically if possible
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .align(Alignment.CenterHorizontally)
                )

                // Follow Button with More Breathing Room
                Button(
                    onClick = { onFollowClick(user.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D6EFD)),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .width(120.dp) // Further adjusted width for better spacing
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = if (isFollower) "Follow\nBack" else "Follow", // "Back" under "Follow"
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
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
    val userProfiles = remember { mutableStateMapOf<String, String>() } // Map to store user profiles

    LaunchedEffect(eventId) {
        eventViewModel.getComments(eventId, onSuccess = { loadedComments ->
            comments.clear()
            comments.addAll(loadedComments)

            // Fetch profile photos for each user in comments
            loadedComments.forEach { comment ->
                userViewModel.getUserProfile(comment.userId) { profilePhotoUrl ->
                    userProfiles[comment.userId] = profilePhotoUrl ?: ""
                }
            }
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
                            // Fetch the profile photo URL from the map
                            val profilePhotoUrl = userProfiles[comment.userId]

                            Image(
                                painter = rememberImagePainter(data = profilePhotoUrl),
                                contentDescription = "Profile Image",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.Gray)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Column {
                                val displayedUsername = if (comment.userId == currentUserId) "You" else comment.username
                                Text(
                                    text = displayedUsername,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable {
                                        if (comment.userId == currentUserId) {
                                            navController.navigate("profile")
                                        } else {
                                            navController.navigate("profile/${comment.userId}")
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
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        label = { Text("Add a comment") },
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.Transparent),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xFF0D6EFD),
                            unfocusedBorderColor = Color(0xFF0D6EFD)
                        )
                    )
                    IconButton(
                        onClick = {
                            eventViewModel.addComment(eventId, currentUserId, currentUsername, commentText, onSuccess = {
                                commentText = ""
                                eventViewModel.getComments(eventId, onSuccess = { loadedComments ->
                                    comments.clear()
                                    comments.addAll(loadedComments)

                                    // Fetch profile photos for each user in comments
                                    loadedComments.forEach { comment ->
                                        userViewModel.getUserProfile(comment.userId) { profilePhotoUrl ->
                                            userProfiles[comment.userId] = profilePhotoUrl ?: ""
                                        }
                                    }
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
                            contentDescription = "Send Comment",
                            tint = Color(0xFF0D6EFD)
                        )
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareDialog(
    onDismiss: () -> Unit,
    currentUserId: String,
    userViewModel: UserViewModel,
    navController: NavController,
    event: Event
) {
    var searchQuery by remember { mutableStateOf("") }
    val followingUsers by userViewModel.getFollowingUsers(currentUserId).observeAsState(emptyList())

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = (LocalContext.current.resources.displayMetrics.heightPixels * 0.75f).dp)
        ) {
            Column {
                // Search Bar
                SearchBar(searchQuery, onSearchQueryChanged = {
                    searchQuery = it
                })

                // User List
                LazyColumn {
                    items(followingUsers.filter { user ->
                        user.username.contains(searchQuery, ignoreCase = true)
                    }) { user ->
                        UserListItem(user, navController, event, currentUserId, userViewModel)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(searchQuery: String, onSearchQueryChanged: (String) -> Unit) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChanged,
        label = { Text("Search users") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = Color(0xFF0D6EFD),
            unfocusedBorderColor = Color(0xFF0D6EFD)
        )
    )
}

//@Composable
//fun UserListItem(
//    user: UserRegistration,
//    navController: NavController,
//    event: Event,
//    currentUserId: String,
//    userViewModel: UserViewModel
//) {
//    val chats by userViewModel.getUserChats(currentUserId).observeAsState(emptyList())
//
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(8.dp)
//            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
//            .padding(8.dp)
//            .clickable {
//                // Log the event image URL before navigating
//                Log.d("UserListItem", "Navigating to chat screen. Event Image URL: ${event.imageUrl}")
//
//                // Check for existing chat
//                val existingChat = chats.find { it.receiverId == user.id }
//                if (existingChat != null) {
//                    navController.navigate("individual_chat/${existingChat.id}/${user.id}?eventImageUrl=${event.imageUrl}")
//                } else {
//                    val chatId = UUID.randomUUID().toString()
//                    navController.navigate("individual_chat/$chatId/${user.id}?eventImageUrl=${event.imageUrl}")
//                }
//            },
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Image(
//            painter = rememberImagePainter(data = user.profilePhotoUrl),
//            contentDescription = null,
//            modifier = Modifier
//                .size(40.dp)
//                .clip(RoundedCornerShape(8.dp))
//                .background(Color.Gray)
//        )
//
//        Spacer(modifier = Modifier.width(8.dp))
//
//        Text(
//            text = user.username,
//            style = MaterialTheme.typography.bodyMedium
//        )
//    }
//}

@Composable
fun UserListItem(
    user: UserRegistration,
    navController: NavController,
    event: Event,
    currentUserId: String,
    userViewModel: UserViewModel
) {
    val chats by userViewModel.getUserChats(currentUserId).observeAsState(emptyList())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
            .padding(8.dp)
            .clickable {
                // Encode the event image URL
                val encodedImageUrl = URLEncoder.encode(event.imageUrl, StandardCharsets.UTF_8.toString())
                Log.d("UserListItem", "Navigating to chat screen. Event Image URL: ${event.imageUrl}")
                Log.d("UserListItem", "Encoded Image URL: $encodedImageUrl")

                // Check for existing chat
                val existingChat = chats.find { it.receiverId == user.id }
                if (existingChat != null) {
                    val url = "individual_chat/${existingChat.id}/${user.id}?eventImageUrl=$encodedImageUrl"
                    Log.d("UserListItem", "Navigating to: $url")
                    navController.navigate(url)
                } else {
                    val chatId = UUID.randomUUID().toString()
                    val url = "individual_chat/$chatId/${user.id}?eventImageUrl=$encodedImageUrl"
                    Log.d("UserListItem", "Navigating to: $url")
                    navController.navigate(url)
                }
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberImagePainter(data = user.profilePhotoUrl),
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Gray)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = user.username,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
