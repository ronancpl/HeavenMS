package tools.exceptions;

public class NotEnabledException extends RuntimeException {

    public NotEnabledException() {
        super("Feature not enabled, please enable the feature in ServerConstant");
    }

    public NotEnabledException(String message) {
        super(message);
    }
}
