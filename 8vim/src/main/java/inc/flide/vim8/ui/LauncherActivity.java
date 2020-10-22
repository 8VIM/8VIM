package inc.flide.vim8.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.afollestad.materialdialogs.MaterialDialog;

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

    //Buttons
    private Button switchToEmojiKeyboardButton;
    private Button leftButtonClick;
    private Button rightButtonClick;
    private Button resizeButton;
    private Button setCenterButton;

    //Switch
    private Switch touch_trail_switch;

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

        switchToEmojiKeyboardButton = findViewById(R.id.emoji);
        switchToEmojiKeyboardButton.setOnClickListener(v -> askUserPreferredEmoticonKeyboard());

        resizeButton = findViewById(R.id.resize_button);
        resizeButton.setOnClickListener(v -> allowUserToResizeCircle());

        setCenterButton = findViewById(R.id.setcenter_button);
        setCenterButton.setOnClickListener(v -> allowUserToSetCentreForCircle());

        leftButtonClick = findViewById(R.id.left_button);
        leftButtonClick.setOnClickListener(v -> switchSidebarPosition(getString(R.string.mainKeyboard_sidebar_position_preference_left_value)));

        rightButtonClick = findViewById(R.id.right_button);
        rightButtonClick.setOnClickListener(v -> switchSidebarPosition(getString(R.string.mainKeyboard_sidebar_position_preference_right_value)));


        // switch functionality
        touch_trail_switch = (Switch) findViewById(R.id.touch_trail);

        SharedPreferences sp = getSharedPreferences(getString(R.string.basic_preference_file_name), Activity.MODE_PRIVATE);
        String current_switch_mode = sp.getString(getString(R.string.switch_mode),"");

        if(current_switch_mode.equals("true"))
        {
            touch_trail_switch.setChecked(true);
        }
        else{
            touch_trail_switch.setChecked(false);
        }

        touch_trail_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.basic_preference_file_name), Activity.MODE_PRIVATE);
                    SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
                    sharedPreferencesEditor.putString(getString(R.string.switch_mode),"true");
                    sharedPreferencesEditor.apply();
                }
                else {

                    SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.basic_preference_file_name), Activity.MODE_PRIVATE);
                    SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
                    sharedPreferencesEditor.putString(getString(R.string.switch_mode),"false");
                    sharedPreferencesEditor.apply();
                }
            }
        });
    }

    private void switchSidebarPosition(String userPreferedPositionForSidebar) {
        if (userPreferedPositionForSidebar.equals(getString(R.string.mainKeyboard_sidebar_position_preference_left_value)) ||
            userPreferedPositionForSidebar.equals(getString(R.string.mainKeyboard_sidebar_position_preference_right_value))) {

            SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.basic_preference_file_name), Activity.MODE_PRIVATE);
            SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
            sharedPreferencesEditor.putString(
                    getString(R.string.mainKeyboard_sidebar_position_preference_key),
                    userPreferedPositionForSidebar);
            sharedPreferencesEditor.apply();
        }
    }

    private void allowUserToResizeCircle(){
        Intent intent = new Intent(this, ResizeActivity.class);
        startActivity(intent);
    }

    private void allowUserToSetCentreForCircle(){
        Intent intent = new Intent(this,SetCenterActivity.class);
        startActivity(intent);
    }

    private void askUserPreferredEmoticonKeyboard(){
       InputMethodManager imeManager = (InputMethodManager) getApplicationContext().getSystemService(INPUT_METHOD_SERVICE);
       List<InputMethodInfo> inputMethods = imeManager.getEnabledInputMethodList();

        Map<String,String> inputMethodsNameAndId = new HashMap<>();
        for(InputMethodInfo inputMethodInfo : inputMethods){
            if(inputMethodInfo.getId().compareTo(Constants.SELF_KEYBOARD_ID) != 0) {
                inputMethodsNameAndId.put(inputMethodInfo.loadLabel(getPackageManager()).toString(),inputMethodInfo.getId());
            }
        }
        ArrayList<String> keyboardIds = new ArrayList<>(inputMethodsNameAndId.values());

        SharedPreferences sp = getSharedPreferences(getString(R.string.basic_preference_file_name), Activity.MODE_PRIVATE);
        String selectedKeyboardId = sp.getString(getString(R.string.bp_selected_emoticon_keyboard),"");
        int selectedKeyboardIndex = -1;
        if (!selectedKeyboardId.isEmpty()) {
            selectedKeyboardIndex = keyboardIds.indexOf(selectedKeyboardId);

            if (selectedKeyboardIndex == -1) {
                // seems like we have a stale selection, it should be removed.
                sp.edit().remove(getString(R.string.bp_selected_emoticon_keyboard)).commit();
            }
        }
        new MaterialDialog.Builder(this)
            .title(R.string.select_prefered_emoji_keyboard_dialog_title)
            .items(inputMethodsNameAndId.keySet())
            .itemsCallbackSingleChoice(selectedKeyboardIndex, (dialog, itemView, which, text) -> {

                if(which != -1) {
                    SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.basic_preference_file_name), Activity.MODE_PRIVATE);
                    SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
                    sharedPreferencesEditor.putString(getString(R.string.bp_selected_emoticon_keyboard),keyboardIds.get(which));
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.share:
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, R.string.app_name);
                String shareMessage= "\nCheck out this awesome keyboard application\n\n";
                shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID +"\n";
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                startActivity(Intent.createChooser(shareIntent, "Share "+ R.string.app_name));
                break;

            case R.id.help:
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri data = Uri.parse("mailto:flideravi@gmail.com?subject=" + "Feedback");
                intent.setData(data);
                startActivity(intent);
                break;

            case R.id.about :
                AlertDialog.Builder about = new AlertDialog.Builder(this);
                about.setTitle("About "+ R.string.app_name);
                about.setMessage("More than just a clone for now defunct 8Pen Application\n\n" +
                        "Designed and Developed by Flide\n" + R.string.version_name);
                about.setCancelable(true);
                about.show();
                break;
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
            if(inputMethodInfo.getId().compareTo(Constants.SELF_KEYBOARD_ID) == 0) {
                isKeyboardEnabled = true;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Ask user to enable the IME if it is not enabled yet
        if(!isKeyboardEnabled) {
            enableInputMethodDialog();
        }
        // Ask user to activate the IME while he is using the settings application
        else if(!Constants.SELF_KEYBOARD_ID.equals(Settings.Secure.getString(getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD))) {
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
