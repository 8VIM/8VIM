package inc.flide.vim8.ime.keyboard.xpad

import android.content.Context
import android.graphics.Matrix
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import arrow.core.Option
import arrow.core.firstOrNone
import arrow.core.getOrNone
import arrow.core.raise.nullable
import arrow.core.raise.option
import arrow.core.recover
import inc.flide.vim8.AppPrefs
import inc.flide.vim8.appPreferenceModel
import inc.flide.vim8.ime.layout.loadKeyboardData
import inc.flide.vim8.ime.layout.models.CHARACTER_SET_SIZE
import inc.flide.vim8.ime.layout.models.FingerPosition
import inc.flide.vim8.ime.layout.models.KeyboardAction
import inc.flide.vim8.ime.layout.models.KeyboardData
import inc.flide.vim8.ime.layout.models.LayerLevel
import inc.flide.vim8.ime.layout.models.LayerLevel.Companion.MovementSequences
import inc.flide.vim8.ime.layout.models.LayerLevel.Companion.VisibleLayers
import inc.flide.vim8.ime.layout.models.MovementSequence
import inc.flide.vim8.ime.layout.models.MovementSequenceType
import inc.flide.vim8.ime.layout.models.characterSets
import inc.flide.vim8.ime.layout.models.findLayer
import inc.flide.vim8.ime.layout.models.yaml.ExtraLayer
import inc.flide.vim8.ime.layout.safeLoadKeyboardData
import inc.flide.vim8.layoutLoader

class Keyboard(private val context: Context) {
    companion object {
        val extraLayerMovementSequences = ExtraLayer.MOVEMENT_SEQUENCES.values.toSet()
    }

    private val prefs: AppPrefs by appPreferenceModel()
    private val layoutLoader by context.layoutLoader()
    var keyboardData: KeyboardData? by mutableStateOf(null)

    val keys = List(CHARACTER_SET_SIZE) { Key(it, this) }

    var layerLevel: LayerLevel by mutableStateOf(LayerLevel.FIRST)

    init {
        keyboardData = safeLoadKeyboardData(layoutLoader, context)!!
        prefs.layout.current.observe {
            it.loadKeyboardData(layoutLoader, context)
                .onRight { keyboardData ->
                    this.keyboardData = keyboardData
                }
        }
    }

    fun reset() {
        layerLevel = LayerLevel.FIRST
    }

    fun findLayer(movementSequence: MutableList<FingerPosition>) {
        keyboardData?.let {
            for (i in VisibleLayers.size - 1 downTo LayerLevel.SECOND.ordinal) {
                val layerLevel = LayerLevel.entries[i]
                val extraLayerMovementSequence = MovementSequences[layerLevel]
                if (extraLayerMovementSequence == null) {
                    this.layerLevel = LayerLevel.FIRST
                    return
                }
                if (movementSequence.size < extraLayerMovementSequence.size) {
                    continue
                }
                val startWith: MovementSequence =
                    movementSequence.subList(0, extraLayerMovementSequence.size)

                if (extraLayerMovementSequences.contains(startWith) &&
                    layerLevel.ordinal <= it.totalLayers
                ) {
                    this.layerLevel = layerLevel
                    return
                }
            }

            this.layerLevel = it
                .findLayer(movementSequence + FingerPosition.INSIDE_CIRCLE)
        }
    }

    fun key(movementSequence: MovementSequence): Key? = nullable {
        val keyboardData = ensureNotNull(keyboardData)
        val keyboardAction = keyboardData.actionMap.getOrNone(movementSequence).bind()
        val characterSet = keyboardData.characterSets(layerLevel).bind()
        keys.firstOrNone { key ->
            characterSet[key.index] == keyboardAction
        }.bind()
    }

    fun action(
        movementSequence: MovementSequence,
        currentMovementSequenceType: MovementSequenceType
    ): Option<KeyboardAction> = option {
        val keyboardData = ensureNotNull(keyboardData)
        keyboardData.actionMap
            .getOrNone(movementSequence)
            .recover {
                keyboardData.actionMap
                    .getOrNone(listOf(FingerPosition.NO_TOUCH) + movementSequence)
                    .filter { currentMovementSequenceType == MovementSequenceType.NEW_MOVEMENT }
                    .bind()
            }.bind()
    }

    fun hasAction(movementSequence: MovementSequence): Boolean =
        keyboardData?.actionMap?.containsKey(movementSequence) ?: false

    fun layout(
        characterHeight: Float,
        circleCenter: Offset,
        radius: Float,
        lengthOfLineDemarcatingSectors: Float
    ) {
        val letterPositions = List(4 * 2 * 4 * 2) { 0f }.toFloatArray()
        val matrix = Matrix()
        val eastEdge = circleCenter.x + radius + characterHeight / 2f
        for (i in 0 until 4) {
            val dx = i * lengthOfLineDemarcatingSectors / 4f
            letterPositions[4 * i] = eastEdge + dx
            letterPositions[4 * i + 1] = circleCenter.y - characterHeight / 2f
            letterPositions[4 * i + 2] = eastEdge + dx
            letterPositions[4 * i + 3] = circleCenter.y + characterHeight / 2f
        }

        matrix.postRotate(90f, circleCenter.x, circleCenter.y)
        matrix.mapPoints(letterPositions, 0, letterPositions, 0, 8)
        matrix.reset()
        matrix.postRotate(90f, circleCenter.x, circleCenter.y)

        for (i in 1 until 4) {
            matrix.mapPoints(
                letterPositions,
                4 * 4 * i,
                letterPositions,
                4 * 4 * (i - 1),
                8
            )
        }
        matrix.reset()
        matrix.postTranslate(0F, 3 * characterHeight / 16)
        matrix.mapPoints(letterPositions)
        keys.withIndex().forEach { (i, key) ->
            key.position = Offset(letterPositions[i * 2], letterPositions[i * 2 + 1])
            key.characterHeightWidth = characterHeight
        }
    }
}