package inc.flide.vim8.models.yaml

import android.view.KeyEvent
import arrow.optics.optics
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSetter
import inc.flide.vim8.lib.android.tryOrNull
import inc.flide.vim8.models.CustomKeycode
import inc.flide.vim8.models.FingerPosition
import inc.flide.vim8.models.KeyboardActionType
import java.util.Locale

@optics
data class Action(
    @JsonProperty(value = "type") val actionType: KeyboardActionType = KeyboardActionType.INPUT_TEXT,
    val lowerCase: String = "",
    val upperCase: String = "",
    val movementSequence: List<FingerPosition> = ArrayList(),
    val flags: Flags = Flags.empty()
) {
    companion object

    var keyCode = 0
        private set

    @JsonSetter("key_code")
    fun setKeyCode(keyCodeString: String?) {
        keyCode = tryOrNull {
            keyCodeString?.let {
                val uppercaseKeyCodeString = it.uppercase(Locale.getDefault())
                val keyCode = KeyEvent.keyCodeFromString(uppercaseKeyCodeString)
                if (keyCode == KeyEvent.KEYCODE_UNKNOWN) {
                    CustomKeycode.valueOf(uppercaseKeyCodeString).keyCode
                } else keyCode
            }
        } ?: 0
    }

}

fun Action?.isEmpty(): Boolean = this == null || (this.lowerCase.isEmpty()
        && this.upperCase.isEmpty()
        && this.movementSequence.isEmpty() && this.flags.value == 0)