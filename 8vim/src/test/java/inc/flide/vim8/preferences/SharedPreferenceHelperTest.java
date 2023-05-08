package inc.flide.vim8.preferences;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SharedPreferenceHelperTest {
    static SharedPreferences sharedPreferences;
    static Context context;

    @BeforeAll
    static void setup() {
        sharedPreferences = mock(SharedPreferences.class);
        context = mock(Context.class);

        MockedStatic<PreferenceManager> preferenceManager = Mockito.mockStatic(PreferenceManager.class);
        preferenceManager.when(() -> PreferenceManager.getDefaultSharedPreferences(any())).thenReturn(sharedPreferences);
    }

    @Test
    void listener_called() {
        when(sharedPreferences.getString(anyString(), anyString())).thenReturn("test");
        SharedPreferenceHelper helper = SharedPreferenceHelper.getInstance(context);
        SharedPreferenceHelper.Listener listener = mock(SharedPreferenceHelper.Listener.class);
        helper.addListener(listener);
        assertThat(helper.getString("test", "")).isEqualTo("test");

        helper.onSharedPreferenceChanged(sharedPreferences, "test");
        verify(listener).onPreferenceChanged();
    }

    @Test
    void getString() {
        when(sharedPreferences.getString(anyString(), anyString())).thenReturn("test");
        SharedPreferenceHelper helper = SharedPreferenceHelper.getInstance(context);
        assertThat(helper.getString("test", "")).isEqualTo("test");
    }

    @Test
    void getInt() {
        when(sharedPreferences.getInt(anyString(), anyInt())).thenReturn(42);
        assertThat(SharedPreferenceHelper.getInstance(context).getInt("testInt", 0))
            .isEqualTo(42);
    }

    @Test
    void getBoolean() {
        when(sharedPreferences.getBoolean(anyString(), anyBoolean())).thenReturn(true);
        assertThat(SharedPreferenceHelper.getInstance(context).getBoolean("testBool", false))
            .isTrue();
    }
}
