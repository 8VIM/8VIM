package inc.flide.vim8.lib.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import inc.flide.vim8.models.EmbeddedLayout
import inc.flide.vim8.models.embeddedLayouts
import java.util.TreeMap

@Composable
fun rememberEmbeddedLayouts(): TreeMap<String, EmbeddedLayout> {
    val context = LocalContext.current
    return remember { embeddedLayouts(context) }
}
