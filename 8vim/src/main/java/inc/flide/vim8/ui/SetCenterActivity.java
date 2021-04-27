package inc.flide.vim8.ui;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

import inc.flide.vim8.R;
import inc.flide.vim8.preferences.SharedPreferenceHelper;

public class SetCenterActivity extends AppCompatActivity {


    private TextView textView_x_Value;
    private SeekBar seekBar_x;

    private TextView textView_y_value;
    private SeekBar seekBar_y;


    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_center_activity_layout);

        textView_x_Value = findViewById(R.id.x_value);
        seekBar_x = findViewById(R.id.seekbar_x);

        int offset_x = SharedPreferenceHelper
                .getInstance(getApplicationContext())
                .getInt(getString(R.string.board_circle_centre_x_offset_key), 0);

        int current_x_Value = 0;
        if (!(offset_x == 0)) {
            current_x_Value = offset_x;
        }

        textView_x_Value.setText(String.format(Locale.US, "%d", current_x_Value));

        seekBar_x.setMin(-5);
        seekBar_x.setProgress(current_x_Value);
        seekBar_x.setMax(5);


        seekBar_x.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                seekBar_x.setProgress(progress);
                textView_x_Value.setText(String.format(Locale.US, "%d", progress));

                SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.basic_preference_file_name), Activity.MODE_PRIVATE);
                SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
                sharedPreferencesEditor.putInt(getString(R.string.board_circle_centre_x_offset_key), progress);
                sharedPreferencesEditor.apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        textView_y_value = findViewById(R.id.y_value);
        seekBar_y = findViewById(R.id.seekbar_y);

        int offset_y = SharedPreferenceHelper
                .getInstance(getApplicationContext())
                .getInt(getString(R.string.board_circle_centre_y_offset_key), 0);

        int current_y_Value = 0;
        if (!(offset_y == 0)) {
            current_y_Value = offset_y;
        }

        textView_y_value.setText(String.format(Locale.US, "%d", current_y_Value));

        seekBar_y.setMin(-5);
        seekBar_y.setProgress(current_y_Value);
        seekBar_y.setMax(5);

        seekBar_y.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                seekBar_y.setProgress(progress);
                textView_y_value.setText(String.format(Locale.US, "%d", progress));

                SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.basic_preference_file_name), Activity.MODE_PRIVATE);
                SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
                sharedPreferencesEditor.putInt(getString(R.string.board_circle_centre_y_offset_key), progress);
                sharedPreferencesEditor.apply();

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


    }


}
