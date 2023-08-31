package inc.flide.vim8.models.error

import arrow.optics.optics
import com.networknt.schema.ValidationMessage
import inc.flide.vim8.lib.ExcludeFromJacocoGeneratedReport

sealed interface LayoutError {
    val message: String
}

@ExcludeFromJacocoGeneratedReport
@optics
data class InvalidLayoutError(val validationMessages: Set<ValidationMessage>) : LayoutError {
    override val message: String
        get() = validationMessages.joinToString("\n") { it.message }

    companion object
}

@ExcludeFromJacocoGeneratedReport
data class ExceptionWrapperError(val exception: Throwable) : LayoutError {
    override val message: String
        get() = exception.message ?: ""
}
