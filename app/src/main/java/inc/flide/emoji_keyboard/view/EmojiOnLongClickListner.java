package inc.flide.emoji_keyboard.view;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import java.util.ArrayList;
import java.util.List;

import inc.flide.eightvim.R;
import inc.flide.emoji_keyboard.InputMethodServiceProxy;
import inc.flide.emoji_keyboard.adapter.BaseEmojiAdapter;
import inc.flide.emoji_keyboard.utilities.CategorizedEmojiList;
import inc.flide.emoji_keyboard.sqlite.EmojiDataSource;
import inc.flide.emoji_keyboard.utilities.Emoji;

public class EmojiOnLongClickListner implements View.OnLongClickListener {

    private Emoji emoji;
    private InputMethodServiceProxy inputMethodService;

    public EmojiOnLongClickListner(Emoji emoji, InputMethodServiceProxy inputMethodService) {
        this.emoji = emoji;
        this.inputMethodService = inputMethodService;
    }

    @Override
    public boolean onLongClick(View view) {
        setupDiversityEmojiPopup((ImageView) view);
        return true;
    }

    private void setupDiversityEmojiPopup(ImageView imageView) {
        LayoutInflater layoutInflater = (LayoutInflater)inputMethodService.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.popup, null);
        final PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout popupViewLinearLayout = (LinearLayout) popupView.findViewById(R.id.popupWindowLinearLayout);

        List<ImageView> diversityEmojis = getDiversityEmojisImageViewList(popupWindow);
        for(ImageView view: diversityEmojis) {
            popupViewLinearLayout.addView(view);
        }

        popupWindow.setTouchable(true);
        popupWindow.setOutsideTouchable(true);

        popupWindow.showAsDropDown(imageView,0,-imageView.getHeight()*2);
    }

    protected List<ImageView> getDiversityEmojisImageViewList(final PopupWindow popupWindow) {

        List<Emoji> diversityEmojiList = CategorizedEmojiList.getInstance().getDiversityEmojisList(emoji);

        List<ImageView> diversityEmojiImageViewList = new ArrayList<>();

        for (final Emoji emoji: diversityEmojiList) {
            ImageView imageView = BaseEmojiAdapter.setupImageView(null, emoji);

            imageView.setOnClickListener(new PopupWindowEmojiOnClickListner(emoji, inputMethodService, popupWindow));
            diversityEmojiImageViewList.add(imageView);
        }
        return diversityEmojiImageViewList;
    }

}
