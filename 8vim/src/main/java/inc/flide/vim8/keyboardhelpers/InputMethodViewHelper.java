package inc.flide.vim8.keyboardhelpers;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import inc.flide.vim8.R;
import inc.flide.vim8.geometry.Dimension;
import inc.flide.vim8.preferences.SharedPreferenceHelper;

public final class InputMethodViewHelper {
    private InputMethodViewHelper() {
    }

    public static Dimension computeDimension(Context context) {
        Resources resources = context.getResources();
        SharedPreferenceHelper sharedPreferenceHelper = SharedPreferenceHelper.getInstance(context);

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
        int scale = sharedPreferenceHelper.getInt(context.getString(R.string.pref_keyboard_height), 100);
        height = height * scale / 100;
        return new Dimension(displayMetrics.widthPixels, height);
    }

}
