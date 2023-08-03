package inc.flide.vim8.structures;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import inc.flide.vim8.R;
import inc.flide.vim8.keyboardactionlisteners.MainKeypadActionListener;
import inc.flide.vim8.preferences.SharedPreferenceHelper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class AvailableLayouts {
    private static final String DEFAULT_FILENAME = "en";
    private static AvailableLayouts singleton;
    private final ContentResolver contentResolver;
    private final SharedPreferenceHelper sharedPreferences;
    private final String selectKeyboardLayout;
    private final String customKeyboardLayoutUri;
    private final String customKeyboardLayoutHistory;
    private final String customSelectedKeyboardLayout;
    private Map<String, String> embeddedLayouts;
    private ArrayList<String> embeddedLayoutIds;
    private LinkedHashMap<String, String> customLayoutsHistory;
    private ArrayList<String> customLayoutHistoryUris;
    private Set<String> displayNames;
    private int index = -1;
    private int defaultIndex;

    private AvailableLayouts(Context context, Resources resources) {
        this.sharedPreferences = SharedPreferenceHelper.getInstance(context);
        this.contentResolver = context.getContentResolver();
        selectKeyboardLayout = context.getString(R.string.pref_selected_keyboard_layout);
        customSelectedKeyboardLayout = context.getString(R.string.pref_use_custom_selected_keyboard_layout);
        customKeyboardLayoutUri = context.getString(R.string.pref_selected_custom_keyboard_layout_uri);
        customKeyboardLayoutHistory = context.getString(R.string.pref_custom_keyboard_layout_history);
        listEmbeddedLayouts(context, resources);
        listCustomLayoutHistory();
        updateDisplayNames();
        findIndex();
    }

    public static AvailableLayouts getInstance(Context context, Resources resources) {
        if (singleton == null) {
            singleton = new AvailableLayouts(context, resources);
        }
        return singleton;
    }

    public void reloadCustomLayouts() {
        listCustomLayoutHistory();
        updateDisplayNames();
        findIndex();
    }

    public void selectLayout(Context context, Resources resources, int which) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        int embeddedLayoutSize = embeddedLayouts.size();
        if (which < embeddedLayoutSize) {
            editor
                    .putString(selectKeyboardLayout, embeddedLayoutIds.get(which))
                    .putBoolean(customSelectedKeyboardLayout, false);
        } else {
            editor
                    .putBoolean(customSelectedKeyboardLayout, true)
                    .putString(customKeyboardLayoutUri, customLayoutHistoryUris.get(which - embeddedLayoutSize));
        }
        editor.apply();
        index = which;
        MainKeypadActionListener.rebuildKeyboardData(resources, context);
    }

    public Set<String> getDisplayNames() {
        return displayNames;
    }

    public int getIndex() {
        return index;
    }

    private void listCustomLayoutHistory() {
//        LinkedHashSet<String> uris =
//                new LinkedHashSet<>(sharedPreferences.getStringSet(customKeyboardLayoutHistory, new LinkedHashSet<>()));
//        customLayoutsHistory = new LinkedHashMap<>();
//        LinkedHashSet<String> newUris = new LinkedHashSet<>();
//        for (String customLayoutUriString : uris) {
//            Uri customLayoutUri = Uri.parse(customLayoutUriString);
//            try (InputStream inputStream = contentResolver.openInputStream(customLayoutUri)) {
//                KeyboardData keyboardData = KeyboardDataYamlParser.readKeyboardData(inputStream);
//                int totalLayers = keyboardData.getTotalLayers();
//                if (totalLayers == 0) {
//                    continue;
//                }
//                String name = getFileName(customLayoutUri);
//                if (keyboardData.info != null && !keyboardData.info.name.isEmpty()) {
//                    name = keyboardData.info.name;
//                }
//                if (totalLayers > 1) {
//                    name += " (" + totalLayers + " layers)";
//                }
//                customLayoutsHistory.put(name, customLayoutUriString);
//                newUris.add(customLayoutUriString);
//            } catch (Exception ignored) {
//            }
//        }
//        sharedPreferences
//                .edit()
//                .putStringSet(customKeyboardLayoutHistory, newUris)
//                .apply();
//        customLayoutHistoryUris = new ArrayList<>(customLayoutsHistory.values());
    }

    private String getFileName(Uri uri) {
        try {
            String scheme = uri.getScheme();
            String fileName = "";
            if (scheme.equals("file")) {
                Class<R.raw> rawClass = R.raw.class;
                fileName = uri.getLastPathSegment();
            } else if (scheme.equals("content")) {
                Cursor cursor = contentResolver.query(uri, null, null, null, null);
                if (cursor != null && cursor.getCount() != 0) {
                    int columnIndex = cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME);
                    cursor.moveToFirst();
                    fileName = cursor.getString(columnIndex);
                }
                if (cursor != null) {
                    cursor.close();
                }
            }
            return fileName;
        } catch (Exception e) {
            return "";
        }
    }

    private void listEmbeddedLayouts(Context context, Resources resources) {
        embeddedLayouts = new TreeMap<>();
        Context applicationContext = context.getApplicationContext();
        String[] fields = resources.getStringArray(R.array.keyboard_layouts_id);

        for (String field : fields) {
            LayoutFileName file = new LayoutFileName(resources, applicationContext, field);
            if (file.isValidLayout()) {
                embeddedLayouts.put(file.getLayoutDisplayName(), file.getResourceName());
            }
        }
        embeddedLayoutIds = new ArrayList<>(embeddedLayouts.values());
        defaultIndex = embeddedLayoutIds.indexOf(DEFAULT_FILENAME);
    }

    private void findIndex() {

        boolean isCustom = sharedPreferences.getBoolean(customSelectedKeyboardLayout, false);

        String customLayoutUri = sharedPreferences.getString(customKeyboardLayoutUri, "");
        String selectedKeyboardId = sharedPreferences.getString(selectKeyboardLayout, DEFAULT_FILENAME);

        index = -1;
        int embeddedLayoutSize = embeddedLayouts.size();
        if (!isCustom && !selectedKeyboardId.isEmpty()) {
            index = embeddedLayoutIds.indexOf(selectedKeyboardId);
            if (index == -1) {
                // seems like we have a stale selection, it should be removed.
                sharedPreferences.edit().remove(selectKeyboardLayout).apply();
            }
        } else if (isCustom && !customLayoutUri.isEmpty()) {
            index = customLayoutHistoryUris.indexOf(customLayoutUri);
            if (index == -1) {
                // seems like we have a stale selection, it should be removed.
                sharedPreferences
                        .edit()
                        .remove(customKeyboardLayoutUri)
                        .putString(selectKeyboardLayout, DEFAULT_FILENAME)
                        .putBoolean(customSelectedKeyboardLayout, false)
                        .apply();
                index = defaultIndex;
            } else {
                index += embeddedLayoutSize;
            }
        }
    }

    private void updateDisplayNames() {
        displayNames = new LinkedHashSet<>(embeddedLayouts.keySet());
        displayNames.addAll(customLayoutsHistory.keySet());
    }
}
