package inc.flide.vim8.app.setup

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import inc.flide.vim8.R
import inc.flide.vim8.app.LocalNavController
import inc.flide.vim8.app.MainActivity
import inc.flide.vim8.app.Routes
import inc.flide.vim8.app.Urls
import inc.flide.vim8.lib.android.launchActivity
import inc.flide.vim8.lib.android.launchUrl
import inc.flide.vim8.lib.compose.Screen
import inc.flide.vim8.lib.compose.Step
import inc.flide.vim8.lib.compose.StepLayout
import inc.flide.vim8.lib.compose.StepState
import inc.flide.vim8.lib.compose.stringRes
import inc.flide.vim8.lib.util.InputMethodUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

private object SetupStep {
    const val EnableIme: Int = 1
    const val SelectIme: Int = 2
    const val FinishUp: Int = 3
}

@Composable
fun SetupScreen() = Screen {
    title = stringRes(R.string.setup__title)
    imeAlerts = false
    navigationIconVisible = false
    scrollable = false
    val navController = LocalNavController.current
    val context = LocalContext.current

    val is8VimEnabled by InputMethodUtils.observeIs8VimEnabled()
    val is8VimSelected by InputMethodUtils.observeIs8VimSelected(foregroundOnly = true)

    val stepState = rememberSaveable(saver = StepState.Saver) {
        val initStep = when {
            !is8VimEnabled -> SetupStep.EnableIme
            !is8VimSelected -> SetupStep.SelectIme
            else -> SetupStep.FinishUp
        }
        StepState.new(init = initStep)
    }

    content {
        LaunchedEffect(is8VimEnabled, is8VimSelected) {
            stepState.setCurrentAuto(
                when {
                    !is8VimEnabled -> SetupStep.EnableIme
                    !is8VimSelected -> SetupStep.SelectIme
                    else -> SetupStep.FinishUp
                }
            )
        }

        // Below block allows to return from the system IME enabler activity
        // as soon as it gets selected.
        LaunchedEffect(Unit) {
            while (isActive) {
                delay(200)
                val isEnabled = InputMethodUtils.parseIs8VimEnabled(context)
                if (stepState.getCurrentAuto().value == SetupStep.EnableIme &&
                    stepState.getCurrentManual().value == -1 &&
                    !is8VimEnabled &&
                    !is8VimSelected &&
                    isEnabled
                ) {
                    context.launchActivity(MainActivity::class) {
                        it.flags = (
                                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                                        or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                        or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                )
                    }
                }
            }
        }

        StepLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            stepState = stepState,
            header = {
                StepText(stringRes(R.string.setup__intro_message))
                Spacer(modifier = Modifier.height(16.dp))
            },
            steps = listOf(
                Step(
                    id = SetupStep.EnableIme,
                    title = stringRes(R.string.setup__enable_ime__title)
                ) {
                    StepText(stringRes(R.string.setup__enable_ime__description))
                    StepButton(label = stringRes(R.string.setup__enable_ime__open_settings_btn)) {
                        InputMethodUtils.showImeEnablerActivity(context)
                    }
                },
                Step(
                    id = SetupStep.SelectIme,
                    title = stringRes(R.string.setup__select_ime__title)
                ) {
                    StepText(stringRes(R.string.setup__select_ime__description))
                    StepButton(label = stringRes(R.string.setup__select_ime__switch_keyboard_btn)) {
                        InputMethodUtils.showImePicker(context)
                    }
                },
                Step(
                    id = SetupStep.FinishUp,
                    title = stringRes(R.string.setup__finish_up__title)
                ) {
                    StepText(stringRes(R.string.setup__finish_up__description_p1))
                    StepText(stringRes(R.string.setup__finish_up__description_p2))
                    StepButton(label = stringRes(R.string.setup__finish_up__finish_btn)) {
                        this@content.prefs.internal.isImeSetup.set(true)
                        navController.navigate(Routes.Settings.Home) {
                            popUpTo(Routes.Setup.Screen) {
                                inclusive = true
                            }
                        }
                    }
                }
            ),
            footer = {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(onClick = { context.launchUrl(Urls.GITHUB) }) {
                        Text(text = stringRes(R.string.setup__footer__repository))
                    }
                }
            }
        )
    }
}
