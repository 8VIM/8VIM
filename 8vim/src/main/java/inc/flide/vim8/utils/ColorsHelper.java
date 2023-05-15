package inc.flide.vim8.utils;

import android.content.Context;
import android.content.res.Resources.Theme;
import android.util.TypedValue;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;

public class ColorsHelper {
    private ColorsHelper() {
    }

    @ColorInt
    public static int getThemeColor(Context context, @AttrRes int colorAttr) {
        TypedValue value = new TypedValue();
        Theme theme = context.getTheme();
        theme.resolveAttribute(colorAttr, value, true);
        return value.data;
    }

}
