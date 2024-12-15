package com.lensnap.app.ui.theme.screens

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.lensnap.app.ui.theme.screens.eventRoom.EventRoomScreen
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.*
import java.util.UUID
import com.google.accompanist.pager.*

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun MainEventScreen(eventCode: String?, navController: NavHostController, viewModel: EventViewModel) {
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()

    HorizontalPager(
        count = 2,
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        when (page) {
            0 -> PhotoCaptureTab(eventCode, navController, viewModel)
            1 -> EventRoomTab(eventCode)
        }
    }
}

@Composable
fun PhotoCaptureTab(eventCode: String?, navController: NavHostController, viewModel: EventViewModel) {
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var filteredBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var imageSelected by remember { mutableStateOf(false) }
    val gpuImage = remember { GPUImage(context) }

    // Define filters
    val filters = listOf(
        GPUImageFilter(), // Normal
        // Add other filters as desired
    )
    var selectedFilterIndex by remember { mutableStateOf(0) }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            selectedImageUri?.let { uri ->
                Log.d("PhotoCaptureTab", "Photo captured successfully: $uri")
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    gpuImage.setImage(bitmap)
                    gpuImage.setFilter(filters[selectedFilterIndex])
                    filteredBitmap = gpuImage.bitmapWithFilterApplied
                    imageSelected = true
                } catch (e: Exception) {
                    Log.e("PhotoCaptureTab", "Error processing captured photo: ${e.message}", e)
                }
            }
        } else {
            Log.e("PhotoCaptureTab", "Failed to capture photo")
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            Log.d("PhotoCaptureTab", "Image selected from gallery: $uri")
            selectedImageUri = it
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                gpuImage.setImage(bitmap)
                gpuImage.setFilter(filters[selectedFilterIndex])
                filteredBitmap = gpuImage.bitmapWithFilterApplied
                imageSelected = true
            } catch (e: Exception) {
                Log.e("PhotoCaptureTab", "Error processing selected image: ${e.message}", e)
            }
        } ?: Log.e("PhotoCaptureTab", "No image selected from gallery")
    }

    Box(modifier = Modifier.fillMaxSize()) {
        filteredBitmap?.let { bitmap ->
            Image(bitmap = bitmap.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxSize())
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            IconButton(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier.align(Alignment.Bottom)
            ) {
                Icon(Icons.Filled.PhotoLibrary, contentDescription = "Select from Gallery")
            }
            IconButton(
                onClick = {
                    val uri = createImageUri(context)
                    selectedImageUri = uri
                    cameraLauncher.launch(uri)
                },
                modifier = Modifier.align(Alignment.Bottom)
            ) {
                Icon(Icons.Filled.CameraAlt, contentDescription = "Capture Photo")
            }
        }

        if (imageSelected) {
            Column(
                modifier = Modifier.align(Alignment.Center)
            ) {
                // Filter selection
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                ) {
                    filters.forEachIndexed { index, filter ->
                        Button(
                            onClick = {
                                selectedFilterIndex = index
                                selectedImageUri?.let { uri ->
                                    val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                                    gpuImage.setImage(bitmap)
                                    gpuImage.setFilter(filters[selectedFilterIndex])
                                    filteredBitmap = gpuImage.bitmapWithFilterApplied
                                }
                            },
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Text("Filter $index")
                        }
                    }
                }

                // Upload image
                Button(
                    onClick = {
                        val eventId = eventCode ?: return@Button
                        selectedImageUri?.let { uri ->
                            viewModel.uploadGalleryImages(eventId, listOf(uri), onSuccess = {
                                Log.d("PhotoCaptureTab", "Gallery image uploaded successfully")
                                imageSelected = false
                            }, onError = { error ->
                                Log.e("PhotoCaptureTab", "Failed to upload gallery image: $error")
                            })
                        }
                        filteredBitmap?.let { bitmap ->
                            viewModel.uploadCapturedImages(eventId, listOf(bitmap), onSuccess = {
                                Log.d("PhotoCaptureTab", "Captured image uploaded successfully")
                                imageSelected = false
                            }, onError = { error ->
                                Log.e("PhotoCaptureTab", "Failed to upload captured image: $error")
                            })
                        }

                    },
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Text("Upload Image")
                }
            }
        }
    }
}

fun createImageUri(context: Context): Uri {
    val contentResolver = context.contentResolver
    val contentValues = android.content.ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "${UUID.randomUUID()}.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
    }
    return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)!!
}

@Composable
fun EventRoomTab(eventCode: String?) {
    EventRoomScreen(eventCode)
}

