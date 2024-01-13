package inc.flide.vim8.lib.util

import android.content.res.Resources
import android.util.DisplayMetrics

object ViewUtils {
    fun px2dp(px: Float): Float {
        return px /
            (
                Resources.getSystem().displayMetrics.densityDpi.toFloat() /
                    DisplayMetrics.DENSITY_DEFAULT
                )
    }
}
