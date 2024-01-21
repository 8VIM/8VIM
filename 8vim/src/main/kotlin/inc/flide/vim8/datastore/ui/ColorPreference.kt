package inc.flide.vim8.datastore.ui

import android.content.Context
import androidx.annotation.ArrayRes
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import inc.flide.vim8.R
import inc.flide.vim8.datastore.model.PreferenceData
import inc.flide.vim8.datastore.model.PreferenceModel
import inc.flide.vim8.datastore.model.observeAsState
import inc.flide.vim8.lib.compose.stringRes
import inc.flide.vim8.lib.compose.verticalScroll

private fun Color.darkenColor(): Color = Color(
    red * 192 / 256,
    green * 192 / 256,
    blue * 192 / 256
)

private fun extractColors(@ArrayRes arrayId: Int, context: Context): List<Int> =
    context.resources.getStringArray(arrayId).map { android.graphics.Color.parseColor(it) }

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
private fun Grid(choices: List<Int>, current: Int, onClick: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .verticalScroll()
            .fillMaxWidth()
    ) {
        var i = 0
        while (i < choices.size) {
            var j = 0
            Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                while (j < 5 && i < choices.size) {
                    val color = Color(choices[i])
                    Circle(
                        modifier = Modifier
                            .padding(8.dp)
                            .clickable { onClick(color.toArgb()) },
                        color = color
                    ) {
                        if (choices[i] == current) {
                            Icon(
                                Icons.Filled.Check,
                                modifier = Modifier.padding(2.dp),
                                contentDescription = null,
                                tint = if (color.luminance() < 0.5f) Color.White else Color.Black
                            )
                        }
                    }
                    j++
                    i++
                }
            }
        }
    }
}

@Composable
fun <T : PreferenceModel> PreferenceUiScope<T>.ColorPreference(
    pref: PreferenceData<Int>,
    modifier: Modifier = Modifier,
    @ArrayRes colorChoices: Int,
    @DrawableRes iconId: Int? = null,
    iconSpaceReserved: Boolean = this.iconSpaceReserved,
    title: String,
    summary: String? = null,
    enabledIf: PreferenceDataEvaluator = { true },
    visibleIf: PreferenceDataEvaluator = { true }
) {
    val context = LocalContext.current
    val choices = extractColors(colorChoices, context)

    val prefValue by pref.observeAsState()
    val evalScope = PreferenceDataEvaluatorScope.instance()
    var openAlertDialog by remember { mutableStateOf(false) }

    if (this.visibleIf(evalScope) && visibleIf(evalScope)) {
        val color = Color(prefValue)
        if (openAlertDialog) {
            AlertDialog(
                title = { Text(title) },
                onDismissRequest = { openAlertDialog = false },
                confirmButton = {
                    TextButton(onClick = { openAlertDialog = false }) {
                        Text(stringRes(R.string.dialog__dismiss__label))
                    }
                },
                text = {
                    Grid(choices = choices, current = prefValue, onClick = {
                        pref.set(it)
                        openAlertDialog = false
                    })
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
                    color = color
                ) {
                }
            },
            onClick = { openAlertDialog = true },
            enabledIf = enabledIf,
            visibleIf = visibleIf
        )
    }
}
