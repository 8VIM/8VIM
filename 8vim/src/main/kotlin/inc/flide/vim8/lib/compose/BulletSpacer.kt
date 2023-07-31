package inc.flide.vim8.lib.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun RowScope.BulletSpacer(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .align(Alignment.CenterVertically)
            .padding(horizontal = 8.dp)
            .size(4.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.outline),
    )
}
