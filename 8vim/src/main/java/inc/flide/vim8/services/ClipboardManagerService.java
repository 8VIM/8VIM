package inc.flide.vim8.services;

import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;
import java.util.HashSet;
import java.util.Set;

import inc.flide.vim8.preferences.SharedPreferenceHelper;

public class ClipboardManagerService {

    private static final String CLIPBOARD_HISTORY = "clipboard_history";
    private static final int MAX_HISTORY_SIZE = 10; // This could be made user-configurable

    private final ClipboardManager clipboardManager;
    private final SharedPreferenceHelper sharedPreferenceHelper;
    private ClipboardHistoryListener clipboardHistoryListener;

    public ClipboardManagerService(Context context) {
        this.sharedPreferenceHelper = SharedPreferenceHelper.getInstance(context);
        clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

        clipboardManager.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
                String newClip = clipboardManager.getPrimaryClip().getItemAt(0).getText().toString();
                addClipToHistory(newClip);
                if (clipboardHistoryListener != null) {
                    clipboardHistoryListener.onClipboardHistoryChanged();
                }
            }
        });
    }

    private void addClipToHistory(String newClip) {
        if (TextUtils.isEmpty(newClip)) return;

        Set<String> history = new HashSet<>(sharedPreferenceHelper.getStringSet(CLIPBOARD_HISTORY, new HashSet<String>()));
        history.add(newClip);

        // If history size exceeds max, remove oldest clip
        while (history.size() > MAX_HISTORY_SIZE) {
            history.remove(history.iterator().next());
        }

        sharedPreferenceHelper.edit().putStringSet(CLIPBOARD_HISTORY, history).apply();
    }

    public Set<String> getClipHistory() {
        return sharedPreferenceHelper.getStringSet(CLIPBOARD_HISTORY, new HashSet<String>());
    }

    public void setClipboardHistoryListener(ClipboardHistoryListener listener) {
        this.clipboardHistoryListener = listener;
    }

    public interface ClipboardHistoryListener {
        void onClipboardHistoryChanged();
    }
}
