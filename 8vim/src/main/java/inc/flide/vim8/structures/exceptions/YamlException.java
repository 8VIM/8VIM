package inc.flide.vim8.structures.exceptions;

public abstract class YamlException extends RuntimeException {
    public YamlException() {
        super();
    }

    public YamlException(String message) {
        super(message);
    }

    public YamlException(String message, Throwable exception) {
        super(message, exception);
    }

}
