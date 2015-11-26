package inc.flide.eightvim;

import android.inputmethodservice.InputMethodService;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;

public class EightVimInputMethodService extends InputMethodService {

    EightVimKeyboardView eightVimKeyboardView;

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
    public View onCreateInputView()
    {
        eightVimKeyboardView = new EightVimKeyboardView(this);
        return eightVimKeyboardView;
    }

    /** Helper to commit text to input */
    public void sendText(String str)
    {
        getCurrentInputConnection().commitText(str,1);
    }

    /** Helper to send a special key to input */
    public void sendKey(int keyEventCode)
    {
        getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
        getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
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
    }
}
