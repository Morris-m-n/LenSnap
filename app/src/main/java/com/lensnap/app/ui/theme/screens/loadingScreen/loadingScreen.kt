import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun LoadingTextAnimation() {
    val infiniteTransition = rememberInfiniteTransition()
    val dotOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(3) { index ->
                val delay = index * 200 // Stagger the animations
                AnimatedDot(dotOffset, delay)
            }
        }
    }
}

@Composable
fun AnimatedDot(dotOffset: Float, delay: Int) {
    val offset by animateFloatAsState(
        targetValue = dotOffset,
        animationSpec = tween(durationMillis = 1000, delayMillis = delay, easing = FastOutSlowInEasing)
    )

    Canvas(modifier = Modifier.size(16.dp)) {
        drawCircle(
            color = Color(0xFF0D6EFD),
            radius = 8.dp.toPx(),
            center = center.copy(y = center.y + offset * 30.dp.toPx())
        )
    }
}
