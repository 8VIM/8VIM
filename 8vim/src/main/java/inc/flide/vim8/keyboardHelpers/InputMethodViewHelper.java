package inc.flide.vim8.keyboardHelpers;

import inc.flide.vim8.geometry.Dimension;

public class InputMethodViewHelper {
    public static Dimension onMeasureHelper(int width, int height, int orientation) {

        if (orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
            //Landscape is just un-usable right now.
            // TODO: Landscape mode requires more clarity, what exactly do you want to do?
            width = Math.round(1.33f * height);
        } else  // Portrait mode
        {
            height = Math.round(0.8f * (width - (60 * 3)));
            //height = Math.round(0.8f * (width));
        }
        return new Dimension(width, height);
    }

}
