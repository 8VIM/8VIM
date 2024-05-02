package inc.flide.vim8.ime.layout.parsers

import arrow.core.Either
import inc.flide.vim8.ime.layout.models.KeyboardData
import inc.flide.vim8.ime.layout.models.error.LayoutError
import java.io.InputStream

interface LayoutParser {
    fun readKeyboardData(inputStream: InputStream?): Either<LayoutError, KeyboardData>
}
