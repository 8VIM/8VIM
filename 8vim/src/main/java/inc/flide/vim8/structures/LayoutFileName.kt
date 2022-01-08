package inc.flide.vim8.structures

import org.apache.commons.lang3.StringUtils
import java.util.*

class LayoutFileName() {
    private var languageCode: String = "en"
    private var fontCode: String = "regular"
    private var layoutName: String = "8pen"
    private var languageName: String = "English"
    private var isValidLayout = true
    private var resourceName: String
    private var layoutDisplayName: String?

    fun getResourceName() : String {
        return resourceName
    }

    fun isValidLayout() : Boolean {
        return isValidLayout
    }

    fun getLayoutDisplayName() : String? {
        return layoutDisplayName
    }

    constructor(fileName: String) : this() {
        val nameComponents: Array<String?> = fileName.split("_", ignoreCase = false, limit = 3).toTypedArray()
        if (nameComponents.size != 3) {
            setLayoutValidityFalse()
            return
        }
        if (ISO_LANGUAGES.contains(nameComponents[0]) && FONT_CODES.contains(nameComponents[1])) {
            resourceName = fileName
            languageCode = nameComponents[0]!!
            fontCode = nameComponents[1]!!
            layoutName = nameComponents[2]!!
            languageName = Locale.forLanguageTag(languageCode).getDisplayName(Locale(languageCode))
            layoutDisplayName = StringUtils.capitalize(layoutName) + " (" + StringUtils.capitalize(languageName) + ")"
            isValidLayout = true
        } else {
            setLayoutValidityFalse()
        }
    }

    private fun setLayoutValidityFalse() {
        isValidLayout = false
        languageCode = ""
        fontCode = ""
        layoutName = ""
        languageName = ""
    }

    companion object {
        private val ISO_LANGUAGES: MutableSet<String?> = HashSet(listOf(*Locale.getISOLanguages()))
        private val FONT_CODES: MutableSet<String?> = HashSet(
                listOf(
                "regular",
                "bold",
                "italic",
                "underline"
            )
        )
    }

    init {
        resourceName = languageCode + "_" + fontCode + "_" + layoutName
        layoutDisplayName = StringUtils.capitalize(layoutName) + " (" + StringUtils.capitalize(languageName) + ")"
    }
}