package inc.flide.vim8.ui.activities;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.Settings;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import inc.flide.vim8.BuildConfig;
import inc.flide.vim8.R;
import inc.flide.vim8.ime.AvailableLayouts;
import inc.flide.vim8.lib.android.AndroidSettings;
import inc.flide.vim8.lib.android.SystemSettingsObserver;
import inc.flide.vim8.ui.fragments.SettingsFragment;
import inc.flide.vim8.utils.InputMethodUtils;
import inc.flide.vim8.views.MaterialCard;

public class SettingsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private final HandlerThread handlerThread = new HandlerThread("SettingsActivityBackPress");
    private Handler backPressHandler;
    private boolean pressBackTwice;
    private final Runnable runnable = () -> pressBackTwice = false;
    private SystemSettingsObserver keyboardEnabledObserver;
    private SystemSettingsObserver keyboardSelectedObserver;
    private MaterialCard keyboardNotEnabledCard;
    private MaterialCard keyboardNotSelectedCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AvailableLayouts.initialize(this);
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
        setupCards();
    }

    private void setupCards() {
        keyboardNotEnabledCard = findViewById(R.id.error_card);
        keyboardNotSelectedCard = findViewById(R.id.warning_card);
        keyboardNotEnabledCard.setOnClickListener(
                (v) -> InputMethodUtils.INSTANCE.showImeEnablerActivity(getApplicationContext()));
        keyboardNotSelectedCard.setOnClickListener(
                (v) -> InputMethodUtils.INSTANCE.showImePicker(getApplicationContext()));
        keyboardEnabledObserver =
                new SystemSettingsObserver(getApplicationContext(), this::updateCards);
        keyboardSelectedObserver =
                new SystemSettingsObserver(getApplicationContext(), this::updateCards);
        updateCards();
    }


    @Override
    protected void onStart() {
        super.onStart();
        observe();
    }

    @Override
    protected void onResume() {
        super.onResume();
        observe();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handlerThread.quit();
        removeObservers();
    }

    @Override
    protected void onPause() {
        super.onPause();
        backPressHandler.removeCallbacks(runnable);
        removeObservers();
    }

    private void observe() {
        observe(Settings.Secure.ENABLED_INPUT_METHODS, keyboardEnabledObserver);
        observe(Settings.Secure.DEFAULT_INPUT_METHOD, keyboardSelectedObserver);
    }

    private void observe(String key, SystemSettingsObserver observer) {
        Uri uri = AndroidSettings.INSTANCE.getSecure().getUriFor(key);
        if (uri != null) {
            getApplicationContext().getContentResolver().registerContentObserver(uri, false, observer);
            observer.dispatchChange(false, uri);
        }
    }

    private void removeObservers() {
        removeObserver(keyboardEnabledObserver);
        removeObserver(keyboardSelectedObserver);
    }

    private void removeObserver(SystemSettingsObserver observer) {
        getApplicationContext().getContentResolver().unregisterContentObserver(observer);
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

    private void updateCards() {
        updateKeyboardNotEnabledCard();
        updateKeyboardNotSelectedCard();
    }

    private void updateKeyboardNotEnabledCard() {
        Context context = getApplicationContext();
        String imeIds = AndroidSettings.INSTANCE.getSecure().getString(context, Settings.Secure.ENABLED_INPUT_METHODS);
        int visibility =
                imeIds != null && InputMethodUtils.INSTANCE.parseIs8VimEnabled(context, imeIds) ? GONE : VISIBLE;
        keyboardNotEnabledCard.setVisibility(visibility);
    }

    private void updateKeyboardNotSelectedCard() {
        Context context = getApplicationContext();
        String selectedImeId =
                AndroidSettings.INSTANCE.getSecure().getString(context, Settings.Secure.DEFAULT_INPUT_METHOD);
        boolean isSelected =
                selectedImeId != null && InputMethodUtils.INSTANCE.parseIs8VimSelected(context, selectedImeId);
        int visibility =
                keyboardNotEnabledCard.getVisibility() == VISIBLE || isSelected ? GONE : VISIBLE;
        keyboardNotSelectedCard.setVisibility(visibility);
    }
}
