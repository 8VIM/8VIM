package inc.flide.eightvim;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Preferences8Vim extends PreferenceActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.activity_preferences_8vim);

        //setContentView(R.layout.activity_preferences_8vim);
    }
}
