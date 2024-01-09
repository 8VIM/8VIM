package inc.flide.vim8.ime.views

import android.content.Intent
import android.view.KeyEvent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import inc.flide.vim8.R
import inc.flide.vim8.app.MainActivity
import inc.flide.vim8.appPreferenceModel
import inc.flide.vim8.datastore.model.observeAsState
import inc.flide.vim8.ime.clipboard.ClipboardLayout
import inc.flide.vim8.ime.input.ImeUiMode
import inc.flide.vim8.ime.keyboard.text.toKeyboardAction
import inc.flide.vim8.ime.layout.models.CustomKeycode
import inc.flide.vim8.keyboardManager
import inc.flide.vim8.lib.android.launchActivity
import inc.flide.vim8.lib.compose.ImageButton
import inc.flide.vim8.lib.compose.stringRes

@Composable
fun KeyboardLayout() {
    val prefs by appPreferenceModel()
    val context = LocalContext.current
    val keyboardManager by context.keyboardManager()

    val isOnLeft by prefs.keyboard.sidebar.isOnLeft.observeAsState()
    val state by keyboardManager.activeState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
        ) {
            Row {
                if (isOnLeft) Sidebar()
                Column(
                    modifier = Modifier
                        .weight(5f)
                ) {
                    if (state.imeUiMode == ImeUiMode.TEXT) {
                        XpadLayout()
                    } else {
                        ClipboardLayout()
                    }
                }
                if (!isOnLeft) Sidebar()
            }
        }
    }
}

@Composable
fun RowScope.Sidebar() {
    val prefs by appPreferenceModel()

    val isVisible by prefs.keyboard.sidebar.isVisible.observeAsState()

    if (!isVisible) return

    val context = LocalContext.current
    val keyboardManager by context.keyboardManager()
    val inputEventDispatcher = keyboardManager.inputEventDispatcher
    val state by keyboardManager.activeState.collectAsState()

    Column(modifier = Modifier.weight(1f)) {
        ImageButton(
            resourceId = R.drawable.ic_emoji,
            description = stringRes(R.string.open_emoticon_keyboard_button_content_description),
            onClick = { inputEventDispatcher.sendDownUp(CustomKeycode.SWITCH_TO_EMOTICON_KEYBOARD.toKeyboardAction()) }
        )

        ImageButton(
            resourceId = R.drawable.ic_keyboard_tab,
            description = stringRes(R.string.open_emoticon_keyboard_button_content_description),
            onClick = { inputEventDispatcher.sendDownUp(KeyEvent.KEYCODE_TAB.toKeyboardAction()) }
        )

        ImageButton(
            resourceId = R.drawable.ic_open_with_black,
            description = stringRes(R.string.open_emoticon_keyboard_button_content_description),
            onClick = { inputEventDispatcher.sendDownUp(CustomKeycode.SWITCH_TO_SELECTION_KEYPAD.toKeyboardAction()) }
        )

        ImageButton(
            resourceId = R.drawable.key_icon_settings,
            description = stringRes(R.string.open_emoticon_keyboard_button_content_description),
            onClick = {
                context.launchActivity(MainActivity::class) {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            }
        )

        ImageButton(
            resourceId = if (state.isCtrlOn) R.drawable.ic_ctrl_engaged else R.drawable.ic_ctrl,
            description = stringRes(R.string.open_emoticon_keyboard_button_content_description),
            onClick = { inputEventDispatcher.sendDownUp(CustomKeycode.CTRL_TOGGLE.toKeyboardAction()) }
        )
        if (state.imeUiMode == ImeUiMode.TEXT) {
            ImageButton(
                resourceId = R.drawable.ic_content_paste,
                description = stringRes(R.string.open_emoticon_keyboard_button_content_description),
                onClick = { inputEventDispatcher.sendDownUp(CustomKeycode.SWITCH_TO_CLIPPAD_KEYBOARD.toKeyboardAction()) }
            )
        } else {
            ImageButton(
                resourceId = R.drawable.ic_viii,
                description = stringRes(R.string.main_keyboard_button_content_description),
                onClick = { inputEventDispatcher.sendDownUp(CustomKeycode.SWITCH_TO_MAIN_KEYPAD.toKeyboardAction()) }
            )
        }
    }
}