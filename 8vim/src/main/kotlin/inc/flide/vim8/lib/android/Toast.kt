package inc.flide.vim8.lib.android

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import inc.flide.vim8.lib.kotlin.CurlyArg

fun Context.showToast(@StringRes id: Int, vararg args: CurlyArg) {
    showToast(stringRes(id, *args))
}

fun Context.showToast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_LONG).also { it.show() }
}
