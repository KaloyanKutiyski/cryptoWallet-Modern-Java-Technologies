import bg.sofia.uni.fmi.mjt.crypto.server.command.Command;
import bg.sofia.uni.fmi.mjt.crypto.server.command.CommandExecutor;
import bg.sofia.uni.fmi.mjt.crypto.server.currencyinfo.CoinApiClient;
import bg.sofia.uni.fmi.mjt.crypto.server.exceptions.CurrencyPurchaseException;
import bg.sofia.uni.fmi.mjt.crypto.server.exceptions.CurrencySaleException;
import bg.sofia.uni.fmi.mjt.crypto.server.users.User;
import bg.sofia.uni.fmi.mjt.crypto.server.users.UsersController;
import bg.sofia.uni.fmi.mjt.crypto.server.wallet.CryptoCurrencyWallet;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CommandExecutorTest {
    CommandExecutor commandExecutor;

    @Before
    public void initialize() throws CurrencyPurchaseException, CurrencySaleException {
        CoinApiClient coinApiClientMock;
        UsersController usersControllerMock;
        CryptoCurrencyWallet walletMock = mock(CryptoCurrencyWallet.class);

        coinApiClientMock = mock(CoinApiClient.class);
        usersControllerMock = mock(UsersController.class);

        when(usersControllerMock.register(USERNAME, PASSWORD))
                .thenReturn(RESPONSE);
        when(usersControllerMock.login(CLIENT_TOKEN_NOT_LOGGED_IN, USERNAME, PASSWORD))
                .thenReturn(RESPONSE);
        when(usersControllerMock.isLoggedIn(CLIENT_TOKEN_NOT_LOGGED_IN))
                .thenReturn(false);
        when(usersControllerMock.isLoggedIn(CLIENT_TOKEN_LOGGED_IN))
                .thenReturn(true);
        when(usersControllerMock.userOf(CLIENT_TOKEN_LOGGED_IN))
                .thenReturn(new User(USERNAME, PASSWORD, walletMock));

        when(walletMock.getBalance())
                .thenReturn(BALANCE);
        when(walletMock.buy(CURRENCY, QUANTITY))
                .thenReturn(QUANTITY);

        doThrow(CurrencySaleException.class)
                .when(walletMock).sell(CURRENCY_INVALID, QUANTITY);
        when(walletMock.walletSummary(anyBoolean()))
                .thenReturn(RESPONSE);
        when(coinApiClientMock.listOfferings(anyString()))
                .thenReturn(RESPONSE);


        commandExecutor = new CommandExecutor(usersControllerMock, coinApiClientMock);
    }

    @Test
    public void registerWithNullParametersTest() {
        assertEquals(CommandExecutor.NULLS,
                commandExecutor.executeCommand(
                        CLIENT_TOKEN_NOT_LOGGED_IN,
                        new Command("register",
                                null,
                                PASSWORD,
                                null)));
        assertEquals(CommandExecutor.NULLS,
                commandExecutor.executeCommand(
                        CLIENT_TOKEN_NOT_LOGGED_IN,
                        new Command(
                                "register",
                                USERNAME,
                                null,
                                null)));
    }

    @Test
    public void registerWithNumberPasswordTest() {
        assertEquals(CommandExecutor.NUMBER_PASSWORD,
                commandExecutor.executeCommand(
                        CLIENT_TOKEN_NOT_LOGGED_IN,
                        new Command("register",
                                USERNAME,
                                null,
                                QUANTITY)));
    }


    @Test
    public void registerTest() {
        assertEquals(RESPONSE,
                commandExecutor.executeCommand(
                        CLIENT_TOKEN_NOT_LOGGED_IN,
                        new Command(
                                "register",
                                USERNAME,
                                PASSWORD,
                                null)));
    }

    @Test
    public void loginWithNullParameters() {

        assertEquals(CommandExecutor.NULLS,
                commandExecutor.executeCommand(
                        CLIENT_TOKEN_NOT_LOGGED_IN,
                        new Command("login",
                                null,
                                PASSWORD,
                                null)));
        assertEquals(CommandExecutor.NULLS,
                commandExecutor.executeCommand(
                        CLIENT_TOKEN_NOT_LOGGED_IN,
                        new Command(
                                "login",
                                USERNAME,
                                null,
                                null)));
    }

    @Test
    public void loginTest() {
        assertEquals(RESPONSE,
                commandExecutor.executeCommand(
                        CLIENT_TOKEN_NOT_LOGGED_IN,
                        new Command(
                                "login",
                                USERNAME,
                                PASSWORD,
                                null)));
    }

    @Test
    public void depositWithNullParametersTest() {
        assertEquals(CommandExecutor.NULLS,
                commandExecutor.executeCommand(
                        CLIENT_TOKEN_LOGGED_IN,
                        new Command("deposit",
                                null,
                                null,
                                null
                        )
                )
        );
    }

    @Test
    public void depositAsNotLoggedIn() {
        assertEquals(CommandExecutor.NOT_LOGGED_IN,
                commandExecutor.executeCommand(
                        CLIENT_TOKEN_NOT_LOGGED_IN,
                        new Command("deposit",
                                null,
                                null,
                                QUANTITY
                        )
                )
        );
    }

    @Test
    public void depositNonPositiveAmountTest() {
        assertEquals(CommandExecutor.NON_POSITIVE,
                commandExecutor.executeCommand(
                        CLIENT_TOKEN_LOGGED_IN,
                        new Command("deposit",
                                null,
                                null,
                                NEGATIVE
                        )
                )
        );

        assertEquals(CommandExecutor.NON_POSITIVE,
                commandExecutor.executeCommand(
                        CLIENT_TOKEN_LOGGED_IN,
                        new Command("deposit",
                                null,
                                null,
                                0.0
                        )
                )
        );
    }

    @Test
    public void depositTest() {
        assertEquals(CommandExecutor.DEPOSIT_SUCCESS + BALANCE,
                commandExecutor.executeCommand(
                        CLIENT_TOKEN_LOGGED_IN,
                        new Command("deposit",
                                null,
                                null,
                                QUANTITY
                        )
                )
        );
    }

    @Test
    public void buyWithNullParametersTest() {
        assertEquals(CommandExecutor.NULLS,
                commandExecutor.executeCommand(
                        CLIENT_TOKEN_LOGGED_IN,
                        new Command("buy",
                                null,
                                null,
                                QUANTITY)
                ));
        assertEquals(CommandExecutor.NULLS,
                commandExecutor.executeCommand(
                        CLIENT_TOKEN_LOGGED_IN,
                        new Command("buy",
                                USERNAME,
                                null,
                                null)
                ));
    }

    @Test
    public void buyWhenNotLoggedIn() {
        assertEquals(CommandExecutor.NOT_LOGGED_IN,
                commandExecutor.executeCommand(
                        CLIENT_TOKEN_NOT_LOGGED_IN,
                        new Command("buy",
                                CURRENCY,
                                null,
                                QUANTITY)));
    }

    @Test
    public void buyNonPositiveAmount() {
        assertEquals(CommandExecutor.NON_POSITIVE,
                commandExecutor.executeCommand(
                        CLIENT_TOKEN_LOGGED_IN,
                        new Command("buy",
                                CURRENCY,
                                null,
                                NEGATIVE)));
        assertEquals(CommandExecutor.NON_POSITIVE,
                commandExecutor.executeCommand(
                        CLIENT_TOKEN_LOGGED_IN,
                        new Command("buy",
                                CURRENCY,
                                null,
                                0.0)));
    }

    @Test
    public void buyTest() {
        assertEquals(CommandExecutor.PURCHASE_SUCCESS
                        + QUANTITY + " " + CURRENCY,
                commandExecutor.executeCommand(
                        CLIENT_TOKEN_LOGGED_IN,
                        new Command("buy",
                                CURRENCY,
                                null,
                                QUANTITY)));
    }

    @Test
    public void sellWithNullCurrency() {
        assertEquals(CommandExecutor.NULLS,
                commandExecutor.executeCommand(
                        CLIENT_TOKEN_LOGGED_IN,
                        new Command("sell",
                                null,
                                null,
                                QUANTITY)
                ));
    }

    @Test
    public void sellWhenNotLoggedIn() {
        assertEquals(CommandExecutor.NOT_LOGGED_IN,
                commandExecutor.executeCommand(
                        CLIENT_TOKEN_NOT_LOGGED_IN,
                        new Command("sell",
                                CURRENCY,
                                null,
                                QUANTITY)));
    }

    @Test
    public void sellNonNegativeAmountTest() {
        assertEquals(CommandExecutor.NON_POSITIVE,
                commandExecutor.executeCommand(
                        CLIENT_TOKEN_LOGGED_IN,
                        new Command("sell",
                                CURRENCY,
                                null,
                                NEGATIVE)));
        assertEquals(CommandExecutor.NON_POSITIVE,
                commandExecutor.executeCommand(
                        CLIENT_TOKEN_LOGGED_IN,
                        new Command("sell",
                                CURRENCY,
                                null,
                                0.0)));
    }

    @Test
    public void sellAllTest() {
        String successMessage = "Successful sale of all "
                + CURRENCY + " valued at";
        assertTrue(commandExecutor
                .executeCommand(
                        CLIENT_TOKEN_LOGGED_IN,
                        new Command("sell",
                                CURRENCY,
                                null,
                                null))
                .startsWith(successMessage));
    }

    @Test
    public void sellQuantityTest() {
        String successMessage = "Successful sale of "
                + QUANTITY + " " + CURRENCY + " valued at";
        assertTrue(commandExecutor
                .executeCommand(
                        CLIENT_TOKEN_LOGGED_IN,
                        new Command("sell",
                                CURRENCY,
                                null,
                                QUANTITY))
                .startsWith(successMessage));
    }

    @Test
    public void getWalletSummaryWhenNotLoggedInTest() {
        assertEquals(CommandExecutor.NOT_LOGGED_IN,
                commandExecutor.executeCommand(
                        CLIENT_TOKEN_NOT_LOGGED_IN,
                        new Command("get-wallet-summary",
                                null,
                                null,
                                null)));
    }

    @Test
    public void getWalletOverallSummaryWhenNotLoggedInTest() {
        assertEquals(CommandExecutor.NOT_LOGGED_IN,
                commandExecutor.executeCommand(
                        CLIENT_TOKEN_NOT_LOGGED_IN,
                        new Command("get-wallet-overall-summary",
                                null,
                                null,
                                null)));
    }

    @Test
    public void getWalletSummaryTest() {
        assertEquals(RESPONSE,
                commandExecutor.executeCommand(
                        CLIENT_TOKEN_LOGGED_IN,
                        new Command("get-wallet-summary",
                                null,
                                null,
                                null)));
    }

    @Test
    public void getWalletOverallSummaryTest() {
        assertEquals(RESPONSE,
                commandExecutor.executeCommand(
                        CLIENT_TOKEN_LOGGED_IN,
                        new Command("get-wallet-overall-summary",
                                null,
                                null,
                                null)));
    }

    @Test
    public void listOfferingsWithNullPrefixTest() {
        assertEquals(CommandExecutor.NULLS,
                commandExecutor.executeCommand(
                        CLIENT_TOKEN_NOT_LOGGED_IN,
                        new Command("list-offerings",
                                null,
                                null,
                                null)));
    }

    @Test
    public void listOfferingsTest() {
        assertEquals(RESPONSE,
                commandExecutor.executeCommand(
                        CLIENT_TOKEN_NOT_LOGGED_IN,
                        new Command("list-offerings",
                                CURRENCY,
                                null,
                                null)));
    }

    private final String USERNAME = "username1";
    private final String PASSWORD = "password";
    private final Object CLIENT_TOKEN_LOGGED_IN = "client logged in";
    private final Object CLIENT_TOKEN_NOT_LOGGED_IN = "client not logged in";

    private final String RESPONSE = "response";

    private final double BALANCE = 1000.00;
    private final String CURRENCY_INVALID = "noCoin";
    private final String CURRENCY = "Coin";
    private final double QUANTITY = 1.1;
    private final double NEGATIVE = -0.2;
}
