package inc.flide.vim8.views.mainkeyboard;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import inc.flide.vim8.MainInputMethodService;
import inc.flide.vim8.R;
import inc.flide.vim8.keyboardactionlisteners.MainKeypadActionListener;
import inc.flide.vim8.preferences.SharedPreferenceHelper;
import inc.flide.vim8.ui.Theme;
import inc.flide.vim8.views.ConstraintLayoutWithSidebar;


public class MainKeyboardView extends ConstraintLayoutWithSidebar {

    private MainKeypadActionListener actionListener;
    private String prefSidebarLeftKey;
    private String prefSidebarVisibilityKey;
    private SharedPreferenceHelper sharedPreferenceHelper;
    private LayoutInflater inflater;

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
        prefSidebarVisibilityKey = context.getString(R.string.pref_sidebar_visibility_key);
        Theme.getInstance(context).onChange(this::setColors);
        sharedPreferenceHelper = SharedPreferenceHelper
                .getInstance(context)
                .addListener(this::initializeView, prefSidebarLeftKey)
                .addListener(this::initializeView, prefSidebarVisibilityKey);

        initializeView();
    }

    private void initializeView() {
        setupMainKeyboardView();
        setupButtonsOnSideBar(actionListener);
        setupSwitchToClipboardKeypadButton();
        setColors();
        setHapticFeedbackEnabled(true);
    }

    private void setupMainKeyboardView() {
        boolean preferredSidebarLeft = sharedPreferenceHelper.getBoolean(prefSidebarLeftKey, true);
        removeAllViews();
        if (preferredSidebarLeft) {
            inflater.inflate(R.layout.main_keyboard_left_sidebar_view, this, true);
        } else {
            inflater.inflate(R.layout.main_keyboard_right_sidebar_view, this, true);
        }
        boolean preferredSidebarVisibility = sharedPreferenceHelper.getBoolean(prefSidebarVisibilityKey, true);
        if (!preferredSidebarVisibility) {
            View sidebar = this.findViewById(R.id.sidebarButtonsLayout);
            LayoutParams params = (LayoutParams) sidebar.getLayoutParams();
            params.horizontalWeight = 0;
            sidebar.setLayoutParams(params);
        }
    }
}
