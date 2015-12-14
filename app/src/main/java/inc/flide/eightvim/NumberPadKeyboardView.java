package inc.flide.eightvim;

import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.InputConnection;

import inc.flide.eightvim.keyboardHelpers.KeyboardAction;
import inc.flide.logging.Logger;

/**
 * Created by flide on 30/11/15.
 */
public class NumberPadKeyboardView extends KeyboardView implements KeyboardView.OnKeyboardActionListener {

    private EightVimInputMethodService eightVimInputMethodService;

    private Keyboard keyboard;

    public NumberPadKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public NumberPadKeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    public void initialize(Context context){
        eightVimInputMethodService = (EightVimInputMethodService) context;
        keyboard = new Keyboard(eightVimInputMethodService, R.xml.keyboard_view);
        this.setKeyboard(keyboard);
        this.setOnKeyboardActionListener(this);
    }
    /**
     * Called when the user presses a key. This is sent before the {@link #onKey} is called.
     * For keys that repeat, this is only called once.
     *
     * @param primaryCode the unicode of the key being pressed. If the touch is not on a valid
     *                    key, the value will be zero.
     */
    @Override
    public void onPress(int primaryCode) {

    }

    /**
     * Called when the user releases a key. This is sent after the {@link #onKey} is called.
     * For keys that repeat, this is only called once.
     *
     * @param primaryCode the code of the key that was released
     */
    @Override
    public void onRelease(int primaryCode) {

    }

    /**
     * Send a key press to the listener.
     *
     * @param primaryCode this is the key that was pressed
     * @param keyCodes    the codes for all the possible alternative keys
     *                    with the primary code being the first. If the primary key code is
     *                    a single character such as an alphabet or number or symbol, the alternatives
     *                    will include other characters that may be on the same key or adjacent keys.
     *                    These codes are useful to correct for accidental presses of a key adjacent to
     */
    @Override
    public void onKey(int primaryCode, int[] keyCodes) {

        InputConnection ic = eightVimInputMethodService.getCurrentInputConnection();

        switch(primaryCode){
            case KeyEvent.KEYCODE_EISU:
                KeyboardAction switchToEightVimKeyboardView = new KeyboardAction(KeyboardAction.KeyboardActionType.INPUT_SPECIAL,null, KeyEvent.KEYCODE_EISU);
                eightVimInputMethodService.handleSpecialInput(switchToEightVimKeyboardView);
                break;
            case KeyEvent.KEYCODE_DEL  :
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_SPACE:
            case KeyEvent.KEYCODE_NUMPAD_DOT:
            case KeyEvent.KEYCODE_NUMPAD_SUBTRACT:
                    eightVimInputMethodService.sendKey(primaryCode);
                break;
            default:
                char code = (char)primaryCode;
                ic.commitText(String.valueOf(code),1);
        }
    }

    /**
     * Sends a sequence of characters to the listener.
     *
     * @param text the sequence of characters to be displayed.
     */
    @Override
    public void onText(CharSequence text) {

    }

    /**
     * Called when the user quickly moves the finger from right to left.
     */
    @Override
    public void swipeLeft() {

    }

    /**
     * Called when the user quickly moves the finger from left to right.
     */
    @Override
    public void swipeRight() {

    }

    /**
     * Called when the user quickly moves the finger from up to down.
     */
    @Override
    public void swipeDown() {

    }

    /**
     * Called when the user quickly moves the finger from down to up.
     */
    @Override
    public void swipeUp() {

    }

}
