package inc.flide.vim8.app.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.PreferenceFragmentCompat
import arrow.core.None
import arrow.core.some
import inc.flide.vim8.R
import inc.flide.vim8.appPreferenceModel
import inc.flide.vim8.ime.LayoutLoader
import inc.flide.vim8.ime.layout.AvailableLayouts
import inc.flide.vim8.ime.layout.CustomLayout
import inc.flide.vim8.ime.layout.loadKeyboardData
import inc.flide.vim8.ime.layout.models.KeyboardData
import inc.flide.vim8.ime.layout.models.error.ExceptionWrapperError
import inc.flide.vim8.ime.layout.models.error.LayoutError
import inc.flide.vim8.utils.DialogsHelper.showAlert

abstract class LayoutFileSelector : PreferenceFragmentCompat() {
    protected val prefs by appPreferenceModel()
    protected var availableLayouts: AvailableLayouts? = null
    protected var layoutLoader: LayoutLoader? = null
    private val openContent = registerForActivityResult<Array<String>, Uri>(
        ActivityResultContracts.OpenDocument()
    ) { selectedCustomLayoutFile: Uri? -> callback(selectedCustomLayoutFile) }

    private fun callback(selectedCustomLayoutFile: Uri?) {
        val layoutPrefs = prefs.layout
        val context = context
        if (selectedCustomLayoutFile == null || context == null) {
            return
        }
        context.contentResolver
            .takePersistableUriPermission(
                selectedCustomLayoutFile,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        val layout = CustomLayout(selectedCustomLayoutFile)
        val currentHistory = layoutPrefs.custom.history.get()
        val isInHistory = currentHistory.contains(selectedCustomLayoutFile.toString())
        if (isInHistory) {
            availableLayouts!!.updateKeyboardData(layout)
        } else {
            layout.loadKeyboardData(layoutLoader!!, context)
                .fold({ error: LayoutError ->
                    val title = if (error is ExceptionWrapperError) {
                        R.string.generic_error_text
                    } else {
                        R.string.yaml_error_title
                    }
                    (title to error.message).some()
                }, { keyboardData: KeyboardData ->
                    if (keyboardData.totalLayers == 0) {
                        (R.string.yaml_error_title to "The layout requires at least one layer")
                            .some()
                    } else {
                        None
                    }
                })
                .onSome { (titleId, message) -> showAlert(context, titleId, message) }
                .onNone {
                    prefs.layout.current.set(layout)
                    val history =
                        listOf(selectedCustomLayoutFile.toString()) + currentHistory.toList()

                    layoutPrefs.custom.history.set(LinkedHashSet(history))
                }
        }
    }

    protected fun openFileSelector() {
        openContent.launch(LAYOUT_FILTER)
    }

    companion object {
        private val LAYOUT_FILTER = arrayOf("application/octet-stream")
    }
}
