package inc.flide.vi8.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;

import inc.flide.vi8.R;
import inc.flide.vi8.structures.Constants;

public class LauncherActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private boolean isKeyboardEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.configure_emoji) {
            startActivity(new Intent(this, ConfigureEmoticonKeyboardActivity.class));
        } else if (id == R.id.add_gesture) {

        } else if (id == R.id.donate) {

        } else if (id == R.id.about) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        List<InputMethodInfo> enabledInputMethodList = inputMethodManager.getEnabledInputMethodList();
        isKeyboardEnabled = false;
        for(InputMethodInfo inputMethodInfo: enabledInputMethodList) {
            if(inputMethodInfo.getId().compareTo(Constants.KEYBOARD_ID) == 0) {
                isKeyboardEnabled = true;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!isKeyboardEnabled) {
            enableInputMethodDialog();
        }
    }

    private void enableInputMethodDialog() {
        final MaterialDialog enableInputMethodNotificationDialog = new MaterialDialog.Builder(this)
                .title(R.string.enable_ime_dialog_title)
                .content(R.string.enable_ime_dialog_content)
                .neutralText(R.string.enable_ime_dialog_neutral_button_text)
                .cancelable(false)
                .canceledOnTouchOutside(false)
                .build();

        enableInputMethodNotificationDialog.getBuilder()
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        startActivityForResult(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS), 0);
                        enableInputMethodNotificationDialog.dismiss();
                    }
                });

        enableInputMethodNotificationDialog.show();
    }
}
