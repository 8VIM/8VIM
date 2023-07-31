package inc.flide.vim8.datastore.ui

import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource

@Composable
internal fun maybeJetIcon(
    @DrawableRes id: Int?,
    iconSpaceReserved: Boolean,
    contentDescription: String? = null,
): @Composable (() -> Unit)? {
    return when {
        id != null -> ({
            Icon(
                painter = painterResource(id),
                contentDescription = contentDescription,
            )
        })

        iconSpaceReserved -> ({ })
        else -> null
    }
}