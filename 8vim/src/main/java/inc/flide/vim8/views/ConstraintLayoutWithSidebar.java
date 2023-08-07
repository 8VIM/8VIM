package inc.flide.vim8.views;

import static inc.flide.vim8.models.AppPrefsKt.appPreferenceModel;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import inc.flide.vim8.R;
import inc.flide.vim8.geometry.Dimension;
import inc.flide.vim8.ime.KeyboardTheme;
import inc.flide.vim8.keyboardactionlisteners.KeypadActionListener;
import inc.flide.vim8.keyboardhelpers.InputMethodViewHelper;
import inc.flide.vim8.models.AppPrefs;
import inc.flide.vim8.models.CustomKeycode;
import inc.flide.vim8.ui.activities.SettingsActivity;

public abstract class ConstraintLayoutWithSidebar<T extends KeypadActionListener> extends ConstraintLayout {
    protected T actionListener;
    protected KeyboardTheme keyboardTheme;
    protected AppPrefs prefs;
    protected LayoutInflater inflater;

    public ConstraintLayoutWithSidebar(@NonNull Context context) {
        super(context);
        initialize(context);
    }

    public ConstraintLayoutWithSidebar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public ConstraintLayoutWithSidebar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    protected void initialize(Context context) {
        prefs = appPreferenceModel().java();
        keyboardTheme = KeyboardTheme.getInstance();
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        initializeActionListener(context);
        keyboardTheme.onChange(this::setColors);
        AppPrefs.Keyboard.SideBar sidebarPrefs = prefs.getKeyboard().getSidebar();
        prefs.getKeyboard().getHeight().observe(newValue -> {
            initializeView();
            invalidate();
        });
        sidebarPrefs.isOnLeft().observe(newValue -> initializeView());
        sidebarPrefs.isVisible().observe(newValue -> initializeView());
        initializeView();
    }

    protected void initializeView() {
        setupMainKeyboardView();
        setupButtonsOnSideBar();
        setColors();
        setHapticFeedbackEnabled(true);
    }

    protected void setupMainKeyboardView() {
        removeAllViews();
        AppPrefs.Keyboard.SideBar sidebarPrefs = prefs.getKeyboard().getSidebar();
        Boolean isSidebarOnLeft = sidebarPrefs.isOnLeft().get();
        int sidebarLayout = getSidebarLayout(isSidebarOnLeft);
        inflater.inflate(sidebarLayout, this, true);
        if (!sidebarPrefs.isVisible().get()) {
            View sidebar = this.findViewById(R.id.sidebarButtonsLayout);
            LayoutParams params = (LayoutParams) sidebar.getLayoutParams();
            params.horizontalWeight = 0;
            sidebar.setLayoutParams(params);
        }
    }

    protected abstract void initializeActionListener(Context context);

    protected abstract int getSidebarLayout(boolean isSidebarOnLeft);

    protected void setupButtonsOnSideBar() {
        setupSwitchToEmojiKeyboardButton();
        setupSwitchToSelectionKeyboardButton();
        setupTabKey();
        setupGoToSettingsButton();
    }

    protected void setupSwitchToMainKeyboardButton() {
        ImageButton switchToMainKeyboardButton = findViewById(R.id.switchKeypadButton);
        switchToMainKeyboardButton.setContentDescription(
                this.getContext().getString(R.string.main_keyboard_button_content_description));
        switchToMainKeyboardButton.setImageDrawable(
                AppCompatResources.getDrawable(this.getContext(), R.drawable.ic_viii));
        switchToMainKeyboardButton.setOnClickListener(
                view -> actionListener.handleInputKey(CustomKeycode.SWITCH_TO_MAIN_KEYPAD.keyCode, 0));
    }

    protected void setupSwitchToClipboardKeypadButton() {
        ImageButton switchToClipboardButton = findViewById(R.id.switchKeypadButton);
        switchToClipboardButton.setContentDescription(
                this.getContext().getString(R.string.clipboard_button_content_description));
        switchToClipboardButton.setImageDrawable(
                AppCompatResources.getDrawable(this.getContext(), R.drawable.clipboard));
        switchToClipboardButton.setOnClickListener(
                view -> actionListener.handleInputKey(CustomKeycode.SWITCH_TO_CLIPPAD_KEYBOARD.keyCode, 0));
    }

    private void setupGoToSettingsButton() {
        ImageButton goToSettingsButton = findViewById(R.id.goToSettingsButton);
        goToSettingsButton.setOnClickListener(view -> {
            Intent vim8SettingsIntent = new Intent(getContext(), SettingsActivity.class);
            vim8SettingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(vim8SettingsIntent);
        });
    }

    private void setupTabKey() {
        ImageButton tabKeyButton = findViewById(R.id.tabButton);
        tabKeyButton.setOnClickListener(view -> actionListener.handleInputKey(KeyEvent.KEYCODE_TAB, 0));
    }

    private void setupSwitchToSelectionKeyboardButton() {
        ImageButton switchToSelectionKeyboardButton = findViewById(R.id.switchToSelectionKeyboard);
        switchToSelectionKeyboardButton.setOnClickListener(
                view -> actionListener.handleInputKey(CustomKeycode.SWITCH_TO_SELECTION_KEYPAD.keyCode, 0));
    }

    private void setupSwitchToEmojiKeyboardButton() {
        ImageButton switchToEmojiKeyboardButton = findViewById(R.id.switchToEmojiKeyboard);
        switchToEmojiKeyboardButton.setOnClickListener(
                view -> actionListener.handleInputKey(CustomKeycode.SWITCH_TO_EMOTICON_KEYBOARD.keyCode, 0));
    }

    protected void setImageButtonTint(int tintColor, int id) {
        ImageButton button = findViewById(id);
        button.setColorFilter(tintColor);
    }

    protected void setColors() {
        int backgroundColor = keyboardTheme.getBackgroundColor();
        int tintColor = keyboardTheme.getForegroundColor();

        this.setBackgroundColor(backgroundColor);
        setImageButtonTint(tintColor, R.id.switchKeypadButton);
        setImageButtonTint(tintColor, R.id.goToSettingsButton);
        setImageButtonTint(tintColor, R.id.tabButton);
        setImageButtonTint(tintColor, R.id.switchToSelectionKeyboard);
        setImageButtonTint(tintColor, R.id.switchToEmojiKeyboard);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Dimension computedDimension = InputMethodViewHelper.computeDimension(getResources());
        setMeasuredDimension(computedDimension.width, computedDimension.height);
        super.onMeasure(MeasureSpec.makeMeasureSpec(computedDimension.width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(computedDimension.height, MeasureSpec.EXACTLY));

    }

}
