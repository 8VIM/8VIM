package inc.flide.vim8.preferences;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.SharedPreferences;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SharedPreferenceHelperTest {
    private static SharedPreferences sharedPreferences;
    private static Context context;
    private static MockedStatic<SharedPreferenceHelper> sharedPreferenceHelper;

    @BeforeClass
    public static void setup() {
        context = mock(Context.class);
        sharedPreferences = mock(SharedPreferences.class);

        when(context.getSharedPreferences(anyString(),anyInt())).thenReturn(sharedPreferences);

        when(sharedPreferences.getString(anyString(), anyString())).thenReturn("test");
        when(sharedPreferences.getInt(anyString(), anyInt())).thenReturn(42);
        when(sharedPreferences.getBoolean(anyString(), anyBoolean())).thenReturn(true);

        doNothing().when(sharedPreferences).registerOnSharedPreferenceChangeListener(any());

        SharedPreferenceHelper singleton = SharedPreferenceHelper.getInstance(context);
        sharedPreferenceHelper = mockStatic(SharedPreferenceHelper.class);
        sharedPreferenceHelper.when(() -> SharedPreferenceHelper.getInstance(any())).thenReturn(singleton);
    }

    @AfterClass
    public static void close() {
        sharedPreferenceHelper.close();
    }

    @Test
    public void listener_called() {
        SharedPreferenceHelper helper = SharedPreferenceHelper.getInstance(context);
        SharedPreferenceHelper.Listener listener = mock(SharedPreferenceHelper.Listener.class);
        helper.addListener(listener);
        assertThat(helper.getString("test", "")).isEqualTo("test");

        helper.onSharedPreferenceChanged(sharedPreferences, "test");
        verify(listener).onPreferenceChanged();
    }

    @Test
    public void getString() {
        SharedPreferenceHelper helper = SharedPreferenceHelper.getInstance(context);
        assertThat(helper.getString("test", "")).isEqualTo("test");
    }

    @Test
    public void getInt() {
        assertThat(SharedPreferenceHelper.getInstance(context).getInt("testInt", 0)).isEqualTo(42);
    }

    @Test
    public void getBoolean() {
        assertThat(SharedPreferenceHelper.getInstance(context).getBoolean("testBool", false)).isTrue();
    }

}
