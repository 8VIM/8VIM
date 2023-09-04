package inc.flide.vim8.views;

import android.content.Context;
import com.hijamoya.keyboardview.Keyboard;
import inc.flide.vim8.R;
import inc.flide.vim8.ime.actionlisteners.ButtonKeypadActionListener;
import java.util.Currency;
import java.util.Locale;

public class NumberKeypadView extends ButtonKeypadView {

    public NumberKeypadView(Context context) {
        super(context);
        initialize(context);
    }

    public void initialize(Context context) {
        super.initialize(context);
        setCurrencySymbolBasedOnLocale();
        ButtonKeypadActionListener actionListener =
                new ButtonKeypadActionListener(mainInputMethodService, this);
        setOnKeyboardActionListener(actionListener);
    }

    @Override
    protected int getLayoutView() {
        return R.layout.number_keypad_view;
    }

    private void setCurrencySymbolBasedOnLocale() {
        for (Keyboard.Key key : keyboard.getKeys()) {
            if (key.label != null && key.label.toString().equals(getResources().getString(R.string.currencySymbol))) {
                Currency currency = Currency.getInstance(Locale.getDefault());
                key.label = currency.getSymbol();
                key.text = currency.getSymbol();
            }
        }
    }
}
