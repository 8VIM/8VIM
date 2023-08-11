package inc.flide.vim8.utils;

import android.content.Context;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import inc.flide.vim8.R;

public class AlertHelper {
    private AlertHelper() {
    }

    public static void showAlert(Context context, int titleRes, String message) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(titleRes)
                .setMessage(message)
                .setPositiveButton(R.string.generic_okay_text, null)
                .show()
                .show();

    }
}
