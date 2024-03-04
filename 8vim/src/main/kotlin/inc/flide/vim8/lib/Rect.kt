package inc.flide.vim8.lib

import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntOffset

class Rect private constructor(
    var left: Float,
    var top: Float,
    var right: Float,
    var bottom: Float
) {
    companion object {
        fun empty() = Rect(0.0f, 0.0f, 0.0f, 0.0f)
        fun new(
            left: Float = 0.0f,
            top: Float = 0.0f,
            right: Float = 0.0f,
            bottom: Float = 0.0f
        ) = Rect(left, top, right, bottom)

        fun new(
            width: Float,
            height: Float
        ) = Rect(0.0f, 0.0f, width, height)

        fun from(r: Rect) = Rect(r.left, r.top, r.right, r.bottom)
    }

    fun applyFrom(other: Rect): Rect {
        left = other.left
        top = other.top
        right = other.right
        bottom = other.bottom
        return this
    }

    fun isEmpty(): Boolean {
        return left >= right || top >= bottom
    }

    fun contains(offsetX: Float, offsetY: Float): Boolean {
        return offsetX >= left && offsetX < right && offsetY >= top && offsetY < bottom
    }

    fun deflateBy(deltaX: Float, deltaY: Float) = inflateBy(-deltaX, -deltaY)

    private fun inflateBy(deltaX: Float, deltaY: Float) {
        left -= deltaX
        top -= deltaY
        right += deltaX
        bottom += deltaY
    }

    val topLeft: Offset
        get() = Offset(left, top)

    var width: Float
        get() = right - left
        set(v) {
            right = left + v
        }

    var height: Float
        get() = bottom - top
        set(v) {
            bottom = top + v
        }

    val size: Size
        get() = Size(width, height)
}

@Suppress("NOTHING_TO_INLINE")
@Stable
inline fun Offset.toIntOffset() = IntOffset(x.toInt(), y.toInt())
