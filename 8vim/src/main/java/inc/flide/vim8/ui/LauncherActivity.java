package inc.flide.vim8.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Switch;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import inc.flide.vim8.BuildConfig;
import inc.flide.vim8.R;
import inc.flide.vim8.structures.Constants;

public class LauncherActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private boolean isKeyboardEnabled;

    private ConstraintLayout constraintLayout_select_color;
    private ConstraintLayout constraintLayout_display_icons;
    private ConstraintLayout constraintLayout_touch_trail;

    private Button red_button;
    private Button green_button;
    private Button blue_button;
    private Button yellow_button;

    private SwitchCompat display_icon_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.white));
        toolbar.setTitleTextColor(getResources().getColor(R.color.black));

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        constraintLayout_select_color = findViewById(R.id.constraintlayout_colors);

        Button switchToEmojiKeyboardButton = findViewById(R.id.emoji);
        switchToEmojiKeyboardButton.setOnClickListener(v -> askUserPreferredEmoticonKeyboard());

        Button resizeButton = findViewById(R.id.resize_button);
        resizeButton.setOnClickListener(v -> allowUserToResizeCircle());

        Button setCenterButton = findViewById(R.id.setcenter_button);
        setCenterButton.setOnClickListener(v -> allowUserToSetCentreForCircle());

        Button leftButtonClick = findViewById(R.id.left_button);
        leftButtonClick.setOnClickListener(v -> switchSidebarPosition(getString(R.string.mainKeyboard_sidebar_position_preference_left_value)));

        Button rightButtonClick = findViewById(R.id.right_button);
        rightButtonClick.setOnClickListener(v -> switchSidebarPosition(getString(R.string.mainKeyboard_sidebar_position_preference_right_value)));

        SwitchCompat touch_trail_switch = findViewById(R.id.touch_trail);

        SharedPreferences sp = getSharedPreferences(getString(R.string.basic_preference_file_name), Activity.MODE_PRIVATE);
        touch_trail_switch.setChecked(sp.getBoolean(getString(R.string.user_preferred_typing_trail_visibility), true));
        touch_trail_switch.setOnCheckedChangeListener((buttonView, isChecked) -> touchTrailPreferenceChangeListner(isChecked));

        boolean touch_trail_visibility = sp.getBoolean(getString(R.string.user_preferred_typing_trail_visibility), true);

        if (touch_trail_visibility) {
            constraintLayout_select_color.setVisibility(View.VISIBLE);
        } else {
            constraintLayout_select_color.setVisibility(View.INVISIBLE);
        }

        // for display icons in the sector

        display_icon_button = findViewById(R.id.display_icons_switch);

        display_icon_button.setChecked(sp.getBoolean(getString(R.string.user_preferred_display_icons_for_sectors), true));
        display_icon_button.setOnCheckedChangeListener((buttonView, isChecked) -> displayIconsPreferenceChangeListner(isChecked));

        red_button = findViewById(R.id.red_button);
        green_button = findViewById(R.id.green_button);
        blue_button = findViewById(R.id.blue_button);
        yellow_button = findViewById(R.id.yellow_button);

        red_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                red_button.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.red_rounded_button_pressed));
                red_button.setTextColor(Color.DKGRAY);

                green_button.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.green_rounded_button_unpressed));
                green_button.setTextColor(Color.WHITE);

                yellow_button.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.yellow_rounded_button_unpressed));
                yellow_button.setTextColor(Color.WHITE);

                blue_button.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.blue_rounded_button_unpressed));
                blue_button.setTextColor(Color.WHITE);

                SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.basic_preference_file_name), Activity.MODE_PRIVATE);
                SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
                sharedPreferencesEditor.putInt(getString(R.string.color_selection), Color.RED);
                sharedPreferencesEditor.apply();
            }
        });

        green_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                green_button.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.green_rounded_button_pressed));
                green_button.setTextColor(Color.DKGRAY);

                red_button.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.red_rounded_button_unpressed));
                red_button.setTextColor(Color.WHITE);

                yellow_button.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.yellow_rounded_button_unpressed));
                yellow_button.setTextColor(Color.WHITE);

                blue_button.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.blue_rounded_button_unpressed));
                blue_button.setTextColor(Color.WHITE);

                SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.basic_preference_file_name), Activity.MODE_PRIVATE);
                SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
                sharedPreferencesEditor.putInt(getString(R.string.color_selection), Color.GREEN);
                sharedPreferencesEditor.apply();
            }
        });

        yellow_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                yellow_button.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.yellow_rounded_button_pressed));
                yellow_button.setTextColor(Color.DKGRAY);

                red_button.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.red_rounded_button_unpressed));
                red_button.setTextColor(Color.WHITE);

                green_button.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.green_rounded_button_unpressed));
                green_button.setTextColor(Color.WHITE);

                blue_button.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.blue_rounded_button_unpressed));
                blue_button.setTextColor(Color.WHITE);

                SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.basic_preference_file_name), Activity.MODE_PRIVATE);
                SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
                sharedPreferencesEditor.putInt(getString(R.string.color_selection), Color.YELLOW);
                sharedPreferencesEditor.apply();
            }
        });

        blue_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                blue_button.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.blue_rounded_button_pressed));
                blue_button.setTextColor(Color.DKGRAY);

                red_button.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.red_rounded_button_unpressed));
                red_button.setTextColor(Color.WHITE);

                yellow_button.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.yellow_rounded_button_unpressed));
                yellow_button.setTextColor(Color.WHITE);

                green_button.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.green_rounded_button_unpressed));
                green_button.setTextColor(Color.WHITE);

                SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.basic_preference_file_name), Activity.MODE_PRIVATE);
                SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
                sharedPreferencesEditor.putInt(getString(R.string.color_selection), Color.BLUE);
                sharedPreferencesEditor.apply();
            }
        });

    }

    private void touchTrailPreferenceChangeListner(boolean isChecked) {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.basic_preference_file_name), Activity.MODE_PRIVATE);
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        sharedPreferencesEditor.putBoolean(getString(R.string.user_preferred_typing_trail_visibility), isChecked);
        sharedPreferencesEditor.apply();

        if (isChecked) {
            Transition transition = new Fade();
            transition.setDuration(600);
            transition.addTarget(R.id.constraintlayout_colors);

            TransitionManager.beginDelayedTransition(constraintLayout_select_color, transition);
            constraintLayout_select_color.setVisibility(View.VISIBLE);
        } else {
            Transition transition = new Fade();
            transition.setDuration(600);
            transition.addTarget(R.id.constraintlayout_colors);

            TransitionManager.beginDelayedTransition(constraintLayout_select_color, transition);
            constraintLayout_select_color.setVisibility(View.INVISIBLE);
        }

    }

    private void displayIconsPreferenceChangeListner(boolean isChecked) {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.basic_preference_file_name), Activity.MODE_PRIVATE);
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        sharedPreferencesEditor.putBoolean(getString(R.string.user_preferred_display_icons_for_sectors), isChecked);
        sharedPreferencesEditor.apply();

    }


        private void switchSidebarPosition(String userPreferredPositionForSidebar) {
        if (userPreferredPositionForSidebar.equals(getString(R.string.mainKeyboard_sidebar_position_preference_left_value)) ||
                userPreferredPositionForSidebar.equals(getString(R.string.mainKeyboard_sidebar_position_preference_right_value))) {

            SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.basic_preference_file_name), Activity.MODE_PRIVATE);
            SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
            sharedPreferencesEditor.putString(
                    getString(R.string.mainKeyboard_sidebar_position_preference_key),
                    userPreferredPositionForSidebar);
            sharedPreferencesEditor.apply();
        }
    }

    private void allowUserToResizeCircle() {
        Intent intent = new Intent(this, ResizeActivity.class);
        startActivity(intent);
    }

    private void allowUserToSetCentreForCircle() {
        Intent intent = new Intent(this, SetCenterActivity.class);
        startActivity(intent);
    }

    private void askUserPreferredEmoticonKeyboard() {
        InputMethodManager imeManager = (InputMethodManager) getApplicationContext().getSystemService(INPUT_METHOD_SERVICE);
        List<InputMethodInfo> inputMethods = imeManager.getEnabledInputMethodList();

        Map<String, String> inputMethodsNameAndId = new HashMap<>();
        for (InputMethodInfo inputMethodInfo : inputMethods) {
            if (inputMethodInfo.getId().compareTo(Constants.SELF_KEYBOARD_ID) != 0) {
                inputMethodsNameAndId.put(inputMethodInfo.loadLabel(getPackageManager()).toString(), inputMethodInfo.getId());
            }
        }
        ArrayList<String> keyboardIds = new ArrayList<>(inputMethodsNameAndId.values());

        SharedPreferences sp = getSharedPreferences(getString(R.string.basic_preference_file_name), Activity.MODE_PRIVATE);
        String selectedKeyboardId = sp.getString(getString(R.string.bp_selected_emoticon_keyboard), "");
        int selectedKeyboardIndex = -1;
        if (!selectedKeyboardId.isEmpty()) {
            selectedKeyboardIndex = keyboardIds.indexOf(selectedKeyboardId);
            if (selectedKeyboardIndex == -1) {
                // seems like we have a stale selection, it should be removed.
                sp.edit().remove(getString(R.string.bp_selected_emoticon_keyboard)).apply();
            }
        }
        new MaterialDialog.Builder(this)
                .title(R.string.select_preferred_emoticon_keyboard_dialog_title)
                .items(inputMethodsNameAndId.keySet())
                .itemsCallbackSingleChoice(selectedKeyboardIndex, (dialog, itemView, which, text) -> {

                    if (which != -1) {
                        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.basic_preference_file_name), Activity.MODE_PRIVATE);
                        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
                        sharedPreferencesEditor.putString(getString(R.string.bp_selected_emoticon_keyboard), keyboardIds.get(which));
                        sharedPreferencesEditor.apply();
                    }
                    return true;
                })
                .positiveText(R.string.generic_okay_text)
                .show();
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


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.share) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, R.string.app_name);
            String shareMessage = "\nCheck out this awesome keyboard application\n\n";
            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "\n";
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "Share " + R.string.app_name));
        } else if (item.getItemId() == R.id.help) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri data = Uri.parse("mailto:flideravi@gmail.com?subject=" + "Feedback");
            intent.setData(data);
            startActivity(intent);
        } else if (item.getItemId() == R.id.about) {
            Intent intent_about = new Intent(LauncherActivity.this, AboutUsActivity.class);
            startActivity(intent_about);
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
        for (InputMethodInfo inputMethodInfo : enabledInputMethodList) {
            if (inputMethodInfo.getId().compareTo(Constants.SELF_KEYBOARD_ID) == 0) {
                isKeyboardEnabled = true;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.basic_preference_file_name), Activity.MODE_PRIVATE);
        int currentColor = sharedPreferences.getInt(getString(R.string.color_selection), Color.YELLOW);

        if (currentColor == Color.RED) {
            red_button.setBackground(ContextCompat.getDrawable(this, R.drawable.red_rounded_button_pressed));
            red_button.setTextColor(Color.DKGRAY);

        } else if (currentColor == Color.GREEN) {
            green_button.setBackground(ContextCompat.getDrawable(this, R.drawable.green_rounded_button_pressed));
            green_button.setTextColor(Color.DKGRAY);

        } else if (currentColor == Color.YELLOW) {
            yellow_button.setBackground(ContextCompat.getDrawable(this, R.drawable.yellow_rounded_button_pressed));
            yellow_button.setTextColor(Color.DKGRAY);

        } else if (currentColor == Color.BLUE) {
            blue_button.setBackground(ContextCompat.getDrawable(this, R.drawable.blue_rounded_button_pressed));
            blue_button.setTextColor(Color.DKGRAY);

        }

        // Ask user to enable the IME if it is not enabled yet
        if (!isKeyboardEnabled) {
            enableInputMethodDialog();
        }
        // Ask user to activate the IME while he is using the settings application
        else if (!Constants.SELF_KEYBOARD_ID.equals(Settings.Secure.getString(getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD))) {
            activateInputMethodDialog();
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
                .onNeutral((dialog, which) -> {
                    startActivityForResult(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS), 0);
                    enableInputMethodNotificationDialog.dismiss();
                });

        enableInputMethodNotificationDialog.show();
    }


    private void activateInputMethodDialog() {
        final MaterialDialog activateInputMethodNotificationDialog = new MaterialDialog.Builder(this)
                .title(R.string.activate_ime_dialog_title)
                .content(R.string.activate_ime_dialog_content)
                .positiveText(R.string.activate_ime_dialog_positive_button_text)
                .negativeText(R.string.activate_ime_dialog_negative_button_text)
                .cancelable(false)
                .canceledOnTouchOutside(false)
                .build();

        activateInputMethodNotificationDialog.getBuilder()
                .onPositive((dialog, which) -> {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.showInputMethodPicker();
                    activateInputMethodNotificationDialog.dismiss();
                });

        activateInputMethodNotificationDialog.show();
    }

}
