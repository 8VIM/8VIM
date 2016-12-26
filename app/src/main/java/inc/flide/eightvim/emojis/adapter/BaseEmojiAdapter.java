package inc.flide.eightvim.emojis.adapter;

import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;

import inc.flide.eightvim.EightVimInputMethodService;
import inc.flide.eightvim.R;

public abstract class BaseEmojiAdapter extends BaseAdapter {

    protected EightVimInputMethodService emojiKeyboardService;
    protected ArrayList<String> emojiTexts;
    protected ArrayList<Integer> iconIds;

    public BaseEmojiAdapter(EightVimInputMethodService emojiKeyboardService ) {
        this.emojiKeyboardService = emojiKeyboardService;
    }

    @Override
    public int getCount() {
        return emojiTexts.size();
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

        imageView.setImageResource(iconIds.get(position));
        imageView.setBackgroundResource(R.drawable.btn_background);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emojiKeyboardService.sendText(emojiTexts.get(position));
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
}
