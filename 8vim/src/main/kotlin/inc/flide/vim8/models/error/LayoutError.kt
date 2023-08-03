package inc.flide.vim8.models.error

import arrow.optics.optics
import com.networknt.schema.ValidationMessage

sealed interface LayoutError {
    val message: String
}

@optics
data class InvalidLayoutError(val validationMessages: Set<ValidationMessage>) : LayoutError {
    override val message: String
        get() = validationMessages.joinToString("\n") { it.message }

    companion object
}

data class ExceptionWrapperError(val exception: Throwable) : LayoutError {
    override val message: String
        get() = exception.message ?: ""
}