package inc.flide.vim8.models.yaml

import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.recover
import arrow.optics.optics
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import java.io.IOException

@optics
data class Layer(
    @JsonDeserialize(using = SectorDeserializer::class)
    @JsonProperty(required = true)
    val sectors: Map<Int, Part> = mapOf()
) {
    companion object
}

class SectorDeserializer : JsonDeserializer<Map<Int, Part>>() {
    private enum class Direction {
        BOTTOM, LEFT, TOP, RIGHT;
    }

    @Throws(IOException::class, NullPointerException::class)
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Map<Int, Part> {
        return p.codec.readValue<Map<String, Part>?>(p, jacksonTypeRef()).mapKeys { (key) ->
            Option
                .fromNullable(key.toIntOrNull())
                .filter { it > 1 }
                .recover {
                    Option.catch { Direction.valueOf(key.uppercase()) }
                        .map { it.ordinal + 1 }
                        .bind()
                }
                .getOrElse {
                    throw MismatchedInputException.from(
                        p,
                        null as Class<*>?,
                        "Sector value must be positive number or LEFT/BOTTOM/TOP/RIGHT"
                    )
                }
        }
    }
}
