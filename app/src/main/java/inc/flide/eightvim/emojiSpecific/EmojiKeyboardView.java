package inc.flide.eightvim.emojiSpecific;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.astuetz.PagerSlidingTabStrip;

import inc.flide.eightvim.EightVimInputMethodService;
import inc.flide.eightvim.R;
import inc.flide.eightvim.keyboardHelpers.KeyboardAction;
import inc.flide.eightvim.structures.InputSpecialKeyEventCode;
import inc.flide.eightvim.structures.KeyboardActionType;

public class EmojiKeyboardView extends View implements SharedPreferences.OnSharedPreferenceChangeListener{

    private ViewPager viewPager;
    private PagerSlidingTabStrip pagerSlidingTabStrip;
    private LinearLayout layout;

    private EmojiPagerAdapter emojiPagerAdapter;
    private EightVimInputMethodService eightVimInputMethodService;

    public EmojiKeyboardView(Context context) {
        super(context);
        initialize(context);
    }

    public EmojiKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public EmojiKeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    private void initialize(Context context) {

        eightVimInputMethodService = (EightVimInputMethodService) context;

        LayoutInflater inflater = (LayoutInflater)   getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        layout = (LinearLayout) inflater.inflate(R.layout.emoji_keyboard_view, null);

        viewPager = (ViewPager) layout.findViewById(R.id.emojiKeyboard);

        pagerSlidingTabStrip = (PagerSlidingTabStrip) layout.findViewById(R.id.emojiCategorytab);

        pagerSlidingTabStrip.setIndicatorColor(getResources().getColor(R.color.pager_color));

        emojiPagerAdapter = new EmojiPagerAdapter(context, viewPager, height);

        viewPager.setAdapter(emojiPagerAdapter);

        setupDeleteButton();
        setupKeyboardButton();

        pagerSlidingTabStrip.setViewPager(viewPager);

        viewPager.setCurrentItem(1);

        PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(this);
    }

    public View getView() {
        return layout;
    }

    public void notifyDataSetChanged() {
        emojiPagerAdapter.notifyDataSetChanged();
        viewPager.refreshDrawableState();
    }

    private void setupDeleteButton() {

        Button delete = (Button) layout.findViewById(R.id.deleteButton);

        delete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                eightVimInputMethodService.sendDownAndUpKeyEvent(KeyEvent.KEYCODE_DEL, 0);
            }
        });

        delete.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //eightVimInputMethodService.switchToPreviousInputMethod();
                return false;
            }
        });
    }

    private void setupKeyboardButton() {

        Button delete = (Button) layout.findViewById(R.id.switchToKeyboard);

        delete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                KeyboardAction switchToEightVimKeyboardView = new KeyboardAction(
                        KeyboardActionType.INPUT_SPECIAL
                        , InputSpecialKeyEventCode.SWITCH_TO_MAIN_KEYBOARD.toString()
                        , null, 0,0);
                eightVimInputMethodService.handleSpecialInput(switchToEightVimKeyboardView);
            }
        });

        delete.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //eightVimInputMethodService.switchToPreviousInputMethod();
                return false;
            }
        });
    }


    private int width;
    private int height;
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);

        Log.d("emojiKeyboardView", width +" : " + height);
        setMeasuredDimension(width, height);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        Log.d("sharedPreferenceChange", "function called on change of shared preferences with key " + key);
        if (key.equals("icon_set")){
            emojiPagerAdapter = new EmojiPagerAdapter(getContext(), viewPager, height);
            viewPager.setAdapter(emojiPagerAdapter);
            this.invalidate();
        }
    }
}
