package inc.flide.vim8.ui.views.mainkeyboard;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import inc.flide.vim8.MainInputMethodService;
import inc.flide.vim8.R;
import inc.flide.vim8.geometry.Dimension;
import inc.flide.vim8.keyboardactionlisteners.MainKeypadActionListener;
import inc.flide.vim8.keyboardhelpers.InputMethodViewHelper;
import inc.flide.vim8.preferences.SharedPreferenceHelper;
import inc.flide.vim8.structures.CustomKeycode;
import inc.flide.vim8.ui.activities.SettingsActivity;
import inc.flide.vim8.utils.ColorsHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class MainKeyboardView extends ConstraintLayout {
    private MainKeypadActionListener actionListener;
    private List<ImageButton> imageButtons;
    private SharedPreferenceHelper sharedPreferenceHelper;
    private LayoutInflater inflater;
    private String prefSidebarLeftKey;

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
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        prefSidebarLeftKey = context.getString(R.string.pref_sidebar_left_key);
        sharedPreferenceHelper = SharedPreferenceHelper
                .getInstance(context)
                .addListener(this::setColors,
                        context.getString(R.string.pref_board_fg_color_key),
                        context.getString(R.string.pref_board_bg_color_key),
                        context.getString(R.string.pref_color_mode_key))
                .addListener(this::initializeView, prefSidebarLeftKey);
        initializeView();
    }

    private void initializeView() {
        setupMainKeyboardView();
        setupButtonsOnSideBar();
        setColors();
        setHapticFeedbackEnabled(true);
    }


    private List<ImageButton> getImageButtons(ViewGroup root) {
        List<ImageButton> acc = new ArrayList<>();
        getImageButtons(root, acc);
        return acc;
    }

    private void getImageButtons(ViewGroup root, List<ImageButton> acc) {
        final int childCount = root.getChildCount();
        IntStream.range(0, childCount).forEach(i -> {
            final View child = root.getChildAt(i);
            if (child instanceof ViewGroup) {
                getImageButtons((ViewGroup) child, acc);
            }

            if (child instanceof ImageButton) {
                ImageButton button = (ImageButton) child;
                acc.add(button);
            }
        });
    }

    private void setupMainKeyboardView() {
        boolean preferredSidebarLeft = sharedPreferenceHelper.getBoolean(prefSidebarLeftKey, true);
        removeAllViews();
        if (preferredSidebarLeft) {
            inflater.inflate(R.layout.main_keyboard_left_sidebar_view, this, true);
        } else {
            inflater.inflate(R.layout.main_keyboard_right_sidebar_view, this, true);
        }
        imageButtons = getImageButtons(this);
    }

    private void setupButtonsOnSideBar() {
        setupSwitchToEmojiKeyboardButton();
        setupSwitchToSelectionKeyboardButton();
        setupTabKey();
        setupGoToSettingsButton();
        setupCtrlKey();
    }

    private void setColors() {
        Context context = getContext();
        int backgroundColor = ColorsHelper.getThemeColor(context, R.attr.colorSurface, R.string.pref_board_bg_color_key,
                R.color.defaultBoardBg);
        int tintColor = ColorsHelper.getThemeColor(context, R.attr.colorOnSurface, R.string.pref_board_fg_color_key,
                R.color.defaultBoardFg);

        this.setBackgroundColor(backgroundColor);
        imageButtons.forEach(button -> button.setColorFilter(tintColor));
    }

    private void setupSwitchToSelectionKeyboardButton() {
        ImageButton switchToSelectionKeyboardButton = findViewById(R.id.switchToSelectionKeyboard);
        switchToSelectionKeyboardButton.setOnClickListener(
                view -> actionListener.handleInputKey(CustomKeycode.SWITCH_TO_SELECTION_KEYPAD.getKeyCode(), 0));
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

    private void setupSwitchToEmojiKeyboardButton() {
        ImageButton switchToEmojiKeyboardButton = findViewById(R.id.switchToEmojiKeyboard);
        switchToEmojiKeyboardButton.setOnClickListener(
                view -> actionListener.handleInputKey(CustomKeycode.SWITCH_TO_EMOTICON_KEYBOARD.getKeyCode(), 0));
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Dimension computedDimension = InputMethodViewHelper.computeDimension(getResources());
        setMeasuredDimension(computedDimension.getWidth(), computedDimension.getHeight());
        super.onMeasure(MeasureSpec.makeMeasureSpec(computedDimension.getWidth(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(computedDimension.getHeight(), MeasureSpec.EXACTLY));
    }
}