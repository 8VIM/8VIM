package inc.flide.vim8.lib.geometry

import android.content.res.Resources
import android.util.DisplayMetrics

fun Float.px2dp(): Float = this / (
    Resources.getSystem().displayMetrics.densityDpi.toFloat() /
        DisplayMetrics.DENSITY_DEFAULT
    )
