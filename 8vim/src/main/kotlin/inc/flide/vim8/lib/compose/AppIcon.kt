package inc.flide.vim8.lib.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import inc.flide.vim8.R

@Composable
fun AppIcon() {
    Image(
        modifier = Modifier
            .requiredSize(100.dp)
            .clip(CircleShape),
        painter = painterResource(R.drawable.app_icon),
        contentDescription = stringRes(R.string.app_name)
    )
}
