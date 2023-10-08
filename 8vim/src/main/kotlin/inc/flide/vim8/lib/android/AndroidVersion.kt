package inc.flide.vim8.lib.android

import android.os.Build

object AndroidVersion {
    inline val ATLEAST_API31_S get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    inline val ATLEAST_API28_P get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
    val ATLEAST_API29_Q get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    inline val ATLEAST_API26_O get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
}
