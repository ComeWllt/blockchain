package blockchain;

import java.security.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class BotUser {

    private static final Map<String, BotUser> users = new HashMap<>();

    private final String name;
    private final Blockchain blockchain;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private final VCoin initialBalance = new VCoin(100L); // Will likely break if changed for already existing blockchain

    protected BotUser(String name) {
        this.name = name;
        this.blockchain = Blockchain.getInstance();
        users.put(name, this);
        createKeyPair();
    }

    public static BotUser getBotUser(String name) {
        return users.containsKey(name) ? users.get(name) : new BotUser(name);
    }

    public static Map<String, BotUser> getUsers() {
        return new HashMap<>(users);
    }

    public VCoin getInitialBalance() {
        return initialBalance;
    }

    public String getName() {
        return name;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void createKeyPair() {
        try {
            KeyPair keyPair = KeyPairCreator.createKeyPair();
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
        }
    }

    public void createTransaction(long amount, String receiver) {
        byte[] signature;
        VCoin vCoin = new VCoin(amount);
        LocalDateTime dateTime = LocalDateTime.now();
        long id = blockchain.getNewDataId();
        try {
            signature = signTransaction(name + vCoin + receiver + dateTime.toString() + id);
            Transaction transaction = new Transaction(vCoin, name, receiver, dateTime, id, signature, publicKey);
            blockchain.addTransaction(transaction);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
        }
    }

    private byte[] signTransaction(String data) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature rsa = Signature.getInstance("SHA1withRSA");
        rsa.initSign(privateKey);
        rsa.update(data.getBytes());
        return rsa.sign();
    }

    public VCoin getBalance() {
        long amountIn = blockchain.getBlocks().stream()
                .flatMap(block -> block.getTransactions().stream())
                .filter(transaction -> transaction.getReceiverId().equals(name))
                .map(transaction -> transaction.getValue().getAmount())
                .mapToLong(d -> d)
                .sum();
        long amountOut = blockchain.getBlocks().stream()
                .flatMap(block -> block.getTransactions().stream())
                .filter(transaction -> transaction.getSenderId().equals(name))
                .map(transaction -> transaction.getValue().getAmount())
                .mapToLong(d -> d)
                .sum();
        long amountReward = blockchain.getBlocks().stream()
                .filter(block -> block.getMinerId().equals(name))
                .map(block -> block.getMinerAward().getAmount())
                .mapToLong(l -> l)
                .sum();
        long totalAmount = amountIn - amountOut + amountReward + initialBalance.getAmount();
        return new VCoin(totalAmount);
    }
}
