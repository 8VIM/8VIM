package inc.flide.vim8.lib.compose

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role

@Composable
fun ColumnScope.ImageButton(
    modifier: Modifier = Modifier,
    description: String = "",
    onClick: () -> Unit = {},
    @DrawableRes resourceId: Int
) {
    Image(
        painter = painterResource(resourceId),
        contentDescription = description,
        modifier = modifier
            .clickable(enabled = true, role = Role.Button, onClick = onClick)
            .fillMaxWidth()
            .weight(1f),
        contentScale = ContentScale.Inside,
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
    )
}
