package inc.flide.vim8.ime

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import inc.flide.vim8.datastore.CachedPreferenceModel
import inc.flide.vim8.datastore.model.PreferenceData
import inc.flide.vim8.models.AppPrefs
import inc.flide.vim8.models.appPreferenceModel
import inc.flide.vim8.theme.ThemeMode
import inc.flide.vim8.theme.darkColorPalette
import inc.flide.vim8.theme.lightColorPalette
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.clearMocks
import io.mockk.clearStaticMockk
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import kotlin.random.Random

class KeyboardThemeSpec : FunSpec({
    val defaultColorScheme = ColorScheme(
        Color.Black,
        Color.Black,
        Color.Black,
        Color.Black,
        Color.Black,
        Color.Black,
        Color.Black,
        Color.Black,
        Color.Black,
        Color.Black,
        Color.Black,
        Color.Black,
        Color.Black,
        Color.Black,
        Color.Black,
        Color.Black,
        Color.Black,
        Color.Black,
        Color.Black,
        Color.Black,
        Color.Black,
        Color.Black,
        Color.Black, Color.Black, Color.Black, Color.Black, Color.Black, Color.Black, Color.Black
    )
    val darkColorScheme = defaultColorScheme.copy(onBackground = Color.White)
    val lightColorScheme = defaultColorScheme.copy(background = Color.White)

    val prefs = mockk<AppPrefs>()
    val themePref = mockk<AppPrefs.Theme>()
    val mode = mockk<PreferenceData<ThemeMode>>()
    val keyboardPref = mockk<AppPrefs.Keyboard>()
    val customColorPref = mockk<AppPrefs.Keyboard.CustomColors>()
    val backgroundColor = mockk<PreferenceData<Int>>()
    val foregroundColor = mockk<PreferenceData<Int>>()
    val trailPref = mockk<AppPrefs.Keyboard.Trail>()
    val useRandomColor = mockk<PreferenceData<Boolean>>()
    val trailColor = mockk<PreferenceData<Int>>()
    val context = mockk<Context>()
    val resources = mockk<Resources>()
    val configuration = Configuration()

    beforeSpec {
        mockkObject(Random)
        mockkStatic(::appPreferenceModel)
        mockkStatic(::darkColorPalette)
        mockkStatic(::lightColorPalette)

        every { darkColorPalette(any()) } returns darkColorScheme
        every { lightColorPalette(any()) } returns lightColorScheme

        every { themePref.mode } returns mode
        every { prefs.theme } returns themePref
        every { prefs.keyboard } returns keyboardPref
        every { keyboardPref.customColors } returns customColorPref

        every { customColorPref.foreground } returns foregroundColor
        every { customColorPref.background } returns backgroundColor
        every { backgroundColor.get() } returns Color.Blue.toArgb()
        every { foregroundColor.get() } returns Color.Red.toArgb()
        every { backgroundColor.observe(any()) } just Runs
        every { foregroundColor.observe(any()) } just Runs

        every { keyboardPref.trail } returns trailPref
        every { trailPref.color } returns trailColor
        every { trailColor.get() } returns Color.Yellow.toArgb()

        every { trailPref.useRandomColor } returns useRandomColor
        every { appPreferenceModel() } returns CachedPreferenceModel(prefs)
        every { context.resources } returns resources
        every { resources.configuration } returns configuration
    }

    beforeTest {
        mockkObject(KeyboardTheme)
        configuration.uiMode = Configuration.UI_MODE_NIGHT_YES
        every { mode.get() } returns ThemeMode.LIGHT
        every { mode.observe(any()) } just Runs
        every { KeyboardTheme.getInstance() } answers { KeyboardTheme(context) }
    }

    context("getting right colors from ThemeMode/UI settings") {
        withData(
            nameFn = {
                val ui = if (it.second == Configuration.UI_MODE_NIGHT_YES) "yes" else "no"
                "${it.first}/$ui"
            },
            Triple(ThemeMode.LIGHT, Configuration.UI_MODE_NIGHT_YES, lightColorScheme),
            Triple(ThemeMode.DARK, Configuration.UI_MODE_NIGHT_YES, darkColorScheme),
            Triple(ThemeMode.SYSTEM, Configuration.UI_MODE_NIGHT_NO, lightColorScheme),
            Triple(ThemeMode.SYSTEM, Configuration.UI_MODE_NIGHT_YES, darkColorScheme),
            Triple(
                ThemeMode.CUSTOM,
                Configuration.UI_MODE_NIGHT_YES,
                defaultColorScheme.copy(background = Color.Blue, onBackground = Color.Red)
            )
        ) { (themeMode, ui, colorScheme) ->
            every { mode.get() } returns themeMode
            configuration.uiMode = ui
            val keyboardTheme = KeyboardTheme.getInstance()
            keyboardTheme.backgroundColor shouldBe colorScheme.background.toArgb()
            keyboardTheme.foregroundColor shouldBe colorScheme.onBackground.toArgb()
        }
    }

    context("getting trailing color") {
        test("using random color") {
            every { Random.nextInt(any()) } returns 0
            every { useRandomColor.get() } returns true
            val keyboardTheme = KeyboardTheme.getInstance()
            keyboardTheme.trailColor shouldBe Color.Black.toArgb()
        }
        test("not using random color") {
            every { useRandomColor.get() } returns false
            val keyboardTheme = KeyboardTheme.getInstance()
            keyboardTheme.trailColor shouldBe Color.Yellow.toArgb()
        }
    }

    afterTest {
        clearMocks(mode, useRandomColor)
    }

    afterSpec {
        clearStaticMockk(Random::class)
    }
})
