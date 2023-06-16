package inc.flide.vim8.utils;

import android.content.Context;
import com.afollestad.materialdialogs.MaterialDialog;
import inc.flide.vim8.R;

public class AlertHelper {
    private AlertHelper() {
    }

    public static void showAlert(Context context, int titleRes, String message) {
        new MaterialDialog(context, MaterialDialog.getDEFAULT_BEHAVIOR())
                .title(titleRes, null)
                .message(null, message, null)
                .positiveButton(R.string.generic_okay_text, null, null)
                .show();

    }
}
