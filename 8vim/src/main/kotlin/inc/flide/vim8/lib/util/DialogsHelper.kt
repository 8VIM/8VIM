package inc.flide.vim8.lib.util

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import inc.flide.vim8.R

object DialogsHelper {
    fun showAlert(context: Context, titleRes: Int, message: String) {
        MaterialAlertDialogBuilder(context)
            .setTitle(titleRes)
            .setMessage(message)
            .setPositiveButton(R.string.dialog__confirm__label, null)
            .show()
            .show()
    }
}
