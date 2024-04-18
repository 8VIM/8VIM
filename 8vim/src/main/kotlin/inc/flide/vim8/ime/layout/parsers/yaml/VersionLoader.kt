package inc.flide.vim8.ime.layout.parsers.yaml

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import com.networknt.schema.JsonSchema
import com.networknt.schema.ValidationMessage
import inc.flide.vim8.ime.layout.models.error.InvalidLayoutError
import inc.flide.vim8.ime.layout.models.error.LayoutError
import inc.flide.vim8.ime.layout.models.error.validationMessages
import inc.flide.vim8.ime.layout.models.yaml.versions.version2.Layout as Layout2
import inc.flide.vim8.ime.layout.models.yaml.versions.version2.toLatest
import inc.flide.vim8.ime.layout.models.yaml.versions.version21.Layout as Layout21
import java.text.MessageFormat

fun JsonNode.loadYaml(schema: JsonSchema, mapper: ObjectMapper): Either<LayoutError, Layout21> =
    schema.validate(this)
        .fold(InvalidLayoutError(emptySet())) { acc, error ->
            val newError =
                if (error.message.startsWith('$')) {
                    error
                } else {
                    ValidationMessage.Builder()
                        .type(error.type)
                        .code(error.code)
                        .path(error.path)
                        .details(error.details)
                        .arguments(error.message)
                        .format(MessageFormat("{0}: {1}"))
                        .build()
                }
            InvalidLayoutError.validationMessages.modify(acc) { it + newError }
        }.let {
            if (it.validationMessages.isNotEmpty()) {
                it.left()
            } else {
                val version = get("version")?.asText() ?: "2"
                when (version) {
                    "2.1" -> mapper.convertValue<Layout21>(this).right()
                    else -> mapper.convertValue<Layout2>(this).toLatest().right()
                }
            }
        }
