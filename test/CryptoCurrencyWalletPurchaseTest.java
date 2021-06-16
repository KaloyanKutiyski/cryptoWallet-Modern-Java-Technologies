import bg.sofia.uni.fmi.mjt.crypto.server.currencyinfo.CoinApiClient;
import bg.sofia.uni.fmi.mjt.crypto.server.exceptions.CurrencyPriceException;
import bg.sofia.uni.fmi.mjt.crypto.server.exceptions.CurrencyPurchaseException;
import bg.sofia.uni.fmi.mjt.crypto.server.wallet.CryptoCurrencyWallet;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CryptoCurrencyWalletPurchaseTest {
    private CoinApiClient coinApiClientMock;
    private CryptoCurrencyWallet wallet;

    @Before
    public void initialize() throws CurrencyPriceException {
        coinApiClientMock = mock(CoinApiClient.class);

        when(coinApiClientMock.currencyExists(CURRENCY1)).thenReturn(true);
        when(coinApiClientMock.currencyExists(CURRENCY2)).thenReturn(true);
        when(coinApiClientMock.currencyExists(CURRENCY3)).thenReturn(false);

        when(coinApiClientMock.getPriceOf(CURRENCY1))
                .thenReturn(PRICE);
        when(coinApiClientMock.getPriceOf(CURRENCY2))
                .thenThrow(CurrencyPriceException.class);


        wallet = new CryptoCurrencyWallet(coinApiClientMock);
        wallet.deposit(START_CASH);
    }

    @Test(expected = CurrencyPurchaseException.class)
    public void attemptToBuyNonExistentCurrencyTest()
            throws CurrencyPurchaseException {
        wallet.buy(CURRENCY3, 40_000.00);
    }

    @Test(expected = CurrencyPurchaseException.class)
    public void attemptToBuyCurrencyWithUndeterminedPriceTest()
            throws CurrencyPurchaseException {
        wallet.buy(CURRENCY2, 40_000.00);
    }

    @Test(expected = CurrencyPurchaseException.class)
    public void attemptToBuyMoreCurrencyThanBalanceTest()
            throws CurrencyPurchaseException {
        wallet.buy(CURRENCY1, 50_000.99);
    }

    @Test
    public void buyTest() throws CurrencyPurchaseException {
        wallet.buy(CURRENCY1, BUDGET);
        assertEquals(START_CASH - BUDGET,
                wallet.getBalance(),
                DELTA);

        String summary = wallet.walletSummary(false);

        int indexOfNewLine = summary.indexOf(System.lineSeparator());
        summary = summary.substring(indexOfNewLine + 1);

        int indexOfSpace = summary.indexOf(' ');
        assertNotEquals(indexOfSpace, -1);

        summary = summary.substring(0, indexOfSpace);

        double countOfAsset = Double.valueOf(summary);
        assertEquals(BUDGET / PRICE, countOfAsset, DELTA);
    }

    private final String CURRENCY1 = "BTC";
    private final String CURRENCY2 = "DOGEUSD";
    private final String CURRENCY3 = "XRP";

    private final double START_CASH = 50_000.00;
    private final double PRICE = 40_199.55;
    private final double BUDGET = 49_500.0;
    private final double DELTA = 0.000_000_001;
}
