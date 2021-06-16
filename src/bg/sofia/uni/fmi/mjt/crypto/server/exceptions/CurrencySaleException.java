package bg.sofia.uni.fmi.mjt.crypto.server.exceptions;

public class CurrencySaleException extends Exception {
    public CurrencySaleException(final String message) {
        super(message);
    }

    public CurrencySaleException(final String message, final Exception e) {
        super(message, e);
    }
}
