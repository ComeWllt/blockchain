package blockchain;

import java.util.List;
import java.util.stream.Collectors;

public class HashCreator {

    public static String createHash(
            String hashOfPreviousBlock,
            int id,
            long timestamp,
            int magicNumber,
            List<Transaction> transactions,
            String minerId,
            VCoin minerAward
    ) {
        String strTransactions = transactions.stream()
                .map(Object::toString)
                .collect(Collectors.joining("\n"));
        String data = hashOfPreviousBlock + id + timestamp + strTransactions + minerId + magicNumber + minerAward;
        return StringUtil.applySha256(data);
    }
}
