package inc.flide.vim8.lib.android

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect

inline val Rect.offset: Offset
    get() = Offset(left, top)
