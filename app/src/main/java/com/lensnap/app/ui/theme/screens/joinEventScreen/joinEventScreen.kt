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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import androidx.compose.material3.MaterialTheme
import com.google.mlkit.vision.barcode.BarcodeScanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinEventScreen(navController: NavHostController, onJoinSuccess: (String) -> Unit) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    var errorMessage by remember { mutableStateOf("") }

    Log.d("JoinEventScreen", "Screen initialized")

    // Permission launcher for camera access
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d("JoinEventScreen", "Camera permission granted: $isGranted")
        if (!isGranted) {
            Toast.makeText(context, "Camera permission is required to scan QR codes", Toast.LENGTH_LONG).show()
        }
    }

    // Request camera permission on start
    LaunchedEffect(Unit) {
        Log.d("JoinEventScreen", "Requesting camera permission")
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Join Event") }
            )
        },
        content = { paddingValues ->
            Log.d("JoinEventScreen", "Building UI components")
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Scan QR Code") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Use Pairing Code") }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                when (selectedTab) {
                    0 -> {
                        Log.d("JoinEventScreen", "QR Code Scanner selected")
                        QRCodeScannerView(onScanResult = { qrCodeContents: String ->
                            Log.d("JoinEventScreen", "QR code scanned: $qrCodeContents")
                            verifyQrCode(qrCodeContents, onSuccess = { eventDocument: DocumentSnapshot ->
                                val eventId = eventDocument.id
                                Log.d("JoinEventScreen", "QR code verified successfully with event ID: $eventId")
                                onJoinSuccess(eventId)
                                navController.navigate("photoCapture/$eventId")
                            }, onError = { error: String ->
                                Log.e("JoinEventScreen", "QR code verification failed: $error")
                                errorMessage = error
                            })
                        })
                    }
                    1 -> {
                        Log.d("JoinEventScreen", "Pairing Code selected")
                        PairingCodeScreen(onJoinSuccess = { pairingCode: String ->
                            Log.d("JoinEventScreen", "Pairing code entered: $pairingCode")
                            verifyPairingCode(pairingCode, onSuccess = { eventDocument: DocumentSnapshot ->
                                val eventId = eventDocument.id
                                Log.d("JoinEventScreen", "Pairing code verified successfully with event ID: $eventId")
                                onJoinSuccess(eventId)
                                navController.navigate("photoCapture/$eventId")
                            }, onError = { error: String ->
                                Log.e("JoinEventScreen", "Pairing code verification failed: $error")
                                errorMessage = error
                            })
                        }, navController = navController)
                    }
                }

                if (errorMessage.isNotEmpty()) {
                    Text(errorMessage, color = Color.Red)
                }
            }
        }
    )
}

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
//fun verifyPairingCode(pairingCode: String, onSuccess: (DocumentSnapshot) -> Unit, onError: (String) -> Unit) {
//    val db = FirebaseFirestore.getInstance()
//    Log.d("VerifyPairingCode", "Starting verification for pairing code: $pairingCode")
//
//    if (pairingCode.isBlank()) {
//        Log.e("VerifyPairingCode", "Pairing code is blank")
//        onError("Pairing code cannot be blank")
//        return
//    }
//
//    db.collection("events")
//        .whereEqualTo("pairingCode", pairingCode)
//        .get()
//        .addOnSuccessListener { documents ->
//            if (documents.isEmpty) {
//                Log.e("VerifyPairingCode", "Invalid pairing code: $pairingCode")
//                onError("Invalid pairing code")
//            } else {
//                Log.d("VerifyPairingCode", "Pairing code valid: $pairingCode")
//                val eventDocument = documents.documents[0]
//                onSuccess(eventDocument)
//            }
//        }
//        .addOnFailureListener { exception ->
//            Log.e("VerifyPairingCode", "Failed to verify pairing code: $pairingCode", exception)
//            onError("Failed to verify pairing code")
//        }
//}
fun cleanQrCodeData(qrCodeContents: String): String {
    var cleaned = qrCodeContents.trim().replace("\n", "").replace("\r", "")

    // Remove redundant pairingCode: prefix
    while (cleaned.startsWith("pairingCode:pairingCode:")) {
        cleaned = cleaned.removePrefix("pairingCode:")
    }

    return cleaned
}

//fun verifyQrCode(qrCodeContents: String, onSuccess: (DocumentSnapshot) -> Unit, onError: (String) -> Unit) {
//    Log.d("VerifyQrCode", "Starting verification for QR code: $qrCodeContents")
//
//    if (qrCodeContents.isBlank()) {
//        Log.e("VerifyQrCode", "QR code content is blank")
//        onError("QR code content cannot be blank")
//        return
//    }
//
//    val cleanedQrCodeContents = cleanQrCodeData(qrCodeContents)
//
//    if (!validatePairingCode(cleanedQrCodeContents)) {
//        Log.e("VerifyQrCode", "Invalid QR code format")
//        onError("Invalid QR code format")
//        return
//    }
//
//    val pairingCode = extractPairingCode(cleanedQrCodeContents)
//
//    verifyPairingCode(pairingCode, onSuccess, onError)
//}

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

@Composable
fun QRCodeScannerView(onScanResult: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    Box(
        modifier = Modifier
            .size(300.dp)
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background)
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