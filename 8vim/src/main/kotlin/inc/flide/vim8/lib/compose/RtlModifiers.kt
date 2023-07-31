package inc.flide.vim8.lib.compose

import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

fun Modifier.autoMirrorForRtl() = composed {
    if (LocalLayoutDirection.current == LayoutDirection.Rtl) {
        this.scale(-1f, 1f)
    } else {
        this
    }
}
