package inc.flide.vim8.app.settings.layouts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import inc.flide.vim8.R
import inc.flide.vim8.app.LocalNavController
import inc.flide.vim8.app.Routes
import inc.flide.vim8.datastore.ui.JetPrefListItem
import inc.flide.vim8.datastore.ui.PreferenceGroup
import inc.flide.vim8.lib.compose.Screen
import inc.flide.vim8.lib.compose.scrollbar
import inc.flide.vim8.lib.compose.stringRes
import inc.flide.vim8.models.appPreferenceModel
import inc.flide.vim8.models.rememberEmbeddedLayouts

@Composable
fun LayoutsScreen() = Screen {
    title = stringRes(R.string.settings__layouts__title)

    scrollable = false
//    previewFieldVisible = false
//    iconSpaceReserved = false
    val prefs by appPreferenceModel()
    val navController = LocalNavController.current
    val layoutPref = prefs.layout.current
    val (tmpLayoutValue, setTmplayoutValue) = remember { mutableStateOf(layoutPref.get()) }
    val embeddedLayouts = rememberEmbeddedLayouts()

    content {
        val state = rememberLazyListState()
        PreferenceGroup(title = stringRes(id = R.string.settings__layouts__title)) {
            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .scrollbar(state, isVertical = true),
                    state = state,
                ) {
                    items(embeddedLayouts.toList()) { layout ->
                        JetPrefListItem(
                            modifier = Modifier.clickable {
                                setTmplayoutValue(layout.second)
                            },
                            text = layout.first,
                            leadingContent = {
                                RadioButton(
                                    selected = layout.second == tmpLayoutValue,
                                    onClick = null,
                                    modifier = Modifier.padding(end = 12.dp),
                                )
                            }
                        )
                    }
                }
            }
        }

    }
    bottomBar {
        BottomAppBar(floatingActionButton = {
            ExtendedFloatingActionButton(
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_add),
                        contentDescription = "Import"
                    )
                },
                text = { Text(text = "Import") },
                onClick = { navController.navigate(Routes.Settings.LayoutImport) })
        },
            actions = {
                TextButton(onClick = { navController.popBackStack() }) {
                    Text(text = stringRes(id = R.string.generic_cancel_text))
                }
                TextButton(onClick = {
                    layoutPref.set(tmpLayoutValue)
                    navController.popBackStack()
                }) {
                    Text(text = stringRes(id = R.string.action__save))
                }
            }
        )
    }
}