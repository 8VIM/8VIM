package inc.flide.vim8.datastore.model

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.atomic.AtomicReference

interface PreferenceData<V : Any> : OnSharedPreferenceChangeListener {
    val serde: PreferenceSerDe<V>
    val key: String
    val default: V
    fun get(): V
    fun getOrNull(): V?
    fun set(value: V, sync: Boolean = true)
    fun reset()
    fun hasObservers(): Boolean
    fun observe(owner: LifecycleOwner, observer: PreferenceObserver<V>)
    fun observe(observer: PreferenceObserver<V>)
    fun removeObserver(observer: PreferenceObserver<V>)
}

internal abstract class SharedPreferencePreferenceData<V : Any>(
    private val model: PreferenceModel
) : PreferenceData<V> {

    private var cachedValue: AtomicReference<V?> = AtomicReference(null)

    private val observers: HashSet<PreferenceObserver<V>> = HashSet()

    final override fun get(): V = cachedValue.get() ?: default

    final override fun getOrNull(): V? = cachedValue.get()
    final override fun set(value: V, sync: Boolean) {
        if (cachedValue.get() != value) {
            cachedValue.set(value)
            if (sync) {
                val editor = model.sharedPreferences.get()!!.edit()
                serde.serialize(editor, key, value)
                editor.apply()
            }
        }
    }

    final override fun hasObservers(): Boolean = observers.isNotEmpty()
    final override fun reset() {
        model.sharedPreferences.get()!!.edit().remove(key).apply()
        cachedValue.set(null)
    }

    override fun observe(owner: LifecycleOwner, observer: PreferenceObserver<V>) {
        if (owner.lifecycle.currentState == Lifecycle.State.DESTROYED) {
            return
        }
        val eventObserver =
            LifecycleEventObserver { source, event ->
                var currentState = owner.lifecycle.currentState
                if (currentState == Lifecycle.State.DESTROYED) {
                    removeObserver(observer)
                    return@LifecycleEventObserver
                }
                var prevState: Lifecycle.State? = null
                while (prevState != currentState) {
                    prevState = currentState
                    currentState = owner.lifecycle.currentState
                }
            }
        if (observers.add(observer))
            owner.lifecycle.addObserver(eventObserver)

    }

    override fun observe(observer: PreferenceObserver<V>) {
        observers.add(observer)
    }

    override fun removeObserver(observer: PreferenceObserver<V>) {
        observers.remove(observer)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == this.key) {
            val newValue = serde.deserialize(model.sharedPreferences.get()!!, key, default)
            cachedValue.set(newValue)
            observers.forEach { it.onChanged(newValue) }
        }
    }
}

internal class BooleanSharedPreferencePreferenceData(
    model: PreferenceModel,
    override val key: String,
    override val default: Boolean
) : SharedPreferencePreferenceData<Boolean>(model) {
    override val serde: PreferenceSerDe<Boolean> = BooleanPreferenceSerde
}

internal class FloatSharedPreferencePreferenceData(
    model: PreferenceModel,
    override val key: String,
    override val default: Float
) : SharedPreferencePreferenceData<Float>(model) {
    override val serde: PreferenceSerDe<Float> = FloatPreferenceSerde
}

internal class IntSharedPreferencePreferenceData(
    model: PreferenceModel,
    override val key: String,
    override val default: Int
) : SharedPreferencePreferenceData<Int>(model) {
    override val serde: PreferenceSerDe<Int> = IntPreferenceSerde
}

internal class LongSharedPreferencePreferenceData(
    model: PreferenceModel,
    override val key: String,
    override val default: Long
) : SharedPreferencePreferenceData<Long>(model) {
    override val serde: PreferenceSerDe<Long> = LongPreferenceSerde
}

internal class StringSharedPreferencePreferenceData(
    model: PreferenceModel,
    override val key: String,
    override val default: String
) : SharedPreferencePreferenceData<String>(model) {
    override val serde: PreferenceSerDe<String> = StringPreferenceSerde
}


internal class StringSetSharedPreferencePreferenceData(
    model: PreferenceModel,
    override val key: String,
    override val default: Set<String>
) : SharedPreferencePreferenceData<Set<String>>(model) {
    override val serde: PreferenceSerDe<Set<String>> = StringSetPreferenceSerde
}

internal class CustomSharedPreferencePreferenceData<V : Any>(
    model: PreferenceModel,
    override val key: String,
    override val default: V,
    override val serde: PreferenceSerDe<V>
) : SharedPreferencePreferenceData<V>(model)