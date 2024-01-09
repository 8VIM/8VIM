package inc.flide.vim8.ime.keyboard.text

import kotlin.math.abs

class Keyboard(private val arrangement: Array<Array<Key>>) {
    val rowCount: Int
        get() = arrangement.size

    fun keys(): Iterator<Key> {
        return KeyboardIterator(arrangement)
    }

    private fun rows(): Iterator<Array<Key>> {
        return arrangement.iterator()
    }

    fun layout(
        keyboardWidth: Float,
        keyboardHeight: Float,
        desiredKey: Key
    ) {
        if (arrangement.isEmpty()) return

        val desiredTouchBounds = desiredKey.touchBounds
        val desiredVisibleBounds = desiredKey.visibleBounds
        if (desiredTouchBounds.isEmpty() || desiredVisibleBounds.isEmpty()) return
        if (keyboardWidth.isNaN() || keyboardHeight.isNaN()) return
        val rowMarginH = abs(desiredTouchBounds.width - desiredVisibleBounds.width)
        val rowMarginV =
            (keyboardHeight - desiredTouchBounds.height * rowCount.toFloat()) / (rowCount - 1).coerceAtLeast(
                1
            ).toFloat()

        for ((r, row) in rows().withIndex()) {
            val posY = (desiredTouchBounds.height + rowMarginV) * r
            val availableWidth = (keyboardWidth - rowMarginH) / desiredTouchBounds.width
            var requestedWidth = 0.0f
            var shrinkSum = 0.0f
            var growSum = 0.0f
            for (key in row) {
                requestedWidth += 1.0f
                shrinkSum += 1.0f
                growSum += 1.0f
            }
            if (requestedWidth <= availableWidth) {
                // Requested with is smaller or equal to the available with, so we can grow
                val additionalWidth = availableWidth - requestedWidth
                var posX = rowMarginH / 2.0f
                for ((k, key) in row.withIndex()) {
                    val keyWidth = desiredTouchBounds.width * when (growSum) {
                        0.0f -> when (k) {
                            0, row.size - 1 -> 1.0f + additionalWidth / 2.0f
                            else -> 1.0f
                        }

                        else -> 1.0f + additionalWidth * (1.0f / growSum)
                    }
                    key.touchBounds.apply {
                        left = posX
                        top = posY
                        right = posX + keyWidth
                        bottom = posY + desiredTouchBounds.height
                    }
                    key.visibleBounds.apply {
                        left =
                            key.touchBounds.left + abs(desiredTouchBounds.left - desiredVisibleBounds.left) + when {
                                growSum == 0.0f && k == 0 -> ((additionalWidth / 2.0f) * desiredTouchBounds.width)
                                else -> 0.0f
                            }
                        top =
                            key.touchBounds.top + abs(desiredTouchBounds.top - desiredVisibleBounds.top)
                        right =
                            key.touchBounds.right - abs(desiredTouchBounds.right - desiredVisibleBounds.right) - when {
                                growSum == 0.0f && k == row.size - 1 -> ((additionalWidth / 2.0f) * desiredTouchBounds.width)
                                else -> 0.0f
                            }
                        bottom =
                            key.touchBounds.bottom - abs(desiredTouchBounds.bottom - desiredVisibleBounds.bottom)
                    }
                    posX += keyWidth
                    // After-adjust touch bounds for the row margin
                    key.touchBounds.apply {
                        if (k == 0) {
                            left = 0.0f
                        } else if (k == row.size - 1) {
                            right = keyboardWidth
                        }
                    }
                }
            } else {
                // Requested size too big, must shrink.
                val clippingWidth = requestedWidth - availableWidth
                var posX = rowMarginH / 2.0f
                for ((k, key) in row.withIndex()) {
                    val keyWidth =
                        desiredTouchBounds.width * (1.0f - clippingWidth * (1.0f / shrinkSum))
                    key.touchBounds.apply {
                        left = posX
                        top = posY
                        right = posX + keyWidth
                        bottom = posY + desiredTouchBounds.height
                    }
                    key.visibleBounds.apply {
                        left =
                            key.touchBounds.left + abs(desiredTouchBounds.left - desiredVisibleBounds.left)
                        top =
                            key.touchBounds.top + abs(desiredTouchBounds.top - desiredVisibleBounds.top)
                        right =
                            key.touchBounds.right - abs(desiredTouchBounds.right - desiredVisibleBounds.right)
                        bottom =
                            key.touchBounds.bottom - abs(desiredTouchBounds.bottom - desiredVisibleBounds.bottom)
                    }
                    posX += keyWidth
                    // After-adjust touch bounds for the row margin
                    key.touchBounds.apply {
                        if (k == 0) {
                            left = 0.0f
                        } else if (k == row.size - 1) {
                            right = keyboardWidth
                        }
                    }
                }
            }

        }
    }

    fun getKeyForPos(pointerX: Float, pointerY: Float): Key? {
        for (key in keys()) {
            if (key.touchBounds.contains(pointerX, pointerY)) {
                return key
            }
        }
        return null
    }

    class KeyboardIterator internal constructor(
        private val arrangement: Array<Array<Key>>
    ) : Iterator<Key> {
        private var rowIndex: Int = 0
        private var keyIndex: Int = 0

        override fun hasNext(): Boolean {
            return rowIndex < arrangement.size && keyIndex < arrangement[rowIndex].size
        }

        override fun next(): Key {
            val next = arrangement[rowIndex][keyIndex]
            if (keyIndex + 1 == arrangement[rowIndex].size) {
                rowIndex++
                keyIndex = 0
            } else {
                keyIndex++
            }
            return next
        }
    }
}
