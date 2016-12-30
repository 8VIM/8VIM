package inc.flide.eightvim.emojis.adapter;

import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import inc.flide.eightvim.EightVimInputMethodService;
import inc.flide.eightvim.R;
import inc.flide.eightvim.emojis.Emoji;

public abstract class BaseEmojiAdapter extends BaseAdapter {

    protected EightVimInputMethodService emojiKeyboardService;
    protected List<Emoji> emojiList;

    public BaseEmojiAdapter(EightVimInputMethodService emojiKeyboardService ) {
        this.emojiKeyboardService = emojiKeyboardService;
    }

    @Override
    public int getCount() {
        return emojiList.size();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(emojiKeyboardService);
            int scale = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, emojiKeyboardService.getResources().getDisplayMetrics());
            imageView.setPadding(scale, (int)(scale*1.2), scale, (int)(scale * 1.2));
            imageView.setAdjustViewBounds(true);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageResource(getIconId(position));
        imageView.setBackgroundResource(R.drawable.btn_background);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emojiKeyboardService.sendText(getEmojiUnicodeString(position));
            }
        });

        return imageView;
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public abstract int getIconId(int position);
    public abstract String getEmojiUnicodeString(int position);
}
