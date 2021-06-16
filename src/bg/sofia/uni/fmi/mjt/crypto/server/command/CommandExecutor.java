package bg.sofia.uni.fmi.mjt.crypto.server.command;

import bg.sofia.uni.fmi.mjt.crypto.server.currencyinfo.CoinApiClient;
import bg.sofia.uni.fmi.mjt.crypto.server.exceptions.CurrencyPurchaseException;
import bg.sofia.uni.fmi.mjt.crypto.server.exceptions.CurrencySaleException;
import bg.sofia.uni.fmi.mjt.crypto.server.users.UsersController;
import bg.sofia.uni.fmi.mjt.crypto.server.wallet.CryptoCurrencyWallet;

public class CommandExecutor {

    private final UsersController usersController;
    private final CoinApiClient coinApiClient;

    public CommandExecutor(final UsersController usersController,
                           final CoinApiClient coinApiClient) {
        this.usersController = usersController;
        this.coinApiClient = coinApiClient;
    }

    // most commands have a private method here which validates their input
    // the ClientKeys used are the selection keys in the server
    // and provide a way to know which clients are logged in and as which users.
    public String executeCommand(Object clientKey, final Command command) {
        if (command == null) {
            return NULLS;
        }

        return switch (command.keyWord().toLowerCase()) {
            case "register" -> register(command);
            case "login" -> login(clientKey, command);
            case "logout" -> usersController.logout(clientKey);
            case "disconnect" -> usersController.disconnect(clientKey);
            case "deposit" -> deposit(clientKey, command);
            case "buy" -> buy(clientKey, command);
            case "sell" -> sell(clientKey, command);
            case "get-wallet-summary" -> getWalletSummary(
                    clientKey, command, false);
            case "get-wallet-overall-summary" -> getWalletSummary(
                    clientKey, command, true);
            case "list-offerings" -> listOfferings(command);

            default -> "[ invalid command ]";
        };
    }

    private String register(final Command command) {
        if (command.currencyOrUsername() == null
                || command.password() == null) {
            if (command.quantity() != null) {
                return NUMBER_PASSWORD;
            }
            return NULLS;
        }
        return usersController.register(
                command.currencyOrUsername(),
                command.password());
    }

    private String login(final Object clientKey, final Command command) {
        if (command.currencyOrUsername() == null
                || command.password() == null) {
            return NULLS;
        }
        return usersController.login(clientKey,
                command.currencyOrUsername(),
                command.password());
    }

    private String deposit(final Object clientKey, final Command command) {
        if (command.quantity() == null) {
            return NULLS;
        }
        if (!usersController.isLoggedIn(clientKey)) {
            return NOT_LOGGED_IN;
        }
        if (command.quantity() <= 0) {
            return NON_POSITIVE;
        }
        CryptoCurrencyWallet wallet = usersController.userOf(clientKey).getWallet();
        wallet.deposit(command.quantity());
        return DEPOSIT_SUCCESS + wallet.getBalance();
    }

    private String buy(final Object clientKey, final Command command) {
        if (command.currencyOrUsername() == null
                || command.quantity() == null) {
            return NULLS;
        }

        if (!usersController.isLoggedIn(clientKey)) {
            return NOT_LOGGED_IN;
        }

        if (command.quantity() <= 0) {
            return NON_POSITIVE;
        }


        CryptoCurrencyWallet wallet = usersController.userOf(clientKey).getWallet();
        double oldBalance = wallet.getBalance();
        try {
            double boughtQuantity = wallet.buy(
                    command.currencyOrUsername(), command.quantity());
            return PURCHASE_SUCCESS + boughtQuantity + " " + command.currencyOrUsername();
        } catch (CurrencyPurchaseException e) {
            System.out.println(e.getMessage());
            return e.getMessage();
        }
    }

    private String sell(final Object clientKey, final Command command) {
        if (command.currencyOrUsername() == null) {
            return NULLS;
        }

        if (!usersController.isLoggedIn(clientKey)) {
            return NOT_LOGGED_IN;
        }

        double quantity;
        if (command.quantity() == null) {
            quantity = CryptoCurrencyWallet.SELL_ALL;
        } else if (command.quantity() <= 0) {
            return NON_POSITIVE;
        } else {
            quantity = command.quantity();
        }

        CryptoCurrencyWallet wallet = usersController.userOf(clientKey).getWallet();
        double oldBalance = wallet.getBalance();
        try {
            String quantityAsString;
            wallet.sell(command.currencyOrUsername(), quantity);

            if (command.quantity() != null) {
                quantityAsString = command.quantity().toString();
            } else {
                quantityAsString = "all";
            }
            return "Successful sale of " + quantityAsString
                    + " " + command.currencyOrUsername()
                    + " valued at " + (wallet.getBalance() - oldBalance);
        } catch (CurrencySaleException e) {
            return e.getMessage();
        }
    }

    private String getWalletSummary(final Object clientKey,
                                    final Command command,
                                    final Boolean overall) {
        if (!usersController.isLoggedIn(clientKey)) {
            return NOT_LOGGED_IN;
        }

        CryptoCurrencyWallet wallet = usersController.userOf(clientKey).getWallet();
        return wallet.walletSummary(overall);
    }

    private String listOfferings(final Command command) {
        if (command.currencyOrUsername() == null) {
            return NULLS;
        }
        return coinApiClient.listOfferings(command.currencyOrUsername());
    }

    public static final String NULLS = "[ Necessary parameters missing ]";
    public static final String NOT_LOGGED_IN = "[ you are not logged in ]";
    public static final String NON_POSITIVE = "[ quantity is not positive ]";
    public static final String DEPOSIT_SUCCESS = "Cash deposited successfully. New balance ";
    public static final String PURCHASE_SUCCESS = "Successful purchase of ";
    public static final String NUMBER_PASSWORD = "[ Password cannot be a number ]";
}
