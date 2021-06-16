package bg.sofia.uni.fmi.mjt.crypto.server.exceptions;

public class CurrencyPurchaseException extends Exception {
    public CurrencyPurchaseException(final String message) {
        super(message);
    }
    public CurrencyPurchaseException(final String message, final Exception e) {
        super(message, e);
    }

}
