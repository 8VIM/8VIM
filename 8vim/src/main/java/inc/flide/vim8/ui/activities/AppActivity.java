package inc.flide.vim8.ui.activities;

import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.WindowCompat;
import inc.flide.vim8.R;
import inc.flide.vim8.preferences.SharedPreferenceHelper;
import inc.flide.vim8.structures.Constants;
import java.util.List;

public class AppActivity extends AppCompatActivity {
    protected boolean is8VIMEnabled = false;
    protected boolean is8VIMSelected = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        switch (SharedPreferenceHelper.getInstance(getApplicationContext())
                .getString(getString(R.string.pref_color_mode_key), "system")) {
            case "system":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            default:
        }
        detect8VIMKeyboard();
        migrate8VimSetup();
    }

    private void detect8VIMKeyboard() {
        is8VIMEnabled = false;
        is8VIMSelected = false;
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        List<InputMethodInfo> enabledInputMethodList = inputMethodManager.getEnabledInputMethodList();
        for (InputMethodInfo inputMethodInfo : enabledInputMethodList) {
            if (inputMethodInfo.getId().equals(Constants.SELF_KEYBOARD_ID)) {
                is8VIMEnabled = true;
                if (Constants.SELF_KEYBOARD_ID.equals(
                        Settings.Secure.getString(getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD))) {
                    is8VIMSelected = true;
                }
                return;
            }
        }
    }

    private void migrate8VimSetup() {
        String preferenceId = getString(R.string.pref_is_8vim_setup);
        SharedPreferenceHelper sharedPreferenceHelper = SharedPreferenceHelper.getInstance(getApplicationContext());
        if (!sharedPreferenceHelper.getBoolean(preferenceId, false)
                && is8VIMEnabled
                && is8VIMSelected) {
            sharedPreferenceHelper.edit().putBoolean(preferenceId, true).commit();
        }
    }
}
