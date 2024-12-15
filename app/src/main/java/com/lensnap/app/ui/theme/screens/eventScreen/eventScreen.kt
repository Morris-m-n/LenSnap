//package com.lensnap.app.ui.theme.screens.eventScreen
//
//import com.lensnap.app.ui.theme.screens.dashboard.EventCardWithDetails
//
//package com.lensnap.app.ui.theme.screens
//
//import android.util.Log
//import androidx.compose.foundation.layout.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.lifecycle.viewmodel.compose.viewModel
//import com.lensnap.app.ui.theme.screens.components.EventCardWithDetails
//
//@Composable
//fun EventScreen(viewModel: EventViewModel = viewModel(), eventId: String, userId: String) {
//    val capturedImages = remember { mutableStateListOf<String>() }
//    var likeCount by remember { mutableStateOf(0) }
//    var commentCount by remember { mutableStateOf(0) }
//
//    LaunchedEffect(eventId) {
//        viewModel.getLikesCount(eventId, onSuccess = { count ->
//            likeCount = count
//        }, onError = { error ->
//            Log.e("EventScreen", "Failed to retrieve likes count: $error")
//        })
//
//        viewModel.getComments(eventId, onSuccess = { comments ->
//            commentCount = comments.size
//        }, onError = { error ->
//            Log.e("EventScreen", "Failed to retrieve comments: $error")
//        })
//    }
//
//    viewModel.loadEventDetails(eventId) { event ->
//        EventCardWithDetails(
//            event = event,
//            capturedImages = capturedImages,
//            onEventClick = { /* Navigate to event details */ },
//            likeCount = likeCount,
//            commentCount = commentCount,
//            onLikeClick = {
//                viewModel.addLike(eventId, userId, onSuccess = {
//                    likeCount += 1
//                }, onError = { error ->
//                    Log.e("EventScreen", "Failed to like: $error")
//                })
//            },
//            onCommentClick = {
//                // Handle comment click
//            }
//        )
//    }
//}
