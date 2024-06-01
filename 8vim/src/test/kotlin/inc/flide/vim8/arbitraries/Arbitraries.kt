package inc.flide.vim8.arbitraries

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import inc.flide.vim8.ime.keyboard.xpad.gestures.GlideGesture
import inc.flide.vim8.ime.layout.EmbeddedLayout
import inc.flide.vim8.ime.layout.models.CHARACTER_SET_SIZE
import inc.flide.vim8.ime.layout.models.KeyboardAction
import inc.flide.vim8.ime.layout.models.KeyboardActionType
import inc.flide.vim8.ime.layout.models.KeyboardData
import inc.flide.vim8.ime.layout.models.LayerLevel
import inc.flide.vim8.ime.layout.models.MovementSequence
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.float
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string

object Arbitraries {
    private val arbMovementSequence: Arb<MovementSequence> = Arb.list(Arb.enum(), 1..10)

    val arbKeyboardAction: Arb<KeyboardAction> = arbitrary {
        val type = Arb.enum<KeyboardActionType>().bind()
        val lowerCase = Arb.string().bind()
        val upperCase = Arb.string().bind()
        val keyEventCode = Arb.int(-16..16).bind()
        val flags = Arb.int(0..5).bind()
        val layer = Arb.enum<LayerLevel>().bind()
        KeyboardAction(type, lowerCase, upperCase, keyEventCode, flags, layer)
    }

    val arbCharactersSet = Arb.list(arbKeyboardAction.orNull(), 0..CHARACTER_SET_SIZE)

    val arbKeyboardData: Arb<KeyboardData> = arbitrary {
        val actionMap = Arb.map(arbMovementSequence, arbKeyboardAction, 1, 20).bind()
        val characterSets = Arb.list(arbCharactersSet, 6..6).bind()
        KeyboardData(actionMap, characterSets)
    }

    val arbEmbeddedLayout: Arb<EmbeddedLayout> = Arb.string(1..10).map { EmbeddedLayout(it) }

    val arbPoint: Arb<GlideGesture.Point> = arbitrary {
        val radius = Arb.float(1f, 14f).bind()
        val x = Arb.float().bind()
        val y = Arb.float().bind()
        GlideGesture.Point(radius, Offset(x, y))
    }

    val arbRect: Arb<Rect> = arbitrary {
        val left = Arb.float().bind()
        val top = Arb.float().bind()
        val right = Arb.float().bind()
        val bottom = Arb.float().bind()
        Rect(left, top, right, bottom)
    }
}
