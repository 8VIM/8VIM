package inc.flide.eightvim.views.mainKeyboard;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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

    }

    private void setupSwitchToSelectionKeyboardButton() {
        Button switchToSelectionKeyboardButton = (Button) layout.findViewById(R.id.switchToSelectionKeyboard);
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
        Button switchToEmojiKeyboardButton = (Button) layout.findViewById(R.id.switchToEmojiKeyboard);
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
