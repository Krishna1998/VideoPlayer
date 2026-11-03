package app.video.player.ui.liquidglass

import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.lerp
import app.video.player.ui.theme.AppMotion
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberBackdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import com.kyant.shapes.Capsule
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.abs

@Composable
fun LiquidToggle(
    selected: () -> Boolean,
    onSelect: (Boolean) -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    enabled: Boolean = true,
    isInteractive: Boolean = true,
) {
    val trackOffColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.70f)
    val trackOnColor = accentColor.copy(alpha = 0.92f)
    val currentSelected = rememberUpdatedState(selected)
    val currentOnSelect = rememberUpdatedState(onSelect)
    val reduceMotion = AppMotion.shouldReduceMotion()

    // Switch track: 52.dp x 32.dp, Thumb: 26.dp x 26.dp, Padding: 3.dp
    // Travel distance = 52 - 26 - (3 * 2) = 20.dp
    val density = LocalDensity.current
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val dragWidth = with(density) { 20f.dp.toPx() }
    val paddingPx = with(density) { 3f.dp.toPx() }
    val animationScope = rememberCoroutineScope()
    var didDrag by remember { mutableStateOf(false) }
    var fraction by remember { mutableFloatStateOf(if (selected()) 1f else 0f) }
    val dampedDragAnimation = remember(animationScope, enabled, isLtr, dragWidth, reduceMotion) {
        DampedDragAnimation(
            animationScope = animationScope,
            initialValue = fraction,
            valueRange = 0f..1f,
            visibilityThreshold = 0.001f,
            initialScale = 1f,
            pressedScale = if (reduceMotion) 1f else 1.05f,
            onDragStarted = {},
            onDragStopped = {
                if (!enabled) return@DampedDragAnimation
                if (didDrag) {
                    val target = if (fraction >= 0.5f) 1f else 0f
                    fraction = target
                    currentOnSelect.value(target == 1f)
                    animateToValue(target)
                    didDrag = false
                } else {
                    val target = if (currentSelected.value()) 0f else 1f
                    fraction = target
                    currentOnSelect.value(target == 1f)
                    animateToValue(target)
                }
            },
            onDrag = { _, dragAmount ->
                if (!enabled) return@DampedDragAnimation
                if (!didDrag) {
                    didDrag = dragAmount.x != 0f
                }
                val delta = dragAmount.x / dragWidth
                val newFraction =
                    if (isLtr) (fraction + delta).fastCoerceIn(0f, 1f)
                    else (fraction - delta).fastCoerceIn(0f, 1f)
                fraction = newFraction
                snapToValue(newFraction)
            }
        )
    }
    LaunchedEffect(Unit) {
        snapshotFlow { currentSelected.value() }
            .collectLatest { isSelected ->
                val target = if (isSelected) 1f else 0f
                if (target != fraction) {
                    fraction = target
                    dampedDragAnimation.animateToValue(target)
                }
            }
    }

    val trackBackdrop = rememberLayerBackdrop()

    Box(
        modifier = modifier.alpha(if (enabled) 1f else 0.38f),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier.size(52.dp, 32.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(
                Modifier
                    .layerBackdrop(trackBackdrop)
                    .clip(Capsule())
                    .drawBehind {
                        val frac = dampedDragAnimation.value
                        val currentTrack = lerp(trackOffColor, trackOnColor, frac)
                        drawRect(currentTrack)

                        // Refined liquid glass rim stroke
                        val strokeWidth = 1.dp.toPx()
                        val strokeColor = lerp(
                            Color.White.copy(alpha = 0.20f),
                            Color.White.copy(alpha = 0.45f),
                            frac,
                        )
                        drawRoundRect(
                            color = strokeColor,
                            topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                            size = Size(size.width - strokeWidth, size.height - strokeWidth),
                            cornerRadius = CornerRadius((size.height - strokeWidth) / 2f),
                            style = Stroke(width = strokeWidth),
                        )
                    }
                    .size(52.dp, 32.dp)
            )

            val toggleShadow = remember {
                Shadow(
                    radius = 3.dp,
                    color = Color.Black.copy(alpha = 0.22f),
                )
            }

            Box(
                Modifier
                    .graphicsLayer {
                        val frac = dampedDragAnimation.value
                        translationX =
                            if (isLtr) lerp(paddingPx, paddingPx + dragWidth, frac)
                            else lerp(-paddingPx, -(paddingPx + dragWidth), frac)
                    }
                    .semantics {
                        role = Role.Switch
                        toggleableState = ToggleableState(selected())
                    }
                    .then(if (isInteractive && enabled) dampedDragAnimation.modifier else Modifier)
                    .drawBackdrop(
                        backdrop = rememberCombinedBackdrop(
                            backdrop,
                            rememberBackdrop(trackBackdrop) { drawBackdrop ->
                                val progress = dampedDragAnimation.pressProgress
                                val scale = lerp(0.85f, 0.95f, progress)
                                scale(scale, scale) {
                                    drawBackdrop()
                                }
                            }
                        ),
                        shape = { Capsule() },
                        effects = {
                            val progress = dampedDragAnimation.pressProgress
                            blur(6f.dp.toPx() * (1f - progress * 0.25f))
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                lens(3.5f.dp.toPx(), 7f.dp.toPx(), chromaticAberration = true)
                            }
                        },
                        highlight = {
                            val progress = dampedDragAnimation.pressProgress
                            Highlight.Ambient.copy(
                                width = Highlight.Ambient.width / 1.5f,
                                blurRadius = Highlight.Ambient.blurRadius / 1.5f,
                                alpha = lerp(0.55f, 0.85f, progress),
                            )
                        },
                        shadow = { toggleShadow },
                        innerShadow = {
                            val progress = dampedDragAnimation.pressProgress
                            InnerShadow(
                                radius = 2.dp,
                                color = Color.White.copy(alpha = lerp(0.35f, 0.55f, progress)),
                            )
                        },
                        layerBlock = {
                            scaleX = dampedDragAnimation.scaleX
                            scaleY = dampedDragAnimation.scaleY
                            val velocity = (dampedDragAnimation.velocity / 300f).fastCoerceIn(-0.1f, 0.1f)
                            scaleX = (scaleX + abs(velocity) * 0.15f).fastCoerceIn(0.95f, 1.1f)
                            scaleY = (scaleY - abs(velocity) * 0.05f).fastCoerceIn(0.95f, 1.05f)
                        },
                        onDrawSurface = {
                            val selection = dampedDragAnimation.value.coerceIn(0f, 1f)
                            val film = lerp(Color.White.copy(alpha = 0.94f), Color.White, selection)
                            drawCapsuleTint(film)
                        }
                    )
                    .size(26.dp, 26.dp)
            )
        }
    }
}
