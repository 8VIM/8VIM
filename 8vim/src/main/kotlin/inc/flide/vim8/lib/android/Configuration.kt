package inc.flide.vim8.lib.android

import android.content.res.Configuration

fun Configuration.isOrientationPortrait(): Boolean {
    return this.orientation == Configuration.ORIENTATION_PORTRAIT
}

fun Configuration.isDarkTheme(): Boolean =
    this.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

fun Configuration.isFullScreen(): Boolean = !isOrientationPortrait() && screenHeightDp < 480

fun Configuration.isTablet(): Boolean = if (!isOrientationPortrait()) {
    screenWidthDp > 840
} else {
    screenWidthDp > 600
}
