package inc.flide.emoji_keyboard.adapter;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.List;

import inc.flide.eightvim.R;
import inc.flide.emoji_keyboard.InputMethodServiceProxy;
import inc.flide.emoji_keyboard.utilities.Emoji;
import inc.flide.emoji_keyboard.view.EmojiOnClickListner;
import inc.flide.emoji_keyboard.view.EmojiOnLongClickListner;

public abstract class BaseEmojiAdapter extends BaseAdapter {

    protected static InputMethodServiceProxy emojiKeyboardService;
    private static String filePrefix;

    protected List<Emoji> emojiList;

    public BaseEmojiAdapter(InputMethodServiceProxy emojiKeyboardService ) {
        this.emojiKeyboardService = emojiKeyboardService;
    }

    @Override
    public int getCount() {
        if (emojiList == null) {
            return 0;
        }
        return emojiList.size();
    }

    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {

        final ImageView imageView = setupImageView(convertView, emojiList.get(position));

        imageView.setOnClickListener(new EmojiOnClickListner(emojiList.get(position), emojiKeyboardService));

        if (emojiList.get(position).isDiversityAvailable()) {

            Drawable[] layers = new Drawable[2];
            layers[0] = emojiKeyboardService.getContext().getResources().getDrawable(getIconIdBasedOnEmoji(emojiList.get(position)));
            layers[1] = emojiKeyboardService.getContext().getResources().getDrawable(R.drawable.ic_diversityindicator);
            LayerDrawable layerDrawable = new LayerDrawable(layers);

            imageView.setImageDrawable(layerDrawable);
            imageView.setLongClickable(true);

            imageView.setOnLongClickListener(new EmojiOnLongClickListner(emojiList.get(position), emojiKeyboardService));
        }
        return imageView;
    }

    public static ImageView setupImageView(final View convertView, Emoji emoji) {
        final ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(emojiKeyboardService.getContext());
            int scale = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, emojiKeyboardService.getContext().getResources().getDisplayMetrics());
            imageView.setPadding(scale, (int)(scale*1.2), scale, (int)(scale * 1.2));
            imageView.setAdjustViewBounds(true);
        } else {
            imageView = (ImageView) convertView;
            imageView.setLongClickable(false);
        }

        imageView.setImageResource(getIconIdBasedOnEmoji(emoji));
        imageView.setBackgroundResource(R.drawable.btn_background);

        return imageView;
    }

    @Override
    public Object getItem(int position) {
        return emojiList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    private static int getIconIdBasedOnEmoji(Emoji emoji) {
        String resourceString = filePrefix + emoji.getUnicodeHexcode().replace('-','_');
        int resourceId;
        resourceId = emojiKeyboardService.getDrawableResourceId(resourceString);
        if (resourceId == 0) {
            resourceId = emojiKeyboardService.getDrawableResourceId("ic_not_available_sign");
        }

        return resourceId;
    }

    public static void setFilePrefix(String prefix) {
        filePrefix = prefix;
    }
}
