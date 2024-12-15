//import android.graphics.Bitmap
//import android.graphics.Color
//import com.google.firebase.storage.FirebaseStorage
//import com.google.zxing.BarcodeFormat
//import com.google.zxing.WriterException
//import com.google.zxing.qrcode.QRCodeWriter
//import kotlinx.coroutines.tasks.await
//import java.io.ByteArrayOutputStream
//
////package com.lensnap.app.ui.theme.screens.joinEventScreen
////
////import android.app.Activity
////import android.content.Context
////import android.content.Intent
////import androidx.activity.compose.rememberLauncherForActivityResult
////import androidx.activity.result.ActivityResult
////import androidx.activity.result.contract.ActivityResultContracts
////import androidx.compose.foundation.layout.*
////import androidx.compose.material3.*
////import androidx.compose.runtime.*
////import androidx.compose.ui.Alignment
////import androidx.compose.ui.Modifier
////import androidx.compose.ui.graphics.Color
////import androidx.compose.ui.platform.LocalContext
////import androidx.compose.ui.unit.dp
////import com.google.firebase.firestore.FirebaseFirestore
////import com.google.zxing.integration.android.IntentIntegrator
////import com.google.zxing.integration.android.IntentResult
////
////@OptIn(ExperimentalMaterial3Api::class)
////@Composable
////fun QrCodeScannerScreen(onJoinSuccess: () -> Unit) {
////    val context = LocalContext.current
////    var errorMessage by remember { mutableStateOf("") }
////
////    val qrCodeLauncher = rememberLauncherForActivityResult(
////        contract = ActivityResultContracts.StartActivityForResult()
////    ) { result ->
////        val intentResult: IntentResult? = IntentIntegrator.parseActivityResult(result.resultCode, result.data)
////        if (result.resultCode == Activity.RESULT_OK && intentResult != null) {
////            val qrCodeContents = intentResult.contents
////            if (qrCodeContents != null) {
////                // Verify QR code with Firebase Firestore
////                verifyQrCode(qrCodeContents, onSuccess = {
////                    onJoinSuccess()
////                }, onError = {
////                    errorMessage = it
////                })
////            } else {
////                errorMessage = "QR Code not found"
////            }
////        } else {
////            errorMessage = "Cancelled"
////        }
////    }
////
////    Scaffold(
////        topBar = {
////            TopAppBar(
////                title = { Text("Scan QR Code") }
////            )
////        },
////        content = { paddingValues ->
////            Column(
////                horizontalAlignment = Alignment.CenterHorizontally,
////                verticalArrangement = Arrangement.Center,
////                modifier = Modifier
////                    .fillMaxSize()
////                    .padding(paddingValues)
////                    .padding(16.dp)
////            ) {
////                Button(onClick = {
////                    // Launch QR Code scanner
////                    launchQrCodeScanner(context, qrCodeLauncher)
////                }) {
////                    Text("Scan QR Code")
////                }
////                if (errorMessage.isNotEmpty()) {
////                    Text(errorMessage, color = Color.Red)
////                }
////            }
////        }
////    )
////}
////
////fun launchQrCodeScanner(context: Context, qrCodeLauncher: androidx.activity.result.ActivityResultLauncher<Intent>) {
////    val activity = context as? Activity
////    activity?.let {
////        val intentIntegrator = IntentIntegrator(it)
////        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
////        qrCodeLauncher.launch(intentIntegrator.createScanIntent())
////    }
////}
////
////fun verifyQrCode(qrCode: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
////    val db = FirebaseFirestore.getInstance()
////    db.collection("events")
////        .whereEqualTo("qrCode", qrCode)
////        .get()
////        .addOnSuccessListener { documents ->
////            if (documents.isEmpty) {
////                onError("Invalid QR code")
////            } else {
////                onSuccess()
////            }
////        }
////        .addOnFailureListener { exception ->
////            onError("Failed to verify QR code")
////        }
////}
//private fun generateQrCode(content: String): Bitmap {
//    val size = 250 // Dimensions for the QR code
//    val qrCodeWriter = QRCodeWriter()
//    return try {
//        val bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, size, size)
//        val width = bitMatrix.width
//        val height = bitMatrix.height
//        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
//        for (x in 0 until width) {
//            for (y in 0 until height) {
//                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
//            }
//        }
//        bitmap
//    } catch (e: WriterException) {
//        e.printStackTrace()
//        Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
//    }
//}
//
//private suspend fun uploadQrCodeToFirebase(bitmap: Bitmap, fileName: String): String {
//    val baos = ByteArrayOutputStream()
//    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
//    val data = baos.toByteArray()
//    val storageRef = FirebaseStorage.getInstance().reference.child("qrcodes/$fileName.png")
//    storageRef.putBytes(data).await()
//    return storageRef.downloadUrl.await().toString()
//}
