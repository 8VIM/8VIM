package inc.flide.vim8.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import inc.flide.vim8.R;

public class AboutUsActivity extends AppCompatActivity {

    private ConstraintLayout github;
    private ConstraintLayout twitter;
    private ConstraintLayout website;
    private ConstraintLayout playstore;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_page_layout);

        //for Github

        github = (ConstraintLayout) findViewById(R.id.constraintLayout_github);
        github.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch(event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        github.setBackgroundColor(Color.LTGRAY);
                        break;
                    case MotionEvent.ACTION_UP:
                        github.setBackgroundColor(Color.WHITE);
                        Uri uri = Uri.parse("https://github.com/flide"); // missing 'http://' will cause crashed
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                        break;
                }

                return true;
            }

        });

        //for twitter

        twitter = (ConstraintLayout) findViewById(R.id.constraintLayout_twitter);
        twitter.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch(event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        twitter.setBackgroundColor(Color.LTGRAY);
                        break;
                    case MotionEvent.ACTION_UP:
                        twitter.setBackgroundColor(Color.WHITE);
                        Uri uri = Uri.parse("https://twitter.com/8vim_?s=09"); // missing 'http://' will cause crashed
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                        break;
                }

                return true;
            }

        });

        //for website

        website = (ConstraintLayout) findViewById(R.id.constraintLayout_website);
        website.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch(event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        website.setBackgroundColor(Color.LTGRAY);
                        break;
                    case MotionEvent.ACTION_UP:
                        website.setBackgroundColor(Color.WHITE);
                        Uri uri = Uri.parse("https://twitter.com/8vim_?s=09"); // missing 'http://' will cause crashed
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                        break;
                }

                return true;
            }

        });

        //for playstore
        
        playstore = (ConstraintLayout) findViewById(R.id.constraintLayout_playstore);
        playstore.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        playstore.setBackgroundColor(Color.LTGRAY);
                        break;
                    case MotionEvent.ACTION_UP:
                        playstore.setBackgroundColor(Color.WHITE);
                        Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=inc.flide.vi8"); // missing 'http://' will cause crashed
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                        break;
                }

                return true;
            }
        });


        ImageView back_arrow = (ImageView) findViewById(R.id.back_arrow);
        back_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AboutUsActivity.this,LauncherActivity.class);
                startActivity(intent);
            }
        });

    }

}



