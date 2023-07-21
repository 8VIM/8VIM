package inc.flide.vim8.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import inc.flide.vim8.MainInputMethodService;
import inc.flide.vim8.R;
import inc.flide.vim8.keyboardactionlisteners.ClipboardActionListener;
import inc.flide.vim8.preferences.SharedPreferenceHelper;
import java.text.MessageFormat;
import java.util.List;

public class ClipboardKeypadView extends ConstraintLayoutWithSidebar {

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
        setupOverallView(context);
        setupButtonsOnSideBar(actionListener);
        setColors();
        setHapticFeedbackEnabled(true);

        SharedPreferenceHelper.getInstance(context).addListener(this::setColors);
    }
    private void setupOverallView(Context context) {
        LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        boolean preferredSidebarLeft = SharedPreferenceHelper
                .getInstance(context)
                .getBoolean(
                        context.getString(R.string.pref_sidebar_left_key),
                        true);

        if (preferredSidebarLeft) {
            inflater.inflate(R.layout.clipboard_keypad_left_sidebar_view, this, true);
        } else {
            inflater.inflate(R.layout.clipboard_keypad_right_sidebar_view, this, true);
        }
        setupClipboardListView();
    }


    public void setupClipboardListView() {

        Log.d("clipboard_history", "setup clipboard list view called");
        List<String> clipHistory = actionListener.getClipHistory();
        ListView clipboardItemsList = this.findViewById(R.id.clipboardItemsList);

        adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, (clipHistory)) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);
                textView.setText(
                        MessageFormat.format(
                                "{0}{1}{2}",
                                String.valueOf(position + 1),
                                ". ",
                                getItem(position)));
                textView.setMaxLines(2);
                return view;
            }
        };
        clipboardItemsList.setAdapter(adapter);
        Log.d("clipboard_history", "adapter created and set");

        clipboardItemsList.setOnItemClickListener((parent, itemView, position, id) -> {
            String selectedClip = adapter.getItem(position);
            actionListener.onClipSelected(selectedClip);
        });
    }

    public void updateClipHistory() {
        if (adapter == null) {
            Log.e("clipboard_history", "adapter seems to be null for no reason");
        } else {

        List<String> clipHistory = actionListener.getClipHistory();
        adapter.clear();
        adapter.addAll(clipHistory);
        adapter.notifyDataSetChanged();
        }
    }

}
