package inc.flide.vi8.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import inc.flide.vi8.R;
import inc.flide.vi8.structures.Constants;

public class ConfigureEmoticonKeyboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_emoticon_keyboard);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setUpRadioButtons();
        //getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setUpRadioButtons() {
        List<Map.Entry<InputMethodInfo, RadioButton>> availableKeyboards = getAllAvailableKeyboards();
        final RadioGroup radioGroup = findViewById(R.id.radioGroup);
        if (radioGroup != null){
            availableKeyboards.stream().forEachOrdered(ime -> radioGroup.addView(ime.getValue()));

            radioGroup.setOnCheckedChangeListener( (group, checkedId) -> {
                String text = "the selected keyboard " + availableKeyboards.get(checkedId).getKey().loadLabel(this.getPackageManager());
                Toast.makeText(getApplicationContext(),text, Toast.LENGTH_LONG).show();
                //TODO: save the selected keyboard to the preferences
                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(
                        getString(R.string.basic_preference_file_name), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(getString(R.string.bp_selected_emoticon_keyboard) ,availableKeyboards.get(checkedId).getKey().getId());
                editor.apply();
                });
        }

    }

    private List<Map.Entry<InputMethodInfo, RadioButton>> getAllAvailableKeyboards() {

        final InputMethodManager imeManager = (InputMethodManager)getApplicationContext().getSystemService(INPUT_METHOD_SERVICE);
        List<InputMethodInfo> inputMethods = imeManager.getEnabledInputMethodList();

        List<Map.Entry<InputMethodInfo, RadioButton>> resultList = new ArrayList<>();

        for (InputMethodInfo inputMethodInfo: inputMethods){
            if(!inputMethodInfo.getId().equals(Constants.KEYBOARD_ID)) {
                RadioButton radioButton = new RadioButton(this);
                radioButton.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                radioButton.setText(inputMethodInfo.loadLabel((this.getPackageManager())));

                Map.Entry<InputMethodInfo, RadioButton> entry = new AbstractMap.SimpleEntry<>(inputMethodInfo, radioButton);

                resultList.add(entry);

                resultList.get(resultList.indexOf(entry)).getValue().setId(resultList.indexOf(entry));
            }
        }

        return resultList;
    }

}
