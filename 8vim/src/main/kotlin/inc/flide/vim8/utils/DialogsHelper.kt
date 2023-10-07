package inc.flide.vim8.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import inc.flide.vim8.R
import java.util.function.IntConsumer

object DialogsHelper {
    fun createItemsChoice(
        context: Context,
        titleRes: Int,
        items: Collection<String>,
        selectedIndex: Int,
        callback: IntConsumer
    ): AlertDialog {
        var currentIndex = selectedIndex
        return MaterialAlertDialogBuilder(context)
            .setTitle(titleRes)
            .setPositiveButton(R.string.generic_okay_text) { _, _ ->
                if (currentIndex != -1 && currentIndex != selectedIndex) {
                    callback.accept(currentIndex)
                }
            }
            .setNegativeButton(R.string.generic_cancel_text, null)
            .setSingleChoiceItems(items.toTypedArray(), selectedIndex) { _, which ->
                currentIndex = which
            }
            .show()
    }

    fun showAlert(context: Context, titleRes: Int, message: String) {
        MaterialAlertDialogBuilder(context)
            .setTitle(titleRes)
            .setMessage(message)
            .setPositiveButton(R.string.generic_okay_text, null)
            .show()
            .show()
    }
}
