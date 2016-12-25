package inc.flide.eightvim.emojiSpecific;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import inc.flide.eightvim.Setting;
import inc.flide.logging.Logger;

public class EmojiPagerAdapter extends PagerAdapter {

    private final String[] TITLES = { "people",
                                    "things",
                                    "nature",
                                    "places",
                                    "symbols" };


    private ViewPager pager;
    private ArrayList<View> pages;
    private int keyboardHeight;

    public EmojiPagerAdapter(Context context, ViewPager pager, int keyboardHeight) {
        super();

        this.pager = pager;
        this.keyboardHeight = keyboardHeight;
        this.pages = new ArrayList<View>();

        EmojiIcons icons = getPreferedIconSet();
        /*pages.add(new KeyboardSinglePageView(context, new RecentEmojiAdapter(context)).getView());*/
        pages.add(new KeyboardSinglePageView(context, new StaticEmojiAdapter(context, EmojiTexts.peopleEmojiTexts, icons.getPeopleIconIds())).getView());
        pages.add(new KeyboardSinglePageView(context, new StaticEmojiAdapter(context, EmojiTexts.thingsEmojiTexts, icons.getThingsIconIds())).getView());
        pages.add(new KeyboardSinglePageView(context, new StaticEmojiAdapter(context, EmojiTexts.natureEmojiTexts, icons.getNatureIconIds())).getView());
        pages.add(new KeyboardSinglePageView(context, new StaticEmojiAdapter(context, EmojiTexts.transEmojiTexts, icons.getTransIconIds())).getView());
        pages.add(new KeyboardSinglePageView(context, new StaticEmojiAdapter(context, EmojiTexts.otherEmojiTexts, icons.getOtherIconIds())).getView());

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
    public CharSequence getPageTitle(int position) {
        return TITLES[position];
    }

    @Override
    public int getCount() {
        return TITLES.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    private EmojiIcons getPreferedIconSet() {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(pager.getContext());

        if (sharedPreferences
                .getString(Setting.CHANGE_ICON_SET_KEY, Setting.CHANGE_ICON_SET_VALUE_DEFAULT)
                .equals(Setting.CHANGE_ICON_SET_VALUE_GOOGLE)){
            return new Google_EmojiIcons();
        } else if (sharedPreferences
                .getString(Setting.CHANGE_ICON_SET_KEY, Setting.CHANGE_ICON_SET_VALUE_DEFAULT)
                .equals(Setting.CHANGE_ICON_SET_VALUE_APPLE)) {
            return new Apple_EmojiIcons();
        }

        return new Google_EmojiIcons();
    }
}
