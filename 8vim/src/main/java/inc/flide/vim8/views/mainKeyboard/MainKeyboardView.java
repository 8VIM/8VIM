package inc.flide.vim8.views.mainKeyboard;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.constraintlayout.widget.ConstraintLayout;

import inc.flide.vim8.MainInputMethodService;
import inc.flide.vim8.R;
import inc.flide.vim8.keyboardActionListners.MainKeyboardActionListener;
import inc.flide.vim8.keyboardHelpers.KeyboardAction;
import inc.flide.vim8.structures.InputSpecialKeyEventCode;
import inc.flide.vim8.structures.KeyboardActionType;

public class MainKeyboardView extends View{

    private MainKeyboardActionListener actionListener;

    private View layout;
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

    public void initialize(Context context){

        actionListener = new MainKeyboardActionListener((MainInputMethodService) context, this);
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.basic_preference_file_name), Activity.MODE_PRIVATE);
        String selectedKeyboardId = sp.getString(context.getString(R.string.input_keyboard_id),"");
        String selectedKeyboardIndex = "left_click";
        if(!(selectedKeyboardId == "")){
            selectedKeyboardIndex = selectedKeyboardId;
        }

        if(selectedKeyboardIndex == "left_click"){

            layout = inflater.inflate(R.layout.main_keyboard_view, null);
        }
        else{
            layout = inflater.inflate(R.layout.main_keyboard_right_view, null);
        }

        xboardView = layout.findViewById(R.id.xboardView);
        setupButtonsOnSideBar();
        //setupPredictiveTextCandidateButtons();

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
                actionListener.setModifierFlags(KeyEvent.META_CTRL_MASK);
            }
        });
    }

    private void setupAltKey() {
        Button altKeyButton = (Button) layout.findViewById(R.id.altButton);
        altKeyButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                actionListener.setModifierFlags(KeyEvent.META_ALT_MASK);
            }
        });
    }

    private void setupTabKey() {
        ImageButton tabKeyButton = (ImageButton) layout.findViewById(R.id.tabButton);

        tabKeyButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                actionListener.sendKey(KeyEvent.KEYCODE_TAB, 0);
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
                actionListener.handleSpecialInput(switchToSelectionKeyboard);
            }
        });
    }

    private void setupSwitchToEmojiKeyboardButton() {
        ImageButton switchToEmojiKeyboardButton = layout.findViewById(R.id.switchToEmojiKeyboard);
        switchToEmojiKeyboardButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                KeyboardAction switchToEmojiKeyboard = new KeyboardAction(
                        KeyboardActionType.INPUT_SPECIAL
                        , InputSpecialKeyEventCode.SWITCH_TO_EMOJI_KEYBOARD.toString()
                        , null, 0,0);
                actionListener.handleSpecialInput(switchToEmojiKeyboard);
            }
        });
    }

    /*
    private void setupPredictiveTextCandidateButtons() {
        Button leftCanditateButton = layout.findViewById(R.id.selectLeftPrediction);
        Button centreCanditateButton = layout.findViewById(R.id.selectCentrePrediction);
        Button rightCanditateButton = layout.findViewById(R.id.selectRightPrediction);

        leftCanditateButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                leftCanditateButton.getText();
            }
        });

        centreCanditateButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        rightCanditateButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
     */

    public View getView() {

        actionListener = new MainKeyboardActionListener((MainInputMethodService) getContext(), this);
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        SharedPreferences sp = getContext().getSharedPreferences(getContext().getString(R.string.basic_preference_file_name), Activity.MODE_PRIVATE);
        String selectedKeyboardId = sp.getString(getContext().getString(R.string.input_keyboard_id),"");
        String selectedKeyboardIndex = "left_click";
        if(!(selectedKeyboardId == "")){
            selectedKeyboardIndex = selectedKeyboardId;
        }

        if(selectedKeyboardIndex == "left_click"){
            layout = inflater.inflate(R.layout.main_keyboard_view, null);
        }
        else{
            layout = inflater.inflate(R.layout.main_keyboard_right_view, null);
        }

        xboardView = layout.findViewById(R.id.xboardView);
        setupButtonsOnSideBar();
        //setupPredictiveTextCandidateButtons();
        setHapticFeedbackEnabled(true);
        return layout;
    }

}
