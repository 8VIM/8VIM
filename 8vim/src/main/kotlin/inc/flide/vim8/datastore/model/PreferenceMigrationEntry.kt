package inc.flide.vim8.datastore.model

import inc.flide.vim8.lib.ExcludeFromJacocoGeneratedReport

@ExcludeFromJacocoGeneratedReport
class PreferenceMigrationEntry internal constructor(
    internal val action: Action,
    val key: String,
    val rawValue: Any?
) {
    /**
     * Keep this entry as is in the migration process.
     */
    fun keepAsIs() = if (action == Action.KEEP_AS_IS) this else copy(action = Action.KEEP_AS_IS)

    /**
     * Reset this entry back to the default value in the migration process.
     */
    fun reset() = if (action == Action.RESET) this else copy(action = Action.RESET)

    /**
     * Transform this entry's type, key, and/or raw value in the migration process.
     */
    fun transform(
        key: String = this.key,
        rawValue: Any? = this.rawValue
    ) = PreferenceMigrationEntry(Action.TRANSFORM, key, rawValue)

    private fun copy(
        action: Action = this.action,
        key: String = this.key,
        rawValue: Any? = this.rawValue
    ) = PreferenceMigrationEntry(action, key, rawValue)

    internal enum class Action {
        KEEP_AS_IS,
        RESET,
        TRANSFORM;
    }
}
