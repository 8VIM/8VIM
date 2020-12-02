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

public class ResizeCircleActivity extends AppCompatActivity {

    private TextView textView;

    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.resize_circle_activty_layout);

        textView = findViewById(R.id.textView);
        SeekBar seekBar = findViewById(R.id.seekBar);
        SharedPreferences sp = getSharedPreferences(getString(R.string.basic_preference_file_name), Activity.MODE_PRIVATE);
        float spRadius = sp.getFloat(getString(R.string.x_board_circle_radius_size_factor_key), 0);
        float currentRadius = 0.1f;
        if (!(spRadius == 0)) {
            currentRadius = spRadius;
        }
        seekBar.setProgress((int) (currentRadius * 20));
        textView.setText(String.format(Locale.US, "%.2f", currentRadius));
        seekBar.setMin(1);
        seekBar.setMax(10);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                seekBar.setProgress(progress);
                float x = (float) (0.05 * progress);

                textView.setText(String.format(Locale.US, "%.2f", x));

                SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.basic_preference_file_name), Activity.MODE_PRIVATE);
                SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
                sharedPreferencesEditor.putFloat(getString(R.string.x_board_circle_radius_size_factor_key), x);
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
