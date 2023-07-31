package inc.flide.vim8.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import inc.flide.vim8.lib.android.AndroidSettings
import kotlin.reflect.KClass

private const val DELIMITER = ':'
private const val IME_SERVICE_CLASS_NAME = "inc.flide.vim8.MainInputMethodService"

object InputMethodUtils {
    fun is8VimEnabled(context: Context): Boolean {
        val enabledImeList =
            AndroidSettings.Secure.getString(context, Settings.Secure.ENABLED_INPUT_METHODS)
        return enabledImeList != null && parseIs8VimEnabled(context, enabledImeList)
    }

    fun is8VimSelected(context: Context): Boolean {
        val selectedIme =
            AndroidSettings.Secure.getString(context, Settings.Secure.DEFAULT_INPUT_METHOD)
        return selectedIme != null && parseIs8VimSelected(context, selectedIme)
    }

    @Composable
    fun observeIs8VimEnabled(
        context: Context = LocalContext.current.applicationContext,
        foregroundOnly: Boolean = false,
    ) = AndroidSettings.Secure.observeAsState(
        key = Settings.Secure.ENABLED_INPUT_METHODS,
        foregroundOnly = foregroundOnly,
        transform = { parseIs8VimEnabled(context, it.toString()) },
    )

    @Composable
    fun observeIs8VimSelected(
        context: Context = LocalContext.current.applicationContext,
        foregroundOnly: Boolean = false,
    ) = AndroidSettings.Secure.observeAsState(
        key = Settings.Secure.DEFAULT_INPUT_METHOD,
        foregroundOnly = foregroundOnly,
        transform = { parseIs8VimSelected(context, it.toString()) },
    )

    fun parseIs8VimEnabled(context: Context, activeImeIds: String): Boolean {
        return activeImeIds.split(DELIMITER).map { componentStr ->
            ComponentName.unflattenFromString(componentStr)
        }.any { it?.packageName == context.packageName && it?.className == IME_SERVICE_CLASS_NAME }
    }

    fun parseIs8VimSelected(context: Context, selectedImeId: String): Boolean {
        val component = ComponentName.unflattenFromString(selectedImeId)
        return component?.packageName == context.packageName && component?.className == IME_SERVICE_CLASS_NAME
    }

    fun showImeEnablerActivity(context: Context) {
        val intent = Intent()
        intent.action = Settings.ACTION_INPUT_METHOD_SETTINGS
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        context.startActivity(intent)
    }

    fun showImePicker(context: Context): Boolean {
        val imm = context.systemServiceOrNull(InputMethodManager::class)
        return if (imm != null) {
            imm.showInputMethodPicker()
            true
        } else {
            false
        }
    }
}

@Throws(NullPointerException::class, ClassCastException::class)
fun <T : Any> Context.systemService(kClass: KClass<T>): T {
    val serviceName = this.getSystemServiceName(kClass.java)!!
    @Suppress("UNCHECKED_CAST")
    return this.getSystemService(serviceName) as T
}
fun <T : Any> Context.systemServiceOrNull(kClass: KClass<T>): T? {
    return try {
        this.systemService(kClass)
    } catch (e: Exception) {
        null
    }
}
