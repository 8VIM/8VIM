package inc.flide.vim8.views;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.security.Key;

import inc.flide.vim8.R;
import inc.flide.vim8.keyboardactionlisteners.KeypadActionListener;
import inc.flide.vim8.structures.CustomKeycode;
import inc.flide.vim8.structures.KeyboardAction;
import inc.flide.vim8.structures.KeyboardActionType;
import inc.flide.vim8.ui.SettingsActivity;

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

    public ConstraintLayoutWithSidebar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
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
}
