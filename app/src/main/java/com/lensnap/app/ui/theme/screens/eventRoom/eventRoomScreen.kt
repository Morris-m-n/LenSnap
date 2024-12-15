package com.lensnap.app.ui.theme.screens.eventRoom

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.ktx.storage
import com.google.firebase.ktx.Firebase
import android.graphics.BitmapFactory
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventRoomScreen(eventCode: String?) {
    val context = LocalContext.current
    var photos by remember { mutableStateOf<List<String>>(emptyList()) }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var pairingCode by remember { mutableStateOf<String?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    // Fetch event data
    LaunchedEffect(eventCode) {
        if (eventCode != null) {
            fetchEventPhotos(eventCode) { fetchedPhotos ->
                photos = fetchedPhotos
            }
            fetchEventQRCode(eventCode) { bitmap ->
                qrBitmap = bitmap
            }
            fetchEventPairingCode(eventCode) { code ->
                pairingCode = code
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event Room") }
            )
        },
        content = { paddingValues ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Text("Welcome to the Event Room!")
                Text("Event Code: $eventCode")
                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { showDialog = true }) {
                    Text("Show QR Code & Pairing Code")
                }

                if (showDialog) {
                    ShowDialog(
                        qrBitmap = qrBitmap,
                        pairingCode = pairingCode,
                        onDismiss = { showDialog = false }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (photos.isEmpty()) {
                    Text("No photos yet. Start capturing and uploading photos!")
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(photos) { photoUrl ->
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(photoUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Event Photo",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun ShowDialog(
    qrBitmap: Bitmap?,
    pairingCode: String?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        },
        title = { Text("Event QR Code & Pairing Code") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                qrBitmap?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Event QR Code",
                        modifier = Modifier.size(200.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Pairing Code: ${pairingCode ?: "Loading..."}")
            }
        }
    )
}

fun fetchEventPhotos(eventCode: String, onPhotosFetched: (List<String>) -> Unit) {
    val storage = Firebase.storage
    val storageRef = storage.reference.child("events/$eventCode/photos")

    storageRef.listAll().addOnSuccessListener { listResult ->
        val photoUrls = listResult.items.map { it.path }
        onPhotosFetched(photoUrls)
    }.addOnFailureListener {
        onPhotosFetched(emptyList())
    }
}

fun fetchEventQRCode(eventCode: String, onQRCodeFetched: (Bitmap?) -> Unit) {
    val storage = Firebase.storage
    val storageRef = storage.reference.child("events/$eventCode/qrCode.jpg")
    val localFile = File.createTempFile("qrCode", "jpg")

    storageRef.getFile(localFile).addOnSuccessListener {
        val bitmap = BitmapFactory.decodeFile(localFile.path)
        onQRCodeFetched(bitmap)
    }.addOnFailureListener {
        onQRCodeFetched(null)
    }
}

fun fetchEventPairingCode(eventCode: String, onPairingCodeFetched: (String?) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("events").document(eventCode).get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                val pairingCode = document.getString("pairingCode")
                onPairingCodeFetched(pairingCode)
            } else {
                onPairingCodeFetched(null)
            }
        }
        .addOnFailureListener {
            onPairingCodeFetched(null)
        }
}
