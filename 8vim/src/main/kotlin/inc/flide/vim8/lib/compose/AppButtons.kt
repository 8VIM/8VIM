package inc.flide.vim8.lib.compose

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.takeOrElse

@Composable
fun AppIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: Painter,
    enabled: Boolean = true,
    iconModifier: Modifier = Modifier,
    iconColor: Color = Color.Unspecified,
) {
    IconButton(
        modifier = modifier,
        enabled = enabled,
        onClick = onClick,
    ) {
        val contentColor = iconColor.takeOrElse { LocalContentColor.current }
        CompositionLocalProvider(
            LocalContentColor provides contentColor,
        ) {
            Icon(
                modifier = iconModifier,
                painter = icon,
                contentDescription = null,
            )
        }
    }
}
