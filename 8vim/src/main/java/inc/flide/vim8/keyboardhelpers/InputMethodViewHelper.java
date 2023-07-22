package inc.flide.vim8.keyboardhelpers;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import inc.flide.vim8.R;
import inc.flide.vim8.geometry.Dimension;

public final class InputMethodViewHelper {
    private InputMethodViewHelper() {
    }

    public static Dimension computeDimension(Resources resources) {
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        float minBaseSize;
        if (resources.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            minBaseSize = resources.getFraction(R.fraction.inputView_minHeightFraction, displayMetrics.heightPixels,
                    displayMetrics.heightPixels);
        } else {
            minBaseSize = resources.getFraction(R.fraction.inputView_minHeightFraction, displayMetrics.widthPixels,
                    displayMetrics.widthPixels);
        }
        float maxBaseSize = resources.getFraction(R.fraction.inputView_maxHeightFraction, displayMetrics.heightPixels,
                displayMetrics.heightPixels);
        int height = (int) (Math.max((minBaseSize + maxBaseSize) / 2.0f,
                resources.getDimension(R.dimen.inputView_baseHeight)));
        return new Dimension(displayMetrics.widthPixels, height);
    }

}
