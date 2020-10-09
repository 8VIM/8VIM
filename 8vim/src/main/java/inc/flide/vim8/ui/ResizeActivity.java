package inc.flide.vim8.ui;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import inc.flide.vim8.R;
import inc.flide.vim8.views.ResizeCircleView;

public class ResizeActivity extends AppCompatActivity {

    private TextView textView;
    private SeekBar seekBar;

    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.resize_activity);

//        LinearLayout layout = new LinearLayout(this);
//        layout.setLayoutParams(new LinearLayout.LayoutParams(320,420));
//        layout.setOrientation(LinearLayout.VERTICAL);
//
//        ResizeCircleView resizeCircleView = new ResizeCircleView(this);
//        resizeCircleView.setLayoutParams(new LinearLayout.LayoutParams(300,200));
//        SeekBar bar = new SeekBar(this);
//        bar.setMax(100);
//        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//
//                resizeCircleView.setRadius(progress);
//                resizeCircleView.invalidate();
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//
//            }
//        });

//        layout.addView(bar);
//        setContentView(layout);

        textView = (TextView)findViewById(R.id.textView);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        SharedPreferences sp = getSharedPreferences(getString(R.string.basic_preference_file_name), Activity.MODE_PRIVATE);
        float selectedKeyboardId = sp.getFloat(getString(R.string.current_radius_value),0);
        float selectedKeyboardIndex = 0.1f;
        if (!(selectedKeyboardId == 0)) {
            selectedKeyboardIndex = selectedKeyboardId;
        }
        seekBar.setProgress((int) (selectedKeyboardIndex * 20));
        textView.setText("" + selectedKeyboardIndex + "");
        seekBar.setMin(1);
        seekBar.setMax(10);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {



                seekBar.setProgress(progress);
                Float x = new Float((0.05 * progress));
                textView.setText("" + x + "");



                SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.basic_preference_file_name), Activity.MODE_PRIVATE);
                SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
                sharedPreferencesEditor.putFloat(getString(R.string.current_radius_value),x);
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
