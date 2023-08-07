package inc.flide.vim8.models

import arrow.core.Option
import arrow.core.none
import arrow.core.some
import arrow.optics.dsl.index
import arrow.optics.optics
import arrow.optics.typeclasses.Index
import inc.flide.vim8.models.yaml.LayoutInfo

@optics
data class KeyboardData(
    val actionMap: Map<List<FingerPosition>, KeyboardAction> = HashMap(),
    val characterSets: List<CharacterSet> = List(LayerLevel.VisibleLayers.size) { CharacterSet() },
    val info: LayoutInfo = LayoutInfo()
) {
    companion object

    val totalLayers = characterSets
        .indexOfLast { it.isNotEmpty() }
        .let { if (it == -1) 0 else it + 1 }

    fun addAllToActionMap(actionMapAddition: Map<List<FingerPosition>, KeyboardAction>): KeyboardData {
        return KeyboardData.actionMap.modify(this) {
            it + actionMapAddition
        }
    }

    fun lowerCaseCharacters(layer: LayerLevel): Option<String> {
        return Option.fromNullable(
            KeyboardData.characterSets.index(
                Index.list(),
                layer.ordinal - 1
            ).lowerCaseCharacters.getOrNull(
                this
            )
        ).flatMap { if (it.isEmpty()) none() else it.some() }
    }

    fun upperCaseCharacters(layer: LayerLevel): Option<String> {
        return Option.fromNullable(
            KeyboardData.characterSets.index(
                Index.list(),
                layer.ordinal - 1
            ).upperCaseCharacters.getOrNull(
                this
            )
        ).flatMap { if (it.isEmpty()) none() else it.some() }
    }

    fun setLowerCaseCharacters(lowerCaseCharacters: String, layer: LayerLevel): KeyboardData {
        return KeyboardData.characterSets.index(
            Index.list(),
            layer.ordinal - 1
        ).lowerCaseCharacters.set(
            this,
            lowerCaseCharacters
        )
    }

    fun setUpperCaseCharacters(upperCaseCharacters: String, layer: LayerLevel): KeyboardData {
        return KeyboardData.characterSets.index(
            Index.list(),
            layer.ordinal - 1
        ).upperCaseCharacters.set(
            this,
            upperCaseCharacters
        )
    }

    fun findLayer(movementSequence: MovementSequence): LayerLevel {
        return actionMap[movementSequence]?.layer ?: LayerLevel.FIRST
    }

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
