package inc.flide.vim8.ime.layout.models

import arrow.core.None
import arrow.core.Option
import arrow.core.elementAtOrNone
import arrow.core.fold
import arrow.optics.dsl.index
import arrow.optics.optics
import arrow.optics.typeclasses.Index
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import inc.flide.vim8.ime.layout.models.yaml.LayoutInfo

const val CHARACTER_SET_SIZE = 4 * 4 * 2 // 4 sectors, 2 parts, 4 characters per parts



@optics
data class KeyboardData(
    @JsonSerialize(keyUsing = MovementSequenceSerializer::class)
    @JsonDeserialize(keyUsing = MovementSequenceDeserializer::class)
    val actionMap: Map<MovementSequence, KeyboardAction> = HashMap(),
    val characterSets: List<List<KeyboardAction?>> =
        List(LayerLevel.VisibleLayers.size) { emptyList() },
    val info: LayoutInfo = LayoutInfo()
) {
    companion object

    val totalLayers = characterSets
        .indexOfLast { it.isNotEmpty() }
        .let { if (it == -1) 0 else it + 1 }

    override fun toString(): String {
        val sb = StringBuilder(info.name)
        if (totalLayers > 1) {
            sb.append(" (")
            sb.append(totalLayers)
            sb.append(" layers)")
        }
        return sb.toString()
    }
}

fun KeyboardData.addAllToActionMap(
    actionMapAddition: Map<MovementSequence, KeyboardAction>
): KeyboardData {
    return KeyboardData.actionMap.modify(this) {
        it + actionMapAddition
    }
}

fun KeyboardData.characterSets(layer: LayerLevel): Option<List<KeyboardAction?>> =
    characterSets.elementAtOrNone(layer.ordinal - 1)
        .filter { it.isNotEmpty() && it.any { action -> action != null } }

fun KeyboardData.setCharacterSets(
    characterSets: List<KeyboardAction?>,
    layer: LayerLevel
): KeyboardData =
    KeyboardData.characterSets.index(
        Index.list(),
        layer.ordinal - 1
    ).set(this, characterSets)

fun KeyboardData.findLayer(movementSequence: MovementSequence): LayerLevel =
    actionMap[movementSequence]?.layer.let {
        if (it == LayerLevel.HIDDEN) {
            LayerLevel.FIRST
        } else {
            it
        }
    } ?: LayerLevel.FIRST
