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
import inc.flide.vim8.datastore.ui.Preference
import inc.flide.vim8.lib.android.launchUrl
import inc.flide.vim8.lib.compose.AppIcon
import inc.flide.vim8.lib.compose.Screen
import inc.flide.vim8.lib.compose.stringRes

@Composable
fun AboutScreen() = Screen {
    title = stringRes(R.string.about_us_label)

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
                text = stringRes(R.string.about_8vim_text),
                fontSize = 15.sp
            )
            Text(
                text = stringRes(R.string.owner_text),
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 10.dp)
            )
        }

        Preference(
            iconId = R.drawable.ic_error_outline,
            title = stringRes(R.string.version_name_label),
            summary = stringRes(R.string.version_name)
        )
        Divider()
        Preference(
            iconId = R.drawable.github_vd_vector,
            title = stringRes(R.string.connect_using_github_label),
            onClick = { context.launchUrl("https://github.com/8VIM/8VIM") }
        )
        Divider()
        Preference(
            iconId = R.drawable.matrix_vd_vector,
            title = stringRes(R.string.join_us_on_matrix_label),
            onClick = { context.launchUrl("https://app.element.io/#/room/#8vim/lobby:matrix.org") }
        )
        Divider()
        Preference(
            iconId = R.drawable.twitter_vd_vector,
            title = stringRes(R.string.connect_using_twitter_label),
            onClick = { context.launchUrl("https://twitter.com/8vim_?s=09") }
        )
        Divider()
        Preference(
            iconId = R.drawable.playstore_vd_vector,
            title = stringRes(R.string.connect_using_google_play_store_label),
            onClick = {
                context.launchUrl(
                    "https://play.google.com/store/apps/details?id=inc.flide.vi8"
                )
            }
        )
        Divider()
    }
}
