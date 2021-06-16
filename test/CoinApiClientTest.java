import bg.sofia.uni.fmi.mjt.crypto.server.currencyinfo.CoinApiClient;
import bg.sofia.uni.fmi.mjt.crypto.server.exceptions.CoinApiNetworkException;
import bg.sofia.uni.fmi.mjt.crypto.server.exceptions.CurrencyPriceException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CoinApiClientTest {
    private CoinApiClient coinApiClient;
    @Mock
    private HttpClient httpClientMock;
    @Mock
    private HttpResponse responseMock;
    private String json;

    @Before
    public void initialize() {

        coinApiClient = new CoinApiClient(httpClientMock);
        String json = "["
                + "{"
                + "\"asset_id\": \"BTC\""
                + "\"type_is_crypto\": 1"
                + "\"price_usd\": 100.0"
                + "},"
                + "{"
                + "\"asset_id\": \"notCoin\""
                + "\"type_is_crypto\": 0"
                + "\"price_usd\": 100.0"
                + "},"
                + "{"
                + "\"asset_id\": \"untradable\""
                + "\"type_is_crypto\": 1"
                + "},"
                + "{"
                + "\"asset_id\": \"nullCoin\""
                + "\"type_is_crypto\": 0"
                + "\"price_usd\": null"
                + "}"
                + "]";
    }

    @Test
    public void updateCacheTest() throws Exception {

        when(httpClientMock.send(Mockito.any(HttpRequest.class),
                Mockito.any(HttpResponse.BodyHandler.class)) )
                .thenReturn(responseMock);
        when(responseMock.statusCode()).thenReturn(200);
        when(responseMock.body()).thenReturn(json);

        coinApiClient.updateCache();
        assertTrue(coinApiClient.currencyExists("btc"));
        assertFalse(coinApiClient.currencyExists("notCoin"));
        assertFalse(coinApiClient.currencyExists("untradable"));
        assertFalse(coinApiClient.currencyExists("nullCoin"));


    }

    @Test(expected = CoinApiNetworkException.class)
    public void badConnectionTest() throws Exception {
        when(httpClientMock.send(Mockito.any(HttpRequest.class),
                Mockito.any(HttpResponse.BodyHandler.class)))
                .thenReturn(responseMock);
        when(responseMock.statusCode()).thenReturn(404);
        when(responseMock.body()).thenReturn("");

        coinApiClient.updateCache();
    }

    @Test(expected = CurrencyPriceException.class)
    public void getPriceOfCurrencyNotInCacheTest() throws CurrencyPriceException {
        coinApiClient.getPriceOf("notCoin");
    }

    @Test
    public void checkNullCurrencyTest() {
        assertFalse(coinApiClient.currencyExists(null));
    }

    @Test(expected = CurrencyPriceException.class)
    public void getPriceOfNullTest() throws CurrencyPriceException {
        coinApiClient.getPriceOf(null);
    }
}
