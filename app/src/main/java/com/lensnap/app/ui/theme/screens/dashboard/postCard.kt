package com.lensnap.app.ui.theme.screens.dashboard

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.rememberImagePainter
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.lensnap.app.models.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostCard(post: Post) {
    val primaryBlueColor = Color(0xFF0D6EFD)
    val context = LocalContext.current

    var mediaBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var blurredBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var imageWidth by remember { mutableStateOf(1) }
    var imageHeight by remember { mutableStateOf(1) }

    LaunchedEffect(post.mediaUrl) {
        mediaBitmap = getBitmapFromUrl(post.mediaUrl, context)
        mediaBitmap?.let {
            blurredBitmap = blurBitmap(it, 25f, context)
            imageWidth = it.width
            imageHeight = it.height
        }
    }

    val aspectRatio = if (imageHeight != 0) imageWidth.toFloat() / imageHeight else 1f
    val maxHeight = 400.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Transparent)
            .drawBehind {
                blurredBitmap?.let {
                    val imageBitmap = it.asImageBitmap()
                    val srcSize = IntSize(imageBitmap.width, imageBitmap.height)
                    val dstSize = IntSize(size.width.toInt(), size.height.toInt())
                    val srcRatio = srcSize.width.toFloat() / srcSize.height
                    val dstRatio = dstSize.width.toFloat() / dstSize.height

                    val srcRect = if (srcRatio > dstRatio) {
                        val srcWidth = (srcSize.height * dstRatio).toInt()
                        Rect(
                            (srcSize.width - srcWidth) / 2,
                            0,
                            (srcSize.width + srcWidth) / 2,
                            srcSize.height
                        )
                    } else {
                        val srcHeight = (srcSize.width / dstRatio).toInt()
                        Rect(
                            0,
                            (srcSize.height - srcHeight) / 2,
                            srcSize.width,
                            (srcSize.height + srcHeight) / 2
                        )
                    }

                    val dstRect = Rect(0, 0, dstSize.width, dstSize.height)
                    drawContext.canvas.nativeCanvas.drawBitmap(
                        it,
                        srcRect,
                        dstRect,
                        Paint().apply { isAntiAlias = true }
                    )
                }
            }
    ) {
        Image(
            painter = rememberImagePainter(post.mediaUrl),
            contentDescription = "Post Media",
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxHeight)
                .aspectRatio(aspectRatio, matchHeightConstraintsFirst = true)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .align(Alignment.TopStart)
                .zIndex(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Photo
            Image(
                painter = rememberImagePainter(data = post.profilePhotoUrl),
                contentDescription = "Profile Photo",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            // Username
            Text(
                text = post.username,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .align(Alignment.BottomStart)
                .zIndex(1f),
            horizontalArrangement = Arrangement.Start
        ) {
            // Like Icon
            IconButton(onClick = { /* Handle Like */ }) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Like",
                    tint = primaryBlueColor
                )
            }
            // Comment Icon
            IconButton(onClick = { /* Handle Comment */ }) {
                Icon(
                    imageVector = Icons.Default.Comment,
                    contentDescription = "Comment",
                    tint = primaryBlueColor
                )
            }
            // Share Icon
            IconButton(onClick = { /* Handle Share */ }) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = primaryBlueColor
                )
            }
        }

        // Caption
        Text(
            text = post.caption,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.BottomCenter)
                .zIndex(1f)
        )
    }
}

private suspend fun getBitmapFromUrl(url: String, context: Context): Bitmap? {
    return withContext(Dispatchers.IO) {
        val request = ImageRequest.Builder(context)
            .data(url)
            .allowHardware(false) // Important for RenderScript
            .build()

        val result = (request.context.imageLoader.execute(request) as? SuccessResult)?.drawable
        (result as? BitmapDrawable)?.bitmap
    }
}

private fun blurBitmap(bitmap: Bitmap, radius: Float, context: Context): Bitmap {
    val outputBitmap = Bitmap.createBitmap(bitmap)
    val renderScript = RenderScript.create(context)
    val input = Allocation.createFromBitmap(renderScript, bitmap)
    val output = Allocation.createFromBitmap(renderScript, outputBitmap)
    val scriptIntrinsicBlur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
    scriptIntrinsicBlur.setRadius(radius)
    scriptIntrinsicBlur.setInput(input)
    scriptIntrinsicBlur.forEach(output)
    output.copyTo(outputBitmap)
    renderScript.destroy()
    return outputBitmap
}
