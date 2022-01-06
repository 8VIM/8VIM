package inc.flide.vim8.structures

import org.apache.commons.lang3.StringUtils
import java.util.*

class LayoutFileName() {
    private var languageCode: String? = "en"
    private var fontCode: String? = "regular"
    private var layoutName: String? = "8pen"
    private var languageName: String? = "English"
    private var isValidLayout = true
    private var resourceName: String?
    private var layoutDisplayName: String?

    constructor(fileName: String?) : this() {
        val nameComponents: Array<String?> = fileName.split("_", 3.toBoolean()).toTypedArray()
        if (nameComponents.size != 3) {
            setLayoutValidityFalse()
            return
        }
        if (ISO_LANGUAGES.contains(nameComponents[0]) && FONT_CODES.contains(nameComponents[1])) {
            resourceName = fileName
            languageCode = nameComponents[0]
            fontCode = nameComponents[1]
            layoutName = nameComponents[2]
            languageName = Locale.forLanguageTag(languageCode).getDisplayName(Locale(languageCode))
            layoutDisplayName = StringUtils.capitalize(layoutName) + " (" + StringUtils.capitalize(languageName) + ")"
            isValidLayout = true
        } else {
            setLayoutValidityFalse()
        }
    }

    private fun setLayoutValidityFalse() {
        setValidLayout(false)
        languageCode = ""
        fontCode = ""
        layoutName = ""
        languageName = ""
    }

    fun getResourceName(): String? {
        return resourceName
    }

    fun setResourceName(resourceName: String?) {
        this.resourceName = resourceName
    }

    fun getLayoutDisplayName(): String? {
        return layoutDisplayName
    }

    fun setLayoutDisplayName(layoutDisplayName: String?) {
        this.layoutDisplayName = layoutDisplayName
    }

    fun isValidLayout(): Boolean {
        return isValidLayout
    }

    fun setValidLayout(validLayout: Boolean) {
        isValidLayout = validLayout
    }

    fun getLanguageCode(): String? {
        return languageCode
    }

    fun setLanguageCode(languageCode: String?) {
        this.languageCode = languageCode
    }

    fun getFontCode(): String? {
        return fontCode
    }

    fun setFontCode(fontCode: String?) {
        this.fontCode = fontCode
    }

    fun getLayoutName(): String? {
        return layoutName
    }

    fun setLayoutName(layoutName: String?) {
        this.layoutName = layoutName
    }

    fun getLanguageName(): String? {
        return languageName
    }

    fun setLanguageName(languageName: String?) {
        this.languageName = languageName
    }

    companion object {
        private val ISO_LANGUAGES: MutableSet<String?>? = HashSet(Arrays.asList(*Locale.getISOLanguages()))
        private val FONT_CODES: MutableSet<String?>? = HashSet(Arrays.asList(*arrayOf<String?>("regular", "bold", "italic", "underline")))
    }

    init {
        resourceName = languageCode + "_" + fontCode + "_" + layoutName
        layoutDisplayName = StringUtils.capitalize(layoutName) + " (" + StringUtils.capitalize(languageName) + ")"
    }
}