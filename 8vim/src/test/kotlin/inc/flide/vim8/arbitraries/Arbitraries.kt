package inc.flide.vim8.arbitraries

import inc.flide.vim8.models.KeyboardAction
import inc.flide.vim8.models.KeyboardActionType
import inc.flide.vim8.models.LayerLevel
import inc.flide.vim8.models.MovementSequence
import inc.flide.vim8.structures.Constants
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string

object Arbitraries {
    val arbMovementSequence: Arb<MovementSequence> = Arb.list(Arb.enum(), 1..10)
    val arbCharactersSet = Arb.string(Constants.CHARACTER_SET_SIZE)
    val arbKeyboardAction: Arb<KeyboardAction> = arbitrary {
        val type = Arb.enum<KeyboardActionType>().bind()
        val lowerCase = arbCharactersSet.bind()
        val upperCase = arbCharactersSet.bind()
        val keyEventCode = Arb.int(-16..16).bind()
        val flags = Arb.int(0..5).bind()
        val layer = Arb.enum<LayerLevel>().bind()
        KeyboardAction(type, lowerCase, upperCase, keyEventCode, flags, layer)
    }
    val arbKeyboardActions = Arb.map(arbMovementSequence, arbKeyboardAction)

}