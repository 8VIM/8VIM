package inc.flide.vim8.ime

import android.content.res.Resources
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.clearMocks
import io.mockk.mockk

class InputMethodServiceHelperSpec : DescribeSpec({
    val resources = mockk<Resources>()

    afterTest {
        clearMocks(resources)
    }

    describe("InputMethodServiceHelper") {
        it("load a layout from an inputStream") {
//    InputMethodServiceHelper.initializeKeyboardActionMap()
        }
    }
})
