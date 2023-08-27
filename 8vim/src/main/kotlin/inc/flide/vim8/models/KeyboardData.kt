package inc.flide.vim8.models

import arrow.core.Option
import arrow.core.elementAtOrNone
import arrow.core.fold
import arrow.core.none
import arrow.core.some
import arrow.optics.dsl.index
import arrow.optics.optics
import arrow.optics.typeclasses.Index
import inc.flide.vim8.models.yaml.Layer
import inc.flide.vim8.models.yaml.LayoutInfo

const val CHARACTER_SET_SIZE = 4 * 4 * 2 // 4 sectors, 2 parts, 4 characters per parts

@optics
data class KeyboardData(
    val actionMap: Map<MovementSequence, KeyboardAction> = HashMap(),
    val characterSets: List<CharacterSet> = List(LayerLevel.visibleLayers.size) { CharacterSet() },
    val layoutPositions: Int = 0,
    val sectors: Int = 1,
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

fun KeyboardData.lowerCaseCharacters(layer: LayerLevel): Option<String> {
    return characterSets.elementAtOrNone(layer.ordinal - 1).flatMap {
        if (it.lowerCaseCharacters.isEmpty()) {
            none()
        } else {
            it.lowerCaseCharacters.some()
        }
    }
}

fun KeyboardData.upperCaseCharacters(layer: LayerLevel): Option<String> {
    return characterSets.elementAtOrNone(layer.ordinal - 1).flatMap {
        if (it.upperCaseCharacters.isEmpty()) {
            none()
        } else {
            it.upperCaseCharacters.some()
        }
    }
}

fun KeyboardData.setLowerCaseCharacters(
    lowerCaseCharacters: String,
    layer: LayerLevel
): KeyboardData {
    return KeyboardData.characterSets.index(
        Index.list(),
        layer.ordinal - 1
    ).lowerCaseCharacters.set(
        this,
        lowerCaseCharacters
    )
}

fun KeyboardData.setUpperCaseCharacters(
    upperCaseCharacters: String,
    layer: LayerLevel
): KeyboardData {
    return KeyboardData.characterSets.index(
        Index.list(),
        layer.ordinal - 1
    ).upperCaseCharacters.set(
        this,
        upperCaseCharacters
    )
}

fun KeyboardData.findLayer(movementSequence: MovementSequence): LayerLevel {
    return actionMap[movementSequence]?.layer.let {
        if (it == LayerLevel.HIDDEN) {
            LayerLevel.FIRST
        } else {
            it
        }
    } ?: LayerLevel.FIRST
}

fun KeyboardData.oppositeDirection(direction: Int): Int {
    return ((direction - 1) + sectors / 2) % sectors + 1
}

fun KeyboardData.computeSectorsAndLayouts(layers: Collection<Layer>): KeyboardData {
    val sectorsAndLayouts =
        layers.fold(sectors to layoutPositions) { acc, layer ->
            layer.sectors.fold(acc) { (maxSector, maxLayoutPositions), (sector, value) ->
                val layoutPositions = value.parts.values.maxByOrNull { it.size }?.size ?: 0
                maxSector.coerceAtLeast(sector) to maxLayoutPositions.coerceAtLeast(
                    layoutPositions
                )
            }
        }
    return copy(sectors = sectorsAndLayouts.first, layoutPositions = sectorsAndLayouts.second)
}

fun KeyboardData.characterSetSize(): Int {
    return sectors * 2 * layoutPositions
}
