package inc.flide.vim8.models.yaml

import android.view.KeyEvent
import com.fasterxml.jackson.core.JacksonException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.databind.node.ArrayNode
import java.io.IOException

class Flags(val value: Int) {

    class FlagsDeserializer : JsonDeserializer<Flags>() {
        @Throws(IOException::class, NullPointerException::class)
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Flags {
            val node = p.codec.readTree<JsonNode>(p)
            if (node.isInt || node.isTextual) {
                return Flags(getArrayFlag(node, p))
            } else if (node.isArray) {
                val arrayNode = node as ArrayNode
                val iterator = arrayNode.elements()
                var result = 0
                while (iterator.hasNext()) {
                    val current = iterator.next()
                    result = result or getArrayFlag(current, p)
                }
                return Flags(result)
            }
            throw MismatchedInputException.from(
                p,
                null as Class<*>?,
                "When using an array for flags, it only support integer or string"
            )
        }

        @Throws(JacksonException::class)
        private fun getArrayFlag(node: JsonNode, p: JsonParser): Int {
            return if (node.isInt && node.intValue() >= 0) {
                node.intValue()
            } else if (node.isInt) {
                throw MismatchedInputException.from(
                    p,
                    null as Class<*>?,
                    "flag value must be positive"
                )
            } else if (node.isTextual) {
                when (node.textValue().uppercase()) {
                    "META_SHIFT_ON" -> KeyEvent.META_SHIFT_ON
                    "META_ALT_ON" -> KeyEvent.META_ALT_ON
                    "META_SYM_ON" -> KeyEvent.META_SYM_ON
                    "META_FUNCTION_ON" -> KeyEvent.META_FUNCTION_ON
                    "META_ALT_LEFT_ON" -> KeyEvent.META_ALT_LEFT_ON
                    "META_ALT_RIGHT_ON" -> KeyEvent.META_ALT_RIGHT_ON
                    "META_SHIFT_LEFT_ON" -> KeyEvent.META_SHIFT_LEFT_ON
                    "META_SHIFT_RIGHT_ON" -> KeyEvent.META_SHIFT_RIGHT_ON
                    "META_CTRL_ON" -> KeyEvent.META_CTRL_ON
                    "META_CTRL_LEFT_ON" -> KeyEvent.META_CTRL_LEFT_ON
                    "META_CTRL_RIGHT_ON" -> KeyEvent.META_CTRL_RIGHT_ON
                    "META_META_ON" -> KeyEvent.META_META_ON
                    "META_META_LEFT_ON" -> KeyEvent.META_META_LEFT_ON
                    "META_META_RIGHT_ON" -> KeyEvent.META_META_RIGHT_ON
                    "META_CAPS_LOCK_ON" -> KeyEvent.META_CAPS_LOCK_ON
                    "META_NUM_LOCK_ON" -> KeyEvent.META_NUM_LOCK_ON
                    "META_SCROLL_LOCK_ON" -> KeyEvent.META_SCROLL_LOCK_ON
                    else -> throw MismatchedInputException.from(
                        p,
                        null as Class<*>?,
                        "unknown meta modifier"
                    )
                }
            } else {
                throw MismatchedInputException.from(
                    p,
                    null as Class<*>?,
                    "When using an array for flags, all values must be of the same type"
                )
            }
        }
    }

    companion object {
        fun empty(): Flags {
            return Flags(0)
        }
    }
}
