package inc.flide.vim8.lib.util

import android.content.res.Resources
import android.util.DisplayMetrics

object ViewUtils {
    fun dp2px(dp: Float): Float {
        return dp * (Resources.getSystem().displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun dp2sp(dp:Float):Float {
        return (dp2px(dp)/ Resources.getSystem().displayMetrics.scaledDensity.toFloat())
    }

    fun px2dp(px: Float): Float {
        return px / (Resources.getSystem().displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }


}