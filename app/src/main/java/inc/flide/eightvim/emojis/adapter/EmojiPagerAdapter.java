package inc.flide.eightvim.emojis.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;

import java.util.ArrayList;

import inc.flide.eightvim.R;
import inc.flide.eightvim.Setting;
import inc.flide.eightvim.emojis.CategorizedEmojiList;
import inc.flide.eightvim.emojis.view.EmojiKeyboardView;
import inc.flide.eightvim.keyboardHelpers.EightVimInputMethodServiceHelper;
import inc.flide.logging.Logger;

public class EmojiPagerAdapter extends PagerAdapter implements PagerSlidingTabStrip.IconTabProvider {

    private final int ICONS[] = {R.drawable.ic_emoji_category_people,
                                R.drawable.ic_emoji_category_nature,
                                R.drawable.ic_emoji_category_foods,
                                R.drawable.ic_emoji_category_activity,
                                R.drawable.ic_emoji_category_travel,
                                R.drawable.ic_emoji_category_objects,
                                R.drawable.ic_emoji_category_symbols,
                                R.drawable.ic_emoji_category_flags};

    private ViewPager pager;
    private ArrayList<View> pages;
    private int keyboardHeight;

    public EmojiPagerAdapter(Context context, ViewPager pager, int keyboardHeight) {
        super();

        CategorizedEmojiList categorizedEmojiList = new CategorizedEmojiList(EightVimInputMethodServiceHelper.loadEmojiData(context.getResources(), context.getPackageName()));

        this.pager = pager;
        this.keyboardHeight = keyboardHeight;
        this.pages = new ArrayList<View>();

        String iconSetFilePrefix = getPreferedIconSet();
        Logger.d(this, iconSetFilePrefix);
        pages.add(new EmojiKeyboardView.KeyboardSinglePageView(context, new StaticEmojiAdapter(context, categorizedEmojiList.getPeople(), iconSetFilePrefix)).getView());
        pages.add(new EmojiKeyboardView.KeyboardSinglePageView(context, new StaticEmojiAdapter(context, categorizedEmojiList.getNature(), iconSetFilePrefix)).getView());
        pages.add(new EmojiKeyboardView.KeyboardSinglePageView(context, new StaticEmojiAdapter(context, categorizedEmojiList.getFood(), iconSetFilePrefix)).getView());
        pages.add(new EmojiKeyboardView.KeyboardSinglePageView(context, new StaticEmojiAdapter(context, categorizedEmojiList.getActivity(), iconSetFilePrefix)).getView());
        pages.add(new EmojiKeyboardView.KeyboardSinglePageView(context, new StaticEmojiAdapter(context, categorizedEmojiList.getTravel(), iconSetFilePrefix)).getView());
        pages.add(new EmojiKeyboardView.KeyboardSinglePageView(context, new StaticEmojiAdapter(context, categorizedEmojiList.getObjects(), iconSetFilePrefix)).getView());
        pages.add(new EmojiKeyboardView.KeyboardSinglePageView(context, new StaticEmojiAdapter(context, categorizedEmojiList.getSymbols(), iconSetFilePrefix)).getView());
        pages.add(new EmojiKeyboardView.KeyboardSinglePageView(context, new StaticEmojiAdapter(context, categorizedEmojiList.getFlags(), iconSetFilePrefix)).getView());


    }

    @Override
    public View instantiateItem(ViewGroup container, int position) {
        pager.addView(pages.get(position), position, keyboardHeight);
        return pages.get(position);
    }

    @Override
    public void destroyItem (ViewGroup container, int position, Object object) {
        pager.removeView(pages.get(position));
    }

    @Override
    public int getCount() {
        return ICONS.length;
    }

    @Override
    public int getPageIconResId(int position) {
        return ICONS[position];
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    private String getPreferedIconSet() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(pager.getContext());
        return sharedPreferences.getString(Setting.CHANGE_ICON_SET_KEY, Setting.CHANGE_ICON_SET_VALUE_DEFAULT);
    }
}
