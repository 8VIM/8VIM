package inc.flide.vim8.app.settings.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import inc.flide.vim8.R
import inc.flide.vim8.app.Urls
import inc.flide.vim8.datastore.ui.Preference
import inc.flide.vim8.lib.android.launchUrl
import inc.flide.vim8.lib.compose.AppIcon
import inc.flide.vim8.lib.compose.Screen
import inc.flide.vim8.lib.compose.stringRes

@Composable
fun AboutScreen() = Screen {
    title = stringRes(R.string.settings__about__title)

    val context = LocalContext.current
    content {
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 32.dp)
        ) {
            AppIcon()
            Text(
                text = stringRes(R.string.app_name),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
        Column(
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = stringRes(R.string.settings__about__description),
                fontSize = 15.sp
            )
            Text(
                text = stringRes(R.string.settings__about__owner__label),
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 10.dp)
            )
        }

        Preference(
            iconId = R.drawable.ic_error_outline,
            title = stringRes(R.string.app__version__label),
            summary = stringRes(R.string.version_name)
        )
        Divider()
        Preference(
            iconId = R.drawable.github_vd_vector,
            title = stringRes(R.string.settings__about__github__label),
            onClick = { context.launchUrl(Urls.GITHUB) }
        )
        Divider()
        Preference(
            iconId = R.drawable.matrix_vd_vector,
            title = stringRes(R.string.settings__about__matrix__label),
            onClick = { context.launchUrl(Urls.MATRIX) }
        )
        Divider()
        Preference(
            iconId = R.drawable.twitter_vd_vector,
            title = stringRes(R.string.settings__about__twitter__label),
            onClick = { context.launchUrl(Urls.TWITTER) }
        )
        Divider()
        Preference(
            iconId = R.drawable.playstore_vd_vector,
            title = stringRes(R.string.settings__about__play_store__label),
            onClick = { context.launchUrl(Urls.PLAY_STORE) }
        )

    }
}
