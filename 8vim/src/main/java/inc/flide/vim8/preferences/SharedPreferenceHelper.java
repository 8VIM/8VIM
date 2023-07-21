package inc.flide.vim8.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SharedPreferenceHelper implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static SharedPreferenceHelper singleton = null;
    private final SharedPreferences sharedPreferences;
    private final Map<String, List<Listener>> listeners = new HashMap<>();

    private SharedPreferenceHelper(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    public static SharedPreferenceHelper getInstance(Context context) {
        //These two ifs should be probably swapped as it can still return null
        //when singleton is null.
        if (context == null) {
            return singleton;
        }
        if (singleton == null) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            singleton = new SharedPreferenceHelper(sp);
        }
        return singleton;
    }

    public SharedPreferenceHelper addListener(Listener note, String firstKey, String... otherKeys) {
        List<String> keys = new ArrayList<>(Collections.singletonList(firstKey));
        keys.addAll(Arrays.asList(otherKeys));
        keys.forEach(key -> {
            listeners.putIfAbsent(key, new LinkedList<>());
            listeners.get(key).add(note);
        });
        return this;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences pref, String s) {
        listeners.getOrDefault(s, Collections.emptyList())
                .forEach(Listener::onPreferenceChanged);
    }

    public String getString(String preferenceId, String defaultValue) {
        try {
            return this.sharedPreferences.getString(preferenceId, defaultValue);
        } catch (ClassCastException ignored) {
            return defaultValue;
        }
    }

    public int getInt(String preferenceId, int defaultValue) {
        try {
            return this.sharedPreferences.getInt(preferenceId, defaultValue);
        } catch (ClassCastException ignored) {
            return defaultValue;
        }
    }

    public boolean getBoolean(String preferenceId, boolean defaultValue) {
        try {
            return this.sharedPreferences.getBoolean(preferenceId, defaultValue);
        } catch (ClassCastException ignored) {
            return defaultValue;
        }
    }

    public Set<String> getStringSet(String preferenceId, Set<String> defaultValue) {
        try {
            return this.sharedPreferences.getStringSet(preferenceId, defaultValue);
        } catch (ClassCastException ignored) {
            return defaultValue;
        }
    }

    public SharedPreferences.Editor edit() {
        return sharedPreferences.edit();
    }

    public interface Listener {
        void onPreferenceChanged();
    }
}
