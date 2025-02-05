package com.lensnap.app.ui.theme.screens

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Camera
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
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.google.common.util.concurrent.ListenableFuture
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import kotlinx.coroutines.launch
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
            0 -> CameraScreen(eventCode, navController, viewModel) // Directly launch CameraScreen
            1 -> EventRoomTab(eventCode)
        }
    }
}

@Composable
fun CameraScreen(eventCode: String?, navController: NavHostController, viewModel: EventViewModel) {
    val context = LocalContext.current
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val showDiscardDialog = remember { mutableStateOf(false) }
    val showExitEventDialog = remember { mutableStateOf(false) }
    val selectedFilter = remember { mutableStateOf(FilterType.None) }

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { PreviewView(context) }
    val gpuImage = remember { GPUImage(context) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bitmap = getBitmapFromUri(context, it)
            capturedBitmap = bitmap
        }
    }

    SetupCamera(cameraProviderFuture, previewView, selectedFilter, gpuImage)

    // Back press handling
    BackHandler {
        if (capturedBitmap != null) {
            showDiscardDialog.value = true
        } else {
            showExitEventDialog.value = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        DiscardImageDialog(showDiscardDialog, capturedBitmap) {
            capturedBitmap = null
        }

        ExitEventDialog(showExitEventDialog, navController) {
            showExitEventDialog.value = false
        }

        FilterSelectionButtons(selectedFilter)

        ImageCaptureUI(previewView) { bitmap ->
            capturedBitmap = bitmap
        }

        CameraOverlay() // Add overlay here

        // FloatingActionButton to launch the gallery
        FloatingActionButton(
            onClick = { launcher.launch("image/* video/*") },
            containerColor = Color(0xFF0D6EFD),
            contentColor = Color.White,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd)
        ) {
            Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = "Select from Gallery")
        }

        capturedBitmap?.let { bitmap ->
            ImagePreview(bitmap, isLoading, context, eventCode, viewModel) {
                capturedBitmap = null
            }
        }
    }
}

fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        BitmapFactory.decodeStream(inputStream)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun SetupCamera(
    cameraProviderFuture: ListenableFuture<ProcessCameraProvider>,
    previewView: PreviewView,
    selectedFilter: MutableState<FilterType>,
    gpuImage: GPUImage
) {
    val context = LocalContext.current

    LaunchedEffect(selectedFilter.value) {
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()
                val camera = cameraProvider.bindToLifecycle(context as LifecycleOwner, cameraSelector, preview)

                // Apply the selected filter
                applyPreviewFilter(gpuImage, selectedFilter.value)

                // Set the correct scale type to avoid zoom issues
                previewView.scaleType = PreviewView.ScaleType.FILL_CENTER

                // Optionally set zoom ratio to the default value
                camera.cameraControl.setZoomRatio(1.0f)

            } catch (e: Exception) {
                Log.e("Camera", "Camera initialization failed: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(context))
    }
}

@Composable
fun CameraOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        // Add any overlay components like grid lines or borders here
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw grid lines as an example
            drawLine(
                color = Color.White.copy(alpha = 0.5f),
                start = Offset(x = 0f, y = size.height / 3),
                end = Offset(x = size.width, y = size.height / 3),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = Color.White.copy(alpha = 0.5f),
                start = Offset(x = 0f, y = 2 * size.height / 3),
                end = Offset(x = size.width, y = 2 * size.height / 3),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = Color.White.copy(alpha = 0.5f),
                start = Offset(x = size.width / 3, y = 0f),
                end = Offset(x = size.width / 3, y = size.height),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = Color.White.copy(alpha = 0.5f),
                start = Offset(x = 2 * size.width / 3, y = 0f),
                end = Offset(x = 2 * size.width / 3, y = size.height),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}

@Composable
fun DiscardImageDialog(showDiscardDialogState: MutableState<Boolean>, capturedBitmap: Bitmap?, onDiscard: () -> Unit) {
    if (showDiscardDialogState.value) {
        AlertDialog(
            onDismissRequest = { showDiscardDialogState.value = false },
            title = { Text("Discard Image") },
            text = { Text("Are you sure you want to discard the captured image?") },
            shape = RoundedCornerShape(8.dp),
            confirmButton = {
                TextButton(onClick = {
                    onDiscard()
                    showDiscardDialogState.value = false
                }) {
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialogState.value = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ExitEventDialog(showExitEventDialogState: MutableState<Boolean>, navController: NavHostController, onDismiss: () -> Unit) {
    if (showExitEventDialogState.value) {
        AlertDialog(
            onDismissRequest = { showExitEventDialogState.value = false },
            title = { Text("Leave Event") },
            text = { Text("Are you sure you want to leave the event? Any unsaved progress will be lost.") },
            shape = RoundedCornerShape(8.dp),
            confirmButton = {
                TextButton(onClick = {
                    showExitEventDialogState.value = false
                    navController.popBackStack()
                }) {
                    Text("Leave")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitEventDialogState.value = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun FilterSelectionButtons(selectedFilter: MutableState<FilterType>) {
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
            Button(onClick = { selectedFilter.value = FilterType.Sepia }) {
                Text("Sepia")
            }
            Button(onClick = { selectedFilter.value = FilterType.Grayscale }) {
                Text("Grayscale")
            }
            Button(onClick = { selectedFilter.value = FilterType.None }) {
                Text("None")
            }
        }
    }
}

@Composable
fun ImageCaptureUI(previewView: PreviewView, onCapture: (Bitmap) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        // Capture Button
        FloatingActionButton(
            onClick = {
                captureImage(previewView) { bitmap -> onCapture(bitmap) }
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
}

@Composable
fun ImagePreview(
    bitmap: Bitmap,
    isLoading: Boolean,
    context: Context,
    eventCode: String?,
    viewModel: EventViewModel,
    onReset: () -> Unit
) {
    var mutableLoading by remember { mutableStateOf(isLoading) }

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
                    mutableLoading = true
                    saveBitmapToGallery(context, bitmap) {
                        mutableLoading = false
                        onReset() // Reset to camera screen
                    }
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF0D6EFD)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (mutableLoading) {
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
                    mutableLoading = true
                    val eventId = eventCode ?: return@Button
                    viewModel.uploadCapturedImages(
                        eventId,
                        listOf(bitmap),
                        onSuccess = {
                            mutableLoading = false
                            onReset() // Reset to camera screen
                        },
                        onError = {
                            mutableLoading = false
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
                if (mutableLoading) {
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