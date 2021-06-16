package bg.sofia.uni.fmi.mjt.crypto.server.wallet;

import bg.sofia.uni.fmi.mjt.crypto.server.currencyinfo.CoinApiClient;
import bg.sofia.uni.fmi.mjt.crypto.server.exceptions.CurrencyPriceException;
import bg.sofia.uni.fmi.mjt.crypto.server.exceptions.CurrencyPurchaseException;
import bg.sofia.uni.fmi.mjt.crypto.server.exceptions.CurrencySaleException;

import java.util.HashMap;
import java.util.Map;

public class CryptoCurrencyWallet {
    public static final double SELL_ALL = -1;

    private double balance;
    private final Map<String, Asset> cryptoCurrencyAssets;
    private final CoinApiClient coinApiClient;

    public CryptoCurrencyWallet(final CoinApiClient coinApiClient) {
        balance = 0.0;
        cryptoCurrencyAssets = new HashMap<>();
        this.coinApiClient = coinApiClient;
    }

    public double getBalance() {
        return balance;
    }

    public void deposit(final double quantity) {
        balance += quantity;
    }

    public double buy(final String currency, final double quantity)
            throws CurrencyPurchaseException {
        if (!coinApiClient.currencyExists(currency)) {
            throw new CurrencyPurchaseException(
                    "Currency " + currency + " does not exist");
        }

        if (quantity > balance) {
            throw new CurrencyPurchaseException("Not enough dollars to buy "
                    + quantity + " " + currency);
        }

        double price;
        try {
            price = coinApiClient.getPriceOf(currency);
        } catch (CurrencyPriceException e) {
            throw new CurrencyPurchaseException(
                    "price of currency could not be determined", e);
        }

        balance -= quantity;
        double boughtQuantity = quantity / price;
        if (cryptoCurrencyAssets.containsKey(currency)) {
            cryptoCurrencyAssets.get(currency)
                    .updateWithNewPurchase(boughtQuantity, price);
        } else {
            cryptoCurrencyAssets.put(currency,
                    new Asset(boughtQuantity, price));
        }
        return boughtQuantity;
    }

    // can either sell all currency if a quantity parameter is not given
    // or sell a specified amount smaller than the one currently owned
    public void sell(final String currency, final double quantity)
            throws CurrencySaleException {
        if (!cryptoCurrencyAssets.containsKey(currency)) {
            throw new CurrencySaleException("Wallet does not contain currency " + currency);
        }

        Asset soldCurrency = cryptoCurrencyAssets.get(currency);
        if (quantity > soldCurrency.getQuantity()) {
            throw new CurrencySaleException("Wallet contains fewer than "
                    + quantity + " " + currency);
        }

        double price;
        try {
            price = coinApiClient.getPriceOf(currency);
        } catch (CurrencyPriceException e) {
            throw new CurrencySaleException(
                    "price of currency could not be determined", e);
        }

        if (quantity == SELL_ALL || quantity == soldCurrency.getQuantity()) {
            balance += price * soldCurrency.getQuantity();
            cryptoCurrencyAssets.remove(currency);
        } else {
            balance += price * quantity;
            soldCurrency.decreaseQuantity(quantity);
        }
    }

    public String walletSummary(final Boolean overall) {
        double totalValue = balance;
        StringBuilder res = new StringBuilder();
        res.append("dollars : ").append(balance).append(System.lineSeparator());

        for (Map.Entry<String, Asset> pair : cryptoCurrencyAssets.entrySet()) {
            double priceOfUnit;
            try {
                priceOfUnit = coinApiClient.getPriceOf(pair.getKey());
            } catch (CurrencyPriceException e) {
                priceOfUnit = Double.NaN;
            }
            double valueOfAsset = Double.isNaN(priceOfUnit)
                    ? Double.NaN
                    : pair.getValue().getQuantity() * priceOfUnit;

            totalValue += Double.isNaN(valueOfAsset) ? 0 : valueOfAsset;
            res.append(pair.getValue().getQuantity())
                    .append(" ").append(pair.getKey())
                    .append(" valued @ ").append(valueOfAsset);

            if (overall) {
                double investment = pair.getValue().getQuantity()
                        * pair.getValue().getAveragedBuyPrice();
                res.append(" bought @ ").append(pair.getValue().getAveragedBuyPrice());
                if (!Double.isNaN(valueOfAsset) && valueOfAsset > investment) {
                    res.append("profit of ").append(valueOfAsset - investment);
                } else if (!Double.isNaN(valueOfAsset) && investment > valueOfAsset) {
                    res.append("loss of ").append(investment - valueOfAsset);
                }
            }

            res.append(System.lineSeparator());
        }

        res.append("total value of wallet ").append(totalValue);
        return res.toString();
    }

}
