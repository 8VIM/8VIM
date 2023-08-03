package inc.flide.vim8.models

import arrow.core.Option
import arrow.core.none
import arrow.core.some
import arrow.optics.dsl.index
import arrow.optics.optics
import arrow.optics.typeclasses.Index
import inc.flide.vim8.models.yaml.LayoutInfo
import inc.flide.vim8.structures.Constants
import inc.flide.vim8.structures.actionMap
import inc.flide.vim8.structures.characterSets
import inc.flide.vim8.structures.lowerCaseCharacters
import inc.flide.vim8.structures.upperCaseCharacters

@optics
data class KeyboardData(
    val actionMap: Map<List<FingerPosition>, KeyboardAction> = HashMap(),
    val characterSets: List<CharacterSet> = List(Constants.MAX_LAYERS) { CharacterSet() },
    val info: LayoutInfo = LayoutInfo()
) {
    companion object

    val totalLayers = characterSets
        .indexOfLast { it.isNotEmpty() }
        .let { if (it == -1) 0 else it }

    fun addAllToActionMap(actionMapAddition: Map<List<FingerPosition>, KeyboardAction>): KeyboardData {
        return KeyboardData.actionMap.modify(this) {
            it + actionMapAddition
        }
    }

    fun lowerCaseCharacters(layer: Int): Option<String> {
        return Option.fromNullable(
            KeyboardData.characterSets.index(Index.list(), layer).lowerCaseCharacters.getOrNull(
                this
            )
        ).flatMap { if (it.isEmpty()) none() else it.some() }
    }

    fun upperCaseCharacters(layer: Int): Option<String> {
        return Option.fromNullable(
            KeyboardData.characterSets.index(Index.list(), layer).upperCaseCharacters.getOrNull(
                this
            )
        ).flatMap { if (it.isEmpty()) none() else it.some() }
    }

    fun setLowerCaseCharacters(lowerCaseCharacters: String, layer: Int): KeyboardData {
        return KeyboardData.characterSets.index(Index.list(), layer).lowerCaseCharacters.set(
            this,
            lowerCaseCharacters
        )
    }

    fun setUpperCaseCharacters(upperCaseCharacters: String, layer: Int): KeyboardData {
        return KeyboardData.characterSets.index(Index.list(), layer).upperCaseCharacters.set(
            this,
            upperCaseCharacters
        )
    }

    fun findLayer(movementSequence: List<FingerPosition>): Int {
        return actionMap[movementSequence]?.layer ?: Constants.DEFAULT_LAYER
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


