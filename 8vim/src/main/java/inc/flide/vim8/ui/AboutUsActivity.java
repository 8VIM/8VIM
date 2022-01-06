package inc.flide.vim8.ui;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import inc.flide.vim8.R;

public class AboutUsActivity extends AppCompatActivity {

    private ConstraintLayout github;
    private ConstraintLayout matrix;
    private ConstraintLayout website;
    private ConstraintLayout twitter;
    private ConstraintLayout googlePlayStore;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_us_activity_layout);

        github = findViewById(R.id.constraintLayout_github);
        github.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    github.setBackgroundColor(Color.LTGRAY);
                    break;
                case MotionEvent.ACTION_UP:
                    github.setBackgroundColor(Color.WHITE);
                    Uri uri = Uri.parse("https://github.com/flide/8vim");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                    break;
            }

            return true;
        });

        matrix = findViewById(R.id.constraintLayout_matrix);
        matrix.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    matrix.setBackgroundColor(Color.LTGRAY);
                    break;
                case MotionEvent.ACTION_UP:
                    matrix.setBackgroundColor(Color.WHITE);
                    Uri uri = Uri.parse("https://app.element.io/#/room/#8vim/lobby:matrix.org");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                    break;
            }

            return true;
        });

        website = findViewById(R.id.constraintLayout_website);
        website.setOnTouchListener((v, event) -> {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    website.setBackgroundColor(Color.LTGRAY);
                    break;
                case MotionEvent.ACTION_UP:
                    website.setBackgroundColor(Color.WHITE);
                    Uri uri = Uri.parse("https://8vim.com");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                    break;
            }

            return true;
        });

        twitter = findViewById(R.id.constraintLayout_twitter);
        twitter.setOnTouchListener((v, event) -> {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    twitter.setBackgroundColor(Color.LTGRAY);
                    break;
                case MotionEvent.ACTION_UP:
                    twitter.setBackgroundColor(Color.WHITE);
                    Uri uri = Uri.parse("https://twitter.com/8vim_?s=09");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                    break;
            }
            return true;
        });

        googlePlayStore = findViewById(R.id.constraintLayout_playstore);
        googlePlayStore.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    googlePlayStore.setBackgroundColor(Color.LTGRAY);
                    break;
                case MotionEvent.ACTION_UP:
                    googlePlayStore.setBackgroundColor(Color.WHITE);
                    Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=inc.flide.vi8"); // missing 'http://' will cause crashed
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                    break;
            }

            return true;
        });


        ImageView backArrow = findViewById(R.id.back_arrow);

        backArrow.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                backArrow.setBackgroundColor(Color.LTGRAY);
            }
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                backArrow.setBackgroundColor(Color.WHITE);
                Intent intent = new Intent(AboutUsActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
            return true;
        });

    }

}



