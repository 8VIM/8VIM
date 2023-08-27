package inc.flide.vim8.models.yaml

import android.view.KeyEvent
import arrow.core.Option
import arrow.core.getOrElse
import arrow.optics.optics
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JacksonException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.databind.node.ArrayNode
import inc.flide.vim8.models.CustomKeycode
import inc.flide.vim8.models.FingerPosition
import inc.flide.vim8.models.KeyboardActionType
import inc.flide.vim8.models.MovementSequence
import java.io.IOException
import java.util.Locale

@optics
data class Action(
    @JsonProperty(value = "type")
    val actionType: KeyboardActionType = KeyboardActionType.INPUT_TEXT,
    val lowerCase: String = "",
    val upperCase: String = "",
    @JsonDeserialize(using = FingerPositionDeserializer::class)
    val movementSequence: MovementSequence = listOf(),
    @JsonProperty("key_code") val keyCodeString: String = "",
    val flags: Flags = Flags.empty()
) {
    companion object
}

fun Action.keyCode(): Int {
    return Option.catch {
        val uppercaseKeyCodeString = keyCodeString.uppercase(Locale.getDefault())
        val keyCode = KeyEvent.keyCodeFromString(uppercaseKeyCodeString)
        if (keyCode == KeyEvent.KEYCODE_UNKNOWN) {
            CustomKeycode.valueOf(uppercaseKeyCodeString).keyCode
        } else {
            keyCode
        }
    }.getOrElse { 0 }
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

class FingerPositionDeserializer : JsonDeserializer<MovementSequence>() {
    @Throws(IOException::class, NullPointerException::class)
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): List<Int> {
        val node = p.codec.readTree<JsonNode>(p)
        if (!node.isArray) throw MismatchedInputException.from(
            p, null as Class<*>?,
            "Impossible to deserialize this array of FingerPositions"
        )
        val arrayNode = node as ArrayNode
        val iterator = arrayNode.elements()
        val result: MutableList<Int> = ArrayList()
        while (iterator.hasNext()) {
            val current = iterator.next()
            result.add(getFingerPosition(current, p))
        }
        return result
    }

    @Throws(JacksonException::class)
    private fun getFingerPosition(node: JsonNode, p: JsonParser): Int {
        return if (node.isInt && node.intValue() > 0) {
            node.intValue()
        } else if (node.isInt) {
            throw MismatchedInputException.from(
                p,
                null as Class<*>?,
                "FingerPosition value must be positive"
            )
        } else if (node.isTextual) {
            when (node.textValue().uppercase()) {
                "NO_TOUCH" -> FingerPosition.NO_TOUCH
                "INSIDE_CIRCLE" -> FingerPosition.INSIDE_CIRCLE
                "LONG_PRESS" -> FingerPosition.LONG_PRESS
                "LONG_PRESS_END" -> FingerPosition.LONG_PRESS_END
                else -> throw MismatchedInputException.from(
                    p,
                    null as Class<*>?,
                    "unknown FingerPosition"
                )
            }
        } else {
            throw MismatchedInputException.from(
                p, null as Class<*>?,
                "Impossible to deserializeFingerPosition"
            )
        }
    }
}
