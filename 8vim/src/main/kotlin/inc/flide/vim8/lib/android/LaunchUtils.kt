package inc.flide.vim8.lib.android

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.annotation.StringRes
import inc.flide.vim8.R
import inc.flide.vim8.lib.kotlin.CurlyArg
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.reflect.KClass

fun Context.launchUrl(url: String) {
    val intent = Intent().also {
        it.action = Intent.ACTION_VIEW
        it.data = Uri.parse(url)
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    try {
        this.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(
            this,
            this.stringRes(R.string.general__no_browser_app_found_for_url, "url" to url),
            Toast.LENGTH_LONG,
        ).show()
    }
}

fun Context.launchUrl(@StringRes url: Int) {
    launchUrl(this.stringRes(url))
}

fun Context.launchUrl(@StringRes url: Int, vararg args: CurlyArg) {
    launchUrl(this.stringRes(url, *args))
}

inline fun <T : Any> Context.launchActivity(
    kClass: KClass<T>,
    intentModifier: (Intent) -> Unit = { }
) {
    contract {
        callsInPlace(intentModifier, InvocationKind.EXACTLY_ONCE)
    }
    try {
        val intent = Intent(this, kClass.java)
        intentModifier(intent)
        this.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(this, e.localizedMessage, Toast.LENGTH_LONG).show()
    }
}

inline fun Context.launchActivity(intentModifier: (Intent) -> Unit) {
    contract {
        callsInPlace(intentModifier, InvocationKind.EXACTLY_ONCE)
    }
    try {
        val intent = Intent()
        intentModifier(intent)
        this.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(this, e.localizedMessage, Toast.LENGTH_LONG).show()
    }
}
