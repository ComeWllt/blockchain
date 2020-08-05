package blockchain;

import java.io.Serializable;

/**
 * The virtual coins of the blockchain
 */
public class VCoin implements Serializable {

    private static final long serialVersionUID = 13L;

    private final long amount;
    private final String currencyName = "VC";

    public VCoin(long amount) {
        this.amount = amount;
    }

    public long getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return amount + " " + currencyName;
    }
}
