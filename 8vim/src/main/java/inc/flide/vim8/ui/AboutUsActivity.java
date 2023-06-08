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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_us_activity_layout);

        setupLink(R.id.constraintLayout_github, "https://github.com/flide/8vim");
        setupLink(R.id.constraintLayout_matrix, "https://app.element.io/#/room/#8vim/lobby:matrix.org");
        setupLink(R.id.constraintLayout_website, "https://8vim.com");
        setupLink(R.id.constraintLayout_twitter, "https://twitter.com/8vim_?s=09");
        setupLink(R.id.constraintLayout_playstore, "https://play.google.com/store/apps/details?id=inc.flide.vi8");
        setupBackArrow();

    }

    private void setupBackArrow() {
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

    private void setupLink(int constraintLayoutId, String uriString) {
        ConstraintLayout constraintLayout = findViewById(constraintLayoutId);
        constraintLayout.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    constraintLayout.setBackgroundColor(Color.LTGRAY);
                    break;
                case MotionEvent.ACTION_UP:
                    constraintLayout.setBackgroundColor(Color.WHITE);
                    Uri uri = Uri.parse(uriString);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                    break;
                default:
            }

            return true;
        });
    }

}



