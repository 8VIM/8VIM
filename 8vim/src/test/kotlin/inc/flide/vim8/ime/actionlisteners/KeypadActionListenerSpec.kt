package inc.flide.vim8.ime.actionlisteners

import android.content.Context
import android.media.AudioManager
import android.view.KeyEvent
import android.view.View
import inc.flide.vim8.MainInputMethodService
import inc.flide.vim8.datastore.CachedPreferenceModel
import inc.flide.vim8.datastore.model.PreferenceData
import inc.flide.vim8.lib.android.AndroidVersion
import inc.flide.vim8.models.AppPrefs
import inc.flide.vim8.models.appPreferenceModel
import io.kotest.core.spec.style.FunSpec
import io.mockk.clearMocks
import io.mockk.clearStaticMockk
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.spyk

class KeypadActionListenerSpec : FunSpec({
    val prefs = mockk<AppPrefs>()
    val inputFeedback = mockk<AppPrefs.InputFeedback>()
    val hapticEnabled = mockk<PreferenceData<Boolean>>()
    val soundEnabled = mockk<PreferenceData<Boolean>>()
    val context = mockk<Context>()
    val view = mockk<View>()
    val audioManager = mockk<AudioManager>(relaxed = true)
    val mainInputMethodService = mockk<MainInputMethodService>(relaxed = true)

    beforeSpec {
        mockkStatic(::appPreferenceModel)
        mockkObject(AndroidVersion)

        every { prefs.inputFeedback } returns inputFeedback
        every { inputFeedback.soundEnabled } returns soundEnabled
        every { inputFeedback.hapticEnabled } returns hapticEnabled

        every { view.context } returns context
        every { context.getSystemService(Context.AUDIO_SERVICE) } returns audioManager
        every { appPreferenceModel() } returns CachedPreferenceModel(prefs)
    }

    test("test") {
        every { AndroidVersion.ATLEAST_API29_Q } returns true
        every { hapticEnabled.get() } returns false
        every { soundEnabled.get() } returns false
        val listener = spyk(object : KeypadActionListener(mainInputMethodService, view) {})
        listener.handleInputKey(KeyEvent.KEYCODE_0, 0)
    }

    afterTest {
        clearStaticMockk(AndroidVersion::class)
        clearMocks(hapticEnabled, soundEnabled)
    }
})
