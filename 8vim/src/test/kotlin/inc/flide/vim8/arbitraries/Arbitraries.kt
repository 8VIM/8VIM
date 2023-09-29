package inc.flide.vim8.arbitraries

import inc.flide.vim8.models.CharacterSet
import inc.flide.vim8.models.EmbeddedLayout
import inc.flide.vim8.models.KeyboardAction
import inc.flide.vim8.models.KeyboardActionType
import inc.flide.vim8.models.KeyboardData
import inc.flide.vim8.models.LayerLevel
import inc.flide.vim8.models.MovementSequence
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.constant
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string

object Arbitraries {
    private val arbMovementSequence: Arb<MovementSequence> = Arb.list(Arb.int(-3, 4), 1..10)
    val arbCharactersSet = Arb.string(16)

    private val arbKeyboardAction: Arb<KeyboardAction> = arbitrary {
        val type = Arb.enum<KeyboardActionType>().bind()
        val lowerCase = arbCharactersSet.bind()
        val upperCase = arbCharactersSet.bind()
        val keyEventCode = Arb.int(-16..16).bind()
        val flags = Arb.int(0..5).bind()
        val layer = Arb.enum<LayerLevel>().bind()
        KeyboardAction(type, lowerCase, upperCase, keyEventCode, flags, layer)
    }

    private val arbCharacterSet: Arb<CharacterSet> = arbitrary {
        CharacterSet(arbCharactersSet.bind(), arbCharactersSet.bind())
    }

    val arbKeyboardData: Arb<KeyboardData> = arbitrary {
        val actionMap = Arb.map(arbMovementSequence, arbKeyboardAction, 1, 20).bind()
        val arbMaybeCharacterSet = Arb.choice(arbCharacterSet, Arb.constant(CharacterSet()))
        val characterSets = Arb.list(arbMaybeCharacterSet, 6..6).bind()
        KeyboardData(actionMap, characterSets)
    }

    val arbEmbeddedLayout: Arb<EmbeddedLayout> = Arb.string(1..10).map { EmbeddedLayout(it) }
}
