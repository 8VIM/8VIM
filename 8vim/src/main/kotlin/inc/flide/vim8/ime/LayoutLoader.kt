package inc.flide.vim8.ime

import android.content.Context
import arrow.core.Either
import arrow.core.firstOrNone
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.raise.catch
import arrow.core.raise.either
import inc.flide.vim8.R
import inc.flide.vim8.ime.layout.Cache
import inc.flide.vim8.ime.layout.models.KeyboardAction
import inc.flide.vim8.ime.layout.models.KeyboardData
import inc.flide.vim8.ime.layout.models.LayerLevel
import inc.flide.vim8.ime.layout.models.MovementSequence
import inc.flide.vim8.ime.layout.models.addAllToActionMap
import inc.flide.vim8.ime.layout.models.characterSets
import inc.flide.vim8.ime.layout.models.error.ExceptionWrapperError
import inc.flide.vim8.ime.layout.models.error.LayoutError
import inc.flide.vim8.ime.layout.models.info
import inc.flide.vim8.ime.layout.models.setCharacterSets
import inc.flide.vim8.ime.layout.parsers.LayoutParser
import java.io.InputStream

interface LayoutLoader {
    fun loadKeyboardData(inputStream: InputStream): Either<LayoutError, KeyboardData>
}

class YamlLayoutLoader(
    private val layoutParser: LayoutParser,
    private val cache: Cache,
    private val context: Context
) :
    LayoutLoader {
    private var layoutIndependentKeyboardData: KeyboardData? = null

    private fun validateNoConflictingActions(
        mainKeyboardActionsMap: Map<MovementSequence, KeyboardAction>?,
        newKeyboardActionsMap: Map<MovementSequence, KeyboardAction>
    ): Boolean {
        return mainKeyboardActionsMap.isNullOrEmpty() || newKeyboardActionsMap.keys.firstOrNone {
            mainKeyboardActionsMap.containsKey(
                it
            )
        }
            .isNone()
    }

    override fun loadKeyboardData(inputStream: InputStream): Either<LayoutError, KeyboardData> =
        either {
            val mainKeyboardData = getLayoutIndependentKeyboardData()
            loadKeyboardData(mainKeyboardData, inputStream).bind()
        }

    private fun loadKeyboardData(
        keyboardData: KeyboardData,
        inputStream: InputStream
    ): Either<LayoutError, KeyboardData> =
        layoutParser.readKeyboardData(inputStream).map { tempKeyboardData ->
            val tempKeyboardDataActionMap = tempKeyboardData.actionMap
            KeyboardData.info
                .set(keyboardData, tempKeyboardData.info)
                .addAllToActionMap(
                    if (validateNoConflictingActions(
                            keyboardData.actionMap,
                            tempKeyboardDataActionMap
                        )
                    ) {
                        tempKeyboardDataActionMap
                    } else {
                        emptyMap()
                    }
                ).let {
                    LayerLevel.VisibleLayers.fold(it) { acc, layer ->
                        val characterSets =
                            tempKeyboardData.characterSets(layer).map { characterSets ->
                                acc.characterSets(layer).getOrElse { characterSets }
                            }.getOrElse { emptyList() }
                        acc.setCharacterSets(characterSets, layer)
                    }
                }
        }

    private fun loadKeyboardData(
        keyboardData: KeyboardData,
        resourceId: Int
    ): Either<LayoutError, KeyboardData> {
        catch({
            return context.resources.openRawResource(resourceId).use { inputStream ->
                loadKeyboardData(keyboardData, inputStream)
            }
        }) { exception: Throwable ->
            return ExceptionWrapperError(exception = exception).left()
        }
    }

    private fun getLayoutIndependentKeyboardData(): KeyboardData {
        if (layoutIndependentKeyboardData == null) {
            layoutIndependentKeyboardData = cache.load("common").getOrElse {
                either {
                    val sectorCircleButtonsKeyboard = loadKeyboardData(
                        KeyboardData(),
                        R.raw.sector_circle_buttons
                    ).bind()
                    val dPadActionKeyboard = loadKeyboardData(
                        sectorCircleButtonsKeyboard,
                        R.raw.d_pad_actions
                    ).bind()
                    loadKeyboardData(
                        dPadActionKeyboard,
                        R.raw.special_core_gestures
                    ).bind()
                }
                    .onLeft {
                        if (it is ExceptionWrapperError) {
                            it.exception.printStackTrace()
                        }
                    }
                    .getOrNull()?.also { cache.add("common", it) }
            }
        }
        return layoutIndependentKeyboardData!!
    }
}
