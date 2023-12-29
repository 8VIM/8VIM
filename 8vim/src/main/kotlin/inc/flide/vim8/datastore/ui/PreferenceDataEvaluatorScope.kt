package inc.flide.vim8.datastore.ui

import androidx.compose.runtime.Composable
import inc.flide.vim8.datastore.model.PreferenceData
import inc.flide.vim8.datastore.model.observeAsState

typealias PreferenceDataEvaluator = @Composable PreferenceDataEvaluatorScope.() -> Boolean

class PreferenceDataEvaluatorScope {
    companion object {
        private val staticInstance = PreferenceDataEvaluatorScope()

        fun instance() = staticInstance
    }

    @Composable
    infix fun <V : Any> PreferenceData<V>.isEqualTo(other: PreferenceData<V>): Boolean {
        val pref1 = this.observeAsState()
        val pref2 = other.observeAsState()
        return pref1.value == pref2.value
    }

    @Composable
    infix fun <V : Any> PreferenceData<V>.isEqualTo(other: V): Boolean {
        val pref = this.observeAsState()
        return pref.value == other
    }

    @Composable
    infix fun <V : Any> V.isEqualTo(other: PreferenceData<V>): Boolean {
        val pref = other.observeAsState()
        return this == pref.value
    }

    @Composable
    infix fun <V : Any> PreferenceData<V>.isNotEqualTo(other: PreferenceData<V>): Boolean {
        return !(this isEqualTo other)
    }

    @Composable
    infix fun <V : Any> PreferenceData<V>.isNotEqualTo(other: V): Boolean {
        return !(this isEqualTo other)
    }

    @Composable
    infix fun <V : Any> V.isNotEqualTo(other: PreferenceData<V>): Boolean {
        return !(this isEqualTo other)
    }

    @Composable
    fun PreferenceData<Boolean>.isTrue(): Boolean {
        return this isEqualTo true
    }

    @Composable
    fun PreferenceData<Boolean>.isFalse(): Boolean {
        return this isEqualTo false
    }
}
