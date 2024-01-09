package inc.flide.vim8.lib

import androidx.annotation.RestrictTo

class PointerMap<P : Pointer>(val capacity: Int = 4, init: (Int) -> P) : Iterable<P> {
    private val pointers: List<P> = List(capacity.coerceAtLeast(1)) { i ->
        init(i).also { pointer -> pointer.reset() }
    }

    fun add(id: Int, index: Int): P? {
        for (pointer in pointers) {
            if (pointer.isNotUsed) {
                pointer.id = id
                pointer.index = index
                return pointer
            }
        }
        return null
    }

    fun clear() {
        for (pointer in pointers) {
            pointer.reset()
        }
    }

    /**
     * Finds a pointer by given [id].
     *
     * @param id The id of the pointer which should be found.
     *
     * @return The pointer with given [id] or null.
     */
    fun findById(id: Int): P? {
        for (pointer in pointers) {
            if (pointer.id == id) {
                return pointer
            }
        }
        return null
    }

    /**
     * Gets a pointer from the internal array based on the internal array index. This method
     * is intended to be used only by the [PointerIterator].
     *
     * @param index
     *
     * @return The pointer for given index or null, excluding unused pointers.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
    fun get(index: Int): P? {
        val pointer = pointers.getOrNull(index)
        if (pointer != null && pointer.isUsed) {
            return pointer
        }
        return null
    }

    override fun iterator(): Iterator<P> {
        return PointerIterator(this)
    }

    /**
     * Removes a pointer with given [id] and returns a boolean result.
     *
     * @param id The id of the pointer to remove. If the id is not existent, noting happens.
     *
     * @return True if a pointer was removed, false otherwise.
     */
    fun removeById(id: Int): Boolean {
        for (pointer in pointers) {
            if (pointer.id == id) {
                pointer.reset()
                return true
            }
        }
        return false
    }

    /**
     * Returns the size of this map (only counting active pointers). This value is anywhere
     * between 0 and [capacity].
     */
    val size: Int
        get() = pointers.count { it.isUsed }
}

class PointerIterator<P : Pointer>(private val pointerMap: PointerMap<P>) : Iterator<P> {
    private var index: Int = 0

    override fun hasNext(): Boolean {
        do {
            if (pointerMap.get(index) != null) {
                return true
            }
        } while (++index < pointerMap.capacity)
        return false
    }

    override fun next(): P {
        return pointerMap.get(index++)!!
    }
}

/**
 * Abstract touch pointer definition.
 */
abstract class Pointer {
    companion object {
        const val UNUSED_P: Int = -1
    }

    /**
     * The id of this pointer, corresponds to the motion event this pointer originated.
     */
    var id: Int = UNUSED_P

    /**
     * The index of this pointer, corresponds to the motion event this pointer originated.
     */
    var index: Int = UNUSED_P

    /**
     * True if this pointer is used and active, false otherwise.
     */
    val isUsed: Boolean
        get() = id >= 0

    /**
     * False if this pointer is used and active, true otherwise.
     */
    val isNotUsed: Boolean
        get() = !isUsed

    /**
     * Resets this pointer to be used again.
     */
    open fun reset() {
        id = UNUSED_P
        index = UNUSED_P
    }
}
