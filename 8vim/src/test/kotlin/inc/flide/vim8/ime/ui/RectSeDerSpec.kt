package inc.flide.vim8.ime.ui

import android.content.SharedPreferences
import androidx.compose.ui.geometry.Rect
import inc.flide.vim8.arbitraries.Arbitraries
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.arbitrary.single
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class RectSeDerSpec : FunSpec({
    val key = "key"
    val editor = mockk<SharedPreferences.Editor>(relaxed = true)
    val sharedPreferences = mockk<SharedPreferences>(relaxed = true)

    test("Serialize") {
        val rect = Arbitraries.arbRect.single()
        RectSerDe.serialize(editor, key, rect)

        verify {
            editor.putString(
                key,
                listOf(rect.left, rect.top, rect.right, rect.bottom).joinToString(";")
            )
        }
    }

    context("Deserialize") {
        val rect = Arbitraries.arbRect.single()
        val rectString = listOf(rect.left, rect.top, rect.right, rect.bottom).joinToString(";")

        test("empty key") {
            every { sharedPreferences.getString(key, any()) } returns null
            RectSerDe.deserialize(sharedPreferences, key, Rect.Zero) shouldBe Rect.Zero
        }

        test("successfully") {
            every { sharedPreferences.getString(key, any()) } returns rectString
            RectSerDe.deserialize(sharedPreferences, key, Rect.Zero) shouldBe rect
        }

        test("invalid serialized data") {
            every { sharedPreferences.getString(key, any()) } returns "1"
            RectSerDe.deserialize(sharedPreferences, key, Rect.Zero) shouldBe Rect.Zero
        }

        test("invalid float value") {
            every { sharedPreferences.getString(key, any()) } returns "1;1;1;wrong"
            RectSerDe.deserialize(sharedPreferences, key, Rect.Zero) shouldBe Rect.Zero
        }
    }

    afterTest { clearMocks(sharedPreferences) }
})
