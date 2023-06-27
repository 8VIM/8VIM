package inc.flide.vim8.views;

import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.emoji2.emojipicker.EmojiPickerView;

public class EmojiView extends ConstraintLayout {
    public EmojiView(Context context) {
        super(context);
        initialize(context);
    }

    public EmojiView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public EmojiView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    private void initialize(Context context) {
        EmojiPickerView emojiPickerView = new EmojiPickerView(context);
        emojiPickerView.setEmojiGridColumns(9);
        emojiPickerView.setEmojiGridRows(5);
        addView(emojiPickerView);
    }
}
