package inc.flide.vim8;

import static inc.flide.vim8.models.AppPrefsKt.appPreferenceModel;

import android.content.Context;
import android.content.res.Configuration;
import android.inputmethodservice.InputMethodService;
import android.os.IBinder;
import android.os.SystemClock;
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.WindowInsetsControllerCompat;
import com.google.android.material.color.DynamicColors;
import inc.flide.vim8.ime.KeyboardTheme;
import inc.flide.vim8.lib.android.AndroidVersion;
import inc.flide.vim8.models.AppPrefs;
import inc.flide.vim8.models.KeyboardData;
import inc.flide.vim8.models.LayoutKt;
import inc.flide.vim8.services.ClipboardManagerService;
import inc.flide.vim8.views.ClipboardKeypadView;
import inc.flide.vim8.views.NumberKeypadView;
import inc.flide.vim8.views.SelectionKeypadView;
import inc.flide.vim8.views.SymbolKeypadView;
import inc.flide.vim8.views.mainkeyboard.MainKeyboardView;
import java.util.List;

public class MainInputMethodService extends InputMethodService
        implements ClipboardManagerService.ClipboardHistoryListener {
    private InputConnection inputConnection;
    private EditorInfo editorInfo;
    private ClipboardManagerService clipboardManagerService;
    private MainKeyboardView mainKeyboardView;
    private NumberKeypadView numberKeypadView;
    private SelectionKeypadView selectionKeypadView;
    private SymbolKeypadView symbolKeypadView;
    private ClipboardKeypadView clipboardKeypadView;
    private View currentKeypadView;
    private int shiftLockFlag;
    private int capsLockFlag;
    private int modifierFlags;
    private AppPrefs prefs;
    private KeyboardTheme keyboardTheme;

    public MainInputMethodService() {
        super();
        setTheme(R.style.AppTheme_NoActionBar);
    }

    public ClipboardManagerService getClipboardManagerService() {
        return clipboardManagerService;
    }

    private void setCurrentKeypadView(View view) {
        this.currentKeypadView = view;
        currentKeypadView.invalidate();
        setInputView(currentKeypadView);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = appPreferenceModel().java();
        DynamicColors.applyToActivitiesIfAvailable(this.getApplication());
        Context applicationContext = getApplicationContext();
        this.clipboardManagerService = new ClipboardManagerService(applicationContext);
        this.clipboardManagerService.setClipboardHistoryListener(this);
        keyboardTheme = KeyboardTheme.getInstance();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        keyboardTheme.setConfiguration(newConfig);
    }

    /**
     * Lifecycle of IME
     * <p>
     * 01. InputMethodService Starts
     * 02. onCreate()
     * 03. onCreateInputView()
     * 04. onCreateCandidateViews()
     * 05. onStartInputViews()
     * 06. Text input gets the current input method subtype
     * 07. InputMethodManager#getCurrentInputMethodSubtype()
     * 08. Text input has started
     * 09. onCurrentInputMethodSubtypeChanged()
     * 10. Detect the current input method subtype has been changed -> can go to
     * step 6
     * 11. onFinishInput() -> cursor can Move to an additional field -> step 5
     * 12. onDestroy()
     * 13. InputMethodService stops
     */

    @Override
    public View onCreateInputView() {
        numberKeypadView = new NumberKeypadView(this);
        selectionKeypadView = new SelectionKeypadView(this);
        clipboardKeypadView = new ClipboardKeypadView(this);
        symbolKeypadView = new SymbolKeypadView(this);
        mainKeyboardView = new MainKeyboardView(this);
        setCurrentKeypadView(mainKeyboardView);

        Window window = getWindow().getWindow();
        if (AndroidVersion.INSTANCE.getATLEAST_API28_P() && window != null) {
            WindowInsetsControllerCompat windowInsetsControllerCompat = new WindowInsetsControllerCompat(window,
                    window.getDecorView());
            keyboardTheme.onChange(() -> setNavigationBarColor(window, windowInsetsControllerCompat));
            setNavigationBarColor(window, windowInsetsControllerCompat);
        }
        return currentKeypadView;
    }

    @Override
    public View onCreateExtractTextView() {
        return super.onCreateExtractTextView();
    }

    private void setNavigationBarColor(Window window, WindowInsetsControllerCompat windowInsetsControllerCompat) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setNavigationBarColor(keyboardTheme.getBackgroundColor());
        boolean isLight = ColorUtils.calculateLuminance(keyboardTheme.getBackgroundColor()) >= 0.5;
        windowInsetsControllerCompat.setAppearanceLightNavigationBars(isLight);
    }

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
        this.inputConnection = getCurrentInputConnection();
    }

    @Override
    public void onBindInput() {
        inputConnection = getCurrentInputConnection();
    }

    @Override
    public boolean onEvaluateFullscreenMode() {
        Configuration configuration = getResources().getConfiguration();
        return configuration.orientation == Configuration.ORIENTATION_LANDSCAPE && configuration.screenHeightDp < 480;
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        this.editorInfo = info;
        int inputType = this.editorInfo.inputType & InputType.TYPE_MASK_CLASS;
        switch (inputType) {
            case InputType.TYPE_CLASS_NUMBER:
            case InputType.TYPE_CLASS_PHONE:
            case InputType.TYPE_CLASS_DATETIME:
                switchToNumberPad();
                break;
            case InputType.TYPE_CLASS_TEXT:
            default:
                switchToMainKeypad();
                break;
        }
    }

    @Override
    public void onInitializeInterface() {
        super.onInitializeInterface();
        inputConnection = getCurrentInputConnection();
        setShiftLockFlag(0);
        setCapsLockFlag(0);
        clearModifierFlags();
    }

    public KeyboardData buildKeyboardActionMap() {
        return LayoutKt.safeLoadKeyboardData(prefs.getLayout().getCurrent().get(), getApplicationContext());
    }

    public void sendText(String text) {
        inputConnection.commitText(text, 1);
        clearModifierFlags();
    }

    private void clearModifierFlags() {
        modifierFlags = 0;
    }

    public void sendDownKeyEvent(int keyEventCode, int flags) {
        inputConnection.sendKeyEvent(
                new KeyEvent(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), KeyEvent.ACTION_DOWN, keyEventCode,
                        0, flags));
    }

    public void sendUpKeyEvent(int keyEventCode, int flags) {
        inputConnection.sendKeyEvent(
                new KeyEvent(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), KeyEvent.ACTION_UP, keyEventCode,
                        0, flags));
    }

    public void sendDownAndUpKeyEvent(int keyEventCode, int flags) {
        sendDownKeyEvent(keyEventCode, flags);
        sendUpKeyEvent(keyEventCode, flags);
    }

    @SuppressWarnings("DEPRECATION")
    public void switchToExternalEmoticonKeyboard() {
        String keyboardId = getSelectedEmoticonKeyboardId();
        if (keyboardId.isEmpty()) {
            if (AndroidVersion.INSTANCE.getATLEAST_API28_P()) {
                switchToPreviousInputMethod();
            } else {
                InputMethodManager inputMethodManager = (InputMethodManager) this
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                IBinder tokenIBinder = this.getWindow().getWindow().getAttributes().token;
                inputMethodManager.switchToLastInputMethod(tokenIBinder);
            }
        } else {
            switchInputMethod(keyboardId);
        }

    }

    private String getSelectedEmoticonKeyboardId() {
        String emoticonKeyboardId = prefs.getKeyboard().getEmoticonKeyboard().get();

        // Before returning verify that this keyboard Id we have does exist in the
        // system.
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        List<InputMethodInfo> enabledInputMethodList = inputMethodManager.getEnabledInputMethodList();
        for (InputMethodInfo inputMethodInfo : enabledInputMethodList) {
            if (inputMethodInfo.getId().compareTo(emoticonKeyboardId) == 0) {
                return emoticonKeyboardId;
            }
        }
        return "";
    }

    public void sendKey(int keyEventCode, int flags) {
        sendDownAndUpKeyEvent(keyEventCode, getShiftLockFlag() | getCapsLockFlag() | modifierFlags | flags);
        clearModifierFlags();
    }

    public void delete() {
        CharSequence sel = inputConnection.getSelectedText(0);
        if (TextUtils.isEmpty(sel)) {
            inputConnection.deleteSurroundingTextInCodePoints(1, 0);
        } else {
            inputConnection.commitText("", 0);
        }
    }

    public void switchAnchor() {
        ExtractedText extractedText = inputConnection.getExtractedText(new ExtractedTextRequest(),
                InputConnection.GET_EXTRACTED_TEXT_MONITOR);
        int start = extractedText.selectionStart;
        int end = extractedText.selectionEnd;
        inputConnection.setSelection(end, start);
    }

    public void switchToSelectionKeypad() {
        setCurrentKeypadView(selectionKeypadView);
    }

    public void switchToClipboardKeypad() {
        setCurrentKeypadView(clipboardKeypadView);
    }

    public void switchToSymbolsKeypad() {
        setCurrentKeypadView(symbolKeypadView);
    }

    public void switchToMainKeypad() {
        setCurrentKeypadView(mainKeyboardView);
    }

    public void switchToNumberPad() {
        setCurrentKeypadView(numberKeypadView);
    }

    public void cut() {
        inputConnection.performContextMenuAction(android.R.id.cut);
    }

    public void copy() {
        inputConnection.performContextMenuAction(android.R.id.copy);
    }

    public void paste() {
        inputConnection.performContextMenuAction(android.R.id.paste);
    }

    public void hideKeyboard() {
        this.requestHideSelf(InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public void performShiftToggle() {
        // single press locks the shift key,
        // double press locks the caps key
        // a third press unlocks both.
        if (getShiftLockFlag() == KeyEvent.META_SHIFT_ON) {
            setShiftLockFlag(0);
            setCapsLockFlag(KeyEvent.META_CAPS_LOCK_ON);
        } else if (getCapsLockFlag() == KeyEvent.META_CAPS_LOCK_ON) {
            setShiftLockFlag(0);
            setCapsLockFlag(0);
        } else {
            setShiftLockFlag(KeyEvent.META_SHIFT_ON);
            setCapsLockFlag(0);
        }
    }

    public boolean areCharactersCapitalized() {
        return getShiftLockFlag() == KeyEvent.META_SHIFT_ON || getCapsLockFlag() == KeyEvent.META_CAPS_LOCK_ON;
    }

    public void setModifierFlags(int newModifierFlags) {
        this.modifierFlags = this.modifierFlags | newModifierFlags;
    }

    public int getShiftLockFlag() {
        return shiftLockFlag;
    }

    public void setShiftLockFlag(int shiftLockFlag) {
        this.shiftLockFlag = shiftLockFlag;
        if (getWindow().findViewById(R.id.xboardView) != null) {
            getWindow().findViewById(R.id.xboardView).invalidate();
        }
    }

    public int getCapsLockFlag() {
        return capsLockFlag;
    }

    public void setCapsLockFlag(int capsLockFlag) {
        this.capsLockFlag = capsLockFlag;
    }

    /*
     * |-------|-------|-------|-------|
     * 1111 IME_MASK_ACTION
     * |-------|-------|-------|-------|
     * IME_ACTION_UNSPECIFIED
     * 1 IME_ACTION_NONE
     * 1 IME_ACTION_GO
     * 11 IME_ACTION_SEARCH
     * 1 IME_ACTION_SEND
     * 1 1 IME_ACTION_NEXT
     * 11 IME_ACTION_DONE
     * 111 IME_ACTION_PREVIOUS
     * 1 IME_FLAG_NO_PERSONALIZED_LEARNING
     * 1 IME_FLAG_NO_FULLSCREEN
     * 1 IME_FLAG_NAVIGATE_PREVIOUS
     * 1 IME_FLAG_NAVIGATE_NEXT
     * 1 IME_FLAG_NO_EXTRACT_UI
     * 1 IME_FLAG_NO_ACCESSORY_ACTION
     * 1 IME_FLAG_NO_ENTER_ACTION
     * 1 IME_FLAG_FORCE_ASCII
     * |-------|-------|-------|-------|
     */
    public void commitImeOptionsBasedEnter() {
        int imeAction = this.editorInfo.imeOptions & EditorInfo.IME_MASK_ACTION;
        switch (imeAction) {
            case EditorInfo.IME_ACTION_GO:
            case EditorInfo.IME_ACTION_SEARCH:
            case EditorInfo.IME_ACTION_SEND:
            case EditorInfo.IME_ACTION_NEXT:
            case EditorInfo.IME_ACTION_DONE:
            case EditorInfo.IME_ACTION_PREVIOUS:
                int imeNoEnterFlag = this.editorInfo.imeOptions & EditorInfo.IME_FLAG_NO_ENTER_ACTION;
                if (imeNoEnterFlag == EditorInfo.IME_FLAG_NO_ENTER_ACTION) {
                    sendDownAndUpKeyEvent(KeyEvent.KEYCODE_ENTER, 0);
                } else {
                    inputConnection.performEditorAction(imeAction);
                }
                break;
            case EditorInfo.IME_ACTION_UNSPECIFIED:
            case EditorInfo.IME_ACTION_NONE:
            default:
                sendDownAndUpKeyEvent(KeyEvent.KEYCODE_ENTER, 0);
        }
    }

    @Override
    public void onClipboardHistoryChanged() {
        if (clipboardKeypadView != null) {
            clipboardKeypadView.updateClipHistory();
        }
    }
}
