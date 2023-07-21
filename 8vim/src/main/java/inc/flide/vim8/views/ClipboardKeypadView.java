package inc.flide.vim8.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import inc.flide.vim8.MainInputMethodService;
import inc.flide.vim8.R;
import inc.flide.vim8.geometry.Dimension;
import inc.flide.vim8.keyboardactionlisteners.ClipboardActionListener;
import inc.flide.vim8.keyboardhelpers.InputMethodViewHelper;
import inc.flide.vim8.preferences.SharedPreferenceHelper;
import inc.flide.vim8.structures.CustomKeycode;
import inc.flide.vim8.structures.KeyboardAction;
import inc.flide.vim8.structures.KeyboardActionType;
import inc.flide.vim8.utils.ColorsHelper;

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

    private void setImageButtonTint(int tintColor, int id) {
        ImageButton button = findViewById(id);
        button.setColorFilter(tintColor);
    }

    private void setColors() {
        Context context = getContext();
        int backgroundColor =
                ColorsHelper.getThemeColor(context, R.attr.backgroundColor,
                        R.string.pref_board_bg_color_key,
                        R.color.defaultBoardBg);
        int tintColor =
                ColorsHelper.getThemeColor(context, R.attr.colorOnBackground,
                        R.string.pref_board_fg_color_key,
                        R.color.defaultBoardFg);

        this.setBackgroundColor(backgroundColor);
        setImageButtonTint(tintColor, R.id.clipboardButton);
        setImageButtonTint(tintColor, R.id.goToSettingsButton);
        setImageButtonTint(tintColor, R.id.tabButton);
        setImageButtonTint(tintColor, R.id.switchToSelectionKeyboard);
        setImageButtonTint(tintColor, R.id.switchToEmojiKeyboard);
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

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Dimension computedDimension = InputMethodViewHelper.onMeasureHelper(
                MeasureSpec.getSize(widthMeasureSpec),
                MeasureSpec.getSize(heightMeasureSpec),
                getResources().getConfiguration().orientation);

        setMeasuredDimension(computedDimension.getWidth(), computedDimension.getHeight());

        super.onMeasure(
                MeasureSpec.makeMeasureSpec(computedDimension.getWidth(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(computedDimension.getHeight(), MeasureSpec.EXACTLY));
    }
}
