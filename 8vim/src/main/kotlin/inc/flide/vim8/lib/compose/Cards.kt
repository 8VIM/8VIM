package inc.flide.vim8.lib.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import inc.flide.vim8.R

object CardDefaults {
    val IconRequiredSize = 24.dp
    val IconSpacing = 8.dp

    val ContentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp)
}

@Composable
fun ErrorCard(
    text: String,
    modifier: Modifier = Modifier,
    showIcon: Boolean = true,
    contentPadding: PaddingValues = CardDefaults.ContentPadding,
    onClick: (() -> Unit)? = null
) {
    BaseCard(
        modifier = modifier,
        backgroundColor = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        onClick = onClick,
        icon = if (showIcon) ({
            Icon(
                modifier = Modifier
                    .padding(end = CardDefaults.IconSpacing)
                    .requiredSize(CardDefaults.IconRequiredSize),
                painter = painterResource(R.drawable.ic_error_outline),
                contentDescription = null
            )
        }) else null,
        text = text,
        contentPadding = contentPadding
    )
}

@Composable
fun WaringCard(
    text: String,
    modifier: Modifier = Modifier,
    showIcon: Boolean = true,
    contentPadding: PaddingValues = CardDefaults.ContentPadding,
    onClick: (() -> Unit)? = null
) {
    BaseCard(
        modifier = modifier,
        backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        onClick = onClick,
        icon = if (showIcon) ({
            Icon(
                modifier = Modifier
                    .padding(end = CardDefaults.IconSpacing)
                    .requiredSize(CardDefaults.IconRequiredSize),
                painter = painterResource(R.drawable.ic_error_outline),
                contentDescription = null
            )
        }) else null,
        text = text,
        contentPadding = contentPadding
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseCard(
    modifier: Modifier = Modifier,
    text: String,
    secondaryText: String? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(backgroundColor),
    contentPadding: PaddingValues = CardDefaults.ContentPadding,
    icon: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    OutlinedCard(
        onClick = onClick ?: {},
        enabled = onClick != null,
        modifier = modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
    ) {
        Row(
            modifier = Modifier.padding(contentPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                icon()
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = if (icon != null) 16.dp else 0.dp),
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                )
                if (secondaryText != null) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = secondaryText,
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
            }
        }

    }
}