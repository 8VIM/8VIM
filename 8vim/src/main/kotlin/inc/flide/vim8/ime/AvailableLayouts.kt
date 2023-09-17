package inc.flide.vim8.ime

import android.content.Context
import arrow.core.elementAtOrNone
import inc.flide.vim8.ime.actionlisteners.MainKeypadActionListener
import inc.flide.vim8.models.AppPrefs
import inc.flide.vim8.models.CustomLayout
import inc.flide.vim8.models.EmbeddedLayout
import inc.flide.vim8.models.Layout
import inc.flide.vim8.models.appPreferenceModel
import inc.flide.vim8.models.embeddedLayouts
import inc.flide.vim8.models.loadKeyboardData
import inc.flide.vim8.models.toCustomLayout

class AvailableLayouts internal constructor(context: Context) {
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

    fun selectLayout(context: Context, which: Int) {
        if (index != which) {
            val embeddedLayoutSize = embeddedLayoutsWithName.size
            val layoutOption = if (which < embeddedLayoutSize) {
                embeddedLayouts.elementAtOrNone(which)
            } else {
                customLayouts.elementAtOrNone(which - embeddedLayoutSize)
            }
            layoutOption
                .flatMap { layout ->
                    layout.loadKeyboardData(context).getOrNone().map { layout to it }
                        .onNone { reloadCustomLayouts(context) }
                }
                .onSome { (layout, keyboardData) ->
                    prefs.layout.current.set(layout)
                    MainKeypadActionListener.rebuildKeyboardData(keyboardData)
                    index = which
                }
        }
    }

    private fun listCustomLayoutHistory(context: Context) {
        val uris = LinkedHashSet(prefs.layout.custom.history.get())
        customLayoutsWithName.clear()
        val newUris = linkedSetOf<String>()
        for (customLayoutUriString in uris) {
            val layout = customLayoutUriString.toCustomLayout()
            val keyboardData = layout.loadKeyboardData(context).getOrNull()
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
            prefs.layout.current.reset()
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
