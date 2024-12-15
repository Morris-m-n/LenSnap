//import android.graphics.Bitmap
//import android.graphics.Color
//import com.google.zxing.BarcodeFormat
//import com.google.zxing.WriterException
//import com.google.zxing.qrcode.QRCodeWriter
//import com.google.firebase.storage.FirebaseStorage
//import kotlinx.coroutines.tasks.await
//import java.io.ByteArrayOutputStream
//
//// QR Code Generation
//fun generateQrCode(content: String): Bitmap {
//    val size = 512 // Dimensions for the QR code
//    val qrCodeWriter = QRCodeWriter()
//    try {
//        val bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, size, size)
//        val width = bitMatrix.width
//        val height = bitMatrix.height
//        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
//        for (x in 0 until width) {
//            for (y in 0 until height) {
//                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
//            }
//        }
//        return bitmap
//    } catch (e: WriterException) {
//        e.printStackTrace()
//        // Return an empty bitmap in case of an error
//        return Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
//    }
//}
//
//// Upload QR Code to Firebase Storage
//suspend fun uploadQrCodeToFirebase(bitmap: Bitmap, fileName: String): String {
//    val baos = ByteArrayOutputStream()
//    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
//    val data = baos.toByteArray()
//    val storageRef = FirebaseStorage.getInstance().reference.child("qrcodes/$fileName.png")
//    storageRef.putBytes(data).await()
//    return storageRef.downloadUrl.await().toString()
//}
