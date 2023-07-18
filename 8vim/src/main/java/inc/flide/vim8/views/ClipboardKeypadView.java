package inc.flide.vim8.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Set;

import inc.flide.vim8.MainInputMethodService;
import inc.flide.vim8.geometry.Dimension;
import inc.flide.vim8.keyboardactionlisteners.ClipboardActionListener;
import inc.flide.vim8.keyboardhelpers.InputMethodViewHelper;

public class ClipboardKeypadView extends ListView {

    private ClipboardActionListener actionListener;

    public ClipboardKeypadView(Context context) {
        super(context);
        initialize(context);
    }

    public ClipboardKeypadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public ClipboardKeypadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }
    public void initialize(Context context) {
        actionListener = new ClipboardActionListener((MainInputMethodService) context, this);

        Set<String> clipHistory = actionListener.getClipHistory();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, new ArrayList<>(clipHistory));
        setAdapter(adapter);

        setOnItemClickListener((parent, view, position, id) -> {
            String selectedClip = adapter.getItem(position);
            actionListener.onClipSelected(selectedClip);
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
