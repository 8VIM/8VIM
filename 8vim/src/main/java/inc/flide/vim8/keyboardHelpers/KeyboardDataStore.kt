package inc.flide.vim8.keyboardHelpers

import android.content.Context
import android.content.res.Resources
import android.net.Uri
import inc.flide.vim8.structures.KeyboardData

object KeyboardDataStore {
    lateinit var keyboardData: KeyboardData
    fun rebuildKeyboardData(resource: Resources, context: Context) {
        keyboardData = InputMethodServiceHelper.initializeKeyboardActionMap(resource, context)
    }

    fun rebuildKeyboardData(resource: Resources, context: Context, customLayoutUri: Uri) {
        keyboardData = InputMethodServiceHelper.initializeKeyboardActionMapForCustomLayout(resource, context, customLayoutUri)
    }
}