package inc.flide.vim8.structures;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.res.Resources;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.InputStream;

@RunWith(MockitoJUnitRunner.class)
public class LayoutFileNameTest {
    @Mock
    Context context;
    @Mock
    Resources resources;

    @Before
    public void initMock() {
        lenient().when(context.getPackageName()).thenReturn("package");
        lenient().when(resources.getIdentifier(anyString(), anyString(), anyString())).thenReturn(0);
    }

    @Test
    public void invalid_ISO_Code() {
        LayoutFileName layoutFileName = new LayoutFileName(resources, context, "xyz");
        assertThat(layoutFileName.isValidLayout()).isFalse();
    }

    @Test
    public void valid_Layout_with_one_layer() {
        setupInputStream("/one_layer.yaml");
        LayoutFileName layoutFileName = new LayoutFileName(resources, context, "en");
        assertThat(layoutFileName.isValidLayout()).isTrue();
        assertThat(layoutFileName.getLayoutDisplayName()).isEqualTo("English");
        assertThat(layoutFileName.getResourceName()).isEqualTo("en");
    }

    @Test
    public void valid_Layout_with_multiple_layers() {
        setupInputStream("/multiple_layers.yaml");
        LayoutFileName layoutFileName = new LayoutFileName(resources, context, "en");
        assertThat(layoutFileName.isValidLayout()).isTrue();
        assertThat(layoutFileName.getLayoutDisplayName()).isEqualTo("English (2 layers)");
        assertThat(layoutFileName.getResourceName()).isEqualTo("en");
    }

    @Test
    public void invalid_Layout_with_only_hidden_layer() {
        setupInputStream("/hidden_layer.yaml");
        LayoutFileName layoutFileName = new LayoutFileName(resources, context, "en");
        assertThat(layoutFileName.isValidLayout()).isFalse();
    }

    void setupInputStream(String file) {
        InputStream inputStream = getClass().getResourceAsStream(file);
        when(resources.openRawResource(anyInt())).thenReturn(inputStream);
    }
}
