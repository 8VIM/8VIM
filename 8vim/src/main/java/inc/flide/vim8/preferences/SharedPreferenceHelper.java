package inc.flide.vim8.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import inc.flide.vim8.R;

public class SharedPreferenceHelper {
    private SharedPreferences sharedPreferences;
    private static SharedPreferenceHelper singleton = null;

    public static SharedPreferenceHelper getInstance(Context context){
        //These two ifs should be probably swapped as it can still return null
        //when singleton is null.
        if (context == null) {
            return singleton;
        }
        if(singleton == null){
            SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.basic_preference_file_name), Context.MODE_PRIVATE);
            singleton = new SharedPreferenceHelper(sp);
        }
        return singleton;
    }

    private SharedPreferenceHelper (SharedPreferences sharedPreferences){
        this.sharedPreferences = sharedPreferences;
    }

    public String getString(String preferenceId, String defaultValue) {
        String preferencesString;
        try {
            preferencesString = this.sharedPreferences.getString(preferenceId, defaultValue);
        } catch (ClassCastException e) {
            return defaultValue;
        }

        return preferencesString;
    }

    public int getInt(String preferenceId, int defaultValue) {
        int preferenceInt;
        try {
            preferenceInt = this.sharedPreferences.getInt(preferenceId, defaultValue);
        } catch (ClassCastException e) {
            return defaultValue;
        }

        return preferenceInt;
    }

    public boolean getBoolean(String preferenceId, boolean defaultValue) {
        boolean preferenceBoolean;
        try {
            preferenceBoolean = this.sharedPreferences.getBoolean(preferenceId, defaultValue);
        } catch (ClassCastException e) {
            return defaultValue;
        }

        return preferenceBoolean;
    }

    public float getFloat(String preferenceId, float defaultValue) {
        float preferenceFloat;
        try {
            preferenceFloat = this.sharedPreferences.getFloat(preferenceId, defaultValue);
        } catch (ClassCastException e) {
            return defaultValue;
        }

        return preferenceFloat;
    }
}
