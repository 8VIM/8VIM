package inc.flide.vim8.ime.ui.floating.composable

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FloatingScope.MovingBar() {
    HorizontalDivider(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(0.8f)
            .movable(),
        thickness = 5.dp,
        color = MaterialTheme.colorScheme.onSurface
    )
}
