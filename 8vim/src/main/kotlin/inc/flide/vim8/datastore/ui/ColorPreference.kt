package inc.flide.vim8.datastore.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import arrow.core.Option
import com.github.skydoves.colorpicker.compose.AlphaTile
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import inc.flide.vim8.R
import inc.flide.vim8.datastore.model.PreferenceData
import inc.flide.vim8.datastore.model.PreferenceModel
import inc.flide.vim8.datastore.model.observeAsState
import inc.flide.vim8.lib.compose.stringRes

private fun Color.darkenColor(): Color = Color(
    red * 192 / 256,
    green * 192 / 256,
    blue * 192 / 256
)

@Composable
private fun Circle(modifier: Modifier = Modifier, color: Color, content: @Composable () -> Unit) {
    Surface(
        modifier = modifier.size(32.dp),
        color = color,
        shape = CircleShape,
        border = BorderStroke(1.dp, color.darkenColor()),
        content = content
    )
}

@Composable
fun <T : PreferenceModel> PreferenceUiScope<T>.ColorPreference(
    pref: PreferenceData<Int>,
    modifier: Modifier = Modifier,
    @DrawableRes iconId: Int? = null,
    iconSpaceReserved: Boolean = this.iconSpaceReserved,
    title: String,
    summary: String? = null,
    enabledIf: PreferenceDataEvaluator = { true },
    visibleIf: PreferenceDataEvaluator = { true }
) {
    val controller = rememberColorPickerController()
    val prefValue by pref.observeAsState()
    var color by remember { mutableStateOf(Color(pref.get())) }
    val evalScope = PreferenceDataEvaluatorScope.instance()
    var openAlertDialog by remember { mutableStateOf(false) }
    var hexCode by remember { mutableStateOf("#${color.toHexCode().uppercase()}") }
    var isError by remember { mutableStateOf(false) }
    if (this.visibleIf(evalScope) && visibleIf(evalScope)) {
        if (openAlertDialog) {
            AlertDialog(
                title = { Text(title) },
                onDismissRequest = { openAlertDialog = false },
                dismissButton = {
                    TextButton(onClick = {
                        openAlertDialog = false
                    }) {
                        Text(stringRes(R.string.dialog__dismiss__label))
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        pref.set(color.toArgb())
                        openAlertDialog = false
                    }) {
                        Text(stringRes(R.string.dialog__confirm__label))
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.Top) {
                        Box(modifier = Modifier.weight(8f)) {
                            HsvColorPicker(
                                modifier = Modifier.padding(10.dp),
                                controller = controller,
                                initialColor = Color(pref.get()),
                                onColorChanged = {
                                    color = it.color
                                    hexCode = "#${it.color.toHexCode().uppercase()}"
                                }
                            )
                        }
                        BrightnessSlider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                                .height(35.dp)
                                .align(Alignment.CenterHorizontally),
                            controller = controller
                        )
                        TextField(
                            value = hexCode,
                            isError = isError,
                            onValueChange = {
                                val value = if (it[0] == '#') it else "#$it"
                                if (value.length > 7) return@TextField
                                isError = Option
                                    .catch { Color(android.graphics.Color.parseColor(value)) }
                                    .onSome { color -> controller.selectByColor(color, true) }
                                    .onNone { hexCode = value }
                                    .isNone()
                            },
                            singleLine = true,
                            trailingIcon = {
                                AlphaTile(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .align(Alignment.CenterHorizontally)
                                        .clip(RoundedCornerShape(6.dp)),
                                    controller = controller
                                )
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            )
        }
        Preference(
            title = title,
            summary = summary,
            modifier = modifier,
            iconId = iconId,
            iconSpaceReserved = iconSpaceReserved,
            trailing = {
                Circle(
                    modifier = Modifier.size(32.dp),
                    color = Color(prefValue)
                ) {
                }
            },
            onClick = { openAlertDialog = true },
            enabledIf = enabledIf,
            visibleIf = visibleIf
        )
    }
}

private fun Color.toHexCode(): String {
    val red = this.red * 255
    val green = this.green * 255
    val blue = this.blue * 255
    return String.format("%02x%02x%02x", red.toInt(), green.toInt(), blue.toInt())
}
