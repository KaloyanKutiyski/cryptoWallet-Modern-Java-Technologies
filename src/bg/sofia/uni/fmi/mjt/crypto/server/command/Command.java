package bg.sofia.uni.fmi.mjt.crypto.server.command;

public record Command(String keyWord,
                      String currencyOrUsername,
                      String password, Double quantity) {
}
