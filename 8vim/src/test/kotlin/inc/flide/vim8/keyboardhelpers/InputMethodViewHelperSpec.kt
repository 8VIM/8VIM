package inc.flide.vim8.keyboardhelpers

import android.content.res.Configuration
import android.content.res.Resources
import android.util.DisplayMetrics
import inc.flide.vim8.datastore.CachedPreferenceModel
import inc.flide.vim8.datastore.model.PreferenceData
import inc.flide.vim8.models.AppPrefs
import inc.flide.vim8.models.appPreferenceModel
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic

class InputMethodViewHelperSpec : FunSpec({
    val resources: Resources = mockk()
    val configuration = Configuration()

    beforeSpec {
        mockkStatic(::appPreferenceModel)
        val prefs: AppPrefs = mockk()
        val keyboard: AppPrefs.Keyboard = mockk()
        val scale: PreferenceData<Int> = mockk()
        val displayMetrics = DisplayMetrics()

        displayMetrics.heightPixels = 1
        displayMetrics.widthPixels = 5

        every { appPreferenceModel() } returns CachedPreferenceModel(prefs)
        every { prefs.keyboard } returns keyboard
        every { keyboard.height } returns scale
        every { scale.get() } returns 50

        every { resources.displayMetrics } returns displayMetrics
        every { resources.configuration } returns configuration
        every {
            resources.getFraction(
                any(),
                any(),
                any()
            )
        } answers { (it.invocation.args[2] as Int).toFloat() }
        every { resources.getDimension(any()) } returns 1f
    }

    test("compute dimension when it's not in landscape mode") {
        configuration.orientation = Configuration.ORIENTATION_PORTRAIT
        val res = InputMethodViewHelper.computeDimension(resources)
        res.height shouldBe 1
        res.width shouldBe 5
    }

    test("compute dimension when it's in landscape mode") {
        configuration.orientation = Configuration.ORIENTATION_LANDSCAPE
        val res = InputMethodViewHelper.computeDimension(resources)
        res.height shouldBe 0
        res.width shouldBe 5
    }
})
