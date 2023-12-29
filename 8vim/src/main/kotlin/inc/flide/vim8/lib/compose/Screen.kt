package inc.flide.vim8.lib.compose

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import inc.flide.vim8.AppPrefs
import inc.flide.vim8.R
import inc.flide.vim8.app.LocalNavController
import inc.flide.vim8.appPreferenceModel
import inc.flide.vim8.datastore.ui.PreferenceLayout
import inc.flide.vim8.datastore.ui.PreferenceUiContent
import inc.flide.vim8.lib.util.InputMethodUtils

@Composable
fun Screen(builder: @Composable ScreenScope.() -> Unit) {
    val scope = remember { ScreenScopeImpl() }
    builder(scope)
    scope.Render()
}

typealias ScreenActions = @Composable RowScope.() -> Unit
typealias ScreenBottomBar = @Composable () -> Unit
typealias ScreenContent = PreferenceUiContent<AppPrefs>
typealias ScreenFab = @Composable () -> Unit
typealias ScreenNavigationIcon = @Composable () -> Unit

interface ScreenScope {
    var title: String

    var navigationIconVisible: Boolean

    var previewFieldVisible: Boolean

    var scrollable: Boolean

    var iconSpaceReserved: Boolean

    var imeAlerts: Boolean

    fun actions(actions: ScreenActions)

    fun bottomBar(bottomBar: ScreenBottomBar)

    fun content(content: ScreenContent)

    fun floatingActionButton(fab: ScreenFab)

    fun navigationIcon(navigationIcon: ScreenNavigationIcon)
}

private class ScreenScopeImpl : ScreenScope {
    override var title: String by mutableStateOf("")
    override var navigationIconVisible: Boolean by mutableStateOf(true)
    override var previewFieldVisible: Boolean by mutableStateOf(false)
    override var scrollable: Boolean by mutableStateOf(true)
    override var iconSpaceReserved: Boolean by mutableStateOf(true)
    override var imeAlerts: Boolean by mutableStateOf(true)

    private var actions: ScreenActions = @Composable { }
    private var bottomBar: ScreenBottomBar = @Composable { }
    private var content: ScreenContent = @Composable { }
    private var fab: ScreenFab = @Composable { }
    private var navigationIcon: ScreenNavigationIcon = @Composable {
        val navController = LocalNavController.current
        AppIconButton(
            icon = painterResource(R.drawable.ic_arrow_back),
            onClick = { navController.popBackStack() },
            modifier = Modifier.autoMirrorForRtl()
        )
    }

    override fun actions(actions: ScreenActions) {
        this.actions = actions
    }

    override fun bottomBar(bottomBar: ScreenBottomBar) {
        this.bottomBar = bottomBar
    }

    override fun content(content: ScreenContent) {
        this.content = content
    }

    override fun floatingActionButton(fab: ScreenFab) {
        this.fab = fab
    }

    override fun navigationIcon(navigationIcon: ScreenNavigationIcon) {
        this.navigationIcon = navigationIcon
    }

    @Composable
    fun Render() {
        val previewFieldController = LocalPreviewFieldController.current

        SideEffect {
            previewFieldController?.isVisible = previewFieldVisible
        }
        Scaffold(
            topBar = { AppBar(title, navigationIcon.takeIf { navigationIconVisible }, actions) },
            bottomBar = bottomBar,
            floatingActionButton = fab
        ) { innerPadding ->
            val modifier = if (scrollable) {
                Modifier.verticalScroll()
            } else {
                Modifier
            }
            PreferenceLayout(
                appPreferenceModel(),
                modifier = modifier
                    .padding(innerPadding)
                    .fillMaxWidth(),
            ) {
                if (imeAlerts) ImeAlertCards()
                content()
            }
        }
    }
}

@Composable
fun ImeAlertCards() {
    val context = LocalContext.current
    val is8VimBoardEnabled by InputMethodUtils.observeIs8VimEnabled()
    val is8VimBoardSelected by InputMethodUtils.observeIs8VimSelected(foregroundOnly = true)

    if (!is8VimBoardEnabled) {
        ErrorCard(
            modifier = Modifier.padding(8.dp),
            text = stringRes(R.string.settings__home__ime_not_enabled),
            onClick = {
                InputMethodUtils.showImeEnablerActivity(context)
            }
        )
    } else if (!is8VimBoardSelected) {
        WarningCard(
            modifier = Modifier.padding(8.dp),
            text = stringRes(R.string.settings__home__ime_not_selected),
            onClick = { InputMethodUtils.showImePicker(context) }
        )
    }
}