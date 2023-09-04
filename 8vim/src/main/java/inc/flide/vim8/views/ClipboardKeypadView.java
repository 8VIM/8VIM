package inc.flide.vim8.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import inc.flide.vim8.MainInputMethodService;
import inc.flide.vim8.R;
import inc.flide.vim8.ime.actionlisteners.ClipboardActionListener;
import java.text.MessageFormat;
import java.util.List;

public class ClipboardKeypadView extends ConstraintLayoutWithSidebar<ClipboardActionListener> {
    private ArrayAdapter<String> adapter;
    private ListView clipboardItemsList;

    public ClipboardKeypadView(Context context) {
        super(context);
    }

    public ClipboardKeypadView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ClipboardKeypadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initializeActionListener(Context context) {
        actionListener = new ClipboardActionListener((MainInputMethodService) context, this);
    }

    @Override
    protected int getSidebarLayout(boolean isSidebarOnLeft) {
        if (isSidebarOnLeft) {
            return R.layout.clipboard_keypad_left_sidebar_view;
        } else {
            return R.layout.clipboard_keypad_right_sidebar_view;
        }
    }

    @Override
    protected void setupMainKeyboardView() {
        super.setupMainKeyboardView();
        setupClipboardListView();
    }

    @Override
    protected void setupButtonsOnSideBar() {
        super.setupButtonsOnSideBar();
        setupSwitchToMainKeyboardButton();
    }

    private void setupClipboardListView() {
        List<String> clipHistory = actionListener.getClipHistory();
        clipboardItemsList = this.findViewById(R.id.clipboardItemsList);
        keyboardTheme.onChange(() -> {
            int children = clipboardItemsList.getChildCount();
            for (int i = 0; i < children; i++) {
                View child = clipboardItemsList.getChildAt(i);

                TextView textView = child.findViewById(android.R.id.text1);
                textView.setTextColor(keyboardTheme.getForegroundColor());

            }
        });
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, (clipHistory)) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);
                textView.setText(
                        MessageFormat.format(
                                "{0}{1}{2}",
                                String.valueOf(position + 1),
                                ". ",
                                getItem(position)));
                textView.setTextColor(keyboardTheme.getForegroundColor());
                textView.setMaxLines(2);
                return view;
            }
        };

        clipboardItemsList.setAdapter(adapter);

        clipboardItemsList.setOnItemClickListener((parent, itemView, position, id) -> {
            String selectedClip = adapter.getItem(position);
            assert selectedClip != null;
            actionListener.onClipSelected(selectedClip);
        });
    }

    public void updateClipHistory() {
        List<String> clipHistory = actionListener.getClipHistory();
        adapter.setNotifyOnChange(false);
        adapter.clear();
        adapter.addAll(clipHistory);
        adapter.notifyDataSetChanged();
        if (clipboardItemsList != null) {
            clipboardItemsList.smoothScrollToPosition(0);
        }
    }

}
