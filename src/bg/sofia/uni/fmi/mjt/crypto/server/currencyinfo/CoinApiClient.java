package bg.sofia.uni.fmi.mjt.crypto.server.currencyinfo;

import bg.sofia.uni.fmi.mjt.crypto.server.exceptions.CoinApiNetworkException;
import bg.sofia.uni.fmi.mjt.crypto.server.exceptions.CurrencyPriceException;
import bg.sofia.uni.fmi.mjt.crypto.server.network.Main;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CoinApiClient {
    private static final String API_KEY =
            "D3DF6C70-B390-4598-A09D-6EF73F1D97BC";
    public static final int UPDATE_INTERVAL = 30 * 60 * 1000;

    private final HttpClient client;
    private final Map<String, Double> cachedPrices;
    private final Logger logger;


    public CoinApiClient(final HttpClient client) {
        this.client = client;
        cachedPrices = new ConcurrentHashMap<>();

        logger = Logger.getGlobal();
        try {
            FileHandler fh = new FileHandler(Main.logDestination, true);
            logger.addHandler(fh);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "file logger could not be initialized");
        }

        Thread updateDaemon = new Thread(this::autoUpdater);
        updateDaemon.setDaemon(true);
        updateDaemon.start();
    }

    // daemon to update the cached prices every 30 minutes
    private void autoUpdater() {
        while (true) {
            boolean hasUpdated;
            try {
                updateCache();
                hasUpdated = true;
            } catch (CoinApiNetworkException e) {
                hasUpdated = false;
            }

            try {
                if (hasUpdated) {
                    Thread.sleep(UPDATE_INTERVAL);
                } else {
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, "update daemon interrupted");
            }
        }
    }

    // only loads cryptocurrencies that have a price in usd
    public void updateCache() throws CoinApiNetworkException {
        URI uri;
        try {
            uri = new URI("https",
                    "rest.coinapi.io",
                    "/v1/assets",
                    null);
        } catch (URISyntaxException e) {
            logger.log(Level.WARNING, "error in retrieving offerings");
            throw new CoinApiNetworkException("error in retrieving offerings", e);
        }

        HttpRequest request = HttpRequest.newBuilder().uri(uri)
                .setHeader("X-CoinAPI-Key", API_KEY).build();
        try {
            HttpResponse<String> response
                    = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new CoinApiNetworkException("connection problems");
            }

            JsonArray currencies
                    = JsonParser.parseString
                    (response.body())
                    .getAsJsonArray();

            for (int i = 0; i < currencies.size(); i++) {
                JsonObject object = currencies.get(i).getAsJsonObject();
                if (object.get("type_is_crypto").getAsInt() == 1
                        && object.has("price_usd")
                        && !object.get("price_usd").isJsonNull()) {
                    cachedPrices.put(
                            object.get("asset_id").getAsString(),
                            object.get("price_usd").getAsDouble());
                }
            }
        } catch (InterruptedException | IOException e) {
            throw new CoinApiNetworkException("error in retrieving offerings", e);
        }
    }

    // requires a prefix as a parameter.
    // lists those currencies whose code starts with the prefix
    public String listOfferings(final String prefix) {
        StringBuilder resultBuilder = new StringBuilder();

        Set<String> prefixedSet = cachedPrices.keySet().stream()
                .filter(key -> key.toUpperCase().startsWith(prefix.toUpperCase()))
                .collect(Collectors.toSet());

        for (String key : prefixedSet) {
            resultBuilder.append(key).append(" : ")
                    .append(cachedPrices.get(key)).append(System.lineSeparator());
        }
        return resultBuilder.toString();
    }

    public Boolean currencyExists(final String currency) {
        if (currency == null) {
            return false;
        }
        return cachedPrices.containsKey(currency.toUpperCase());
    }

    public double getPriceOf(final String currency) throws CurrencyPriceException {
        if (currency == null
                || !cachedPrices.containsKey(currency.toUpperCase())) {
            throw new CurrencyPriceException(
                    "Currency cannot be found or its price cannot be retrieved");
        }
        return cachedPrices.get(currency.toUpperCase());
    }
}