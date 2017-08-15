package inc.flide.eightvim.views.mainKeyboard;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import inc.flide.eightvim.EightVimInputMethodService;
import inc.flide.eightvim.R;
import inc.flide.eightvim.keyboardHelpers.KeyboardAction;
import inc.flide.eightvim.structures.InputSpecialKeyEventCode;
import inc.flide.eightvim.structures.KeyboardActionType;

public class MainKeyboardView extends View{

    private EightVimInputMethodService eightVimInputMethodService;

    private LinearLayout layout;
    private XboardView xboardView;

    public MainKeyboardView(Context context) {
        super(context);
        initialize(context);
    }

    public MainKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public MainKeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    private void initialize(Context context){
        eightVimInputMethodService = (EightVimInputMethodService) context;

        LayoutInflater inflater = (LayoutInflater)   getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        layout = (LinearLayout) inflater.inflate(R.layout.main_keyboard_view, null);
        xboardView = (XboardView) layout.findViewById(R.id.xboardView);
        setupButtonsOnSideBar();

        setHapticFeedbackEnabled(true);
    }

    private void setupButtonsOnSideBar() {

        setupSwitchToEmojiKeyboardButton();
        setupSwitchToSelectionKeyboardButton();
        setupTabKey();
        setupAltKey();
        setupCtrlKey();

    }

    private void setupCtrlKey() {
        Button ctrlKeyButton = (Button) layout.findViewById(R.id.ctrlButton);

        ctrlKeyButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                eightVimInputMethodService.setModifierFlags(KeyEvent.META_CTRL_MASK);
            }
        });
    }

    private void setupAltKey() {
        Button altKeyButton = (Button) layout.findViewById(R.id.altButton);
        altKeyButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                eightVimInputMethodService.setModifierFlags(KeyEvent.META_ALT_MASK);
            }
        });
    }

    private void setupTabKey() {
        ImageButton tabKeyButton = (ImageButton) layout.findViewById(R.id.tabButton);

        tabKeyButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                eightVimInputMethodService.sendKey(KeyEvent.KEYCODE_TAB, 0);
            }
        });
    }

    private void setupSwitchToSelectionKeyboardButton() {
        ImageButton switchToSelectionKeyboardButton = (ImageButton) layout.findViewById(R.id.switchToSelectionKeyboard);
        switchToSelectionKeyboardButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                KeyboardAction switchToSelectionKeyboard = new KeyboardAction(
                        KeyboardActionType.INPUT_SPECIAL
                        , InputSpecialKeyEventCode.SWITCH_TO_SELECTION_KEYBOARD.toString()
                        , null, 0,0);
                eightVimInputMethodService.handleSpecialInput(switchToSelectionKeyboard);
            }
        });
    }

    private void setupSwitchToEmojiKeyboardButton() {
        ImageButton switchToEmojiKeyboardButton = (ImageButton) layout.findViewById(R.id.switchToEmojiKeyboard);
        switchToEmojiKeyboardButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                KeyboardAction switchToEmojiKeyboard = new KeyboardAction(
                        KeyboardActionType.INPUT_SPECIAL
                        , InputSpecialKeyEventCode.SWITCH_TO_EMOJI_KEYBOARD.toString()
                        , null, 0,0);
                eightVimInputMethodService.handleSpecialInput(switchToEmojiKeyboard);
            }
        });
    }

    public View getView() {
        return layout;
    }

}
