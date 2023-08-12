package inc.flide.vim8.services;

import static inc.flide.vim8.models.AppPrefsKt.appPreferenceModel;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import inc.flide.vim8.models.AppPrefs;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClipboardManagerService {

    private static final int MAX_HISTORY_SIZE = 10; // This could be made user-configurable
    private final ClipboardManager clipboardManager;
    private final AppPrefs prefs;
    private ClipboardHistoryListener clipboardHistoryListener;

    public ClipboardManagerService(Context context) {
        prefs = appPreferenceModel().java();
        clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

        clipboardManager.addPrimaryClipChangedListener(() -> {
            ClipData primaryClip = clipboardManager.getPrimaryClip();
            if (primaryClip != null) {
                String newClip = primaryClip.getItemAt(0).getText().toString();
                addClipToHistory(newClip);
                if (clipboardHistoryListener != null) {
                    clipboardHistoryListener.onClipboardHistoryChanged();
                }
            } else {
                Log.e("Clipboard Manager", "Unable to access the primary clip");
            }
        });
    }

    private long getTimestampFromTimestampedClip(String timestampedClip) {
        String timestampString = timestampedClip.substring(1, timestampedClip.indexOf("] "));
        return Long.parseLong(timestampString);
    }

    private String getClipFromTimestampedClip(String timestampedClip) {
        return timestampedClip.substring(timestampedClip.indexOf("] ") + 2);
    }

    private void addClipToHistory(String newClip) {
        if (TextUtils.isEmpty(newClip)) {
            return;
        }
        Set<String> history = new HashSet<>(prefs.getClipboard().getHistory().get());
        String timestampedClip = "[" + System.currentTimeMillis() + "] " + newClip;
        history.add(timestampedClip);

        // Remove duplicate clips
        Map<String, Long> clipsWithTimestampsMap = new HashMap<>();
        for (String clip : history) {
            String cleanedClip = getClipFromTimestampedClip(clip);
            Long timestamp = getTimestampFromTimestampedClip(clip);
            Long existingTimestamp = clipsWithTimestampsMap.putIfAbsent(cleanedClip, timestamp);
            if (existingTimestamp != null) {
                if (timestamp > existingTimestamp) {
                    clipsWithTimestampsMap.put(cleanedClip, timestamp);
                }
            }
        }
        history.clear();
        for (Map.Entry<String, Long> entry : clipsWithTimestampsMap.entrySet()) {
            String formattedHistoryClip = "[" + entry.getValue() + "] " + entry.getKey();
            history.add(formattedHistoryClip);
        }

        // If history size exceeds max, remove oldest clip
        while (history.size() > MAX_HISTORY_SIZE) {
            String oldestClip = Collections.min(
                    history,
                    Comparator.comparingLong(this::getTimestampFromTimestampedClip)
            );
            history.remove(oldestClip);
        }
        prefs.getClipboard().getHistory().set(history, true);
    }

    public List<String> getClipHistory() {
        Set<String> history = prefs.getClipboard().getHistory().get();
        List<String> timestampedClipHistory = new ArrayList<>(history);

        // Reverse the order so the most recent clip is at the top
        timestampedClipHistory.sort(
                Comparator.comparingLong(this::getTimestampFromTimestampedClip).reversed()
        );

        // Remove timestamps from the clips
        List<String> clipHistory = new ArrayList<>();
        for (String timestampedClip : timestampedClipHistory) {
            clipHistory.add(getClipFromTimestampedClip(timestampedClip));
        }
        return clipHistory;
    }

    public void setClipboardHistoryListener(ClipboardHistoryListener listener) {
        this.clipboardHistoryListener = listener;
    }

    public interface ClipboardHistoryListener {
        void onClipboardHistoryChanged();
    }
}
