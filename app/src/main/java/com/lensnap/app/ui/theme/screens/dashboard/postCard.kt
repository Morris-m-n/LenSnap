package com.lensnap.app.ui.theme.screens.dashboard

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.rememberImagePainter
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.lensnap.app.models.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.graphics.Paint
import android.graphics.Rect
import android.util.Log
import android.view.ViewTreeObserver
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsCompat
import androidx.palette.graphics.Palette
import coil.compose.rememberAsyncImagePainter
import com.lensnap.app.data.UserViewModel
import com.lensnap.app.models.PostComments
import kotlinx.coroutines.launch
import android.widget.FrameLayout
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.SimpleExoPlayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostCard(
    post: Post,
    isLiked: Boolean,
    onLikeClick: () -> Unit,
    onUnlikeClick: () -> Unit,
    onShareClick: () -> Unit,
    isLikedState: MutableState<Boolean>,
    totalLikes: Int,
    userViewModel: UserViewModel, // Pass the UserViewModel to the composable
    currentUserId: String, // Pass the currentUserId to the composable
    navController: NavController // Pass the NavController to the composable
) {
    val greyColor = Color.Gray
    val redColor = Color.Red
    val context = LocalContext.current

    var mediaBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var dominantColor by remember { mutableStateOf(Color.Transparent) }
    var imageWidth by remember { mutableStateOf(1) }
    var imageHeight by remember { mutableStateOf(1) }

    var commentsCount by remember { mutableStateOf(0) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(post.id) {
        coroutineScope.launch {
            commentsCount = userViewModel.getCommentsCount(post.userId, post.id)
        }
    }

    LaunchedEffect(post.mediaUrl) {
        mediaBitmap = getBitmapFromUrl(post.mediaUrl, context)
        mediaBitmap?.let { bitmap ->
            imageWidth = bitmap.width
            imageHeight = bitmap.height

            Palette.from(bitmap).generate { palette ->
                palette?.dominantSwatch?.rgb?.let { colorValue ->
                    dominantColor = Color(colorValue)
                }
            }
        }
    }

    val aspectRatio = if (imageHeight != 0) imageWidth.toFloat() / imageHeight else 1f
    val maxHeight = 400.dp
    val roundedCornerShape = RoundedCornerShape(16.dp)

    var showBottomSheet by remember { mutableStateOf(false) }
    val commentText = remember { mutableStateOf("") }

    CommentBottomSheet(
        showBottomSheet = showBottomSheet,
        onDismissRequest = { showBottomSheet = false },
        commentText = commentText,
        onPostComment = {
            if (commentText.value.isNotBlank()) {
                coroutineScope.launch {
                    Log.d("FirestoreDebug", "Posting comment: ${commentText.value}")
                    userViewModel.addPostComment(post.userId, post.id, commentText.value) {
                        // Hide the bottom sheet after the comment is posted
                        showBottomSheet = false
                        // Clear the comment text
                        commentText.value = ""
                        // Update the comments count
                        coroutineScope.launch {
                            commentsCount = userViewModel.getCommentsCount(post.userId, post.id)
                        }
                    }
                }
            } else {
                Log.e("FirestoreDebug", "Comment text is blank")
            }
        },
        userId = post.userId, // Use post.userId as postOwnerId
        postId = post.id,
        userViewModel = userViewModel
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(roundedCornerShape),
        shape = roundedCornerShape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.background(Color.Transparent)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(roundedCornerShape)
                    .background(dominantColor)
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(12.dp))
            ) {
                if (post.mediaType == "video") {
                    VideoPlayer(post.mediaUrl)
                } else {
                    mediaBitmap?.let { bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Post Media",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = maxHeight)
                                .aspectRatio(aspectRatio, matchHeightConstraintsFirst = true)
                                .clip(roundedCornerShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                ClickableText(
                    text = AnnotatedString.Builder(post.username).toAnnotatedString(),
                    onClick = {
                        navController.navigate("profile/${post.userId}")
                    },
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = Color.White),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (isLikedState.value) {
                        onUnlikeClick()
                    } else {
                        onLikeClick()
                    }
                    isLikedState.value = !isLikedState.value
                }) {
                    Icon(
                        imageVector = if (isLikedState.value) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLikedState.value) redColor else greyColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = totalLikes.toString(),
                    color = greyColor,
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = { showBottomSheet = true }) {
                    Icon(
                        imageVector = Icons.Default.Comment,
                        contentDescription = "Comment",
                        tint = greyColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = commentsCount.toString(),
                    color = greyColor,
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = onShareClick) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Share",
                        tint = greyColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Text(
                text = post.caption,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun PostCard(
//    post: Post,
//    isLiked: Boolean,
//    onLikeClick: () -> Unit,
//    onUnlikeClick: () -> Unit,
//    onShareClick: () -> Unit,
//    isLikedState: MutableState<Boolean>,
//    totalLikes: Int,
//    userViewModel: UserViewModel, // Pass the UserViewModel to the composable
//    currentUserId: String // Pass the currentUserId to the composable
//) {
//    val greyColor = Color.Gray
//    val redColor = Color.Red
//    val context = LocalContext.current
//
//    var mediaBitmap by remember { mutableStateOf<Bitmap?>(null) }
//    var dominantColor by remember { mutableStateOf(Color.Transparent) }
//    var imageWidth by remember { mutableStateOf(1) }
//    var imageHeight by remember { mutableStateOf(1) }
//
//    var commentsCount by remember { mutableStateOf(0) }
//
//    val coroutineScope = rememberCoroutineScope()
//
//    LaunchedEffect(post.id) {
//        coroutineScope.launch {
//            commentsCount = userViewModel.getCommentsCount(post.userId, post.id)
//        }
//    }
//
//    LaunchedEffect(post.mediaUrl) {
//        mediaBitmap = getBitmapFromUrl(post.mediaUrl, context)
//        mediaBitmap?.let { bitmap ->
//            imageWidth = bitmap.width
//            imageHeight = bitmap.height
//
//            Palette.from(bitmap).generate { palette ->
//                palette?.dominantSwatch?.rgb?.let { colorValue ->
//                    dominantColor = Color(colorValue)
//                }
//            }
//        }
//    }
//
//    val aspectRatio = if (imageHeight != 0) imageWidth.toFloat() / imageHeight else 1f
//    val maxHeight = 400.dp
//    val roundedCornerShape = RoundedCornerShape(16.dp)
//
//    var showBottomSheet by remember { mutableStateOf(false) }
//    val commentText = remember { mutableStateOf("") }
//
//    CommentBottomSheet(
//        showBottomSheet = showBottomSheet,
//        onDismissRequest = { showBottomSheet = false },
//        commentText = commentText,
//        onPostComment = {
//            if (commentText.value.isNotBlank()) {
//                coroutineScope.launch {
//                    Log.d("FirestoreDebug", "Posting comment: ${commentText.value}")
//                    userViewModel.addPostComment(post.userId, post.id, commentText.value) {
//                        // Hide the bottom sheet after the comment is posted
//                        showBottomSheet = false
//                        // Clear the comment text
//                        commentText.value = ""
//                        // Update the comments count
//                        coroutineScope.launch {
//                            commentsCount = userViewModel.getCommentsCount(post.userId, post.id)
//                        }
//                    }
//                }
//            } else {
//                Log.e("FirestoreDebug", "Comment text is blank")
//            }
//        },
//        userId = post.userId, // Use post.userId as postOwnerId
//        postId = post.id,
//        userViewModel = userViewModel
//    )
//
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(16.dp)
//            .clip(roundedCornerShape),
//        shape = roundedCornerShape,
//        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
//    ) {
//        Column(modifier = Modifier.background(Color.Transparent)) {
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .clip(roundedCornerShape)
//                    .background(dominantColor)
//                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(12.dp))
//            ) {
//                if (post.mediaType == "video") {
//                    VideoPlayer(post.mediaUrl)
//                } else {
//                    mediaBitmap?.let { bitmap ->
//                        Image(
//                            bitmap = bitmap.asImageBitmap(),
//                            contentDescription = "Post Media",
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .heightIn(max = maxHeight)
//                                .aspectRatio(aspectRatio, matchHeightConstraintsFirst = true)
//                                .clip(roundedCornerShape),
//                            contentScale = ContentScale.Crop
//                        )
//                    }
//                }
//
//                Text(
//                    text = post.username,
//                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
//                    color = Color.White,
//                    modifier = Modifier
//                        .align(Alignment.BottomStart)
//                        .padding(8.dp)
//                )
//            }
//
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .background(Color.Transparent)
//                    .padding(horizontal = 4.dp, vertical = 2.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                IconButton(onClick = {
//                    if (isLikedState.value) {
//                        onUnlikeClick()
//                    } else {
//                        onLikeClick()
//                    }
//                    isLikedState.value = !isLikedState.value
//                }) {
//                    Icon(
//                        imageVector = if (isLikedState.value) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
//                        contentDescription = "Like",
//                        tint = if (isLikedState.value) redColor else greyColor,
//                        modifier = Modifier.size(20.dp)
//                    )
//                }
//                Text(
//                    text = totalLikes.toString(),
//                    color = greyColor,
//                    style = MaterialTheme.typography.bodySmall
//                )
//
//                Spacer(modifier = Modifier.width(8.dp))
//
//                IconButton(onClick = { showBottomSheet = true }) {
//                    Icon(
//                        imageVector = Icons.Default.Comment,
//                        contentDescription = "Comment",
//                        tint = greyColor,
//                        modifier = Modifier.size(20.dp)
//                    )
//                }
//                Text(
//                    text = commentsCount.toString(),
//                    color = greyColor,
//                    style = MaterialTheme.typography.bodySmall
//                )
//
//                Spacer(modifier = Modifier.width(8.dp))
//
//                IconButton(onClick = onShareClick) {
//                    Icon(
//                        imageVector = Icons.Outlined.Share,
//                        contentDescription = "Share",
//                        tint = greyColor,
//                        modifier = Modifier.size(20.dp)
//                    )
//                }
//            }
//
//            Text(
//                text = post.caption,
//                style = MaterialTheme.typography.bodyLarge,
//                color = Color.Black,
//                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
//            )
//        }
//    }
//}

@Composable
fun VideoPlayer(videoUrl: String) {
    val context = LocalContext.current
    val exoPlayer = remember {
        SimpleExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl))
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = { context ->
            PlayerView(context).apply {
                player = exoPlayer
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 400.dp)
            .aspectRatio(16 / 9f)
            .clip(RoundedCornerShape(16.dp))
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentBottomSheet(
    showBottomSheet: Boolean,
    onDismissRequest: () -> Unit,
    commentText: MutableState<String>,
    onPostComment: () -> Unit,
    userId: String,
    postId: String,
    userViewModel: UserViewModel
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )
    val coroutineScope = rememberCoroutineScope()
    val comments = remember { mutableStateListOf<PostComments>() }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            comments.addAll(userViewModel.getPostComments(userId, postId))
        }
    }

    // Get the keyboard controller
    val keyboardController = LocalSoftwareKeyboardController.current

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = sheetState,
            modifier = Modifier.fillMaxHeight(fraction = 0.75f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .imePadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = commentText.value,
                        onValueChange = { commentText.value = it },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .padding(end = 8.dp),
                        maxLines = 15,
                        shape = RoundedCornerShape(16.dp),
                        textStyle = TextStyle(fontSize = 14.sp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            containerColor = Color(0xFFF0F0F0),
                            cursorColor = Color(0xFF0D6EFD),
                            focusedLabelColor = Color(0xFF0D6EFD)
                        ),
                        placeholder = { Text(text = "Comment...", fontSize = 14.sp) }
                    )

                    Button(
                        onClick = {
                            if (commentText.value.isNotBlank()) {
                                coroutineScope.launch {
                                    Log.d("FirestoreDebug", "Adding comment: ${commentText.value}")
                                    userViewModel.addPostComment(userId, postId, commentText.value) {
                                        Log.d("FirestoreDebug", "Comment added successfully")
                                        commentText.value = "" // Clear input field after posting
                                        keyboardController?.hide() // Hide keyboard after posting
                                        onPostComment()
                                        // Fetch and update comments within coroutine scope
                                        coroutineScope.launch {
                                            comments.clear()
                                            comments.addAll(userViewModel.getPostComments(userId, postId))
                                        }
                                    }
                                }
                            } else {
                                Log.e("FirestoreDebug", "Comment text is blank")
                            }
                        },
                        enabled = commentText.value.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0D6EFD)
                        ),
                        modifier = Modifier.size(50.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = "Send",
                            tint = Color.White
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    items(comments) { comment ->
                        CommentItem(comment = comment)
                    }
                }
            }
        }
    }
}

@Composable
fun CommentItem(comment: PostComments) {
    val maxCommentLength = 100 // Adjust the maximum comment length as needed
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.LightGray),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Profile Image
            Image(
                painter = rememberAsyncImagePainter(model = comment.profilePhotoUrl),
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
            // Username and Comment
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = comment.username,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF333333)
                )

                // Display truncated or full comment based on the expanded state
                if (comment.commentText.length > maxCommentLength && !expanded) {
                    Text(
                        text = comment.commentText.substring(0, maxCommentLength) + "...",
                        fontSize = 14.sp,
                        color = Color(0xFF666666),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Text(
                        text = "Read More",
                        fontSize = 14.sp,
                        color = Color.Blue,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clickable { expanded = true }
                    )
                } else {
                    Text(
                        text = comment.commentText,
                        fontSize = 14.sp,
                        color = Color(0xFF666666),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            // Comment Icon
            Icon(
                imageVector = Icons.Default.Comment,
                contentDescription = "Comment Icon",
                tint = Color(0xFF888888),
                modifier = Modifier
                    .size(7.dp)
                    .padding(start = 8.dp)
            )
        }
    }
}

private suspend fun getBitmapFromUrl(url: String, context: Context): Bitmap? {
    return withContext(Dispatchers.IO) {
        val request = ImageRequest.Builder(context)
            .data(url)
            .allowHardware(false)
            .build()

        val result = (request.context.imageLoader.execute(request) as? SuccessResult)?.drawable
        (result as? BitmapDrawable)?.bitmap
    }
}
