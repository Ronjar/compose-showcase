package com.joco.showcase.sequence

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import com.joco.showcaseview.AnimationDuration
import com.joco.showcaseview.BackgroundAlpha
import com.joco.showcaseview.ShowcaseAlignment
import com.joco.showcaseview.ShowcaseDisplayState
import com.joco.showcaseview.ShowcasePosition
import com.joco.showcaseview.ShowcaseView
import com.joco.showcaseview.highlight.ShowcaseHighlight

/**
 * CompositionLocal containing the current [SequenceShowcaseState], or null if not within a [SequenceShowcase].
 */
val LocalSequenceShowcaseState = staticCompositionLocalOf<SequenceShowcaseState?> { null }

@Composable
fun SequenceShowcase(
    state: SequenceShowcaseState = rememberSequenceShowcaseState(),
    overlayModifier: Modifier = Modifier,
    defaultHighlight: ShowcaseHighlight = ShowcaseHighlight.Rectangular(),
    defaultDuration: AnimationDuration = AnimationDuration.Default,
    defaultBackgroundAlpha: BackgroundAlpha = BackgroundAlpha.Normal,
    content: @Composable SequenceShowcaseScope.() -> Unit
) {
    val scope = remember(state, defaultHighlight, defaultDuration, defaultBackgroundAlpha) {
        SequenceShowcaseScope(
            state = state,
            defaultHighlight = defaultHighlight,
            defaultDuration = defaultDuration,
            defaultBackgroundAlpha = defaultBackgroundAlpha
        )
    }

    CompositionLocalProvider(LocalSequenceShowcaseState provides state) {
        Box(modifier = Modifier.fillMaxWidth()) {
            scope.content()

            state.currentTarget?.let { target ->
                if (target.coordinates.isAttached && target.coordinates.size.width > 0 && target.coordinates.size.height > 0) {
                    ShowcaseView(
                        visible = state.showCaseVisible,
                        targetCoordinates = target.coordinates,
                        overlayModifier = overlayModifier,
                        position = target.position,
                        alignment = target.alignment,
                        highlight = target.highlight,
                        animationDuration = target.duration,
                        backgroundAlpha = target.backgroundAlpha,
                        onDisplayStateChanged = { displayState ->
                            when(displayState) {
                                ShowcaseDisplayState.Appeared -> {
                                    state.onShowcaseViewAppear()
                                }
                                ShowcaseDisplayState.Disappeared -> {
                                    state.onShowcaseViewDisappear()
                                }
                            }
                        }
                    ) { targetRect ->
                        target.content(targetRect)
                    }
                }
            }
        }
    }
}

/**
 * Provides a function to create a Modifier that marks a Composable as a target for the SequenceShowcase.
 */
class SequenceShowcaseScope(
    private val state: SequenceShowcaseState,
    internal val defaultHighlight: ShowcaseHighlight = ShowcaseHighlight.Rectangular(),
    internal val defaultDuration: AnimationDuration = AnimationDuration.Default,
    internal val defaultBackgroundAlpha: BackgroundAlpha = BackgroundAlpha.Normal,
) {
    /**
     * Creates a Modifier that marks a Composable as a target for the SequenceShowcase.
     *
     * @param index The index of the target in the sequence.
     * @param position The position of the dialog relative to the target element.
     * @param alignment The alignment of the dialog relative to the target element.
     * @param highlight The highlight around the target element. Defaults to [defaultHighlight].
     * @param animationDuration The duration of the fade enter and exit animation. Defaults to [defaultDuration].
     * @param backgroundAlpha The alpha value of the background overlay. Defaults to [defaultBackgroundAlpha].
     * @param content The content of the dialog.
     */
    fun Modifier.sequenceShowcaseTarget(
        index: Int,
        position: ShowcasePosition = ShowcasePosition.Default,
        alignment: ShowcaseAlignment = ShowcaseAlignment.Default,
        highlight: ShowcaseHighlight = defaultHighlight,
        animationDuration: AnimationDuration = defaultDuration,
        backgroundAlpha: BackgroundAlpha = defaultBackgroundAlpha,
        content: @Composable (Rect) -> Unit,
    ): Modifier = onGloballyPositioned { coordinates ->
        if (coordinates.isAttached && coordinates.size.width > 0 && coordinates.size.height > 0) {
            state.targets[index] = SequenceShowcaseTarget(
                index = index,
                coordinates = coordinates,
                position = position,
                alignment = alignment,
                highlight = highlight,
                duration = animationDuration,
                backgroundAlpha = backgroundAlpha,
                content = content
            )
        }
    }
}