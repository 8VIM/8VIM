package inc.flide.vim8.ime

import arrow.core.Either
import arrow.core.fold
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.raise.catch
import arrow.core.right
import arrow.core.toMap
import arrow.integrations.jackson.module.registerArrowModule
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.networknt.schema.JsonSchema
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersionDetector
import com.networknt.schema.ValidationMessage
import inc.flide.vim8.models.CharacterPosition
import inc.flide.vim8.models.FingerPosition
import inc.flide.vim8.models.KeyboardAction
import inc.flide.vim8.models.KeyboardData
import inc.flide.vim8.models.LayerLevel
import inc.flide.vim8.models.Quadrant
import inc.flide.vim8.models.error.ExceptionWrapperError
import inc.flide.vim8.models.error.InvalidLayoutError
import inc.flide.vim8.models.error.LayoutError
import inc.flide.vim8.models.error.validationMessages
import inc.flide.vim8.models.info
import inc.flide.vim8.models.yaml.Action
import inc.flide.vim8.models.yaml.Flags
import inc.flide.vim8.models.yaml.Flags.FlagsDeserializer
import inc.flide.vim8.models.yaml.Layer
import inc.flide.vim8.models.yaml.Layout
import inc.flide.vim8.models.yaml.isEmpty
import inc.flide.vim8.models.yaml.toLayerLevel
import inc.flide.vim8.models.yaml.upperCase
import inc.flide.vim8.structures.Constants
import java.io.IOException
import java.io.InputStream
import java.text.MessageFormat

object KeyboardDataYamlParser {
    private val module = SimpleModule(FlagsDeserializer::class.qualifiedName).addDeserializer(
        Flags::class.java,
        FlagsDeserializer()
    )
    private val mapper: ObjectMapper =
        YAMLMapper.builder().enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .addModule(module)
            .build()
            .registerKotlinModule()
            .registerArrowModule()
    private var schema: JsonSchema

    init {
        KeyboardDataYamlParser::class.java.getResourceAsStream("/schema.json")
            .use { schemaInputStream ->
                val schemaJson = mapper.readTree(schemaInputStream)
                val factory = JsonSchemaFactory.builder(
                    JsonSchemaFactory.getInstance(SpecVersionDetector.detect(schemaJson))
                )
                    .objectMapper(mapper)
                    .build()
                schema = factory.getSchema(schemaJson)
            }
    }

    @JvmStatic
    fun readKeyboardData(inputStream: InputStream?): Either<LayoutError, KeyboardData> {
        return catch({
            validateYaml(mapper.readTree(inputStream))
                .map { layout ->
                    val keyboardData = KeyboardData.info.set(KeyboardData(), layout.info)
                    layout.layers.map { layers ->
                        val keyboardWithHiddenLayer = keyboardData.addAllToActionMap(
                            if (layers.hidden.isNotEmpty()) {
                                addKeyboardActions(layers.hidden)
                            } else {
                                emptyMap()
                            }
                        )
                        if (layers.extraLayers.isNotEmpty() && layers.defaultLayer.isNone()) {
                            keyboardWithHiddenLayer
                        } else {
                            (
                                layers.defaultLayer
                                    .map { LayerLevel.FIRST to it }
                                    .toMap() + layers
                                    .extraLayers
                                    .mapKeys { it.key.toLayerLevel() }
                                )
                                .fold(keyboardWithHiddenLayer) { acc, (layerId, layer) ->
                                    addLayer(acc, layerId, layer)
                                }
                        }
                    }.getOrElse { keyboardData }
                }
        }) { exception: IOException ->
            ExceptionWrapperError(exception).left()
        }
    }

    private fun validateYaml(node: JsonNode): Either<LayoutError, Layout> {
        return schema.validate(node)
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
                    mapper.treeToValue(node, Layout::class.java).right()
                }
            }
    }

    private fun addLayer(
        keyboardData: KeyboardData,
        layer: LayerLevel,
        layerData: Layer
    ): KeyboardData {
        val lowerCaseCharacters = StringBuilder()
        val upperCaseCharacters = StringBuilder()
        val characterSets = (lowerCaseCharacters to upperCaseCharacters)
        return layerData.sectors.fold(keyboardData) { acc, (sector, value) ->
            value.parts.fold(acc) { acc1, (part, actions) ->
                acc1.addAllToActionMap(
                    addKeyboardActions(
                        layer = layer,
                        quadrant = Quadrant(sector, part),
                        actions = actions,
                        characterSets = characterSets
                    )
                )
            }
        }
            .setLowerCaseCharacters(lowerCaseCharacters.toString(), layer)
            .setUpperCaseCharacters(upperCaseCharacters.toString(), layer)
    }

    private fun addKeyboardActions(actions: List<Action>): Map<List<FingerPosition>, KeyboardAction> {
        return actions
            .filterNot { it.movementSequence.isEmpty() }
            .associateBy({ it.movementSequence }, {
                KeyboardAction(
                    it.actionType,
                    it.lowerCase,
                    it.upperCase,
                    it.keyCode,
                    it.flags.value,
                    LayerLevel.HIDDEN
                )
            })
    }

    private fun addKeyboardActions(
        layer: LayerLevel,
        quadrant: Quadrant,
        actions: List<Action?>,
        characterSets: Pair<StringBuilder, StringBuilder>
    ): Map<List<FingerPosition>, KeyboardAction> {
        return actions
            .take(4)
            .withIndex()
            .filterNot { it.value.isEmpty() }
            .fold(emptyMap()) { acc, (i, action) ->
                val characterPosition = CharacterPosition.values()[i]
                var movementSequence: List<FingerPosition> = action!!.movementSequence
                if (movementSequence.isEmpty()) {
                    movementSequence = FingerPosition.computeMovementSequence(
                        layer,
                        quadrant,
                        characterPosition
                    )
                }
                val characterSetIndex: Int =
                    quadrant.characterIndexInString(characterPosition)
                val updatedAction = if (action.lowerCase.isNotEmpty()) {
                    if (characterSets.first.isEmpty()) {
                        characterSets.first.setLength(Constants.CHARACTER_SET_SIZE)
                    }
                    characterSets.first.setCharAt(characterSetIndex, action.lowerCase[0])
                    if (action.upperCase.isEmpty()) {
                        Action.upperCase.set(action, action.lowerCase.uppercase())
                    } else {
                        action
                    }
                } else {
                    action
                }
                if (updatedAction.upperCase.isNotEmpty()) {
                    if (characterSets.second.isEmpty()) {
                        characterSets.second.setLength(Constants.CHARACTER_SET_SIZE)
                    }
                    characterSets.second.setCharAt(
                        characterSetIndex,
                        updatedAction.upperCase[0]
                    )
                }
                val keyboardAction = KeyboardAction(
                    updatedAction.actionType,
                    updatedAction.lowerCase,
                    updatedAction.upperCase,
                    updatedAction.keyCode,
                    updatedAction.flags.value,
                    layer
                )
                val baseActionMap = movementSequence to keyboardAction
                val actionMap = when {
                    layer != LayerLevel.FIRST && action.movementSequence.isEmpty() -> mapOf(
                        baseActionMap,
                        FingerPosition.computeQuickMovementSequence(
                            layer,
                            quadrant,
                            characterPosition
                        ) to keyboardAction
                    )

                    else -> mapOf(baseActionMap)
                }

                acc + actionMap
            }
    }
}
