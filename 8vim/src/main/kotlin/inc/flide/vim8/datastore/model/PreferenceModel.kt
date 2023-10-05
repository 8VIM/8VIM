package inc.flide.vim8.datastore.model

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.preference.PreferenceManager
import inc.flide.vim8.lib.android.tryOrNull
import java.util.concurrent.atomic.AtomicBoolean

abstract class PreferenceModel(val version: Int) :
    SharedPreferences.OnSharedPreferenceChangeListener {
    companion object {
        private const val INTERNAL_PREFIX = "__internal"
        internal const val DATASTORE_VERSION = "${INTERNAL_PREFIX}_datastore_version"
    }

    internal lateinit var sharedPreferences: SharedPreferences
    private val isReady: AtomicBoolean = AtomicBoolean(false)

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        registry[key]?.dispatch()
    }

    /*
    Allows migration to run if a user already has installed an old version 8VIM
    This should temporary until
     */
    private fun detectPreviousDataStoreVersion(): Int {
        val default = if (sharedPreferences.all.isEmpty()) version else 0
        return sharedPreferences.getInt(DATASTORE_VERSION, default)
    }

    private val registry: MutableMap<String, PreferenceData<*>> = mutableMapOf()
    private var onReadyObserver: PreferenceObserver<Boolean>? = null

    fun isReady(): Boolean = isReady.get()

    fun onReady(observer: PreferenceObserver<Boolean>) {
        if (onReadyObserver == null) {
            onReadyObserver = observer
        }
        if (isReady()) {
            onReadyObserver?.onChanged(true)
        }
    }

    fun onReady(owner: LifecycleOwner, observer: PreferenceObserver<Boolean>) {
        if (owner.lifecycle.currentState == Lifecycle.State.DESTROYED) {
            return
        }
        val eventObserver = LifecycleEventObserver { _, _ ->
            var currentState = owner.lifecycle.currentState
            if (currentState == Lifecycle.State.DESTROYED) {
                onReadyObserver = null
                return@LifecycleEventObserver
            }
            var prevState: Lifecycle.State? = null
            while (prevState != currentState) {
                prevState = currentState
                currentState = owner.lifecycle.currentState
            }
        }
        if (onReadyObserver == null) {
            onReadyObserver = observer
            owner.lifecycle.addObserver(eventObserver)
        }
        if (isReady()) {
            onReadyObserver?.onChanged(true)
        }
    }

    private fun registryAdd(prefData: PreferenceData<*>) {
        registry[prefData.key] = prefData
    }

    @Suppress("Unchecked_cast")
    private fun <V : Any> PreferenceData<V>.serialize(
        editor: Editor,
        rawValue: Any?
    ) {
        rawValue?.let { serde.serialize(editor, key, it as V) }
    }

    protected open fun migrate(
        previousVersion: Int,
        entry: PreferenceMigrationEntry
    ): PreferenceMigrationEntry {
        return entry.keepAsIs()
    }

    protected open fun postInitialize() {
    }

    private fun <V : Any> PreferenceData<V>.deserialize(rawValue: Any?) {
        serde.deserialize(rawValue)?.let { set(it, sync = false) }
    }

    protected fun boolean(
        key: String,
        default: Boolean
    ): PreferenceData<Boolean> {
        val prefData = BooleanSharedPreferencePreferenceData(this, key, default)
        registryAdd(prefData)
        return prefData
    }

    protected fun float(
        key: String,
        default: Float
    ): PreferenceData<Float> {
        val prefData = FloatSharedPreferencePreferenceData(this, key, default)
        registryAdd(prefData)
        return prefData
    }

    protected fun int(
        key: String,
        default: Int
    ): PreferenceData<Int> {
        val prefData = IntSharedPreferencePreferenceData(this, key, default)
        registryAdd(prefData)
        return prefData
    }

    protected fun string(
        key: String,
        default: String
    ): PreferenceData<String> {
        val prefData = StringSharedPreferencePreferenceData(this, key, default)
        registryAdd(prefData)
        return prefData
    }

    protected fun stringSet(
        key: String,
        default: Set<String>
    ): PreferenceData<Set<String>> {
        val prefData = StringSetSharedPreferencePreferenceData(this, key, default)
        registryAdd(prefData)
        return prefData
    }

    protected inline fun <reified V : Enum<V>> enum(
        key: String,
        default: V
    ): PreferenceData<V> {
        val serde = object : PreferenceSerDe<V> {
            override fun serialize(editor: Editor, key: String, value: V) {
                editor.putString(key, value.toString())
            }

            override fun deserialize(
                sharedPreferences: SharedPreferences,
                key: String,
                default: V
            ): V {
                val stringValue =
                    StringPreferenceSerde.deserialize(sharedPreferences, key, default.toString())
                return deserialize(stringValue) ?: default
            }

            override fun deserialize(value: Any?): V? {
                return tryOrNull { enumValueOf<V>(value.toString()) }
            }
        }
        return custom(key, default, serde)
    }

    protected fun <V : Any> custom(
        key: String,
        default: V,
        serde: PreferenceSerDe<V>
    ): PreferenceData<V> {
        val prefData = CustomSharedPreferencePreferenceData(this, key, default, serde)
        registryAdd(prefData)
        return prefData
    }

    fun initialize(context: Context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPreferences.edit()
        val prefsEntries = sharedPreferences.all.entries.toSet()
        val oldVersion = detectPreviousDataStoreVersion()
        prefsEntries.forEach { entry ->
            var prefKey = entry.key
            var rawValue = entry.value
            var updateValue = false
            (oldVersion..version).forEach { fromVersion ->
                val migrationResult = migrate(
                    fromVersion,
                    PreferenceMigrationEntry(
                        action = PreferenceMigrationEntry.Action.KEEP_AS_IS,
                        key = prefKey,
                        rawValue = rawValue
                    )
                )
                when (migrationResult.action) {
                    PreferenceMigrationEntry.Action.KEEP_AS_IS -> {
                        /* Do nothing and continue as no migration is needed */
                    }

                    PreferenceMigrationEntry.Action.RESET -> editor.remove(prefKey)
                    PreferenceMigrationEntry.Action.TRANSFORM -> {
                        updateValue = true
                        if (migrationResult.key != prefKey) {
                            editor.remove(prefKey)
                        }
                        prefKey = migrationResult.key
                        rawValue = migrationResult.rawValue
                    }
                }
            }
            registry[prefKey]?.let { prefData ->
                prefData.deserialize(rawValue)
                if (updateValue) {
                    prefData.serialize(editor, prefData.get())
                }
            }
        }
        editor
            .putInt(DATASTORE_VERSION, version)
            .apply()
        postInitialize()
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        isReady.set(true)
        onReadyObserver?.onChanged(true)
    }
}
