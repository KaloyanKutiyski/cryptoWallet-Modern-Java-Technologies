package bg.sofia.uni.fmi.mjt.crypto.server.exceptions;

public class CommandCreatorException extends Exception {
    public CommandCreatorException(final String message, final Exception e) {
        super(message, e);
    }

    public CommandCreatorException(final String message) {
        super(message);
    }
}
