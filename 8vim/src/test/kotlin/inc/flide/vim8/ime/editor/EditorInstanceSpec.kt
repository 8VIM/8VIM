package inc.flide.vim8.ime.editor

import android.content.Context
import android.text.InputType
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.ExtractedText
import android.view.inputmethod.InputConnection
import inc.flide.vim8.Vim8ImeService
import inc.flide.vim8.ime.input.ImeUiMode
import inc.flide.vim8.ime.input.KeyVariation
import inc.flide.vim8.ime.keyboard.text.ObservableKeyboardState
import inc.flide.vim8.ime.nlp.BreakIteratorGroup
import inc.flide.vim8.keyboardManager
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.property.Arb
import io.kotest.property.Exhaustive
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.enum
import io.mockk.OfTypeMatcher
import io.mockk.clearConstructorMockk
import io.mockk.clearStaticMockk
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkObject
import io.mockk.verify
import java.util.Locale

class EditorInstanceSpec : FunSpec({
    lateinit var context: Context
    lateinit var inputConnection: InputConnection
    lateinit var keyboardState: ObservableKeyboardState

    beforeSpec {
        mockkStatic(Context::keyboardManager)
        mockkObject(Vim8ImeService)
        mockkConstructor(BreakIteratorGroup::class)

        context = mockk {
            every { resources } returns mockk {
                every { configuration } returns mockk {
                    every { locales } returns mockk {
                        every { get(any<Int>()) } returns Locale.ROOT
                    }
                }
            }
            every { keyboardManager() } returns lazy {
                mockk(relaxed = true) {
                    every { activeState } answers { keyboardState }
                }
            }
        }

        every { Vim8ImeService.currentInputConnection() } answers { inputConnection }

        every {
            constructedWith<BreakIteratorGroup>(OfTypeMatcher<Context>(Context::class))
                .measureLastWords(any(), any())
        } returns 2
        every {
            constructedWith<BreakIteratorGroup>(OfTypeMatcher<Context>(Context::class))
                .measureLastCharacters(any(), any())
        } returns 3
    }

    beforeTest {
        inputConnection = mockk<InputConnection>(relaxed = true)
        keyboardState = mockk<ObservableKeyboardState>(relaxed = true)
    }

    context("handleStartInputView") {
        withData(
            nameFn = { it.first.second },
            (InputType.TYPE_CLASS_NUMBER to "TYPE_CLASS_NUMBER") to ImeUiMode.NUMERIC,
            (InputType.TYPE_CLASS_PHONE to "TYPE_CLASS_PHONE") to ImeUiMode.NUMERIC,
            (InputType.TYPE_CLASS_DATETIME to "TYPE_CLASS_DATETIME") to ImeUiMode.NUMERIC,
            (InputType.TYPE_CLASS_TEXT to "TYPE_CLASS_TEXT") to ImeUiMode.TEXT,
            (InputType.TYPE_NULL to "TYPE_NULL") to ImeUiMode.TEXT
        ) { (typeClass, mode) ->
            withData(
                nameFn = { it.first.second },
                (InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD to "TYPE_TEXT_VARIATION_WEB_PASSWORD") to KeyVariation.PASSWORD, // ktlint-disable standard_max-line-length
                (InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD to "TYPE_TEXT_VARIATION_VISIBLE_PASSWORD") to KeyVariation.PASSWORD, // ktlint-disable standard_max-line-length
                (InputType.TYPE_TEXT_VARIATION_PASSWORD to "TYPE_TEXT_VARIATION_PASSWORD") to KeyVariation.PASSWORD, // ktlint-disable standard_max-line-length
                (InputType.TYPE_TEXT_VARIATION_NORMAL to "TYPE_TEXT_VARIATION_NORMAL") to KeyVariation.NORMAL // ktlint-disable standard_max-line-length
            ) { (typeVariation, variation) ->
                val editorInfo = EditorInfo()
                editorInfo.inputType = (typeClass.first or typeVariation.first)
                val editorInstance = EditorInstance(context)

                every {
                    keyboardState.imeUiMode = any()
                } propertyType ImeUiMode::class answers { value }
                every {
                    keyboardState.keyVariation = any()
                } propertyType KeyVariation::class answers { value }

                editorInstance.handleStartInputView(editorInfo)
                val keyVariation = if (typeClass.first == InputType.TYPE_CLASS_TEXT) {
                    variation
                } else {
                    KeyVariation.NORMAL
                }
                verify { keyboardState setProperty "imeUiMode" value mode }
                verify { keyboardState setProperty "keyVariation" value keyVariation }
            }
        }
    }

    test("commitText") {
        checkAll(Arb.string(4)) { text ->
            every { inputConnection.commitText(any(), any()) } returns true
            val editorInstance = EditorInstance(context)
            editorInstance.commitText(text)
            verify { inputConnection.commitText(eq(text), eq(1)) }
        }
    }

    test("performEnterAction") {
        checkAll(Exhaustive.enum<ImeOptions.Action>()) { action ->
            every { inputConnection.performEditorAction(any()) } returns true
            val editorInstance = EditorInstance(context)
            editorInstance.performEnterAction(action)
            verify { inputConnection.performEditorAction(eq(action.toInt())) }
        }
    }

    test("performCut") {
        every { inputConnection.performContextMenuAction(any()) } returns true
        val editorInstance = EditorInstance(context)
        editorInstance.performCut()
        verify { inputConnection.performContextMenuAction(eq(android.R.id.cut)) }
    }

    test("performCopy") {
        every { inputConnection.performContextMenuAction(any()) } returns true
        val editorInstance = EditorInstance(context)
        editorInstance.performCopy()
        verify { inputConnection.performContextMenuAction(eq(android.R.id.copy)) }
    }

    test("performPaste") {
        every { inputConnection.performContextMenuAction(any()) } returns true
        val editorInstance = EditorInstance(context)
        editorInstance.performPaste()
        verify { inputConnection.performContextMenuAction(eq(android.R.id.paste)) }
    }

    test("performEnter") {
        val editorInstance = mockk<EditorInstance>()
        every { editorInstance.sendDownAndUpKeyEvent(any(), any()) } returns true
        every { editorInstance.performEnter() } answers { callOriginal() }
        editorInstance.performEnter()
        verify { editorInstance.sendDownAndUpKeyEvent(eq(KeyEvent.KEYCODE_ENTER), eq(0)) }
    }

    context("performDelete") {
        val editorInstance = EditorInstance(context)
        val extractedText = ExtractedText()
        extractedText.text = ""

        every { inputConnection.deleteSurroundingText(any(), any()) } returns true

        test("Empty selected text") {
            every { inputConnection.getSelectedText(any()) } returns "text"
            every { inputConnection.commitText(any(), any()) } returns true
            editorInstance.performDelete()
            verify { inputConnection.commitText(eq(""), eq(0)) }
        }
        withData(
            nameFn = {
                "getExtractedText is ${if (!it.first) "not" else ""} null, ctrl: ${it.second}"
            },
            Triple(true, false, 1),
            Triple(false, true, 2),
            Triple(false, false, 3)
        ) { (isNull, isCtrl, length) ->
            every { inputConnection.getSelectedText(any()) } returns null
            every {
                inputConnection.getExtractedText(any(), any())
            } returns if (isNull) null else extractedText
            every { keyboardState.isCtrlOn } returns isCtrl
            editorInstance.performDelete()
            if (!isNull) {
                if (isCtrl) {
                    verify {
                        constructedWith<BreakIteratorGroup>(OfTypeMatcher<Context>(Context::class))
                            .measureLastWords(any(), any())
                    }
                } else {
                    verify {
                        constructedWith<BreakIteratorGroup>(OfTypeMatcher<Context>(Context::class))
                            .measureLastCharacters(any(), any())
                    }
                }
            }
            verify { inputConnection.deleteSurroundingText(eq(length), eq(0)) }
        }
    }

    test("performSwitchAnchor") {
        val extractedText = ExtractedText()
        extractedText.selectionStart = 0
        extractedText.selectionEnd = 1
        every { inputConnection.getExtractedText(any(), any()) } returns extractedText
        every { inputConnection.setSelection(any(), any()) } returns true
        val editorInstance = EditorInstance(context)
        editorInstance.performSwitchAnchor()
        verify { inputConnection.setSelection(1, 0) }
    }

    test("sendDownAndUpKeyEvent") {
        val editorInstance = EditorInstance(context)
        every { inputConnection.sendKeyEvent(any()) } returns true
        editorInstance.sendDownAndUpKeyEvent(0, 0)
        verify(exactly = 2) { inputConnection.sendKeyEvent(any()) }
    }

    afterSpec {
        clearStaticMockk(Context::class)
        clearConstructorMockk(BreakIteratorGroup::class)
        unmockkObject(Vim8ImeService)
    }
})
