package blockchain;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Blockchain implements Serializable {

    private static final long serialVersionUID = 13L;
    private static Blockchain instance;

    private String fileName;
    private long nbOfZeros = 0L;
    private long dataId = 1L;
    private long minerRewardAmount = 100L;
    private final double DURATION_LOWER_BOUND = 0.1; //seconds
    private final double DURATION_HIGHER_BOUND = 3; //seconds
    private final List<Block> blocks = new ArrayList<>();
    private final List<Transaction> newTransactionsA = new ArrayList<>();
    private final List<Transaction> newTransactionsB = new ArrayList<>();
    /**
     * List which receives the latest transactions
     */
    private List<Transaction> newTransactionsCurrent = newTransactionsA;

    private Blockchain() {
    }

    public static Blockchain getInstance() {
        if (instance == null) {
            instance = new Blockchain();
        }
        return instance;
    }

    public static Blockchain resetInstance() {
        instance = new Blockchain();
        return instance;
    }

    // To fix Singleton deserialize issue.
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        instance = this;
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    public long getNbOfZeros() {
        return nbOfZeros;
    }

    public long getMinerRewardAmount() {
        return minerRewardAmount;
    }

    public List<Transaction> transactionsProcessedByMiners() {
        return new ArrayList<>(newTransactionsCurrent == newTransactionsA ? newTransactionsB : newTransactionsA);
    }

    public List<Transaction> allWaitingTransactions() {
        return Stream.of(newTransactionsA, newTransactionsB)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public void addTransaction(Transaction newTransaction) {
        if (BlockchainValidator.isNewTransactionIdInvalid(newTransaction, transactionsProcessedByMiners())) {
            System.out.printf("Transaction %s failed! (invalid Id)%n", newTransaction);
            return;
        }
        if (BlockchainValidator.isTransactionAmountInvalid(newTransaction, allWaitingTransactions())) {
            System.out.printf("Transaction %s failed! (invalid amount)%n", newTransaction);
            return;
        }
        newTransactionsCurrent.add(newTransaction);
    }

    private void emptyTransactions() {
        if (newTransactionsCurrent == newTransactionsA) {
            newTransactionsB.clear();
            newTransactionsCurrent = newTransactionsB;
        } else {
            newTransactionsA.clear();
            newTransactionsCurrent = newTransactionsA;
        }
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getNbOfBlocks() {
        return blocks.size();
    }

    public Block getLastBlock() {
        return blocks.size() > 0 ? blocks.get(blocks.size() - 1) : null;
    }

    public void addBlock(Block block) {
        if (BlockchainValidator.isNewBlockValid(block)) {
            int update = updateNbOfZeros(block.getGenerationDuration());
            block.setChangeOfZeros(update);
            blocks.add(block);
            emptyTransactions();
            serializeBlockchain();
            System.out.printf("Added new block... (%s)%n", block.getMinerId());
        }
    }

    public long getNewDataId() {
        return dataId++;
    }

    private void serializeBlockchain() {
        try {
            SerializationUtils.serialize(this, fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int updateNbOfZeros(double generationDuration) {
        if (generationDuration < DURATION_LOWER_BOUND) {
            nbOfZeros++;
            return 1;
        }
        if (generationDuration > DURATION_HIGHER_BOUND) {
            nbOfZeros--;
            return -1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return blocks.stream()
                .skip(Math.max(0, blocks.size() - 15))
                .map(String::valueOf)
                .collect(Collectors.joining("\n"));
    }

}
