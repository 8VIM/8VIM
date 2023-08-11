package inc.flide.vim8.models

import android.content.Context
import android.net.Uri
import inc.flide.vim8.keyboardactionlisteners.MainKeypadActionListener

class AvailableLayouts private constructor(context: Context) {
    private val prefs: AppPrefs by appPreferenceModel()
    private val embeddedLayoutsWithName: Map<String, EmbeddedLayout>
    private val defaultIndex: Int
    private val embeddedLayouts: List<EmbeddedLayout>
    private val layouts: MutableList<Layout<*>> = arrayListOf()
    private val customLayoutsWithName: MutableMap<String, CustomLayout> = hashMapOf()
    private val customLayouts: MutableList<CustomLayout> = arrayListOf()
    val displayNames: MutableSet<String> = linkedSetOf()
    var index = -1
        private set

    init {
        embeddedLayoutsWithName = embeddedLayouts(context)
        embeddedLayouts = embeddedLayoutsWithName.values.toList()
        defaultIndex = embeddedLayouts.indexOf(prefs.layout.current.default as EmbeddedLayout)
        reloadCustomLayouts(context)
        prefs.layout.custom.history.observe {
            if (it.size > customLayouts.size) {
                reloadCustomLayouts(context)
            }
        }
    }

    private fun reloadCustomLayouts(context: Context) {
        listCustomLayoutHistory(context)
        updateDisplayNames()
        findIndex()
    }

    fun selectLayout(context: Context?, which: Int) {
        val embeddedLayoutSize = embeddedLayoutsWithName.size
        val layoutOption = if (which < embeddedLayoutSize) {
            embeddedLayouts.getOrNull(which)
        } else {
            customLayouts.getOrNull(which - embeddedLayoutSize)
        }
        layoutOption?.let {
            prefs.layout.current.set(it, true)
            MainKeypadActionListener.rebuildKeyboardData(
                it.loadKeyboardData(context!!).getOrNull()
            )
            index = which
        }
    }

    private fun listCustomLayoutHistory(context: Context) {
        val uris = LinkedHashSet(prefs.layout.custom.history.get())
        customLayoutsWithName.clear()
        val newUris = linkedSetOf<String>()
        for (customLayoutUriString in uris) {
            val customLayoutUri = Uri.parse(customLayoutUriString)
            val layout = CustomLayout(customLayoutUri)
            val keyboardData = layout.loadKeyboardData(context)
                .getOrNull()
            if (keyboardData == null || keyboardData.totalLayers == 0) {
                continue
            }
            customLayoutsWithName[keyboardData.toString()] = layout
            newUris.add(customLayoutUriString)
        }
        if (newUris.size != uris.size) {
            prefs.layout.custom.history.set(newUris)
        }
        customLayouts.clear()
        customLayouts.addAll(customLayoutsWithName.values)
    }

    private fun findIndex() {
        index = layouts.indexOf(prefs.layout.current.get())
        if (index == -1) {
            index = defaultIndex
        }
    }

    private fun updateDisplayNames() {
        displayNames.clear()
        displayNames.addAll(embeddedLayoutsWithName.keys)
        displayNames.addAll(customLayoutsWithName.keys)
        layouts.clear()
        layouts.addAll(embeddedLayouts)
        layouts.addAll(customLayouts)
    }

    companion object {
        private var singleton: AvailableLayouts? = null
        fun initialize(context: Context) {
            if (singleton == null) {
                singleton = AvailableLayouts(context)
            }
        }

        @JvmStatic
        val instance: AvailableLayouts
            get() = singleton!!
    }
}
