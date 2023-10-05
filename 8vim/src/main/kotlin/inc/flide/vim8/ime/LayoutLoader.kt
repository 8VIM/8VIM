package inc.flide.vim8.ime

import android.content.res.Resources
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
import inc.flide.vim8.ime.layout.models.error.ExceptionWrapperError
import inc.flide.vim8.ime.layout.models.error.LayoutError
import inc.flide.vim8.ime.layout.models.info
import inc.flide.vim8.ime.layout.models.lowerCaseCharacters
import inc.flide.vim8.ime.layout.models.setLowerCaseCharacters
import inc.flide.vim8.ime.layout.models.setUpperCaseCharacters
import inc.flide.vim8.ime.layout.models.upperCaseCharacters
import inc.flide.vim8.ime.parsers.Yaml.readKeyboardData
import java.io.InputStream

object LayoutLoader {
    internal var layoutIndependentKeyboardData: KeyboardData? = null

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

    fun loadKeyboardData(
        resources: Resources,
        inputStream: InputStream
    ): Either<LayoutError, KeyboardData> = either {
        val mainKeyboardData = getLayoutIndependentKeyboardData(resources)
        loadKeyboardData(mainKeyboardData, inputStream).bind()
    }

    private fun loadKeyboardData(
        keyboardData: KeyboardData,
        inputStream: InputStream
    ): Either<LayoutError, KeyboardData> {
        return readKeyboardData(inputStream).map { tempKeyboardData ->
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
                        val lowerCase =
                            tempKeyboardData.lowerCaseCharacters(layer).map { characterSets ->
                                acc.lowerCaseCharacters(layer).getOrElse { characterSets }
                            }.getOrNull().orEmpty()
                        val upperCase =
                            tempKeyboardData.upperCaseCharacters(layer).map { characterSets ->
                                acc.upperCaseCharacters(layer).getOrElse { characterSets }
                            }.getOrNull().orEmpty()
                        acc.setLowerCaseCharacters(lowerCase, layer)
                            .setUpperCaseCharacters(upperCase, layer)
                    }
                }
        }
    }

    private fun loadKeyboardData(
        keyboardData: KeyboardData,
        resources: Resources,
        resourceId: Int
    ): Either<LayoutError, KeyboardData> {
        catch({
            return resources.openRawResource(resourceId).use { inputStream ->
                loadKeyboardData(keyboardData, inputStream)
            }
        }) { exception: Throwable ->
            return ExceptionWrapperError(exception = exception).left()
        }
    }

    private fun getLayoutIndependentKeyboardData(resources: Resources): KeyboardData {
        if (layoutIndependentKeyboardData == null) {
            val cache = Cache.instance
            layoutIndependentKeyboardData = cache.load("common").getOrElse {
                either {
                    val sectorCircleButtonsKeyboard = loadKeyboardData(
                        KeyboardData(),
                        resources,
                        R.raw.sector_circle_buttons
                    ).bind()
                    val dPadActionKeyboard = loadKeyboardData(
                        sectorCircleButtonsKeyboard,
                        resources,
                        R.raw.d_pad_actions
                    ).bind()
                    loadKeyboardData(
                        dPadActionKeyboard,
                        resources,
                        R.raw.special_core_gestures
                    ).bind()
                }.getOrNull()?.also { cache.add("common", it) }
            }
        }
        return layoutIndependentKeyboardData!!
    }
}
