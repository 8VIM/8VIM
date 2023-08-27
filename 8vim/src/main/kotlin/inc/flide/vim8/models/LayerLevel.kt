package inc.flide.vim8.models

enum class LayerLevel {
    HIDDEN, FIRST, SECOND, THIRD, FOURTH, FIFTH, SIXTH;

    companion object {
        @JvmStatic
        val visibleLayers = listOf(*values()).drop(1)

        @JvmStatic
        val movementSequences: Map<LayerLevel, MovementSequence> =
            values().drop(2).fold(mapOf()) { acc, layer ->
                val movementSequence = when (layer) {
                    SECOND -> listOf(
                        FingerPosition.BOTTOM,
                        FingerPosition.INSIDE_CIRCLE,
                        FingerPosition.BOTTOM
                    )

                    THIRD -> acc[SECOND].orEmpty() + FingerPosition.LEFT
                    FOURTH -> acc[THIRD].orEmpty() + FingerPosition.TOP
                    FIFTH -> acc[FOURTH].orEmpty() + FingerPosition.RIGHT
                    SIXTH -> acc[FIFTH].orEmpty() + FingerPosition.BOTTOM
                    else -> emptyList()
                }
                acc + (layer to movementSequence)
            }
    }
}
