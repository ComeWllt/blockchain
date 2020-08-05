package blockchain;

import java.util.*;
import java.util.stream.Collectors;

public class BlockchainValidator {

    public static boolean isBlockchainValid() {
        Blockchain blockchain = Blockchain.getInstance();
        List<Block> blocks = blockchain.getBlocks();

        if (isBlockchainEmpty()) {
            return true;
        }
        if (!isFirstBlockValid(blocks.get(0))) {
            return false;
        }
        for (int i = 1; i < blocks.size(); i++) {
            if (hasWrongPreviousHash(blocks.get(i), blocks.get(i - 1))) {
                return false;
            }
            if (hasOneOfTheTransactionsAnInvalidId(blocks.get(i), i - 1)) {
                return false;
            }
            if (hasOneOfTheTransactionsAnInvalidSignature(blocks.get(i))) {
                return false;
            }
        }
        if (isBlockBalanceInvalid(blocks)) {
            return false;
        }
        return true;
    }

    public static boolean isNewBlockValid(Block block) {
        if (isBlockchainEmpty()) {
            return isFirstBlockValid(block);
        }
        Block lastBlock = Blockchain.getInstance().getLastBlock();
        if (hasWrongPreviousHash(block, lastBlock)) {
            return false;
        }
        int indexOfLastBlock = Blockchain.getInstance().getNbOfBlocks() - 1;
        if (hasOneOfTheTransactionsAnInvalidId(block, indexOfLastBlock)) {
            return false;
        }
        if (hasOneOfTheTransactionsAnInvalidSignature(block)) {
            return false;
        }
        if (hasOneOfTheSenderAnInvalidTotalAmount(block)) {
            return false;
        }
        if (hasWrongNumberOfZerosInHash(block)) {
            return false;
        }
        return true;
    }

    public static boolean isTransactionAmountInvalid(Transaction transaction, List<Transaction> waitingTransactions) {
        String senderId = transaction.getSenderId();
        long waitingAmount = waitingTransactions.stream()
                .filter(t -> t.getSenderId().equals(senderId))
                .map(t -> t.getValue().getAmount())
                .mapToLong(l -> l)
                .sum();
        VCoin senderBalance = BotUser.getBotUser(senderId).getBalance();
        return senderBalance.getAmount() < (transaction.getValue().getAmount() + waitingAmount);
    }

    public static boolean isNewTransactionIdInvalid(Transaction newTransaction,
                                                    List<Transaction> transactionsProcessedByMiners) {
        long previousBlockMax = transactionsProcessedByMiners.stream()
                .map(Transaction::getId)
                .mapToLong(l -> l)
                .max()
                .orElse(0);
        return previousBlockMax >= newTransaction.getId();
    }

    private static boolean isBlockchainEmpty() {
        List<Block> blocks = Blockchain.getInstance().getBlocks();
        return blocks.size() == 0;
    }

    private static boolean isFirstBlockValid(Block block) {
        return "0".equals(block.getHashOfPreviousBlock());
    }

    private static boolean hasOneOfTheTransactionsAnInvalidId(Block block, int indexOfPreviousBlock) {
        if (indexOfPreviousBlock == 0) {
            return false;
        }
        List<Block> blocks = Blockchain.getInstance().getBlocks();
        OptionalLong previousBlockMax = blocks.get(indexOfPreviousBlock).getTransactions().stream()
                .map(Transaction::getId)
                .mapToLong(l -> l)
                .max();
        if (previousBlockMax.isPresent()) {
            return block.getTransactions().stream()
                    .anyMatch(transaction -> transaction.getId() <= previousBlockMax.getAsLong());
        }
        // Recursive check of another block up the chain if there was no transaction in the previous one.
        return hasOneOfTheTransactionsAnInvalidId(block, indexOfPreviousBlock - 1);
    }

    private static boolean hasWrongPreviousHash(Block block, Block previousBlock) {
        return !Objects.equals(previousBlock.getHash(), block.getHashOfPreviousBlock());
    }

    private static boolean hasOneOfTheTransactionsAnInvalidSignature(Block block) {
        return block.getTransactions().stream()
                .anyMatch(transaction -> !SignatureChecker.check(transaction));
    }

    private static boolean hasOneOfTheSenderAnInvalidTotalAmount(Block block) {
        Map<String, Long> totalExpensesPerSender = block.getTransactions().stream()
                .collect(Collectors.groupingBy(Transaction::getSenderId,
                        Collectors.summingLong(transaction -> transaction.getValue().getAmount())));
        return totalExpensesPerSender.entrySet().stream()
                .anyMatch(el -> BotUser.getBotUser(el.getKey()).getBalance().getAmount() < el.getValue());
    }

    private static boolean hasWrongNumberOfZerosInHash(Block block) {
        long nbOfZeros = Blockchain.getInstance().getNbOfZeros();
        String pattern = String.format("0{%s}.*", nbOfZeros);
        return !block.getHash().matches(pattern);
    }

    private static boolean isBlockBalanceInvalid(List<Block> blocks) {
        Map<String, Long> balances = new HashMap<>();
        for (Block block : blocks) {
            Map<String, Long> totalExpensesPerSender = block.getTransactions().stream()
                    .collect(Collectors.groupingBy(Transaction::getSenderId,
                            Collectors.summingLong(transaction -> transaction.getValue().getAmount())));
            Map<String, Long> totalEarningsPerSender = block.getTransactions().stream()
                    .collect(Collectors.groupingBy(Transaction::getReceiverId,
                            Collectors.summingLong(transaction -> transaction.getValue().getAmount())));
            boolean hasAnyoneOverspent = totalExpensesPerSender.entrySet().stream()
                    .anyMatch(el -> balances.getOrDefault(el.getKey(),
                            BotUser.getBotUser(el.getKey()).getInitialBalance().getAmount()) < el.getValue());
            if (hasAnyoneOverspent) {
                return true;
            }
            balances.put(block.getMinerId(), balances.getOrDefault(block.getMinerId(),
                    BotUser.getBotUser(block.getMinerId()).getInitialBalance().getAmount()) + block.getMinerAward().getAmount());
            totalEarningsPerSender.forEach(
                    (s, aLong) -> balances.put(s, balances.getOrDefault(s,
                            BotUser.getBotUser(s).getInitialBalance().getAmount()) + aLong)
            );
            totalExpensesPerSender.forEach(
                    (s, aLong) -> balances.put(s, balances.getOrDefault(s,
                            BotUser.getBotUser(s).getInitialBalance().getAmount()) - aLong)
            );
        }
        return false;
    }

}
