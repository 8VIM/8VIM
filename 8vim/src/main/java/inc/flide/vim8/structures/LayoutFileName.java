package inc.flide.vim8.structures;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class LayoutFileName {
    private static final Set<String> ISO_LANGUAGES = new HashSet<String>(Arrays.asList(Locale.getISOLanguages()));
    private static final Set<String> FONT_CODES = new HashSet<String>(Arrays.asList(new String[]{"regular", "bold", "italic", "underline"}));

    private String languageCode;
    private String fontCode;
    private String layoutName;
    private String languageName;
    private boolean isValidLayout;
    private String resourceName;
    private String layoutDisplayName;

    public LayoutFileName() {
        languageCode = "en";
        fontCode = "regular";
        layoutName = "8pen";
        languageName = "English";
        isValidLayout = true;
        resourceName = languageCode + "_" + fontCode + "_" + layoutName;
        layoutDisplayName = StringUtils.capitalize(layoutName) + " (" + StringUtils.capitalize(languageName) + ")";
    }

    public LayoutFileName(String fileName) {
        this();
        String[] nameComponents = fileName.split("_", 3);
        if (nameComponents.length != 3) {
            setLayoutValidityFalse();
            return;
        }
        if (ISO_LANGUAGES.contains(nameComponents[0]) && FONT_CODES.contains(nameComponents[1])) {
            resourceName = fileName;
            languageCode = nameComponents[0];
            fontCode = nameComponents[1];
            layoutName = nameComponents[2];
            languageName = Locale.forLanguageTag(languageCode).getDisplayName(new Locale(languageCode));
            layoutDisplayName = StringUtils.capitalize(layoutName) + " (" + StringUtils.capitalize(languageName) + ")";
            isValidLayout = true;
        } else {
            setLayoutValidityFalse();
        }
    }

    private void setLayoutValidityFalse() {
        this.setValidLayout(false);
        this.languageCode = "";
        this.fontCode = "";
        this.layoutName = "";
        this.languageName = "";
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getLayoutDisplayName() {
        return layoutDisplayName;
    }

    public void setLayoutDisplayName(String layoutDisplayName) {
        this.layoutDisplayName = layoutDisplayName;
    }

    public boolean isValidLayout() {
        return isValidLayout;
    }

    public void setValidLayout(boolean validLayout) {
        isValidLayout = validLayout;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getFontCode() {
        return fontCode;
    }

    public void setFontCode(String fontCode) {
        this.fontCode = fontCode;
    }

    public String getLayoutName() {
        return layoutName;
    }

    public void setLayoutName(String layoutName) {
        this.layoutName = layoutName;
    }

    public String getLanguageName() {
        return languageName;
    }

    public void setLanguageName(String languageName) {
        this.languageName = languageName;
    }
}
