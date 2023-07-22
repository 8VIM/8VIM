package inc.flide.vim8.ui;

import android.content.Context;
import inc.flide.vim8.R;
import inc.flide.vim8.preferences.SharedPreferenceHelper;
import inc.flide.vim8.utils.ColorsHelper;
import java.util.ArrayList;
import java.util.List;

public class Theme {
    private static Theme singleton = null;
    private static int backgroundColor;
    private static int foregroundColor;
    private final List<OnChangeCallback> callbacks = new ArrayList<>();

    private Theme(Context context) {
        updateTheme(context);
        SharedPreferenceHelper.getInstance(context).addListener(() -> dispatchChange(context),
                context.getString(R.string.pref_board_bg_color_key),
                context.getString(R.string.pref_board_fg_color_key),
                context.getString(R.string.pref_color_mode_key));
    }

    public static Theme getInstance(Context context) {
        if (context == null) {
            return singleton;
        }
        if (singleton == null) {
            singleton = new Theme(context);
        }
        return singleton;
    }

    public static int getBackgroundColor() {
        return backgroundColor;
    }

    public static int getForegroundColor() {
        return foregroundColor;
    }

    public void onChange(OnChangeCallback callback) {
        callbacks.add(callback);
    }

    private void dispatchChange(Context context) {
        updateTheme(context);
        for (OnChangeCallback callback : callbacks) {
            callback.invoke();
        }
    }

    private void updateTheme(Context context) {
        backgroundColor =
                ColorsHelper.getThemeColor(context, R.attr.colorSurface,
                        R.string.pref_board_bg_color_key,
                        R.color.defaultBoardBg);
        foregroundColor =
                ColorsHelper.getThemeColor(context, R.attr.colorOnSurface,
                        R.string.pref_board_fg_color_key,
                        R.color.defaultBoardFg);
    }

    public interface OnChangeCallback {
        void invoke();
    }
}
