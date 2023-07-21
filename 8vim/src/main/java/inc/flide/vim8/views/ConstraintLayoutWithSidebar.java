package inc.flide.vim8.views;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import inc.flide.vim8.R;
import inc.flide.vim8.geometry.Dimension;
import inc.flide.vim8.keyboardactionlisteners.KeypadActionListener;
import inc.flide.vim8.keyboardhelpers.InputMethodViewHelper;
import inc.flide.vim8.structures.CustomKeycode;
import inc.flide.vim8.structures.KeyboardAction;
import inc.flide.vim8.structures.KeyboardActionType;
import inc.flide.vim8.ui.SettingsActivity;
import inc.flide.vim8.utils.ColorsHelper;

public class ConstraintLayoutWithSidebar extends ConstraintLayout {
    protected KeypadActionListener actionListener;

    public ConstraintLayoutWithSidebar(@NonNull Context context) {
        super(context);
    }

    public ConstraintLayoutWithSidebar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ConstraintLayoutWithSidebar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected void setupButtonsOnSideBar(KeypadActionListener actionListener) {
        this.actionListener = actionListener;
        setupSwitchToEmojiKeyboardButton();
        setupSwitchToSelectionKeyboardButton();
        setupTabKey();
        setupGoToSettingsButton();
        setupSwitchToClipboardKeypadButton();
    }

    private void setupSwitchToClipboardKeypadButton() {
        ImageButton switchToClipboardButton = findViewById(R.id.clipboardButton);
        switchToClipboardButton.setOnClickListener(
                view -> {
                    KeyboardAction switchToClipboardKeyboard = new KeyboardAction(
                            KeyboardActionType.INPUT_KEY,
                            "",
                            null,
                            CustomKeycode.SWITCH_TO_CLIPPAD_KEYBOARD.getKeyCode(),
                            0,
                            0);
                    actionListener.handleInputKey(switchToClipboardKeyboard);
                });
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
        switchToSelectionKeyboardButton.setOnClickListener(view -> {
            KeyboardAction switchToSelectionKeyboard = new KeyboardAction(
                    KeyboardActionType.INPUT_KEY,
                    "",
                    null,
                    CustomKeycode.SWITCH_TO_SELECTION_KEYPAD.getKeyCode(),
                    0,
                    0);
            actionListener.handleInputKey(switchToSelectionKeyboard);
        });
    }

    private void setupSwitchToEmojiKeyboardButton() {
        ImageButton switchToEmojiKeyboardButton = findViewById(R.id.switchToEmojiKeyboard);
        switchToEmojiKeyboardButton.setOnClickListener(view -> {
            KeyboardAction switchToEmojiKeyboard = new KeyboardAction(
                    KeyboardActionType.INPUT_KEY,
                    "",
                    null,
                    CustomKeycode.SWITCH_TO_EMOTICON_KEYBOARD.getKeyCode(),
                    0,
                    0);
            actionListener.handleInputKey(switchToEmojiKeyboard);
        });
    }

    protected void setImageButtonTint(int tintColor, int id) {
        ImageButton button = findViewById(id);
        button.setColorFilter(tintColor);
    }

    protected void setColors() {
        Context context = getContext();
        int backgroundColor =
                ColorsHelper.getThemeColor(context, R.attr.backgroundColor,
                        R.string.pref_board_bg_color_key,
                        R.color.defaultBoardBg);
        int tintColor =
                ColorsHelper.getThemeColor(context, R.attr.colorOnBackground,
                        R.string.pref_board_fg_color_key,
                        R.color.defaultBoardFg);

        this.setBackgroundColor(backgroundColor);
        setImageButtonTint(tintColor, R.id.clipboardButton);
        setImageButtonTint(tintColor, R.id.goToSettingsButton);
        setImageButtonTint(tintColor, R.id.tabButton);
        setImageButtonTint(tintColor, R.id.switchToSelectionKeyboard);
        setImageButtonTint(tintColor, R.id.switchToEmojiKeyboard);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Dimension computedDimension = InputMethodViewHelper.onMeasureHelper(
                MeasureSpec.getSize(widthMeasureSpec),
                MeasureSpec.getSize(heightMeasureSpec),
                getResources().getConfiguration().orientation);

        setMeasuredDimension(computedDimension.getWidth(), computedDimension.getHeight());

        super.onMeasure(
                MeasureSpec.makeMeasureSpec(computedDimension.getWidth(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(computedDimension.getHeight(), MeasureSpec.EXACTLY));
    }

}