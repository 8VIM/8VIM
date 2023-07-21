package inc.flide.vim8.views.mainkeyboard;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import inc.flide.vim8.MainInputMethodService;
import inc.flide.vim8.R;
import inc.flide.vim8.keyboardactionlisteners.MainKeypadActionListener;
import inc.flide.vim8.preferences.SharedPreferenceHelper;
import inc.flide.vim8.views.ConstraintLayoutWithSidebar;


public class MainKeyboardView extends ConstraintLayoutWithSidebar {

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
        setupButtonsOnSideBar(actionListener);
        setColors();
        setHapticFeedbackEnabled(true);

        SharedPreferenceHelper.getInstance(context).addListener(this::setColors);
    }

    private void setupMainKeyboardView(Context context) {
        LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

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


}
