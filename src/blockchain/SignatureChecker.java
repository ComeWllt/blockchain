package blockchain;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;

public class SignatureChecker {

    public static boolean check(Transaction transaction) {
        Signature signature;
        try {
            signature = Signature.getInstance("SHA1withRSA");
            signature.initVerify(transaction.getPublicKey());
            String data = transaction.getSenderId() + transaction.getValue() + transaction.getReceiverId() +
                    transaction.getDateTime().toString() + transaction.getId();
            signature.update(data.getBytes());
            return signature.verify(transaction.getSignature());
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
        }
        return false;
    }
}
