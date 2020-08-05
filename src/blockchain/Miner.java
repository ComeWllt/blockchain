package blockchain;

import java.util.List;
import java.util.Random;

public class Miner extends BotUser implements Runnable {

    private final Blockchain blockchain;
    private final int nbOfBlocks;
    private int currentNbOfBlocks;

    public Miner(String name, Blockchain blockchain, int nbOfBlocks) {
        super(name);
        this.blockchain = blockchain;
        this.nbOfBlocks = nbOfBlocks;
    }

    private Block generateBlock(int id, String hashOfPreviousBlock, List<Transaction> transactions, long nbOfZeros, VCoin minerAward) {
        Random rand = new Random();
        int magicNumber = rand.nextInt(Integer.MAX_VALUE);
        long timestamp = System.currentTimeMillis();
        String minerId = Thread.currentThread().getName();
        String pattern = String.format("0{%s}.*", nbOfZeros);
        String hash = HashCreator.createHash(hashOfPreviousBlock, id, timestamp, magicNumber, transactions, minerId, minerAward);
        while (!hash.matches(pattern)) {
            if (blockHasBeenAdded()) return null;
            magicNumber = rand.nextInt(Integer.MAX_VALUE);
            hash = HashCreator.createHash(hashOfPreviousBlock, id, timestamp, magicNumber, transactions, minerId, minerAward);
        }
        double generationDuration = (System.currentTimeMillis() - timestamp) / 1000.0;
        return new Block(id, hashOfPreviousBlock, timestamp, magicNumber, generationDuration, transactions, minerId, minerAward);
    }

    /**
     * Checks if another miner has already added the block.
     */
    private boolean blockHasBeenAdded() {
        return blockchain.getNbOfBlocks() > currentNbOfBlocks;
    }

    @Override
    public void run() {
        currentNbOfBlocks = blockchain.getNbOfBlocks();
        while (currentNbOfBlocks < nbOfBlocks) {
            long nbOfZeros = blockchain.getNbOfZeros();
            Block lastBlock = blockchain.getLastBlock();
            List<Transaction> transactions = blockchain.transactionsProcessedByMiners();
            String previousHash = currentNbOfBlocks > 0 ? lastBlock.getHash() : "0";
            int id = currentNbOfBlocks > 0 ? lastBlock.getId() + 1 : 1;
            VCoin minerAward = new VCoin(blockchain.getMinerRewardAmount());
            Block block = generateBlock(id, previousHash, transactions, nbOfZeros, minerAward);
            if (block != null) {
                synchronized (Miner.class) {
                    blockchain.addBlock(block);
                }
            }
            currentNbOfBlocks = blockchain.getNbOfBlocks();
        }
    }
}
