package inc.flide.vim8.lib.compose

import android.content.Context
import android.view.View
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import inc.flide.vim8.R
import inc.flide.vim8.lib.kotlin.CurlyArg
import inc.flide.vim8.lib.kotlin.curlyFormat

private val localResourcesContext = staticCompositionLocalOf<Context> {
    error("resources context not initialized!!")
}

private val localAppNameString = staticCompositionLocalOf {
    "8VIM"
}

@Composable
fun ProvideLocalizedResources(
    resourcesContext: Context,
    forceLayoutDirection: LayoutDirection? = null,
    content: @Composable () -> Unit
) {
    val layoutDirection = forceLayoutDirection ?: when (resourcesContext.resources.configuration.layoutDirection) {
        View.LAYOUT_DIRECTION_LTR -> LayoutDirection.Ltr
        View.LAYOUT_DIRECTION_RTL -> LayoutDirection.Rtl
        else -> error("Given configuration specifies invalid layout direction!")
    }
    CompositionLocalProvider(
        localResourcesContext provides resourcesContext,
        LocalLayoutDirection provides layoutDirection,
        localAppNameString provides stringResource(R.string.app_name)
    ) {
        content()
    }
}

@Composable
fun stringRes(
    @StringRes id: Int,
    vararg args: CurlyArg
): String {
    val string = localResourcesContext.current.resources
        .getString(id)
    return formatString(string, args)
}

@Composable
private fun formatString(
    string: String,
    args: Array<out CurlyArg>
): String {
    return string.curlyFormat(
        "app_name" to localAppNameString.current,
        *args
    )
}
