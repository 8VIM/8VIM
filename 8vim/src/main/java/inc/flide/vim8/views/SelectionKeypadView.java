package inc.flide.vim8.views;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.NonNull;
import inc.flide.vim8.R;
import inc.flide.vim8.keyboardactionlisteners.ButtonKeypadActionListener;
import inc.flide.vim8.models.CustomKeycode;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SelectionKeypadView extends ButtonKeypadView {
    private static final Set<Integer> MOVE_CURRENT_END_POINTS = new HashSet<>(
            Arrays.asList(CustomKeycode.MOVE_CURRENT_END_POINT_DOWN.keyCode,
                    CustomKeycode.MOVE_CURRENT_END_POINT_LEFT.keyCode,
                    CustomKeycode.MOVE_CURRENT_END_POINT_UP.keyCode,
                    CustomKeycode.MOVE_CURRENT_END_POINT_RIGHT.keyCode));

    public SelectionKeypadView(Context context) {
        super(context);
        initialize(context);
    }

    public void initialize(Context context) {
        super.initialize(context);
        ButtonKeypadActionListener actionListener = new ButtonKeypadActionListener(mainInputMethodService, this);
        setOnKeyboardActionListener(actionListener);
    }

    @Override
    protected int getLayoutView() {
        return R.layout.selection_keypad_view;
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        if (visibility == VISIBLE) {
            updateCtrlKey();
            updateShiftKey();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent me) {
        boolean result = super.onTouchEvent(me);
        if (result && me.getAction() == MotionEvent.ACTION_UP) {
            if (MOVE_CURRENT_END_POINTS.contains(lastKey)) {
                lastKey = -1;
                mainInputMethodService.resetCtrlState();
                mainInputMethodService.resetShiftState();
                updateCtrlKey();
                updateShiftKey();
            }

        }
        return result;
    }
}
