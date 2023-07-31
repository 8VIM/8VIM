package inc.flide.vim8.lib.compose

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    title: String,
    navigationIcon: ScreenNavigationIcon?,
    actions: @Composable RowScope.() -> Unit = { },
) {
    TopAppBar(
        navigationIcon = navigationIcon ?: {},
        title = { Text(text = title) },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
    )
}
