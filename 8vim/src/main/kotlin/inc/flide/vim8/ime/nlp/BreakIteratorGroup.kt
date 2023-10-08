package inc.flide.vim8.ime.nlp

import android.content.Context
import android.icu.text.BreakIterator
import java.util.Locale

class BreakIteratorGroup(private val context: Context) {
    private val charInstances = hashMapOf<Locale, BreakIterator>()
    private val wordInstances = hashMapOf<Locale, BreakIterator>()
    private val currentLocale: Locale
        get() = context.resources.configuration.locales[0]

    private fun <R> character(locale: Locale, action: (BreakIterator) -> R): R {
        return charInstances.getOrPut(locale) {
            BreakIterator.getCharacterInstance(locale)
        }.let { action(it) }
    }

    private fun <R> word(locale: Locale, action: (BreakIterator) -> R): R {
        return wordInstances.getOrPut(locale) {
            BreakIterator.getWordInstance(locale)
        }.let { action(it) }
    }

    fun measureLastCharacters(text: String, numChars: Int): Int {
        return character(currentLocale) {
            it.setText(text)
            val end = it.last()
            var start: Int
            var n = 0
            do {
                start = it.previous()
            } while (start != BreakIterator.DONE && ++n < numChars)
            end - (if (start == BreakIterator.DONE) 0 else start)
        }.coerceIn(0, text.length)
    }

    fun measureLastWords(text: String, numWords: Int): Int {
        return word(currentLocale) {
            it.setText(text)
            val end = it.last()
            var start: Int
            var n = 0
            do {
                if (it.ruleStatus != BreakIterator.WORD_NONE) n++
                start = it.previous()
            } while (start != BreakIterator.DONE && n < numWords)
            end - (if (start == BreakIterator.DONE) 0 else start)
        }.coerceIn(0, text.length)
    }
}
