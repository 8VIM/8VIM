package inc.flide.vim8.lib.android

import android.os.Build

object AndroidVersion {
    inline val ATLEAST_API26_O get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    inline val ATLEAST_API28_P get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
    inline val ATLEAST_API31_S get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
}
