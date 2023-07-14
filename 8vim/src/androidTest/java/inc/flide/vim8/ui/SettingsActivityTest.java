package inc.flide.vim8.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import inc.flide.vim8.R;
import inc.flide.vim8.ui.activities.SettingsActivity;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SettingsActivityTest {
    @Rule
    public ActivityScenarioRule<SettingsActivity> settingsActivityActivityScenarioRule =
            new ActivityScenarioRule<>(SettingsActivity.class);

    @Test
    public void activate_input_method_dialog() {
        onView(withText(R.string.activate_ime_dialog_content)).check(matches(isDisplayed()));
    }
}
