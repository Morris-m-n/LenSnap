package com.lensnap.app.ui.theme.screens

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.opengl.EGLConfig
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLSurfaceView.Renderer
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavHostController
import com.lensnap.app.ui.theme.screens.eventRoom.EventRoomScreen
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.*
import java.util.UUID
import com.google.accompanist.pager.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalPagerApi::class)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoCaptureTab(eventCode: String?, navController: NavHostController, viewModel: EventViewModel) {
    var showCameraScreen by remember { mutableStateOf(false) }

    if (!showCameraScreen) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { showCameraScreen = true },
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = "Launch Camera", tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(16.dp))
                IconButton(
                    onClick = { /* Open Gallery */ },
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(Icons.Filled.PhotoLibrary, contentDescription = "Select from Gallery", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    } else {
        CameraScreen(eventCode, navController, viewModel)
    }
}

@Composable
fun CameraScreen(eventCode: String?, navController: NavHostController, viewModel: EventViewModel) {
    val context = LocalContext.current
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showExitEventDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf<FilterType>(FilterType.None) }

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { PreviewView(context) }
    val gpuImage = remember { GPUImage(context) }

    // Setup Camera with Filters Applied in Real-Time
    LaunchedEffect(selectedFilter) {
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(context as LifecycleOwner, cameraSelector, preview)

                // Apply the selected filter
                applyPreviewFilter(gpuImage, selectedFilter)
            } catch (e: Exception) {
                Log.e("Camera", "Camera initialization failed: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(context))
    }

    // Back press handling
    BackHandler {
        if (capturedBitmap != null) {
            showDiscardDialog = true
        } else {
            showExitEventDialog = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Dialog to confirm discard action
        if (showDiscardDialog) {
            AlertDialog(
                onDismissRequest = { showDiscardDialog = false },
                title = { Text("Discard Image") },
                text = { Text("Are you sure you want to discard the captured image?") },
                shape = RoundedCornerShape(8.dp),
                confirmButton = {
                    TextButton(onClick = {
                        capturedBitmap = null
                        showDiscardDialog = false
                    }) {
                        Text("Discard")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDiscardDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Dialog to confirm leaving the event
        if (showExitEventDialog) {
            AlertDialog(
                onDismissRequest = { showExitEventDialog = false },
                title = { Text("Leave Event") },
                text = { Text("Are you sure you want to leave the event? Any unsaved progress will be lost.") },
                shape = RoundedCornerShape(8.dp),
                confirmButton = {
                    TextButton(onClick = {
                        showExitEventDialog = false
                        navController.popBackStack()
                    }) {
                        Text("Leave")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExitEventDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Filter Selection Buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(onClick = { selectedFilter = FilterType.Sepia }) {
                    Text("Sepia")
                }
                Button(onClick = { selectedFilter = FilterType.Grayscale }) {
                    Text("Grayscale")
                }
                Button(onClick = { selectedFilter = FilterType.None }) {
                    Text("None")
                }
            }
        }

        // Image Capture UI
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            // Capture Button
            FloatingActionButton(
                onClick = {
                    captureImage(previewView) { bitmap -> capturedBitmap = bitmap }
                },
                modifier = Modifier
                    .size(72.dp) // Adjust size of the FAB
                    .align(Alignment.CenterHorizontally), // Center the FAB horizontally
                containerColor = Color.White, // White background color for the FAB
                contentColor = Color(0xFF0D6EFD) // Primary blue color for the icon
            ) {
                Icon(
                    Icons.Filled.CameraAlt, // The camera icon
                    contentDescription = "Capture Photo",
                    tint = Color(0xFF0D6EFD) // Ensure the icon color is primary blue
                )
            }
        }

        // Image Preview and Save/Upload Buttons
        capturedBitmap?.let { bitmap ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f)) // Use `colorScheme` for Material3
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Captured Image",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Save Button
                    OutlinedButton(
                        onClick = {
                            isLoading = true
                            saveBitmapToGallery(context, bitmap) {
                                isLoading = false
                                capturedBitmap = null // Reset to camera screen
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF0D6EFD)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color(0xFF0D6EFD)
                            )
                        } else {
                            Text("Save")
                        }
                    }

                    // Upload Button
                    Button(
                        onClick = {
                            isLoading = true
                            val eventId = eventCode ?: return@Button
                            viewModel.uploadCapturedImages(
                                eventId,
                                listOf(bitmap),
                                onSuccess = {
                                    isLoading = false
                                    capturedBitmap = null // Reset to camera screen
                                },
                                onError = {
                                    isLoading = false
                                    Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0D6EFD),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White
                            )
                        } else {
                            Text("Upload")
                        }
                    }
                }
            }
        }
    }
}

// Function to apply GPU Image filter based on selection
fun applyPreviewFilter(gpuImage: GPUImage, filterType: FilterType) {
    val filter = when (filterType) {
        FilterType.Sepia -> GPUImageSepiaToneFilter()
        FilterType.Grayscale -> GPUImageGrayscaleFilter()
        else -> null
    }
    filter?.let { gpuImage.setFilter(it) }
}

// Capture Image Logic (Moved out of composable)
fun captureImage(previewView: PreviewView, onCaptured: (Bitmap) -> Unit) {
    try {
        val bitmap = previewView.bitmap ?: throw Exception("No bitmap available from preview")
        onCaptured(bitmap)
    } catch (e: Exception) {
        Log.e("PhotoCapture", "Failed to capture image: ${e.message}")
    }
}

// Helper function to save the bitmap to the gallery
fun saveBitmapToGallery(context: Context, bitmap: Bitmap, onComplete: () -> Unit) {
    val uri = createImageUri(context)
    if (uri != null) {
        try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                Toast.makeText(context, "Image saved to gallery", Toast.LENGTH_SHORT).show()
                onComplete()
            }
        } catch (e: Exception) {
            Log.e("Save", "Failed to save image: ${e.message}")
            onComplete()
        }
    } else {
        Log.e("Save", "Failed to create URI for saving image")
        onComplete()
    }
}

// Function to create URI for saving image
fun createImageUri(context: Context): Uri? {
    val contentResolver = context.contentResolver
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "${UUID.randomUUID()}.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        } else {
            val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath
            put(MediaStore.Images.Media.DATA, "$picturesDir/${UUID.randomUUID()}.jpg")
        }
    }

    return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues).also {
        if (it == null) Log.e("PhotoCapture", "Failed to create image URI")
    }
}

@Composable
fun EventRoomTab(eventCode: String?) {
    EventRoomScreen(eventCode)
}

enum class FilterType {
    Sepia, Grayscale, None
}



