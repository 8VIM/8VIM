package inc.flide.vim8.ime.nlp

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.icu.text.BreakIterator
import android.os.LocaleList
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.clearStaticMockk
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import java.util.Locale

class BreakIteratorGroupSpec : FunSpec({
    val context = mockk<Context>(relaxed = true)
    val resources = mockk<Resources>(relaxed = true)
    val configuration = mockk<Configuration>()
    val localeList = mockk<LocaleList>()
    val breakIterator = mockk<BreakIterator>(relaxed = true)

    beforeSpec {
        every { context.resources } returns resources
        every { resources.configuration } returns configuration
        every { configuration.locales } returns localeList
        every { localeList.get(0) } returns Locale.US
        mockkStatic(BreakIterator::class)
        every { BreakIterator.getWordInstance(Locale.US) } returns breakIterator
        every { BreakIterator.getCharacterInstance(Locale.US) } returns breakIterator
    }

    context("Measure last words length") {
        val breakIteratorGroup = BreakIteratorGroup(context)
        withData(
            nameFn = {
                val words = it.first.substring(it.third.last())
                "${it.first} -> find ${it.second} word(s): $words"
            },
            Triple("", 1, listOf(0)),
            Triple("a b", 1, listOf(2)),
            Triple("a", 2, listOf(0)),
            Triple("a b cd  ", 2, listOf(-2, 4, 2))
        ) { (text, num, positions) ->
            val words = text.substring(positions.last())
            every { breakIterator.last() } returns text.length
            every { breakIterator.previous() } returnsMany (positions + BreakIterator.DONE)
            every { breakIterator.ruleStatus } returnsMany positions.map {
                if (it < 0) {
                    BreakIterator.WORD_NONE
                } else {
                    BreakIterator.WORD_NUMBER_LIMIT
                }
            }

            val length = breakIteratorGroup.measureLastWords(text, num)
            length shouldBe words.length
            text.substring(text.length - length) shouldBe words
        }
    }

    context("Measure last characters length") {
        val breakIteratorGroup = BreakIteratorGroup(context)
        withData(
            nameFn = {
                val characters = it.first.substring(it.third.last())
                "${it.first} -> find ${it.second} characters(s): $characters"
            },
            Triple("", 1, listOf(0)),
            Triple("a b", 1, listOf(2)),
            Triple("a", 2, listOf(0)),
            Triple("a b cd", 2, listOf(5, 4))
        ) { (text, num, positions) ->
            val characters = text.substring(positions.last())
            every { breakIterator.last() } returns text.length
            every { breakIterator.previous() } returnsMany (positions + BreakIterator.DONE)
            val length = breakIteratorGroup.measureLastCharacters(text, num)
            length shouldBe characters.length
            text.substring(text.length - length) shouldBe characters
        }
    }
    afterTest { clearMocks(breakIterator) }
    afterSpec {
        clearStaticMockk(BreakIterator::class)
    }
})
