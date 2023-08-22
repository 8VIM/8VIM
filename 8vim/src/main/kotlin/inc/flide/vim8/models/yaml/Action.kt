package inc.flide.vim8.models.yaml

import android.view.KeyEvent
import arrow.optics.optics
import com.fasterxml.jackson.annotation.JsonProperty
import inc.flide.vim8.lib.android.tryOrNull
import inc.flide.vim8.models.CustomKeycode
import inc.flide.vim8.models.FingerPosition
import inc.flide.vim8.models.KeyboardActionType
import java.util.Locale

@optics
data class Action(
    @JsonProperty(value = "type")
    val actionType: KeyboardActionType = KeyboardActionType.INPUT_TEXT,
    val lowerCase: String = "",
    val upperCase: String = "",
    val movementSequence: List<FingerPosition> = ArrayList(),
    @JsonProperty("key_code") val keyCodeString: String = "",
    val flags: Flags = Flags.empty()
) {
    companion object
}

fun Action.keyCode(): Int {
    return if (keyCodeString.isEmpty()) {
        0
    } else {
        tryOrNull {
            keyCodeString.let {
                val uppercaseKeyCodeString = it.uppercase(Locale.getDefault())
                val keyCode = KeyEvent.keyCodeFromString(uppercaseKeyCodeString)
                if (keyCode == KeyEvent.KEYCODE_UNKNOWN) {
                    CustomKeycode.valueOf(uppercaseKeyCodeString).keyCode
                } else {
                    keyCode
                }
            }
        } ?: 0
    }
}

fun Action?.isEmpty(): Boolean {
    return this?.let {
        lowerCase.isEmpty() &&
            upperCase.isEmpty() &&
            movementSequence.isEmpty() &&
            flags.value == 0
    }
        ?: true
}
