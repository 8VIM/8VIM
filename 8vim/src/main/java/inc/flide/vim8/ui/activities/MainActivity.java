package inc.flide.vim8.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import inc.flide.vim8.R;
import inc.flide.vim8.preferences.SharedPreferenceHelper;

public class MainActivity extends AppActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean isSetupDone = SharedPreferenceHelper.getInstance(getApplicationContext())
                .getBoolean(getString(R.string.pref_is_8vim_setup), false);
        Intent intent;
        if (isSetupDone) {
            intent = new Intent(this, SettingsActivity.class);
        } else {
            intent = new Intent(this, SetupActivity.class);
        }
        startActivity(intent);
    }
}
