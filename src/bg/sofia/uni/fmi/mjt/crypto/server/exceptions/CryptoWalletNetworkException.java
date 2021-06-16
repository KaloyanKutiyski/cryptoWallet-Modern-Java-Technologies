package bg.sofia.uni.fmi.mjt.crypto.server.exceptions;

public class CryptoWalletNetworkException extends Exception {
    public CryptoWalletNetworkException(final String message, final Exception e) {
        super(message, e);
    }
}
