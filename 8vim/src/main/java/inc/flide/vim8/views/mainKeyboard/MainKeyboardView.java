package inc.flide.vim8.views.mainKeyboard;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;

import androidx.constraintlayout.widget.ConstraintLayout;

import androidx.constraintlayout.widget.ConstraintSet;
import inc.flide.vim8.MainInputMethodService;
import inc.flide.vim8.R;
import inc.flide.vim8.geometry.Dimension;
import inc.flide.vim8.keyboardActionListners.MainKeypadActionListener;
import inc.flide.vim8.keyboardHelpers.InputMethodViewHelper;
import inc.flide.vim8.keyboardHelpers.KeyboardAction;
import inc.flide.vim8.preferences.SharedPreferenceHelper;
import inc.flide.vim8.structures.CustomKeycode;
import inc.flide.vim8.structures.KeyboardActionType;
import inc.flide.vim8.ui.SettingsActivity;


public class MainKeyboardView extends ConstraintLayout {

    private MainKeypadActionListener actionListener;

    public MainKeyboardView(Context context) {
        super(context);
        initialize(context);
    }

    public MainKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public MainKeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    public void initialize(Context context) {

        actionListener = new MainKeypadActionListener((MainInputMethodService) context, this);
        setupMainKeyboardView(context);
        setupBackgroundColours();
        setupButtonsOnSideBar();
        setHapticFeedbackEnabled(true);
    }

    private void setupMainKeyboardView(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        String preferredSidebarPositionOnMainKeyboard = SharedPreferenceHelper
                .getInstance(context)
                .getString(
                        context.getString(R.string.mainKeyboard_sidebar_position_preference_key),
                        context.getString(R.string.mainKeyboard_sidebar_position_preference_left_value));

        if (preferredSidebarPositionOnMainKeyboard.equals(context.getString(R.string.mainKeyboard_sidebar_position_preference_right_value))) {
            inflater.inflate(R.layout.main_keyboard_right_sidebar_view, this, true);
        } else {
            inflater.inflate(R.layout.main_keyboard_left_sidebar_view, this, true);
        }
    }

    private void setupBackgroundColours() {
        View sidebar = findViewById(R.id.sidebarButtonsLayout);
        sidebar.setBackgroundColor(getResources().getColor(R.color.secondaryBackground));
    }

    private void setupButtonsOnSideBar() {

        setupSwitchToEmojiKeyboardButton();
        setupSwitchToSelectionKeyboardButton();
        setupTabKey();
        setupGoToSettingsButton();
        setupCtrlKey();

    }

    private void setupCtrlKey() {
        ImageButton ctrlKeyButton = findViewById(R.id.ctrlButton);

        ctrlKeyButton.setOnClickListener(view -> actionListener.setModifierFlags(KeyEvent.META_CTRL_MASK));
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
                    KeyboardActionType.INPUT_KEY
                    , "" , null
                    , CustomKeycode.SWITCH_TO_SELECTION_KEYPAD.getKeyCode(), 0);
            actionListener.handleInputKey(switchToSelectionKeyboard);
        });
    }

    private void setupSwitchToEmojiKeyboardButton() {
        ImageButton switchToEmojiKeyboardButton = findViewById(R.id.switchToEmojiKeyboard);
        switchToEmojiKeyboardButton.setOnClickListener(view -> {
            KeyboardAction switchToEmojiKeyboard = new KeyboardAction(
                    KeyboardActionType.INPUT_KEY
                    , "" , null
                    , CustomKeycode.SWITCH_TO_EMOTICON_KEYBOARD.getKeyCode(), 0);
            actionListener.handleInputKey(switchToEmojiKeyboard);
        });
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Dimension computedDimension = InputMethodViewHelper.onMeasureHelper(
                MeasureSpec.getSize(widthMeasureSpec),
                MeasureSpec.getSize(heightMeasureSpec),
                getResources().getConfiguration().orientation);

        setMeasuredDimension(computedDimension.getWidth(), computedDimension.getHeight());

        super.onMeasure(
                MeasureSpec.makeMeasureSpec(
                        computedDimension.getWidth(),
                        MeasureSpec.getMode(widthMeasureSpec)
                ),
                MeasureSpec.makeMeasureSpec(
                        computedDimension.getHeight(),
                        MeasureSpec.getMode(heightMeasureSpec)
                )
        );
    }

}
