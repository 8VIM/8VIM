package inc.flide.vim8.structures.exceptions;

import androidx.annotation.Nullable;
import com.networknt.schema.ValidationMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class InvalidYamlException extends YamlException {
    private final String message;

    public InvalidYamlException(String message) {
        super(message);
        this.message = message;
    }

    public InvalidYamlException(Set<ValidationMessage> validationMessages) {
        super();
        List<String> messages = new ArrayList<>();
        for (ValidationMessage validationMessage : validationMessages) {
            messages.add(validationMessage.getMessage());
        }

        message = String.join("\n", messages);
    }

    @Nullable
    @Override
    public String getMessage() {
        return message;
    }
}
