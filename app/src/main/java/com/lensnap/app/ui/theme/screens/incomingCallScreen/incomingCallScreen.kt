package com.lensnap.app.ui.theme.screens.incomingCallScreen

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter

@Composable
fun IncomingCallScreen(
    navController: NavController,
    receiverId: String,
    callerName: String,
    callerProfileUrl: String,
    isVideoCall: Boolean,
    onAcceptCall: () -> Unit,
    onRejectCall: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isVideoCall) "Incoming Video Call" else "Incoming Voice Call",
            style = MaterialTheme.typography.h4.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Image(
            painter = rememberImagePainter(data = callerProfileUrl),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = callerName,
            style = MaterialTheme.typography.h6
        )
        Spacer(modifier = Modifier.height(20.dp))
        Row {
            IconButton(onClick = {
                onRejectCall()
                navController.popBackStack()
            }) {
                Icon(Icons.Default.CallEnd, contentDescription = "Reject Call", tint = Color.Red)
            }
            Spacer(modifier = Modifier.width(30.dp))
            IconButton(onClick = {
                onAcceptCall()
                navController.navigate("callScreen/$receiverId/${if (isVideoCall) "video" else "voice"}")
            }) {
                Icon(Icons.Default.Call, contentDescription = "Accept Call", tint = Color.Green)
            }
        }
    }
}
