package bg.sofia.uni.fmi.mjt.crypto.server.exceptions;

public class CoinApiNetworkException extends Exception{
    public CoinApiNetworkException(final String message, final Exception e) {
        super(message, e);
    }
    public CoinApiNetworkException(final String message) {
        super(message);
    }
}
