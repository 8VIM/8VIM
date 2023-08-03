package inc.flide.vim8.models.yaml

import inc.flide.vim8.models.FingerPosition

enum class ExtraLayer {
    FIRST, SECOND, THIRD, FOURTH, FIFTH;

    companion object {
        @JvmField
        val MOVEMENT_SEQUENCES = HashMap<ExtraLayer, List<FingerPosition>>()

        init {
            val movementSequence: MutableList<FingerPosition> =
                ArrayList(listOf(FingerPosition.BOTTOM, FingerPosition.INSIDE_CIRCLE))
            for (extraLayer in values()) {
                when (extraLayer) {
                    FIRST, FIFTH -> movementSequence.add(FingerPosition.BOTTOM)
                    SECOND -> movementSequence.add(FingerPosition.LEFT)
                    THIRD -> movementSequence.add(FingerPosition.TOP)
                    FOURTH -> movementSequence.add(FingerPosition.RIGHT)
                }
                MOVEMENT_SEQUENCES[extraLayer] = ArrayList(movementSequence)
            }
        }
    }
}