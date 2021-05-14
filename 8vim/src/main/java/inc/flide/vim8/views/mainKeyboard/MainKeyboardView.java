package inc.flide.vim8.views.mainKeyboard;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.widget.ImageButton;

import androidx.constraintlayout.widget.ConstraintLayout;

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
        Resources resources = getResources();

        int backgroundColor = SharedPreferenceHelper.getInstance(getContext()).getInt(
                resources.getString(R.string.pref_board_bg_color_key),
                resources.getColor(R.color.defaultBoardBg));

        int foregroundColor = SharedPreferenceHelper.getInstance(getContext()).getInt(
                resources.getString(R.string.pref_board_fg_color_key),
                resources.getColor(R.color.defaultBoardFg));

        actionListener = new MainKeypadActionListener((MainInputMethodService) context, this);
        setupMainKeyboardView(context);
        setBackgroundColor(backgroundColor);
        setupButtonsOnSideBar(foregroundColor);
        setHapticFeedbackEnabled(true);
    }

    private void setupMainKeyboardView(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        boolean preferredSidebarLeft = SharedPreferenceHelper
                .getInstance(context)
                .getBoolean(
                        context.getString(R.string.pref_sidebar_left_key),
                        true);

        if (preferredSidebarLeft) {
            inflater.inflate(R.layout.main_keyboard_left_sidebar_view, this, true);
        } else {
            inflater.inflate(R.layout.main_keyboard_right_sidebar_view, this, true);
        }
    }

    private void setupButtonsOnSideBar(int tintColor) {
        setupSwitchToEmojiKeyboardButton(tintColor);
        setupSwitchToSelectionKeyboardButton(tintColor);
        setupTabKey(tintColor);
        setupGoToSettingsButton(tintColor);
        setupCtrlKey(tintColor);
    }

    private void setupCtrlKey(int color) {
        ImageButton ctrlKeyButton = findViewById(R.id.ctrlButton);
        ctrlKeyButton.setColorFilter(color);

        ctrlKeyButton.setOnClickListener(view -> actionListener.setModifierFlags(KeyEvent.META_CTRL_MASK));
    }

    private void setupGoToSettingsButton(int color) {
        ImageButton goToSettingsButton = findViewById(R.id.goToSettingsButton);
        goToSettingsButton.setColorFilter(color);

        goToSettingsButton.setOnClickListener(view -> {
            Intent vim8SettingsIntent = new Intent(getContext(), SettingsActivity.class);
            vim8SettingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(vim8SettingsIntent);
        });
    }

    private void setupTabKey(int color) {
        ImageButton tabKeyButton = findViewById(R.id.tabButton);
        tabKeyButton.setColorFilter(color);

        tabKeyButton.setOnClickListener(view -> actionListener.handleInputKey(KeyEvent.KEYCODE_TAB, 0));
    }

    private void setupSwitchToSelectionKeyboardButton(int color) {
        ImageButton switchToSelectionKeyboardButton = findViewById(R.id.switchToSelectionKeyboard);
        switchToSelectionKeyboardButton.setColorFilter(color);
        switchToSelectionKeyboardButton.setOnClickListener(view -> {
            KeyboardAction switchToSelectionKeyboard = new KeyboardAction(
                    KeyboardActionType.INPUT_KEY
                    , "", null
                    , CustomKeycode.SWITCH_TO_SELECTION_KEYPAD.getKeyCode(), 0);
            actionListener.handleInputKey(switchToSelectionKeyboard);
        });
    }

    private void setupSwitchToEmojiKeyboardButton(int color) {
        ImageButton switchToEmojiKeyboardButton = findViewById(R.id.switchToEmojiKeyboard);
        switchToEmojiKeyboardButton.setColorFilter(color);
        switchToEmojiKeyboardButton.setOnClickListener(view -> {
            KeyboardAction switchToEmojiKeyboard = new KeyboardAction(
                    KeyboardActionType.INPUT_KEY
                    , "", null
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
                        MeasureSpec.EXACTLY
                ),
                MeasureSpec.makeMeasureSpec(
                        computedDimension.getHeight(),
                        MeasureSpec.EXACTLY
                )
        );
    }

}
