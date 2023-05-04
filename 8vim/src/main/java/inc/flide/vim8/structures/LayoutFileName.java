package inc.flide.vim8.structures;

import android.content.Context;
import android.content.res.Resources;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import inc.flide.vim8.keyboardHelpers.KeyboardDataYamlParser;

public class LayoutFileName {
    private static final Set<String> ISO_LANGUAGES = new HashSet<>(Arrays.asList(Locale.getISOLanguages()));
    private String languageCode;
    private String languageName;
    private boolean isValidLayout;
    private String resourceName;
    private String layoutDisplayName;

    private int totalLayers;

    public LayoutFileName() {
        languageCode = "en";
        languageName = "English";
        isValidLayout = true;
        resourceName = languageCode;
        layoutDisplayName = StringUtils.capitalize(languageName);
        totalLayers = 1;
    }

    public LayoutFileName(Resources resources, Context context, String fileName) {
        this();
        if (ISO_LANGUAGES.contains(fileName)) {
            resourceName = fileName;
            languageCode = fileName;
            languageName = Locale.forLanguageTag(languageCode).getDisplayName(new Locale(languageCode));
            layoutDisplayName = StringUtils.capitalize(languageName);
            int resourceId = resources.getIdentifier(fileName, "raw", context.getPackageName());
            totalLayers = KeyboardDataYamlParser.isValidFile(resources, resourceId);
            isValidLayout = true;
            if (totalLayers > 1) {
                layoutDisplayName += " (" + totalLayers + " layers)";
            }
            if (totalLayers == 0) {
                setLayoutValidityFalse();
            }
        } else {
            setLayoutValidityFalse();
        }
    }

    private void setLayoutValidityFalse() {
        this.setValidLayout(false);
        this.languageCode = "";
        this.languageName = "";
        this.totalLayers = 0;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getLayoutDisplayName() {
        return layoutDisplayName;
    }

    public boolean isValidLayout() {
        return isValidLayout;
    }

    public void setValidLayout(boolean validLayout) {
        isValidLayout = validLayout;
    }
}
