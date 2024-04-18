package inc.flide.vim8.lib.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import inc.flide.vim8.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElevatedCard(
    modifier: Modifier = Modifier,
    text: String,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(backgroundColor = containerColor),
    icon: (@Composable RowScope.() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    androidx.compose.material3.ElevatedCard(
        onClick = onClick ?: {},
        enabled = onClick != null,
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(
            contentColor = contentColor,
            containerColor = containerColor
        )
    ) {
        Row(
            modifier = Modifier.padding(PaddingValues(16.dp))
        ) {
            if (icon != null) {
                icon()
            }
            Column(
                modifier = Modifier
                    .padding(start = if (icon != null) 16.dp else 0.dp)
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium,
                    text = text
                )
            }
        }
    }
}

@Composable
fun ErrorCard(text: String, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    ElevatedCard(
        text = text,
        containerColor = MaterialTheme.colorScheme.errorContainer,
        onClick = onClick,
        modifier = modifier,
        icon = {
            Icon(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .requiredSize(24.dp)
                    .align(Alignment.CenterVertically),
                painter = painterResource(R.drawable.ic_error_outline),
                contentDescription = null
            )
        }
    )
}

@Composable
fun WarningCard(text: String, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    ElevatedCard(
        text = text,
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        onClick = onClick,
        modifier = modifier,
        icon = {
            Icon(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .requiredSize(24.dp)
                    .align(Alignment.CenterVertically),
                painter = painterResource(R.drawable.ic_outline_warning),
                contentDescription = null
            )
        }
    )
}
