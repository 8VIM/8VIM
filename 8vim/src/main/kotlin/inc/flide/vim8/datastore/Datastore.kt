package inc.flide.vim8.datastore

object Datastore {
    private val preferenceModelCache: HashMap<KClass<*>, CachedPreferenceModel<*>> = hashMapOf()

    @Suppress("unchecked_cast")
    fun <T : PreferenceModel> getOrCreatePreferenceModel(
        kClass: KClass<T>,
        factory: () -> T,
    ): CachedPreferenceModel<T> = synchronized(preferenceModelCache) {
        return preferenceModelCache.getOrPut(kClass) {
            CachedPreferenceModel(factory())
        } as CachedPreferenceModel<T>
    }
}

class CachedPreferenceModel<T : PreferenceModel>(
    private val preferenceModel: T,
) : ReadOnlyProperty<Any?, T> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return preferenceModel
    }
}
