package inc.flide.vim8.app

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import br.com.colman.kotest.KotestRunnerAndroid
import inc.flide.vim8.R
import io.kotest.core.spec.style.FunSpec
import org.junit.runner.RunWith

@RunWith(KotestRunnerAndroid::class)
class SettingsScreenTest : FunSpec({
    test("test") {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val compose = createAndroidComposeRule<MainActivity>()
        compose.onNodeWithText(appContext.resources.getString(R.string.setup__enable_ime__open_settings_btn))
            .performClick()
    }

})