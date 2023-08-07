package inc.flide.vim8.ui.activities;

import static inc.flide.vim8.models.AppPrefsKt.appPreferenceModel;
import static inc.flide.vim8.models.LayoutKt.rememberEmbeddedLayouts;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import inc.flide.vim8.BuildConfig;
import inc.flide.vim8.R;
import inc.flide.vim8.models.AppPrefs;
import inc.flide.vim8.preferences.SharedPreferenceHelper;
import inc.flide.vim8.structures.Constants;
import inc.flide.vim8.ui.fragments.SettingsFragment;
import inc.flide.vim8.utils.DialogsHelper;
import java.util.List;

public class SettingsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private final HandlerThread handlerThread = new HandlerThread("SettingsActivityBackPress");
    private Handler backPressHandler;
    private boolean isKeyboardEnabled;
    private boolean pressBackTwice;
    private final Runnable runnable = () -> pressBackTwice = false;
    private ActivityResultLauncher<Intent> launchInputMethodSettings;
    private InputMethodManager inputMethodManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handlerThread.start();
        backPressHandler = new Handler(handlerThread.getLooper(), null);

        setContentView(R.layout.settings_page_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle =
                new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open,
                        R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        getSupportFragmentManager().beginTransaction().replace(R.id.settings_fragment, new SettingsFragment()).commit();

        launchInputMethodSettings =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                });

        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handlerThread.quit();
    }

    @Override
    protected void onPause() {
        super.onPause();
        backPressHandler.removeCallbacks(runnable);
    }

    @Override
    public void onBackPressed() {
        if (pressBackTwice) {
            finishAndRemoveTask();
        } else {
            pressBackTwice = true;
            Toast.makeText(SettingsActivity.this, "Please press Back again to exit", Toast.LENGTH_SHORT).show();
            backPressHandler.postDelayed(runnable, 2000);
        }
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.share) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, R.string.app_name);
            String shareMessage = String.format(
                    "\nCheck out this awesome keyboard application\n\nhttps://play.google.com/store/apps/details?id=%s\n",
                    BuildConfig.APPLICATION_ID);
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "Share " + R.string.app_name));
        } else if (item.getItemId() == R.id.help) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri data = Uri.parse("mailto:flideravi@gmail.com?subject=" + "Feedback");
            intent.setData(data);
            startActivity(intent);
        } else if (item.getItemId() == R.id.about) {
            Intent intentAboutUs = new Intent(SettingsActivity.this, AboutUsActivity.class);
            startActivity(intentAboutUs);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        List<InputMethodInfo> enabledInputMethodList = inputMethodManager.getEnabledInputMethodList();
        isKeyboardEnabled = false;
        for (InputMethodInfo inputMethodInfo : enabledInputMethodList) {
            if (inputMethodInfo.getId().equals(Constants.SELF_KEYBOARD_ID)) {
                isKeyboardEnabled = true;
            }
        }

        if (isKeyboardEnabled) {
            // Ask user to activate the IME while he is using the settings application
            if (!Constants.SELF_KEYBOARD_ID.equals(
                    Settings.Secure.getString(getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD))) {
                activateInputMethodDialog();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Ask user to enable the IME if it is not enabled yet
        if (!isKeyboardEnabled) {
            enableInputMethodDialog();
        }
    }

    private void enableInputMethodDialog() {
        DialogsHelper.createMaterialDialog(this, R.string.enable_ime_dialog_title, R.string.enable_ime_dialog_content,
                R.string.enable_ime_dialog_neutral_button_text,
                (dialog) -> {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.choose_the_8vim),
                                    Snackbar.LENGTH_LONG)
                            .addCallback(new Snackbar.Callback() {
                                @Override
                                public void onDismissed(Snackbar transientBottomBar, @DismissEvent int event) {
                                    launchInputMethodSettings.launch(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS));
                                }
                            }).show();
                    return null;
                }).show();
    }

    private void activateInputMethodDialog() {
        DialogsHelper.createMaterialDialog(this, R.string.activate_ime_dialog_title,
                R.string.activate_ime_dialog_content,
                R.string.activate_ime_dialog_positive_button_text,
                (dialog) -> {
                    inputMethodManager.showInputMethodPicker();
                    return null;
                }).show();
    }
}
