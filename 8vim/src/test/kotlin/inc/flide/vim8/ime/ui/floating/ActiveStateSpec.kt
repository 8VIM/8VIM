package inc.flide.vim8.ime.ui.floating

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow

class ActiveStateSpec : FunSpec({
    var state: Boolean

    beforeSpec {
        mockkStatic("kotlinx.coroutines.flow.StateFlowKt")
    }

    beforeTest {
        state = false
        every {
            MutableStateFlow<Boolean>(any())
        } returns mockk {
            every { value = any() } propertyType Boolean::class answers {
                state = value
            }
            every { value } answers { state }
        }
    }

    test("start only once") {
        val activeState = CoroutineActiveState(50.milliseconds)
        activeState.isActive.value.shouldBeFalse()
        activeState.start()
        activeState.isActive.value.shouldBeTrue()
        delay(25)
        activeState.isActive.value.shouldBeTrue()
        delay(26)
        activeState.isActive.value.shouldBeFalse()
    }

    test("stop and restart") {
        val activeState = CoroutineActiveState(20.milliseconds)
        activeState.isActive.value.shouldBeFalse()
        activeState.start()
        activeState.isActive.value.shouldBeTrue()
        delay(5)
        activeState.stop()
        activeState.isActive.value.shouldBeTrue()
        delay(11)
        activeState.isActive.value.shouldBeTrue()
        activeState.start()
        delay(35)
        activeState.isActive.value.shouldBeFalse()
    }
})
