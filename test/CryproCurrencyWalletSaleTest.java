import bg.sofia.uni.fmi.mjt.crypto.server.currencyinfo.CoinApiClient;
import bg.sofia.uni.fmi.mjt.crypto.server.exceptions.CurrencyPriceException;
import bg.sofia.uni.fmi.mjt.crypto.server.exceptions.CurrencyPurchaseException;
import bg.sofia.uni.fmi.mjt.crypto.server.exceptions.CurrencySaleException;
import bg.sofia.uni.fmi.mjt.crypto.server.wallet.CryptoCurrencyWallet;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CryproCurrencyWalletSaleTest {
    private CoinApiClient coinApiClientMock;
    private CryptoCurrencyWallet wallet;

    @Before
    public void initialize()
            throws CurrencyPriceException, CurrencyPurchaseException {
        coinApiClientMock = mock(CoinApiClient.class);

        when(coinApiClientMock.currencyExists(CURRENCY1)).thenReturn(true);
        when(coinApiClientMock.currencyExists(CURRENCY2)).thenReturn(true);
        when(coinApiClientMock.currencyExists(CURRENCY3)).thenReturn(false);

        when(coinApiClientMock.getPriceOf(CURRENCY1)).thenReturn(PRICE);


        wallet = new CryptoCurrencyWallet(coinApiClientMock);
        wallet.deposit(START_CASH);
        wallet.buy(CURRENCY1, START_CASH / 2.0);
    }

    @Test(expected = CurrencySaleException.class)
    public void attemptToSellCurrencyNotInWalletTest()
            throws CurrencySaleException {
        wallet.sell(CURRENCY2, CryptoCurrencyWallet.SELL_ALL);
    }

    @Test(expected = CurrencySaleException.class)
    public void attemptToSellMoreCurrencyThanInWalletTest()
            throws CurrencySaleException {
        wallet.sell(CURRENCY1, 2.0);
    }

    @Test
    public void sellAllTest() throws CurrencySaleException {
        wallet.sell(CURRENCY1, CryptoCurrencyWallet.SELL_ALL);
        assertEquals(START_CASH, wallet.getBalance(), DELTA);
    }

    @Test
    public void sellPartTest() throws CurrencySaleException {
        wallet.sell(CURRENCY1, 0.5);

        assertEquals((START_CASH + PRICE) / 2, wallet.getBalance(), DELTA);

        String summary = wallet.walletSummary(false);

        int indexOfNewLine = summary.indexOf(System.lineSeparator());
        summary = summary.substring(indexOfNewLine + 1);

        int indexOfSpace = summary.indexOf(' ');
        assertNotEquals(indexOfSpace, -1);

        summary = summary.substring(0, indexOfSpace);

        double valueOfAsset = Double.valueOf(summary);
        assertEquals((START_CASH - PRICE)
                / (2 * PRICE), valueOfAsset, DELTA);
    }

    private final String CURRENCY1 = "BTC";
    private final String CURRENCY2 = "DOGEUSD";
    private final String CURRENCY3 = "XRP";


    private final double START_CASH = 50_000.00;
    private final double PRICE = 40_199.55;
    private final double DELTA = 0.000_000_001;

}
