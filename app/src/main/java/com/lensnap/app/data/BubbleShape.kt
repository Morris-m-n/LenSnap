package com.lensnap.app.data

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

//BUBBLE SHAPE FOR MESSAGES
class BubbleShape(
    private val isSender: Boolean,
    private val cornerRadius: Dp
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val cornerRadiusPx = with(density) { cornerRadius.toPx() }
        val tailSize = with(density) { 10.dp.toPx() }

        val path = Path().apply {
            if (isSender) {
                // Sender bubble (tail on the right)
                moveTo(0f, cornerRadiusPx)
                arcTo(Rect(0f, 0f, cornerRadiusPx * 2, cornerRadiusPx * 2), 180f, 90f, false)
                lineTo(size.width - cornerRadiusPx - tailSize, 0f) // Tail start
                lineTo(size.width - tailSize, tailSize)            // Tail end
                lineTo(size.width - cornerRadiusPx, cornerRadiusPx) // Top right
                arcTo(
                    Rect(
                        size.width - cornerRadiusPx * 2,
                        0f,
                        size.width,
                        cornerRadiusPx * 2
                    ),
                    270f, 90f, false
                )
                lineTo(size.width, size.height - cornerRadiusPx)
                arcTo(
                    Rect(
                        size.width - cornerRadiusPx * 2,
                        size.height - cornerRadiusPx * 2,
                        size.width,
                        size.height
                    ),
                    0f, 90f, false
                )
                lineTo(cornerRadiusPx, size.height)
                arcTo(
                    Rect(0f, size.height - cornerRadiusPx * 2, cornerRadiusPx * 2, size.height),
                    90f, 90f, false
                )
                close()
            } else {
                // Receiver bubble (tail on the left)
                moveTo(tailSize, cornerRadiusPx)
                lineTo(cornerRadiusPx + tailSize, 0f)
                arcTo(
                    Rect(
                        tailSize,
                        0f,
                        cornerRadiusPx * 2 + tailSize,
                        cornerRadiusPx * 2
                    ),
                    180f, 90f, false
                )
                lineTo(size.width - cornerRadiusPx, 0f)
                arcTo(
                    Rect(
                        size.width - cornerRadiusPx * 2,
                        0f,
                        size.width,
                        cornerRadiusPx * 2
                    ),
                    270f, 90f, false
                )
                lineTo(size.width, size.height - cornerRadiusPx)
                arcTo(
                    Rect(
                        size.width - cornerRadiusPx * 2,
                        size.height - cornerRadiusPx * 2,
                        size.width,
                        size.height
                    ),
                    0f, 90f, false
                )
                lineTo(cornerRadiusPx + tailSize, size.height)
                arcTo(
                    Rect(
                        tailSize,
                        size.height - cornerRadiusPx * 2,
                        cornerRadiusPx * 2 + tailSize,
                        size.height
                    ),
                    90f, 90f, false
                )
                close()
            }
        }

        return Outline.Generic(path)
    }
}
