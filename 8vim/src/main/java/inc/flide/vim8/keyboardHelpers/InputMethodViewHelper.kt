package inc.flide.vim8.keyboardHelpers

import android.content.res.Configuration
import inc.flide.vim8.geometry.Dimension
import kotlin.math.roundToInt

object InputMethodViewHelper {
    fun onMeasureHelper(width: Int, height: Int, orientation: Int): Dimension {
        var width = width
        var height = height
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //Landscape is just un-usable right now.
            // TODO: Landscape mode requires more clarity, what exactly do you want to do?
            width = (1.33f * height).roundToInt()
        } else { // Portrait mode
            height = (0.8f * (width - 60 * 3)).roundToInt()
        }
        return Dimension(width, height)
    }
}