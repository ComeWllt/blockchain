package blockchain;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Block implements Serializable {

    private static final long serialVersionUID = 13L;

    private final int id;
    private final long timeStamp;
    private final int magicNumber;
    private final double generationDuration;
    private String changeOfNbOfZeros;
    private final String minerId;
    private final VCoin minerAward;
    private final String hash;
    private final String hashOfPreviousBlock;
    private final List<Transaction> transactions;

    public Block(int id, String hashOfPreviousBlock, long timestamp,
                 int magicNumber, double generationDuration,
                 List<Transaction> transactions, String minerId, VCoin minerAward) {
        this.id = id;
        this.hashOfPreviousBlock = hashOfPreviousBlock;
        this.magicNumber = magicNumber;
        this.generationDuration = generationDuration;
        this.timeStamp = timestamp;
        this.minerId = minerId;
        this.transactions = transactions;
        this.minerAward = minerAward;
        this.hash = HashCreator.createHash(hashOfPreviousBlock, id, timeStamp,
                magicNumber, transactions, minerId, minerAward);
    }

    public int getId() {
        return id;
    }

    public double getGenerationDuration() {
        return generationDuration;
    }

    public String getHashOfPreviousBlock() {
        return hashOfPreviousBlock;
    }

    public String getHash() {
        return hash;
    }

    public String getMinerId() {
        return minerId;
    }

    public VCoin getMinerAward() {
        return minerAward;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setChangeOfZeros(int change) {
        switch (change) {
            case 0:
                this.changeOfNbOfZeros = "N stays the same";
                break;
            case 1:
                this.changeOfNbOfZeros = "N was increased by 1";
                break;
            case -1:
                this.changeOfNbOfZeros = "N was decreased by 1";
                break;
            default:
                this.changeOfNbOfZeros = "?";
        }
    }

    @Override
    public String toString() {
        String strTransactions = transactions.isEmpty() ? "No transactions" :
                transactions.stream()
                        .map(Objects::toString)
                        .collect(Collectors.joining("\n"));
        return String.format("Block: %n" +
                        "Created by %s%n" +
                        "%s gets %s%n" +
                        "Id: %s%nTimestamp: %d%n" +
                        "Magic number: %d%n" +
                        "Hash of the previous block: %n%s%n" +
                        "Hash of the block: %n%s%n" +
                        "Block data:%n" +
                        "%s%n" +
                        "Block was generating for %.2f seconds%n" +
                        "%s%n",
                minerId, minerId, minerAward, id, timeStamp, magicNumber, hashOfPreviousBlock,
                hash, strTransactions, generationDuration, changeOfNbOfZeros);
    }
}
