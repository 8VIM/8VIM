package inc.flide.vim8.views.mainkeyboard;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import inc.flide.vim8.R;
import java.util.List;

public class SuggestionView extends LinearLayout {

    private TextView suggestion1;
    private TextView suggestion2;
    private TextView suggestion3;

    public SuggestionView(Context context) {
        super(context);
        initialize(context);
    }

    public SuggestionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public SuggestionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    private void initialize(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.suggestion_view, this, true);

        suggestion1 = view.findViewById(R.id.suggestion1);
        suggestion2 = view.findViewById(R.id.suggestion2);
        suggestion3 = view.findViewById(R.id.suggestion3);
    }

    public void setSuggestions(List<String> suggestions) {
        if (suggestions == null || suggestions.isEmpty()) {
            suggestion1.setText("");
            suggestion2.setText("");
            suggestion3.setText("");
        } else {
            if (suggestions.size() > 0) {
                suggestion1.setText(suggestions.get(0));
            }
            if (suggestions.size() > 1) {
                suggestion2.setText(suggestions.get(1));
            }
            if (suggestions.size() > 2) {
                suggestion3.setText(suggestions.get(2));
            }
        }
        invalidate();
    }
}

