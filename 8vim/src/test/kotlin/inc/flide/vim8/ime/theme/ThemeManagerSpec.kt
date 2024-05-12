package inc.flide.vim8.ime.theme

import android.content.Context
import android.content.res.Configuration
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.MutableLiveData
import inc.flide.vim8.appPreferenceModel
import inc.flide.vim8.datastore.CachedPreferenceModel
import inc.flide.vim8.datastore.model.PreferenceData
import inc.flide.vim8.datastore.model.PreferenceObserver
import inc.flide.vim8.lib.android.isDarkTheme
import inc.flide.vim8.theme.ThemeMode
import inc.flide.vim8.theme.darkColorPalette
import inc.flide.vim8.theme.lightColorPalette
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import io.kotest.property.Exhaustive
import io.kotest.property.exhaustive.boolean
import io.kotest.property.exhaustive.enum
import io.mockk.clearConstructorMockk
import io.mockk.clearStaticMockk
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.verify
import kotlin.random.Random

class ThemeManagerSpec : FunSpec({
    lateinit var context: Context
    lateinit var androidConfiguration: Configuration
    lateinit var backgroundPref: PreferenceData<Int>
    lateinit var foregroundPref: PreferenceData<Int>
    lateinit var trailColorPref: PreferenceData<Int>
    lateinit var useRandomColorPref: PreferenceData<Boolean>
    lateinit var modePref: PreferenceData<ThemeMode>
    lateinit var backgroundObserver: PreferenceObserver<Int>
    lateinit var foregroundObserver: PreferenceObserver<Int>
    lateinit var trailColorObserver: PreferenceObserver<Int>
    lateinit var useRandomColorObserver: PreferenceObserver<Boolean>
    lateinit var modeObserver: PreferenceObserver<ThemeMode>
    lateinit var random: Random

    val lightColorScheme = lightColorScheme()
    val darkColorScheme = darkColorScheme()

    beforeSpec {
        mockkStatic(::appPreferenceModel)
        mockkStatic(::darkColorPalette)
        mockkStatic(::lightColorPalette)
        mockkStatic(Configuration::isDarkTheme)
        mockkConstructor(MutableLiveData::class)

        random = mockk {
            every { nextInt(any()) } returns 0
        }

        every { darkColorPalette(any()) } returns darkColorScheme
        every { lightColorPalette(any()) } returns lightColorScheme

        context = mockk {
            every { resources } returns mockk {
                every { configuration } answers { androidConfiguration }
            }
        }

        every { appPreferenceModel() } returns CachedPreferenceModel(
            mockk {
                every { theme } returns mockk {
                    every { mode } answers { modePref }
                }

                every { keyboard } returns mockk {
                    every { customColors } returns mockk {
                        every { background } answers { backgroundPref }
                        every { foreground } answers { foregroundPref }
                        every { trail } returns mockk {
                            every { color } answers { trailColorPref }
                            every { useRandomColor } answers { useRandomColorPref }
                        }
                        every { background } answers { backgroundPref }
                    }
                }
            }
        )

        var themeInfoData: ThemeManager.ThemeInfo =
            ThemeManager.ThemeInfo(lightColorScheme, ThemeManager.RandomTrailColor())

        every {
            anyConstructed<MutableLiveData<ThemeManager.ThemeInfo>>().value
        } answers { themeInfoData }

        every {
            anyConstructed<MutableLiveData<ThemeManager.ThemeInfo>>().value = any()
        } propertyType ThemeManager.ThemeInfo::class answers { themeInfoData = value }

        every {
            anyConstructed<MutableLiveData<ThemeManager.ThemeInfo>>().postValue(any())
        } answers { themeInfoData = firstArg() }
    }

    beforeTest {
        androidConfiguration = mockk(relaxed = true)
        backgroundPref = mockk(relaxed = true) {
            every { get() } returns Color.White.toArgb()
            every { observe(any()) } answers { backgroundObserver = firstArg() }
        }
        foregroundPref = mockk(relaxed = true) {
            every { get() } returns Color.Black.toArgb()
            every { observe(any()) } answers { foregroundObserver = firstArg() }
        }
        trailColorPref = mockk(relaxed = true) {
            every { get() } returns Color.Blue.toArgb()
            every { observe(any()) } answers { trailColorObserver = firstArg() }
        }
        useRandomColorPref = mockk(relaxed = true) {
            every { get() } returns true
            every { observe(any()) } answers { useRandomColorObserver = firstArg() }
        }
        modePref = mockk(relaxed = true) {
            every { get() } returns ThemeMode.SYSTEM
            every { observe(any()) } answers { modeObserver = firstArg() }
        }
        every { androidConfiguration.isDarkTheme() } returns false
    }

    test("RandomTrailColor") {
        val randomTrailColor = ThemeManager.RandomTrailColor(random)
        randomTrailColor.color() shouldBe Color.Black
    }

    context("Initialize color mode") {
        withData(
            nameFn = { "System dark mode: $it" },
            Exhaustive.boolean().values
        ) { darkMode ->
            withData(Exhaustive.enum<ThemeMode>().values) { mode ->
                every { modePref.get() } returns mode
                every { androidConfiguration.isDarkTheme() } returns darkMode
                val manager = ThemeManager(context)
                val scheme = when {
                    mode == ThemeMode.CUSTOM -> {
                        (if (darkMode)darkColorScheme else lightColorScheme)
                            .copy(
                                surface = Color.White,
                                onSurface = Color.Black
                            )
                    }

                    mode == ThemeMode.DARK ||
                        (mode == ThemeMode.SYSTEM && darkMode) -> darkColorScheme

                    else -> lightColorScheme
                }
                manager.currentTheme.value?.scheme?.shouldBeEqualToComparingFields(scheme)
            }
        }
    }

    context("Initialize trail color") {
        withData(
            nameFn = { "Random color: $it" },
            Exhaustive.boolean().values
        ) { random ->
            every { useRandomColorPref.get() } returns random
            val manager = ThemeManager(context)
            if (random) {
                manager.currentTheme.value
                    ?.trailColor?.shouldBeTypeOf<ThemeManager.RandomTrailColor>()
            } else {
                manager.currentTheme.value
                    ?.trailColor?.shouldBeTypeOf<ThemeManager.FixedTrailColor> {
                        it.color() shouldBe Color.Blue
                    }
            }
        }
    }

    test("Observe change") {
        ThemeManager(context)
        backgroundObserver.onChanged(1)
        foregroundObserver.onChanged(1)
        trailColorObserver.onChanged(1)
        useRandomColorObserver.onChanged(true)
        modeObserver.onChanged(ThemeMode.SYSTEM)
        verify(exactly = 5) {
            anyConstructed<MutableLiveData<ThemeManager.ThemeInfo>>().postValue(
                any()
            )
        }
    }

    afterSpec {
        clearConstructorMockk(MutableLiveData::class)
        clearStaticMockk(Configuration::class)
    }
})
