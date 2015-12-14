package inc.flide.eightvim;

import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.KeyboardView;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import inc.flide.eightvim.keyboardHelpers.FingerPosition;
import inc.flide.eightvim.keyboardHelpers.KeyboardAction;
import inc.flide.eightvim.keyboardHelpers.KeyboardActionXmlParser;
import inc.flide.eightvim.views.EightVimKeyboardView;
import inc.flide.eightvim.views.NumberPadKeyboardView;
import inc.flide.logging.Logger;

public class EightVimInputMethodService extends InputMethodService implements KeyboardView.OnKeyboardActionListener{

    private EightVimKeyboardView eightVimKeyboardView;
    private boolean isEightVimKeyboardViewVisible;

    private NumberPadKeyboardView numberPadKeyboardView;


    private boolean isShiftLockOn;
    private boolean isCapsLockOn;
    Map<List<FingerPosition>, KeyboardAction> keyboardActionMap;

    @Override
    public View onCreateInputView() {

        numberPadKeyboardView = (NumberPadKeyboardView)getLayoutInflater().inflate(R.layout.keyboard, null);

        eightVimKeyboardView = new EightVimKeyboardView(this);
        isEightVimKeyboardViewVisible = true;
        return eightVimKeyboardView;

    }


    @Override
    public void onStartInput (EditorInfo attribute, boolean restarting){
        super.onStartInput(attribute, restarting);
    }

    @Override
    public void onStartInputView (EditorInfo info, boolean restarting){
        super.onStartInputView(info, restarting);
    }

    @Override
    public void onInitializeInterface(){
        super.onInitializeInterface();
        initializeKeyboardActionMap();
        isShiftLockOn = false;
        isCapsLockOn = false;
    }

    /** Helper to commit text to input */
    public void sendText(String str) {
        getCurrentInputConnection().commitText(str, 1);
    }

    /** Helper to send a special key to input */
    public void sendKey(int keyEventCode) {
        getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
        getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
    }

    private void initializeKeyboardActionMap() {

        InputStream inputStream = null;
        try{
            inputStream = getResources().openRawResource(getResources().getIdentifier("raw/keyboard_actions", "raw", getPackageName()));
            KeyboardActionXmlParser keyboardActionXmlParser = new KeyboardActionXmlParser(inputStream);
            keyboardActionMap = keyboardActionXmlParser.readKeyboardActionMap();

        } catch (XmlPullParserException exception){
            exception.printStackTrace();
        } catch (IOException exception){
            exception.printStackTrace();
        } catch(Exception exception){
            exception.printStackTrace();
        }
        finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void processMovementSequence(List<FingerPosition> movementSequence) {

        KeyboardAction keyboardAction = keyboardActionMap.get(movementSequence);
        boolean isMovementValid = true;
        if(keyboardAction == null){
            Logger.Warn(this, "No Action Mapping has been defined for the given Sequence : " + movementSequence.toString());
            movementSequence.clear();
            return;
        }

        switch (keyboardAction.getKeyboardActionType()){
            case INPUT_TEXT:
                handleInputText(keyboardAction);
                break;
            case INPUT_KEY:
                handleInputKey(keyboardAction);
                break;
            case INPUT_SPECIAL:
                handleSpecialInput(keyboardAction);
                break;
            default:
                Logger.Warn(this, "Action Type Undefined : " + keyboardAction.getKeyboardActionType().toString());
                isMovementValid = false;
                break;
        }
        if(isMovementValid){
            eightVimKeyboardView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        }
    }

    private void handleInputText(KeyboardAction keyboardAction) {
        if(keyboardAction.getText().length() == 1 && (isShiftLockOn || isCapsLockOn)){
            sendText(keyboardAction.getText().toUpperCase());
            isShiftLockOn = false;
        }else{
            sendText(keyboardAction.getText());
        }
    }

    private void handleInputKey(KeyboardAction keyboardAction) {
        sendKey(keyboardAction.getKeyEventCode());
    }

    public void handleSpecialInput(KeyboardAction keyboardAction) {
        switch (keyboardAction.getKeyEventCode()){
            case KeyEvent.KEYCODE_SHIFT_RIGHT:
            case KeyEvent.KEYCODE_SHIFT_LEFT:
                if(isShiftLockOn){
                    isShiftLockOn = false;
                    isCapsLockOn = true;
                } else if(isCapsLockOn){
                    isShiftLockOn = false;
                    isCapsLockOn = false;
                } else{
                    isShiftLockOn = true;
                    isCapsLockOn = false;
                }
                break;
            case KeyEvent.KEYCODE_EISU:
                if(isEightVimKeyboardViewVisible){
                    isEightVimKeyboardViewVisible = false;
                    setInputView(numberPadKeyboardView);
                } else {
                    isEightVimKeyboardViewVisible = true;
                    setInputView(eightVimKeyboardView);
                }

                break;
            default:
                Logger.Warn(this, "Special Event undefined for keyCode : " + keyboardAction.getKeyEventCode());
                break;
        }
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

        InputConnection ic = this.getCurrentInputConnection();

        switch(primaryCode){
            case KeyEvent.KEYCODE_EISU:
                KeyboardAction switchToEightVimKeyboardView = new KeyboardAction(KeyboardAction.KeyboardActionType.INPUT_SPECIAL,null, KeyEvent.KEYCODE_EISU);
                this.handleSpecialInput(switchToEightVimKeyboardView);
                break;
            case KeyEvent.KEYCODE_DEL  :
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_SPACE:
            case KeyEvent.KEYCODE_NUMPAD_DOT:
            case KeyEvent.KEYCODE_NUMPAD_SUBTRACT:
                this.sendKey(primaryCode);
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
