package com.lensnap.app.ui.theme.screens.joinEventScreen

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import android.annotation.SuppressLint
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import com.google.firebase.firestore.DocumentSnapshot
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import androidx.camera.core.CameraSelector
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.google.mlkit.vision.barcode.BarcodeScanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinEventScreen(navController: NavHostController, onJoinSuccess: (String) -> Unit) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    var errorMessage by remember { mutableStateOf("") }

    // Permission launcher for camera access
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Camera permission is required to scan QR codes", Toast.LENGTH_LONG).show()
        }
    }

    // Request camera permission on start
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = { JoinEventTopBar() },
        content = { paddingValues ->
            JoinEventContent(
                navController = navController,
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                onScanResult = { qrCodeContents: String ->
                    verifyQrCode(qrCodeContents, onSuccess = { eventDocument ->
                        val eventId = eventDocument.id
                        Toast.makeText(context, "Successfully joined the event!", Toast.LENGTH_SHORT).show() // Success toast
                        onJoinSuccess(eventId)
                        navController.navigate("photoCapture/$eventId")
                    }, onError = { error ->
                        errorMessage = error
                    })
                },
                onPairingCodeEntered = { pairingCode: String ->
                    verifyPairingCode(pairingCode, onSuccess = { eventDocument ->
                        val eventId = eventDocument.id
                        Toast.makeText(context, "Successfully joined the event!", Toast.LENGTH_SHORT).show() // Success toast
                        onJoinSuccess(eventId)
                        navController.navigate("photoCapture/$eventId")
                    }, onError = { error ->
                        errorMessage = error
                    })
                },
                errorMessage = errorMessage,
                paddingValues = paddingValues
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinEventTopBar() {
    TopAppBar(
        title = { Text("Join Event", fontSize = 35.sp, fontWeight = FontWeight.Thin) },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
    )
}

@Composable
fun JoinEventContent(
    navController: NavHostController,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onScanResult: (String) -> Unit,
    onPairingCodeEntered: (String) -> Unit,
    errorMessage: String,
    paddingValues: PaddingValues
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
    ) {
        CenteredFABs(selectedTab = selectedTab, onTabSelected = onTabSelected)
        Spacer(modifier = Modifier.height(16.dp))

        // Fixed container for tab content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp) // Set a fixed height for tab content
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF9F9F9)) // Optional background for a cleaner look
                .padding(16.dp)
        ) {
            when (selectedTab) {
                0 -> QRCodeScanner(onScanResult = onScanResult)
                1 -> PairingCodeScreen(onJoinSuccess = onPairingCodeEntered, navController = navController)
            }
        }

        // Error message
        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                errorMessage,
                color = Color.Red,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun CenteredFABs(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(40.dp),
            modifier = Modifier.align(Alignment.TopCenter),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // QR Code Icon
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .background(if (selectedTab == 0) Color(0xFFF0F8FF) else Color.Transparent)
                    .clip(RoundedCornerShape(16.dp)) // Rounded corners for the background
                    .padding(8.dp)
            ) {
                IconButton(
                    onClick = { onTabSelected(0) },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = "Scan QR Code",
                        tint = if (selectedTab == 0) Color(0xFF0d6efd) else Color.Black
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "QR Code",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selectedTab == 0) Color(0xFF0d6efd) else Color.Black
                )
            }

            // Pairing Code Icon
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .background(if (selectedTab == 1) Color(0xFFF0F8FF) else Color.Transparent)
                    .clip(RoundedCornerShape(16.dp)) // Rounded corners for the background
                    .padding(8.dp)
            ) {
                IconButton(
                    onClick = { onTabSelected(1) },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Password,
                        contentDescription = "Use Pairing Code",
                        tint = if (selectedTab == 1) Color(0xFF0D6EFD) else Color.Black
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Pairing Code",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selectedTab == 1) Color(0xFF0d6efd) else Color.Black
                )
            }
        }
    }
}

//@Composable
//fun CenteredFABs(selectedTab: Int, onTabSelected: (Int) -> Unit) {
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
////            .padding(top = 50.dp)
//    ) {
//        Row(
//            horizontalArrangement = Arrangement.spacedBy(40.dp),
//            modifier = Modifier.align(Alignment.TopCenter),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            // QR Code FAB
//            Column(
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                FloatingActionButton(
//                    onClick = { onTabSelected(0) },
//                    modifier = Modifier.size(56.dp),
//                    containerColor = if (selectedTab == 0) Color(0xFFF0F8FF) else Color.White
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.QrCode,
//                        contentDescription = "Scan QR Code",
//                        tint = Color(0xFF0d6efd)
//                    )
//                }
//                Text(
//                    "QR Code",
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = if (selectedTab == 0) Color(0xFF0d6efd) else Color.Black
//                )
//            }
//
//            // Pairing Code FAB
//            Column(
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                FloatingActionButton(
//                    onClick = { onTabSelected(1) },
//                    modifier = Modifier.size(56.dp),
//                    containerColor = if (selectedTab == 1) Color(0xFFF0F8FF) else Color.White
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Password,
//                        contentDescription = "Use Pairing Code",
//                        tint = Color(0xFF0d6efd)
//                    )
//                }
//                Text(
//                    "Pairing Code",
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = if (selectedTab == 1) Color(0xFF0d6efd) else Color.Black
//                )
//            }
//        }
//    }
//}

@Composable
fun QRCodeScanner(onScanResult: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Scan QR Code", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))

        Box(
            modifier = Modifier
                .size(300.dp)
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.background)
                .clip(RoundedCornerShape(16.dp)) // Rounded corners for the scanner view
        ) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )

            LaunchedEffect(cameraProviderFuture) {
                try {
                    val cameraProvider = cameraProviderFuture.get() as ProcessCameraProvider
                    bindCameraUseCases(cameraProvider, previewView, lifecycleOwner, onScanResult)
                } catch (e: Exception) {
                    Log.e("QRCodeScanner", "Camera initialization failed", e)
                }
            }
        }
    }
}

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun JoinEventScreen(navController: NavHostController, onJoinSuccess: (String) -> Unit) {
//    val context = LocalContext.current
//    var selectedTab by remember { mutableStateOf(0) }
//    var errorMessage by remember { mutableStateOf("") }
//
//    // Permission launcher for camera access
//    val permissionLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.RequestPermission()
//    ) { isGranted ->
//        if (!isGranted) {
//            Toast.makeText(context, "Camera permission is required to scan QR codes", Toast.LENGTH_LONG).show()
//        }
//    }
//
//    // Request camera permission on start
//    LaunchedEffect(Unit) {
//        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//            permissionLauncher.launch(Manifest.permission.CAMERA)
//        }
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Join Event", color = Color.Black) },
//                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
//                actions = {
//                    IconButton(onClick = { /* Action on top app bar */ }) {
//                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More")
//                    }
//                }
//            )
//        },
//        content = { paddingValues ->
//            Column(
//                horizontalAlignment = Alignment.CenterHorizontally,
//                verticalArrangement = Arrangement.Center,
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(paddingValues)
//                    .padding(16.dp)
//            ) {
//                // Centered FABs with padding from the top
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(top = 50.dp) // Add padding for distinction from the top app bar
//                ) {
//                    Row(
//                        horizontalArrangement = Arrangement.spacedBy(40.dp),
//                        modifier = Modifier.align(Alignment.TopCenter),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        // QR Code FAB
//                        FloatingActionButton(
//                            onClick = { selectedTab = 0 },
//                            modifier = Modifier.size(56.dp),
//                            containerColor = if (selectedTab == 0) Color(0xFFF0F8FF) else Color.White
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.QrCode,
//                                contentDescription = "Scan QR Code",
//                                tint = Color(0xFF0d6efd) // Icon color
//                            )
//                        }
//
//                        // Pairing Code FAB
//                        FloatingActionButton(
//                            onClick = { selectedTab = 1 },
//                            modifier = Modifier.size(56.dp),
//                            containerColor = if (selectedTab == 1) Color(0xFFF0F8FF) else Color.White
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.Lock,
//                                contentDescription = "Use Pairing Code",
//                                tint = Color(0xFF0d6efd) // Icon color
//                            )
//                        }
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Display content based on selected tab
//                when (selectedTab) {
//                    0 -> {
//                        QRCodeScannerView(onScanResult = { qrCodeContents: String ->
//                            verifyQrCode(qrCodeContents, onSuccess = { eventDocument ->
//                                val eventId = eventDocument.id
//                                onJoinSuccess(eventId)
//                                navController.navigate("photoCapture/$eventId")
//                            }, onError = { error ->
//                                errorMessage = error
//                            })
//                        })
//                    }
//                    1 -> {
//                        PairingCodeScreen(onJoinSuccess = { pairingCode ->
//                            verifyPairingCode(pairingCode, onSuccess = { eventDocument ->
//                                val eventId = eventDocument.id
//                                onJoinSuccess(eventId)
//                                navController.navigate("photoCapture/$eventId")
//                            }, onError = { error ->
//                                errorMessage = error
//                            })
//                        }, navController = navController)
//                    }
//                }
//
//                // Display error message if any
//                if (errorMessage.isNotEmpty()) {
//                    Text(errorMessage, color = Color.Red)
//                }
//            }
//        }
//    )
//}



//@Composable
//fun QRCodeScannerView(onScanResult: (String) -> Unit) {
//    val context = LocalContext.current
//    val lifecycleOwner = LocalLifecycleOwner.current
//    val previewView = remember { PreviewView(context) }
//    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
//
//    Box(
//        modifier = Modifier
//            .size(300.dp)
//            .padding(16.dp)
//            .background(MaterialTheme.colorScheme.background)
//            .clip(RoundedCornerShape(16.dp)) // Rounded corners for the scanner view
//    ) {
//        AndroidView(
//            factory = { previewView },
//            modifier = Modifier.fillMaxSize()
//        )
//
//        LaunchedEffect(cameraProviderFuture) {
//            try {
//                val cameraProvider = cameraProviderFuture.get() as ProcessCameraProvider
//                bindCameraUseCases(cameraProvider, previewView, lifecycleOwner, onScanResult)
//            } catch (e: Exception) {
//                Log.e("QRCodeScanner", "Camera initialization failed", e)
//            }
//        }
//    }
//}

fun verifyPairingCode(pairingCode: String, onSuccess: (DocumentSnapshot) -> Unit, onError: (String) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    Log.d("VerifyPairingCode", "Starting verification for pairing code: $pairingCode")

    if (pairingCode.isBlank()) {
        Log.e("VerifyPairingCode", "Pairing code is blank")
        onError("Pairing code cannot be blank")
        return
    }

    db.collection("events")
        .whereEqualTo("pairingCode", pairingCode)
        .get()
        .addOnSuccessListener { documents ->
            if (documents.isEmpty) {
                Log.e("VerifyPairingCode", "Invalid pairing code: $pairingCode")
                onError("Invalid pairing code")
            } else {
                Log.d("VerifyPairingCode", "Pairing code valid: $pairingCode")
                val eventDocument = documents.documents[0]
                onSuccess(eventDocument)
            }
        }
        .addOnFailureListener { exception ->
            Log.e("VerifyPairingCode", "Failed to verify pairing code: $pairingCode", exception)
            onError("Failed to verify pairing code")
        }
}
fun verifyQrCode(qrCodeContents: String, onSuccess: (DocumentSnapshot) -> Unit, onError: (String) -> Unit) {
    Log.d("VerifyQrCode", "Starting verification for QR code: $qrCodeContents")

    if (qrCodeContents.isBlank()) {
        Log.e("VerifyQrCode", "QR code content is blank")
        onError("QR code content cannot be blank")
        return
    }

    val cleanedQrCodeContents = cleanQrCodeData(qrCodeContents)

    if (!validatePairingCode(cleanedQrCodeContents)) {
        Log.e("VerifyQrCode", "Invalid QR code format")
        onError("Invalid QR code format")
        return
    }

    val pairingCode = extractPairingCode(cleanedQrCodeContents)
    Log.d("CompareCodes", "QR Code: $pairingCode, Manual Entry: ${cleanQrCodeData(pairingCode)}")

    verifyPairingCode(pairingCode, onSuccess, onError)
}

fun cleanQrCodeData(qrCodeContents: String): String {
    var cleaned = qrCodeContents.trim().replace("\n", "").replace("\r", "")

    // Remove redundant pairingCode: prefix
    while (cleaned.startsWith("pairingCode:pairingCode:")) {
        cleaned = cleaned.removePrefix("pairingCode:")
    }

    return cleaned
}

fun validatePairingCode(result: String): Boolean {
    Log.d("ValidatePairingCode", "Validating content: $result")
    return result.startsWith("pairingCode:")
}

fun extractPairingCode(result: String): String {
    val pairingCode = result.removePrefix("pairingCode:")
    Log.d("ExtractPairingCode", "Extracted Pairing Code: $pairingCode")
    return pairingCode
}
fun compareManualAndQrCode(manualCode: String, qrCodeContents: String) {
    val cleanedQrCodeContents = cleanQrCodeData(qrCodeContents)
    val qrPairingCode = extractPairingCode(cleanedQrCodeContents)
    Log.d("Comparison", "Manual Code: $manualCode, QR Code: $qrPairingCode")
}

@SuppressLint("UnsafeOptInUsageError")
private fun bindCameraUseCases(
    cameraProvider: ProcessCameraProvider,
    previewView: PreviewView,
    lifecycleOwner: LifecycleOwner,
    onScanResult: (String) -> Unit
) {
    val preview = Preview.Builder()
        .build()
        .also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

    val barcodeScannerOptions = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .build()

    val barcodeScanner = BarcodeScanning.getClient(barcodeScannerOptions)

    val analysis = ImageAnalysis.Builder()
        .setTargetResolution(Size(1280, 720))
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()
        .also {
            it.setAnalyzer(ContextCompat.getMainExecutor(previewView.context)) { imageProxy ->
                processImageProxy(barcodeScanner, imageProxy, onScanResult)
            }
        }

    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    try {
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, analysis)
    } catch (exc: Exception) {
        Log.e("QRCodeScanner", "Use case binding failed", exc)
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
private fun processImageProxy(
    barcodeScanner: BarcodeScanner,
    imageProxy: ImageProxy,
    onScanResult: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                barcodes.forEach { barcode ->
                    barcode.rawValue?.let { value ->
                        Log.d("QRCodeScanner", "Full QR code content: $value")
                        val cleanedValue = cleanQrCodeData(value)
                        Log.d("QRCodeScanner", "Cleaned QR code content: $cleanedValue")
                        onScanResult(cleanedValue)
                        return@forEach
                    }
                }
            }
            .addOnFailureListener {
                Log.e("QRCodeScanner", "Barcode processing failed", it)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}