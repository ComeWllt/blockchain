package blockchain;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class TransactionSimulator implements Runnable {

    public static BotUser getRandomUser() {
        Random generator = new Random();
        Map<String, BotUser> users = BotUser.getUsers();
        String[] userList = users.keySet().toArray(new String[0]);
        return users.get(userList[generator.nextInt(userList.length)]);
    }

    @Override
    public void run() {
        Random generator = new Random();
        List.of(
                "Tom",
                "Sarah",
                "Vladimir",
                "JetBrains",
                "Shop"
        ).forEach(BotUser::getBotUser);
        while (!Thread.interrupted()) {
            try {
                TimeUnit.MILLISECONDS.sleep(generator.nextInt(2500));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            BotUser sender = getRandomUser();
            BotUser receiver = getRandomUser();
            VCoin userBalance = sender.getBalance();
            long amount = (long) (generator.nextDouble() * 2 * userBalance.getAmount());
            sender.createTransaction(amount, receiver.getName());
        }
    }
}
