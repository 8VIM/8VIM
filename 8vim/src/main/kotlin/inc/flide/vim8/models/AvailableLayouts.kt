package inc.flide.vim8.models

import android.content.Context
import android.net.Uri
import arrow.core.elementAtOrNone
import arrow.core.firstOrNone
import arrow.core.getOrElse
import arrow.core.getOrNone
import inc.flide.vim8.keyboardactionlisteners.MainKeypadActionListener
import inc.flide.vim8.lib.android.ext.CustomLayoutHistoryManager

class AvailableLayouts internal constructor(
    context: Context,
    customLayoutHistoryManager: CustomLayoutHistoryManager
) {
    private val prefs: AppPrefs by appPreferenceModel()
    private val defaultIndex: Int
    private val embeddedLayoutsSize: Int
    private val layoutsWithKeyboardData: MutableMap<Layout<*>, KeyboardData> = linkedMapOf()
    val displayNames: ArrayList<String> = arrayListOf()
    private val defaultKeyboard: KeyboardData
        get() = layoutsWithKeyboardData[prefs.layout.current.default]!!
    val currentKeyboardData: KeyboardData
        get() = layoutsWithKeyboardData
            .getOrNone(prefs.layout.current.get())
            .getOrElse {
                removeFromHistory(prefs.layout.current.get().path.toString())
                defaultKeyboard
            }
    var index = -1
        private set

    init {
        val embeddedLayoutsWithName = embeddedLayouts(context)
        embeddedLayoutsSize = embeddedLayoutsWithName.size
        layoutsWithKeyboardData.putAll(embeddedLayoutsWithName)
        defaultIndex =
            layoutsWithKeyboardData.keys.indexOf(prefs.layout.current.default as EmbeddedLayout)
        reloadCustomLayouts(context)

        prefs.layout.custom.history.observe {
            if (it.size > layoutsWithKeyboardData.size - embeddedLayoutsSize) {
                reloadCustomLayouts(context)
                MainKeypadActionListener.rebuildKeyboardData(currentKeyboardData)
            }
        }

        customLayoutHistoryManager.observe(object :
                CustomLayoutHistoryManager.FileChangeObserver {

                override fun onDelete(uri: Uri) {
                    removeFromHistory(uri.toString())
                    MainKeypadActionListener.rebuildKeyboardData(currentKeyboardData)
                }

                override fun onChange(uri: Uri): Boolean {
                    if (!updateKeyboardData(prefs.layout.current.get(), context)) {
                        removeFromHistory(uri.toString())
                        MainKeypadActionListener.rebuildKeyboardData(currentKeyboardData)
                        return true
                    }
                    return false
                }
            })
    }

    private fun removeFromHistory(path: String) {
        val historyPref = prefs.layout.custom.history
        val history = LinkedHashSet(historyPref.get())
        history.remove(path)
        historyPref.set(history)
        prefs.layout.current.reset()
        layoutsWithKeyboardData
            .toList()
            .firstOrNone { (layout, _) -> layout.path.toString() == path }
            .onSome { (layout, _) ->
                layoutsWithKeyboardData.remove(layout)
            }
        updateDisplayNames()
        findIndex()
    }

    private fun reloadCustomLayouts(context: Context) {
        listCustomLayoutHistory(context)
        updateDisplayNames()
        findIndex()
    }

    fun updateKeyboardData(layout: Layout<*>, context: Context): Boolean {
        return layoutsWithKeyboardData
            .getOrNone(layout)
            .flatMap { layout.loadKeyboardData(context).getOrNone() }
            .onSome {
                prefs.layout.current.set(layout)
                MainKeypadActionListener.rebuildKeyboardData(it)
            }
            .isSome()
    }

    fun selectLayout(which: Int) {
        layoutsWithKeyboardData.keys.elementAtOrNone(which)
            .flatMap { layout -> layoutsWithKeyboardData.getOrNone(layout).map { layout to it } }
            .onSome { (layout, keyboardData) ->
                prefs.layout.current.set(layout)
                MainKeypadActionListener.rebuildKeyboardData(keyboardData)
                index = which
            }
    }

    private fun listCustomLayoutHistory(context: Context) {
        val uris = LinkedHashSet(prefs.layout.custom.history.get())
        val customLayouts = layoutsWithKeyboardData
            .toList()
            .filter { it.first is CustomLayout }
            .toMap()

        customLayouts
            .filter { !uris.contains(it.key.toString()) }
            .forEach { (layout, _) -> layoutsWithKeyboardData.remove(layout) }

        uris.toList().flatMap { customLayoutUriString ->
            val customLayoutUri = Uri.parse(customLayoutUriString)
            val layout = CustomLayout(customLayoutUri)
            if (customLayouts.containsKey(layout)) {
                emptyList()
            } else {
                layout.loadKeyboardData(context)
                    .getOrNone()
                    .filterNot { it.totalLayers == 0 }
                    .map { layout to it }
                    .onNone { uris.remove(customLayoutUriString) }
                    .toList()
            }
        }.let { layoutsWithKeyboardData.putAll(it) }

        prefs.layout.custom.history.set(uris)
    }

    private fun findIndex() {
        index = layoutsWithKeyboardData.keys.indexOf(prefs.layout.current.get())
        if (index == -1) {
            index = defaultIndex
            prefs.layout.current.reset()
        }
    }

    private fun updateDisplayNames() {
        displayNames.clear()
        displayNames.addAll(layoutsWithKeyboardData.values.map { it.toString() })
    }

    companion object {
        private var singleton: AvailableLayouts? = null

        @JvmStatic
        fun initialize(context: Context, customLayoutHistoryManager: CustomLayoutHistoryManager) {
            if (singleton == null) {
                singleton = AvailableLayouts(context, customLayoutHistoryManager)
            }
        }

        @JvmStatic
        val instance: AvailableLayouts
            get() = singleton!!
    }
}
