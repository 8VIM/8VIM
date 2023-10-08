package inc.flide.vim8.views.mainkeyboard;

import android.content.Context;
import android.util.AttributeSet;
import inc.flide.vim8.MainInputMethodService;
import inc.flide.vim8.R;
import inc.flide.vim8.ime.actionlisteners.MainKeypadActionListener;
import inc.flide.vim8.views.ConstraintLayoutWithSidebar;

public class MainKeyboardView extends ConstraintLayoutWithSidebar<MainKeypadActionListener> {
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

    @Override
    protected void initializeActionListener(Context context) {
        actionListener = new MainKeypadActionListener((MainInputMethodService) context, this);
    }

    @Override
    protected int getSidebarLayout(boolean isSidebarOnLeft) {
        if (isSidebarOnLeft) {
            return R.layout.main_keyboard_left_sidebar_view;
        } else {
            return R.layout.main_keyboard_right_sidebar_view;
        }
    }

    @Override
    protected void setupButtonsOnSideBar() {
        super.setupButtonsOnSideBar();
        setupSwitchToClipboardKeypadButton();
    }
}
