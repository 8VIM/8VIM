package inc.flide.vim8.structures.exceptions;

import java.io.IOException;

public class YamlParsingException extends YamlException {
    public YamlParsingException(IOException exception) {
        super(exception.getMessage(), exception);
    }
}
