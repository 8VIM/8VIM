package inc.flide.vim8.lib.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import arrow.core.Option
import arrow.core.firstOrNone
import inc.flide.vim8.appPreferenceModel
import inc.flide.vim8.lib.android.AndroidSettings
import inc.flide.vim8.lib.android.AndroidVersion.ATLEAST_API28_P
import inc.flide.vim8.lib.android.systemServiceOrNull

private const val IME_SERVICE_CLASS_NAME = "inc.flide.vim8.Vim8ImeService"

object InputMethodUtils {
    @Composable
    private fun rememberLifecycleEvent(
        lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    ): Lifecycle.Event {
        var state by remember { mutableStateOf(Lifecycle.Event.ON_ANY) }
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                state = event
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
        return state
    }

    @Composable
    fun observeIs8VimEnabled(
        context: Context = LocalContext.current.applicationContext
    ): State<Boolean> {
        val state = remember { mutableStateOf(parseIs8VimEnabled(context)) }
        val lifecycleEvent = rememberLifecycleEvent()
        LaunchedEffect(lifecycleEvent) {
            if (lifecycleEvent == Lifecycle.Event.ON_RESUME) {
                state.value = parseIs8VimEnabled(context)
            }
        }
        return state
    }

    @Composable
    fun observeIs8VimSelected(
        context: Context = LocalContext.current.applicationContext,
        foregroundOnly: Boolean = false
    ) = AndroidSettings.Secure.observeAsState(
        key = Settings.Secure.DEFAULT_INPUT_METHOD,
        foregroundOnly = foregroundOnly,
        transform = { parseIs8VimSelected(context, it.toString()) }
    )

    @Suppress("DEPRECATION")
    fun switchToEmoticonKeyboard(ime: InputMethodService) {
        selectedEmoticonKeyboard(ime.applicationContext)
            .onSome { ime.switchInputMethod(it) }
            .onNone {
                if (ATLEAST_API28_P) {
                    ime.switchToPreviousInputMethod()
                } else {
                    ime.applicationContext.systemServiceOrNull(InputMethodManager::class)?.let {
                        val tokenIBinder = ime.window.window!!.attributes.token
                        it.switchToLastInputMethod(tokenIBinder)
                    }
                }
            }
    }

    private fun selectedEmoticonKeyboard(context: Context): Option<String> {
        val prefs by appPreferenceModel()
        val emoticonKeyboardId = prefs.keyboard.emoticonKeyboard.get()
        return Option.fromNullable(context.systemServiceOrNull(InputMethodManager::class))
            .flatMap { imm ->
                imm.enabledInputMethodList.firstOrNone { it.id == emoticonKeyboardId }.map { it.id }
            }
    }

    fun parseIs8VimEnabled(context: Context): Boolean {
        return Option
            .fromNullable(context.systemServiceOrNull(InputMethodManager::class))
            .isSome {
                it.enabledInputMethodList.firstOrNone { inputMethodInfo ->
                    inputMethodInfo.component.packageName == context.packageName &&
                            inputMethodInfo.component.className == IME_SERVICE_CLASS_NAME
                }.isSome()
            }
    }

    private fun parseIs8VimSelected(context: Context, selectedImeId: String): Boolean {
        val component = ComponentName.unflattenFromString(selectedImeId)
        return component?.packageName == context.packageName &&
                component?.className == IME_SERVICE_CLASS_NAME
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

    fun listOtherKeyboard(context: Context): Map<String, String> {
        return context.systemServiceOrNull(InputMethodManager::class)?.let {
            it.enabledInputMethodList
                .fold(emptyMap()) { acc, inputMethodInfo ->
                    if (inputMethodInfo.component.packageName == context.packageName &&
                        inputMethodInfo.component.className == IME_SERVICE_CLASS_NAME
                    ) {
                        acc
                    } else {
                        val label = inputMethodInfo.loadLabel(context.packageManager)
                            .toString()
                        acc + (label to inputMethodInfo.id)
                    }
                }
        } ?: emptyMap()
    }
}
