package inc.flide.vim8.ime.keyboard

import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import inc.flide.vim8.R
import inc.flide.vim8.appPreferenceModel
import inc.flide.vim8.datastore.model.observeAsState
import inc.flide.vim8.lib.android.isOrientationPortrait
import inc.flide.vim8.lib.util.ViewUtils

val LocalKeyboardHeight = staticCompositionLocalOf { 65.dp }

@Composable
fun ProvideKeyboardHeight(content: @Composable () -> Unit) {
    val resources = LocalContext.current.resources
    val prefs by appPreferenceModel()
    val height by prefs.keyboard.height.observeAsState()
    val keyboardHeight = remember(resources, height) {
        calcInputViewHeight(resources, height)
    }

    CompositionLocalProvider(
        LocalKeyboardHeight provides keyboardHeight
    ) {
        content()
    }
}

private fun calcInputViewHeight(resources: Resources, keyboardHeight: Int): Dp {
    val dm = resources.displayMetrics
    val minBaseSize = when {
        resources.configuration.isOrientationPortrait() -> resources.getFraction(
            R.fraction.inputView_minHeightFraction,
            dm.widthPixels,
            dm.widthPixels
        )

        else -> resources.getFraction(
            R.fraction.inputView_minHeightFraction,
            dm.heightPixels,
            dm.heightPixels
        )
    }
    val maxBaseSize = resources.getFraction(
        R.fraction.inputView_maxHeightFraction,
        dm.heightPixels,
        dm.heightPixels
    )
    val scale = keyboardHeight / 100f
    val height = ((minBaseSize + maxBaseSize) / 2.0f).coerceAtLeast(
        resources.getDimension(R.dimen.inputView_baseHeight)
    ) * scale
    return ViewUtils.px2dp(height).dp
}
