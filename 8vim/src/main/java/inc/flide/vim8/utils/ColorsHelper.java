package inc.flide.vim8.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.StringRes;
import inc.flide.vim8.R;
import inc.flide.vim8.preferences.SharedPreferenceHelper;

public class ColorsHelper {
    private ColorsHelper() {
    }

    @ColorInt
    public static int getThemeColor(Context context, @AttrRes int colorAttr, @StringRes int defaultColorKey,
                                    @ColorRes int defaultColorValue) {
        Resources resources = context.getResources();
        SharedPreferenceHelper sharedPreferenceHelper = SharedPreferenceHelper.getInstance(context);
        int color;
        TypedValue value = new TypedValue();
        Resources.Theme theme;
        String system = SharedPreferenceHelper.getInstance(context)
                .getString(context.getString(R.string.pref_color_mode_key), "system");
        switch (system) {
            case "system":
                theme = context.getResources().newTheme();
                theme.applyStyle(R.style.AppTheme_NoActionBar, true);
                theme.resolveAttribute(colorAttr, value, true);
                color = value.data;
                break;
            case "dark":
                theme = context.getResources().newTheme();
                theme.applyStyle(R.style.AppThemeDark_NoActionBar, true);
                theme.resolveAttribute(colorAttr, value, true);
                color = value.data;
                break;
            case "light":
                theme = context.getResources().newTheme();
                theme.applyStyle(R.style.AppThemeLight_NoActionBar, true);
                theme.resolveAttribute(colorAttr, value, true);
                color = value.data;
                break;
            default:
                color = sharedPreferenceHelper.getInt(
                        resources.getString(defaultColorKey),
                        resources.getColor(defaultColorValue));
        }
        return color;
    }

}
