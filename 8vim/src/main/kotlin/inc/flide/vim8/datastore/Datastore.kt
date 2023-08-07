package inc.flide.vim8.datastore

import inc.flide.vim8.datastore.model.PreferenceModel
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

object Datastore {
    private val preferenceModelCache: HashMap<KClass<*>, CachedPreferenceModel<*>> = hashMapOf()

    @Suppress("unchecked_cast")
    fun <T : PreferenceModel> getOrCreatePreferenceModel(
        kClass: KClass<T>,
        factory: () -> T
    ): CachedPreferenceModel<T> = synchronized(preferenceModelCache) {
        return preferenceModelCache.getOrPut(kClass) {
            CachedPreferenceModel(factory())
        } as CachedPreferenceModel<T>
    }
}

class CachedPreferenceModel<T : PreferenceModel>(
    private val preferenceModel: T
) : ReadOnlyProperty<Any?, T> {
    fun java(): T = preferenceModel
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return preferenceModel
    }
}
