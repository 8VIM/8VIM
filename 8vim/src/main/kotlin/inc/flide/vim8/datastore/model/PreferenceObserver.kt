package inc.flide.vim8.datastore.model

fun interface PreferenceObserver<V : Any> {
    fun onChanged(newValue: V)
}
