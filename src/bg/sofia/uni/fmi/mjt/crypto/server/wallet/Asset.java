package bg.sofia.uni.fmi.mjt.crypto.server.wallet;

public class Asset {
    private double quantity;
    private double averagedBuyPrice;

    public Asset(double quantity, double averagedBuyPrice) {
        this.quantity = quantity;
        this.averagedBuyPrice = averagedBuyPrice;
    }

    public void updateWithNewPurchase(final double boughtQuantity, final double buyPrice) {
        averagedBuyPrice =
                (averagedBuyPrice * quantity + buyPrice * boughtQuantity)
                        / (quantity + boughtQuantity);
        quantity += boughtQuantity;
    }

    public void decreaseQuantity(final double soldQuantity) {
        if (soldQuantity <= quantity) {
            quantity -= soldQuantity;
        }
    }

    public double getQuantity() {
        return quantity;
    }

    public double getAveragedBuyPrice() {
        return averagedBuyPrice;
    }

    @Override
    public String toString() {
        return "asset{" +
                "quantity=" + quantity +
                ", averagedBuyPrice=" + averagedBuyPrice +
                '}';
    }
}
