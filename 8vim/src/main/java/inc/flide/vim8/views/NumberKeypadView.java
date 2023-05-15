package inc.flide.vim8.views;

import android.content.Context;
import android.content.res.Resources;
import com.hijamoya.keyboardview.Keyboard;
import inc.flide.vim8.MainInputMethodService;
import inc.flide.vim8.R;
import inc.flide.vim8.keyboardactionlisteners.ButtonKeypadActionListener;
import inc.flide.vim8.preferences.SharedPreferenceHelper;
import inc.flide.vim8.structures.Constants;
import inc.flide.vim8.utils.ColorsHelper;
import java.util.Currency;
import java.util.Locale;

public class NumberKeypadView extends ButtonKeypadView {

    public NumberKeypadView(Context context) {
        super(context);
        initialize(context);
    }

    public void initialize(Context context) {
        MainInputMethodService mainInputMethodService = (MainInputMethodService) context;

        Keyboard keyboard = new Keyboard(context, R.layout.number_keypad_view);
        setCurrencySymbolBasedOnLocale(keyboard);
        setColors(keyboard);
        setKeyboard(keyboard);

        ButtonKeypadActionListener actionListener = new ButtonKeypadActionListener(mainInputMethodService, this);
        setOnKeyboardActionListener(actionListener);
        SharedPreferenceHelper.getInstance(getContext()).addListener(() -> setColors(keyboard));
    }

    private void setCurrencySymbolBasedOnLocale(Keyboard keyboard) {
        for (Keyboard.Key key : keyboard.getKeys()) {
            if (key.label != null && key.label.toString().equals(getResources().getString(R.string.currencySymbol))) {
                Currency currency = Currency.getInstance(Locale.getDefault());
                key.label = currency.getSymbol();
                key.text = currency.getSymbol();
            }
        }
    }

    private void setColors(Keyboard keyboard) {
        Resources resources = getResources();
        int foregroundColor = ColorsHelper.getThemeColor(getContext(), R.attr.colorOnBackground);
//        int foregroundColor = SharedPreferenceHelper.getInstance(getContext()).getInt(
//                resources.getString(R.string.pref_board_fg_color_key),
//                resources.getColor(R.color.defaultBoardFg));
        // Tint icon keys
        for (Keyboard.Key key : keyboard.getKeys()) {
            if (key.icon != null) {
                // Has to be mutated, otherwise icon has linked alpha to same key
                // on xpad view
                key.icon = key.icon.mutate();
                key.icon.setTint(foregroundColor);
                key.icon.setAlpha(Constants.MAX_RGB_COMPONENT_VALUE);
            }
        }
        invalidate();
    }
}
