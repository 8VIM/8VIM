package inc.flide.vim8.ime.layout.parsers

import inc.flide.vim8.arbitraries.Arbitraries
import io.kotest.assertions.arrow.core.shouldBeSome
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.property.checkAll
import java.io.File

class CacheParserSpec : FunSpec({
    val file = File.createTempFile("8vim", ".cbor")
    val parser = CborParser()

    context("Saving/loading") {
        test("Success") {
            checkAll(Arbitraries.arbKeyboardData) { keyboardData ->
                parser.save(file, keyboardData).shouldBeTrue()
                parser.load(file) shouldBeSome keyboardData
            }
        }
    }
})
