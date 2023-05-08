package inc.flide.vim8.structures;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.res.Resources;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;

@ExtendWith(MockitoExtension.class)
public class LayoutFileNameTest {
    @Mock
    Context context;
    @Mock
    Resources resources;

    @BeforeEach
    void init() {
        lenient().when(context.getPackageName()).thenReturn("package");
        lenient().when(resources.getIdentifier(anyString(), eq("raw"), anyString())).thenReturn(0);
    }

    @Test
    void invalid_ISO_Code() {
        LayoutFileName layoutFileName = new LayoutFileName(resources, context, "xyz");
        assertThat(layoutFileName.isValidLayout()).isFalse();
    }

    @Test
    void valid_Layout_with_one_layer() {
        InputStream inputStream = getClass().getResourceAsStream("/one_layer.yaml");
        when(resources.openRawResource(0)).thenReturn(inputStream);
        LayoutFileName layoutFileName = new LayoutFileName(resources, context, "en");
        assertThat(layoutFileName.isValidLayout()).isTrue();
        assertThat(layoutFileName.getLayoutDisplayName()).isEqualTo("English");
        assertThat(layoutFileName.getResourceName()).isEqualTo("en");
    }

    @Test
    void valid_Layout_with_multiple_layers() {
        InputStream inputStream = getClass().getResourceAsStream("/multiple_layers.yaml");
        when(resources.openRawResource(0)).thenReturn(inputStream);
        LayoutFileName layoutFileName = new LayoutFileName(resources, context, "en");
        assertThat(layoutFileName.isValidLayout()).isTrue();
        assertThat(layoutFileName.getLayoutDisplayName()).isEqualTo("English (2 layers)");
        assertThat(layoutFileName.getResourceName()).isEqualTo("en");
    }

    @Test
    void invalid_Layout_with_only_hidden_layer() {
        InputStream inputStream = getClass().getResourceAsStream("/hidden_layer.yaml");
        when(resources.openRawResource(0)).thenReturn(inputStream);
        LayoutFileName layoutFileName = new LayoutFileName(resources, context, "en");
        assertThat(layoutFileName.isValidLayout()).isFalse();

    }
}
