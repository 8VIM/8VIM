package inc.flide.vim8.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Set;

import inc.flide.vim8.MainInputMethodService;
import inc.flide.vim8.geometry.Dimension;
import inc.flide.vim8.keyboardactionlisteners.ClipboardActionListener;
import inc.flide.vim8.keyboardhelpers.InputMethodViewHelper;

public class ClipboardKeypadView extends ListView {

    private ClipboardActionListener actionListener;
    private ArrayAdapter<String> adapter;

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
        adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, new ArrayList<>(clipHistory)){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView1 = view.findViewById(android.R.id.text1);
                textView1.setText(MessageFormat.format("{0}{1}{2}", String.valueOf(position + 1), ". ", getItem(position))); // Display the position number
                textView1.setMaxLines(2); // Limit the clip display to two lines of text
                return view;
            }
        };
        ;
        setAdapter(adapter);

        setOnItemClickListener((parent, view, position, id) -> {
            String selectedClip = adapter.getItem(position);
            actionListener.onClipSelected(selectedClip);
        });
    }

    public void updateClipHistory() {
        Set<String> clipHistory = actionListener.getClipHistory();
        adapter.clear();
        adapter.addAll(new ArrayList<>(clipHistory));
        adapter.notifyDataSetChanged();
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
