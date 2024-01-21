package inc.flide.vim8.app.settings.about

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import inc.flide.vim8.R
import inc.flide.vim8.lib.compose.Screen
import inc.flide.vim8.lib.compose.scrollbar
import inc.flide.vim8.lib.compose.stringRes

@Composable
fun ThirdPartyLicencesScreen() = Screen {
    title = stringRes(R.string.about__third_party_licenses__title)
    scrollable = false

    val lazyListState = rememberLazyListState()
    content {
        LibrariesContainer(
            modifier = Modifier
                .fillMaxSize()
                .scrollbar(lazyListState, isVertical = true),
            lazyListState = lazyListState
        )
    }
}
