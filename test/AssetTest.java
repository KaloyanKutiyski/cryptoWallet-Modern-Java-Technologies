import bg.sofia.uni.fmi.mjt.crypto.server.wallet.Asset;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AssetTest {
    private Asset asset = new Asset(3.2, 13.41);

    @Test
    public void updateWithNewPurchaseTest() {
        asset.updateWithNewPurchase(1.9, 11.987);
        assertEquals(asset.getQuantity(), 5.1, 0.0000000001);
        assertEquals(asset.getAveragedBuyPrice(), 12.879862745, 0.0000000001);
    }
}
