package inc.flide.eightvim;

import android.inputmethodservice.InputMethodService;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import inc.flide.eightvim.keyboardHelpers.FingerPosition;
import inc.flide.eightvim.keyboardHelpers.KeyboardAction;
import inc.flide.logging.Logger;

public class EightVimInputMethodService extends InputMethodService {

    EightVimKeyboardView eightVimKeyboardView;
    private boolean isShiftPressed;
    Map<List<FingerPosition>, KeyboardAction> keyboardActionMap;

    /**
     *Create and return the view hierarchy used for the input area (such as a soft keyboard).
     * This will be called once, when the input area is first displayed.
     * You can return null to have no input area; the default implementation returns null.
     *
     * To control when the input view is displayed, implement onEvaluateInputViewShown().
     * To change the input view after the first one is created by this function, use setInputView(View).
     *
     */
    @Override
    public View onCreateInputView() {
        eightVimKeyboardView = new EightVimKeyboardView(this);
        return eightVimKeyboardView;
    }
    /**
     * Called to inform the input method that text input has started in an editor.
     * You should use this callback to initialize the state of your input to match
     * the state of the editor given to it.
     *
     * @param attribute The attributes of the editor that input is starting in.
     * @param restarting Set to true if input is restarting in the same editor such as
     *                   because the application has changed the text in the editor.
     *                   Otherwise will be false, indicating this is a new session with the editor.
     */
    @Override
    public void onStartInput (EditorInfo attribute, boolean restarting){
        super.onStartInput(attribute, restarting);
    }

    /**
     * Called when the input view is being shown and input has started on a new editor.
     * This will always be called after onStartInput(EditorInfo, boolean),
     * allowing you to do your general setup there and just view-specific setup here.
     * You are guaranteed that onCreateInputView() will have been called some
     * time before this function is called.
     *
     * @param info Description of the type of text being edited.
     * @param restarting Set to true if we are restarting input on the same text field as before.
     */
    @Override
    public void onStartInputView (EditorInfo info, boolean restarting){
        super.onStartInputView(info, restarting);
    }

    /**
     * This is a hook that subclasses can use to perform initialization of their interface.
     * It is called for you prior to any of your UI objects being created,
     * both after the service is first created and after a configuration change happens.
     */
    @Override
    public void onInitializeInterface(){
        super.onInitializeInterface();
        initializeKeyboardActionMap();
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
        //Clear the queue before this function finishes off
        movementSequence.clear();
    }

    private void handleInputText(KeyboardAction keyboardAction) {
        if(keyboardAction.getText().length() == 1 && isShiftPressed){
            sendText(keyboardAction.getText().toUpperCase());
            isShiftPressed = false;
        }else{
            sendText(keyboardAction.getText());
        }
    }

    private void handleInputKey(KeyboardAction keyboardAction) {
        sendKey(keyboardAction.getKeyEventCode());
    }

    private void handleSpecialInput(KeyboardAction keyboardAction) {
        switch (keyboardAction.getKeyEventCode()){
            case KeyEvent.KEYCODE_SHIFT_RIGHT:
            case KeyEvent.KEYCODE_SHIFT_LEFT:
                isShiftPressed = true;
                break;
            default:
                Logger.Warn(this, "Special Event undefined for keyCode : " + keyboardAction.getKeyEventCode());
                break;
        }
    }
}
