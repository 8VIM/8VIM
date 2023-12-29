package inc.flide.vim8.ime.keyboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import inc.flide.vim8.R
import inc.flide.vim8.appPreferenceModel
import inc.flide.vim8.datastore.model.observeAsState
import inc.flide.vim8.ime.input.ImeUiMode
import inc.flide.vim8.ime.layout.models.CustomKeycode
import inc.flide.vim8.keyboardManager
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
                if (state.imeUiMode == ImeUiMode.TEXT) {
                    XpadLayout()
                } else {
                    ClipboardLayout()
                }
                if (!isOnLeft) Sidebar()
            }
        }
    }
}

@Composable
fun ClipboardLayout() {
    TODO("Not yet implemented")
}

@Composable
fun RowScope.XpadLayout() {
    val height = LocalKeyboardHeight.current
    Column(
        modifier = Modifier
            .weight(5f)
    ) {
        Text(text = height.toString())

    }
}

@Composable
fun RowScope.Sidebar() {
    val prefs by appPreferenceModel()

    val isVisible by prefs.keyboard.sidebar.isVisible.observeAsState()

    if (!isVisible) return

    val context = LocalContext.current
    val keyboardManager by context.keyboardManager()
    val activeState = keyboardManager.activeState
    val state by activeState.collectAsState()

    Column(
        modifier = Modifier
            .padding(12.dp)
            .weight(1f)
    ) {
        Image(
            painter = painterResource(R.drawable.ic_emoji),
            contentDescription = stringRes(R.string.open_emoticon_keyboard_button_content_description),
            modifier = Modifier
                .clickable(
                    enabled = true,
                    role = Role.Button,
                    onClick = { keyboardManager.handleInput(CustomKeycode.SWITCH_TO_EMOTICON_KEYBOARD) })
                .padding(8.dp),
            contentScale = ContentScale.Inside,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
        )

        Image(
            painter = painterResource(R.drawable.ic_keyboard_tab),
            contentDescription = stringRes(R.string.open_emoticon_keyboard_button_content_description),
            modifier = Modifier
                .clickable(enabled = true, role = Role.Button, onClick = {})
                .padding(8.dp),
            contentScale = ContentScale.Inside,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
        )

        Image(
            painter = painterResource(R.drawable.ic_open_with_black),
            contentDescription = stringRes(R.string.open_emoticon_keyboard_button_content_description),
            modifier = Modifier
                .clickable(enabled = true, role = Role.Button, onClick = {})
                .padding(8.dp),
            contentScale = ContentScale.Inside,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
        )

        Image(
            painter = painterResource(R.drawable.key_icon_settings),
            contentDescription = stringRes(R.string.open_emoticon_keyboard_button_content_description),
            modifier = Modifier
                .clickable(enabled = true, role = Role.Button, onClick = {})
                .padding(8.dp),
            contentScale = ContentScale.Inside,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
        )

        Image(
            painter = painterResource(if (state.isCtrlOn) R.drawable.ic_ctrl_engaged else R.drawable.ic_ctrl),
            contentDescription = stringRes(R.string.open_emoticon_keyboard_button_content_description),
            modifier = Modifier
                .clickable(enabled = true, role = Role.Button, onClick = {
                    activeState.batchEdit { it.isCtrlOn = !it.isCtrlOn }
                })
                .padding(8.dp),
            contentScale = ContentScale.Inside,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
        )

        Image(
            painter = painterResource(R.drawable.ic_content_paste),
            contentDescription = stringRes(R.string.open_emoticon_keyboard_button_content_description),
            modifier = Modifier
                .clickable(enabled = true, role = Role.Button, onClick = {})
                .padding(8.dp),
            contentScale = ContentScale.Inside,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
        )
    }
}