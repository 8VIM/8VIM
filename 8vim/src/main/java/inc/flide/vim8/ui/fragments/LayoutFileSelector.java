package inc.flide.vim8.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.preference.PreferenceFragmentCompat;
import arrow.core.EitherKt;
import inc.flide.vim8.R;
import inc.flide.vim8.keyboardactionlisteners.MainKeypadActionListener;
import inc.flide.vim8.models.AppPrefs;
import inc.flide.vim8.models.CustomLayout;
import inc.flide.vim8.models.LayoutKt;
import inc.flide.vim8.models.error.ExceptionWrapperError;
import inc.flide.vim8.utils.DialogsHelper;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import kotlin.Pair;

public abstract class LayoutFileSelector extends PreferenceFragmentCompat {
    private static final String[] LAYOUT_FILTER = { "application/octet-stream" };

    protected AppPrefs prefs;

    private final ActivityResultLauncher<String[]> openContent = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(), this::callback);

    private void callback(Uri selectedCustomLayoutFile) {
        AppPrefs.Layout layoutPrefs = prefs.getLayout();
        Context context = getContext();
        if (selectedCustomLayoutFile == null || context == null) {
            return;
        }
        final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
        context.getContentResolver()
                .takePersistableUriPermission(selectedCustomLayoutFile, takeFlags);
        CustomLayout layout = new CustomLayout(selectedCustomLayoutFile);
        Set<String> currentHistory = layoutPrefs.getCustom().getHistory().get();
        boolean isInHistory = currentHistory.contains(selectedCustomLayoutFile.toString());
        if (isInHistory) {
            updateKeyboard(layout, context);
        } else {
            Pair<Integer, String> errorToShow = EitherKt.getOrElse(
                    LayoutKt
                            .loadKeyboardData(layout, context)
                            .map(keyboardData -> {
                                if (keyboardData.getTotalLayers() == 0) {
                                    return new Pair<>(R.string.yaml_error_title,
                                            "The layout requires at least one layer");
                                }
                                return null;
                            }),
                    error -> {
                        int title = R.string.yaml_error_title;
                        if (error instanceof ExceptionWrapperError) {
                            title = R.string.generic_error_text;
                        }
                        return new Pair<>(title, error.getMessage());
                    });
            if (errorToShow != null) {
                DialogsHelper.showAlert(context, errorToShow.getFirst(), errorToShow.getSecond());
                return;
            }
            updateKeyboard(layout, context);
            List<String> history = new ArrayList<>(currentHistory);
            history.add(0, selectedCustomLayoutFile.toString());
            layoutPrefs.getCustom().getHistory().set(new LinkedHashSet<>(history), true);
        }
    }

    private void updateKeyboard(CustomLayout layout, Context context) {
        prefs.getLayout().getCurrent().set(layout, true);
        MainKeypadActionListener.rebuildKeyboardData(LayoutKt.loadKeyboardData(layout, context).getOrNull());
    }

    protected void openFileSelector() {
        openContent.launch(LAYOUT_FILTER);
    }
}
