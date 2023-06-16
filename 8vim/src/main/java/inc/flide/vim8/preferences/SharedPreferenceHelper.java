package inc.flide.vim8.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class SharedPreferenceHelper implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static SharedPreferenceHelper singleton = null;
    private final SharedPreferences sharedPreferences;
    private final Set<String> prefKeys = new HashSet<>();
    private final List<Listener> listeners = new ArrayList<>();

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

    public void addListener(Listener note) {
        listeners.add(note);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (prefKeys.contains(s)) {
            for (Listener n : listeners) {
                n.onPreferenceChanged();
            }
        }
    }

    public String getString(String preferenceId, String defaultValue) {
        prefKeys.add(preferenceId);
        String preferencesString;
        try {
            preferencesString = this.sharedPreferences.getString(preferenceId, defaultValue);
        } catch (ClassCastException e) {
            return defaultValue;
        }

        return preferencesString;
    }

    public int getInt(String preferenceId, int defaultValue) {
        prefKeys.add(preferenceId);
        int preferenceInt;
        try {
            preferenceInt = this.sharedPreferences.getInt(preferenceId, defaultValue);
        } catch (ClassCastException e) {
            return defaultValue;
        }

        return preferenceInt;
    }

    public boolean getBoolean(String preferenceId, boolean defaultValue) {
        prefKeys.add(preferenceId);
        boolean preferenceBoolean;
        try {
            preferenceBoolean = this.sharedPreferences.getBoolean(preferenceId, defaultValue);
        } catch (ClassCastException e) {
            return defaultValue;
        }

        return preferenceBoolean;
    }

    public interface Listener {
        void onPreferenceChanged();
    }
}
